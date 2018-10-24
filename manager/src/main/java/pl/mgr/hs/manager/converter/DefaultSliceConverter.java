package pl.mgr.hs.manager.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.manager.dto.SliceDto;
import pl.mgr.hs.manager.entity.Slice;


/**
 * Created by dominik on 20.10.18.
 */
@Component
public class DefaultSliceConverter implements GenericConverter<SliceDto, Slice> {

    private final DockerMachineService dockerMachineService;
    private final DockerIntegrationService dockerIntegrationService;

    @Autowired
    public DefaultSliceConverter(DockerMachineService dockerMachineService,
                                 DockerIntegrationService dockerIntegrationService) {
        this.dockerMachineService = dockerMachineService;
        this.dockerIntegrationService = dockerIntegrationService;
    }

    @Override
    public Slice createEntity(SliceDto dto) {
        return updateEntity(new Slice(), dto);
    }

    @Override
    public SliceDto createDto(Slice entity) {
        SliceDto dto = new SliceDto();
        dto.setId(entity.getId());
        dto.setManagerHostName(entity.getManagerHostName());
        dto.setName(entity.getName());
        DockerMachineStatus machineStatus = dockerMachineService.getMachineStatus(entity.getManagerHostName());
        dto.setWorking(machineStatus.equals(DockerMachineStatus.Running));

        if (dto.isWorking()) {
            DockerMachineEnv machineEnv = dockerMachineService.getMachineEnv(entity.getManagerHostName());
            dto.setActiveHosts(dockerIntegrationService.getConnectedHosts(machineEnv).size());
        }
        return dto;
    }

    @Override
    public Slice updateEntity(Slice entity, SliceDto dto) {
        //NOOP
        return null;
    }
}
