package com.jd.spider.wenshu.dao;

import java.util.List;
import java.util.Map;

import com.jd.spider.wenshu.domain.ArticleEntity;

public interface IArticleDao {

	/**
	 * 添加并返回设置id的Article对象
	 * 
	 * @param article
	 * @return
	 */
	public Long addArticle(ArticleEntity article);

	/**
	 * 更新article
	 * 
	 * @param article
	 */
	public Long updateArticle(ArticleEntity article);
	
	/**
	 * 根据主键删除article
	 * 
	 * @param article
	 */
	public void deleteArticleById(ArticleEntity article);

	/**
	 * 根据主键获取article
	 * 
	 * @param id
	 * @return
	 */
	public ArticleEntity getArticleById(ArticleEntity article);

	/**
	 * 分页取得article列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<ArticleEntity> getArticleByPage(Map<String, Object> paramMap);

	/**
	 * 根据查询条件返回数量
	 * 
	 * @param paramMap
	 * @return
	 */
	public int count(Map<String, Object> paramMap);

	public ArticleEntity getUnique(Map<String, Object> param);
	
	/**
	 *article列表
	 * 
	 * @param paramMap
	 * @return
	 */
	public List<ArticleEntity> getArticle(ArticleEntity article);
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public List<ArticleEntity> getAllArticle();

	public void batchUpdateState(ArticleEntity newArticleEntity);
	/**
	 * 新增解释js之后的数据，id，docid都与原来表一致
	 * @param articleEntity
	 */
	public void addArticleDecode(ArticleEntity articleEntity);
	
	//废弃了
	public void updateFailedArticle();
	
	public Long updateFailArticleToNew(ArticleEntity article);

}
