package com.geccocrawler.gecco.downloader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;

/**
 * 默认使用 firefox 驱动下载。webdriver.gecko.driver一般用于自动化测试，
 * 对于某些网页，特别是动态项很多的情况，需要模拟网页来获取数据。
 * @author yangdongjun
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("defaultWebClientDownloader")
public class DefaultWebClientDownloader extends AbstractDownloader {
	private static Log logger = LogFactory.getLog(DefaultWebClientDownloader.class);
	RemoteWebDriver driver = null;
	public static void initDriver(String driverPath){
		System.out.println("默认web初始化。。。"+driverPath);
		System.setProperty("webdriver.gecko.driver",driverPath);
	}
	@Override
	public HttpResponse download(HttpRequest request, int timeout) throws DownloadException {
//		System.out.println();
		try{
			System.out.println("开始下载。。。");
			driver =new FirefoxDriver();
			//driver.manage().timeouts().
			WebDriver.Navigation navigate = driver.navigate();
	        navigate.to(request.getUrl());
	        
	        driver.manage().window().maximize();
	       
	        Thread.sleep(1000);
	//        System.out.println(driver.getPageSource());
//	        Document doc = Jsoup.parse(driver.getPageSource());
	        otherProcess(driver);
	        
			HttpResponse resp = new HttpResponse();
			resp.setCharset("UTF-8");
			resp.setStatus(200);
			resp.setContentType("text/html");
			//System.out.println("获取资源结果==="+driver.getPageSource());
			resp.setContent(driver.getPageSource());
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			HttpResponse resp = new HttpResponse();
			resp.setStatus(404);
		}finally{
			if(driver!=null){
				try{
				driver.close();
				driver.quit();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void otherProcess(RemoteWebDriver driver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public RemoteWebDriver getDriver() {
		return driver;
	}

	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}
	
}
