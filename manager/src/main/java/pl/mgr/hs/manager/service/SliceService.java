package pl.mgr.hs.manager.service;

import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;

import java.util.List;

/** Created by dominik on 20.10.18. */
public interface SliceService {

  Iterable getAllSlices();

  SliceDetailsDto getSlice(int id);

  void removeSlice(int id);

  void restartSlice(int id);

  void stopSlice(int id);

  void startSlice(int id);

  Integer createSlice(NewSliceForm sliceForm, boolean isNew);

  List<SliceDto> getAvailableSlicesForHost(String hostId);
}
