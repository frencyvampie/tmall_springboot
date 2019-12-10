package com.how2java.tmall.service;
 
import java.util.List;
 
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
 
import com.how2java.tmall.dao.CategoryDAO;
import com.how2java.tmall.pojo.Category;
 
@Service
//指定本类下的所有方法的返回值，都保存在"categories"的缓存空间
@CacheConfig(cacheNames="categories")
public class CategoryService {
    @Autowired CategoryDAO categoryDAO;
    
    //将方法的返回对象缓存进key为categories-page-0-5
    @Cacheable(key="'categories-page-'+#p0+'-'+#p1")
    public Page4Navigator<Category> list(int start, int size, int navigatePages) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size,sort);
        Page<Category> pageFromJPA =categoryDAO.findAll(pageable);
 
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }
    
    @Cacheable(key="'categories-all'")
    public List<Category> list() {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        return categoryDAO.findAll(sort);
    }
    
    //执行本方法清空本缓存空间的所有key，也就是对象category写入数据库，然后清空所有key（这里没有用@CachePut）
    @CacheEvict(allEntries=true)
//	@CachePut(key="'category-one-'+ #p0")
    public void add(Category bean) {
        categoryDAO.save(bean);
    }
    
    @CacheEvict(allEntries=true)
    public void delete(int id) {
        categoryDAO.delete(id);
    }
    
    @Cacheable(key="'categories-one-'+#p0")
    public Category get(int id) {
        Category c= categoryDAO.findOne(id);
        return c;
    }
    
    @CacheEvict(allEntries=true)
    public void update(Category bean) {
        categoryDAO.save(bean);
    }
    /*
     * 
     */
    public void removeCategoryFromProduct(List<Category> cs) {
        for (Category category : cs) {
            removeCategoryFromProduct(category);
        }
    }
 
    public void removeCategoryFromProduct(Category category) {
        List<Product> products =category.getProducts();
        if(null!=products) {
            for (Product product : products) {
                product.setCategory(null);
            }
        }
 
        List<List<Product>> productsByRow =category.getProductsByRow();
        if(null!=productsByRow) {
            for (List<Product> ps : productsByRow) {
                for (Product p: ps) {
                    p.setCategory(null);
                }
            }
        }
    }
}