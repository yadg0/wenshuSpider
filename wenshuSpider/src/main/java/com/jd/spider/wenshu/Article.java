package com.jd.spider.wenshu;

import java.util.ArrayList;
import java.util.List;

import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
//文章实体类
@Gecco(matchUrl="http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID={DocId}",
	timeout=20000)
public class Article implements HtmlBean{
	@RequestParameter("DocId")
	private String id;
	private String orgionContent;
	
	private String title;
	private String caseInfo;
//	private String LegalBase;
	
	private String dirData;//包含了RelateInfo，LegalBase
	private String url;
	private String taskStartDate;
	
	public static List<Article> finishedArticle=new ArrayList<Article>();
	
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		Article article  = (Article)currSpiderBeanClass;
		String content=response.getContent();
		article.setOrgionContent(content);
		article.setTaskStartDate(request.getParameter("tb"));
//		System.out.println("===="+article.getTaskStartDate());
		finishedArticle.add(article);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOrgionContent() {
		return orgionContent;
	}

	public void setOrgionContent(String orgionContent) {
		this.orgionContent = orgionContent;
	}


	public String getCaseInfo() {
		return caseInfo;
	}

	public void setCaseInfo(String caseInfo) {
		this.caseInfo = caseInfo;
	}

	public String getDirData() {
		return dirData;
	}

	public void setDirData(String dirData) {
		this.dirData = dirData;
	}
	
	public String getTaskStartDate() {
		return taskStartDate;
	}

	public void setTaskStartDate(String taskStartDate) {
		this.taskStartDate = taskStartDate;
	}

