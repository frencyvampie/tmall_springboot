package com.how2java.tmall.web;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.OrderService;
import com.how2java.tmall.util.Page4Navigator;
import com.how2java.tmall.util.Result;

@RestController
public class OrderController {
	@Autowired
	OrderService orderService;
	@Autowired
	OrderItemService orderItemService;
	
	@GetMapping("/orders")
	public Page4Navigator<Order> list(@RequestParam(name="start",defaultValue="0")int start,
										@RequestParam(name="size",defaultValue="5")int size)throws Exception{
		start=start<0?0:start;
		Page4Navigator<Order> page=orderService.list(start, size, 5);
		//为订单计算它们的总价、总数量
		List<Order> orders=page.getContent();
		orderItemService.fill(orders);
		orderService.removeOrderFromOrderItem(orders);
		return page;
	}
	//发货，把状态从waitDelivery变为waitConfirm
	@PutMapping("/deliveryOrder/{oid}")
	public Object deliveryOrder(@PathVariable("oid")int oid) throws Exception{
		Order order=orderService.get(oid);
		order.setDeliveryDate(new Date());
		order.setStatus(OrderService.waitConfirm);
		orderService.update(order);
		return Result.success();
	}
}
