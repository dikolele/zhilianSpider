package main.org.lele.controller;


import main.org.lele.spider.spider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/catch")
public class CatchDataController {
	
	@RequestMapping(value="",method=RequestMethod.GET)  
    public String index(){
		spider.catchData();
		return "redirect:/index";
    }
}
