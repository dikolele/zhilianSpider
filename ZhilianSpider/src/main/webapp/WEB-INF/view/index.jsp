<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>

<body>
<style type="text/css">
table.hovertable {
	font-family: verdana,arial,sans-serif;
	font-size:11px;
	color:#333333;
	border-width: 1px;
	border-color: #999999;
	border-collapse: collapse;
	margin:0 auto;
}
table.hovertable th {
	background-color:#c3dde0;
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #a9c6c9;
}
table.hovertable tr {
	background-color:#d4e3e5;
}
table.hovertable td {
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #a9c6c9;
}
</style>

<table class="hovertable" id="mytable">
<caption><h2><a href="/ZhilianSpider/catch">获取数据</a></h2></caption>
<caption><h1>智联招聘信息</h1></caption>
<tr>
	<th>职位名称</th><th>职位链接</th><th>发布日期</th>
	<th>工作地点</th><th>公司行业</th><th>公司规模</th>
	<th>公司类型</th><th>招聘人数</th><th>职位要求</th>
</tr>

<c:forEach items="${offerList}" var="offerList">
	<tr onmouseover="this.style.backgroundColor='#ffff66';" onmouseout="this.style.backgroundColor='#d4e3e5';">
		
		<td>${offerList.jobName}</td>
		<td>${offerList.jobHref}</td>
		<td>${offerList.releaseDate}</td>
		<td>${offerList.workPlace}</td>
		<td>${offerList.companyIndustry}</td>
		<td>${offerList.companySize}</td>
		<td>${offerList.companyType}</td>
		<td>${offerList.recruitmentNumber}</td>
		<td>${offerList.jobDescription}</td>
		
	</tr>
</c:forEach>
</table>
</body>
</html>