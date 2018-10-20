package pl.mgr.hs.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.repository.SliceRepository;

/**
 * Created by dominik on 19.10.18.
 */
@Controller
@RequestMapping("/test")
public class ListController {

    private final SliceRepository sliceRepository;

    @Autowired
    public ListController(SliceRepository sliceRepository) {
        this.sliceRepository = sliceRepository;
    }

    @GetMapping("/create")
    @ResponseBody
    public String create(@RequestParam String name) {

        Slice slice = new Slice();
        slice.setName(name);
        sliceRepository.save(slice);
        return "created";
    }

    @GetMapping("/get-all")
    @ResponseBody
    public Iterable<Slice> getSlices() {
        return sliceRepository.findAll();
    }
}
