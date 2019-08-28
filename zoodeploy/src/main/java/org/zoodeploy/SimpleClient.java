package org.zoodeploy;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

/**
 * The client that gets spawned for the SimpleSysTest 
 *
 */
public class SimpleClient implements  Watcher, AsyncCallback.DataCallback, StringCallback, StatCallback {
    private static final long serialVersionUID = 1L;
    String hostPort;
    ZooKeeper zk;
    transient int index;
    transient String myPath;
    byte data[];
    boolean createdEphemeral;
    public void configure(String params) {
        String parts[] = params.split(" ");
        hostPort = parts[1];
        this.index = Integer.parseInt(parts[0]);
        myPath = "/simpleCase/" + index;
    }
    
    public void start() {
        try {
            zk = new ZooKeeper(hostPort, 15000, this);
            zk.getData("/simpleCase", true, this, null);
//            if (null != r) {
//                r.report("Client " + index + " connecting to " + hostPort);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stop() {
        try {
            if (zk != null) {
                zk.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void process(WatchedEvent event) {
        if (event.getPath() != null && event.getPath().equals("/simpleCase")) {
            zk.getData("/simpleCase", true, this, null);
        }
    }
    
    public void processResult(int rc, String path, Object ctx, byte[] data,
            Stat stat) {
        if (rc != 0) {
            zk.getData("/simpleCase", true, this, null);
        } else {
            this.data = data;
            String content = new String(data);
            if (content.equals("die")) {
                this.stop();
                return;
            }
            if (!createdEphemeral) {
                zk.create(myPath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, this, null);
                createdEphemeral = true;
            } else {
                zk.setData(myPath, data, -1, this, null);
            }
        }            
    }
    
    public void processResult(int rc, String path, Object ctx, String name) {
        if (rc != 0) {
            zk.create(myPath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, this, null);
        }
    }
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc != 0) {
            zk.setData(myPath, data, -1, this, null);
        }
    }
    @Override
    public String toString() {
        return SimpleClient.class.getName() + "[" + index + "] using " + hostPort;
    }
    
    public static void main(String[] args) {
//		SimpleClient client=new SimpleClient();
//		client.configure("1 192.168.162.16:2181");
//		client.start();
    	SimpleClient sc=new SimpleClient();
    	ZooKeeper zk;
		try {
			zk = new ZooKeeper( "192.168.162.16:2181", 15000, null);
	        int timeout=1000;
			boolean connected = (zk.getState() == States.CONNECTED);
	        long end = System.currentTimeMillis() + timeout;
	        while(!connected && end > System.currentTimeMillis()) {
	        	zk.wait(timeout);
	            connected = (zk.getState() == States.CONNECTED);
	        }
	        zk.getData("/simpleCase", true, null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
    
    boolean connected;
    synchronized private boolean waitForConnect(ZooKeeper zk, long timeout) throws InterruptedException {
        connected = (zk.getState() == States.CONNECTED);
        long end = System.currentTimeMillis() + timeout;
        while(!connected && end > System.currentTimeMillis()) {
            wait(timeout);
            connected = (zk.getState() == States.CONNECTED);
        }
        return connected;
    }
//    Reporter r;
//    public void setReporter(Reporter r) {
//        this.r = r;
//    }
}
