package pl.mgr.hs.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.mgr.hs.manager.service.SliceService;

/**
 * Created by dominik on 19.10.18.
 */
@Controller
public class DashboardController {

    private final SliceService sliceService;

    @Autowired
    public DashboardController(SliceService sliceService) {
        this.sliceService = sliceService;
    }


    @GetMapping("/")
    public String showMainPage(Model model) {
        model.addAttribute("slices", sliceService.getAllSlices());

        return "pages/index";
    }
}
