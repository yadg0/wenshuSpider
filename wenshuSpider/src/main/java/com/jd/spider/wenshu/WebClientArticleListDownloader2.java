package com.jd.spider.wenshu;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.downloader.DownloadException;
import com.geccocrawler.gecco.downloader.HttpClientDownloader;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.render.RequestParameterFieldRender;
import com.geccocrawler.gecco.utils.UrlMatcher;
import com.jd.spider.wenshu.domain.ArticleEntity;
import com.jd.spider.wenshu.domain.ArticlePageTask;
import com.jd.spider.wenshu.service.DateType;
import com.jd.spider.wenshu.service.State;
import com.jd.spider.wenshu.service.WenshuService;

import net.sf.cglib.beans.BeanMap;

/**
 * 破解了js后的文章列表下载方式。
 * 使用 HttpClientDownloader 请求服务器，使用 WebClient 计算guid和vlx5
 * @author yangdongjun
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("webClientArticleListDownloader2")
public class WebClientArticleListDownloader2 extends HttpClientDownloader {
	public static int maxPageNo=25;//最大页数
	private static Log logger = LogFactory.getLog(WebClientArticleListDownloader2.class);
	public static List<HttpRequest> articleListRequest;
	private static int yzmUsedTimes=0; 
	private String cookie;
	private String guid;
	private String number;
	private String vl5x;
	
	//多个线程处理时，使用公用信号量来
//	public static Integer index=0;
	private static String ArticleListUrlPattern="http://wenshu.court.gov.cn/List/List/?"
			+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
			+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}";
	private static String listContentUrl="http://wenshu.court.gov.cn/List/ListContent";
	
	private static String getCookieUrl="http://wenshu.court.gov.cn/List/List?sorttype=1&conditions=searchWord+1+AJLX++%E6%A1%88%E4%BB%B6%E7%B1%BB%E5%9E%8B:%E5%88%91%E4%BA%8B%E6%A1%88%E4%BB%B6&conditions=";
	
	@Override
	public void otherProcess(HttpRequestBase reqObj, HttpResponse resp) {
		//一次load10个进来处理
		for(int index=0;index<articleListRequest.size();index++){
			HttpRequest request=null;
			request =articleListRequest.get(index);
			if(request==null) continue;
			ArticlePageTask task=new ArticlePageTask();
			HttpResponse response=new HttpResponse();
			try {
//				PageArticleList detail= PageArticleList.class.newInstance();
				//解释url中的参数
				PageArticleList detail =new PageArticleList();
				BeanMap beanMap = BeanMap.create(detail);
				response.setContent("");
//				String urlPattern=null;
				Map<String, String> params =null;
				for(Annotation anotation : PageArticleList.class.getAnnotations()){
					Gecco gecco=(Gecco)anotation;
					for(String urlPattern:gecco.matchUrl()){
						params = UrlMatcher.match(request.getUrl(), urlPattern);
						if(params==null || params.get("sort6").indexOf("&")>-1)
							continue;
						else
							break;
					}
				}
				if (params != null) {
					request.setParameters(params);
				}
				logger.info("params==="+JSON.toJSONString(params));
//				PageArticleList detail= (PageArticleList) htmlRender.inject(PageArticleList.class, request, response);
				RequestParameterFieldRender requestParameterFieldRender= new RequestParameterFieldRender();
				requestParameterFieldRender.render(request, response, beanMap, detail);
				task.setDateType(detail.getSort3());
				task.setCourtName(detail.getSort2());
				task.setDate(detail.getSort5());
				task.setEndDate(detail.getSort6());
			//保存到数据库
			} catch (Exception e) {
				e.printStackTrace();
			}
			Date begin=new Date();
			try{
				//从cookie中得到值
				cookie=getCookie(null);
				guid=getGuid();
				number=getNumber(cookie,guid);
				vl5x=getVlx5(cookie);

				yzmUsedTimes=0;
				Thread.sleep(1000);
				task.setState(State.DOWNLOADING);
				task.setSuccCount(0);
				WenshuService.updateArticlePageTask(task);
				oneArticleListRequestProc(task,task.getDate(),task.getEndDate());
				Date end=new Date();
				task.setCostTime(end.getTime()-begin.getTime());
				if(task.getState()!=State.FAILED ){
					if(task.getState()==State.OVERFLOW_FAILED){
					}else
						task.setState(State.DOWNLOADED);
				}
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
	
	private void oneArticleListRequestProc(ArticlePageTask task,String startDate,String endDate) throws Exception{
		PageArticleList pageArticleList=new PageArticleList();
		List<Article> articles=new ArrayList<Article>();
		/////获取 pageNumber
		String response=listContentRequest(startDate, endDate, task.getCourtName(), task.getDateType(),1);
		ScriptEngineManager manager=new ScriptEngineManager();
		ScriptEngine se = manager.getEngineByName("js");
		String resultObj=null;
		JSONArray result=null;
		try {
			resultObj=(String)se.eval(response);
			result=JSON.parseArray(resultObj);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		if(result.size()==0){
			return;
		}
		Integer total = result.getJSONObject(0).getInteger("Count");
		if(total>=WenshuMain.step3_maxTotal){
			task.setState(State.OVERFLOW_FAILED);
		}
		task.setTotalCount(total);
		
		if(startDate.equals(endDate)){//开始和结束日期一样了，那也没办法了
			logger.info("开始日期和结束日期一样，不用拆分了。。。");
			oneArticleListSubRequestProc2(startDate,endDate,task.getCourtName(),result,task);//(task);//开始获取分页数据。
			return;
		}
		logger.info("total="+total+","+WenshuMain.step3_maxTotal+",判断是否需要拆分："+(total>=WenshuMain.step3_maxTotal));
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
			splitRequest2(startDate,midDateStr,task.getCourtName(),task);
			midDate.setDate(midDate.getDate()+1);
			midDateStr=DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate);
			splitRequest2(midDateStr,endDate,task.getCourtName(),task);
		}else{
			oneArticleListSubRequestProc2(startDate,endDate,task.getCourtName(),result,task);//(task);//开始获取分页数据。
			return;
		}
	}

	private void splitRequest2(String startDate, String endDate, String fymc,ArticlePageTask task) {
		String response = listContentRequest(startDate,endDate,fymc,task.getDateType(),1);
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
		if(result.size()==0){
			return;
		}
		int total = result.getJSONObject(0).getInteger("Count");
		if(startDate.equals(endDate)){//开始和结束日期一样了，那也没办法了
			logger.info("splitRequest2...开始日期和结束日期一样，不用拆分了。。。");
			oneArticleListSubRequestProc2(startDate,endDate,task.getCourtName(),result,task);//(task);//开始获取分页数据。
			return;
		}
		logger.info("splitRequest2...total="+total+","+WenshuMain.step3_maxTotal+",判断是否需要拆分："+(total>=WenshuMain.step3_maxTotal));
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
			splitRequest2(startDate,midDateStr,fymc,task);
			midDate.setDate(midDate.getDate()+1);
			midDateStr=DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(midDate);
			splitRequest2(midDateStr,endDate,fymc,task);
		}else{
			oneArticleListSubRequestProc2(startDate,endDate,fymc,result,task);
		}
	}
	private String listContentRequest(String startDate, String endDate, String fymc,String dateType,int pageIndex) {
		int retryTimes=0;
		int emptyRetryTimes=0;
			while(retryTimes<=WenshuMain.yzmUseTimes){
//				if(yzmUsedTimes>PageSortList.yzmUseTimes){//
//					reloadParameters();
//				}
				HttpPostRequest post = new HttpPostRequest();
				
				try {
					guid=getGuid();
					number=getNumber(cookie,guid);
					HttpClientDownloader downloader = new HttpClientDownloader();
					post.setUrl("http://wenshu.court.gov.cn/List/ListContent");
					post.addHeader("Referer", "http://wenshu.court.gov.cn/List/List");
					post.addHeader("Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
					yzmUsedTimes++;
					post.addField("Direction", "asc");
					post.addCookie("vjkl5", cookie);
					post.addField("guid", guid);
					post.addField("Index", ""+pageIndex);
					post.addField("Page", ""+getPageSize(dateType));
					post.addField("vl5x", vl5x);
					post.addField("number", number);
					post.addField("Order", "法院层级");
					boolean needJcfy=false;
					if(fymc.indexOf("县人民法院")>-1){
						needJcfy=true;
					}
					logger.info("needJcfy="+needJcfy);
					////
					//如果原有task中是上传日期，那么就用上传日期
					//法院名称:最高人民法院,上传日期:2018-06-29+TO+2018-07-25
					post.addField("Param", "法院名称:"+fymc+","+dateType+":"+startDate+" TO "+endDate+(needJcfy?",基层法院:"+fymc:""));
					Date begin=new Date();
					HttpResponse response = downloader.download(post, 20000);
					Date end = new Date();
					logger.info("请求花费时间:"+(end.getTime()-begin.getTime())+",结果:"
					+response.getStatus()+",内容:"+
					((response.getContent()!=null && response.getContent().length()>500)?response.getContent().substring(0,500):response.getContent()));
//					Thread.sleep(PageSortList.listContentSleepTime);//正常休眠1秒
					if(response==null||response.getContent()==null||
							response.getStatus()!=200||
							response.getContent().indexOf("remind key")>-1
							||response.getContent().indexOf("[]")>-1
							){
						logger.info("param=1="+post.getField("Param")+",pageIndex="+pageIndex+",retryTimes="+retryTimes);
						retryTimes++;
						if(response.getContent().indexOf("[]")>-1)
							emptyRetryTimes++;
						//如果内容3次都返回空，那么就确实没有内容
						if(emptyRetryTimes>=3){
							return "[]";
						}
						logger.info("sleep "+getSleepTime(retryTimes)+"...emptyRetryTimes="+emptyRetryTimes);
						Thread.sleep(getSleepTime(retryTimes));//失败休眠10秒
						reloadParameters(null);
						continue;
					}
					String s=response.getContent().length()>100?response.getContent().substring(0,100).substring(0,100):response.getContent();
					logger.info("param=2="+post.getField("Param")+",pageIndex="+pageIndex+","+s);
					return response.getContent();
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("param=3="+post.getField("Param")+",pageIndex="+pageIndex+",retryTimes="+retryTimes);
					retryTimes++;
					try{
						logger.info("sleep "+getSleepTime(retryTimes)+"...");
						Thread.sleep(getSleepTime(retryTimes));//失败休眠10秒
						reloadParameters(null);
					}catch(Exception e2){
						logger.info("reload parameters exception:"+e2.getMessage());
						e2.printStackTrace();
					}
				}
			}
		return null;
	}
	private int getSleepTime(int retryTimes){
		switch (retryTimes) {
		case 1:case 2:case 3:case 4:case 5:
			return retryTimes*1000;
		case 6:case 7:case 8:
			return 10000;
		case 9:case 10:case 11:
			return 30000;
		default:
			return 5000;
		}
	}
	private void reloadParameters(String newCookie) {
		//driver.navigate().refresh();
		if(newCookie==null){
			cookie=getCookie(null);
		}else{
			cookie=newCookie;
		}
		guid=getGuid();
		number=getNumber(cookie,guid);
		vl5x=getVlx5(cookie);
		yzmUsedTimes=0;
	}

	/**
	 * 按日期最小粒度的处理了
	 * @param startDate
	 * @param endDate
	 * @param fymc
	 * @param result
	 * @param task
	 */
	private void oneArticleListSubRequestProc2(String startDate,String endDate,String fymc,JSONArray result,ArticlePageTask task){
		PageArticleList pageArticleList=new PageArticleList();
		List<Article> articles=new ArrayList<Article>();
		Integer pageIndex=1;
		int total = result.getJSONObject(0).getInteger("Count");
		if(startDate.equals(endDate) && total>=500){
			task.setState(State.OVERFLOW_FAILED);
		}

		try{
			while(true){
				//先定向到一个页面，直到，出现加载成功，否则就刷新
				for(int i=1;i<result.size();i++){
					JSONObject jo=result.getJSONObject(i);
					Article article=new Article();
					article.setUrl("http://wenshu.court.gov.cn/content/content?DocID="+jo.getString("文书ID"));
					article.setId(jo.getString("文书ID"));
					article.setTitle(jo.getString("案件名称"));
//					ArticleListPipeline.articles.add(article);
					articles.add(article);
					ArticleEntity articleEntity = new ArticleEntity();
					articleEntity.setDocId(article.getId());
					articleEntity.setTitle(article.getTitle());
					articleEntity.setState(State.NEW);
					articleEntity.setTaskId(task.getId());
					articleEntity.setTaskStartDate(startDate);
					WenshuService.addArticle(articleEntity);
					task.setSuccCount(task.getSuccCount()+1);
				}
				//每页完成更新下进度
				if(task.getSuccCount()==task.getTotalCount()){
					task.setState(State.DOWNLOADED);
				}
				if(pageIndex>getPageSize(task.getDateType())){
					task.setUpdatedTime(DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(startDate));
				}
				WenshuService.updateArticlePageTask(task);
				//判断是否有下一页
				if(pageIndex*getPageSize(task.getDateType())<total){//
					pageIndex++;
					String response = listContentRequest(startDate,endDate,fymc,task.getDateType(),pageIndex);
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
			task.setState(State.FAILED);
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

	private String getCookie(HttpClientDownloader downloader){
		Date begin=new Date();
		
		List<org.apache.http.cookie.Cookie> cookies = null;
		if(downloader==null){
			int getCookieRetry=0;
			while(getCookieRetry<3){
				try {
					HttpClientDownloader tmpDownloader = new HttpClientDownloader();
					HttpGetRequest post = new HttpGetRequest();
					post.setUrl(getCookieUrl);
					post.addHeader("Referer", "http://wenshu.court.gov.cn/List/List");
					post.addHeader("Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
//					post.addHeader
					try {
						tmpDownloader.download(post, 20000);
						cookies = tmpDownloader.getCookieContext().getCookieStore().getCookies();
						if(cookies!=null) break;
						else getCookieRetry++;
					} catch (DownloadException e) {
						e.printStackTrace();
					}
				}  catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			cookies = this.getCookieContext().getCookieStore().getCookies();
		}
		if(cookies==null){
			logger.info(" cookie=null:downloader="+getCookieUrl);
		}
		Date end=new Date();
		logger.info("获取cookie花费时间："+(end.getTime()-begin.getTime()));
		for(Cookie cookie:cookies){
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
	private int getPageSize(String dateType){
		if(DateType.PUB_DATE.equals(dateType)){
			return 5;//上传日期，只支持每页5条数据
		}
		return 20;
	}
	public static void main(String[] args) {
//		System.out.println(getGuid());
		String cookie="868d9cff59647701bf18358c4cdc2d08a36db045";
		String guid=getGuid();
		String yzm=getNumber(cookie, guid);
		System.out.println(getVlx5("868d9cff59647701bf18358c4cdc2d08a36db045"));
		System.out.println("yzm="+yzm);
	}
}
