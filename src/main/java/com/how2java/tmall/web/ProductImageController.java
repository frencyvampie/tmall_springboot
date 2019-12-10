package com.how2java.tmall.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductImage;
import com.how2java.tmall.service.ProductImageService;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.util.ImageUtil;

@RestController
public class ProductImageController {
	@Autowired
	ProductImageService productImageService;
	@Autowired
	ProductService productService;
	
	@GetMapping("/products/{pid}/productImages")
	//因为type是附带在?之后其他参数，按照restful风格，用@RequestParam,不用@PathVariable
	public List<ProductImage> list(@RequestParam("type")String type,
									@PathVariable("pid")int pid) throws Exception{
		Product product=productService.get(pid);
		
		//如果前端请求的附带参数是single
		if(ProductImageService.type_single.equals(type)) {
			List<ProductImage> singles=productImageService.listSingleProductImages(product);
			return singles;
		}
		else if(ProductImageService.type_detail.equals(type)) {
            List<ProductImage> details =  productImageService.listDetailProductImages(product);
            return details;
		}
		else {
			//直接返回一个列表实例，但其元素均为null
			return new ArrayList<>();
		}
	}
	
	@PostMapping("/productImages")
	//因为axios.post(url,formData)，pid和type是url附带的其他参数，用@RequestParam；image对应formData.append("image",..)
    public Object add(@RequestParam("pid") int pid, 
    				@RequestParam("type") String type, 
    				MultipartFile image, 
    				HttpServletRequest request) throws Exception {
		//传参并没有一个完整的ProductImage对象，所以要自己手动创建
        ProductImage bean = new ProductImage();
        Product product = productService.get(pid);
        bean.setProduct(product);
        bean.setType(type);
        
        //将添加到数据库库
        productImageService.add(bean);
        
        /**
         * 函数剩余部分是创建文件、复制
         */
        String folder = "img/";
        if(ProductImageService.type_single.equals(bean.getType())){
        	//其实等于 String folder="img/productSingle";
            folder +="productSingle";
        }
        else{
            folder +="productDetail";
        }
        //E:\java\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\testweb\
        File  imageFolder= new File(request.getServletContext().getRealPath(folder));
        //准备好了destination的file对象，文件名字是手动创建ProductImage对象的id
        File file = new File(imageFolder,bean.getId()+".jpg");
        String fileName = file.getName();
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        try {
        	//将上传文件复制到路径.../img/productSingle下
            image.transferTo(file);
            BufferedImage img = ImageUtil.change2jpg(file);
            ImageIO.write(img, "jpg", file);           
        } catch (IOException e) {
            e.printStackTrace();
        }
        //没搞懂这2个f_small、f_middle的作用；这里是将路径.../img/productSingle的file对象转为2个尺寸，并保存在/..img/productSingle_small和productSingle_middle下
        //前端是调用哪个src？src="'img/productSingle/'+pi.id+'.jpg'"
        if(ProductImageService.type_single.equals(bean.getType())){
            String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");    
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            f_small.getParentFile().mkdirs();
            f_middle.getParentFile().mkdirs();
            ImageUtil.resizeImage(file, 56, 56, f_small);
            ImageUtil.resizeImage(file, 217, 190, f_middle);
        }      
         
        return bean;
    }
    //删除：把要删除的文件抽象为new File(imageFolder,bean.getId()+".jpg")，最后执行delete() 
    @DeleteMapping("/productImages/{id}")
    public String delete(@PathVariable("id") int id, HttpServletRequest request)  throws Exception {
        ProductImage bean = productImageService.get(id);
        productImageService.delete(id);
 
        String folder = "img/";
        if(ProductImageService.type_single.equals(bean.getType()))
            folder +="productSingle";
        else
            folder +="productDetail";
 
        File  imageFolder= new File(request.getServletContext().getRealPath(folder));
        File file = new File(imageFolder,bean.getId()+".jpg");
        String fileName = file.getName();
        file.delete();
        if(ProductImageService.type_single.equals(bean.getType())){
            String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            f_small.delete();
            f_middle.delete();
        }
 
        return null;
    }
}
