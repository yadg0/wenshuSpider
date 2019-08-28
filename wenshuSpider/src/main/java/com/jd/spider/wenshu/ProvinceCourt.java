package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
/**
 * 按地区获取2、3级法院
 * @author yangdongjun
 *
 */
@Gecco(matchUrl="http://wenshu.court.gov.cn/Index/GetCourt",
	timeout=10000)
public class ProvinceCourt implements HtmlBean{
	private static Log log = LogFactory.getLog(ProvinceCourt.class);
	public static List<TreeItem> allCourtTree=new ArrayList<TreeItem>();
	private static Map<String,TreeItem> treeMap=new HashMap<String,TreeItem>();
	static {
		TreeItem item = new TreeItem();
		item.setLevel(2);
		item.setParval("最高人民法院");
		allCourtTree.add(item);
	}
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		ProvinceCourt article  = (ProvinceCourt)currSpiderBeanClass;
		String content=response.getContent();
		content=content.replaceAll("\"","").replaceAll("\\\\u0027", "\"");
		System.out.println("content=="+content);
		JSONArray provinceCourts=JSONArray.parseArray(content);
		TreeItem parentItem=null;
		for(Object provinceCourt:provinceCourts){
			JSONObject tmpCourt=(JSONObject)provinceCourt;
			TreeItem item=new TreeItem();
			String level=tmpCourt.getString("leval");
			item.setParval(tmpCourt.getString("court"));
			item.setCode(tmpCourt.getString("key"));
			item.setLevel(Integer.parseInt(level));
			if("001031018000".equals(item.getCode()))//新疆维吾尔自治区高级人民法院生产建设兵团分院 设置为2级
				item.setLevel(2);
			if(item.getLevel()==2){//先找到2级的
				item.setCode(tmpCourt.getString("key"));
				item.setChildren(new ArrayList<TreeItem>());
				parentItem=item;
				allCourtTree.add(item);
			}else{
				parentItem.getChildren().add(item);
			}
			treeMap.put(item.getCode(), item);
		}
	}
	
	/**
	 * 查找到节点的位置并
	 * @param item
	 */
	public static void addTreeItem(TreeItem item){
		TreeItem parent = treeMap.get(item.getParentCode());
		if(parent==null){
			allCourtTree.add(item);
			log.error("找不到父亲法院！"+item.getParentCode());
		}else{
			if(parent.getChildren()==null){
				parent.setChildren(new ArrayList());
			}
			parent.getChildren().add(item);
		}
		treeMap.put(item.getCode(), item);
	}
}
