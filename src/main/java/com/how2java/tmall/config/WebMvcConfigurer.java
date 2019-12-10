package com.how2java.tmall.config;

import com.how2java.tmall.interceptor.LoginInterceptor;
import com.how2java.tmall.interceptor.OtherInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
class WebMvcConfigurer extends WebMvcConfigurerAdapter{
	
	//生产拦截器的bean
	@Bean
	public LoginInterceptor getLoginIntercepter() {
		return new LoginInterceptor();
	}
	@Bean
	public OtherInterceptor getOtherInterceptor() {
		return new OtherInterceptor();
	}
	
	//将2个拦截器注册
	@Override
    public void addInterceptors(InterceptorRegistry registry){
		//规定拦截器适用的路径是所有输入路径
        registry.addInterceptor(getLoginIntercepter())
        .addPathPatterns("/**");  
        registry.addInterceptor(getOtherInterceptor())
        .addPathPatterns("/**");
    }
}