	public static void main(String[] args) {
		String s="$(function(){$(\"#con_llcs\").html(\"浏览：0次\")});$(function(){var caseinfo=JSON.stringify({\"结案方式\":null,\"补正文书\":\"2\",\"案件类型\":\"1\",\"裁判日期\":null,\"文书ID\":\"f08d1d40-b647-11e3-84e9-5cf3fc0c2c18\",\"审判程序\":\"其他\",\"案号\":\"无\",\"法院名称\":\"最高人民法院\",\"法院ID\":\"0\",\"案件基本情况段原文\":\"\",\"附加原文\":null,\"法院地市\":null,\"法院省份\":null,\"文本首部段落原文\":\"\",\"法院区域\":null,\"案件名称\":\"吴亚贤故意杀人、组织、领导黑社会性质组织等死刑复核刑事裁定书\",\"裁判要旨段原文\":\"\",\"法院区县\":null,\"DocContent\":\"\",\"诉讼记录段原文\":\"广东省湛江市中级人民法院审理湛江市人民检察院指控被告人吴亚贤犯组织、领导黑社会性质组织罪、故意杀人罪、故意伤害罪、非法采矿罪、寻衅滋事罪、强迫交易罪、敲诈勒索罪、故意毁坏财物罪、抽逃出资罪、妨害公务罪、非法持有枪支罪一案，于2010年12月20日以（2010）湛中法刑二初字第26号刑事附带民事判决，认定被告人吴亚贤犯组织、领导黑社会性质组织罪，判处有期徒刑九年；犯故意杀人罪，判处死刑，剥夺政治权利终身；犯故意伤害罪，判处有期徒刑二年；犯非法采矿罪，判处有期徒刑六年，并处罚金人民币四千万元；犯寻衅滋事罪，判处有期徒刑四年；犯强迫交易罪，判处有期徒刑二年，并处罚金人民币十万元；犯敲诈勒索罪，判处有期徒刑六年；犯故意毁坏财物罪，判处有期徒刑五年；犯抽逃出资罪，判处有期徒刑三年，并处罚金人民币一百万元；犯妨害公务罪，判处有期徒刑三年；犯非法持有枪支罪，判处有期徒刑一年，决定执行死刑，剥夺政治权利终身，并处罚金人民币四千一百一十万元。宣判后，吴亚贤提出上诉。广东省高级人民法院经依法开庭审理，于2012年8月17日以（2011）粤高法刑一终字第62号刑事判决，维持原审对被告人吴亚贤的判决，并依法报请本院核准。本院依法组成合议庭，对本案进行了复核，依法讯问了被告人，听取了辩护律师意见。现已复核终结\",\"判决结果段原文\":\"\",\"文本尾部原文\":\"\",\"上传日期\":\"\\/Date(1388039700000)\\/\",\"诉讼参与人信息部分原文\":\"\",\"文书类型\":null,\"文书全文类型\":null,\"效力层级\":null,\"不公开理由\":null});$(document).attr(\"title\",\"吴亚贤故意杀人、组织、领导黑社会性质组织等死刑复核刑事裁定书\");$(\"#tdSource\").html(\"吴亚贤故意杀人、组织、领导黑社会性质组织等死刑复核刑事裁定书 无\");$(\"#hidDocID\").val(\"f08d1d40-b647-11e3-84e9-5cf3fc0c2c18\");$(\"#hidCaseName\").val(\"吴亚贤故意杀人、组织、领导黑社会性质组织等死刑复核刑事裁定书\");$(\"#hidCaseNumber\").val(\"无\");$(\"#hidCaseInfo\").val(caseinfo);$(\"#hidCourt\").val(\"最高人民法院\");$(\"#hidCaseType\").val(\"1\");$(\"#HidCourtID\").val(\"0\");$(\"#hidRequireLogin\").val(\"0\");});$(function(){var dirData = {Elements: [\"RelateInfo\", \"LegalBase\"],RelateInfo: [{ name: \"审理法院\", key: \"court\", value: \"最高人民法院\" },{ name: \"案件类型\", key: \"caseType\", value: \"刑事案件\" },{ name: \"案由\", key: \"reason\", value: \"\" },{ name: \"审理程序\", key: \"trialRound\", value: \"其他\" },{ name: \"裁判日期\", key: \"trialDate\", value: \"2013-06-18\" },{ name: \"当事人\", key: \"appellor\", value: \"吴亚贤\" }],LegalBase: [{法规名称:'《中华人民共和国刑事诉讼法（2012年）》',Items:[{法条名称:'第二百三十五条',法条内容:' 第二百三十五条　死刑由最高人民法院核准。[ly]'},{法条名称:'第二百三十九条',法条内容:' 第二百三十九条　最高人民法院复核死刑案件，应当作出核准或者不核准死刑的裁定。对于不核准死刑的，最高人民法院可以发回重新审判或者予以改判。[ly]'}]},{法规名称:'最高人民法院关于适用《中华人民共和国刑事诉讼法》的解释',Items:[{法条名称:'第三百五十条',法条内容:' 第三百五十条　最高人民法院复核死刑案件，应当按照下列情形分别处理：[ly][ly] （一）原判认定事实和适用法律正确、量刑适当、诉讼程序合法的，应当裁定核准；[ly] （二）原判认定的某一具体事实或者引用的法律条款等存在瑕疵，但判处被告人死刑并无不当的，可以在纠正后作出核准的判决、裁定；[ly] （三）原判事实不清、证据不足的，应当裁定不予核准，并撤销原判，发回重新审判；[ly] （四）复核期间出现新的影响定罪量刑的事实、证据的，应当裁定不予核准，并撤销原判，发回重新审判；[ly] （五）原判认定事实正确，但依法不应当判处死刑的，应当裁定不予核准，并撤销原判，发回重新审判；[ly] （六）原审违反法定诉讼程序，可能影响公正审判的，应当裁定不予核准，并撤销原判，发回重新审判。[ly]'}]}]};if ($(\"#divTool_Summary\").length > 0) {$(\"#divTool_Summary\").ContentSummary({ data: dirData });}});$(function() { var jsonHtmlData = \"{\\\"Title\\\":\\\"吴亚贤故意杀人、组织、领导黑社会性质组织等死刑复核刑事裁定书\\\",\\\"PubDate\\\":\\\"2013-12-26\\\",\\\"Html\\\":\\\"\\\"}\"";
		String content=s;
		System.out.println((content));
		
		String caseInfo = content.substring(content.indexOf("caseinfo")+24);
		String dirData=caseInfo.substring(caseInfo.indexOf("dirData")+10);
		String jsonHtmlData = dirData.substring(dirData.indexOf("jsonHtmlData")+16);
		jsonHtmlData = jsonHtmlData.substring(jsonHtmlData.indexOf("Title")+10);
		String title = jsonHtmlData.substring(0,jsonHtmlData.indexOf("\\"));
		System.out.println(title);
		
		
	}
}
