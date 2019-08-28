package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HrefBean;
/**
 * 处理分类数据，建立sortRequests。下个处理会读取
 * @author yangdongjun
 *
 */
@Deprecated
@PipelineName("sortListPipeline")
public class SortListPipeline implements Pipeline<WenshuMain> {
	
	public static List<HttpRequest> sortRequests = new ArrayList<HttpRequest>();

	@Override
	public void process(WenshuMain sortList) {
//		process(sortList,sortList.getXingshi());
//		process(sortList,sortList.getMingshi());
//		process(sortList,sortList.getXingzheng());
//		process(sortList,sortList.getPeichang());
//		process(sortList,sortList.getZhixing());
	}
	
	private void process(WenshuMain sortList, Sort sort) {
		String url = sort.getUrl();
		if(StringUtils.isNotEmpty(url)){
			url=url.substring(0,url.indexOf("&"));
		}
//		HttpRequest currRequest = sortList.getRequest();
//		sortRequests.add(currRequest.subRequest(url));
	}

}