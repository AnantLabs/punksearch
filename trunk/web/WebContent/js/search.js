function changeSearchType(type)
{
	var params = "?type="+type;
    
	var queryElement = document.forms["searchForm"].elements["query"];
	if ((queryElement != null)&&(queryElement.value.length != 0)) {
		params += "&query=" + queryElement.value.replace(/\+/g, "%2B");
	}                               
	/*
	var offlineElement = document.forms["searchForm"].elements["showoffline"];
	if ((offlineElement != null)&&(offlineElement.checked)) {
		params += "&showoffline=on";
	}       
	*/
	window.location = params;
}

function setFocus(type)
{
	var id = (type != null && type == 'advanced')? 'dir' : 'query';
	var input = document.getElementById(id);
	input.focus();
}