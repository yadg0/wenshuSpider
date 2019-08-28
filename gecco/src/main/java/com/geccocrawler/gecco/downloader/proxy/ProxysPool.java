package com.geccocrawler.gecco.downloader.proxy;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * 代理池管理
 * 代理ip从classpath下的proxys文件里加载，也可以使用addProxy方法动态添加
 * 一个代理失败则需要休眠5秒，如果失败次数超过3次，就抛弃该代理。
 * 记录每个代理成功数。
 * 代理可以多个线程使用
 * @author yangdongjun
 *
 */
public class ProxysPool implements Proxys {
	
	private static Log log = LogFactory.getLog(ProxysPool.class);
	
	private Vector<Proxy> proxyPool;
	
	private Map<String, Proxy> proxysMap = null;
	
	public ProxysPool() {
		try {
			proxysMap = new ConcurrentHashMap<String, Proxy>();
			proxyPool = new Vector<Proxy>();
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
		if(proxysMap.containsKey(proxy.toHostString())) {
			return false;
		} else {
			proxysMap.put(host+":"+port, proxy);
			proxyPool.add(proxy);
			if(log.isDebugEnabled()) {
				log.debug("add proxy : " + host + ":" + port);
			}
			return true;
		}
	}

	@Override
	public void failure(String host, int port) {
		Proxy proxy = proxysMap.get(host+":"+port);
//		if(log.isDebugEnabled()) {
			log.info("下载失败！"+proxy);
//		}
		if(proxy != null) {
			long failure = proxy.getFailureCount().incrementAndGet();
			if(failure>=3) {
				proxy.setStatus(Proxy.STATUS_INAVAILABLE);				
			}else{
//				proxy.setStatus(Proxy.STATUS_SLEEP);
			}
		}
	}

	@Override
	public void success(String host, int ip) {
		Proxy proxy = proxysMap.get(host+":"+ip);
//		if(log.isDebugEnabled()) {
			log.info("下载成功！"+proxy);
//		}
		if(proxy != null) {
			 proxy.getSuccessCount().incrementAndGet();
			//只要成功，清除掉失败的次数
			proxy.getFailureCount().set(0);
			proxy.getFailureCount().get();
		}
	}

	private int poolIndex=0;
	@Override
	public HttpHost getProxy() {
		if(proxyPool == null || proxyPool.size() == 0) {
			return null;
		}
		int maxRetryTimes=proxyPool.size();
//		if(log.isDebugEnabled()) {
			log.info("getProxy:poolSize="+proxyPool.size()+",proxyMap.size="+proxysMap.size());
//		}
		Proxy proxy = null;
		while(maxRetryTimes>0){
			if(poolIndex>=proxyPool.size()) poolIndex=0;
			Proxy tmpProxy = proxyPool.get(poolIndex);
			if(tmpProxy.getStatus().get()==Proxy.STATUS_AVAILABLE){
				proxy=tmpProxy;
			}else if(tmpProxy.getStatus().get()==Proxy.STATUS_INAVAILABLE){
//				if(log.isDebugEnabled()) {
					log.info("释放代理："+tmpProxy);
//				}
				proxyPool.remove(tmpProxy);
				proxysMap.remove(tmpProxy.getHttpHost().getHostName()+":"+tmpProxy.getHttpHost().getPort());
			}
			poolIndex++;
			if(proxy!=null)break;
			maxRetryTimes--;
		}
//		log.info("use proxy : " + proxy);
//		if(log.isDebugEnabled()) {
			log.info("use proxy : " + proxy);
//		}
		if(proxy == null) {
			return null;
		}
		return proxy.getHttpHost();
	}

	/**
	 * 获取可用的代理，包含了休眠状态的代理。
	 * @return
	 */
	public int getAvalibleProxy(){
		return proxyPool.size();
	}
}
