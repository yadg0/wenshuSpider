package com.geccocrawler.gecco.spider;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.downloader.AfterDownload;
import com.geccocrawler.gecco.downloader.BeforeDownload;
import com.geccocrawler.gecco.downloader.DownloadException;
import com.geccocrawler.gecco.downloader.Downloader;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.scheduler.Scheduler;
import com.geccocrawler.gecco.scheduler.UniqueSpiderScheduler;
import com.geccocrawler.gecco.spider.render.Render;

/**
 * 一个爬虫引擎可以包含多个爬虫，每个爬虫可以认为是一个单独线程，爬虫会从Scheduler中获取需要待抓取的请求。
 * 爬虫的任务就是下载网页并渲染相应的JavaBean。
 * 
 * @author huchengyi
 *
 */
public class Spider implements Runnable {
	
	private static Log log = LogFactory.getLog(Spider.class);
	
	private CountDownLatch pauseCountDown;
	
	private volatile boolean stop;
	
	private volatile boolean pause;
	
	private GeccoEngine engine;
	
	private Scheduler spiderScheduler;
	
	private String threadName;
	/**
	 * 当前待渲染的bean
	 */
	public Class<? extends SpiderBean> currSpiderBeanClass;
	
	public Spider(GeccoEngine engine) {
		this.engine = engine;
		this.spiderScheduler = new UniqueSpiderScheduler();
		this.pause = false;
		this.stop = false;
	}
	public Spider(GeccoEngine engine,String threadName) {
		this.engine = engine;
		this.spiderScheduler = new UniqueSpiderScheduler();
		this.pause = false;
		this.stop = false;
		this.threadName=threadName;
	}
	public void run() {
		//将spider放入线程本地变量，之后需要使用
		SpiderThreadLocal.set(this);
		int succCounts=0;
		while(true) {
			//停止
			if(stop) {
				//告知engine线程执行结束
				engine.notifyComplete();
				break;
			}
			//暂停抓取
			if(pause) {
				try {
					this.pauseCountDown.await();
				} catch (InterruptedException e) {
					log.error("can't pause : ", e);
				}
			}
			//获取待抓取的url
			boolean start = false;
			HttpRequest request = spiderScheduler.out();
			if(request == null) {
				//startScheduler
				request = engine.getScheduler().out();
//				System.out.println("开始获取下一个url文章2:"+(request==null?"null":request.getUrl()));
				if(request == null) {
					//告知engine线程执行结束
					engine.notifyComplete();
					break;
				}
				start = true;
			}
			if(log.isDebugEnabled()) {
				log.debug("match url : " + request.getUrl());
			}
			//download
			HttpResponse response = null;
			try {
				//匹配SpiderBean
				currSpiderBeanClass = engine.getSpiderBeanFactory().matchSpider(request);
				if(currSpiderBeanClass == null) {//如果无法匹配但是是302跳转，需要放入抓取队列继续抓取
					response = defaultDownload(request);
					if(response.getStatus() == 302 || response.getStatus() == 301){
						spiderScheduler.into(request.subRequest(response.getContent()));
					} else {
						log.error("cant't match url : " + request.getUrl());
					}
				} else {
					Date begin=new Date();
					//获取SpiderBean的上下文：downloader,beforeDownloader,afterDownloader,render,pipelines
					SpiderBeanContext context = getSpiderBeanContext();
//					try{
						response = download(context, request);
//					}catch(Exception e){
//						//下载时抛异常
//						Render render = context.getRender();
//						SpiderBean spiderBean = null;
//						spiderBean = render.inject(currSpiderBeanClass, request, response);
//						spiderBean.errorProcess();
//						throw e;
//					}
					if(response.getStatus() == 200) {
						//render
						Render render = context.getRender();
						SpiderBean spiderBean = null;
						spiderBean = render.inject(currSpiderBeanClass, request, response);
						if(spiderBean!=null)
							spiderBean.customerProcess(spiderBean,context,request,response);
						//pipelines
						pipelines(spiderBean, context);
						//302,301 重定向
					} else if(response.getStatus() == 302 || response.getStatus() == 301){
						spiderScheduler.into(request.subRequest(response.getContent()));
					}
					Date end=new Date();
					succCounts++;
					log.info("下载一个内容结束。。。"+threadName+",耗时："+(end.getTime()-begin.getTime())+",成功数："+succCounts);
				}
			} catch(Exception ex) {
				if(ex.getMessage()!=null && ex.getMessage().startsWith("502")){
					request.setRetryTimes(request.getRetryTimes()+1);
					if(request.getRetryTimes()<=3){
						spiderScheduler.into(request);
					}
					System.out.println("捕获502或者重定向异常，..."+ex.getMessage()+"，将会重新获取"+request.getUrl());
				}
//				ex.printStackTrace();
				if(engine.isDebug()) {
					log.error(request.getUrl() + " ERROR : ", ex);
				}
				log.error(request.getUrl() + " ERROR : " + ex.getClass().getName() + ex.getMessage());
			} finally {
				if(response != null) {
					response.close();
				}
			}
			//抓取间隔
			interval();
			//开始地址放入队尾重新抓取
			if(start && engine.isLoop()) {
				//如果是一个开始抓取请求，再返回开始队列中
				engine.getScheduler().into(request);
			}
		}
	}
	
