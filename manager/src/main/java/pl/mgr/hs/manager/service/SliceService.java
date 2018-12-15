package pl.mgr.hs.manager.service;

import pl.mgr.hs.manager.dto.internal.NodeDto;
import pl.mgr.hs.manager.dto.rest.JoinTokenDto;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;

import java.util.List;

/** Created by dominik on 20.10.18. */
public interface SliceService {

  List<SliceListDto> getAllSlices();

  SliceDetailsDto getSlice(int id);

  void removeSlice(int id);

  void restartSlice(int id);

  void stopSlice(int id);

  void startSlice(int id);

  Integer createSlice(NewSliceForm sliceForm, boolean isNew);

  List<SliceDto> getAvailableSlicesForHost(String hostId);

  JoinTokenDto getJoinToken(String hostId, Integer sliceId);

  List<NodeDto> getAllNodesForSlice(int sliceId);

  String getNodeState(int sliceId, String nodeId);

  void removeNodeFromSlice(int sliceId, String nodeId);

  void rotateJoinToken(int sliceId);

  String getAttachCommandClientApplication(int sliceId);
}
