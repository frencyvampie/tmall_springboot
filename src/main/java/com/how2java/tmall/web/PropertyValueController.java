package com.how2java.tmall.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.PropertyValue;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.service.PropertyValueService;

@RestController
public class PropertyValueController {
	@Autowired
	PropertyValueService propertyValueService;
	@Autowired
	ProductService productService;
	
	@GetMapping("/products/{pid}/propertyValues")
	//前端提交的只有一个url，附带pid参数
	public List<PropertyValue> list(@PathVariable("pid")int pid) throws Exception{
		//获得一个pid，就等于获得一个有各种信息的Product
		Product product=productService.get(pid);
		propertyValueService.init(product);
		List<PropertyValue> propertyValues=propertyValueService.list(product);
		return propertyValues;
	}
	
	@PutMapping("/propertyValues")
	//前端提交一个不附带任何参数的url，一个bean
	public Object updata(@RequestBody PropertyValue bean) throws Exception{
		propertyValueService.update(bean);
		return bean;
	}
}
