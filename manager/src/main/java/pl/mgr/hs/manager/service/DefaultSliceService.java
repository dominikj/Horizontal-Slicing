package pl.mgr.hs.manager.service;

import com.google.common.collect.Lists;
import com.spotify.docker.client.messages.swarm.Node;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.docker.util.service.virtualbox.VirtualboxService;
import pl.mgr.hs.manager.converter.GenericConverter;
import pl.mgr.hs.manager.converter.SliceListConverter;
import pl.mgr.hs.manager.dto.internal.NodeDto;
import pl.mgr.hs.manager.dto.rest.JoinTokenDto;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.entity.Application;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.form.NewSliceForm;
import pl.mgr.hs.manager.repository.SliceRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.mgr.hs.docker.util.constant.Constants.UNKNOWN_NODE_STATE;

/** Created by dominik on 20.10.18. */
@Service
public class DefaultSliceService implements SliceService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SliceService.class);

  private static final String MASTER_POSTFIX = "-master";
  private static final int MACHINE_ID_LENGTH = 20;
  private static final String SH_COMMAND = "/bin/sh";
  private static final String MANAGER_ADDRESS_VARIABLE = "${MANAGER_ADDRESS}";
  private static final String SPACE_SPLITTER = " ";
  private final SliceRepository sliceRepository;
  private final SliceListConverter sliceListConverter;
  private final GenericConverter<SliceDetailsDto, Slice> sliceDetailsConverter;
  private final DockerIntegrationService dockerIntegrationService;
  private final DockerMachineService dockerMachineService;
  private final VirtualboxService virtualboxService;

  @Autowired
  public DefaultSliceService(
      SliceRepository sliceRepository,
      SliceListConverter sliceListConverter,
      @Qualifier("detailsSliceConverter")
          GenericConverter<SliceDetailsDto, Slice> sliceDetailsConverter,
      DockerIntegrationService dockerIntegrationService,
      DockerMachineService dockerMachineService,
      VirtualboxService virtualboxService) {

    this.sliceRepository = sliceRepository;
    this.sliceListConverter = sliceListConverter;
    this.sliceDetailsConverter = sliceDetailsConverter;
    this.dockerIntegrationService = dockerIntegrationService;
    this.dockerMachineService = dockerMachineService;
    this.virtualboxService = virtualboxService;
  }

  @Override
  public List<SliceListDto> getAllSlices() {
    return sliceListConverter.createDtos(Lists.newArrayList(sliceRepository.findAll()));
  }

  @Override
  public SliceDetailsDto getSlice(int id) {
    return sliceRepository
        .findById(id)
        .map(sliceDetailsConverter::createDto)
        .orElseThrow(() -> new IllegalArgumentException("Given slice is not existing"));
  }

  @Override
  public void stopSlice(int id) {
    Slice sliceToStop = getSliceFromRepository(id);

    Optional<DockerMachineEnv> machineEnv = getMachineEnvironment(sliceToStop.getManagerHostName());

    if (machineEnv.isPresent()) {
      removeNodesInternal(machineEnv.get());
      dockerMachineService.stopMachine(sliceToStop.getManagerHostName());
    }
  }

  @Override
  public void startSlice(int id) {
    Slice sliceToStart = getSliceFromRepository(id);

    dockerMachineService.restartMachine(sliceToStart.getManagerHostName());
    dockerMachineService.regenerateCertsForMachine(sliceToStart.getManagerHostName());
    Optional<DockerMachineEnv> machineEnv =
        getMachineEnvironment(sliceToStart.getManagerHostName());

    if (machineEnv.isPresent()) {
      dockerIntegrationService.leaveSwarm(machineEnv.get());
      dockerIntegrationService.initSwarm(
          machineEnv.get(),
          dockerMachineService.getExternalIpAddress(sliceToStart.getManagerHostName()));

      Application clientApplication = sliceToStart.getClientApplication();
      dockerIntegrationService.createSliceService(
          machineEnv.get(), clientApplication.getImage(), clientApplication.getPublishedPort());

      dockerIntegrationService.restartServerContainer(machineEnv.get());
    } else {
      dockerMachineService.stopMachine(sliceToStart.getManagerHostName());
      throw new RuntimeException("Cannot start slice");
    }
  }

  @Override
  public void removeSlice(int id) {
    Slice sliceToRemove = getSliceFromRepository(id);

    dockerMachineService.removeMachine(sliceToRemove.getManagerHostName());
    sliceRepository.delete(sliceToRemove);
  }

  @Override
  public void restartSlice(int id) {
    Slice slice = getSliceFromRepository(id);

    Optional<DockerMachineEnv> machineEnv = getMachineEnvironment(slice.getManagerHostName());

    if (machineEnv.isPresent()) {
      removeNodesInternal(machineEnv.get());
      dockerMachineService.restartMachine(slice.getManagerHostName());
      dockerMachineService.regenerateCertsForMachine(slice.getManagerHostName());
      dockerIntegrationService.leaveSwarm(machineEnv.get());
      dockerIntegrationService.initSwarm(
          machineEnv.get(), dockerMachineService.getExternalIpAddress(slice.getManagerHostName()));

      Application clientApplication = slice.getClientApplication();
      dockerIntegrationService.createSliceService(
          machineEnv.get(), clientApplication.getImage(), clientApplication.getPublishedPort());

      dockerIntegrationService.restartServerContainer(machineEnv.get());
    }
  }

  @Override
  public Integer createSlice(NewSliceForm sliceForm, boolean isNew) {

    String machineName;
    Slice slice;

    if (!isNew) {
      slice = getSliceFromRepository(sliceForm.getId());
      machineName = slice.getManagerHostName();

      dockerMachineService.removeMachine(machineName);
    } else {
      // FIXME
      machineName = RandomStringUtils.randomAlphanumeric(MACHINE_ID_LENGTH) + MASTER_POSTFIX;
      slice = new Slice();
    }

    createDockerEnvironmentForSlice(machineName, sliceForm);
    return sliceRepository.save(populateSliceEntity(slice, machineName, sliceForm)).getId();
  }

  @Override
  public List<SliceDto> getAvailableSlicesForHost(String hostId) {
    LOGGER.debug("ACL is not implemented yet");

    return sliceListConverter.createAccessSliceDataDtos(
        Lists.newArrayList(sliceRepository.findAll()));
  }

  @Override
  public JoinTokenDto getJoinToken(String hostId, Integer sliceId) {
    LOGGER.debug("ACL is not implemented yet");

    Slice slice = getSliceFromRepository(sliceId);
    return sliceListConverter.createJoinTokenDto(slice);
  }

  @Override
  public List<NodeDto> getAllNodesForSlice(int sliceId) {
    Slice slice = getSliceFromRepository(sliceId);

    Optional<DockerMachineEnv> machineEnvironment =
        getMachineEnvironment(slice.getManagerHostName());

    return machineEnvironment
        .map(
            dockerMachineEnv ->
                dockerIntegrationService
                    .getNodes(dockerMachineEnv)
                    .stream()
                    .map(node -> new NodeDto(node.id(), node.status().state()))
                    .collect(Collectors.toList()))
        .orElseGet(Collections::emptyList);
  }

  @Override
  public String getNodeState(int sliceId, String nodeId) {
    return getAllNodesForSlice(sliceId)
        .stream()
        .filter(nodeDto -> nodeId.equals(nodeDto.getId()))
        .findFirst()
        .map(NodeDto::getState)
        .orElse(UNKNOWN_NODE_STATE);
  }

  @Override
  public void removeNodeFromSlice(int sliceId, String nodeId) {
    Slice slice = getSliceFromRepository(sliceId);

    DockerMachineEnv machineEnvironment =
        getMachineEnvironment(slice.getManagerHostName())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Machine environment is not available. Cannot remove node"));

    dockerIntegrationService.removeNodesFromSwarm(
        machineEnvironment, Collections.singletonList(nodeId));
  }

  @Override
  public void rotateJoinToken(int sliceId) {
    Slice slice = getSliceFromRepository(sliceId);

    getMachineEnvironment(slice.getManagerHostName())
        .ifPresent(dockerIntegrationService::rotateWorkerJoinToken);
  }

  @Override
  public String getAttachCommandClientApplication(int sliceId) {
    Slice slice = getSliceFromRepository(sliceId);
    return replaceVariablesInCommand(
        slice.getClientApplication().getCommand(), slice.getManagerHostName());
  }

  private void createDockerEnvironmentForSlice(String machineName, NewSliceForm sliceForm) {

    dockerMachineService.createNewMachine(machineName);
    dockerMachineService.stopMachine(machineName);
    virtualboxService.createBridgedAdapterForMachine(machineName);
    dockerMachineService.restartMachine(machineName);

    String externalIpAddress = dockerMachineService.getExternalIpAddress(machineName);

    Optional<DockerMachineEnv> machineEnvironment = getMachineEnvironment(machineName);
    if (machineEnvironment.isPresent()) {
      dockerIntegrationService.initSwarm(machineEnvironment.get(), externalIpAddress);

      dockerIntegrationService.createSliceService(
          machineEnvironment.get(),
          sliceForm.getClientAppImageId(),
          sliceForm.getClientAppPublishedPort());

      String processedCommand =
          replaceVariablesInCommand(sliceForm.getServerAppCommand(), machineName);
      dockerIntegrationService.createServerContainer(
          machineEnvironment.get(),
          sliceForm.getServerAppImageId(),
          sliceForm.getServerAppPublishedPort(),
          splitCommand(processedCommand));
      return;
    }

    throw new RuntimeException("Cannot create slice");
  }

  private Slice populateSliceEntity(Slice slice, String machineName, NewSliceForm sliceForm) {

    slice.setManagerHostName(machineName);
    slice.setName(sliceForm.getName());
    slice.setDescription(sliceForm.getDescription());

    Application serverApp = new Application();
    serverApp.setImage(sliceForm.getServerAppImageId());
    serverApp.setPublishedPort(sliceForm.getServerAppPublishedPort());
    serverApp.setCommand(sliceForm.getServerAppCommand());
    slice.setServerApplication(serverApp);

    Application clientApp = new Application();
    clientApp.setImage(sliceForm.getClientAppImageId());
    clientApp.setPublishedPort(sliceForm.getClientAppPublishedPort());

    if (StringUtils.isNotBlank(sliceForm.getClientAppCommand())) {
      clientApp.setCommand(sliceForm.getClientAppCommand());
    } else {
      clientApp.setCommand(SH_COMMAND);
    }
    slice.setClientApplication(clientApp);
    return slice;
  }

  private Optional<DockerMachineEnv> getMachineEnvironment(String hostName) {
    try {
      return Optional.ofNullable(dockerMachineService.getMachineEnv(hostName));
    } catch (DockerOperationException ex) {
      return Optional.empty();
    }
  }

  private Slice getSliceFromRepository(int id) {
    return sliceRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Given slice is not existing"));
  }

  private void removeNodesInternal(DockerMachineEnv env) {
    List<Node> nodes = dockerIntegrationService.getNodes(env);
    List<String> nodeToRemoveIds = nodes.stream().map(Node::id).collect(Collectors.toList());
    dockerIntegrationService.removeNodesFromSwarm(env, nodeToRemoveIds);
  }

  private String replaceVariablesInCommand(String command, String managerHostName) {
    if (StringUtils.isNotBlank(command) && command.contains(MANAGER_ADDRESS_VARIABLE)) {
      return command.replace(
          MANAGER_ADDRESS_VARIABLE, dockerMachineService.getExternalIpAddress(managerHostName));
    }
    return command;
  }

  private List<String> splitCommand(String command) {
    List<String> splittedCommand = new ArrayList<>();

    String[] splitted = command.split(SPACE_SPLITTER);
    for (int i = 0; splitted.length > i; ++i) {
      if (splitted[i].matches("^\".*")) {
        StringBuilder singleCommandAccumulator = new StringBuilder();
        do {
          singleCommandAccumulator.append(" ").append(splitted[i]);
          ++i;
        } while (splitted.length <= i || !splitted[i].matches(".*\"$"));

        splittedCommand.add(singleCommandAccumulator.toString());
        continue;
      }
      splittedCommand.add(splitted[i]);
    }
    return splittedCommand;
  }
}
