package com.jd.spider.wenshu;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;

import com.geccocrawler.gecco.downloader.proxy.Proxy;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * 继承了gecco里面的Proxys，后来放弃使用了，使用自己写的 ProxyPool
 * etProxy 代理ip从classpath下的proxys文件里加载
 * 多代理支持，classpath根目录下放置proxys文件，文件格式如下
 * 127.0.0.1:8888
 * 127.0.0.1:8889
 * 支持记录代理成功率，自动发现无效代理
 * 支持在线添加代理
 * 
 * @author yangdongjun
 *
 */
@Deprecated
public class EtProxys implements Proxys {
	
	private static Log log = LogFactory.getLog(EtProxys.class);
	
	private ConcurrentLinkedQueue<Proxy> proxyQueue;
	
	private Map<String, Proxy> proxys = null;
	
	public EtProxys() {
		try {
			proxys = new ConcurrentHashMap<String, Proxy>();
			proxyQueue = new ConcurrentLinkedQueue<Proxy>();
			URL url = Resources.getResource("proxys");
			File file = new File(url.getPath());
			List<String> lines = Files.readLines(file, Charsets.UTF_8);
			if(lines.size() > 0) {
				for(String line : lines) {
					line = line.trim();
					if(line.startsWith("#")) {
						continue;
					}
					String[] hostPort = line.split(":");
					if(hostPort.length == 2) {
						String host = hostPort[0];
						int port = NumberUtils.toInt(hostPort[1], 80);
						addProxy(host, port);
					}
				}
			}
		} catch(Exception ex) {
			log.info("proxys not load");
		}
	}

	@Override
	public boolean addProxy(String host, int port) {
		
		return addProxy(host, port, null);
	}

	@Override
	public boolean addProxy(String host, int port, String src) {
		Proxy proxy = new Proxy(host, port);
		if(StringUtils.isNotEmpty(src)) {
			proxy.setSrc(src);
		}
		if(proxys.containsKey(proxy.toHostString())) {
			return false;
		} else {
			proxys.put(host+":"+port, proxy);
			proxyQueue.offer(proxy);
			if(log.isDebugEnabled()) {
				log.debug("add proxy : " + host + ":" + port);
			}
			return true;
		}
	}

	@Override
	public void failure(String host, int port) {
		Proxy proxy = proxys.get(host+":"+port);
		if(proxy != null) {
			long failure = proxy.getFailureCount().incrementAndGet();
			long success = proxy.getSuccessCount().get();
			reProxy(proxy, success, failure);
		}
	}

	@Override
	public void success(String host, int ip) {
		Proxy proxy = proxys.get(host+":"+ip);
		if(proxy != null) {
			long success = proxy.getSuccessCount().incrementAndGet();
			proxy.getFailureCount().set(0);//只要有成功的，就让失败数设置为0
			long failure = proxy.getFailureCount().get();
			reProxy(proxy, success, failure);
		}
	}
	
	/**
	 * 将代理重新放入队列。
	 * 成功和失败代理数，小于20，那么在末尾增加一个proxy，
	 * 如果大于20了，成功数>50%，也会增加proxy
	 * @param proxy
	 * @param success
	 * @param failure
	 */
	private void reProxy(Proxy proxy, long success, long failure) {
		long sum = failure + success;
//		if(sum < 20) {
//			proxyQueue.offer(proxy);
//		} else {
		System.out.println(proxy.getIP()+":"+proxy.getPort()+",sum="+sum+",failure="+failure);
		log.info(proxy.getIP()+":"+proxy.getPort()+",sum="+sum+",failure="+failure);
			if(sum<=WenshuMain.step4GetArticleDetail_proxy_max_article 
					&& failure<=4) {//一个ip失败次数大于3，那么不要了// && (success / (float)sum) >= 0.5f
				proxyQueue.offer(proxy);
				return;
			}
			log.info("释放proxy"+proxy.getIP()+":"+proxy.getPort()+","+"后还有:"+proxyQueue.size()+"个代理");
//		}
	}

	@Override
	public HttpHost getProxy() {
		if(proxys == null || proxys.size() == 0) {
			return null;
		}
		Proxy proxy = proxyQueue.poll();
		if(log.isDebugEnabled()) {
			log.debug("use proxy : " + proxy);
		}
		if(proxy == null) {
			return null;
		}
		return proxy.getHttpHost();
	}

	public int getAvalibleProxy(){
		return proxyQueue.size();
	}
}
