package com.jd.spider.wenshu;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.downloader.DefaultWebClientDownloader;

/**
 * 获取1，2级
 * @author yangdongjun
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("webClientWenshuDownloader")
public class WebClientWenshuDownloader extends DefaultWebClientDownloader {
	private static Log logger = LogFactory.getLog(WebClientWenshuDownloader.class);
	List<String> reasonKey=Arrays.asList("一级案由,二级案由,三级案由,四级案由,五级案由".split(","));
	List<String> courtKey=Arrays.asList("法院地域,最高法院,中级法院,基层法院".split(","));
	
	public void otherProcess(RemoteWebDriver driver) {
		System.out.println("文书下载类处理。。。");
		Document document =null;
		String sortKey=null;
		try{
			document= Jsoup.parse(driver.getPageSource());
//			
//			String url=driver.getCurrentUrl();
//			sortKey=url.substring(url.indexOf("AJLX++")+6);
//			if(sortKey.indexOf("&")>0){
//				sortKey=sortKey.substring(0, sortKey.indexOf("&"));
//			}
//			System.out.println("sort key==="+sortKey);
//			sortKey = URLDecoder.decode(sortKey,"UTF-8");
		sortKey="案件类型:刑事案件";
		System.out.println("sort key==="+sortKey);
		Element treeElement=document.getElementById("tree");
		//为什么使用document，是因为可以获取html元素。
		List<TreeItem> tree=new ArrayList<TreeItem>();
		PageSortDetail.allSortTree.put(sortKey,tree);
		
		//treeElement包含了一个头
		System.out.println("children size=="+treeElement.children().size());
		for(int i =1 ;i<treeElement.children().size();i++){
			Element element = treeElement.child(i);
//			Elements treeNodes=element.select(".jstree-node");
			if(element.hasClass("treeItem")){
				String key=element.attr("key");
				//获取 “查看更多”
				WebElement weTreeItem=driver.findElementByCssSelector("#tree>div.treeItem:nth-child("+(i+1)+")");
				WebElement title = weTreeItem.findElement(By.cssSelector(".itemhead>span"));
				WebElement more=weTreeItem.findElement(By.className("unfold"));
				if(more!=null&& StringUtils.isNotEmpty(more.getText())){
					more.click();
				}
			}
		}
		//点开2级
		for(int i =1 ;i<treeElement.children().size();i++){
			Element element = treeElement.child(i);
			if(element.hasClass("treeItem")){
				String key=element.attr("key");
				//.jstree-2 > ul:nth-child(1) > li:nth-child(1) > i:nth-child(1)
				TreeItem item=new TreeItem();
				List<TreeItem> children=new ArrayList<TreeItem>();
				item.setChildren(children);
				item.setKey(key);
				item.setLevel(1);
				System.out.println("开始寻找树节点。。。。。。"+key);
				//driver.findElementByCssSelector("#tree>div.treeItem:nth-child("+(i+1)+")").getText();
				List<WebElement> we=driver.findElementsByCssSelector("#tree>div.treeItem:nth-child("+(i+1)+")>div:nth-child(2)>ul>li");
				String contentUrl=null;
				if(reasonKey.contains(key)){
					contentUrl="http://wenshu.court.gov.cn/List/ReasonTreeContent";
					continue;
				}else if(courtKey.contains(key)){
					if(WenshuMain.step1ContinueProvince.size()!=0
							&&!WenshuMain.step1ContinueProvince.contains(key)){
						continue;
					}
					//获取下级菜单，只保留法院名称
					contentUrl="http://wenshu.court.gov.cn/List/CourtTreeContent";
				}else{
					continue;
				}
				for(WebElement ee:we){
					TreeItem child=new TreeItem();
					child.setLevel(2);
					String childKey=ee.findElement(By.cssSelector("a")).getText();
					if(childKey!=null){
						childKey = childKey.replaceAll("\\(\\d{1,}\\)", "");
					}
					if(WenshuMain.step1ContinueProvince.size()!=0&&!WenshuMain.step1ContinueProvince.contains(childKey)){
						continue;
					}
					child.setParam(key+":"+childKey);
					child.setParval(childKey);
					child.setTreeContentUrl(contentUrl);
					children.add(child);
				}
				System.out.println("tree.add 2级 ["+JSON.toJSONString(item)+"]");
				tree.add(item);
				//System.out.println("json tree:"+JSON.toJSONString(tree));
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		
	}
	
	
	
}
