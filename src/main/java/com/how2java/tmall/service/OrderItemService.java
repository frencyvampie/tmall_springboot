package com.how2java.tmall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.how2java.tmall.dao.OrderItemDAO;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="orderItems")
public class OrderItemService {
	@Autowired
	OrderItemDAO orderItemDAO;
	//ProductImageService和本Serivice属于同一个包，故不需要import
	@Autowired
	ProductImageService productImageService;
	//想想controller的要调用哪些方法以及返回类型
	//貌似没有直接调用这个Service的crud
	
	public void fill(List<Order> orders) {
		for(Order order:orders) {
			fill(order);
		}
	}
	/*
	 * 从数据库中取出来的 Order 是没有 OrderItem集合的，
	 * 这里通过 OrderItemService取出来再放在 Order的 orderItems属性上。
		除此之外，还计算订单总数，总金额等等信息
		总结：在OrderController中，调用此方法是为了能取出订单的总价、总数量，方便视图层的展示
	 */
	public void fill(Order order) {
		OrderItemService orderItemService=SpringContextUtil.getBean(OrderItemService.class);
		List<OrderItem> orderItems=orderItemService.listByOrder(order);
		float total=0;
		int totalNumber=0;
		for(OrderItem oi:orderItems) {
			//记住，一个订单项只有1种产品
			//遍历每个订单项的总价，再把每个总价叠加，就是一个订单的总价
			total+=oi.getNumber()*oi.getProduct().getPromotePrice();
			//统计了所有订单项的数量总数
			totalNumber+=oi.getNumber();
			productImageService.setFirstProductImage(oi.getProduct());
		}
		order.setTotal(total);
        order.setOrderItems(orderItems);
        order.setTotalNumber(totalNumber); 
	}
	
	@Cacheable(key="'orderItems-pid-'+#p0.id")
    public List<OrderItem> listByProduct(Product product) {
        return orderItemDAO.findByProduct(product);
    }
    
	@Cacheable(key="'orderItems-oid-'+#p0.id")
	public List<OrderItem> listByOrder(Order order){
		return orderItemDAO.findByOrderOrderByIdDesc(order);
	}
	//补写crud
	
	@CacheEvict(allEntries=true)
	public void update(OrderItem orderItem) {
		orderItemDAO.save(orderItem);
	}
	
	@CacheEvict(allEntries=true)
	public void add(OrderItem orderItem) {
		orderItemDAO.save(orderItem);
	}
	
	@Cacheable(key="'orderItems-one-'+#p0")
	public OrderItem get(int id) {
		return orderItemDAO.findOne(id);
	}
	
	@CacheEvict(allEntries=true)
	public void delete(int id) {
		orderItemDAO.delete(id);
	}
	
	//实现统计某个产品的销量
	public int getSaleCount(Product product) {
		OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
		List<OrderItem> ois=orderItemService.listByProduct(product);
		int result=0;
		for(OrderItem oi:ois) {
			if(null!=oi.getOrder())
				//为了程序的健壮性？？？
				//订单项已经转化为订单，且该订单已经付款
				if(null!=oi.getOrder() && null!=oi.getOrder().getPayDate())
					result+=oi.getNumber();
		}
		return result;
	}
	
	@Cacheable(key="'orderItems-uid-'+#p0.id")
	public List<OrderItem> listByUser(User user){
		return orderItemDAO.findByUserAndOrderIsNull(user);
	}
}
