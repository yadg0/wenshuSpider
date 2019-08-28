package com.jd.spider.wenshu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.DateTimeAtCompleted;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.annotation.Request;
import com.geccocrawler.gecco.downloader.DefaultWebClientDownloader;
import com.geccocrawler.gecco.downloader.HttpClientDownloader;
import com.geccocrawler.gecco.downloader.proxy.ProxysPool;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
import com.jd.spider.wenshu.domain.ArticleEntity;
import com.jd.spider.wenshu.domain.ArticlePageTask;
import com.jd.spider.wenshu.domain.Court;
import com.jd.spider.wenshu.service.DateType;
import com.jd.spider.wenshu.service.PackageType;
import com.jd.spider.wenshu.service.State;
import com.jd.spider.wenshu.service.WenshuService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 所有大分类获取
 * 
 * @author yangdongjun
 *
 */
@Gecco(matchUrl = "http://wenshu.court.gov.cn", pipelines = { "consolePipeline", "sortListPipeline" }) // ,
																										// "allSortPipeline"
public class WenshuMain implements HtmlBean {
	private static Log log = null;
	
	private static final long serialVersionUID = 665662335318691818L;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static int packageType = 0;// 0java开发环境;1打jar包环境
	public static String driverPath = "E:/opensource/selenium/geckodriver-v0.20.1-win64/geckodriver.exe";
	public static boolean step1GetSortAndCourt = false;// 测试获取分类、1234级法院。
	public static String step1AllProvince="";
	public static List<String> step1ContinueProvince = Arrays.asList();// "法院地域","山西省","吉林省"
	
	public static boolean step2SaveArticleListTask = false;
	public static Date step2_startDate;
	public static Date step2_endDate;
	public static int step2_stepDate;
	
	public static boolean step3GetArticleList = false; // 如果法院都获取完了，可以从数据库中加载法院，并开始获取 文章列表
	public static Integer step3_startPos = 0;// 步骤2中，开始法院index
	public static Integer step3_endPos = 2;// 步骤2中，结束法院index
	public static Date step3_startDate = new Date();
	public static Date step3_endDate = new Date();
	public static Integer step3_maxTotal = 500;// 当大于200条记录时，需要拆分为子请求
	
	public static boolean step4GetArticleDetail = false;// 如果文章列表获取完了，可以从数据库中读取列表，开始
	public static int step4GetArticleDetail_threads=10; //下载文章内容的线程数
	public static int step4GetArticleDetail_interval=100;
	public static int step4GetArticleDetail_proxy_max_article=1000;//每个代理，最大获取文章数量
	public static boolean step5DecodeArticleData = false;// 清洗文章数据。

	public static int step45_startPos;
	public static int step45_endPos;
	public static Date step45_startDate;
	public static Date step45_endDate;
	public static int step45_stepDate;

	public static String crakerHtmlPath = "file:///e:/个人资料/文书网/";
	public static String crakerYzmHtml = "test_new.html";
	public static String decodeDataHtml = "doc.html";
	public static String decodeDataDestHtml = "doc_dest.html";
	public static String step5DeocdeIllegalJs="情节严重,数额较大,数额巨大,数额特别巨大,其他应当归共同所有的财产";
	
	public static int yzmUseTimes = 12;// 验证码，超过n次后，重新获取
	public static String nodeIp="";//本机ip
	
	public static AtomicInteger appState=new AtomicInteger(AppState.FREE);
	public static String configwaitObj="";//String,Integer对象赋值后，不能作为wait和notify的锁对象
	public static boolean configChanged=false;
	/////代理池相关设置
	public static ProxysPool etProxys=new ProxysPool();//代理池
	public static int getProxyTotals=0;  //获取到的代理ip总数，每天会清0
	public static String proxyReqUrl="";
	// zooKeeper相关配置
	public static String zooMasterHostPort = null;
	public static String zooDataPath=null;
	
	///每天获取最新数据时，重试次数。
	private static boolean isMasterIp=false;//主控机器才会执行update语句
	private static int maxRetryTimes=5;
	private static int step1retryTimes=3;
	private static int step2retryTimes=2;
	private static int step3retryTimes=5;
	private static int step4retryTimes=5;
	private static int step5retryTimes=2;
	
