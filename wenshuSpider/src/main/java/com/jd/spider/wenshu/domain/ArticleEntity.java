package com.jd.spider.wenshu.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author yangdongjun3
 *
 */
public class ArticleEntity extends BaseDomain implements Serializable {
	private static final long serialVersionUID = 1L;
	
		private Long id;
		private String docId;
		private String title;
		private String data;
		/**
		* 状态;0未开始;1处理中;2已完成
		 */	
		private Integer state;
		private Long taskId;
		private String taskStartDate;
		private String decodeData;
		
		/**
		 * 分表用的字段
		 */
		private String tableSub;
		private List<Long> ids;
		
		public Long getId(){
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getDocId(){
			return docId;
		}
		public void setDocId(String docId) {
			this.docId = docId;
		}
		public String getTitle(){
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getData(){
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public Integer getState(){
			return state;
		}
		public void setState(Integer state) {
			this.state = state;
		}
		public Long getTaskId() {
			return taskId;
		}
		public void setTaskId(Long taskId) {
			this.taskId = taskId;
		}
		public String getTaskStartDate() {
			return taskStartDate;
		}
		public void setTaskStartDate(String taskStartDate) {
			this.taskStartDate = taskStartDate;
			String tmpTableSub=null;
			if(taskStartDate!=null){
				tmpTableSub=taskStartDate.substring(0,4);
			}else{
				tmpTableSub="";
			}
			if("2017".equals(tmpTableSub)){
				tmpTableSub="";
			}
			tableSub=tmpTableSub;
		}
		public String getTableSub() {
			return tableSub;
		}
		public void setTableSub(String tableSub) {
			this.tableSub = tableSub;
		}
		public String getDecodeData() {
			return decodeData;
		}
		public void setDecodeData(String decodeData) {
			this.decodeData = decodeData;
		}
		public List<Long> getIds() {
			return ids;
		}
		public void setIds(List<Long> ids) {
			this.ids = ids;
		}
}
