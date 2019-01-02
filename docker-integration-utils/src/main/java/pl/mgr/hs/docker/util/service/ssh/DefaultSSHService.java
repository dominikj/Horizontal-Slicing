package pl.mgr.hs.docker.util.service.ssh;

import pl.mgr.hs.docker.util.exception.SSHOpertationException;
import pl.mgr.hs.docker.util.service.CliExecutorService;

import java.util.List;

/** Created by dominik on 01.01.19. */
public class DefaultSSHService extends CliExecutorService implements SSHService {

  private static final String SSH_REMOTE_TUNNEL_COMMAND_NOOP_BACKGROUND_FLAGS =
      "ssh -q -NR %s:%s:%s %s@localhost";

  @Override
  public Thread createTunnelToVirtualMachine(
      String externalPort, String machineIpAddress, String machinePort, String localUserName) {

    Thread.UncaughtExceptionHandler handler =
        (th, ex) -> {
          throw new SSHOpertationException(
              String.format(
                  "Cannot create tunnel to machine: %s:%s from external port: %s",
                  machineIpAddress, machinePort, externalPort),
              ex);
        };

    Thread thread =
        executeCommandAsync(
            String.format(
                SSH_REMOTE_TUNNEL_COMMAND_NOOP_BACKGROUND_FLAGS,
                externalPort,
                machineIpAddress,
                machinePort,
                localUserName),
            this::createResultForCreateTunnel,
            handler);

    return thread;
  }

  private Result createResultForCreateTunnel(List<String> commandOutput) {
    if (commandOutput.isEmpty()) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }
}
