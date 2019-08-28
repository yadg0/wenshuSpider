package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.List;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.jd.spider.wenshu.domain.ArticlePageTask;
import com.jd.spider.wenshu.service.State;
import com.jd.spider.wenshu.service.WenshuService;
/**
 * 文章列表处理类
 * @author yangdongjun
 *
 */
@PipelineName("articleListPipeline")
public class ArticleListPipeline implements Pipeline<PageArticleList> {
	
	public static List<HttpRequest> sortRequests = new ArrayList<HttpRequest>();
	public static List<Article> articles=new ArrayList<Article>();

	@Override
	public void process(PageArticleList detail) {
		//设置 task任务结束
		//detail.getSort1();
//		ArticlePageTask task=new ArticlePageTask();
//		task.setCourtName(detail.getSort2());
//		task.setDate(detail.getSort5());
//		task.setState(State.DONLOADED);
//		WenshuService.updateArticlePageTask(task);
//		for(Article article : articles){
//			HttpRequest request = detail.getRequest().subRequest("http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID="+article.getId());
//			request.setCharset("UTF-8");
//			request.getHeaders().remove("Referer");
//			sortRequests.add(request);
//		}
	}
}