package pl.mgr.hs.manager.converter;

import com.spotify.docker.client.messages.swarm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.dto.web.details.ApplicationDto;
import pl.mgr.hs.manager.dto.web.details.HostDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.entity.Application;
import pl.mgr.hs.manager.entity.Slice;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static pl.mgr.hs.manager.constant.Constants.ServiceIds.CLIENT_APP_SERVICE_ID;
import static pl.mgr.hs.manager.constant.Constants.ServiceIds.SERVER_APP_SERVICE_ID;
import static pl.mgr.hs.manager.constant.Constants.overlayNetwork.OVERLAY_NETWORK_MASK;

/** Created by dominik on 24.10.18. */
@Component
public class DetailsSliceConverter extends SliceConverter<SliceDetailsDto, Slice> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DetailsSliceConverter.class);

  private static final String SHUTDOWN_DESIRED_STATE = "shutdown";
  private static final int IP_ADDRESS = 0;
  private final DockerMachineService dockerMachineService;
  private final DockerIntegrationService dockerIntegrationService;

  @Autowired
  public DetailsSliceConverter(
      DockerMachineService dockerMachineService,
      DockerIntegrationService dockerIntegrationService) {
    this.dockerMachineService = dockerMachineService;
    this.dockerIntegrationService = dockerIntegrationService;
  }

  @Override
  public SliceDetailsDto createDto(Slice entity) {
    SliceDetailsDto dto = new SliceDetailsDto();
    dto.setId(entity.getId());
    dto.setManagerHostName(entity.getManagerHostName());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setManagerHostExternalPort(entity.getExternalPort());

    DockerMachineStatus machineStatus =
        dockerMachineService.getMachineStatus(entity.getManagerHostName());
    dto.setWorking(machineStatus.equals(DockerMachineStatus.Running));

    if (!dto.isWorking()) {
      LOGGER.warn("Slice {} is not working. I will use static config", entity.getName());
      return convertFromStaticConfiguration(entity, dto);
    }

    DockerMachineEnv machineEnv = dockerMachineService.getMachineEnv(entity.getManagerHostName());

    List<Node> connectedNodes = dockerIntegrationService.getNodes(machineEnv);
    List<HostDto> hosts =
        connectedNodes
            .stream()
            .map(node -> convertHost(node, machineEnv))
            .collect(Collectors.toList());
    dto.setHosts(hosts);

    dto.setManagerHostAddressInternal(machineEnv.getAddress().getHost());
    dto.setJoinToken(getJoinToken(machineEnv));
    dto.setClientApplication(
        getApplication(machineEnv, entity.getClientApplication(), CLIENT_APP_SERVICE_ID));
    dto.setServerApplication(
        getApplication(machineEnv, entity.getServerApplication(), SERVER_APP_SERVICE_ID));

    return dto;
  }

  private HostDto convertHost(Node node, DockerMachineEnv env) {
    HostDto host = new HostDto();
    host.setAddress(node.status().addr());
    host.setState(node.status().state());
    host.setName(node.description().hostname());

    List<Task> tasks =
        dockerIntegrationService
            .getTasksForNode(env, node.id())
            .stream()
            .filter(task -> !SHUTDOWN_DESIRED_STATE.equals(task.desiredState()))
            .collect(Collectors.toList());

    if (tasks.size() > 1) {
      throw new IllegalStateException(String.format("Too many tasks for node: %s", host.getName()));
    } else if (tasks.isEmpty()) {
      LOGGER.warn("No task for node {}", host.getName());
      return host;
    }

    host.setReplicationStatus(tasks.get(0).status().state());
    host.setReplicationInfo(tasks.get(0).status().message());

    return host;
  }

  private ApplicationDto getApplication(
      DockerMachineEnv env, Application entity, String serviceId) {
    List<Service> services = dockerIntegrationService.getServices(env);
    if (services.isEmpty()) {
      throw new IllegalStateException(
          String.format("No services on machine: %s", env.getAddress()));
    }
    ApplicationDto app = new ApplicationDto();
    Service service =
        services
            .stream()
            .filter(serv -> serviceId.equals(serv.spec().name()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        String.format(
                            "No service %s on machine: %s", serviceId, env.getAddress())));

    app.setImage(service.spec().taskTemplate().containerSpec().image());

    List<Integer> publishedPorts =
        service
            .endpoint()
            .spec()
            .ports()
            .stream()
            .map(PortConfig::publishedPort)
            .collect(Collectors.toList());

    app.setPublishedPorts(publishedPorts);
    app.setCommand(entity.getCommand());

    service
        .endpoint()
        .virtualIps()
        .stream()
        // FIXME
        .filter(virtualIp -> virtualIp.addr().contains(OVERLAY_NETWORK_MASK))
        .findFirst()
        .ifPresent(endpointVirtualIp -> app.setIpAddress(getOnlyIpAddress(endpointVirtualIp)));

    return app;
  }

  private String getOnlyIpAddress(EndpointVirtualIp endpointVirtualIp) {
    return endpointVirtualIp.addr().split(OVERLAY_NETWORK_MASK)[IP_ADDRESS];
  }

  private SliceDetailsDto convertFromStaticConfiguration(Slice slice, SliceDetailsDto dto) {
    ApplicationDto serverApplication = new ApplicationDto();
    serverApplication.setImage(slice.getServerApplication().getImage());
    serverApplication.setPublishedPorts(
        Collections.singletonList(slice.getServerApplication().getPublishedPort()));

    ApplicationDto clientApplication = new ApplicationDto();
    clientApplication.setImage(slice.getClientApplication().getImage());
    clientApplication.setPublishedPorts(
        Collections.singletonList(slice.getClientApplication().getPublishedPort()));

    dto.setServerApplication(serverApplication);
    dto.setClientApplication(clientApplication);
    return dto;
  }

  @Override
  protected DockerIntegrationService getDockerIntegrationService() {
    return dockerIntegrationService;
  }
}
