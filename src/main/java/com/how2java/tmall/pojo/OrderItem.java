package com.how2java.tmall.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="orderitem")
@JsonIgnoreProperties({"handler","hibernateLazyInitializer"})
public class OrderItem {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	/**
	 * 回顾表关系设计，多个orderItem对应1个product，所以在表orderItem设计外键pid
	 * 这里用@JoinColumn映射pojo的product属性，理解为本pojo的product属性是有完整的一个product记录，pojo.product.id=xxx,是能取到值的
	 * 
	 */
	@ManyToOne
	@JoinColumn(name="pid")
	private Product product;
	
	@ManyToOne
	@JoinColumn(name="oid")
	private Order order;
	
	@ManyToOne
	@JoinColumn(name="uid")
	private User user;
	
	private int number;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
}
