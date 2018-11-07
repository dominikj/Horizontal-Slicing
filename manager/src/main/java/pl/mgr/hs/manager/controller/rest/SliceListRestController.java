package pl.mgr.hs.manager.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.mgr.hs.manager.dto.rest.SliceDto;
import pl.mgr.hs.manager.service.SliceService;

import java.util.List;

/** Created by dominik on 06.11.18. */
@RestController
@RequestMapping("/rest")
public class SliceListRestController {

  private final SliceService sliceService;

  @Autowired
  public SliceListRestController(SliceService sliceService) {
    this.sliceService = sliceService;
  }

  @GetMapping("/available-slices")
  public List<SliceDto> getAvailableSlices(@RequestParam String hostId) {
    return sliceService.getAvailableSlicesForHost(hostId);
  }
}
