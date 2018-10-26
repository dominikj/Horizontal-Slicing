package pl.mgr.hs.manager.service;

import com.google.common.collect.Lists;
import com.spotify.docker.client.messages.swarm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.converter.GenericConverter;
import pl.mgr.hs.manager.dto.SliceListDto;
import pl.mgr.hs.manager.dto.details.SliceDetailsDto;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.repository.SliceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Created by dominik on 20.10.18. */
@Service
public class DefaultSliceService implements SliceService {

  private final SliceRepository sliceRepository;
  private final GenericConverter<SliceListDto, Slice> sliceListConverter;
  private final GenericConverter<SliceDetailsDto, Slice> sliceDetailsConverter;
  private final DockerIntegrationService dockerIntegrationService;
  private final DockerMachineService dockerMachineService;

  @Autowired
  public DefaultSliceService(
      SliceRepository sliceRepository,
      @Qualifier("dashboardSliceConverter")
          GenericConverter<SliceListDto, Slice> sliceListConverter,
      @Qualifier("detailsSliceConverter")
          GenericConverter<SliceDetailsDto, Slice> sliceDetailsConverter,
      DockerIntegrationService dockerIntegrationService,
      DockerMachineService dockerMachineService) {
    this.sliceRepository = sliceRepository;
    this.sliceListConverter = sliceListConverter;
    this.sliceDetailsConverter = sliceDetailsConverter;
    this.dockerIntegrationService = dockerIntegrationService;
    this.dockerMachineService = dockerMachineService;
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

    if (machineEnv.isPresent()) {
      List<Node> nodes = dockerIntegrationService.getNodes(machineEnv.get());
      List<String> nodeToRemoveIds = nodes.stream().map(Node::id).collect(Collectors.toList());
      dockerIntegrationService.removeNodesFromSwarm(machineEnv.get(), nodeToRemoveIds);
    }

    dockerMachineService.removeMachine(sliceToRemove.getManagerHostName());
    sliceRepository.delete(sliceToRemove);
  }

  @Override
  public void restartSlice(int id) {}

  private Optional<DockerMachineEnv> getMachineEnvironment(String hostName) {
    try {
      return Optional.ofNullable(dockerMachineService.getMachineEnv(hostName));
    } catch (DockerOperationException ex) {
      return Optional.empty();
    }
  }
}
