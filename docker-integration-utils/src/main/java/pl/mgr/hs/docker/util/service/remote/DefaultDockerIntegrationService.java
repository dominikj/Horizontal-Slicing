package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Created by dominik on 24.10.18. */
public class DefaultDockerIntegrationService implements DockerIntegrationService {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultDockerIntegrationService.class);
  private static final String WORKER_ROLE = "worker";

  @Override
  public List<Node> getNodes(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return docker.listNodes(Node.Criteria.builder().nodeRole(WORKER_ROLE).build());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get nodes list from machine {}", machineEnv.getAddress().getHost());
      return Collections.emptyList();
    }
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
  public Optional<Swarm> getSwarmConfiguration(DockerMachineEnv machineEnv) {

    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return Optional.of(docker.inspectSwarm());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get swarm configuration for : {}", machineEnv.getAddress());
      return Optional.empty();
    }
  }

  @Override
  public List<Service> getServices(DockerMachineEnv machineEnv) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {

      return docker.listServices();

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get list of services for machine: {}", machineEnv.getAddress());
      return Collections.emptyList();
    }
  }

  @Override
  public List<Container> getContainers(DockerMachineEnv machineEnv, boolean onlyRunningContainers) {
    try (DefaultDockerClient docker = createDockerConnection(machineEnv)) {
      return docker.listContainers(
          onlyRunningContainers ? null : DockerClient.ListContainersParam.allContainers());

    } catch (DockerException | InterruptedException e) {

      LOGGER.error("Cannot get list of containers for machine: {}", machineEnv.getAddress());
      return Collections.emptyList();
    }
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
                  String.format("Cannot remove node: %s from swarm", node));
            }
          });
    }
  }

  private DefaultDockerClient createDockerConnection(DockerMachineEnv machineEnv) {
    try {
      return  DefaultDockerClient.builder()
              .uri(machineEnv.getAddress())
              .dockerCertificates(new DockerCertificates(machineEnv.getCertPath())).build();
    } catch (DockerCertificateException e) {
      throw new IllegalStateException("Cannot get certs for docker machine", e);
    }
  }
}
