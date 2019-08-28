package com.jd.spider.food;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.dynamic.DynamicGecco;

@Gecco(matchUrl="http://www.biqukan.com/1_1094/", pipelines="consolePipeline", timeout=1000)
public class YiNianYongHeng {
	public static void main(String[] args) {
		Class<?> chapters = DynamicGecco.html()
				.listField("chapterList", 
						DynamicGecco.html()
						.stringField("chapterTitle").csspath("dd a").text().build()
//						.stringField("url").csspath("body div.listmain dl dd a").text().build()
						.register()).build()
				.register();
//		
		DynamicGecco.html()
		.gecco("http://www.biqukan.com/1_1094/", "consolePipeline")
		.requestField("request").request().build()
		.listField("chapterList",chapters).csspath("body div.listmain dl dd:nth-child(12)").text().build()
		.register();
//		.listField("chapter", chapters)
//				.csspath(".category-items > div:nth-child(1) > div:nth-child(2) > div.mc > div.items > dl").build()
		
		
		GeccoEngine.create()
		.classpath("com.jd.spider.food")
		//开始抓取的页面地址
		.start("http://www.biqukan.com/1_1094")
		//开启几个爬虫线程,线程数量最好不要大于start request数量
		.thread(2)
		//单个爬虫每次抓取完一个请求后的间隔时间
		.interval(2000)
		//循环抓取
		.loop(false)
		//采用pc端userAgent
		.mobile(false)
		//是否开启debug模式，跟踪页面元素抽取
		.debug(true)
		//非阻塞方式运行
		.start();
	}
}
