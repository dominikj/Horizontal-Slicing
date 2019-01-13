package pl.mgr.hs.docker.util.service.remote;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.swarm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.mgr.hs.docker.util.constant.Constants.DEFAULT_SWARM_PORT;
import static pl.mgr.hs.docker.util.constant.Constants.WORKER_ROLE;

/** Created by dominik on 24.10.18. */
public class DefaultDockerIntegrationService implements DockerIntegrationService {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultDockerIntegrationService.class);
  private static final String DEFAULT_IP = "0.0.0.0";
  private static final String DEFAULT_LISTEN_ADDR = DEFAULT_IP + ":" + DEFAULT_SWARM_PORT;
  private static final String OVERLAY_DRIVER = "overlay";
  private static final String DEFAULT_IPAM_DRIVER = "default";

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
          onlyRunningContainers ? null : ListContainersParam.allContainers());

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
  public void createSliceService(DockerMachineEnv machineEnv, ServiceDockerSpec spec) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      LOGGER.info("Creating service with image: {}....", spec.getImageId());
      ServiceSpec serviceSpecification =
          ServiceSpec.builder()
              .mode(ServiceMode.withGlobal())
              .name(spec.getServiceName())
              .networks(spec.getNetworkAttachmentConfig())
              .taskTemplate(
                  TaskSpec.builder()
                      .containerSpec(
                          ContainerSpec.builder()
                              .labels(Collections.singletonMap(spec.getServiceName(), ""))
                              .image(spec.getImageId())
                              .tty(spec.isCreateVirtualTerminal())
                              .command(spec.getCommand())
                              .build())
                      .restartPolicy(
                          RestartPolicy.builder().condition(spec.getRestartPolicy()).build())
                      .placement(
                          Placement.create(
                              Collections.singletonList(spec.getPlacementConstraint())))
                      .build())
              .endpointSpec(spec.getEndpointSpec())
              .build();

      docker.createService(serviceSpecification);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot create service on machine: %s", getHostAddress(machineEnv)), e);
    }
  }

  @Override
  public void createSliceService(ServiceDockerSpec spec) {
    createSliceService(null, spec);
  }

  @Override
  public void removeContainer(DockerMachineEnv machineEnv, String containerName) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      docker.removeContainer(containerName, DockerClient.RemoveContainerParam.forceKill());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot remove server application container on machine: %s",
              getHostAddress(machineEnv)),
          e);
    }
  }

  @Override
  public void removeContainer(String containerName) {
    removeContainer(null, containerName);
  }

  @Override
  public void restartContainer(DockerMachineEnv machineEnv, String containerName) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      docker.restartContainer(containerName);

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot restart server application container on machine: %s",
              getHostAddress(machineEnv)),
          e);
    }
  }

  @Override
  public void restartContainer(String containerName) {
    removeContainer(null, containerName);
  }

  @Override
  public void createContainer(DockerMachineEnv machineEnv, ContainerDockerSpec spec) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      LOGGER.info("Creating container with image: {}....", spec.getImageId());

      docker.pull(spec.getImageId());

      ContainerCreation container =
          docker.createContainer(
              ContainerConfig.builder()
                  .image(spec.getImageId())
                  .exposedPorts(spec.getPublishedPort())
                  .hostConfig(HostConfig.builder().portBindings(spec.getPortBindings()).build())
                  .cmd(spec.getCommand())
                  .build(),
              spec.getName());

      docker.startContainer(container.id());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format(
              "Cannot create application container on machine: %s", getHostAddress(machineEnv)),
          e);
    }
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
  public Optional<Container> getContainerForLabel(String containerlabel) {
    try (DefaultDockerClient docker = createDockerConnection(null)) {

      List<Container> containers =
          docker.listContainers(
              ListContainersParam.allContainers(), ListContainersParam.withLabel(containerlabel));
      if (containers.size() > 1) {
        throw new DockerOperationException(
            "Too many slice client application containers are obtained");
      }
      if (containers.size() == 0) {
        return Optional.empty();
      }

      return Optional.of(containers.get(0));
    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException("Cannot get container");
    }
  }

  @Override
  public boolean isSwarmRunning(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(null)) {

      return docker.info().swarm().controlAvailable();

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException("Cannot get swarm info");
    }
  }

  @Override
  public void createOverlayNetwork(DockerMachineEnv machineEnv, String subnet, String networkName) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      docker.createNetwork(
          NetworkConfig.builder()
              .name(networkName)
              .driver(OVERLAY_DRIVER)
              .ipam(
                  Ipam.builder()
                      .config(ImmutableList.of(IpamConfig.create(subnet, subnet, "")))
                      .driver(DEFAULT_IPAM_DRIVER)
                      .build())
              .build());

    } catch (DockerException | InterruptedException e) {
      throw new DockerOperationException(
          String.format("Cannot create overlay network on machine: %s", getHostAddress(machineEnv)),
          e);
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
