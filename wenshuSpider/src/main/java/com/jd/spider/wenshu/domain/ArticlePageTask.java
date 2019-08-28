package com.jd.spider.wenshu.domain;

import java.io.Serializable;

/**
 * 
 * @author yangdongjun3
 *
 */
public class ArticlePageTask extends BaseDomain implements Serializable {
	private static final long serialVersionUID = 1L;
	
		private Long id;
		private Long courtId;
		private String courtName;
		private String date;
		private String endDate;
		/**
		* 是否已经完成0未完成;1进行中;2已完成
		 */	
		private Integer state;
		/**
		 * 花费时间
		 */
		private Long costTime;
		/**
		* 第一页时的数量
		 */	
		private Integer totalCount;
		/**
		* 成功获取数量
		 */	
		private Integer succCount;
		
		private String dateType;//日期类型：是裁判日期还是上传日期
		
		public Long getId(){
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getCourtId(){
			return courtId;
		}
		public void setCourtId(Long courtId) {
			this.courtId = courtId;
		}
		public String getCourtName(){
			return courtName;
		}
		public void setCourtName(String courtName) {
			this.courtName = courtName;
		}
		public String getDate(){
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public Integer getState(){
			return state;
		}
		public void setState(Integer state) {
			this.state = state;
		}
		public Long getCostTime() {
			return costTime;
		}
		public void setCostTime(Long costTime) {
			this.costTime = costTime;
		}
		public Integer getTotalCount() {
			return totalCount;
		}
		public void setTotalCount(Integer totalCount) {
			this.totalCount = totalCount;
		}
		public Integer getSuccCount() {
			return succCount;
		}
		public void setSuccCount(Integer succCount) {
			this.succCount = succCount;
		}
		public String getEndDate() {
			return endDate;
		}
		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
		public String getDateType() {
			return dateType;
		}
		public void setDateType(String dateType) {
			this.dateType = dateType;
		}
}
