package pl.mgr.hs.manager.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.mgr.hs.manager.constant.Constants;
import pl.mgr.hs.manager.facade.SliceFacade;

/** Created by dominik on 19.10.18. */
@Controller
public class DashboardController {

  public static final String DASHBOARD_URL = "/";
  private final SliceFacade sliceFacade;

  @Autowired
  public DashboardController(SliceFacade sliceFacade) {
    this.sliceFacade = sliceFacade;
  }

  @GetMapping(DASHBOARD_URL)
  public String showMainPage(Model model) {
    model.addAttribute("slices", sliceFacade.getAllSlices());

    return Constants.Pages.DASHBOARD;
  }
}
