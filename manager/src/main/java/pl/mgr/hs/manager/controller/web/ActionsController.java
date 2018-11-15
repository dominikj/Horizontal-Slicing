package pl.mgr.hs.manager.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import pl.mgr.hs.manager.facade.SliceFacade;

/** Created by dominik on 26.10.18. */
@Controller
@RequestMapping("/actions")
public class ActionsController {

  private final SliceFacade sliceFacade;

  @Autowired
  public ActionsController(SliceFacade sliceFacade) {
    this.sliceFacade = sliceFacade;
  }

  @GetMapping("/remove")
  public RedirectView removeSlice(@RequestParam Integer sliceId) {
    sliceFacade.removeSlice(sliceId);

    return new RedirectView(DashboardController.DASHBOARD_URL);
  }

  @GetMapping("/restart")
  public RedirectView restartSlice(@RequestParam Integer sliceId) {
    sliceFacade.restartSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }

  @GetMapping("/stop")
  public RedirectView stopSlice(@RequestParam Integer sliceId) {
    sliceFacade.stopSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }

  @GetMapping("/start")
  public RedirectView startSlice(@RequestParam Integer sliceId) {
    sliceFacade.startSlice(sliceId);

    return new RedirectView(DetailsController.DETAILS_URL + "/" + sliceId);
  }
}
