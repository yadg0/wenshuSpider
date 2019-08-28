package com.jd.spider.wenshu;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.downloader.DefaultWebClientDownloader;
import com.geccocrawler.gecco.downloader.DownloadException;
import com.geccocrawler.gecco.downloader.HttpClientDownloader;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.render.RequestParameterFieldRender;
import com.geccocrawler.gecco.utils.UrlMatcher;
import com.jd.spider.wenshu.domain.ArticleEntity;
import com.jd.spider.wenshu.domain.ArticlePageTask;
import com.jd.spider.wenshu.service.State;
import com.jd.spider.wenshu.service.WenshuService;

import net.sf.cglib.beans.BeanMap;

/**
 * 老的文书文章列表下载器，使用seleniumhq。17.20
 * @author yangdongjun
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("webClientArticleListDownloader")
public class WebClientArticleListDownloader extends DefaultWebClientDownloader {
	public static int maxPageNo=25;//最大页数
	private static Log logger = LogFactory.getLog(WebClientArticleListDownloader.class);
	public static List<HttpRequest> articleListRequest;
	private static int yzmUsedTimes=0; 
	private String cookie;
	private String guid;
	private String number;
	private String vl5x;
	
	//多个线程处理时，使用公用信号量来
	public static Integer index=0;
	private static String ArticleListUrlPattern="http://wenshu.court.gov.cn/List/List/?"
			+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
			+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}";
	private static String listContentUrl="http://wenshu.court.gov.cn/List/ListContent";
	
	
	public void otherProcess(RemoteWebDriver driver) {
		//一次load10个进来处理
		while(index<articleListRequest.size()){
			HttpRequest request=null;
			synchronized (index) {
				request =articleListRequest.get(index);
				System.out.println(request.getUrl());
				index++;
			}
			if(request==null) continue;
			ArticlePageTask task=new ArticlePageTask();
			try {
//				PageArticleList detail= PageArticleList.class.newInstance();
				PageArticleList detail =new PageArticleList();
				BeanMap beanMap = BeanMap.create(detail);
				HttpResponse response=new HttpResponse();
				response.setContent("");
				String urlPattern=null;
				for(Annotation anotation : PageArticleList.class.getAnnotations()){
					Gecco gecco=(Gecco)anotation;
					urlPattern=gecco.matchUrl()[0];
				}
				
				Map<String, String> params = UrlMatcher.match(request.getUrl(), urlPattern);
				if (params != null) {
					request.setParameters(params);
				}
				System.out.println("params==="+JSON.toJSONString(params));
//				PageArticleList detail= (PageArticleList) htmlRender.inject(PageArticleList.class, request, response);
				RequestParameterFieldRender requestParameterFieldRender= new RequestParameterFieldRender();
				requestParameterFieldRender.render(request, response, beanMap, detail);
				
				task.setCourtName(detail.getSort2());
				task.setDate(detail.getSort5());
				task.setEndDate(detail.getSort6());
			//保存到数据库
			} catch (Exception e) {
				e.printStackTrace();
			}
			Date begin=new Date();
			try{
				driver.navigate().to(request.getUrl());
				yzmUsedTimes=0;
				Thread.sleep(1000);
				task.setState(State.DOWNLOADING);
				task.setSuccCount(0);
				WenshuService.updateArticlePageTask(task);
				oneArticleListRequestProc(driver,task);
				Date end=new Date();
				task.setCostTime(end.getTime()-begin.getTime());
				if(task.getState()!=State.FAILED)
					task.setState(State.DOWNLOADED);
				WenshuService.updateArticlePageTask(task);
			}catch(Exception e){
				e.printStackTrace();
				Date end=new Date();
				task.setCostTime(end.getTime()-begin.getTime());
				task.setState(State.FAILED);
				WenshuService.updateArticlePageTask(task);
			}
			
		}
		//翻页后点击
	}

	@Override
	public void shutdown() {
		
	}
	
	private void oneArticleListRequestProc(RemoteWebDriver driver, ArticlePageTask task){
		PageArticleList pageArticleList=new PageArticleList();
		List<Article> articles=new ArrayList<Article>();
		Document document = tryGetResult(driver);
		if(document == null ){
			task.setState(State.FAILED);
			return;
		}
		if(document.selectFirst(".pageNumber")==null){
			return ;
		}
		
		Integer total = Integer.parseInt(document.selectFirst(".pageNumber").attr("total"));
		if(task.getTotalCount()==null)
			task.setTotalCount(total);
		cookie=getCookie(driver);
		System.out.println("cookie===="+cookie);
		guid=getGuid();
		System.out.println("guid===="+guid);
		number=getNumber(cookie,guid);
		vl5x=getVlx5(cookie);
		////此时可以分页获取数据了
		pageArticleList.setCounts(Integer.parseInt(document.selectFirst(".pageNumber").attr("total")));
		String url=driver.getCurrentUrl();
		Map<String, String> params = UrlMatcher.match(url, ArticleListUrlPattern);
		String startDate=params.get("sort5");
		String endDate=params.get("sort6");
		String fymc=params.get("sort2");
//		if(startDate.equals(endDate)){//开始和结束日期一样了，那也没办法了
//			oneArticleListSubRequestProc(driver,task);
//			return;
//		}
//		System.out.println("begin split2");
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		splitRequest2(startDate,endDate,fymc,task,driver);
	}
	

	private void splitRequest2(String startDate, String endDate, String fymc,ArticlePageTask task, RemoteWebDriver driver) {
		String response = listContentRequest(startDate,endDate,fymc,1,driver);
		//System.out.println(response.replaceAll("\\\\\"", ""));
		ScriptEngineManager manager=new ScriptEngineManager();
		ScriptEngine se = manager.getEngineByName("js");
		String resultObj=null;
		try {
			resultObj=(String)se.eval(response);
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray result=JSON.parseArray(resultObj);
		
		int total = result.getJSONObject(0).getInteger("Count");
		if(total>=WenshuMain.step3_maxTotal){
			Date start=null;
			Date end=null;
			try {
				start = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(startDate);
				end=DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}
			Date midDate = new Date(start.getTime());
			midDate.setTime(start.getTime()+(end.getTime()-start.getTime())/2);//让开始时间
			String midDateStr= DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate);
			splitRequest2(startDate,midDateStr,fymc,task,driver);
			midDate.setDate(midDate.getDate()+1);
			midDateStr=DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate);
			splitRequest2(midDateStr,endDate,fymc,task,driver);
		}else{
			oneArticleListSubRequestProc2(startDate,endDate,fymc,result,driver,task);
		}
	}
	private String listContentRequest(String startDate, String endDate, String fymc,int pageIndex,RemoteWebDriver driver) {
		try {
			HttpClientDownloader downloader = new HttpClientDownloader();
			HttpPostRequest post = new HttpPostRequest();
			post.setUrl("http://wenshu.court.gov.cn/List/ListContent");
			post.addHeader("Referer", "http://wenshu.court.gov.cn/List/List");
			post.addHeader("Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
			if(yzmUsedTimes>WenshuMain.yzmUseTimes){//非食用次数大于20次，就用driver刷新一下，重新获取
				reloadParameters(driver);
			}
			yzmUsedTimes++;
			post.addCookie("vjkl5", cookie);
			post.addField("guid", guid);
			post.addField("Index", ""+pageIndex);
			post.addField("Page", ""+20);
			post.addField("vl5x", vl5x);
			post.addField("number", number);
			post.addField("Order", "法院层级");
			post.addField("Param", "法院名称:"+fymc+",裁判日期:"+startDate+" TO "+endDate);
			post.addField("Direction", "asc");
			try {
				HttpResponse response = downloader.download(post, 20000);
				//休民1秒
//				Thread.sleep(PageSortList.listContentSleepTime);
				return response.getContent();
			} catch (DownloadException e) {
				e.printStackTrace();
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void reloadParameters(RemoteWebDriver driver) {
		driver.navigate().refresh();
		cookie=getCookie(driver);
		guid=getGuid();
		number=getNumber(cookie,guid);
		vl5x=getVlx5(cookie);
		yzmUsedTimes=0;
	}

	private Document tryGetResult(RemoteWebDriver driver) {
		int waitTimes=30;//30秒后还没数据，返回错误了
		while(waitTimes>0){
			Document document = Jsoup.parse(driver.getPageSource());
			if(document.select("#resultList>div>img")!=null&&document.select("#resultList>div>img").size()>0){
				try {
					Thread.sleep(1000);
					waitTimes--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				//获取 .number ，设置每页数量
//				if(!document.getElementById("12_input_20").hasClass("selected"))
//					driver.findElementById("12_input_20").click();
//				else
					return document;
			}
		}
		return null;
	}
	private void oneArticleListSubRequestProc2(String startDate,String endDate,String fymc,JSONArray result,RemoteWebDriver driver,ArticlePageTask task){
		PageArticleList pageArticleList=new PageArticleList();
		List<Article> articles=new ArrayList<Article>();
		Integer pageIndex=1;
		int total = result.getJSONObject(0).getInteger("Count");
		try{
			while(true){
				//先定向到一个页面，直到，出现加载成功，否则就刷新
				for(int i=1;i<result.size();i++){
					JSONObject jo=result.getJSONObject(i);
					Article article=new Article();
					article.setUrl("http://wenshu.court.gov.cn/content/content?DocID="+jo.getString("文书ID"));
					article.setId(jo.getString("文书ID"));
					article.setTitle(jo.getString("案件名称"));
					ArticleListPipeline.articles.add(article);
					articles.add(article);
					ArticleEntity articleEntity = new ArticleEntity();
					articleEntity.setDocId(article.getId());
					articleEntity.setTitle(article.getTitle());
					articleEntity.setState(State.NEW);
					articleEntity.setTaskId(task.getId());
					articleEntity.setTaskStartDate(task.getDate());
					WenshuService.addArticle(articleEntity);
					task.setSuccCount(task.getSuccCount()+1);
				}
				//判断是否有下一页
				if(pageIndex*20<total){//
					pageIndex++;
					String response = listContentRequest(startDate,endDate,fymc,pageIndex,driver);
					ScriptEngineManager manager=new ScriptEngineManager();
					ScriptEngine se = manager.getEngineByName("js");
					String resultObj=null;
					try {
						resultObj=(String)se.eval(response);
					} catch (ScriptException e1) {
						e1.printStackTrace();
					}
					result=JSON.parseArray(resultObj);
				}else{
					break;
				}
			}
		//得到第一页数据
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void oneArticleListSubRequestProc(RemoteWebDriver driver,ArticlePageTask task){
		PageArticleList pageArticleList=new PageArticleList();
		List<Article> articles=new ArrayList<Article>();
		try{
			while(true){
				//先定向到一个页面，直到，出现加载成功，否则就刷新
				Document document = tryGetResult(driver);//Jsoup.parse(driver.getPageSource());
				if(true) return;
				if(document == null ){
					task.setState(State.FAILED);
					return;
				}
				if(document.selectFirst(".pageNumber")==null){
					break;
				}
				pageArticleList.setCounts(Integer.parseInt(document.selectFirst(".pageNumber").attr("total")));
				pageArticleList.setPageSize(Integer.parseInt(document.selectFirst(".pageNumber").attr("pageSize")));
				for(int i=1;i<=5;i++){
					Element el=document.selectFirst("div.dataItem:nth-child("+i+") > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > div:nth-child(1) > a:nth-child(3)");
					if(el==null) 
						break;
					Article article=new Article();
					
					article.setUrl(el.attr("href"));
					article.setId(article.getUrl().substring(article.getUrl().indexOf("DocID=")+6));
					article.setTitle(el.text());
					ArticleListPipeline.articles.add(article);
					articles.add(article);
					//同时保存数据库
					ArticleEntity articleEntity = new ArticleEntity();
					articleEntity.setDocId(article.getId());
					articleEntity.setTitle(article.getTitle());
					articleEntity.setState(State.NEW);
					articleEntity.setTaskId(task.getId());
					articleEntity.setTaskStartDate(task.getDate());
					WenshuService.addArticle(articleEntity);
					task.setSuccCount(task.getSuccCount()+1);
				}
				Integer pageIndex=Integer.parseInt(document.selectFirst(".current:not(.prev)").text());
				pageArticleList.setPageIndex(pageIndex);
				//找到这行，点击下一个目标
				if(pageArticleList.getPageIndex()<new BigDecimal(pageArticleList.getCounts()).divide(new BigDecimal(pageArticleList.getPageSize()),RoundingMode.CEILING).intValue()){
					driver.findElementByClassName("current").getText();
					driver.findElementByCssSelector(".next").click();
				}else{
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		//得到第一页数据
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static String getNumber(String cookie,String guid) {
		try {
			HttpClientDownloader downloader = new HttpClientDownloader();
			HttpPostRequest post = new HttpPostRequest();
			post.setUrl("http://wenshu.court.gov.cn/ValiCode/GetCode");
			post.addHeader("Referer", "http://wenshu.court.gov.cn/List/List");
			post.addHeader("Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
			post.addCookie("vjkl5", cookie);
			post.addField("guid", guid);
			try {
				HttpResponse response = downloader.download(post, 20000);
				return response.getContent();
			} catch (DownloadException e) {
				e.printStackTrace();
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getCookie(RemoteWebDriver driver){
		Set<Cookie> cookies=driver.manage().getCookies();
		for(Cookie cookie: cookies){
			if("vjkl5".equals(cookie.getName())){
				return cookie.getValue();
			}
		}
		return null;
	}
	
	private static String getGuid(){
		WebClient wc=new WebClient(BrowserVersion.FIREFOX_45);
		try {
			 HtmlPage page=wc.getPage(WenshuMain.crakerHtmlPath+WenshuMain.crakerYzmHtml);
			 ScriptResult  result = page.executeJavaScript("createGuid2()");
			 return result.getJavaScriptResult().toString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			wc.close();
		}
		return null;
	}
	
	private static String getVlx5(String cookie){
		WebClient wc=new WebClient(BrowserVersion.FIREFOX_45);
		try {
			 HtmlPage page=wc.getPage(WenshuMain.crakerHtmlPath+WenshuMain.crakerYzmHtml);
			 ScriptResult  result = page.executeJavaScript("getKey('"+cookie+"')");
			 return result.getJavaScriptResult().toString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			wc.close();
		}
		return null;
		
	}
	
	public static void main(String[] args) {
//		System.out.println(getGuid());
		String cookie="868d9cff59647701bf18358c4cdc2d08a36db045";
		String guid=getGuid();
		String yzm=getNumber(cookie, guid);
		System.out.println(getVlx5("868d9cff59647701bf18358c4cdc2d08a36db045"));
		System.out.println("yzm="+yzm);
	}
	
	
	
	/*************/
	@Deprecated
	private void splitRequest(String url, Integer total, ArticlePageTask task, RemoteWebDriver driver,boolean isListPage) {
		if(total>=WenshuMain.step3_maxTotal){
			Map<String, String> params = UrlMatcher.match(url, isListPage?ArticleListUrlPattern:listContentUrl);
			String startDate=params.get("sort5");
			String endDate=params.get("sort6");
			if(startDate.equals(endDate)){//开始和结束日期一样了，那也没办法了
				oneArticleListSubRequestProc(driver,task);
				return;
			}
			Date start=null;
			Date end=null;
			try {
				start = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(startDate);
				end=DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}
			Date midDate = new Date(start.getTime());
			midDate.setTime(start.getTime()+(end.getTime()-start.getTime())/2);//让开始时间
			params.put("sort6", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate));
			String sub1Url=ArticleListUrlPattern;
			sub1Url = UrlMatcher.replaceParams(sub1Url, params).replaceAll("\\\\", "");
			midDate.setDate(midDate.getDate()+1);
			params.put("sort5", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate));
			params.put("sort6", endDate);
			String sub2Url=ArticleListUrlPattern;
			sub2Url = UrlMatcher.replaceParams(sub2Url, params).replaceAll("\\\\", "");
			try {
				driver.navigate().to(sub1Url);
				Thread.sleep(500);
				oneArticleListRequestProc(driver, task);
				driver.navigate().to(sub2Url);
				Thread.sleep(500);
				oneArticleListRequestProc(driver, task);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}else{
			oneArticleListSubRequestProc(driver,task);
		}
	}
}
