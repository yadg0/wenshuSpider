package com.jd.spider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

public class TestProxy {
	
	
	public static void main(String args[]) {
		StringBuffer sb = new StringBuffer();
		testProxy();
		if(true) return;
		// 创建HttpClient实例
		HttpClient client = getHttpClient();
		// 创建httpGet
		HttpGet httpGet = new HttpGet("http://www.sina.com");
		// 执行
		try {
			HttpResponse response = client.execute(httpGet);
			HttpEntity entry = response.getEntity();
			if (entry != null) {
				InputStreamReader is = new InputStreamReader(entry.getContent());
				BufferedReader br = new BufferedReader(is);
				String str = null;
				while ((str = br.readLine()) != null) {
					sb.append(str.trim());
				}
				br.close();
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sb.toString());
	}
	public static void testProxy(){
		System.out.println(10/(float)10>=0.6f);
		if(true)return;
        //设置代理IP、端口、协议（请分别替换）
        HttpHost proxy = new HttpHost("60.185.39.178", 4576, "http");
        //把代理设置到请求配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setProxy(proxy)
                .build();
        //实例化CloseableHttpClient对象
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
        //访问目标地址
        HttpGet httpGet = new HttpGet("http://wenshu.court.gov.cn");
        //请求返回
        CloseableHttpResponse httpResp = null;
        StringBuffer sb = new StringBuffer();
        try {
        	httpResp = httpclient.execute(httpGet);
            int statusCode = httpResp.getStatusLine().getStatusCode();
            HttpEntity responseEntity = httpResp.getEntity();
            InputStreamReader is = new InputStreamReader(responseEntity.getContent());
            BufferedReader br = new BufferedReader(is);
            String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str.trim());
			}
			br.close();
			
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("成功");
                System.out.println(sb.toString());
            }else{
            	
            	System.out.println("失败"+statusCode);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	if(httpResp!=null)
				try {
					httpResp.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
	}
	// 设置代理
	public static HttpClient getHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String proxyHost = "113.241.60.187";
		int proxyPort = 16764;
		String userName = "yadg0";
		String password = "yang11";
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxyHost, proxyPort),
				new UsernamePasswordCredentials(userName, password));
		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		return httpClient;
	}
	
	public static HttpClient getHttpClient2(){
		CloseableHttpClient httpClient;
		RequestConfig clientConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
		try {
			//构造一个信任所有ssl证书的httpclient
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
			           .register("http", PlainConnectionSocketFactory.getSocketFactory())  
			           .register("https", sslsf)  
			           .build();
		} catch(Exception ex) {
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
		}
		PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		syncConnectionManager.setMaxTotal(1000);
		syncConnectionManager.setDefaultMaxPerRoute(50);

		HttpClientContext cookieContext=HttpClientContext.create();
		httpClient = HttpClientBuilder.create()
		.setDefaultRequestConfig(clientConfig)
		.setConnectionManager(syncConnectionManager)
		.build();
//		HttpGet reqObj = new HttpGet("www.sina.com");
		int  timeout=3000;
		RequestConfig.Builder builder = RequestConfig.custom()
				.setConnectionRequestTimeout(1000)//从连接池获取连接的超时时间
				.setSocketTimeout(timeout)//获取内容的超时时间
				.setConnectTimeout(timeout)//建立socket连接的超时时间
				.setRedirectsEnabled(false);
		HttpHost proxy = new HttpHost("115.210.248.48",18489,"http");
		builder.setProxy(proxy);
		builder.setConnectTimeout(timeout);
		//设置proxy验证
		BasicScheme proxyAuth = new BasicScheme();  
		try {
			proxyAuth.processChallenge(new BasicHeader(AUTH.PROXY_AUTH, "BASIC realm=default"));
		} catch (MalformedChallengeException e) {
			e.printStackTrace();
		}
		BasicAuthCache authCache = new BasicAuthCache();
		authCache.put(proxy, proxyAuth);
		///////
		CredentialsProvider credsProvider = new BasicCredentialsProvider();  
		credsProvider.setCredentials(
		        new AuthScope(proxy),
		        new UsernamePasswordCredentials("yadg0", "yang11"));  
		cookieContext.setAuthCache(authCache);  
		cookieContext.setCredentialsProvider(credsProvider);
		
//		httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
//		reqObj.setConfig(builder.build());

		return httpClient;
	}
}
