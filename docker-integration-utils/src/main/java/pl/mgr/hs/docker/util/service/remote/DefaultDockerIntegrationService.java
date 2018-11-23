package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.swarm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.mgr.hs.docker.util.constant.Constants.*;

/** Created by dominik on 24.10.18. */
public class DefaultDockerIntegrationService implements DockerIntegrationService {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultDockerIntegrationService.class);
  private static final String EXCLUDE_MANAGER_PLACEMENT_CONSTRAINT = "node.role!=manager";
  private static final String LATEST_VERSION = "latest";
  private static final String DEFAULT_LISTEN_ADDR = "0.0.0.0" + ":" + DEFAULT_SWARM_PORT;
  private static final String SLICE_CLIENT_APP_KEY = "sliceClientApp";

  @Override
  public List<Node> getNodes(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return docker.listNodes(Node.Criteria.builder().nodeRole(WORKER_ROLE).build());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get nodes list from machine {}", getHostAddress(machineEnv));
      return Collections.emptyList();
    }
  }

  @Override
  public List<Node> getNodes() {
    return getNodes(null);
  }

  @Override
  public List<Task> getTasksForNode(DockerMachineEnv machineEnv, String nodeId) {

    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return docker.listTasks(Task.Criteria.builder().nodeId(nodeId).build());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get node status: {}", nodeId);
      return Collections.emptyList();
    }
  }

  @Override
  public List<Task> getTasksForNode(String nodeId) {
    return getTasksForNode(null, nodeId);
  }

  @Override
  public Optional<Swarm> getSwarmConfiguration(DockerMachineEnv machineEnv) {

    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return Optional.of(docker.inspectSwarm());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get swarm configuration for : {}", getHostAddress(machineEnv));
      return Optional.empty();
    }
  }

  @Override
  public Optional<Swarm> getSwarmConfiguration() {
    return getSwarmConfiguration(null);
  }

  @Override
  public List<Service> getServices(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return docker.listServices();

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get list of services for machine: {}", getHostAddress(machineEnv));
      return Collections.emptyList();
    }
  }

  @Override
  public List<Service> getServices() {
    return getServices(null);
  }

  @Override
  public List<Container> getContainers(DockerMachineEnv machineEnv, boolean onlyRunningContainers) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      return docker.listContainers(
          onlyRunningContainers ? null : DockerClient.ListContainersParam.allContainers());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get list of containers for machine: {}", getHostAddress(machineEnv));
      return Collections.emptyList();
    }
  }

  @Override
  public List<Container> getContainers(boolean onlyRunningContainers) {
    return getContainers(null, onlyRunningContainers);
  }

  @Override
  public void removeNodesFromSwarm(DockerMachineEnv machineEnv, List<String> nodeIds) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      nodeIds.forEach(
          node -> {
            try {
              docker.deleteNode(node, true);
            } catch (DockerException | InterruptedException e) {
              throw new DockerOperationException(
                  String.format("Cannot remove node: %s from swarm", node), e);
            }
          });
    }
  }

  @Override
  public void removeNodesFromSwarm(List<String> nodeIds) {
    removeNodesFromSwarm(null, nodeIds);
  }

  @Override
  public void joinSwarm(String joinToken, String remoteAddress) {
    try (DefaultDockerClient docker = createDockerConnection(null)) {
      docker.joinSwarm(
          SwarmJoin.builder()
              .joinToken(joinToken)
              .remoteAddrs(Collections.singletonList(remoteAddress))
              .listenAddr(DEFAULT_LISTEN_ADDR)
              .build());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot join to swarm using token: %s ", joinToken), e);
    }
  }

  @Override
  public void leaveSwarm(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      docker.leaveSwarm(true);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot leave machine: %s from swarm", getHostAddress(machineEnv)), e);
    }
  }

  @Override
  public void leaveSwarm() {
    leaveSwarm(null);
  }

  @Override
  public void initSwarm(DockerMachineEnv machineEnv, String advertiseAddress) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      LOGGER.info("Initializing swarm with advertise address: {}....", advertiseAddress);
      docker.initSwarm(
          SwarmInit.builder()
              .advertiseAddr(advertiseAddress)
              .listenAddr(DEFAULT_LISTEN_ADDR)
              .build());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot create swarm on machine: %s", getHostAddress(machineEnv)), e);
    }
  }

  @Override
  public void initSwarm(String advertiseAddress) {
    initSwarm(null, advertiseAddress);
  }

  @Override
  public void initSwarm(DockerMachineEnv machineEnv) {
    initSwarm(machineEnv, machineEnv.getAddress().getHost());
  }

  @Override
  public void createSliceService(DockerMachineEnv machineEnv, String imageId, Integer port) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      LOGGER.info("Creating service with image: {}....", imageId);
      ServiceSpec serviceSpecification =
          ServiceSpec.builder()
              .mode(ServiceMode.withGlobal())
              .name(SLICE_SERVICE_NAME)
              .taskTemplate(
                  TaskSpec.builder()
                      .containerSpec(
                          ContainerSpec.builder()
                              .labels(Collections.singletonMap(SLICE_CLIENT_APP_KEY, ""))
                              .image(imageId)
                              .build())
                      .placement(
                          Placement.create(
                              Collections.singletonList(EXCLUDE_MANAGER_PLACEMENT_CONSTRAINT)))
                      .build())
              .endpointSpec(
                  EndpointSpec.builder()
                      .addPort(
                          PortConfig.builder()
                              .publishedPort(port)
                              .targetPort(port)
                              .protocol(PortConfig.PROTOCOL_TCP)
                              .build())
                      .build())
              .build();

      docker.createService(serviceSpecification);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot create service on machine: %s", getHostAddress(machineEnv)), e);
    }
  }

  @Override
  public void createSliceService(String imageId, Integer port) {
    createSliceService(null, imageId, port);
  }

  @Override
  public void removeServerContainer(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      docker.removeContainer(SERVER_APP_ID, DockerClient.RemoveContainerParam.forceKill());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot remove server application container on machine: %s",
              getHostAddress(machineEnv)),
          e);
    }
  }

  @Override
  public void removeServerContainer() {
    removeServerContainer(null);
  }

  @Override
  public void restartServerContainer(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      docker.restartContainer(SERVER_APP_ID);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot restart server application container on machine: %s",
              getHostAddress(machineEnv)),
          e);
    }
  }

  @Override
  public void restartServerContainer() {
    removeServerContainer(null);
  }

  @Override
  public void createServerContainer(
      DockerMachineEnv machineEnv, String imageId, Integer publishedPort) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      LOGGER.info("Creating container with image: {}....", imageId);

      String fullImageId = imageId + ":" + LATEST_VERSION;

      docker.pull(fullImageId);
      docker.createContainer(
          ContainerConfig.builder()
              .image(fullImageId)
              .exposedPorts(String.valueOf(publishedPort))
              .build(),
          SERVER_APP_ID);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot create server application container on machine: %s",
              getHostAddress(machineEnv)),
          e);
    }
  }

  @Override
  public void createServerContainer(String imageId, Integer publishedPort) {
    createServerContainer(null, imageId, publishedPort);
  }

  @Override
  public void rotateWorkerJoinToken(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      docker.updateSwarm(
          docker.inspectSwarm().version().index(), true, docker.inspectSwarm().swarmSpec());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot rotate join token on machine: %s", getHostAddress(machineEnv)), e);
    }
  }

  @Override
  public void rotateWorkerJoinToken() {
    rotateWorkerJoinToken(null);
  }

  @Override
  public Optional<Container> getClientAppContainer() {
    try (DefaultDockerClient docker = createDockerConnection(null)) {

      List<Container> containers =
          docker.listContainers(DockerClient.ListContainersParam.withLabel(SLICE_CLIENT_APP_KEY));
      if (containers.size() > 1) {
        throw new DockerOperationException(
            "Too many slice client application containers are obtained");
      }
      return Optional.of(containers.get(0));
    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException("Cannot get container");
    }
  }

  private DefaultDockerClient createDockerConnection(DockerMachineEnv machineEnv) {
    try {

      if (machineEnv == null) {
        return DefaultDockerClient.fromEnv().build();
      }

      return DefaultDockerClient.builder()
          .uri(machineEnv.getAddress())
          .dockerCertificates(new DockerCertificates(machineEnv.getCertPath()))
          .build();
    } catch (DockerCertificateException e) {
      throw new IllegalStateException("Cannot get certs for docker machine", e);
    }
  }

  private String getHostAddress(DockerMachineEnv env) {
    return env == null ? "localhost" : env.getAddress().getHost();
  }
}
