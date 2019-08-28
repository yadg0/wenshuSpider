package org.esTest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elasticsearch的基本测试
 * 
 * @ClassName: ElasticsearchTest1
 * @author sunt
 * @date 2017年11月22日
 * @version V1.0
 */
public class ElasticsearchTest1 {

	private Logger logger = LoggerFactory.getLogger(ElasticsearchTest1.class);

	public final static String HOST = "localhost";

	public final static int PORT = 9300;// http请求的端口是9200，客户端是9300

	private TransportClient client = null;

	/**
	 * 获取客户端连接信息
	 * 
	 * @Title: getConnect
	 * @author sunt
	 * @date 2017年11月23日
	 * @return void
	 * @throws UnknownHostException
	 */
	@SuppressWarnings({ "resource", "unchecked" })
	@Before
	public void getConnect() throws UnknownHostException {
		try {
			// 获取es主机中节点的ip地址及端口号
			client = new PreBuiltTransportClient(Settings.EMPTY)
					.addTransportAddress(new TransportAddress(InetAddress.getByName(HOST), PORT));
			System.out.println("client: " + client);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			if (client != null) {
//				client.close();
//			}
			// //1、指定es集群 cluster.name 是固定的key值，my-application是ES集群的名称
			// Settings settings = Settings.builder().put("cluster.name",
			// "my-application").build();
			// //2.创建访问ES服务器的客户端
			// TransportClient client = new PreBuiltTransportClient(settings)
			// .addTransportAddress(new
			// TransportAddress(InetAddress.getByName("192.168.1.94"), 9300));
			//
			// client.close();//关闭客户端
		}

		// MigrationClient mc=new MigrationClient();
		// client = new
		// PreBuiltTransportClient(Settings.EMPTY).addTransportAddresses(
		// new InetSocketTransportAddress(InetAddress.getByName(HOST),PORT));
		logger.info("连接信息:" + client.toString());
	}

	/**
	 * 关闭连接
	 * 
	 * @Title: closeConnect
	 * @author sunt
	 * @date 2017年11月23日
	 * @return void
	 */
	@After
	public void closeConnect() {
		if (null != client) {
			logger.info("执行关闭连接操作...");
			client.close();
		}
	}

	/**
	 * 创建索引库
	 * 
	 * @Title: addIndex1
	 * @author sunt
	 * @date 2017年11月23日
	 * @return void 需求:创建一个索引库为：msg消息队列,类型为：tweet,id为1 索引库的名称必须为小写
	 * @throws IOException
	 */
	@Test
	public void addIndex1() throws IOException {
		IndexResponse response = client.prepareIndex("msg", "tweet", "1").setSource(XContentFactory.jsonBuilder()
				.startObject().field("userName", "张三").field("sendDate", new Date()).field("msg", "你好李四").endObject())
				.get();

		logger.info("索引名称:" + response.getIndex() + "\n类型:" + response.getType() + "\n文档ID:" + response.getId()
				+ "\n当前实例状态:" + response.status());
	}

	public static void main(String[] args) {
		ElasticsearchTest1 te = new ElasticsearchTest1();
		try {
			te.getConnect();
			te.addIndex1();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			te.closeConnect();
		}
	}
}