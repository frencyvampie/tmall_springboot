package com.how2java.tmall.pojo;
 
import java.util.List;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
@Entity
@Table(name = "category")
/*
 * 因为是做前后端分离，而前后端数据交互用的是 json 格式。 那么 Category 对象就会被转换为 json 数据。 
 * 而本项目使用 jpa 来做实体类的持久化，jpa 默认会使用 hibernate, 在 jpa 工作过程中，就会创造代理类来继承 Category ，
 * 并添加 handler 和 hibernateLazyInitializer 这两个无须 json 化的属性，所以这里需要用 JsonIgnoreProperties 把这两个属性忽略掉。
 * 本项目的所有pojo都会有这个JsonIgnoreProerties注解
 */
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
 
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;
 
    String name;
    
    //这个products属性，由于表category是没有的，而jpa与数据库做序列化的时候，会把这个products属性也映射到表，这就出现报错，所以transient来注解忽视映射
    @Transient
    List<Product> products;
    @Transient
    List<List<Product>> productsByRow;
 
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
 
    public List<Product> getProducts() {
        return products;
    }
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    public List<List<Product>> getProductsByRow() {
        return productsByRow;
    }
    public void setProductsByRow(List<List<Product>> productsByRow) {
        this.productsByRow = productsByRow;
    }
    @Override
    public String toString() {
        return "Category [id=" + id + ", name=" + name + "]";
    }
}