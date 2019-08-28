package com.jd.spider.wenshu;

import java.util.List;

/**
 * 用于保存法院信息的树形结构
 * @author yangdongjun
 *
 */
public class TreeItem {
	/**
	 * 父亲id
	 */
	private String key;
	private String itemHead;

	private List<TreeItem> children;
	private String treeContentUrl;
	private String cssSelector;//暂时不用了
	private String parval;//需要传递的参数
	private String param;//需要传递的参数
	private Integer level;//级别，从1开始，对应菜单层级
	private Long dbCourtId;
	//有jstree-closed标签的是可以展开的。jstree-open  jstree-last
	//private String isLeaf;
	//http://wenshu.court.gov.cn/List/CourtTreeContent
	
	private String code;//对应文书网的key 001031000
	private String parentCode;//父亲code
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getItemHead() {
		return itemHead;
	}
	public void setItemHead(String itemHead) {
		this.itemHead = itemHead;
	}
	public List<TreeItem> getChildren() {
		return children;
	}
	public void setChildren(List<TreeItem> children) {
		this.children = children;
	}
	public String getTreeContentUrl() {
		return treeContentUrl;
	}
	public void setTreeContentUrl(String treeContentUrl) {
		this.treeContentUrl = treeContentUrl;
	}

	public String getCssSelector() {
		return cssSelector;
	}
	public void setCssSelector(String cssSelector) {
		this.cssSelector = cssSelector;
	}
	public String getParval() {
		return parval;
	}
	public void setParval(String parval) {
		this.parval = parval;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public Long getDbCourtId() {
		return dbCourtId;
	}
	public void setDbCourtId(Long dbCourtId) {
		this.dbCourtId = dbCourtId;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getParentCode() {
		return parentCode;
	}
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
}
