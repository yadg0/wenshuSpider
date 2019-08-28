package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
/**
 * 获取所有4级法院
 * @author yangdongjun
 *
 */
@Gecco(matchUrl="http://wenshu.court.gov.cn/Index/GetChildAllCourt",
	timeout=10000)
public class ProvinceAllCourt implements HtmlBean{
	private static Log log = LogFactory.getLog(ProvinceAllCourt.class);
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		ProvinceAllCourt article  = (ProvinceAllCourt)currSpiderBeanClass;
		String content=response.getContent();
		content=content.replaceAll("\"","").replaceAll("\\\\u0027", "\"");
		System.out.println("content=="+content);
		JSONArray provinceCourts=JSONArray.parseArray(content);
		for(Object provinceCourt:provinceCourts){
			JSONObject tmpCourt=(JSONObject)provinceCourt;
			TreeItem item=new TreeItem();
			String parentkey=tmpCourt.getString("parentkey");
			String level=tmpCourt.getString("leval");
			item.setParval(tmpCourt.getString("court"));
			item.setLevel(Integer.parseInt(level));
			item.setCode(tmpCourt.getString("key"));
			item.setParentCode(parentkey);
			ProvinceCourt.addTreeItem(item);
		}
	}
}
