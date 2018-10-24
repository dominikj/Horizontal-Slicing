package pl.mgr.hs.manager.service.docker.machine;

import pl.mgr.hs.manager.enums.DockerMachineStatus;
import pl.mgr.hs.manager.service.docker.DockerMachineEnv;

/**
 * Created by dominik on 20.10.18.
 */
public interface DockerMachineService {

    DockerMachineStatus getMachineStatus(String name);

    DockerMachineEnv getMachineEnv(String name);
}
