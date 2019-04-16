package com.how2java.tmall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.how2java.tmall.dao.PropertyValueDAO;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.Property;
import com.how2java.tmall.pojo.PropertyValue;
import com.how2java.tmall.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="propertyValues")
public class PropertyValueService {
	@Autowired
	PropertyValueDAO propertyValueDAO;
	@Autowired
	PropertyService propertyService;
	
	@CacheEvict(allEntries=true)
	public void update(PropertyValue proertyValue) {
		propertyValueDAO.save(proertyValue);
	}
	
	@Cacheable(key="'propertyValues-pid-'+#p0.id")
	public List<PropertyValue> list(Product product){
		return propertyValueDAO.findByProductOrderByIdDesc(product);
	}
	@Cacheable(key="'propertyValues-one-ptid-'+#p0.id+'-pid-'+#p1.id")
	public PropertyValue getByPropertyAndProduct(Property property, Product product) {
		return propertyValueDAO.getByPropertyAndProduct(property, product);
	}
	
	/*
	 * 初始化:因为对于PropertyValue的管理，没有增加，只有修改。 
	 * 所以需要通过初始化来进行自动地增加，以便于后面的修改
	 */
	public void init(Product product) {
		List<Property> propertys=propertyService.findByCategory(product.getCategory());
		PropertyValueService propertyValueService = SpringContextUtil.getBean(PropertyValueService.class);
		for(Property property:propertys) {
			//通过产品和属性，查询出的特定属性值，这很符合常理，另外由于可能没有查出，所以调用这个init方法时，要抛出异常
			PropertyValue propertyValue=propertyValueService.getByPropertyAndProduct(property, product);
			//如果没有查询出这个特定属性值，就手动创建，并且添加到表propetyvalue
			if(null==propertyValue) {
				propertyValue=new PropertyValue();
				propertyValue.setProduct(product);
				propertyValue.setProperty(property);
				propertyValueDAO.save(propertyValue);
			}
		}
	}
}
