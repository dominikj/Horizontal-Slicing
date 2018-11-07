package pl.mgr.hs.manager.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mgr.hs.manager.constant.Constants;
import pl.mgr.hs.manager.service.SliceService;

/** Created by dominik on 19.10.18. */
@Controller
@RequestMapping("/list")
public class SliceListController {

  private final SliceService sliceService;

  @Autowired
  public SliceListController(SliceService sliceService) {
    this.sliceService = sliceService;
  }

  @GetMapping
  public String create(Model model) {
    model.addAttribute("slices", sliceService.getAllSlices());
    return Constants.Pages.LIST;
  }
}
