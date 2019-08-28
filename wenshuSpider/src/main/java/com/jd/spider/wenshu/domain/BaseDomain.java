package com.jd.spider.wenshu.domain;

import java.util.Date;

//import org.springframework.format.annotation.DateTimeFormat;

public class BaseDomain {
	private String orderBy;
	/**
	 * 查询字段 limit
	 * @return
	 */
	private Integer startIndex;
	private Integer pageSize;
	/**
	* 0无效1有效
	 */	
	private Integer yn;
//	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createdTime;
	private String createdUser;
//	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updatedTime;
	private String updatedUser;
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public Integer getYn() {
		return yn;
	}
	public void setYn(Integer yn) {
		this.yn = yn;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public String getCreatedUser() {
		return createdUser;
	}
	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}
	public Date getUpdatedTime() {
		return updatedTime;
	}
	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}
	public String getUpdatedUser() {
		return updatedUser;
	}
	public void setUpdatedUser(String updatedUser) {
		this.updatedUser = updatedUser;
	}
	public Integer getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
}
