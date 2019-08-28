package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.JSONPath;
import com.geccocrawler.gecco.annotation.Request;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.JsonBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;

/**
 * 旧的使用 webClientWenshuDownloader 获取下级法院信息
 * @author yangdongjun
 *
 */
@Deprecated
@Gecco(matchUrl="http://wenshu.court.gov.cn/List/CourtTreeContent", pipelines={"consolePipeline","sortDetailGetSubTreeItemPipeline"})//, "allSortPipeline"
public class PageSortDetailGetSubTreeItem implements JsonBean {
	
//	private boolean isJSONArray=true;
	
	@JSONPath("$[0].Key")
	private String parentKey;

	@JSONPath("$[0].Child")
	private List<JSONObject> subKeys;
	
	public String getParentKey() {
		return parentKey;
	}
	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}
	public List<JSONObject> getSubKeys() {
		return subKeys;
	}
	public void setSubKeys(List<JSONObject> subKeys) {
		this.subKeys = subKeys;
	}
	
	public static int processIndex=0;
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		//将返回值和request中的参数，从tree中得到item,将结果放入
		processIndex++;
		System.out.println("处理了："+processIndex+",PageSortDetail.reqs="+
			PageSortDetail.allSortTree.get("案件类型:刑事案件").size()+
			",second size="+SortDetailPipeline.sortRequests.size()+
			",third size="+SortDetailGetSubTreeItemPipeline.sortRequests4.size());
		HttpPostRequest postRequest=(HttpPostRequest)request;
		if(subKeys==null) 
			System.out.println("错误：没有找到孩子："+postRequest.getField("Param"));
		
		TreeItem item=findTreeItem(PageSortDetail.allSortTree,postRequest.getField("Param"));
		
		for(JSONObject jo : subKeys){
			if(jo.get("id").toString().indexOf("NULL")==-1){
				if(item.getChildren()==null) item.setChildren(new ArrayList<TreeItem>());
				TreeItem newTreeItem=new TreeItem();
				String field=jo.getString("Field");
				String value=jo.getString("Key");
				newTreeItem.setLevel(item.getLevel()+1);//3级
				newTreeItem.setKey(value);
				newTreeItem.setParval(jo.getString("Key"));
				newTreeItem.setParam(field+":"+value);
				
				newTreeItem.setTreeContentUrl(item.getTreeContentUrl());
//				newTreeItem.setCssSelector(cssSelector);
//				newTreeItem.getIsLeaf();
				item.getChildren().add(newTreeItem);
			}
			
		}
	}

	
	private TreeItem findTreeItem(Map<String, List<TreeItem>> allSortTree, String parameter) {
		String sortTreeKey = "案件类型:刑事案件";
		List<TreeItem> tree=allSortTree.get(sortTreeKey);
		return findTreeItemInTree(tree,parameter);
	}

	private TreeItem findTreeItemInTree(List<TreeItem> tree, String parameter) {
		for(TreeItem item:tree){
			if(item.getParam()!=null && item.getParam().equals(parameter)){
				return item;
			}
			if(item.getChildren()!=null && item.getChildren().size()>0){
				TreeItem newItem = findTreeItemInTree(item.getChildren(),parameter);
				if(newItem!=null) return newItem;
			}
		}
		return null;
	}

	private static final long serialVersionUID = 3018760488621382659L;
	
	List<TreeItem> tree;
	
	List<String> reasonKey=Arrays.asList("一级案由,二级案由,三级案由,四级案由,五级案由".split(","));
	List<String> courtKey=Arrays.asList("法院地域,最高法院,中级法院,基层法院".split(","));
	
	@Request
	private HttpRequest request;
	public HttpRequest getRequest() {
		return request;
	}
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
	
}
