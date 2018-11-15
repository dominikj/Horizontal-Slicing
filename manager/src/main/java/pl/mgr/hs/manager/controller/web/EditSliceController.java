package pl.mgr.hs.manager.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.mgr.hs.manager.constant.Constants;
import pl.mgr.hs.manager.dto.web.details.SliceDetailsDto;
import pl.mgr.hs.manager.facade.SliceFacade;
import pl.mgr.hs.manager.form.NewSliceForm;

import javax.validation.Valid;
import java.util.List;

/** Created by dominik on 27.10.18. */
@Controller
public class EditSliceController {

  private static final int FIRST_PORT = 0;
  private static final String NEW_SLICE_URL = "/new";
  private static final String EDIT_SLICE_URL = "/edit";
  private static final String SAVE_URL = "/save";
  private final SliceFacade sliceFacade;

  @Autowired
  public EditSliceController(SliceFacade sliceFacade) {
    this.sliceFacade = sliceFacade;
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

    SliceDetailsDto slice = sliceFacade.getSlice(id);
    model.addAttribute("slice", populateForm(slice));

    return Constants.Pages.NEW;
  }

  @PostMapping(SAVE_URL)
  public String saveSlice(
      @Valid @ModelAttribute("slice") NewSliceForm sliceForm,
      BindingResult bindingResult,
      @RequestParam(required = false) boolean isNew,
      Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("isNew", isNew);
      return Constants.Pages.NEW;
    }

    Integer sliceId = sliceFacade.createSlice(sliceForm, isNew);

    return "redirect:details/" + sliceId;
  }

  private NewSliceForm populateForm(SliceDetailsDto slice) {
    NewSliceForm sliceForm = new NewSliceForm();
    sliceForm.setName(slice.getName());
    sliceForm.setId(slice.getId());
    sliceForm.setDescription(slice.getDescription());
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

    return sliceForm;
  }
}
