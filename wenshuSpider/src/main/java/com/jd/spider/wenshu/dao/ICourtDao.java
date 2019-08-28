package com.jd.spider.wenshu.dao;

import java.util.List;
import java.util.Map;

import com.jd.spider.wenshu.domain.Court;

public interface ICourtDao {

	/**
	 * 添加并返回设置id的Court对象
	 * 
	 * @param court
	 * @return
	 */
	public Long addCourt(Court court);

	/**
	 * 更新court
	 * 
	 * @param court
	 */
	public void updateCourt(Court court);

	/**
	 * 根据主键删除court
	 * 
	 * @param court
	 */
	public void deleteCourtById(Court court);

	/**
	 * 根据主键获取court
	 * 
	 * @param id
	 * @return
	 */
	public Court getCourtById(Long id);

	/**
	 * 分页取得court列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<Court> getCourtByPage(Map<String, Object> paramMap);

	/**
	 * 根据查询条件返回数量
	 * 
	 * @param paramMap
	 * @return
	 */
	public int count(Map<String, Object> paramMap);

	public Court getUnique(Map<String, Object> param);
	
	/**
	 *court列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<Court> getCourt(Court court);
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public List<Court> getAllCourt();
	
	

}
