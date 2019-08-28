package com.jd.spider.wenshu.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.jd.spider.wenshu.Article;
import com.jd.spider.wenshu.WenshuMain;
import com.jd.spider.wenshu.dao.IArticleDao;
import com.jd.spider.wenshu.dao.IArticlePageTaskDao;
import com.jd.spider.wenshu.dao.ICourtDao;
import com.jd.spider.wenshu.domain.ArticleEntity;
import com.jd.spider.wenshu.domain.ArticlePageTask;
import com.jd.spider.wenshu.domain.Court;
/**
 * 封装数据库的操作
 * @author yangdongjun
 *
 */
public class WenshuService {
	private static Log log = LogFactory.getLog(WenshuService.class);
	    private static SqlSession sqlSession;
	    private static ICourtDao courtDao;
	    private static IArticlePageTaskDao articlePageTaskDao;
	    private static IArticleDao articleDao;
	    
	    public static void initialConnection() {    
	        try{    
	            String resource = null;
	            Reader reader = null;    
	        	if(WenshuMain.packageType==PackageType.JAR){
	        		resource = System.getProperty("user.dir")+File.separator+"mybatis-config.xml";
	        		reader = new InputStreamReader(new FileInputStream(resource));// Resources.getResourceAsReader(resource);
	        	}else{
	        		resource = "mybatis-config.xml";
	        		reader = Resources.getResourceAsReader(resource);
	        	}
	            SqlSessionFactory ssf = new SqlSessionFactoryBuilder().build(reader);
	            //sqlSession = ssf.openSession();  
	            sqlSession = ssf.openSession(false);
	            courtDao = sqlSession.getMapper(ICourtDao.class);
	            articlePageTaskDao = sqlSession.getMapper(IArticlePageTaskDao.class);
	            articleDao = sqlSession.getMapper(IArticleDao.class);
	            log.info("init mysql conntect ok..");
	        } catch (Exception e) {    
	            e.printStackTrace();    
	        } 
	    }    

	    public static void destroyConnection() {
	        sqlSession.close();
	    }

	    /**
	     * 增加法院，如果存在，那么返回存在法院的id。
	     * @param court
	     */
	    public static void addCourt(Court court) {
	    	Court courtParam=new Court();
	    	courtParam.setName(court.getName());
	    	List<Court> courtList=courtDao.getCourt(courtParam);
	    	if(courtList!=null && courtList.size()>0){
	    		Court tmpCourt=courtList.get(0);
	    		court.setId(tmpCourt.getId());
	    		return;
	    	}
	    	try{    
		    	courtDao.addCourt(court);
		        sqlSession.commit();
	        } catch (Exception e) {
	        	sqlSession.rollback();
	            e.printStackTrace();    
	        }
	    }
	    public static void updateCourt(Court court) {
	    	courtDao.updateCourt(court);
	        sqlSession.commit();
	    }

	    public static List<Court> getCourtList(Court court) {
	        return courtDao.getCourt(court);
	    }

		public static void addArticlePageTask(ArticlePageTask task) {
			ArticlePageTask taskParam=new ArticlePageTask();
			taskParam.setCourtName(task.getCourtName());
			taskParam.setDate(task.getDate());
			taskParam.setEndDate(task.getEndDate());
	    	List<ArticlePageTask> taskList=articlePageTaskDao.getArticlePageTask(taskParam);
	    	if(taskList!=null && taskList.size()>0){
	    		ArticlePageTask tmpTask=taskList.get(0);
	    		task.setId(tmpTask.getId());
	    		return;
	    	}
	    	
			try{    
				articlePageTaskDao.addArticlePageTask(task);
				sqlSession.commit();
	        } catch (Exception e) {
	        	sqlSession.rollback();
	            e.printStackTrace();    
	        }
		}

		public static void addArticle(ArticleEntity articleEntity) {
			Date begin=new Date();
			ArticleEntity articleParam=new ArticleEntity();
			articleParam.setDocId(articleEntity.getDocId());
			articleParam.setTaskStartDate(articleEntity.getTaskStartDate());
	    	List<ArticleEntity> articleList=articleDao.getArticle(articleParam);
	    	if(articleList!=null && articleList.size()>0){
	    		ArticleEntity tmpArticleEntity=articleList.get(0);
	    		articleEntity.setId(tmpArticleEntity.getId());
	    		return;
	    	}
	    	
			try{    
				articleDao.addArticle(articleEntity);
				sqlSession.commit();
	        } catch (Exception e) {
	        	sqlSession.rollback();
	            e.printStackTrace();    
	        }
			Date end=new Date();
			log.info("新增文章 花费时间 :"+(end.getTime()-begin.getTime()));
		}
		
		public static void addArticleDecode(ArticleEntity articleEntity) {
			Date begin=new Date();
			ArticleEntity articleParam=new ArticleEntity();
			articleParam.setDocId(articleEntity.getDocId());
			articleParam.setTaskStartDate(articleEntity.getTaskStartDate());
			try{
				articleDao.addArticleDecode(articleEntity);
				articleEntity.setDecodeData(null);
		    	articleDao.updateArticle(articleEntity);
				sqlSession.commit();
	        } catch (Exception e) {
	        	sqlSession.rollback();
	            e.printStackTrace();    
	        }
			Date end=new Date();
			log.info("新增文章 花费时间 :"+(end.getTime()-begin.getTime()));
		}
		
		public static void updateArticle(ArticleEntity articleEntity){
			articleDao.updateArticle(articleEntity);
			sqlSession.commit();
		}
		
