package pl.mgr.hs.manager.service;

import pl.mgr.hs.manager.dto.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;

/** Created by dominik on 20.10.18. */
public interface SliceService {

  Iterable getAllSlices();

  SliceDetailsDto getSlice(int id);

  void removeSlice(int id);

  void restartSlice(int id);

  Integer createSlice(NewSliceForm sliceForm);
}
