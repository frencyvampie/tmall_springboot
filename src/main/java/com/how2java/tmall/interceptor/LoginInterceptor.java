package com.how2java.tmall.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.how2java.tmall.pojo.User;

public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {		
		String[] requireAuthPages = new String[]{
                "buy",
                "alipay",
                "payed",
                "cart",
                "bought",
                "confirmPay",
                "orderConfirmed",
                 
                "forebuyone",
                "forebuy",
                "foreaddCart",
                "forecart",
                "forechangeOrderItem",
                "foredeleteOrderItem",
                "forecreateOrder",
                "forepayed",
                "forebought",
                "foreconfirmPay",
                "foreorderConfirmed",
                "foredeleteOrder",
                "forereview",
                "foredoreview"                 
        };
		
		HttpSession session=request.getSession();
		//输出"/项目名称"
		String contextPath=session.getServletContext().getContextPath();
		//输出"/项目名称/资源名称"
		String uri=request.getRequestURI();
		//输出"资源名称"
		uri=StringUtils.remove(uri, contextPath+"/");
		String page=uri;
		if(begingWith(page,requireAuthPages)) {
			//进行登录
			Subject subject=SecurityUtils.getSubject();
			if(!subject.isAuthenticated()) {
				response.sendRedirect("login");
                return false;
			}
		}
		return true;
	}
	//如果传入的资源是需要的列表之一的，则返回ture
	private boolean begingWith(String page, String[] requiredAuthPages) {
		boolean result=false;
		for(String requiredAuthPage:requiredAuthPages) {
			if(StringUtils.startsWith(page, requiredAuthPage)) {
				result=true;
				break;
			}
		}
		return result;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
