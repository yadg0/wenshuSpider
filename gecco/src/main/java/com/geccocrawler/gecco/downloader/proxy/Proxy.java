package com.geccocrawler.gecco.downloader.proxy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;

public class Proxy {
	private static Log log = LogFactory.getLog(Proxy.class);
	private HttpHost httpHost;
	
	private AtomicLong successCount;
	
	private AtomicLong failureCount;
	
	private String src;//来源
	private AtomicInteger status;//状态  0正常，1需要等会再用，2无效
	public static int STATUS_AVAILABLE=0;
	public static int STATUS_SLEEP=1;
	public static int STATUS_INAVAILABLE=2;
	
	public Proxy(String host, int port) {
		this.httpHost = new HttpHost(host, port);//TODO:这里是否要加 http
		this.src = "custom";
		this.successCount = new AtomicLong(0);
		this.failureCount = new AtomicLong(0);
		this.status=new AtomicInteger(0);
	}

	public HttpHost getHttpHost() {
		return httpHost;
	}

	public void setHttpHost(HttpHost httpHost) {
		this.httpHost = httpHost;
	}

	public AtomicLong getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(AtomicLong successCount) {
		this.successCount = successCount;
	}

	public AtomicLong getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(AtomicLong failureCount) {
		this.failureCount = failureCount;
	}
	
	public String getIP() {
		return this.getHttpHost().getHostName();
	}
	
	public int getPort() {
		return this.getHttpHost().getPort();
	}

	public String toHostString() {
		return httpHost.toHostString();
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public AtomicInteger getStatus() {
		return status;
	}

	public synchronized void setStatus(int newStatus) {
		if(newStatus==STATUS_SLEEP ){
			log.info("设置代理休眠："+this.toString());
			if(this.status.get()==STATUS_AVAILABLE){
				this.status.set(newStatus);
				AtomicInteger tmpStatus =this.status;
				//启动睡眠
				new Thread(){
					public void run(){
						try {
							Thread.sleep(5000l);//
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(tmpStatus.get()!=STATUS_INAVAILABLE)
							tmpStatus.set(STATUS_AVAILABLE);
					}
				}.start();
			}
		}else{
			this.status.set(newStatus);
		}
	}

	@Override
	public String toString() {
		return "Proxy [httpHost=" + httpHost + ", successCount=" + successCount
				+ ", failureCount=" + failureCount + ",status="+status+"]";
	}
	
}
