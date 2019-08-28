package com.jd.spider.wenshu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.impl.xpath.regex.Match;

import com.geccocrawler.gecco.annotation.Href;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.annotation.Text;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.SpiderBeanContext;

public class Sort implements HtmlBean {

	private static final long serialVersionUID = 3018760488621382659L;

	@Text
	@HtmlField(cssPath="a:nth-child(1)")
	private String categoryName;
	
	@Href
	@HtmlField(cssPath="a:nth-child(1)")
	private String url;
	
	//private PageSortDetail sortDetail;
	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

//	public List<HrefBean> getCategorys() {
//		return categorys;
//	}
//
//	public void setCategorys(List<HrefBean> categorys) {
//		this.categorys = categorys;
//	}
//	
	@Override
	public void customerProcess(SpiderBean currSpiderBeanClass, SpiderBeanContext context,
			HttpRequest request, HttpResponse response) {
		
	}
	
	public static void main(String[] args) {
		//从:'到 ',;} ，替换成 ""
		//也就是用'开头，第2个单引号，和第3个单信号
		//' 一下清新认定为'金额巨大':一、我草'
		String js="LegalBase: [{法规名称:'《中华人民共和国婚姻法（2001年）》',Items:[{法条名称:'第十七条第一款',法条内容:'    第十七条　夫妻在婚姻关系存续期间所得的下列财产，归夫妻共同所有：&amp;#xA;    （一）工资、奖金；&amp;#xA;    （二）生产、经营的收益；&amp;#xA;    （三）知识产权的收益；&amp;#xA;    （四）继承或赠与所得的财产，但本法第十八条第三项规定的除外；&amp;#xA;    （五）其他应当归共同所有的财产。&amp;#xA;    夫妻对共同所有的财产，有平等的处理权。&amp;#xA;'},{法条名称:'第十八条',法条内容:'    第十八条　有下列情形之一的，为夫妻一方的财产：&amp;#xA;    （一）一方的婚前财产；&amp;#xA;    （二）一方因身体受到伤害获得的医疗费、残疾人生活补助费等费用；&amp;#xA;    （三）遗嘱或赠与合同中确定只归夫或妻一方的财产；&amp;#xA;    （四）一方专用的生活用品；&amp;#xA;    （五）其他应当归一方的财产。&amp;#xA;'}]},{法规名称:'最高人民法院关于适用《中华人民共和国婚姻法》若干问题的解释（二）',Items:[{法条名称:'第十一条',法条内容:'    第十一条　婚姻关系存续期间，下列财产属于婚姻法第十七条规定的'其他应当归共同所有的财产'： &amp;#xA;    （一）一方以个人财产投资取得的收益；&amp;#xA;    （二）男女双方实际取得或者应当取得的住房补贴、住房公积金；&amp;#xA;    （三）男女双方实际取得或者应当取得的养老保险金、破产安置补偿费。&amp;#xA;'}]},{法规名称:'最高人民法院关于适用《中华人民共和国婚姻法》若干问题的解释（三）',Items:[{法条名称:'第五条',法条内容:'    第五条　夫妻一方个人财产在婚后产生的收益，除孳息和自然增值外，应认定为夫妻共同财产。&amp;#xA;'}]}]";
//		String js="{依据:'  认定'一级':'一、金额1万''二、不承认'',b:'dd'}";
//		String regex="(.*?):(\\s*)\'(.*?)\'(?=[,|}|;])";
		//
		String regex="(.*?:\')(.*?)(\'[,|}|;])";
		Matcher mather=Pattern.compile(regex).matcher(js);
		StringBuffer sb=new StringBuffer();
        while(mather.find()) {
        	String tmp=mather.group(2).replaceAll("'", "\\\\\\\\'");
        	mather.appendReplacement(sb, "$1"+tmp+"$3");
        }
        mather.appendTail(sb);
        System.out.println(sb);
//        js=js.replaceAll(regex, "$1:$2\"$3\"");
	}
}
