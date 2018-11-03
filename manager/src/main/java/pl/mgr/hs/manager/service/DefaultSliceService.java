package pl.mgr.hs.manager.service;

import com.google.common.collect.Lists;
import com.spotify.docker.client.messages.swarm.Node;
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
import pl.mgr.hs.manager.dto.SliceListDto;
import pl.mgr.hs.manager.dto.details.SliceDetailsDto;
import pl.mgr.hs.manager.entity.Application;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.form.NewSliceForm;
import pl.mgr.hs.manager.repository.SliceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Created by dominik on 20.10.18. */
@Service
public class DefaultSliceService implements SliceService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSliceService.class);
  private static final String MASTER_POSTFIX = "-master";
  private final SliceRepository sliceRepository;
  private final GenericConverter<SliceListDto, Slice> sliceListConverter;
  private final GenericConverter<SliceDetailsDto, Slice> sliceDetailsConverter;
  private final DockerIntegrationService dockerIntegrationService;
  private final DockerMachineService dockerMachineService;
  private final VirtualboxService virtualboxService;

  @Autowired
  public DefaultSliceService(
      SliceRepository sliceRepository,
      @Qualifier("sliceListConverter")
          GenericConverter<SliceListDto, Slice> sliceListConverter,
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
  public Iterable getAllSlices() {
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
  public void removeSlice(int id) {
    Slice sliceToRemove =
        sliceRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Given slice is not existing"));

    Optional<DockerMachineEnv> machineEnv =
        getMachineEnvironment(sliceToRemove.getManagerHostName());

    machineEnv.ifPresent(this::removeNodesInternal);

    dockerMachineService.removeMachine(sliceToRemove.getManagerHostName());
    sliceRepository.delete(sliceToRemove);
  }

  @Override
  public void restartSlice(int id) {
    Slice slice =
        sliceRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Given slice is not existing"));

    Optional<DockerMachineEnv> machineEnv = getMachineEnvironment(slice.getManagerHostName());

    if (machineEnv.isPresent()) {
      removeNodesInternal(machineEnv.get());
      dockerMachineService.restartMachine(slice.getManagerHostName());
      dockerMachineService.regenerateCertsForMachine(slice.getManagerHostName());
      dockerIntegrationService.leaveSwarm(machineEnv.get());
      dockerIntegrationService.initSwarm(machineEnv.get());

      Application clientApplication = slice.getClientApplication();
      dockerIntegrationService.createSliceService(
          machineEnv.get(), clientApplication.getImage(), clientApplication.getPublishedPort());

      dockerIntegrationService.restartServerContainer(machineEnv.get());
    }
  }

  @Override
  public Integer createSlice(NewSliceForm sliceForm) {

    String machineName = sliceForm.getName().replaceAll("\\s+", "-") + MASTER_POSTFIX;

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

      dockerIntegrationService.createServerContainer(
          machineEnvironment.get(),
          sliceForm.getServerAppImageId(),
          sliceForm.getServerAppPublishedPort());

      Slice slice = new Slice();
      slice.setManagerHostName(machineName);
      slice.setName(sliceForm.getName());

      Application serverApp = new Application();
      serverApp.setImage(sliceForm.getServerAppImageId());
      serverApp.setPublishedPort(sliceForm.getServerAppPublishedPort());
      slice.setServerApplication(serverApp);

      Application clientApp = new Application();
      clientApp.setImage(sliceForm.getClientAppImageId());
      clientApp.setPublishedPort(sliceForm.getClientAppPublishedPort());
      slice.setClientApplication(clientApp);

      return sliceRepository.save(slice).getId();
    }
    return 0;
  }

  private Optional<DockerMachineEnv> getMachineEnvironment(String hostName) {
    try {
      return Optional.ofNullable(dockerMachineService.getMachineEnv(hostName));
    } catch (DockerOperationException ex) {
      return Optional.empty();
    }
  }

  private void removeNodesInternal(DockerMachineEnv env) {
    List<Node> nodes = dockerIntegrationService.getNodes(env);
    List<String> nodeToRemoveIds = nodes.stream().map(Node::id).collect(Collectors.toList());
    dockerIntegrationService.removeNodesFromSwarm(env, nodeToRemoveIds);
  }
}
