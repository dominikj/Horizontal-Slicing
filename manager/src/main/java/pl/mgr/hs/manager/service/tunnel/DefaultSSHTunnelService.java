package pl.mgr.hs.manager.service.tunnel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.ssh.SSHService;
import pl.mgr.hs.manager.entity.Slice;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static pl.mgr.hs.docker.util.constant.Constants.DEFAULT_SWARM_PORT;

/** Created by dominik on 02.01.19. */
@Service
public class DefaultSSHTunnelService implements SSHTunnelService {
  private final DockerMachineService dockerMachineService;
  private final SSHService sshService;

  @Value("${local.user.name}")
  private String localUsername;

  private Map<Integer, Thread> tunnels = new ConcurrentHashMap<>();

  @Autowired
  public DefaultSSHTunnelService(DockerMachineService dockerMachineService, SSHService sshService) {
    this.dockerMachineService = dockerMachineService;
    this.sshService = sshService;
  }

  @Override
  public void createTunnelForSlice(Slice slice) {
    Optional<DockerMachineEnv> machineEnvironment =
        getMachineEnvironment(slice.getManagerHostName());

    machineEnvironment.ifPresent(
        env -> {
          Thread tunnelThread =
              sshService.createTunnelToVirtualMachine(
                  String.valueOf(slice.getExternalPort()),
                  env.getAddress().getHost(),
                  String.valueOf(DEFAULT_SWARM_PORT),
                  localUsername);

          tunnels.put(slice.getId(), tunnelThread);
        });
  }

  @Override
  public void removeTunnelForSlice(int sliceId) {
    tunnels.get(sliceId).interrupt();
    tunnels.remove(sliceId);
  }

  private Optional<DockerMachineEnv> getMachineEnvironment(String hostName) {
    try {
      return Optional.ofNullable(dockerMachineService.getMachineEnv(hostName));
    } catch (DockerOperationException ex) {
      return Optional.empty();
    }
  }
}
