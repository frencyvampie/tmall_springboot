package com.how2java.tmall.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.User;

public interface OrderItemDAO extends JpaRepository<OrderItem, Integer> {
	//记住，这个OrderItemDAO的方法，一般是涉及OrderItem对象或者集合
	List<OrderItem> findByOrderOrderByIdDesc(Order order);
	//pojo上有@JoinColumn(name="pid")
	List<OrderItem> findByProduct(Product product);
	
	List<OrderItem> findByUserAndOrderIsNull(User user);
	
}