	public static void main(String[] args) {
		Properties prop = initProperties();
		System.out.println("ip in prop:"+prop.getProperty("ip"));
		if(prop.getProperty("ip")==null){
			nodeIp=getV4IP();
		}else{
			nodeIp=prop.getProperty("ip");
		}
		System.out.println("ip=="+nodeIp);
		// if(true) return;
		boolean isLoadConfig=initZooWatcher(prop);
		if(isLoadConfig){
			System.out.println("配置不为null，已提交到zookeeper，退出应用！");
			return;
		}
		WenshuService.initialConnection();
		
		if(!ConfigWatcher.zooKeeperLoaded){
			initZooProperties(prop,null);
		}
		while(true){
			appState.set(AppState.BUSY);
			runTask();
			if(isMasterIp && maxRetryTimes>0){
				continue;
			}
			//设置休眠线程
	    	synchronized (appState) {
    			appState.set(AppState.FREE);
    			appState.notify();
			}
	    	System.out.println("程序运行完，等待配置变更。。。"+appState);
	    	synchronized (configwaitObj) {
	    		try {
					configwaitObj.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	    }

	}

	private static void runTask() {
		DefaultWebClientDownloader.initDriver(driverPath);
		if(isMasterIp){
			maxRetryTimes--;
			updateFailedTaskState();
		}
		log.info("开始抓取任务，重试任务次数分别是："+step1retryTimes+","+step2retryTimes+","+step3retryTimes+","+step4retryTimes+","+step5retryTimes);
		if (step1GetSortAndCourt) {//获取法院信息
			List<String> provinces= Arrays.asList(step1AllProvince.split(","));
			//获取所有2级和三级
			for(String province:provinces){
				HttpPostRequest request=new HttpPostRequest("http://wenshu.court.gov.cn/Index/GetCourt");
				request.addHeader("Referer", "http://wenshu.court.gov.cn");
				request.addHeader("X-Requested-With", "XMLHttpRequest");
				request.setCharset("UTF-8");
				System.out.println("get "+province+" 法院信息。。。");
				request.addField("province", province);
				GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true)
				// 开始抓取的页面地址
				.start(request)
				// 开启几个爬虫线程
				.thread(1)
				// 单个爬虫每次抓取完一个请求后的间隔时间
				.interval(2000).run();
			}
			//从树中整理所有的4级请求
			for(TreeItem secondItem:ProvinceCourt.allCourtTree){
				String keyCodeArrayStr="";
				if(secondItem.getChildren()==null || secondItem.getChildren().size()==0)
					continue;
				for(TreeItem thirdItem:secondItem.getChildren()){
					keyCodeArrayStr=keyCodeArrayStr+thirdItem.getCode()+",";
				}
				if(keyCodeArrayStr.length()>0)
					keyCodeArrayStr=keyCodeArrayStr.substring(0, keyCodeArrayStr.length()-1);
				HttpPostRequest request=new HttpPostRequest("http://wenshu.court.gov.cn/Index/GetChildAllCourt");
				request.addHeader("Referer", "http://wenshu.court.gov.cn");
				request.addHeader("X-Requested-With", "XMLHttpRequest");
				request.setCharset("UTF-8");
				request.addField("keyCodeArrayStr", keyCodeArrayStr);
				GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true)
				.start(request)
				.thread(1)
				.interval(2000).run();
			}
			saveCourtTreeToDb(0l,ProvinceCourt.allCourtTree);
			if(isMasterIp)
				step1retryTimes--;
			/**老代码
//			HttpGetRequest start = new HttpGetRequest("http://wenshu.court.gov.cn");
//			start.setCharset("UTF-8");
//			GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true)
//					// 开始抓取的页面地址
//					.start(start)
//					// 开启几个爬虫线程
//					.thread(1)
//					// 单个爬虫每次抓取完一个请求后的间隔时间
//					.interval(2000).run();
//			System.out.println("-------获取12级类目" + JSON.toJSONString(SortListPipeline.sortRequests));
//			// 获取案件下的所有1,2级类目
//			GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true)
//					// 开始抓取的页面地址
//					.start(SortListPipeline.sortRequests)
//					// 开启几个爬虫线程
//					.thread(1)
//					// 单个爬虫每次抓取完一个请求后的间隔时间
//					.interval(2000).run();
//			// if(true)return;
//			System.out.println("-------获取3级类目开始，所有1、2级为：" + JSON.toJSONString(PageSortDetail.allSortTree));
//			// 获取3级类目
//			GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true).start(SortDetailPipeline.sortRequests)
//					.thread(1).interval(1000).run();
//
//			System.out.println("-------获取4级类目开始，所有123级为："
//					+ JSON.toJSONString(PageSortDetail.allSortTree.values().iterator().next()));
//			// 获取4级类目
//			GeccoEngine.create().classpath("com.jd.spider.wenshu").debug(true)
//					// 开始抓取的页面地址
//					.start(SortDetailGetSubTreeItemPipeline.sortRequests4)
//					.thread(2)
//					.interval(1000).run();
//			System.out.println("-------分页获取文档开始，所有1234级为：" + JSON.toJSONString(PageSortDetail.allSortTree));
//			saveCourtTreeToDb(PageSortDetail.allSortTree);
 * **/
		}
		// 第二步，只生成获取文件id的任务
		if (step2SaveArticleListTask && step2retryTimes>0) {
			PageSortDetail.allSortTree = loadCourtFromDb();
			getArticleListRequestFromTree(PageSortDetail.allSortTree);
			if(isMasterIp)
				step2retryTimes--;
		}
		// 第三步，执行获取文件id任务
		if (step3GetArticleList && step3retryTimes>0) {
			List<HttpRequest> articleListRequests = null;
			int retryTimes = 0;// 重试失败任务的次数
			while (true) {
				if(configChanged){
					System.out.println("配置文件改变，step3GetArticleList 重新加载配置。。。");
					break;
				}
				articleListRequests = loadFailedTaskFromDb(nodeIp);
				System.out.println("加载了" + articleListRequests.size() + "条数据。。");
				if (articleListRequests == null || articleListRequests.size() == 0) {
					//如果是master，则需要休眠
					if(isMasterIp){//master，2 分钟获取一次数据，三次后还没有最新的数据，就执行下一个任务了，此时配置改变了，要2分钟之后才能生效
						try {
							Thread.sleep(2 * 60 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (retryTimes > 3){
							break;
						}
					}else{// 非master，1分钟扫描下数据库，有失败的任务就一直处理，除非 configchanged
						try {
							Thread.sleep( 60 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					retryTimes++;
					continue;
				} else {
					retryTimes = 0;
				}
				WebClientArticleListDownloader2.articleListRequest = articleListRequests;
				GeccoEngine.create().classpath("com.jd.spider.wenshu")
					.debug(true)
//					.proxy(true)
//					.proxysLoader(etProxys)
					.start(articleListRequests.get(0))
					.retry(10).thread(1).interval(2000).run();
				System.out.println("处理了一批记录。。。");
			}
			if(isMasterIp)
				step3retryTimes--;
		}

		if (step4GetArticleDetail && step4retryTimes>0) {
			if(etProxys==null || etProxys.getAvalibleProxy()==0) step4GetProxyIps();
			int retryTimes = 0;// 重试失败任务的次数
			int succCount=0;
			int oldSuccCount=0;//每1000条会换ip
			while (true) {
				if(configChanged){
					System.out.println("配置文件改变，step4GetArticleDetail 重新加载配置。。。");
					break;
				}
				Date begin=new Date();
				try {
					ArticleListPipeline.articles = loadArticleFromDb();// 获取状态为0的文章
					log.info("得到文章数==="+ArticleListPipeline.articles.size());
					if (ArticleListPipeline.articles == null || 
							ArticleListPipeline.articles.size() == 0) {
						Long updated=updateFailedArticleState(State.NEW);
						int sleepTime=6000;
						if(updated==null || 0==updated.intValue()){
							sleepTime=sleepTime*10;
							log.info("需要休眠。。。"+sleepTime);
							Thread.sleep(sleepTime);// 1分钟扫描下数据库，有失败的任务就重新处理
						}
						retryTimes++;
						if(isMasterIp && retryTimes>3){//是主机，3次内得不到新的要获取内容的文章，就进入下一个任务
							break;
						}
					} else {
						retryTimes = 0;
					}
				} catch (Exception e) {
					e.printStackTrace();
					retryTimes++;
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}//只休眠1分钟
					continue;
				}
				boolean bUserProxy=etProxys.getAvalibleProxy()>0;
				int step4GetArticleDetail_threads=etProxys.getAvalibleProxy();
				int step4GetArticleDetail_interval=WenshuMain.step4GetArticleDetail_interval;
				if(etProxys.getAvalibleProxy()==0){
					step4GetArticleDetail_threads=1;
					step4GetArticleDetail_interval=100;
				}else{
					step4GetArticleDetail_threads=etProxys.getAvalibleProxy()*4;
				}
				// 获取每个文章内容
				if (ArticleListPipeline.articles.size() > 0) {
					Article.finishedArticle=new ArrayList<Article>();
					List<HttpRequest> requests = getRequestFromArticleList(ArticleListPipeline.articles);
					//System.out.println("-------获取文档内容：" + JSON.toJSONString(ArticleListPipeline.articles));
					GeccoEngine.create()
					.classpath("com.jd.spider.wenshu").debug(true)
							.start(requests)
							.proxy(bUserProxy)
							.proxysLoader(etProxys)
							.retry(3)
							.thread(step4GetArticleDetail_threads)
							.interval(step4GetArticleDetail_interval).run();
				}
				//更新数据库
				succCount=succCount+Article.finishedArticle.size();
				log.info("一批任务下载完成。。。更新数据，成功数"+Article.finishedArticle.size()+",总记录数:"+succCount);
				WenshuService.updateArticleList(Article.finishedArticle);
				Date end=new Date();
				log.info("一批任务下载完成。,总耗时："+(end.getTime()-begin.getTime())+"，代理可用数："+etProxys.getAvalibleProxy());
				if(etProxys.getAvalibleProxy()/(float)(WenshuMain.step4GetArticleDetail_threads)<=0.8f || (succCount-oldSuccCount)>=step4GetArticleDetail_proxy_max_article*step4GetArticleDetail_threads){//每下载 ip数*1000 条，换ip
					oldSuccCount=succCount;
					step4GetProxyIps();
				}else{
					try {
						System.out.println("==="+Article.finishedArticle.size()+"ArticleListPipeline.articles.size()="+ArticleListPipeline.articles.size());
						if(etProxys.getAvalibleProxy()==0&&(Article.finishedArticle.size()<=ArticleListPipeline.articles.size()/10)){
							System.out.println("文章内容成功数小于一半的目标，可能服务器在维护。。休眠15分钟");
							Thread.sleep(15*60*1000);//被封号了，休眠15分钟
							etProxys=new ProxysPool();
							step4GetProxyIps();
						}else{
							Thread.sleep(500);
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			if(isMasterIp)
				step4retryTimes--;
		}

		if (step5DecodeArticleData && step5retryTimes>0) {//第5步骤，可以直接支持，跨年清洗数据。
				Date tmpStartDate = new Date(step45_startDate.getTime());
				for (; tmpStartDate.compareTo(step45_endDate) <= 0;) {// 从2010-2018年
					Date tmpEndDate = new Date(tmpStartDate.getTime());
					if (step45_stepDate == 15) {// 按半月拆分任务
						if (tmpStartDate.getDate() <= 15) {
							tmpEndDate.setDate(15);
						} else {
							tmpEndDate.setMonth(tmpEndDate.getMonth() + 1);
							tmpEndDate.setDate(0);
						}
					} else if (step45_stepDate == 365) {// 按一年拆分任务
						tmpEndDate.setYear(tmpEndDate.getYear() + 1);
						tmpEndDate.setMonth(0);
						tmpEndDate.setDate(0);// 得到开始时间一年的最后一天
					} else if (step45_stepDate == 30) {// 按一月拆分任务
						tmpEndDate.setMonth(tmpEndDate.getMonth() + 1);
						tmpEndDate.setDate(0);// 得到开始时间月份的最后一天
					}
					// 转成字符串
					String tmpDateStr = sdf.format(tmpStartDate);
					while(true){
						if(configChanged){
							System.out.println("配置文件改变，step5DecodeArticleData 重新加载配置。。。");
							break;
						}
						System.out.println("begin loading article:"+tmpDateStr);
						List<ArticleEntity> articles = loadArticleEntityFromDb(tmpDateStr);
						System.out.println("article loaded"+(articles==null?0:articles.size()));
						if (articles == null || articles.size() == 0) {
							break;
						}
						getDecodeData(articles);
					}
					tmpStartDate = new Date(tmpEndDate.getTime());
					tmpStartDate.setDate(tmpStartDate.getDate() + 1);
				}
				if(isMasterIp)
					step5retryTimes--;
		}
	}

	/**
	 * 读取本地zooKeeper配置：zooMasterHostPort,zooDataPath,node.i.ips,node.i.config
	 * @param prop
	 * @return
	 */
	private static boolean initZooWatcher( Properties prop) {
		zooMasterHostPort = prop.getProperty("zooMasterHostPort");
		zooDataPath=prop.getProperty("zooDataPath");
		if(StringUtils.isEmpty(zooMasterHostPort)) return false;
		//如果配置中有 node.main.ip，那么就是主控机器
		JSONArray jas=new JSONArray();
		for(int i=0;i<10;i++){
			String nodeIds=prop.getProperty("node."+i+".ips");
			String nodeConf=prop.getProperty("node."+i+".config");
			if(StringUtils.isEmpty(nodeIds)||StringUtils.isEmpty(nodeConf)){
				continue;
			}else{
				String[] ips=nodeIds.split(",");
				JSONArray ja=new JSONArray();
				for(int j=0;j<ips.length;j++){
					ja.add(ips[j]);
				}
				nodeConf=prop.getProperty("node."+i+".config");
				JSONObject tmpJo=new JSONObject();
				tmpJo.put("ips", ja);
				tmpJo.put("config", nodeConf);
				jas.add(tmpJo);
			}
		}
		String zooConifgs=jas.size()==0?null:jas.toJSONString();
		System.out.println("zooMasterHostPort="+zooMasterHostPort+",zooDataPath="+zooDataPath+",conifgs=="+jas.toJSONString());
		new ConfigWatcher(zooMasterHostPort,zooDataPath,zooConifgs);
		return zooConifgs!=null;
	}

	/**
	 * 由listener调用
	 * @param prop
	 * @param in
	 */
	public static void initZooProperties(Properties prop,InputStream in) {
		configChanged=true;//发出改变配置信号
		System.out.println("设置 configChanged="+configChanged);
		if(in!=null){
			prop=new Properties();
			try {
				prop.load(new InputStreamReader(in,"UTF-8"));
				synchronized (appState) {
					System.out.println("判断应用是否还在运行 appState="+appState);
					if(appState.get()==AppState.BUSY){
						System.out.println("等待任务跑完！");
						appState.wait();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		try {
//			driverPath = prop.getProperty("driverPath");
//			isDayTask = Boolean.parseBoolean(prop.getProperty("isDayTask"));
//			if(isDayTask){
				isMasterIp=nodeIp.equals(prop.getProperty("masterIp"));
				resetDayRetryTimes();
//			}
				
			step1GetSortAndCourt = Boolean.parseBoolean(prop.getProperty("step1GetSortAndCourt"));
			step1AllProvince = prop.getProperty("step1AllProvince");
			
			step2SaveArticleListTask = Boolean.parseBoolean(prop.getProperty("step2SaveArticleListTask"));
			step2_startDate = prop.getProperty("step2_startDate")==null?new Date():DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step2_startDate"));
			step2_endDate = StringUtils.isEmpty(prop.getProperty("step2_endDate"))?getEndDateByStartDate(step2_startDate):DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step2_endDate"));
			step2_stepDate = prop.getProperty("step2_stepDate")==null?1:Integer.parseInt(prop.getProperty("step2_stepDate"));
			
			step3GetArticleList = Boolean.parseBoolean(prop.getProperty("step3GetArticleList"));
			step3_startPos = prop.getProperty("step3_startPos")==null?0:Integer.parseInt(prop.getProperty("step3_startPos"));
			step3_endPos = prop.getProperty("step3_endPos")==null?2:Integer.parseInt(prop.getProperty("step3_endPos"));
			step3_startDate = prop.getProperty("step3_startDate")==null?new Date():DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step3_startDate"));
			step3_endDate = StringUtils.isEmpty(prop.getProperty("step3_endDate"))?getEndDateByStartDate(step3_startDate):DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step3_endDate"));
//			step3_stepDate = prop.getProperty("step3_stepDate")==null?1:Integer.parseInt(prop.getProperty("step3_stepDate"));
			step3_maxTotal = prop.getProperty("step3_maxTotal")==null?200:Integer.parseInt(prop.getProperty("step3_maxTotal"));

			step4GetArticleDetail = Boolean.parseBoolean(prop.getProperty("step4GetArticleDetail"));
			step4GetArticleDetail_threads = prop.getProperty("step4GetArticleDetail_threads")==null?2:Integer.parseInt(prop.getProperty("step4GetArticleDetail_threads"));
			step4GetArticleDetail_interval = prop.getProperty("step4GetArticleDetail_interval")==null?100:Integer.parseInt(prop.getProperty("step4GetArticleDetail_interval"));

			step45_startPos = prop.getProperty("step45_startPos")==null?0:Integer.parseInt(prop.getProperty("step45_startPos"));
			step45_endPos = prop.getProperty("step45_endPos")==null?2:Integer.parseInt(prop.getProperty("step45_endPos"));
			step45_startDate = prop.getProperty("step45_startDate")==null?new Date():DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step45_startDate"));
			step45_endDate = StringUtils.isEmpty(prop.getProperty("step45_endDate"))?getEndDateByStartDate(step45_startDate):DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.parse(prop.getProperty("step45_endDate"));
			step45_stepDate = prop.getProperty("step45_stepDate")==null?1:Integer.parseInt(prop.getProperty("step45_stepDate"));

			step5DecodeArticleData = Boolean.parseBoolean(prop.getProperty("step5DecodeArticleData"));
			step5DeocdeIllegalJs = prop.getProperty("step5DeocdeIllegalJs")==null?step5DeocdeIllegalJs:prop.getProperty("step5DeocdeIllegalJs");
//			crakerHtmlPath = prop.getProperty("crakerHtmlPath");
			yzmUseTimes = prop.getProperty("yzmUseTimes")==null?10:Integer.parseInt(prop.getProperty("yzmUseTimes"));
			proxyReqUrl = prop.getProperty("proxyReqUrl");
			
		} catch (Exception e) {
			e.printStackTrace();
			step3_startDate = new Date(2017 - 1900, 0, 01);
			step3_endDate = new Date(2017 - 1900, 11, 31);
			System.out.println("读取配置文件出错，将使用默认值。");
			packageType = PackageType.NO_PACKAGE;
			e.printStackTrace();
		}

		System.out.println("参数：step1GetSortAndCourt=" + step1GetSortAndCourt + "," + "step1ContinueProvince="
				+ step1ContinueProvince + "," + "step3GetArticleList=" + step3GetArticleList + ","
//				+ "step3LoadTaskFromDb=" + step3LoadTaskFromDb + "," 
				+ "startPos="+ step3_startPos + "," 
				+ "endPos=" + step3_endPos + ","
				+ "step4GetArticleDetail=" + step4GetArticleDetail + "," + "step2SaveArticleListTask="
				+ step2SaveArticleListTask + "," 
				+ ",step5DecodeArticleData=" +step5DecodeArticleData+ "," 
//				+ "step2GetArticleList_saveTask=" + step2GetArticleList_saveTask
				+ "," + "startDate=" + step3_startDate + "," + "endDate=" + step3_endDate + "," + "stepDate=" + ","
				+ "maxTotal=" + step3_maxTotal + ",");
		configChanged=false;//发出改变配置信号
		System.out.println("设置配置已改变2 configChanged="+configChanged+",唤醒等待配置的进程。");
		getProxyTotals=0;
		//通知参数改变了，可以从新执行任务了
		synchronized (configwaitObj) {
			configwaitObj.notify();
		}
}
	
	private static void resetDayRetryTimes() {
		step1retryTimes=3;
		step2retryTimes=2;
		step3retryTimes=5;
		step4retryTimes=5;
		step5retryTimes=2;
		maxRetryTimes=step1retryTimes;
		maxRetryTimes=maxRetryTimes>step2retryTimes?maxRetryTimes:step2retryTimes;
		maxRetryTimes=maxRetryTimes>step3retryTimes?maxRetryTimes:step3retryTimes;
		maxRetryTimes=maxRetryTimes>step4retryTimes?maxRetryTimes:step4retryTimes;
		maxRetryTimes=maxRetryTimes>step5retryTimes?maxRetryTimes:step5retryTimes;
	}
	
	/**
	 * 更新失败任务的状态
	 */
	private static void updateFailedTaskState(){
		log.info("开始更新失败的任务！"+step3retryTimes+","+maxRetryTimes);
		ArticlePageTask task = new ArticlePageTask();
		task.setDate(sdf.format(step2_startDate));
		task.setEndDate(sdf.format(step2_endDate));
		task.setDateType(DateType.PUB_DATE);
		//这里不自动update，需要手动update任务和文章列表
//		if(step3retryTimes==maxRetryTimes){
			//是接受了配置变更参数，需要update所有的 task状态为0,否则，只更新失败的task
//			task.setState(1);
//		}
		WenshuService.updateFailedArticlePageTask(task);
		ArticleEntity articleEntity=new ArticleEntity();
		articleEntity.setTaskStartDate(sdf.format(step2_startDate));
		WenshuService.updateFailArticleToNew(articleEntity);
	}
	/**
	 * 通过开始时间获取结束时间，如果开始日期是当前年，那么获取当前日期；如果是以前的年份，那么为开始日期年最后一天
	 * @param step2_startDate2
	 * @return
	 */
	private static Date getEndDateByStartDate(Date startDate) {
		Date now=new Date();
		if(startDate.getYear()==now.getYear()){
			
		}else{
			now.setYear(startDate.getYear());
			now.setMonth(11);
			now.setDate(31);
		}
		return now;
	}
	/**
	 * 判断是jar运行还是非jar，并读取wenshu.prop文件
	 * @return
	 */
	private static Properties initProperties() {
		Properties prop = new Properties();
		InputStreamReader in;
		String customizedPath = "log4j.xml";
		File jarConfFile=new File(System.getProperty("user.dir") + File.separator + "wenshu-conf.prop");
		try {
			if(jarConfFile.exists()){//是jar
				System.out.println("config File:" + System.getProperty("user.dir") + File.separator + "wenshu-conf.prop");
					in = new InputStreamReader(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "wenshu-conf.prop")),"UTF-8");
				packageType = PackageType.JAR;
				customizedPath = System.getProperty("user.dir") + File.separator + "log4j.xml";
				customizedPath =customizedPath.replaceAll("\\\\", "/");
				crakerHtmlPath="file:///"+System.getProperty("user.dir").replaceAll("\\\\", "/")+"/html/";
				System.out.println("--log4j.xml===="+customizedPath+",crakerHtmlPath="+crakerHtmlPath);
			}else{//不是jar
				in = new InputStreamReader(ClassLoader.getSystemResourceAsStream("wenshu-conf.prop"),"UTF-8");
				customizedPath = ClassLoader.getSystemResource("log4j.xml").getPath();
				crakerHtmlPath="file://"+ClassLoader.getSystemResource(".").getPath()+"/html/";
				packageType = PackageType.NO_PACKAGE;
			}
			
		    System.setProperty("log4j.configuration", customizedPath);
		    DOMConfigurator.configure(customizedPath);
		    System.out.println("log4j path===:"+customizedPath+","+System.getProperty("log4j.configuration"));
		    log= LogFactory.getLog(WenshuMain.class);
		    prop.load(in);
		    driverPath = prop.getProperty("driverPath");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}

	private static List<HttpRequest> loadFailedTaskFromDb(String ip) {
		ArticlePageTask taskParam = new ArticlePageTask();
		taskParam.setDate(DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(step3_startDate));
		taskParam.setEndDate(DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(step3_endDate));
		taskParam.setOrderBy("date");
		taskParam.setStartIndex(step3_startPos);
		taskParam.setPageSize(step3_endPos - step3_startPos);
		List<HttpRequest> result = new ArrayList<HttpRequest>();
		List<ArticlePageTask> tasks = WenshuService.findFailedArticlePageTask(taskParam,ip);
		for (ArticlePageTask task : tasks) {
			HttpRequest request = new HttpGetRequest();
			request.setCharset("utf-8");
			request.addHeader("Referer", "http://wenshu.court.gov.cn");
			request.addHeader("X-Requested-With", "XMLHttpRequest");
			String courtName = task.getCourtName();
			String url = null;
			try {
				Court param=new Court();
				param.setId(task.getCourtId());
				List<Court> courtList=WenshuService.getCourtList(param);
				if(courtList!=null&&courtList.size()>0){
					param=courtList.get(0);
				}else{
					continue;
				}
				url = "http://wenshu.court.gov.cn/List/List/?sorttype=1" + "&conditions=searchWord++SLFY++"
						+ URLEncoder.encode("法院名称", "UTF-8") + ":" + URLEncoder.encode(courtName, "UTF-8");
				if(DateType.JUDGE_DATE.equals(task.getDateType())){
					url = url + "&conditions=searchWord++CPRQ++" + URLEncoder.encode(DateType.JUDGE_DATE, "UTF-8") + ":" + task.getDate()
					+ "%20TO%20" + task.getEndDate();
				}else{
					url = url + "&conditions=searchWord+++"+task.getDate()+"%20TO%20"
				    +task.getEndDate()+"+"+URLEncoder.encode(task.getDateType(), "UTF-8")
				    +":"+task.getDate()+"%20TO%20"+task.getEndDate();
				}
				
				if(courtName.indexOf("县人民法院")>-1){
//					url = "http://wenshu.court.gov.cn/List/List/?sorttype=1" +
//							"&conditions=searchWord+"+URLEncoder.encode(courtName, "UTF-8")+
//							"+++"+ URLEncoder.encode("基层法院", "UTF-8") + ":" + URLEncoder.encode(courtName, "UTF-8");
//					url = url + "&conditions=searchWord++CPRQ++" + URLEncoder.encode("裁判日期", "UTF-8") + ":" + task.getDate()
//									+ "%20TO%20" + task.getEndDate();
					url=url+"&conditions=searchWord+"+URLEncoder.encode(courtName, "UTF-8")+
							"+++"+ URLEncoder.encode("基层法院", "UTF-8") + ":" + URLEncoder.encode(courtName, "UTF-8");
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			request.setUrl(url);
			result.add(request);
		}

		return result;
	}
	
	/**
	 * 4步，从数据库查不到文章后，设置所有下载内容失败的文章的状态为新增，这样可以重试
	 * @param state
	 * @return 设置的行数
	 */
	private static Long updateFailedArticleState(int state) {
		ArticleEntity param = new ArticleEntity();
		param.setTaskStartDate(DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(WenshuMain.step45_startDate));
		return WenshuService.updateFailArticleToNew(param);
	}
	
	private static List<HttpRequest> getRequestFromArticleList(List<Article> articles) {
		List<HttpRequest> httpRequest = new ArrayList<HttpRequest>();
		for (Article article : articles) {
			HttpRequest request = new HttpGetRequest();
			request.setUrl("http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID=" + article.getId());
			request.setCharset("UTF-8");
			request.addHeader("Referer", "http://wenshu.court.gov.cn/List/List");
			request.addParameter("tb", article.getTaskStartDate());
			httpRequest.add(request);
		}
		return httpRequest;
	}

	/**
	 * 加载new 状态 文章,同时更新文章的状态为 下载中
	 * 
	 * @param param
	 * @return
	 */
	private static List<Article> loadArticleFromDb() {
		ArticleEntity param = new ArticleEntity();
		param.setOrderBy("id");
		param.setStartIndex(step45_startPos);
		param.setPageSize(step45_endPos - step45_startPos);
		param.setState(State.NEW);// 只获取未下载的文章
		// 设置年份，从不同表中获取数据
		param.setTaskStartDate(DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(WenshuMain.step45_startDate));
		System.out.println("内容：开始获取文章列表："+JSON.toJSONString(param));
		List<ArticleEntity> articleEntityList = WenshuService.getArticleEntityList(param);
		System.out.println("内容：获取文章列表完成");
		List<Article> articles = new ArrayList<Article>();
		for (ArticleEntity articleEntity : articleEntityList) {
			Article article = new Article();
			article.setId(articleEntity.getDocId());
			article.setTitle(articleEntity.getTitle());
			article.setUrl("http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID=" + article.getId());
			article.setTaskStartDate(articleEntity.getTaskStartDate());
			articles.add(article);
			// articleEntity.setState(State.DONLOADING);
			// WenshuService.updateArticleState(articleEntity);
		}
		return articles;
	}

	/**
	 * 加载downloaded文章实体,同时更新文章的状态为 解码中
	 * 
	 * @param param
	 * @return
	 */
	private static List<ArticleEntity> loadArticleEntityFromDb(String startDate) {
		ArticleEntity param = new ArticleEntity();
		param.setOrderBy("id");
		param.setStartIndex(step45_startPos);
		param.setPageSize(step45_endPos - step45_startPos);
		param.setState(State.DOWNLOADED);// 只获取未下载的文章
		// 设置年份，从不同表中获取数据
		param.setTaskStartDate(startDate);
		return WenshuService.getArticleEntityList(param);
	}

	private static Map<String, List<TreeItem>> loadCourtFromDb() {
		Map<String, List<TreeItem>> result = new HashMap<String, List<TreeItem>>();
		Court courtParam = new Court();

		courtParam.setLevel(2);
		List<Court> courtList = WenshuService.getCourtList(courtParam);
		List<TreeItem> tree = new ArrayList<TreeItem>();
		Map<Long, TreeItem> treeMap = new HashMap<Long, TreeItem>();
		for (Court court : courtList) {
			TreeItem treeItem = new TreeItem();
			treeItem.setParam(court.getParam());
			treeItem.setParval(court.getName());
//			if ("最高人民法院".equals(court.getName()) || "新疆维吾尔自治区高级人民法院生产建设兵团分院".equals(court.getName())) {
//				treeItem.setParval(court.getName());
//			} else {//
//				treeItem.setParval(court.getName() + "高级人民法院");
//			}
			treeItem.setLevel(court.getLevel());
			treeItem.setDbCourtId(court.getId());
			tree.add(treeItem);
			treeMap.put(court.getId(), treeItem);
		}
		TreeItem continueItems = new TreeItem();
		continueItems.setDbCourtId(-999l);
		continueItems.setLevel(2);
		continueItems.setChildren(new ArrayList<TreeItem>());
		tree.add(continueItems);
		treeMap.put(-999l, new TreeItem());
		// 加载所有2级
		// if(step2GetArticleList_continue)
		// courtParam.setTaskState(State.NEW);
		for (int i = 3; i <= 10; i++) {
			courtParam.setLevel(i);
			courtList = WenshuService.getCourtList(courtParam);
			for (Court court : courtList) {
				Long parentId = court.getKey();
				TreeItem parent = treeMap.get(parentId);
				if (parent == null) {
					parent = continueItems;
				}
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<TreeItem>());
				}
				TreeItem treeItem = new TreeItem();
				treeItem.setParam(court.getParam());
				treeItem.setParval(court.getName());
				treeItem.setLevel(court.getLevel());
				treeItem.setDbCourtId(court.getId());
				parent.getChildren().add(treeItem);
				treeMap.put(court.getId(), treeItem);
			}
		}
		result.put("刑事案件", tree);
		return result;
	}

	@Deprecated
	private static void saveArticleList(List<Article> articles) {
		for (Article article : articles) {
			ArticleEntity articleEntity = new ArticleEntity();
			articleEntity.setDocId(article.getId());
			articleEntity.setState(State.NEW);
			WenshuService.addArticle(articleEntity);
		}

	}

	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context, HttpRequest request,
			HttpResponse response) {

	}

	/**
	 * 遍历树，获取文章列表请求，保存到 task表中
	 * 
	 * @param allTree
	 * @return
	 */
	private static List<HttpRequest> getArticleListRequestFromTree(Map<String, List<TreeItem>> allTree) {
		List<HttpRequest> listResult = new ArrayList<HttpRequest>();
		for (List<TreeItem> tree : allTree.values()) {
			listResult.addAll(getArticleListRequest(0l, tree));
		}
		return listResult;
	}

	/**
	 * 获取请求列表，如果是stepdate=15,30,365使用裁判日期搜索，其他使用发布日期搜索
	 * 按裁判日期：wenshu.court.gov.cn/list/list/?sorttype=1&conditions=searchWord+夏邑县人民法院+SLFY++法院名称:夏邑县人民法院&conditions=searchWord++CPRQ++裁判日期:2015-03-24 TO 2015-03-24
	 * 按发布日期：wenshu.court.gov.cn/list/list/?sorttype=1&conditions=searchWord+夏邑县人民法院+SLFY++法院名称:夏邑县人民法院&conditions=searchWord+++2015-03-24 TO 2015-03-25+上传日期:2015-03-24 TO 2015-03-25
	 * @param parentId
	 * @param tree
	 * @return
	 */
	private static List<HttpRequest> getArticleListRequest(Long parentId, List<TreeItem> tree) {
		List<HttpRequest> listResult = new ArrayList<HttpRequest>();
		for (TreeItem treeItem : tree) {
			// if(treeItem.getLevel()==2 &&
			// !"最高人民法院".equals(treeItem.getParval())){
			// treeItem.setParval(null);
			// }
			// 不管是不是叶子，都需要添加请求
			if (treeItem.getParval() != null) {
				String courtName = treeItem.getParval();
				String url = null;
				try {
					url = "http://wenshu.court.gov.cn/List/List/?sorttype=1" + "&conditions=searchWord++SLFY++"
							+ URLEncoder.encode("法院名称", "UTF-8") + ":" + URLEncoder.encode(courtName, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				// 案件类型:刑事案件,中级法院:吉林省长春林区中级法院
				// 往mysql中插入数据。
				Date tmpStartDate = new Date(step2_startDate.getTime());

				for (; tmpStartDate.compareTo(step2_endDate) <= 0;) {// 从2010-2018年
					Date tmpEndDate = new Date(tmpStartDate.getTime());
					boolean isPubDate=false;
					if (step2_stepDate == 15) {// 按半月拆分任务
						if (tmpStartDate.getDate() <= 15) {
							tmpEndDate.setDate(15);
						} else {
							tmpEndDate.setMonth(tmpEndDate.getMonth() + 1);
							tmpEndDate.setDate(0);
						}
					} else if (step2_stepDate == 365) {// 按一年拆分任务
						tmpEndDate.setYear(tmpEndDate.getYear() + 1);
						tmpEndDate.setMonth(0);
						tmpEndDate.setDate(0);// 得到开始时间一年的最后一天
					} else if (step2_stepDate == 30) {// 按一月拆分任务
						tmpEndDate.setMonth(tmpEndDate.getMonth() + 1);
						tmpEndDate.setDate(0);// 得到开始时间月份的最后一天
					}else{//step2_stepDate=1,每天，每天的任务url需要变化，且结束日期必须大于开始日期一天
						isPubDate=true;
						tmpEndDate.setDate(tmpEndDate.getDate()+step2_stepDate-1);//
					}
					if(tmpEndDate.getYear()!=tmpStartDate.getYear()){//如果年份不相等
						tmpEndDate.setMonth(0);
						tmpEndDate.setDate(0);// 得到开始时间一年的最后一天
					}
					// 转成字符串
					String tmpDateStr = sdf.format(tmpStartDate);
					tmpStartDate = new Date(tmpEndDate.getTime());
//					if(!isPubDate)
					tmpStartDate.setDate(tmpStartDate.getDate() + 1);
					String tmpEndDateStr = sdf.format(tmpEndDate);
					String tmpUrl = null;
					try {
						if(isPubDate){
							tmpUrl=url+"conditions=searchWord+++"+tmpDateStr + "%20TO%20" + tmpEndDateStr+"+"
									+URLEncoder.encode(DateType.PUB_DATE, "UTF-8")+":"+tmpDateStr + "%20TO%20" + tmpEndDateStr;
						}else{
							tmpUrl = url + "&conditions=searchWord++CPRQ++" + URLEncoder.encode(DateType.JUDGE_DATE, "UTF-8") + ":"
									+ tmpDateStr + "%20TO%20" + tmpEndDateStr;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					HttpRequest request = new HttpGetRequest();
					request.setCharset("utf-8");
					request.setUrl(tmpUrl);
					// listResult.add(request);
					// tmpStartDate.setDate(tmpStartDate.getDate()+1);

					ArticlePageTask task = new ArticlePageTask();
					task.setCourtId(treeItem.getDbCourtId());
					task.setCourtName(treeItem.getParval());
					task.setState(State.NEW);
					task.setDate(tmpDateStr);
					task.setEndDate(tmpEndDateStr);
					task.setYn(1);
					task.setDateType(isPubDate?DateType.PUB_DATE:DateType.JUDGE_DATE);
//					if (step2GetArticleList_saveTask)
					WenshuService.addArticlePageTask(task);
				}
			}
			if (treeItem.getChildren() == null || treeItem.getChildren().size() == 0) {
			} else {
				listResult.addAll(getArticleListRequest(treeItem.getDbCourtId(), treeItem.getChildren()));
			}
		}
		return listResult;
	}

	private static void saveCourtTreeToDb(Map<String, List<TreeItem>> allSortTree) {
		for (List<TreeItem> tree : allSortTree.values()) {
			System.out.println("开始保存法院信息："+tree.size());
			saveCourtTreeToDb(0l, tree);
		}
	}

	private static void saveCourtTreeToDb(Long parentId, List<TreeItem> tree) {
		for (TreeItem treeItem : tree) {
			// 不管是不是叶子，都需要添加请求
			Court court = new Court();
//			if (treeItem.getParam() != null) {
				// 案件类型:刑事案件,中级法院:吉林省长春林区中级法院
			System.out.println("保存法院："+treeItem.getParval());
				court.setParam(treeItem.getParam());
				court.setParval(treeItem.getParval());
				court.setName(treeItem.getParval());
				court.setLevel(treeItem.getLevel());
				court.setKey(parentId);
				court.setYn(1);
				WenshuService.addCourt(court);
//			} else {
//				System.out.println("treeItem.getParam()==null");
//			}
			if (treeItem.getChildren() == null || treeItem.getChildren().size() == 0) {
			} else {
				saveCourtTreeToDb(court.getId(), treeItem.getChildren());
			}
		}
	}

	public static void getDecodeData(List<ArticleEntity> articles) {
		// 将本地文件中的doc.html文件，替换内容
		Template template = null;
		Configuration configuration = new Configuration();
		try {
			configuration.setDirectoryForTemplateLoading(new File(crakerHtmlPath.substring(8)));
			template = configuration.getTemplate(decodeDataHtml,"UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		WebClient wc = new WebClient(BrowserVersion.FIREFOX_45);
		try {
			for (ArticleEntity article : articles) {
				try {
					String data = article.getData().replaceAll("var ", "window.")
							.replaceAll("Content\\.Content\\.InitPlugins\\(\\);", "")
							.replaceAll("Content\\.Content\\.KeyWordMarkRed\\(\\);", "");
//							.replaceAll("'情节严重'", "\"情节严重\"");
					data=replaceIllegalJs(data);
//					String data = article.getData();
					paramMap.put("datas", data.trim());
					Writer writer = new OutputStreamWriter(
							new FileOutputStream(crakerHtmlPath.substring(8) + decodeDataDestHtml), "UTF-8");
					template.process(paramMap, writer);
					HtmlPage page = wc.getPage(crakerHtmlPath + decodeDataDestHtml);
					ScriptResult result = page.executeJavaScript("getData()");
					String newData = result.getJavaScriptResult().toString();
					ArticleEntity tmpArticle = new ArticleEntity();
					tmpArticle.setTableSub(article.getTableSub());
					tmpArticle.setDocId(article.getDocId());
					tmpArticle.setState(State.FINISHED);
					tmpArticle.setDecodeData(newData);
					WenshuService.addArticleDecode(tmpArticle);
//					WenshuService.updateArticle(tmpArticle);
					// newData=newData.replaceAll("<[^>]*>","");
				} catch (Exception e) {
					e.printStackTrace();
					Thread.sleep(2000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			wc.close();
		}
	}
	
	private static String replaceIllegalJs(String js){
		String regex="(.*?:\')(.*?)(\'[,|}|;])";
		Matcher mather=Pattern.compile(regex).matcher(js);
		StringBuffer sb=new StringBuffer();
        while(mather.find()) {
        	String tmp=mather.group(2).replaceAll("'", "\\\\\\\\'");
        	mather.appendReplacement(sb, "$1"+tmp+"$3");
        }
        mather.appendTail(sb);
        return sb.toString();
	}
	
	public static String getV4IP() {
		String ip = "";
		String chinaz = "http://2019.ip138.com/ic.asp";

		StringBuilder inputLine = new StringBuilder();
		String read = "";
		URL url = null;
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		try {
			url = new URL(chinaz);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "GBK"));
			while ((read = in.readLine()) != null) {
				inputLine.append(read + "\r\n");
			}
			// System.out.println(inputLine.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		int start = inputLine.indexOf("[") + 1;
		int end = inputLine.indexOf("]");
		System.out.println(inputLine);
		return inputLine.substring(start, end);
	}
	
	private static Date proxyDay=new Date();
	public static int step4GetProxyIps() {
		//一次获取2个ip，同时使用2个ip下载
		//获取代理时，根据不同时段获取不同数量，晚上可以使用10个，平时使用5个
		Date now=new Date();
		if(proxyDay.getDate()!=now.getDate()){
			proxyDay=now;
			getProxyTotals=0;
		}
		int proxyCount=WenshuMain.step4GetArticleDetail_threads;
		
		if(now.getHours()>=22 || now.getHours()<7 ){
			proxyCount=proxyCount*2;
		}
//		String proxyReqUrl = "http://d.jghttp.golangapi.com/getip?num=" + step4GetArticleDetail_threads
//				+ "&type=2&pro=&city=0&yys=0&port=1&pack=558&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=";
		if(proxyReqUrl!=null)
			proxyReqUrl = proxyReqUrl.replaceAll("\\{thread_count\\}", ""+proxyCount);
		else
			return 0;
		BufferedReader in = null;
		try {
			HttpClientDownloader httpClientDownload=new HttpClientDownloader();
			HttpRequest request = new HttpGetRequest();
			request.setUrl(proxyReqUrl);
			HttpResponse response = httpClientDownload.download(request, 30000);

			log.info("proxy ip ="+response.getContent());
			if(response.getStatus()==200){
				JSONObject result = JSON.parseObject(response.getContent());
				if(result.getBoolean("success")){
					JSONArray ipJsons=result.getJSONArray("data");
					for(int i=0;i<ipJsons.size();i++){
						JSONObject ipJson=ipJsons.getJSONObject(i);
						String tmpIp=ipJson.getString("ip");
						String tmpPort=ipJson.getString("port");
//						String[] ipAndPort=tmpIp.split(":"); 
						etProxys.addProxy(tmpIp, Integer.parseInt(tmpPort));
					}
					getProxyTotals=getProxyTotals+proxyCount;
				}
				log.info("获取proxy ip 总量："+getProxyTotals+",存活的ip数"+etProxys.getAvalibleProxy());
				return proxyCount;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
}
