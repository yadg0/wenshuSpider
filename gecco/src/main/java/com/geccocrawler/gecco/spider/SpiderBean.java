package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;

/**
 * 需要渲染的bean的基础接口
 * 
 * @author huchengyi
 *
 */
public interface SpiderBean extends java.io.Serializable {
	
	/**
	 * ydj增加自定义处理函数
	 * @param currSpiderBeanClass
	 * @param context 
	 * @param request
	 * @param response
	 */
	void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context, HttpRequest request, HttpResponse response);

}
