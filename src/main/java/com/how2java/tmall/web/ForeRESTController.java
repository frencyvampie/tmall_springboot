package com.how2java.tmall.web;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.how2java.tmall.comparator.ProductAllComparator;
import com.how2java.tmall.comparator.ProductDateComparator;
import com.how2java.tmall.comparator.ProductPriceComparator;
import com.how2java.tmall.comparator.ProductReviewComparator;
import com.how2java.tmall.comparator.ProductSaleCountComparator;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductImage;
import com.how2java.tmall.pojo.PropertyValue;
import com.how2java.tmall.pojo.Review;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.OrderService;
import com.how2java.tmall.service.ProductImageService;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.service.PropertyValueService;
import com.how2java.tmall.service.ReviewService;
import com.how2java.tmall.service.UserService;
import com.how2java.tmall.util.Result;
 
@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;
 
    @GetMapping("/forehome")
    public Object home() {
    	//查询所有的分类
        List<Category> cs= categoryService.list();
        //为分类对象下的product属性填充值
        productService.fill(cs);
        productService.fillByRow(cs);
        //将产品对象下的cateogry属性设置为null，这是为了遍历category集合时候，不会陷入对某个category.product的属性再次遍历
        categoryService.removeCategoryFromProduct(cs);
        return cs;
    }
    
    @PostMapping("/foreregister")
    public Object register(@RequestBody User user) {
        String name =  user.getName();
        String password = user.getPassword();
        name = HtmlUtils.htmlEscape(name);
        //这个user对象，本来已经属性name已经是有值的了，转义之后，再次设置回去
        user.setName(name);
        //判定提交的名字与数据库是否有重复
        boolean exist=userService.isExist(name);
        
        if(exist) {
        	String message ="用户名已经被使用,不能使用";
        	//观看Result类的public static Result fail(String message)
        	return Result.fail(message);
        }
        //逻辑到这里，就表示不存在数据库的name和前端提交的name一样的情况
        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        
        String encodePassword=new SimpleHash("md5", password, salt, 2).toString();
        
        user.setSalt(salt);
        user.setPassword(encodePassword);
        userService.add(user);
        
        return Result.success();
    }
    
    //登录
    @PostMapping("/forelogin")
    public Object login(@RequestBody User bean,HttpSession session) {
    	String name=bean.getName();
    	//转义
    	name = HtmlUtils.htmlEscape(name);
    	//使用shiro进行验证登录
    	//获得subejct对象，注意SecurityUtils的源码，有个静态变量SecurityManger，它在shiroConfig里面已经配置好的额
    	Subject subject=SecurityUtils.getSubject();
    	UsernamePasswordToken token=new UsernamePasswordToken(name,bean.getPassword());
    	try {
    		//登录，将User对象设置进一次session里面备用
    		subject.login(token);
    		User user=userService.getByName(name);
    		session.setAttribute("user", user);
    		return Result.success();
    		//以异常的方式报告登录错误
    	}catch(AuthenticationException e) {
    		String message ="账号密码错误";
            return Result.fail(message);
    	}
    }
    
    //响应productPage页面的请求
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid")int pid) {
    	Product product=productService.get(pid);
    	//获取产品的图片，并把图片设置回产品上
    	List<ProductImage> productSingleImages=productImageService.listSingleProductImages(product);
    	List<ProductImage> productDetailImages=productImageService.listDetailProductImages(product);
    	product.setProductSingleImages(productSingleImages);
    	product.setProductDetailImages(productDetailImages);
    	
    	List<PropertyValue> pvs=propertyValueService.list(product);
    	List<Review> reviews=reviewService.list(product);
    	//为产品设置销量和评价数
    	productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProductImage(product);
        
    	Map<String,Object> map= new HashMap<>(); 
    	map.put("product", product);
    	map.put("pvs", pvs);
    	map.put("reviews", reviews);
    	
    	return Result.success(map);
    }
    
    //在产品页面的增加购物车/立即购买链接上
    @GetMapping("forecheckLogin")
    public Object checkLogin() {
    	//shiro的Subject对象
        Subject subject=SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            return Result.success();
        return Result.fail("未登录");
    }
    
    //响应categoryPage的请求
    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable("cid")int cid,@RequestParam(name="sort")String sort) {
    	Category c=categoryService.get(cid);
    	
    	/**这2个方法封装了一些其他逻辑方法的，看回其实现的细节
    	/*返回给web前端的java对象Category c,经过json转换，是需要有对应的产品、产品的第一个图片
    	还需要将这个类下的产品的category属性设置为null
    	*/
    	
    	//为分类c对象的product属性设置值
    	productService.fill(c);
    	//为每个产品设置销量和评价量
    	productService.setSaleAndReviewNumber(c.getProducts());
    	//因为本函数的对象c要序列化，为了避免无穷遍历c.product.category.prdouct这个链，得将产品的category属性设置为null
    	categoryService.removeCategoryFromProduct(c);
    	
    	//对c的产品进行排序
    	if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;
     
                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;
     
                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;
     
                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }
    	return c;
    }
    
    @GetMapping("/foresearch")
    public Object search(@RequestParam(name="keyword")String keyword) {
    	//这个判定有啥用？保证后面对keyword的使用，不会抛出nullpointerexception
    	if(null==keyword)
            keyword = "";
    	List<Product> ps=productService.search(keyword, 0, 20);
    	productImageService.setFirstProdutImages(ps);
    	productService.setSaleAndReviewNumber(ps);
    	return ps;
    }
    
    //在某个产品页面上，立即购买
    @GetMapping("forebuyone")
    public Object buyOne(@RequestParam("pid")int pid,
    					@RequestParam("num")int num,
    					HttpSession session) {
    	 return buyoneAndAddCart(pid,num,session);
    }
    
    //在某个产品页面上，增加到购物车
    @GetMapping("foreaddCart")
    public Object addCart(@RequestParam(name="pid") int pid,
    					@RequestParam(name="num")int num,
    					HttpSession session) {
    	buyoneAndAddCart(pid,num,session);
    	//回看$(".addCartLink").click(function()，它只要求返回Result
    	return Result.success();
    }
    
 /*
1. 获取参数pid
2. 获取参数num
3. 根据pid获取产品对象p
4. 从session中获取用户对象user

接下来就是新增订单项OrderItem， 新增订单项要考虑两个情况
a. 如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
a.1 基于用户对象user，查询没有生成订单的订单项集合
a.2 遍历这个集合
a.3 如果产品是一样的话，就进行数量追加
a.4 获取这个订单项的 id

b. 如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
b.1 生成新的订单项
b.2 设置数量，用户和产品
b.3 插入到数据库
b.4 获取这个订单项的 id

5.返回当前订单项id
6. 在页面上，拿到这个订单项id，就跳转到 location.href="buy?oiid="+oiid;
*/
    /**
     * 这里是为了创建一个orderItem对象，并写入数据库
     * @param pid
     * @param num
     * @param session
     * @return
     */
    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
    	int oiid=0;
    	Product p=productService.get(pid);
    	User user=(User) session.getAttribute("user");
    	//根据当前登录的用户，刷选orderItem集合
    	List<OrderItem> ois=orderItemService.listByUser(user);
    	boolean found=false;
    	for(OrderItem oi:ois) {
    		//登录用户的订单项oi（的产品）和购买的产品是相同的
    		if(oi.getProduct().getId()==p.getId()) {
    			//调整该订单项的产品数量
    			oi.setNumber(oi.getNumber()+num);
    			orderItemService.update(oi);
    			oiid=oi.getId();
    			found=true;
    			break;
    		}
    	}
    	//增加一个OrderItem对象到数据库
    	if(!found) {
    		OrderItem oi=new OrderItem();
    		oi.setNumber(num);
    		oi.setUser(user);
    		oi.setProduct(p);
    		orderItemService.add(oi);
    		oiid=oi.getId();
    	}
    	//想想为什么要拿到这个订单项的id：这个oiid在结算页面上，会以getURLParms("oiid")来获取一个变量
    	return oiid;
    }
    
    /**
     *  * 1. 通过字符串数组获取参数oiid
为什么这里要用字符串数组试图获取多个oiid，而不是int类型仅仅获取一个oiid? 
因为根据购物流程环节与表关系，结算页面还需要显示在购物车中选中的多条OrderItem数据，
所以为了兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid
2. 准备一个泛型是OrderItem的集合ois
3. 根据前面步骤获取的oiids，从数据库中取出OrderItem对象，并放入ois集合中
4. 累计这些ois的价格总数，赋值在total上
5. 把订单项集合放在session的属性 "ois" 上
6. 把订单集合和total 放在map里
7. 通过 Result.success 返回

     */
    @GetMapping("forebuy")
    public Object buy(@RequestParam(name="oiid")String[] oiid,HttpSession session) {
    	//该集合用来存放订单项
    	List<OrderItem> orderItems=new ArrayList<>();
    	//所有orderItem集合的总价
    	float total=0;
    	
    	for(String strid:oiid) {
    		int id=Integer.parseInt(strid);
    		//根据id属性取回orderItem对象
    		OrderItem oi=orderItemService.get(id);
    		total+=oi.getProduct().getPromotePrice()*oi.getNumber();
    		orderItems.add(oi);
    	}
    	//这里其实是为了结算页面每个订单项（产品）显示第一个图片
    	productImageService.setFirstProductImagesOnOrderItems(orderItems);
    	//把数据存在一次会话
    	session.setAttribute("ois", orderItems);
    	//为何这里个不形成order对象，并把total值设置进去呢？
    	
    	Map<String,Object> map = new HashMap<>();
    	map.put("orderItems", orderItems);
    	map.put("total", total);
    	//把集合以及总价存放在返回的结果map
    	return Result.success(map);
    }
    
    //处理查看所有购物车的请求
    @GetMapping("forecart")
    public Object cart(HttpSession session) {
    	User user=(User) session.getAttribute("user");
    	List<OrderItem> ois=orderItemService.listByUser(user);
    	productImageService.setFirstProductImagesOnOrderItems(ois);
    	//这个ois，返回前端会转化为json对象，用来渲染cartPage页面
    	return ois;
    }
    
    @GetMapping("forechangeOrderItem")
    public Object changeOrderItem(HttpSession session,
    								@RequestParam(name="pid")int pid,
    								@RequestParam(name="num")int num) {
    	User user=(User) session.getAttribute("user");
    	if(null==user) {
    		return Result.fail("未登录");
    	}
    	List<OrderItem> ois=orderItemService.listByUser(user);
    	for(OrderItem oi:ois) {
    		if(oi.getProduct().getId()==pid) {
    			oi.setNumber(num);
    			orderItemService.update(oi);
    			break;
    		}	
    	}
    	return Result.success();
    }
    
    @GetMapping("foredeleteOrderItem")
    public Object deleteOrderItem(HttpSession session,
    								@RequestParam(name="oiid")int oiid) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
    	orderItemService.delete(oiid);
    	return Result.success();
    }
    
    //接受post的order:{address:'',post:'',receiver:'',mobile:'',userMessage:''}
    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order,HttpSession session) {
        User user=(User)session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        //给前端传来的order对象设置属性
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        //将订单状态设置为waitPay
        order.setStatus(OrderService.waitPay);
        //从session取回，有何意义？
        //( 在结算功能的ForeRESTController.buy() ，订单项集合被放到了session中 )
        List<OrderItem> ois=(List<OrderItem>) session.getAttribute("ois");
        //计算前端提交的order的总价，并且将order对象序列化（写入）数据库
        float total=orderService.add(order, ois);
        
        Map<String,Object> map=new HashMap<>();
        //在orderService.add(order, ois)内，就已经有add(order)
        map.put("oid", order.getId());
        map.put("total",total);
        
    	return Result.success(map);
    }
    
    @GetMapping("forepayed")
    public Object payed(@RequestParam(name="oid")int oid) {
    	Order order=orderService.get(oid);
    	//修改订单状态为waitDelivery
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }
    
    @GetMapping("forebought")
    public Object bought(HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        List<Order> os= orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(os);
        return os;
    }
    
    @GetMapping("foreconfirmPay")
    public Object confirmPay(@RequestParam(name="oid")int oid) {
    	Order o=orderService.get(oid);
    	orderItemService.fill(o);
    	orderService.cacl(o);
    	orderService.removeOrderFromOrderItem(o);
    	return o;
    }
    
    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed(@RequestParam(name="oid")int oid) {
    	Order o=orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return Result.success();
    }
    
    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return Result.success();
    }
    
    @GetMapping("forereview")
    public Object review(@RequestParam(name="oid")int oid) {
    	Order o=orderService.get(oid);
    	orderItemService.fill(o);
    	//将订单项的order属性设置为null
    	orderService.removeOrderFromOrderItem(o);
    	//假设一个订单有多个订单项（多个产品），只评论第一个订单项（产品）
    	Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p);
        productService.setSaleAndReviewNumber(p);
        
        Map<String,Object> map = new HashMap<>();
        map.put("p", p);
        map.put("o", o);
        map.put("reviews", reviews);
        
        return Result.success(map);
    }
    
    @GetMapping("foredoreview")
    public Object doreview( HttpSession session,
    						@RequestParam(name="oid")int oid,
    						@RequestParam(name="pid")int pid,
    						@RequestParam(name="content")String content) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);
        orderService.update(o);
     
        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);
     
        User user =(User)  session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }
}