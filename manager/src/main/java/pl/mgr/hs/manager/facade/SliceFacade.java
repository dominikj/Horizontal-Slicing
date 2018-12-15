package pl.mgr.hs.manager.facade;

import pl.mgr.hs.manager.dto.rest.JoinTokenDto;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;

import java.util.List;

/** Created by dominik on 09.11.18. */
public interface SliceFacade {
  List<SliceListDto> getAllSlices();

  SliceDetailsDto getSlice(int id);

  void removeSlice(int id);

  void restartSlice(int id);

  void stopSlice(int id);

  void startSlice(int id);

  Integer createSlice(NewSliceForm sliceForm, boolean isNew);

  List<SliceDto> getAvailableSlicesForHost(String hostId);

  JoinTokenDto getJoinToken(String hostId, Integer sliceId);

  String getAttachCommandClientApplication(int sliceId);
}
