package com.how2java.tmall.util;
/*
 * 统一的 REST响应对象。 这个对象里包含是否成功，错误信息，以及数据。
	Result对象是一种常见的 RESTFUL 风格返回的 json 格式，里面可以有错误代码，
	错误信息和数据。 这样就比起以前那样，仅仅返回数据附加了更多的信息，
	方便前端人员识别和显示给用户可识别信息。 在前端注册，登陆等地方有更广泛的运用。
	还是看不出这个对象有啥用
 */
public class Result {
	//这个变量在整个项目都可以访问
	public static int SUCCESS_CODE=0;
	public static int FAIL_CODE = 1;
	
	int code;
	String message;
	Object data;
	
	//private???的构造器？？
    private Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    //静态方法，不用实例对象就可以调用
    public static Result success() {
    	//套用上面那个构造器
    	return new Result(SUCCESS_CODE,null,null);
    }
    
    public static Result success(Object data) {
    	return new Result(SUCCESS_CODE,"",data);
    }
    
    public static Result fail(String message) {
        return new Result(FAIL_CODE,message,null);
    }
    
    public int getCode() {
        return code;
    }
 
    public void setCode(int code) {
        this.code = code;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public Object getData() {
        return data;
    }
 
    public void setData(Object data) {
        this.data = data;
    }
}
