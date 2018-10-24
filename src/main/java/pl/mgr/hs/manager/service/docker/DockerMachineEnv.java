package pl.mgr.hs.manager.service.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;
import java.nio.file.Path;

/**
 * Created by dominik on 24.10.18.
 */
@Data
@AllArgsConstructor
public class DockerMachineEnv {
    private URI address;
    private Path certPath;
}

