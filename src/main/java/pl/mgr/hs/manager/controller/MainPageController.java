package pl.mgr.hs.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by dominik on 19.10.18.
 */
@Controller
public class MainPageController {

    @GetMapping("/")
    public String showMainPage() {
        return "index";
    }
}
