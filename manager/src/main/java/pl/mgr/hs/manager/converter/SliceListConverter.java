package pl.mgr.hs.manager.converter;

import com.spotify.docker.client.messages.swarm.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.dto.SliceListDto;
import pl.mgr.hs.manager.entity.Slice;

import java.util.List;

/** Created by dominik on 20.10.18. */
@Component
public class SliceListConverter implements GenericConverter<SliceListDto, Slice> {

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
    dto.setId(entity.getId());
    dto.setName(entity.getName());

    DockerMachineStatus machineStatus =
        dockerMachineService.getMachineStatus(entity.getManagerHostName());
    dto.setWorking(machineStatus.equals(DockerMachineStatus.Running));

    if (dto.isWorking()) {
      DockerMachineEnv machineEnv = dockerMachineService.getMachineEnv(entity.getManagerHostName());

      dto.setActiveHosts(getNumberOfActiveHosts(machineEnv));
      dto.setIpAddress(dockerMachineService.getExternalIpAddress(entity.getManagerHostName()));
    }

    return dto;
  }

  private long getNumberOfActiveHosts(DockerMachineEnv machineEnv) {
    List<Node> connectedHosts = dockerIntegrationService.getNodes(machineEnv);
    return connectedHosts
        .stream()
        .filter(node -> READY_STATUS.equals(node.status().state()))
        .count();
  }
}
