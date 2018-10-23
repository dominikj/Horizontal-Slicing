package pl.mgr.hs.manager.converter;

import org.springframework.stereotype.Component;
import pl.mgr.hs.manager.dto.SliceDto;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.enums.DockerMachineStatus;
import pl.mgr.hs.manager.service.docker.DockerMachineService;

/**
 * Created by dominik on 20.10.18.
 */
@Component
public class DefaultSliceConverter implements GenericConverter<SliceDto, Slice> {

    private final DockerMachineService dockerMachineService;

    public DefaultSliceConverter(DockerMachineService dockerMachineService) {
        this.dockerMachineService = dockerMachineService;
    }

    @Override
    public Slice createEntity(SliceDto dto) {
        return updateEntity(new Slice(), dto);
    }

    @Override
    public SliceDto createDto(Slice entity) {
        SliceDto dto = new SliceDto();
        dto.setActiveHosts(entity.getActiveHosts());
        dto.setId(entity.getId());
        dto.setManagerHostName(entity.getManagerHostName());
        dto.setName(entity.getName());
        DockerMachineStatus machineStatus = dockerMachineService.getMachineStatus(entity.getManagerHostName());
        dto.setWorking(machineStatus.equals(DockerMachineStatus.Running));
        return dto;
    }

    @Override
    public Slice updateEntity(Slice entity, SliceDto dto) {
        //NOOP
        return null;
    }
}
