package main.org.lele.spider;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.org.lele.Task.HttpResponse;
import main.org.lele.Task.Task;
import main.org.lele.db.MYSQLControl;
import main.org.lele.model.Item;
import main.org.lele.network.MultiplexingHttpConectionPool;
import main.org.lele.threadpool.ThreadPool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 
 * @author LELE 
 * 具体的抓取工作
 */
public class spider {
	private String baseUrl = "http://xiaoyuan.zhaopin.com" ;//首页
	private List<Item> itemList = new ArrayList<Item>() ;
	
	
    static final Log logger = LogFactory.getLog(spider.class);
    //爬取的主页
    public boolean processWelcome(String targetUrl){
		//要爬的页面
		//targetUrl = "http://xiaoyuan.zhaopin.com/job/CC000118114J90000011000" ;
		//网站的相应信息，包括response(网页代码)和statusCode(返回的状态码)
		HttpResponse hr = download(targetUrl) ;
		if(hr == null){
			return false ;
		}
		
		List<String> urlList  = new ArrayList<String>();
		//text存储已经获取的网页源代码
		String text = hr.getResponse();
		
		//targetUrl = "http://xiaoyuan.zhaopin.com/job/CC000118114J90000011000" ;
		String regex1 =  "<a.+?href=\"(/job/\\w+)\".+?\r\n.+?</a>";//正则表达式匹配工作链接                 
		Pattern pat1 = Pattern.compile(regex1); //将正则表达式进行编译
		Matcher m1 = pat1.matcher(text) ;
			while(m1.find()){
			String url = m1.group(1) ;
			url = baseUrl + url ;
			/*Item item = new Item(url);
			itemList.add(item);*/
			urlList.add(url);
			
		}
	    //<a socialhref="http://jobs.zhaopin.com/371716235250023.htm"  href="/FindFullTime/JobPosition/JobPositionDetail/CC371716235J90250023000/1" 
	    //  target="_blank" title="ios工程师实习生"> ios工程师实习生 </a>
	    
		String regex2 =  "<a socialhref=\"(http://jobs.zhaopin.com/\\w+\\.htm)\".+?\r\n.+?</a>";//正则表达式匹配工作链接                 
		Pattern pat2 = Pattern.compile(regex2); //将正则表达式进行编译
		Matcher m2 = pat2.matcher(text) ;
			while(m2.find()){
			String url = m2.group(1) ;
			urlList.add(url);
		}
		//对爬取的URL去重
	    List<String> listWithoutRepeated = new ArrayList<String>(new HashSet<String>(urlList));	
		for(String urlItem : listWithoutRepeated){
			Item item = new Item(urlItem);
			itemList.add(item);
		}
			
		return true ;
	}
	//多线程方式访问招聘详情页面
	public boolean processItem(){
		for(Item item : itemList){
			String targetUrl = item.getJobHref();
			HttpResponse hr = download(targetUrl) ;
			if(hr == null){
				return false ;
			}
			
			String text = hr.getResponse();
			text = text.replace("\n", "");
			//工作描述
			/*
	 *        <!-- SWSStringCutStart -->
                <p>岗位职责：</p><p>1.完成公司项目、产品的所有相关测试工作；<br> 2.根据产品需求和设计文档，制定测试计划，并分析测试需求、设计测试流程；<br> 3.根据产品测试需求完成测试环境的设计与配置工作；<br> 4.执行具体测试任务并确认测试结果、缺陷跟踪，完成测试报告以及测试结果分析；<br> 5.在测试各环节与开发、产品等部门沟通保证测试输入和输出的正确性和完备性；<br> 6.完成产品缺陷验证和确认，对于难以重现的缺陷，需要完成可能性原因分析与验证；<br> 7.定期提交产品缺陷统计分析报告并完成产品测试总结报告；<br> 8.完成测试团队的管理考核及业务培训工作。</p><p>&nbsp;</p><p>任职资格：</p><p>1. 经验不限，大专及以上学历均可；</p><p>2.计算机类、信息类等相关专业；（优秀的非计算机专业的也可以）</p><p>3.工作细致认真并富有耐心，充满热情，；</p><p>4.喜欢技术，踏实务实，一定技术学习能力；</p><p>&nbsp;</p><p>福利待遇：</p><p>1．富有竞争力的薪酬水平和其他福利津贴；</p><p>2．健全的五险一金；</p><p>3．给予完善的绩效考核，年终奖金及定期调薪；</p><p>4．带薪休假（年假，婚假，丧假，病假，培训假等）；</p><p>5．丰富的业余集体活动（拓展，旅游，聚餐，年会等）</p><p><br></p>
              <!-- SWSStringCutEnd -->
			 */
			String regex1 = "<p class=\"mt20\"(.+?)</p>" ;
			Pattern pat1 = Pattern.compile(regex1) ;
			Matcher m1 = pat1.matcher(text);
			
			String regex2 = "<!-- SWSStringCutStart -->\\s*(.+)\\s*<!-- SWSStringCutEnd -->";
			Pattern pat2 = Pattern.compile(regex2) ;
			Matcher m2 = pat2.matcher(text);
			
			String positionDetail = null ;
			
			if(m1.find()){
				positionDetail = m1.group(1) ;
				positionDetail = positionDetail .replaceAll("<br[/]?>", "\r\n") ;
				//positionDetail = positionDetail.replaceAll("</p>", "\r\n") ;
				//positionDetail = positionDetail.replaceAll("(.*?)", "") ;
				//positionDetail = positionDetail.replaceAll("<[^>]+>", "") ;
			}
			if(m2.find()){
				positionDetail = m2.group(1) ;
				// html 标签消除
				positionDetail = positionDetail.replaceAll("&nbsp;", "") ;
				positionDetail = positionDetail .replaceAll("<br[/]?>", "\r\n") ;
				positionDetail = positionDetail .replaceAll("</p>", "\r\n") ;
				positionDetail = positionDetail.replaceAll("<[^>]+>", "") ;
			}
			item.setJobDescription(positionDetail);
			
			//职位名称 jobName
			/**
			 * <h1 id="JobName" class="cJobDetailInforTitle">
                        气象软件工程师 <span class="cIcon cIcon_zl ml10">
                        </span>
                    </h1>
			 职位信息：气象软件工程师
			 */
			String regexJobName1 = "<h1 id=\"JobName\" class=\"cJobDetailInforTitle\">([^<]*)" ;
			Pattern patJobName1 = Pattern.compile(regexJobName1);
			Matcher mJobName1 = patJobName1.matcher(text);
			String regexJobName2 = "<h1>([^<]*)" ;
			Pattern patJobName2 = Pattern.compile(regexJobName2);
			Matcher mJobName2 = patJobName2.matcher(text);
			if(mJobName1.find()){
				String jobName = mJobName1.group(1);
				item.setJobName(jobName.substring(24));
			}
			if(mJobName2.find()){
				String jobName = mJobName2.group(1);
				item.setJobName(jobName);
			}

            //月薪
			//<li class="cJobDetailTit marb">月&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;薪：</li>
			//<li class="cJobDetailInforWd2 marb">6K-8K</li>
			String regexJobsalary1 = "<li class=\"cJobDetailTit marb\">月&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;薪：</li>\\s*<li class=\"cJobDetailInforWd2 marb\">(.+?)</li>";
			Pattern patJobSalary1 = Pattern.compile(regexJobsalary1);
			Matcher mJobSalary1 = patJobSalary1.matcher(text);
			
			//<li><span>职位月薪：</span><strong>8001-10000元/月&nbsp;<a href="http://www.zhaopin.com/gz_beijing/" target="_blank" title="北京工资计算器"><img src="http://jobs.zhaopin.com/images/calculator.png" alt="北京工资计算器"></a></strong></li>
			String regexJobsalary2 = "<li><span>职位月薪：</span><strong>(.+?)&nbsp";
			Pattern patJobSalary2 = Pattern.compile(regexJobsalary2);
			Matcher mJobSalary2 = patJobSalary2.matcher(text);
			
			if(mJobSalary1.find()){
				String jobSalary = mJobSalary1.group(1);
				item.setJobSalary(jobSalary);
			}
			
			if(mJobSalary2.find()){
				String jobSalary = mJobSalary2.group(1);
				item.setJobSalary(jobSalary);
			}
			
			
			//招聘发布时间 <span id="span4freshdate">4小时前</span>
			String regexReleaseDate1 = "<li class=\"cJobDetailInforWd2 marb\" id=\"liJobPublishDate\">(.+?)</li>" ;
			Pattern patReleaseDate1 = Pattern.compile(regexReleaseDate1);
			Matcher mReleaseDate1 = patReleaseDate1.matcher(text);
			
			String regexReleaseDate2 = "<span id=\"span4freshdate\">(.+?)</span>" ;
			Pattern patReleaseDate2 = Pattern.compile(regexReleaseDate2);
			Matcher mReleaseDate2 = patReleaseDate2.matcher(text);
			
			String releaseDate = null ;
			if(mReleaseDate1.find()){
				releaseDate = mReleaseDate1.group(1);
				
			}
			
			if(mReleaseDate2.find()){
				releaseDate = mReleaseDate2.group(1);
			}
			
			item.setReleaseDate(releaseDate);
			
			//工作地点 <li id="currentJobCity" class="cJobDetailInforWd2 marb" jct="北京" title='北京'>
			//<li><span>工作地点：</span><strong><a target="_blank" href="http://www.zhaopin.com/beijing/">北京</a></strong></li>
			String regexWorkPlace1 = "<li id=\"currentJobCity\" class=\"cJobDetailInforWd2 marb\" jct=\"(.*?)\" title=\'(.*?)\'>" ;
			Pattern patWorkPlace1 = Pattern.compile(regexWorkPlace1);
			Matcher mWorkPlace1 = patWorkPlace1.matcher(text);
			
			String regexWorkPlace2 = "<li><span>工作地点：</span><strong><a target=\"_blank\" href=\".*?\">(.*?)</a></strong></li>" ;
			Pattern patWorkPlace2 = Pattern.compile(regexWorkPlace2);
			Matcher mWorkPlace2 = patWorkPlace2.matcher(text);
			
			String workPlace = null ;
			if(mWorkPlace1.find()){
				workPlace = mWorkPlace1.group(2);
			}
			
			if(mWorkPlace2.find()){
				workPlace = mWorkPlace2.group(1);
			}
			item.setWorkPlace(workPlace);
			
			//招聘人数<li class="cJobDetailInforWd2 marb">8人</li>  <li class="cJobDetailInforWd2 marb">若干</li>
			//<li><span>招聘人数：</span><strong>10人 </strong></li>
			String regexRecruitmentNumber1 = "<li class=\"cJobDetailInforWd2 marb\">(.+?)人</li>" ;
			Pattern patRecruitmentNumber1 = Pattern.compile(regexRecruitmentNumber1);
			Matcher mRecruitmentNumber1 = patRecruitmentNumber1.matcher(text);
			
			String regexRecruitmentNumber2 = "<li><span>招聘人数：</span><strong>(.+?)人 </strong></li>" ;
			Pattern patRecruitmentNumber2 = Pattern.compile(regexRecruitmentNumber2);
			Matcher mRecruitmentNumber2 = patRecruitmentNumber2.matcher(text);
			
			String recruitmentNumber = null ;
			if(mRecruitmentNumber1.find()){
				recruitmentNumber = mRecruitmentNumber1.group(1);
			}else if(mRecruitmentNumber2.find()){
				recruitmentNumber = mRecruitmentNumber2.group(1);
			}else{
				recruitmentNumber = "若干";
			}
			item.setRecruitmentNumber(recruitmentNumber);
			
			//公司类型companyType
			//<li><span>公司性质：</span><strong>股份制企业</strong></li>
			
			String regexCompanyType1 = "<li>([\u4e00-\u9fa5]+)</li>" ;
			Pattern patCompanyType1 = Pattern.compile(regexCompanyType1);
			Matcher mCompanyType1 = patCompanyType1.matcher(text);
			
			String regexCompanyType2 = "<li><span>公司性质：</span><strong>(.*?)</strong></li>" ;
			Pattern patCompanyType2 = Pattern.compile(regexCompanyType2);
			Matcher mCompanyType2 = patCompanyType2.matcher(text);
			
			String companyType = null ;
			
			if(mCompanyType1.find()){
				companyType = mCompanyType1.group(1);
			}
			if(mCompanyType2.find()){
				companyType = mCompanyType2.group(1);
			}
			item.setCompanyType(companyType);
			
			//公司所在行业companyIndustry  <li class="cJobDetailInforWd2" title="计算机软件,互联网/电子商务">
			//<li><span>公司行业：</span><strong><a target="_blank" href="http://jobs.zhaopin.com/beijing/in210500/">互联网/电子商务</a></strong></li>
			
			String regexCompanyIndustry1 = "<li class=\"cJobDetailInforWd2\" title=\"(.+?)\">" ;
			Pattern patCompanyIndustry1 = Pattern.compile(regexCompanyIndustry1);
			Matcher mCompanyIndustry1 = patCompanyIndustry1.matcher(text);
			
			String regexCompanyIndustry2 = "<li><span>公司行业：</span><strong><a target=\"_blank\" href=\"(.*?)\">(.*?)</a></strong></li>" ;
			Pattern patCompanyIndustry2 = Pattern.compile(regexCompanyIndustry2);
			Matcher mCompanyIndustry2 = patCompanyIndustry2.matcher(text);
			
			String companyIndustry = null ;
			
			if(mCompanyIndustry1.find()){
				companyIndustry = mCompanyIndustry1.group(1);
			}
			if(mCompanyIndustry2.find()){
				companyIndustry = mCompanyIndustry2.group(2);
			}
			
			item.setCompanyIndustry(companyIndustry);
			
			//公司规模 <li class="cJobDetailInforWd2">1000-9999人</li> companySize
			//<li><span>公司规模：</span><strong>20-99人</strong></li>
			String regexCompanySize1 = "<li class=\"cJobDetailInforWd2\">(.*?)</li>" ;
			Pattern patCompanySize1 = Pattern.compile(regexCompanySize1);
			Matcher mCompanySize1 = patCompanySize1.matcher(text);
			
			String regexCompanySize2 = "<li><span>公司规模：</span><strong>(.*?)</strong></li>" ;
			Pattern patCompanySize2 = Pattern.compile(regexCompanySize2);
			Matcher mCompanySize2 = patCompanySize2.matcher(text);
			
			String companySize = null ;
			
			if(mCompanySize1.find()){
				companySize = mCompanySize1.group(1);
			}
			if(mCompanySize2.find()){
				companySize = mCompanySize2.group(1);
			}
			
			item.setCompanySize(companySize);
			
			
		}
		try {
			MYSQLControl.executeUpdate("delete from job_offers");
			MYSQLControl.executeInsert(itemList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true ;
	}
	public void print(){
		FileOutputStream fos = null ;
		try{
			System.out.println();
			fos = new FileOutputStream("zhilianzhaopin.txt") ;
		}catch(FileNotFoundException e){
		 
			e.printStackTrace();
		}
		for(Item item : itemList){
			try{
				fos.write(item.toString().getBytes());
			
			}catch(IOException e){
				
				e.printStackTrace();
			}
		}
		try{
			fos.flush();
		}catch(IOException e){
			 
			e.printStackTrace();
		}
		try{
			fos.close(); 
		}catch(IOException e){
			
			e.printStackTrace();
		}
	}
	/**
	 * 根据传入的targetUrl去拉取数据，仅当有实质内容返回时才不返回null
	 * @param targetUrl 目标资源的URL
	 * @return当有实质内容返回时返回一个HttpResponse object;其他情况返回null.
	 */
	public HttpResponse download(String targetUrl){
		System.out.println("\"小真君\"正在努力抓取" + targetUrl + "... ...") ;
		//从Http连接池获取一个长连接
		CloseableHttpClient client = MultiplexingHttpConectionPool.getInstance().getConnection(true);
		if(client == null){
			System.out
				.println("MultiplexingHttpConnectionPool.getInstance().getConnetion(true) returned null");
			return null ;
		}
		
		//构建一个task并扔到线程池去执行
		Future<HttpResponse> future = ThreadPool.getInstance().submit(new Task(client,targetUrl)) ;
		HttpResponse hr = null ;
		try{
			//在future对象中获取抓取结果，最多等待五秒，hr中是请求的返回信息  用来获取执行结果，如果在指定时间内，还没获取到结果，就直接返回null。
			hr = future.get(500000000,TimeUnit.MILLISECONDS);
		}catch(Exception e){
			future.cancel(true) ;
			e.printStackTrace();
		}
		
		//如果返回空，或者http返回码不为200，那么肯定没有抓取结果
		if(hr == null || hr.getStatusCode() != 200){
			return null ;
		}
		return hr;
	}
	
	public static void catchData(){//catchData
		
		spider spider1 = new spider() ;
		List<String> targetUrlList = new ArrayList<String>();
		String base = "https://xiaoyuan.zhaopin.com/FindFullTime/FullTimeSearch/SearchResult/?ind=210500&&SearchModel=0&&ref=jobsearch&&OB=1&&ST=0&&PC=530&&PJT=045&&PageSize=50&&PageNumber=";
		for(int  i = 1 ; i < 2 ; i++){
			targetUrlList.add(base + i);
		}
	
		for(String targetUrl : targetUrlList){
			@SuppressWarnings("unused")
			boolean hr = spider1.processWelcome(targetUrl) ;
		}
		
		//多线程
		spider1.processItem();
		
		/*ThreadPool.getInstance().shutdown();//关闭线程池
		MultiplexingHttpConectionPool.getInstance().uninitialize();//关闭http连接池
*/	}
}
