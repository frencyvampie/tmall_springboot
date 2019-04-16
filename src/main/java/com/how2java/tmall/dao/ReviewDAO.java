package com.how2java.tmall.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.Review;

public interface ReviewDAO extends JpaRepository<Review, Integer> {
	//因为在pojo里，给属性product标注了@JoinColumn
	List<Review> findByProductOrderByIdDesc(Product product);
	int countByProduct(Product product);
}
