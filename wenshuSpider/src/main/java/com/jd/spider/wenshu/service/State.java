package com.jd.spider.wenshu.service;

/**
 * article_page_task表,以及article表中对应的状态
 * @author yangdongjun
 *
 */
public class State {
	public static int NEW=0;
	public static int READY=4;//准备阶段，表示已经被读取到内存了，准备下载，一次是读取100条任务
	public static int DOWNLOADING=1;
	public static int DOWNLOADED=2;
	public static int FAILED=3;
	public static int OVERFLOW_FAILED=5;
	public static int DECODEING=6;
	public static int FINISHED=7;
}
