package com.how2java.tmall.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;

public class OtherInterceptor implements HandlerInterceptor {
	@Autowired
	OrderItemService orderItemService;
	@Autowired
	CategoryService categoryService;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		HttpSession session=request.getSession();
		User user=(User) session.getAttribute("user");
		
		int cartTotalItemNumber=0;
		if(null!=user) {
			List<OrderItem> ois=orderItemService.listByUser(user);
			for(OrderItem oi:ois) {
				cartTotalItemNumber+=oi.getNumber();
			}
		}
		
		List<Category> cs =categoryService.list();
		//返回整个应用的上下文路径字符串，在里是/tmall_springboot
		String contextPath=request.getServletContext().getContextPath();
		//设置范围
		//整个应用都可以取到cs
		request.getServletContext().setAttribute("categories_below_search", cs);
		session.setAttribute("cartTotalItemNumber", cartTotalItemNumber);
		request.getServletContext().setAttribute("contextPath", contextPath);
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
