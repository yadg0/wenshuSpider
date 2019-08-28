package com.jd.spider.food;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.annotation.Request;
import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.annotation.Text;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
/**
 * 抓取新浪新闻频道关于食物的爬虫
 * @author yangdongjun
 *
 */
@Gecco(matchUrl="http://news.sina.com.cn/o/{date}/{keyword}.shtml", pipelines="consolePipeline", timeout=1000)
public class FoodSpider implements HtmlBean {

	private static final long serialVersionUID = -7127412585200687225L;
	
	@Request
	private HttpRequest request;
	@RequestParameter
	private String date;
	@RequestParameter
	private String keyword;
	
	@Text(own=false)
	@HtmlField(cssPath="body div#articleContent")
	private String title;
	
//	@Text(own=false)
//	@HtmlField(cssPath=".sinacMNT_logout div#wrapOuter.wrap-outer.clearfix div.wrap-inner div#articleContent.page-content.clearfix div.left div#artibody.article.article_16 p.article-editor")
//	private int author;
//	
//	@HtmlField(cssPath="#articleContent")
//	private String content;

	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	


	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub
		
	}

	//
//	public int getAuthor() {
//		return author;
//	}
//
//	public void setAuthor(int author) {
//		this.author = author;
//	}
//
//	public String getContent() {
//		return content;
//	}
//
//	public void setContent(String content) {
//		this.content = content;
//	}
//
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public static void main(String[] args) {
		GeccoEngine.create()
		.classpath("com.jd.spider.food")
		//开始抓取的页面地址
		.start("http://news.sina.com.cn/o/2017-11-28/doc-ifypacti8843674.shtml")
		//开启几个爬虫线程,线程数量最好不要大于start request数量
		.thread(1)
		//单个爬虫每次抓取完一个请求后的间隔时间
		.interval(2000)
		//循环抓取
		.loop(false)
		//采用pc端userAgent
		.mobile(false)
		//是否开启debug模式，跟踪页面元素抽取
		.debug(false)
		//非阻塞方式运行
		.start();
	}

}
