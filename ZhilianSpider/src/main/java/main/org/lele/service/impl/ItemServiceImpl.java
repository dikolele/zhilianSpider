package main.org.lele.service.impl;

import java.util.List;

import main.org.lele.mapper.ItemMapper;
import main.org.lele.model.Item;
import main.org.lele.service.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService{

	@Autowired
	private ItemMapper itemMapper ;
	
	@Override
	public List<Item> findAllJobOffers() {
		return itemMapper.findAllJobOffers();
	}

}
