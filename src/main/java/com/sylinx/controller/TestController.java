package com.sylinx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping(value={"/", "/index"})
    public String index(){
return "index";
    }

}
