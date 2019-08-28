package com.jd.spider.wenshu;

import java.util.Map;

import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.Request;
import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
import com.geccocrawler.gecco.utils.UrlMatcher;

/**
 * 文章列表类
 * @author yangdongjun
 *
 */
//@Gecco(matchUrl="http://wenshu.court.gov.cn/List/List/?"
//		+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
//		+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}",
//	downloader="webClientArticleListDownloader",
//	pipelines={"consolePipeline","articleListPipeline"})//, "allSortPipeline",downloader="webClientArticleDownloader"
//sort7是一个可能有值，可能没值。 基层法院的条件会放在sort7里
@Gecco(matchUrl={"http://wenshu.court.gov.cn/List/List/?"
		+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
		+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}{sort7}",
//		"http://wenshu.court.gov.cn/List/List/?"
//		+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
//		+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}&{sort7}",
		"http://wenshu.court.gov.cn/List/List/?"
		+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
		+ "&conditions=searchWord\\+\\+\\+{sort5}%20TO%20{sort6}\\+{sort3}:{sort7}"
},
	downloader="webClientArticleListDownloader2",
	pipelines={"consolePipeline","articleListPipeline"})//, "allSortPipeline",downloader="webClientArticleDownloader"
public class PageArticleList implements HtmlBean {
	
	private static final long serialVersionUID = -5708897889609555904L;
	
	private Integer counts;
	@RequestParameter
	private String sort1;
	@RequestParameter
	private String sort2;
	@RequestParameter
	private String sort3;
	@RequestParameter
	private String sort4;
	@RequestParameter
	private String sort5;
	@RequestParameter
	private String sort6;
	private Integer pageIndex;//记录是第几页
	private Integer pageSize;
	@Request
	private HttpRequest request;
	public HttpRequest getRequest() {
		return request;
	}
	
	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		
//		System.out.println("---"+JSON.toJSONString(tree));
	}

	public Integer getCounts() {
		return counts;
	}

	public void setCounts(Integer counts) {
		this.counts = counts;
	}

	public String getSort1() {
		return sort1;
	}

	public void setSort1(String sort1) {
		this.sort1 = sort1;
	}

	public String getSort2() {
		return sort2;
	}

	public void setSort2(String sort2) {
		this.sort2 = sort2;
	}

	public String getSort3() {
		return sort3;
	}

	public void setSort3(String sort3) {
		this.sort3 = sort3;
	}

	public String getSort4() {
		return sort4;
	}

	public void setSort4(String sort4) {
		this.sort4 = sort4;
	}

	public String getSort5() {
		return sort5;
	}

	public void setSort5(String sort5) {
		this.sort5 = sort5;
	}

	public String getSort6() {
		return sort6;
	}

	public void setSort6(String sort6) {
		this.sort6 = sort6;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	public static void main(String[] args) {
		String urlPattern="http://wenshu.court.gov.cn/List/List/?"
				+ "sorttype=1&conditions=searchWord\\+\\+SLFY\\+\\+{sort1}:{sort2}"
				+ "&conditions=searchWord\\+\\+CPRQ\\+\\+{sort3}:{sort5}%20TO%20{sort6}[$|&{}]";
		String url="http://wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++%E6%B3%95%E9%99%A2%E5%90%8D%E7%A7%B0:%E5%8D%97%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%B3%95%E9%99%A2&conditions=searchWord++CPRQ++%E8%A3%81%E5%88%A4%E6%97%A5%E6%9C%9F:2017-01-01%20TO%202017-01-15&conditions=searchWord+%E5%8D%97%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%B3%95%E9%99%A2+++%E5%9F%BA%E5%B1%82%E6%B3%95%E9%99%A2:%E5%8D%97%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%B3%95%E9%99%A2";
//		String url="http://wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++%E6%B3%95%E9%99%A2%E5%90%8D%E7%A7%B0:%E5%8D%97%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%B3%95%E9%99%A2&conditions=searchWord++CPRQ++%E8%A3%81%E5%88%A4%E6%97%A5%E6%9C%9F:2017-01-01%20TO%202017-01-15";
		Map<String, String> params = UrlMatcher.match(url, urlPattern);
		System.out.println(params.toString());
	}
	
}
