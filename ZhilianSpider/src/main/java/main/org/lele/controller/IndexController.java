package main.org.lele.controller;

import java.util.ArrayList;
import java.util.List;

import main.org.lele.model.Item;
import main.org.lele.service.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class IndexController {
	
	@Autowired
	private ItemService itemService ;
	
	@RequestMapping(value="/index",method=RequestMethod.GET)  
    public ModelAndView index(Model model){
		ModelAndView mav = new ModelAndView();
		List<Item> itemList = new ArrayList<Item>();
		itemList = itemService.findAllJobOffers();
		if(itemList != null){
			mav.addObject("offerList", itemList);
			mav.setViewName("index");
		}else{
			mav.setViewName("error");
		}
		return mav; 
    }
}
