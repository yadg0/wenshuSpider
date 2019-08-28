package com.geccocrawler.gecco.demo.jd;

import com.geccocrawler.gecco.annotation.JSONPath;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.JsonBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;

public class JDPrice implements JsonBean {
	
	private static final long serialVersionUID = -5696033709028657709L;

	@JSONPath("$.id[0]")
	private String code;
	
	@JSONPath("$.p[0]")
	private float price;
	
	@JSONPath("$.m[0]")
	private float srcPrice;

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getSrcPrice() {
		return srcPrice;
	}

	public void setSrcPrice(float srcPrice) {
		this.srcPrice = srcPrice;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context, HttpRequest request, HttpResponse response){
		
	}

}