	/**
	 * 暂停，当前正在抓取的请求会继续抓取完成，之后会等到restart的调用才继续抓取
	 */
	public void pause() {
		this.pauseCountDown = new CountDownLatch(1);
		this.pause = true;
	}
	
	/**
	 * 重新开始
	 */
	public void restart() {
		this.pauseCountDown.countDown();
		this.pause = false;
	}
	
	/**
	 * 停止抓取
	 */
	public void stop() {
		this.stop = true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void pipelines(SpiderBean spiderBean, SpiderBeanContext context) {
		if(spiderBean == null) {
			return ;
		}
		List<Pipeline> pipelines = context.getPipelines();
		if(pipelines != null) {
			for(Pipeline pipeline : pipelines) {
				pipeline.process(spiderBean);
			}
		}
	}
	
	private void interval() {
		int interval = engine.getInterval();
		if(interval > 0) {
			try {
				Thread.sleep(randomInterval(interval));
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * 默认下载
	 * 
	 * @param request
	 * @return
	 */
	private HttpResponse defaultDownload(HttpRequest request) throws DownloadException {
		HttpResponse response = download(null, request);
		return response;
	}
	
	private HttpResponse download(SpiderBeanContext context, HttpRequest request) throws DownloadException {
			Downloader currDownloader = null;
			BeforeDownload before = null;
			AfterDownload after = null;
			int timeout = 1000;
			if(context != null) {
				currDownloader = context.getDownloader();
				before = context.getBeforeDownload();
				after = context.getAfterDownload();
				timeout = context.getTimeout();
			} else {
				currDownloader = engine.getSpiderBeanFactory().getDownloaderFactory().defaultDownloader();
			}
			if(before != null) {
				before.process(request);
			}
			HttpResponse response = currDownloader.download(request, timeout);
			if(after != null) {
				after.process(request, response);
			}
			return response;
	}

	/**
	 * 间隔时间在左右1s的范围内随机
	 * 
	 * @param interval
	 * @return
	 */
	private int randomInterval(int interval) {
		int min = interval - 1000;
		if(min < 1) {
			min = 1;
		}
		int max = interval + 1000;
		return (int)Math.rint(Math.random()*(max-min)+min);
	}
	
	public GeccoEngine getEngine() {
		return engine;
	}

	public Scheduler getSpiderScheduler() {
		return spiderScheduler;
	}

	public void setSpiderScheduler(Scheduler spiderScheduler) {
		this.spiderScheduler = spiderScheduler;
	}

	public SpiderBeanContext getSpiderBeanContext() {
		return engine.getSpiderBeanFactory().getContext(currSpiderBeanClass);
	}
	
	public static void main(String[] args) {
		
		String s="http://gov.sss?sorttype=1&conditions=searchWord+1+AJLX++案件类型:刑事案件";
		String pres=s.substring(0,s.indexOf("searchWord+1+AJLX++"));
		String params=s.substring(s.indexOf("searchWord+1+AJLX++")+"searchWord+1+AJLX++".length());
		String[] paramList=params.split(":");
		params="";
		for(String param:paramList){
			params=params+URLEncoder.encode(param)+":";
		}
		if(paramList.length>0){
			params=params.substring(0,params.length()-1);
		}
		s=pres+"searchWord+1+AJLX++"+params;
		
		System.out.println(s);
	}
}
