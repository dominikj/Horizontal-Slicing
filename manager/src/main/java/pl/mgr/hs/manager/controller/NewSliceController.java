package pl.mgr.hs.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import pl.mgr.hs.manager.form.NewSliceForm;
import pl.mgr.hs.manager.service.SliceService;

/** Created by dominik on 27.10.18. */
@Controller
@RequestMapping("/new")
public class NewSliceController {

  private final SliceService sliceService;

  @Autowired
  public NewSliceController(SliceService sliceService) {
    this.sliceService = sliceService;
  }

  @GetMapping
  public String newSlice(Model model) {
    model.addAttribute("slice", new NewSliceForm());
    return "pages/new";
  }

  @PostMapping
  public RedirectView saveSlice(@ModelAttribute("slice") NewSliceForm sliceForm) {

    Integer sliceId = sliceService.createSlice(sliceForm);

    return new RedirectView("/details/" + sliceId);
  }
}
