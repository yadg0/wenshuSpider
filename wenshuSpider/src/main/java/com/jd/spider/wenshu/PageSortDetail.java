package com.jd.spider.wenshu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.Request;
import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;

/**
 * 首先使用webClientWenshuDownloader，下载后，将1级分类，2级分类，保存在treeItem中。
 * 然后在 pipeline 中，处理tree，生成所有的request，放在 sortDetailPipeline 的 sortRequest中
 * 启动新的任务，下载这些请求，
 * 请求返回后，首先隐射到PageSortDetailGetSubTreeItem，使用sortDetailGetSubTreepipeLine处理，
 * 将下级放到tree中。并生成 request，放入到 sortRequest 中。
 * @author yangdongjun
 *
 */
@Deprecated
@Gecco(matchUrl="http://wenshu.court.gov.cn/List/List?sorttype=1", pipelines={"consolePipeline","sortDetailPipeline"},downloader="webClientWenshuDownloader")//, "allSortPipeline",
public class PageSortDetail implements HtmlBean {
	//获取分类关键词 http://wenshu.court.gov.cn/List/TreeList
	//获取分类 http://wenshu.court.gov.cn/List/TreeContent?guid=355e2c26-b3bd-c4c6d0ae-8246aa426df9&number=KY49YTS5&param=案件类型:刑事案件&vl5x=99d83eea87c94b6ab3da4658
	//guid=355e2c26-b3bd-c4c6d0ae-8246aa426df9&number=KY49YTS5&param=案件类型:刑事案件&vl5x=99d83eea87c94b6ab3da4658
	//http://wenshu.court.gov.cn/Assets/js9/Lawyee.CPWSW.JsTree.js
	//getKey
	// https://blog.csdn.net/earbao/article/details/41625929 HtmlUtil可以模拟网页
	
	private static final long serialVersionUID = 3018760488621382659L;
	@RequestParameter
	private String sort;
	public static Map<String,List<TreeItem>> allSortTree=new HashMap<String,List<TreeItem>>();
//	public static List<TreeItem> tree;
	/*
	@HtmlField(cssPath="div.treeItem:nth-child(2) > div:nth-child(1)")
	List<Category> categorys;//所有类目

	@Text
	@HtmlField(cssPath="div.treeItem:nth-child(2) > div:nth-child(1) > span:nth-child(2)")
	String fisrtCategory;//所有类目
*/
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

	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
//		sort  = sort.substring(sort.indexOf("AJLX  ")+6);
//		if(sort.indexOf("&")>-1){
//			sort=sort.substring(0, sort.indexOf("&"));
//		}
//		if(sort!=null){
//			String[] sorts=sort.split(":");
//			sort=sorts[1];
//		}
		this.sort = sort;
	}
	/**
	 * 
	 * @param sort = "案件类型:刑事案件"|"案件类型:民事案件"。。。
	 */
	public static List<TreeItem> getTree(String sort){
		return allSortTree.get(sort);
	}
}
