package com.how2java.tmall.service;
 
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.how2java.tmall.dao.ProductDAO;
import com.how2java.tmall.es.ProductESDAO;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.util.Page4Navigator;
import com.how2java.tmall.util.SpringContextUtil;
 
@Service
@CacheConfig(cacheNames="products")
public class ProductService  {
     
    @Autowired ProductDAO productDAO;
    @Autowired CategoryService categoryService;
    @Autowired ProductImageService productImageService;
    @Autowired OrderItemService orderItemService;
    @Autowired ReviewService reviewService;
    @Autowired ProductESDAO productESDAO;
    
    
    @CacheEvict(allEntries=true)
    public void add(Product bean) {
        productDAO.save(bean);
        //同步到es
        productESDAO.save(bean);
    }
    
    @CacheEvict(allEntries=true)
    public void delete(int id) {
        productDAO.delete(id);
        productESDAO.delete(id);
    }
    
    @Cacheable(key="'products-one-'+#p0")
    public Product get(int id) {
        return productDAO.findOne(id);
    }
    
    @CacheEvict(allEntries=true)
    public void update(Product bean) {
        productDAO.save(bean);
        productESDAO.save(bean);
    }
    
    
    @Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Product> list(int cid, int start, int size,int navigatePages) {
        Category category = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);    
        Page<Product> pageFromJPA =productDAO.findByCategory(category,pageable);
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }
 
    public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }
    //为一个目录填充好对应的产品，并将产品的第一个简单图片设置图片产品
    public void fill(Category category) {
    	/**
    	 * listByCategory()因为是同类方法，本来可以直接调用的，
    	 * 但为了触发aop拦截才能走缓存，对于缓存管理的方法的调用，采用这种方法
    	 */
    	ProductService productService=SpringContextUtil.getBean(ProductService.class);
        List<Product> products = productService.listByCategory(category);
        productImageService.setFirstProdutImages(products);
        category.setProducts(products);
    }
 
    public void fillByRow(List<Category> categorys) {
        int productNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> products =  category.getProducts();
            List<List<Product>> productsByRow =  new ArrayList<>();
            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                int size = i+productNumberEachRow;
                size= size>products.size()?products.size():size;
                List<Product> productsOfEachRow =products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }
    
    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Product> listByCategory(Category category){
        return productDAO.findByCategoryOrderById(category);
    }
    
    //实现为某个产品设置销量和评价数
    //实际上是orderItemService.getSaleCount(product)、reviewService.getCount(product)的封装，方便在Contoller层一次性调用
     public void setSaleAndReviewNumber(List<Product> products) {
    	 for(Product product:products) {
    		 setSaleAndReviewNumber(product);
    	 }
     }
     
     public void setSaleAndReviewNumber(Product product) {
    	 int saleCount=orderItemService.getSaleCount(product);
    	 product.setSaleCount(saleCount);
    	 int reviewCount=reviewService.getCount(product);
    	 product.setReviewCount(reviewCount);
     }
     //按keyword模糊查询，还附带分页
//     public List<Product> search(String keyword,int start,int size){
//    	 Sort sort=new Sort(Sort.Direction.DESC,"id");
//    	 Pageable pageable=new PageRequest(start,size,sort);
//    	 List<Product> products =productDAO.findByNameLike("%"+keyword+"%",pageable);
//    	 return products;
//     }
     
     //利用es服务进行查询，取代了数据库的模糊查询
     public List<Product> search(String keyword,int start,int size){
    	 initDatabase2ES();
    	 FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
 				.add(QueryBuilders.matchPhraseQuery("name", keyword),
 						ScoreFunctionBuilders.weightFactorFunction(100))
 				.scoreMode("sum")
 				.setMinScore(10);
 		Sort sort  = new Sort(Sort.Direction.DESC,"id");
 		Pageable pageable = new PageRequest(start, size,sort);
 		SearchQuery searchQuery = new NativeSearchQueryBuilder()
 				.withPageable(pageable)
 				.withQuery(functionScoreQueryBuilder).build();
 		Page<Product> page = productESDAO.search(searchQuery);
 		return page.getContent();
     }
     
     //初始化Mysql数据库的数据到ES服务
     public void initDatabase2ES() {
    	 Pageable pageable = new PageRequest(0, 5);
    	 Page<Product> page=productESDAO.findAll(pageable);
    	 //如果ESDAO查询的数据是空的
    	 if(page.getContent().isEmpty()) {
    		 List<Product> ps=productDAO.findAll();
    		 //将dao取到的数据添加到es
    		 for(Product p:ps) {
    			 productESDAO.save(p);
    		 }
    	 }
     }
}