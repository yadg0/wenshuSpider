package org.zoodeploy;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * Hello world!
 *
 */
public class MainDeployee {
	private static final String connectString = "192.168.162.16:2181";
	private static final int sessionTimeout = 2000;
	private static final String parentNode = "/servers";
	ZooKeeper zkClient = null;

	public void connection() throws Exception {
		zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println(event.getType() + "," + event.getPath());
				try {
					zkClient.getChildren("/", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("zkState=="+zkClient.getState());
	}

	public void registerServer(String hostname) throws Exception {
		zkClient.create(parentNode + "/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//		zkClient.create("//");
		System.out.println(parentNode + " is created");
	}

	public void handleBussiness(String hostname) throws Exception {
		System.out.println(hostname + " is working");
		Thread.sleep(Long.MAX_VALUE);
	}

	public static void main(String[] args) {
		String server="localhost";
		try {
			MainDeployee zk = new MainDeployee();
			zk.connection();
			zk.registerServer(server);
			zk.handleBussiness(server);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
