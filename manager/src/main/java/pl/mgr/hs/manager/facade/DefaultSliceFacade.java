package pl.mgr.hs.manager.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mgr.hs.manager.dto.rest.JoinTokenDto;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;
import pl.mgr.hs.manager.service.SliceService;
import pl.mgr.hs.manager.service.daemon.SlicingDaemonService;

import java.util.List;

/** Created by dominik on 09.11.18. */
@Component
public class DefaultSliceFacade implements pl.mgr.hs.manager.facade.SliceFacade {

  private final SliceService sliceService;
  private final SlicingDaemonService slicingDaemonService;

  @Autowired
  public DefaultSliceFacade(SliceService sliceService, SlicingDaemonService slicingDaemonService) {
    this.sliceService = sliceService;
    this.slicingDaemonService = slicingDaemonService;
  }

  @Override
  public List<SliceListDto> getAllSlices() {
    return sliceService.getAllSlices();
  }

  @Override
  public SliceDetailsDto getSlice(int id) {
    return sliceService.getSlice(id);
  }

  @Override
  public void removeSlice(int id) {
    sliceService.removeSlice(id);
    slicingDaemonService.removeSlice(id);
  }

  @Override
  public void restartSlice(int id) {
    slicingDaemonService.removeSlice(id);
    sliceService.restartSlice(id);
    slicingDaemonService.registerSlice(id);
  }

  @Override
  public void stopSlice(int id) {
    slicingDaemonService.removeSlice(id);
    sliceService.stopSlice(id);
  }

  @Override
  public void startSlice(int id) {
    sliceService.startSlice(id);
    slicingDaemonService.registerSlice(id);
  }

  @Override
  public Integer createSlice(NewSliceForm sliceForm, boolean isNew) {
    if (!isNew) {
      slicingDaemonService.removeSlice(sliceForm.getId());
    }
    Integer sliceId = sliceService.createSlice(sliceForm, isNew);
    slicingDaemonService.registerSlice(sliceId);
    return sliceId;
  }

  @Override
  public List<SliceDto> getAvailableSlicesForHost(String hostId) {
    return sliceService.getAvailableSlicesForHost(hostId);
  }

  @Override
  public JoinTokenDto getJoinToken(String hostId, Integer sliceId) {
    return sliceService.getJoinToken(hostId, sliceId);
  }
}
