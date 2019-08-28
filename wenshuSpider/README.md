#文书网爬虫项目
本项目主入口类：PageSortList.java

该程序分5步骤来获取数据，现阶段只能手工配置一年一年的获取数据。

##1、获取法院信息。
&emsp;&emsp;主要参数： step1GetSortAndCourt=true  
##2、生成获取文章列表的任务,配合时间参数来拆分获取文章Id任务。
&emsp;&emsp;时间参数为：
- step2_startDate,开始时间，可以为空，空值时使用当前时间
- step2_endDate,结束时间，可以为空，空值时，为开始时间 加 +step2_Date 
- step2_stepDate可以是n，15，30，365，如果为1，代表每天建立任务，这个在每天获取最新数据时有用，当然你也可以每n天跑一次任务。15,30,365为特殊值，表示按半月、月、年建立任务，当按这些值建立任务时，使用的是裁判日期，而其他值使用的是上传日期条件。step2_endDate为空时，会默认为当前开始日期所在年份的最后一天，如果是当前年，就是今天。
示例： 
-	按半月建立2017的数据任务：step2_startDate=2017-01-01,step2_endDate=2017-12-31,step2_stepDate=15
-	按月建立2017的数据任务：step2_startDate=2017-01-01,step2_endDate=2017-12-31,step2_stepDate=30
-	按天建立2018-06-01之后的任务：step2_startDate=2018-06-01,step2_endDate=,step2_stepDate=1
-	按7天建立2018-06-01之后的任务：step2_startDate=2018-06-01,step2_endDate=,step2_stepDate=7
	主要参数： 
		step2SaveArticleListTask=true
		step2_startDate=2017-01-01
		step2_endDate=2017-12-31
		step2_stepDate=15  
		
##3、获取文章Id列表  
&emsp;&emsp;step3_startDate和step3_endDate用于查询哪年的任务，以及取到的目录放入哪个 article表。  
&emsp;&emsp;取出来的任务，首先会将state设置为4，正在下载时，设置状态为1，下载过程中有某天大于500条数据，是需要关注的，state=5；失败状态为3；下载完成为2。
	主要参数： 
	step3_step3GetArticleList=true 
	step3_startDate=2017-01-01
	step3_endDate=2017-12-31
	step3_stepDate=15	
	step3_startPos=0 //一次取多少条任务
	step3_endPos=2
- 第3步获取列表的url如下：  
 - &emsp;&emsp;最新数据，按上传日期搜索:
http://wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++法院名称:最高人民法院&conditions=searchWord+++2018-06-15 TO 2018-06-21+上传日期:2018-06-15 TO 2018-06-21  
 - &emsp;&emsp;旧的数据（15,30,365），按裁判日期搜索:
wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++法院名称:最高人民法院&conditions=searchWord++CPRQ++裁判日期:2018-01-01 TO 2018-01-15
- 如果法院名称为"xxx县人民法院"，还会加上"基层法院"条件，为了避免 "南县人民法院"和"衡南县人民法院"类似问题。
##4、获取文章内容
	获取文章内容，因为一个ip最多获取2000左右文章，就会被封一段时间。所以最好使用代理ip来获取；程序会自动读取proxyReqUrl，通过这个url去获取代理ip；配合设置step4GetArticleDetail_threads=n时，会获取n个代理，4*n个线程获取文章内容。
	文章表的状态开始为0，下载过程中为1，成功为2。
	代理购买的是极光ip代理：http://h.jiguangip.com/，6台机器消耗11000/天ip；不同的代理解释会不一样，需要改代码来解析。
	主要参数： 
		step4GetArticleDetail=true
		step4GetArticleDetail_threads=10
		step4GetArticleDetail_interval=300
		step45_startDate=2017-01-01	//获取哪年的文章内容
		step45_startPos=0	//每次都获取0-100行状态为0数据，数据获取后，会改变状态
		step45_endPos=100
		proxyReqUrl=http://d.jghttp.golangapi.com/getip?num={thread_count}&type=2&pro=&city=0&yys=0&port=1&pack=558&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=		//获取代理的url，这个根据购买的套餐变化而变化
		
##5、清洗js内容。清洗article表中的数据，由data字段清洗后保存到decode_data中
&emsp;&emsp;主要参数： step5DecodeArticleData=true
			step45_startPos=0	//每次都获取0-100行状态为2的数据，数据获取后，会改变状态
			step45_endPos=100
			step45_startDate=2017-01-01//任务5时，可以跨年
			step45_endDate=2018-12-31
##6、部署问题
&emsp;&emsp;39.104.183.144  有3个dos窗口，一个为 zookeeper的service，其他机器都为跟随者。
	该机器上的wenshu.jar，一个用于上传配置，一个用于运行爬虫，所以有2个dos窗口。
-	上传配置，打开144机器上的 wenshu-conf.prop 文件，下面3行是不需要同步的配置。
	driverPath=C:/wenshu/geckodriver.exe
	zooMasterHostPort=172.24.57.153:80
	zooDataPath=/taskConfig
	而后面的 node.n.ips和node.n.config，用于配置机器的运行参数，n是顺序的0-10，用于配置不同组的机器干什么事。程序会判断，如果有node.n.ip等配置，认为是修改配置。
