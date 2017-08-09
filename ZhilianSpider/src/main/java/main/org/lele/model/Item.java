package main.org.lele.model;
/**
 * 
 * @author LELE 
 * 表示一个完整的智联招聘项
 *
 */
public class Item {
	
	private String jobId ;	//招聘信息id
	private String jobName ;	//职位名称
	private String jobSalary ;//月薪
	private String jobHref ;	//招聘信息链接
	private String releaseDate ;	//招聘信息的发布日期
	private String workPlace ;	//工作地点
	private String companyIndustry ;	//公司所在行业
	private String companySize ;	//公司规模
	private String companyType ;	//公司类型
	private String recruitmentNumber ;	//招聘人数
	private String jobDescription ;	//职位描述
	
	
	public Item(){}	
	
	public Item(String jobHref){
		this.jobHref = jobHref ;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder() ;
		sb.append(jobSalary).append("\n")
		.append(jobHref).append("\n")
			.append(jobName).append("\n")
			.append(releaseDate).append("\n")
			.append(workPlace).append("\n")
			.append(recruitmentNumber).append("\n") 
			.append(companyType).append("\n")
			.append(companyIndustry).append("\n")
			.append(companySize).append("\n")
			.append(jobDescription).append("\n\n");

		return  sb.toString();
		
	}
	
	

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobHref() {
		return jobHref;
	}

	public void setJobHref(String jobHref) {
		this.jobHref = jobHref;
	}
	public String getJobSalary() {
		return jobSalary;
	}

	public void setJobSalary(String jobSalary) {
		this.jobSalary = jobSalary;
	}
	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public String getWorkPlace() {
		return workPlace;
	}
	public void setWorkPlace(String workPlace) {
		this.workPlace = workPlace;
	}

	public String getRecruitmentNumber() {
		return recruitmentNumber;
	}

	public void setRecruitmentNumber(String recruitmentNumber) {
		this.recruitmentNumber = recruitmentNumber;
	}

	public String getCompanyType() {
		return companyType;
	}

	public void setCompanyType(String companyType) {
		this.companyType = companyType;
	}

	public String getCompanyIndustry() {
		return companyIndustry;
	}

	public void setCompanyIndustry(String companyIndustry) {
		this.companyIndustry = companyIndustry;
	}

	public String getCompanySize() {
		return companySize;
	}

	public void setCompanySize(String companySize) {
		this.companySize = companySize;
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
}
