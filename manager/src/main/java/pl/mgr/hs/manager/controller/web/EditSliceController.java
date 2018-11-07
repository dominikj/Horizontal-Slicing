package pl.mgr.hs.manager.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.mgr.hs.manager.constant.Constants;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.form.NewSliceForm;
import pl.mgr.hs.manager.service.SliceService;

import javax.validation.Valid;
import java.util.List;

/** Created by dominik on 27.10.18. */
@Controller
public class EditSliceController {

  private static final int FIRST_PORT = 0;
  private static final String NEW_SLICE_URL = "/new";
  private static final String EDIT_SLICE_URL = "/edit";
  private static final String SAVE_URL = "/save";
  private final SliceService sliceService;

  @Autowired
  public EditSliceController(SliceService sliceService) {
    this.sliceService = sliceService;
  }

  @GetMapping(NEW_SLICE_URL)
  public String newSlice(
      @ModelAttribute("slice") NewSliceForm sliceForm,
      @ModelAttribute @RequestParam boolean isNew,
      Model model) {

    model.addAttribute("isNew", isNew);
    return Constants.Pages.NEW;
  }

  @GetMapping(EDIT_SLICE_URL + "/{id}")
  public String editSlice(@PathVariable int id, Model model) {
    SliceDetailsDto slice = sliceService.getSlice(id);
    NewSliceForm sliceForm = new NewSliceForm();
    sliceForm.setName(slice.getName());
    sliceForm.setId(id);
    sliceForm.setClientAppImageId(slice.getClientApplication().getImage().split(":")[0]);

    List<Integer> clientPublishedPorts = slice.getClientApplication().getPublishedPorts();
    if (!clientPublishedPorts.isEmpty()) {
      sliceForm.setClientAppPublishedPort(clientPublishedPorts.get(FIRST_PORT));
    }

    sliceForm.setServerAppImageId(slice.getServerApplication().getImage().split(":")[0]);

    List<Integer> servPublishedPorts = slice.getServerApplication().getPublishedPorts();
    if (!servPublishedPorts.isEmpty()) {
      sliceForm.setServerAppPublishedPort(servPublishedPorts.get(FIRST_PORT));
    }

    model.addAttribute("slice", sliceForm);

    return Constants.Pages.NEW;
  }

  @PostMapping(SAVE_URL)
  public String saveSlice(
      @Valid @ModelAttribute("slice") NewSliceForm sliceForm,
      @RequestParam(required = false) boolean isNew,
      BindingResult bindingResult,
      Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("isNew", isNew);
      return Constants.Pages.NEW;
    }

    Integer sliceId = sliceService.createSlice(sliceForm, isNew);

    return "redirect:details/" + sliceId;
  }
}
