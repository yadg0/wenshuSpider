package com.jd.spider.wenshu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 配置信息监听类，会连接zookeeper，监听配置变更，如果配置中有自己的ip，
 * 那么会调用PageSortList.initZooProperties方法
 * @author yangdongjun
 *
 */
public class ConfigWatcher implements Watcher, Runnable {
	private static String hostPort = "39.104.183.144:2181";
	private static String zooDataPath = "/taskConfig";
	byte zoo_data[] = null;
	ZooKeeper zk;
	public static boolean zooKeeperLoaded=false;
	
	public ConfigWatcher(String hostPort,String zooDataPath,String configs) {
		try {
			ConfigWatcher.hostPort=hostPort;
			ConfigWatcher.zooDataPath=zooDataPath;
			zk = new ZooKeeper(hostPort, 2000, this);
			if (zk != null) {
				try {
					// Create the znode if it doesn't exist, with the following
					// code:
					if (zk.exists(zooDataPath, this) == null) {
						zk.create(zooDataPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
					if(configs!=null){//如果存在节点，且配置不为null，不存在就设置
						zk.setData(zooDataPath, configs.getBytes(), -1);
					}else{
						printData();
					}
//					printData();//加载配置信息
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printData() throws InterruptedException, KeeperException {
		Stat stat=new Stat();
		zoo_data = zk.getData(zooDataPath, true, stat);
		String zString = new String(zoo_data);
		System.out.printf("获取配置文件： %s: %s\n", zooDataPath, zString);
		if(StringUtils.isEmpty(zString)) return;
		zooKeeperLoaded=true;
		JSONArray configArray=JSON.parseArray(zString);
		for(int i=0;i<configArray.size();i++){
			JSONObject tmpConfig=configArray.getJSONObject(i);
			
			List<String> ips=tmpConfig.getJSONArray("ips").toJavaList(String.class);
			System.out.println("看看有没有我的配置："+ips.contains(WenshuMain.nodeIp)+","+ips.toString()+","+WenshuMain.nodeIp);
			if(ips.contains(WenshuMain.nodeIp)){
				String config=tmpConfig.getString("config");
				try {
					config=config.replaceAll(";", "\\\n");
					WenshuMain.initZooProperties(null, new ByteArrayInputStream(config.getBytes("UTF-8")));
					break;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
		System.out.printf("\nEvent Received: %s", event.toString());
		// We will process only events of type NodeDataChanged
		if (event.getType() == Event.EventType.NodeDataChanged) {
			try {
				printData();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (KeeperException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, KeeperException {
		ConfigWatcher dataWatcher = new ConfigWatcher(hostPort,zooDataPath,null);
		dataWatcher.printData();
		dataWatcher.run();
	}

	public void run() {
		try {
			synchronized (this) {
				while (true) {
					wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
}
