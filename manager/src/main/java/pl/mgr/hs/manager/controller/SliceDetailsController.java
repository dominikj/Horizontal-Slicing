package pl.mgr.hs.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.mgr.hs.manager.service.SliceService;

/**
 * Created by dominik on 24.10.18.
 */
@Controller
public class SliceDetailsController {

    private final SliceService sliceService;

    @Autowired
    public SliceDetailsController(SliceService sliceService) {
        this.sliceService = sliceService;
    }

    @GetMapping("/details/{sliceId}")
    String getSliceDetails(@PathVariable("sliceId") int id, Model model) {
        model.addAttribute("sliceName", sliceService.getSlice(id).getName());

        return "pages/details";
    }
}
