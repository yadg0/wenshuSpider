package com.jd.spider.wenshu.dao;

import java.util.List;
import java.util.Map;

import com.jd.spider.wenshu.domain.ArticlePageTask;

public interface IArticlePageTaskDao {

	/**
	 * 添加并返回设置id的ArticlePageTask对象
	 * 
	 * @param articlePageTask
	 * @return
	 */
	public Long addArticlePageTask(ArticlePageTask articlePageTask);

	/**
	 * 更新articlePageTask
	 * 
	 * @param articlePageTask
	 */
	public void updateArticlePageTask(ArticlePageTask articlePageTask);

	/**
	 * 根据主键删除articlePageTask
	 * 
	 * @param articlePageTask
	 */
	public void deleteArticlePageTaskById(ArticlePageTask articlePageTask);

	/**
	 * 根据主键获取articlePageTask
	 * 
	 * @param id
	 * @return
	 */
	public ArticlePageTask getArticlePageTaskById(Long id);

	/**
	 * 分页取得articlePageTask列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<ArticlePageTask> getArticlePageTaskByPage(Map<String, Object> paramMap);

	/**
	 * 根据查询条件返回数量
	 * 
	 * @param paramMap
	 * @return
	 */
	public int count(Map<String, Object> paramMap);

	public ArticlePageTask getUnique(Map<String, Object> param);
	
	/**
	 *articlePageTask列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<ArticlePageTask> getArticlePageTask(ArticlePageTask articlePageTask);
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public List<ArticlePageTask> getAllArticlePageTask();

	public List<ArticlePageTask> findFailedArticlePageTask(ArticlePageTask taskParam);

	public void updateFailedArticlePageTask(ArticlePageTask task);
	
	

}
