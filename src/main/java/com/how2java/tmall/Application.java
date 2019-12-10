package com.how2java.tmall;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; 
@SpringBootApplication
@EnableCaching
@EnableElasticsearchRepositories(basePackages="com.how2java.tmall.es")
//dao包下的类，是没有component注解的，
//用于配置扫描Repositories所在的package及子package，个人理解是相当于把dao包和pojo包下的类都注册为bean
@EnableJpaRepositories(basePackages= {"com.how2java.tmall.dao","com.how2java.tmall.pojo"})
public class Application extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}
	
    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);    	
    }
}
