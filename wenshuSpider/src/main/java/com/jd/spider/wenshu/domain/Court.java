package com.jd.spider.wenshu.domain;

import java.io.Serializable;

/**
 * 
 * @author yangdongjun3
 *
 */
public class Court extends BaseDomain implements Serializable {
	private static final long serialVersionUID = 1L;
	
		private Long id;
		private String name;
		private String param;
		private String parval;
		/**
		* 从1开始，对应菜单层级
		 */	
		private Integer level;
		private Long key; //父亲id
		private Integer taskState;
		
		public Long getId(){
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName(){
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getParam(){
			return param;
		}
		public void setParam(String param) {
			this.param = param;
		}
		public String getParval(){
			return parval;
		}
		public void setParval(String parval) {
			this.parval = parval;
		}
		public Integer getLevel(){
			return level;
		}
		public void setLevel(Integer level) {
			this.level = level;
		}
		public Long getKey(){
			return key;
		}
		public void setKey(Long key) {
			this.key = key;
		}
		public Integer getTaskState() {
			return taskState;
		}
		public void setTaskState(Integer taskState) {
			this.taskState = taskState;
		}
}
