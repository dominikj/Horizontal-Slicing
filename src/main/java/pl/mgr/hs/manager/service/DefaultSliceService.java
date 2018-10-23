package pl.mgr.hs.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.mgr.hs.manager.converter.GenericConverter;
import pl.mgr.hs.manager.dto.SliceDto;
import pl.mgr.hs.manager.repository.SliceRepository;

import java.util.List;

/**
 * Created by dominik on 20.10.18.
 */
@Service
public class DefaultSliceService implements SliceService {

    private final SliceRepository sliceRepository;
    private final GenericConverter sliceConverter;

    @Autowired
    public DefaultSliceService(SliceRepository sliceRepository,
                               @Qualifier("defaultSliceConverter") GenericConverter sliceConverter) {
        this.sliceRepository = sliceRepository;
        this.sliceConverter = sliceConverter;
    }

    @Override
    public List<SliceDto> getAllSlices() {
        return sliceConverter.createDtos((List) sliceRepository.findAll());
    }
}
