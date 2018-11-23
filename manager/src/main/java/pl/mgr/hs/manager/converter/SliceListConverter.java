package pl.mgr.hs.manager.converter;

import com.spotify.docker.client.messages.swarm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.dto.rest.JoinTokenDto;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.entity.Slice;

import java.util.List;
import java.util.stream.Collectors;

import static pl.mgr.hs.docker.util.constant.Constants.DEFAULT_SWARM_PORT;

/** Created by dominik on 20.10.18. */
@Component
public class SliceListConverter extends SliceConverter<SliceListDto, Slice> {

  private static final String READY_STATUS = "ready";
  private final DockerMachineService dockerMachineService;
  private final DockerIntegrationService dockerIntegrationService;

  @Autowired
  public SliceListConverter(
      DockerMachineService dockerMachineService,
      DockerIntegrationService dockerIntegrationService) {
    this.dockerMachineService = dockerMachineService;
    this.dockerIntegrationService = dockerIntegrationService;
  }

  @Override
  public SliceListDto createDto(Slice entity) {
    SliceListDto dto = new SliceListDto();

    dto.setDescription(entity.getDescription());
    dto.setId(entity.getId());
    dto.setName(entity.getName());

    dto.setWorking(machineIsWorking(entity.getManagerHostName()));

    if (dto.isWorking()) {
      DockerMachineEnv machineEnv = dockerMachineService.getMachineEnv(entity.getManagerHostName());

      dto.setActiveHosts(getNumberOfActiveHosts(machineEnv));
      dto.setIpAddress(dockerMachineService.getExternalIpAddress(entity.getManagerHostName()));
    }

    return dto;
  }

  public List<SliceDto> createAccessSliceDataDtos(List<Slice> slices) {
    return slices
        .stream()
        .filter(slice -> machineIsWorking(slice.getManagerHostName()))
        .map(this::createAccessDto)
        .collect(Collectors.toList());
  }

  public JoinTokenDto createJoinTokenDto(Slice slice) {
    JoinTokenDto dto = new JoinTokenDto();

    dto.setToken(getJoinToken(dockerMachineService.getMachineEnv(slice.getManagerHostName())));
    dto.setPort(DEFAULT_SWARM_PORT);
    dto.setIpAddress(dockerMachineService.getExternalIpAddress(slice.getManagerHostName()));
    return dto;
  }

  private long getNumberOfActiveHosts(DockerMachineEnv machineEnv) {
    List<Node> connectedHosts = dockerIntegrationService.getNodes(machineEnv);
    return connectedHosts
        .stream()
        .filter(node -> READY_STATUS.equals(node.status().state()))
        .count();
  }

  private SliceDto createAccessDto(Slice slice) {
    SliceDto dto = new SliceDto();
    dto.setName(slice.getName());
    dto.setId(slice.getId());
    dto.setDescription(slice.getDescription());
    return dto;
  }

  private boolean machineIsWorking(String managerHostName) {
    return DockerMachineStatus.Running.equals(
        dockerMachineService.getMachineStatus(managerHostName));
  }

  @Override
  protected DockerIntegrationService getDockerIntegrationService() {
    return dockerIntegrationService;
  }
}
