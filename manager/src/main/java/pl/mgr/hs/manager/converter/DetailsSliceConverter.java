package pl.mgr.hs.manager.converter;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.dto.details.ApplicationDto;
import pl.mgr.hs.manager.dto.details.HostDto;
import pl.mgr.hs.manager.dto.details.SliceDetailsDto;
import pl.mgr.hs.manager.entity.Slice;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pl.mgr.hs.docker.util.constant.Constants.SERVER_APP_ID;

/** Created by dominik on 24.10.18. */
@Component
public class DetailsSliceConverter implements GenericConverter<SliceDetailsDto, Slice> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DetailsSliceConverter.class);

  private static final String SERVER_APP_CONTAINER_NAME = "\\S+" + SERVER_APP_ID;
  private static final String SHUTDOWN_DESIRED_STATE = "shutdown";
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

    dto.setManagerHostAddress(machineEnv.getAddress().getHost());
    dto.setJoinToken(getJoinToken(machineEnv));
    dto.setClientApplication(getClientApplication(machineEnv));
    dto.setServerApplication(getServerApplication(machineEnv));

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

  private String getJoinToken(DockerMachineEnv env) {
    return dockerIntegrationService
        .getSwarmConfiguration(env)
        .map(swarm -> swarm.joinTokens().worker())
        .orElse("");
  }

  private ApplicationDto getClientApplication(DockerMachineEnv env) {
    List<Service> services = dockerIntegrationService.getServices(env);

    if (services.size() != 1) {
      throw new IllegalStateException(
          String.format("One service expected on machine: %s", env.getAddress()));
    }
    ApplicationDto app = new ApplicationDto();
    Service service = services.get(0);

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
    return app;
  }

  private ApplicationDto getServerApplication(DockerMachineEnv env) {
    List<Container> containers = dockerIntegrationService.getContainers(env, false);
    ApplicationDto app = new ApplicationDto();

    Predicate<String> isServerAppContainerName =
        (String name) -> name.matches(SERVER_APP_CONTAINER_NAME);
    Container serverContainer =
        containers
            .stream()
            .filter(container -> container.names().stream().anyMatch(isServerAppContainerName))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        String.format(
                            "Server application container not found on host: %s",
                            env.getAddress())));

    app.setImage(serverContainer.image());
    app.setStatus(serverContainer.status());
    List<Integer> publishedPorts =
        serverContainer
            .ports()
            .stream()
            .map((Container.PortMapping::publicPort))
            .collect(Collectors.toList());

    app.setPublishedPorts(publishedPorts);
    return app;
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
}
