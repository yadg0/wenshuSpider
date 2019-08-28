package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.annotation.Href;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.annotation.Text;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;

public class HrefBean implements SpiderBean {

	private static final long serialVersionUID = -3770871271092767593L;

	@Href
	@HtmlField(cssPath="a")
	private String url;
	
	@Text
	@HtmlField(cssPath="a")
	private String title;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
	
}
