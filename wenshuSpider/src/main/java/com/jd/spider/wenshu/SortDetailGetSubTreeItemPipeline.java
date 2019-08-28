package com.jd.spider.wenshu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
/**
 * 旧的获取法院列表的解析类
 * 处理分类数据，建立sortRequests。下个处理会读取
 * @author yangdongjun
 *
 */
@Deprecated
@PipelineName("sortDetailGetSubTreeItemPipeline")
public class SortDetailGetSubTreeItemPipeline implements Pipeline<PageSortDetailGetSubTreeItem> {
	
	public static List<HttpRequest> sortRequests = new ArrayList<HttpRequest>();
	public static List<HttpRequest> sortRequests4 = new ArrayList<HttpRequest>();
	
	@Override
	public void process(PageSortDetailGetSubTreeItem detail) {
		if("3".equals(detail.getRequest().getParameter("level"))){//3级请求的处理
			List<TreeItem> tree= PageSortDetail.allSortTree.get(
					detail.getRequest().getParameter("sortType"));
			for(TreeItem firstTreeItem:tree){
				if(firstTreeItem.getChildren()==null) continue;
				for(TreeItem secondTreeItem:firstTreeItem.getChildren()){
					if(secondTreeItem.getChildren()==null) continue;
					for(TreeItem thirdTreeItem:secondTreeItem.getChildren()){
						if(StringUtils.isEmpty(thirdTreeItem.getTreeContentUrl())) continue;
						HttpPostRequest post = new HttpPostRequest();
						post.setCharset(detail.getRequest().getCharset());
						post.setCookies(detail.getRequest().getCookies());
						post.setHeaders(detail.getRequest().getHeaders());
						post.refer(detail.getRequest().getHeaders().get("Referer"));
						post.getHeaders().put("X-Requested-With", "XMLHttpRequest");
						post.setUrl(thirdTreeItem.getTreeContentUrl());
						post.getFields().put("Param", thirdTreeItem.getParam());
						post.getFields().put("parval", thirdTreeItem.getParval());
						post.addParameter("level", "4");
						post.addParameter("sortType", "案件类型:刑事案件");//thirdTreeItem.getParam().substring(0,thirdTreeItem.getParam().indexOf(","))
						sortRequests4.add(post);							
					}
				}
			}
		}else if("4".equals(detail.getRequest().getParameter("level"))){//获取4级请求的处理
			System.out.println("-----");
			//得到的是4级，这时，需要解释每个叶子节点，获取真正的内容了
/*			for(TreeItem treeItem:PageSortDetail.tree){
				if(treeItem.getChildren()==null) continue;
				for(TreeItem secondTreeItem:treeItem.getChildren()){
					if(secondTreeItem.getChildren()==null) continue;
					for(TreeItem  thirdTreeItem:secondTreeItem.getChildren()){
						if(StringUtils.isEmpty(thirdTreeItem.getTreeContentUrl())) continue;
						HttpPostRequest post = new HttpPostRequest();
						post.setCharset(detail.getRequest().getCharset());
						post.setCookies(detail.getRequest().getCookies());
						post.setHeaders(detail.getRequest().getHeaders());
						post.refer(getRefer(detail.getRequest().getUrl()));
						post.getHeaders().put("X-Requested-With", "XMLHttpRequest");
						post.setUrl(thirdTreeItem.getTreeContentUrl());
						post.getFields().put("Param", thirdTreeItem.getParam());
						post.getFields().put("parval", thirdTreeItem.getParval());
						sortRequests.add(post);	
					}
				}
			}*/
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