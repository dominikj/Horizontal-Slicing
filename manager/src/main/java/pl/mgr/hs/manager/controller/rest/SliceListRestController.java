package pl.mgr.hs.manager.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.mgr.hs.manager.facade.SliceFacade;
import pl.mgr.hs.manager.response.JoinDataResponse;
import pl.mgr.hs.manager.response.SliceListResponse;

/** Created by dominik on 06.11.18. */
@RestController
@RequestMapping("/rest/slice")
public class SliceListRestController {

  private final SliceFacade sliceFacade;

  @Autowired
  public SliceListRestController(SliceFacade sliceFacade) {
    this.sliceFacade = sliceFacade;
  }

  @GetMapping("/available")
  public SliceListResponse getAvailableSlices(@RequestParam String hostId) {
    return new SliceListResponse(sliceFacade.getAvailableSlicesForHost(hostId));
  }

  @GetMapping("/join-data")
  public JoinDataResponse getJoinToken(@RequestParam String hostId, @RequestParam Integer sliceId) {
    return new JoinDataResponse(
        sliceFacade.getJoinToken(hostId, sliceId),
        sliceFacade.getAttachCommandClientApplication(sliceId));
  }
}
