package com.jd.spider.food;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.JsonPipeline;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;

@PipelineName("allChapterJsonPipeline")
public class AllChapterJsonPipeline extends JsonPipeline {
	
	public static List<HttpRequest> sortRequests = new ArrayList<HttpRequest>();

	@Override
	public void process(JSONObject allSort) {
		HttpRequest currRequest = HttpGetRequest.fromJson(allSort.getJSONObject("request"));
		JSONArray chapters = allSort.getJSONArray("chapter");
		process(currRequest, chapters);
		/*List<Category> domestics = allSort.getDomestic();
		process(allSort, domestics);
		List<Category> bodys = allSort.getBaby();
		process(allSort, bodys);*/
	}
	
	private void process(HttpRequest currRequest, JSONArray chapters) {
		if(chapters == null) {
			return;
		}
		for(int i = 0; i < chapters.size(); i++) {
			JSONObject category = chapters.getJSONObject(i);
			JSONArray hrefs = category.getJSONArray("chapterList");
			for(int j = 0; j < hrefs.size(); j++) {
				String url = hrefs.getJSONObject(j).getString("url");
				SchedulerContext.into(currRequest.subRequest(url));
			}
		}
	}

}