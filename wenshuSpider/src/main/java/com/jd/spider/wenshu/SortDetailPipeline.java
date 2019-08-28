package com.jd.spider.wenshu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
/**
 * 处理12级分类数据，建立获取3级分类的请求。
 * @author yangdongjun
 *
 */
@Deprecated
@PipelineName("sortDetailPipeline")
public class SortDetailPipeline implements Pipeline<PageSortDetail> {
	
	public static List<HttpRequest> sortRequests = new ArrayList<HttpRequest>();

	@Override
	public void process(PageSortDetail detail) {
		//获取所有的对象 list<treeitem> 中
//		List<TreeItem> tree=new ArrayList<TreeItem>();
//		System.out.println("---------"+detail.getSort());
		List<TreeItem> tree = PageSortDetail.allSortTree.get("案件类型:刑事案件");
		if(tree==null){
			tree=new ArrayList<TreeItem>();
			PageSortDetail.allSortTree.put(detail.getSort(),tree);
		}
		System.out.println("字符集编码==="+detail.getRequest().getCharset());
		for(TreeItem treeItem:tree){
			for(TreeItem subTreeItem:treeItem.getChildren()){
				if(StringUtils.isEmpty(subTreeItem.getTreeContentUrl())) continue;
				
				HttpPostRequest post = new HttpPostRequest();
				post.setCharset(detail.getRequest().getCharset());
//				post.setFields(posts);
				post.setCookies(detail.getRequest().getCookies());
				post.setHeaders(detail.getRequest().getHeaders());
				post.refer((detail.getRequest().getUrl()));//getRefer
				post.getHeaders().put("X-Requested-With", "XMLHttpRequest");
//				HttpRequest subRequest = detail.getRequest().subRequest(subTreeItem.getTreeContentUrl());
//				detail.getRequest().subRequest(url)
				
				post.setUrl(subTreeItem.getTreeContentUrl());
//				entry.getValue().replaceAll("\\(\\d{1,}\\)", "")
				post.getFields().put("Param", subTreeItem.getParam());
				post.getFields().put("parval", subTreeItem.getParval());
				post.addParameter("level", "3");
				post.addParameter("sortType", "案件类型:刑事案件");
				sortRequests.add(post);
				System.out.println("得到获取3级的连接：--"+post.getUrl()+","+post.getField("Param"));
			}
		}
	}
	
	private String getRefer(String s){
//		String s="http://gov.sss?sorttype=1&conditions=searchWord+1+AJLX++案件类型:刑事案件";
		String pres=s.substring(0,s.indexOf("searchWord+1+AJLX++"));
		String params=s.substring(s.indexOf("searchWord+1+AJLX++")+"searchWord+1+AJLX++".length());
		String[] paramList=params.split(":");
		params="";
		for(String param:paramList){
			try {
				params=params+URLEncoder.encode(param,"UTF-8")+":";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if(paramList.length>0){
			params=params.substring(0,params.length()-1);
		}
		s=pres+"searchWord+1+AJLX++"+params;
		return s;
	}
}