-	运行爬虫，只要注释这些配置(前面加#)，就是认为在运行爬虫。
	比如以下配置：配置了node.0的5个ip来下载内容，node.1的1个ip用于清洗js
	node.0.ips=39.104.183.144,39.104.78.19,39.104.97.51,39.104.107.245,39.104.17.205
	node.0.config=step4GetArticleDetail=true;step45_startPos=0;step45_endPos=100;step45_startDate=2017-01-01;step45_endDate=2017-12-31;step45_stepDate=15;step4GetArticleDetail_threads=10;step4GetArticleDetail_interval=50;proxyReqUrl=http://d.jghttp.golangapi.com/getip?num={thread_count}&type=2&pro=&city=0&yys=0&port=1&pack=558&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=
	node.1.ips=101.132.183.8
	node.1.config=step5DecodeArticleData=true=true;step45_startPos=0;step45_endPos=100;step45_startDate=2017-01-01;step45_endDate=2018-12-31;step45_stepDate=365;

##7、关于清洗的说明
	程序加载本地doc.html，替换${datas}为数据库中的data，为数据库中的data做了如下处理：
	String data = article.getData().replaceAll("var ", "window.")
							.replaceAll("Content\\.Content\\.InitPlugins\\(\\);", "")
							.replaceAll("Content\\.Content\\.KeyWordMarkRed\\(\\);", "")
							.replaceAll("'情节严重'", "\"情节严重\"");
	最后，通过调用js的getData方法，得到所有数据：
	function getData(){
			return JSON.stringify({dirData:dirData,htmlData:jsonData,jsonHtml:jsonHtml,caseinfo:JSON.parse(caseinfo)});
		}
##8、每天最新数据抓取说明
每天最新数据，需要有一台机器作为主机，主机通过配置 masterIp=主机ip 来比较和判断，其它机器可以辅助完成第3、4、5步骤。
主机每个步骤运行次数，函数runtask会执行所有5步操作，下面所有变量都会减1，运行这些步骤时，会检查如果该步骤运行次数<0了，就不执行。
当所有任务执行完毕，主控机器会进入等待配置改变状态。如果还有失败的任务，那么需要人工update数据库，然后，再提交配置，让任务重新执行
	step1retryTimes=3;//运行第1步，运行3次
	step2retryTimes=2;//运行第2步，运行2次
	step3retryTimes=5;//运行第3步，运行5次
	step4retryTimes=5;//运行第4步，运行5次
	step5retryTimes=2;//运行第5步，运行2次
	
比如，按周（为什么用周，因为7天生成任务，这样任务数比较少，当然也可以按天，就是task表数据多点而已）抓取最新数据：
	配置开始日期为 stepx_startDate=2018-06-01,stepx_stepDate=7 ，主机（主机的配置需要增加 masterIp ）收到配置改变的任务，首先执行 update 所有 任务状态为0，并执行5步操作。而其它主机不会执行update操作。
	当所有日期抓完之后，可以设置开始日期为上周的周一，这样，每天都发送这个消息，就会每周都会重新获取数据，这样来保证数据更全。
	数据不能跨年，所以，如果到了第一年的开始第一天，需要设置 stepx_startDate=2019-01-01
##9、常用文书网链接
- 第一步获取法院列表，无需cookie  
1、获取2、3级法院：http://wenshu.court.gov.cn/Index/GetCourt,POST 请求，参数：province=北京市  
2、获取4级法院： http://wenshu.court.gov.cn/Index/GetChildAllCourt,POST请求，参数 keyCodeArrayStr，所有的3级法院id列表
- 第3步，获取文章列表，需要cookie,get请求  
 - &emsp;&emsp;新数据，按上传日期获取
http://wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++法院名称:最高人民法院&conditions=searchWord+++2018-06-15 TO 2018-06-21+上传日期:2018-06-15 TO 2018-06-21
 - &emsp;&emsp;旧的数据（15,30,365），按裁判日期搜索:
wenshu.court.gov.cn/List/List/?sorttype=1&conditions=searchWord++SLFY++法院名称:最高人民法院&conditions=searchWord++CPRQ++裁判日期:2018-01-01 TO 2018-01-15
- 第4步，获取文书内容,get 请求
http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID={DocId}

##10、常用sql
-查看第3步任务情况：
//查看未完成的任务
select * from article_page_task where 
 date>'2016-06-01' and end_date<=now() and (
	(state=1) or (state=3) or (state=4)
	or (state=2 and total_count is null)
	or (state=2 and succ_count < total_count)
	or (state=5 and succ_count < total_count)
 )
and date_type='上传日期' order by id;

//查看完成的任务
select * from article_page_task where 
 date>'2016-06-01' and end_date<=now() and ((state=2 or state=5) and succ_count>=total_count)
and date_type='上传日期' order by id;

//更新失败的任务
update article_page_task set state=0 where  date>'2016-06-01' and end_date<=now() and (
	(state=1) or (state=3) or (state=4)
	or (state=2 and total_count is null)
	or (state=2 and succ_count < total_count)
	or (state=5 and succ_count < total_count)
)
and date_type='上传日期'

-查看第4步骤获取内容情况：  
&emsp;&emsp;select count(*),state from article group by state  
失败时，state=1会为1，所以为1的结果过多，需要手动update一下，更新文章状态为1（下载失败）和6（清洗失败）的为0。
&emsp;&emsp; update article set state=0 where state=1 or state=6
-查看第5步清洗情况：  
	select count(*),state from article group by state  
&emsp;&emsp; 清洗失败的数据 state会为6
-更新清洗失败的数据为下载完成
	update article set state=2 where state=6