		public static Long updateFailArticleToNew(ArticleEntity articleEntity){
			Long n=articleDao.updateFailArticleToNew(articleEntity);
			sqlSession.commit();
			return n;
		}
		
		public static void updateArticlePageTask(ArticlePageTask task){
			//查找id
			Date begin=new Date();
			ArticlePageTask taskParam=new ArticlePageTask();
			taskParam.setCourtName(task.getCourtName());
			taskParam.setDate(task.getDate());
			taskParam.setEndDate(task.getEndDate());
			List<ArticlePageTask> taskList=articlePageTaskDao.getArticlePageTask(taskParam);
	    	if(taskList!=null && taskList.size()>0){
	    		task.setId(taskList.get(0).getId());
	    	}
			articlePageTaskDao.updateArticlePageTask(task);
			sqlSession.commit();
			Date end=new Date();
			log.info(" 更新任务 花费时间:"+(end.getTime()-begin.getTime()));
	
		}

		public static List<ArticleEntity> getArticleEntityList(ArticleEntity article) {
			log.info("开始查询。。。");
			Date begin=new Date();
			List<ArticleEntity> articleList = articleDao.getArticle(article);
			if(articleList.size()==0) {
				return articleList;
			}
			log.info("查询完成，锁定这些记录。。。id="+articleList.get(0).getId()+"到"+articleList.get(articleList.size()-1).getId());
			ArticleEntity newArticleEntity=new ArticleEntity();
			if(article.getState()==State.NEW)
				newArticleEntity.setState(State.DOWNLOADING);
			else if(article.getState()==State.DOWNLOADED)
				newArticleEntity.setState(State.DECODEING);
			newArticleEntity.setTaskStartDate(article.getTaskStartDate());
			newArticleEntity.setIds(new ArrayList<Long>());
			for(ArticleEntity tmpArticle:articleList){
				newArticleEntity.getIds().add(tmpArticle.getId());
			}
			articleDao.batchUpdateState(newArticleEntity);
			sqlSession.commit();
			log.info("锁定完成。。。耗时："+(new Date().getTime()-begin.getTime()));
			return articleList;
		}

		public static List<ArticlePageTask> findFailedArticlePageTask(ArticlePageTask taskParam,String ip) {
			Date begin=new Date();
			taskParam.setOrderBy("id");
			List<ArticlePageTask> articleList = articlePageTaskDao.findFailedArticlePageTask(taskParam);
			ArticlePageTask newArticlePageTask=new ArticlePageTask();
			for(ArticlePageTask tmpArticle:articleList){
				newArticlePageTask.setId(tmpArticle.getId());
				newArticlePageTask.setState(State.READY);
				newArticlePageTask.setCreatedUser(ip);
				newArticlePageTask.setCreatedTime(new Date());
				articlePageTaskDao.updateArticlePageTask(newArticlePageTask);
			}
			sqlSession.commit();
			Date end=new Date();
			log.info(" 获取任务 花费时间:"+(end.getTime()-begin.getTime()));
			return articleList;
		}

		/**
		 * 更新文章内容
		 * @param finishedArticle
		 */
		public static void updateArticleList(List<Article> finishedArticle) {
			log.info("开始更新一批内容："+finishedArticle.size());
			ArticleEntity articleEntity=new ArticleEntity();
			Date dt=new Date();
			for(Article article:finishedArticle){
				if(article==null || article.getId()==null) {
					log.info("updateArticleList update but docId is null");
					continue;
				}
				articleEntity.setDocId(article.getId());
				articleEntity.setState(State.DOWNLOADED);
				articleEntity.setTaskStartDate(article.getTaskStartDate());
				articleEntity.setData(article.getOrgionContent());
				articleEntity.setUpdatedTime(dt);
				try{
					log.info("更新内容："+articleEntity.getDocId());
					articleEntity.setData(filterOffUtf8Mb4(articleEntity.getData())); 
					articleDao.updateArticle(articleEntity);
					sqlSession.commit();
				}catch(Exception e){
					log.error("更新失败：",e);
					e.printStackTrace();
				}
			}
			log.info("开始更新一批内容完成："+finishedArticle.size());
		}
		
		public static String filterOffUtf8Mb4(String text) throws UnsupportedEncodingException {
//	        log.info("替换非utf-8字符开始");
			byte[] bytes = text.getBytes("utf-8");
	        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
	        int i = 0;
	        while (i < bytes.length) {
	            short b = bytes[i];
	            if (b > 0) {
	                buffer.put(bytes[i++]);
	                continue;
	            }
	            b += 256;
	            if (((b >> 5) ^ 0x6) == 0) {
	                buffer.put(bytes, i, 2);
	                i += 2;
	            } else if (((b >> 4) ^ 0xE) == 0) {
	                buffer.put(bytes, i, 3);
	                i += 3;
	            } else if (((b >> 3) ^ 0x1E) == 0) {
	                i += 4;
	            } else if (((b >> 2) ^ 0x3E) == 0) {
	                i += 5;
	            } else if (((b >> 1) ^ 0x7E) == 0) {
	                i += 6;
	            } else {
	                buffer.put(bytes[i++]);
	            }
	        }
	        buffer.flip();
//	        log.info("替换非utf-8字符结束");
	        return new String(buffer.array(), "utf-8");
	    }

		public static void updateFailedArticlePageTask(ArticlePageTask task) {
			articlePageTaskDao.updateFailedArticlePageTask(task);
			sqlSession.commit();
		}

		public static void updateFailedArticle(ArticleEntity articleEntity) {
			articleDao.updateFailedArticle();
			sqlSession.commit();
		}
}
