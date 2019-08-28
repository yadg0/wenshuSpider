package org.zoodeploy;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;  
   
public class ZooKeeperTest {  
//	public static ZooKeeper zk = null;
    public static void main(String[] args) throws Exception{  
//        if (zk != null && zk.getState().isAlive()) {
//            zk.close();
//        }

        ZooKeeper zk = new ZooKeeper("192.168.162.16", 3000, new MyWatcher(),true);  
        System.out.println("=========创建节点===========");  
        Thread.sleep(20000);
        if(zk.exists("/test", false) == null)  
        {  
            zk.create("/test", "znode1".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);  
        }  
        System.out.println("=============查看节点是否安装成功===============");  
        System.out.println(new String(zk.getData("/test", false, null)));  
          
        System.out.println("=========修改节点的数据==========");  
        zk.setData("/test", "zNode2".getBytes(), -1);  
        System.out.println("========查看修改的节点是否成功=========");  
        System.out.println(new String(zk.getData("/test", false, null)));  
          
        System.out.println("=======删除节点==========");  
        zk.delete("/test", -1);  
        System.out.println("==========查看节点是否被删除============");  
        System.out.println("节点状态：" + zk.exists("/test", false));  
        zk.close();  
    }   
    
 
}  

 class MyWatcher implements Watcher {
    public void process(WatchedEvent event) {
//        if (getPrintWatches()) {
//            ZooKeeperMain.printMessage("WATCHER::");
//            ZooKeeperMain.printMessage(event.toString());
//        }
    }
}
