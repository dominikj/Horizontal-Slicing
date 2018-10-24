package pl.mgr.hs.manager.dto;

import lombok.Data;
import pl.mgr.hs.manager.service.docker.DockerMachineEnv;

/**
 * Created by dominik on 20.10.18.
 */
@Data
public class SliceDto {
    private Integer id;
    private String name;
    private boolean working;
    private int activeHosts;
    private String managerHostName;
    private DockerMachineEnv machineEnv;
}
