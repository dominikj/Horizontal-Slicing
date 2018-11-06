package pl.mgr.hs.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import pl.mgr.hs.manager.service.SliceService;

/** Created by dominik on 26.10.18. */
@Controller
@RequestMapping("/actions")
public class ActionsController {

  private final SliceService sliceService;

  @Autowired
  public ActionsController(SliceService sliceService) {
    this.sliceService = sliceService;
  }

  @GetMapping("/remove")
  public RedirectView removeSlice(@RequestParam Integer sliceId) {
    sliceService.removeSlice(sliceId);

    return new RedirectView(DashboardController.DASHBOARD_URL);
  }

  @GetMapping("/restart")
  public RedirectView restartSlice(@RequestParam Integer sliceId) {
    sliceService.restartSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }

  @GetMapping("/stop")
  public RedirectView stopSlice(@RequestParam Integer sliceId) {
    sliceService.stopSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }

  @GetMapping("/start")
  public RedirectView startSlice(@RequestParam Integer sliceId) {
    sliceService.startSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }
}
