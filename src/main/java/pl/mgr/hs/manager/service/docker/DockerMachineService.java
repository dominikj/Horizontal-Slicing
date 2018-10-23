package pl.mgr.hs.manager.service.docker;

import pl.mgr.hs.manager.enums.DockerMachineStatus;

/**
 * Created by dominik on 20.10.18.
 */
public interface DockerMachineService {

    DockerMachineStatus getMachineStatus(String name);

    String executeSSHCommandOnMachine(String machineName, String command, String grepRegex);
}
