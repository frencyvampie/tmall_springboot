package com.how2java.tmall.comparator;

import java.util.Comparator;

import com.how2java.tmall.pojo.Product;

public class ProductDateComparator implements Comparator<Product> {

	@Override
	public int compare(Product p1, Product p2) {
		//把 创建日期晚的放前面
		return p1.getCreateDate().compareTo(p2.getCreateDate());
	}

}
