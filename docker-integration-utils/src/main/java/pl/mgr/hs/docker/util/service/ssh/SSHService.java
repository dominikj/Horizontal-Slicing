package pl.mgr.hs.docker.util.service.ssh;

/** Created by dominik on 01.01.19. */
public interface SSHService {
  Thread createTunnelToVirtualMachine(
      String externalPort, String machineIpAddress, String machinePort, String localUserName);
}
