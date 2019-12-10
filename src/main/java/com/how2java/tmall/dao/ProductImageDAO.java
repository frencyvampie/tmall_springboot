package com.how2java.tmall.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductImage;

public interface ProductImageDAO extends JpaRepository<ProductImage, Integer> {
	//寻找ProductImage这是实例，根据实例的product和type属性，返回一个这个实例列表
	public List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product, String type);
}
