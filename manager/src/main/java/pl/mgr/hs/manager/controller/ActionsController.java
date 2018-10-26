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

    return new RedirectView("/");
  }

  @GetMapping("/restart")
  public RedirectView restartSlice(@RequestParam Integer sliceId) {
    sliceService.removeSlice(sliceId);

    return new RedirectView("/");
  }
}
