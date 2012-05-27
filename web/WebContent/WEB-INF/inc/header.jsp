<%@ include file="/WEB-INF/inc/imports.jsp" %>
<div id="topline">
	<a href="#" onclick="changeTheme()" style="float: right;">
		<img style="border: 0px;" width="16" height="16" src="images/color_swatch.gif" />
	</a>
</div>

<c:set var="baseUrl" value="${pageContext.request.contextPath}/"/>

<div id="logo">
	<div style="padding-top: 10px; font-size: 20px;">
		<a href="${baseUrl}"><span id="logo.punk" onmouseover="changeLogoColors()" style="color: #ffffff;">PUNK</span><span id="logo.search" onmouseover="changeLogoColors()" style="color: #FF7B00;">Search</span></a>
	</div>
	<span style="font-size: 10px;">
		<a href="http://code.google.com/p/punksearch">project home</a>
		&#160;&#160;
		<a href="http://code.google.com/p/punksearch/issues/list">issues</a>
	</span>
</div>