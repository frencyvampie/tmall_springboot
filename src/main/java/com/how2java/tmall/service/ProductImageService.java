package com.how2java.tmall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.how2java.tmall.dao.ProductImageDAO;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductImage;
import com.how2java.tmall.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="productImages")
public class ProductImageService {
	public static final String type_single="single";
	public static final String type_detail="detail";
	
	@Autowired
	ProductImageDAO productImageDAO;
	
	@CacheEvict(allEntries=true)
	public void add(ProductImage bean) {
		productImageDAO.save(bean);
	}
	
	@CacheEvict(allEntries=true)
	public void delete(int id) {
		productImageDAO.delete(id);
	}
	
	@Cacheable(key="'productImages-one-'+#p0")
	public ProductImage get(int id) {
		return productImageDAO.findOne(id);
	}
	
	@Cacheable(key="'productImages-single-pid-'+#p0.id")
	public List<ProductImage> listSingleProductImages(Product product){
		return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_single);
	}
	
	@Cacheable(key="'productImages-detail-pid-'+ #p0.id")
	public List<ProductImage> listDetailProductImages(Product product){
		return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_detail);
	}
	
	public void setFirstProdutImages(List<Product> products) {
	    for (Product product : products)
	    	setFirstProductImage(product);         
	}
	//用该产品的简单图片的第一张作为产品管理页面对应产品的缩略图
	public void setFirstProductImage(Product product) {
		ProductImageService productImageService=SpringContextUtil.getBean(ProductImageService.class);
		List<ProductImage> singleImages = productImageService.listSingleProductImages(product);
		if(!singleImages.isEmpty()) {
			//这个是Product的属性
			product.setFirstProductImage(singleImages.get(0));
		}
		else
			product.setFirstProductImage(new ProductImage());
	}
	
	public void setFirstProductImagesOnOrderItems(List<OrderItem> ois) {
        for (OrderItem orderItem : ois) {
            setFirstProductImage(orderItem.getProduct());
        }
    }
}
