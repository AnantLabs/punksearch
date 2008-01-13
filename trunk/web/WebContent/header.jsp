<script>
    function changeSearchType(type)
    {
        var params = "?type="+type;
        
        var queryElement = document.forms["searchForm"].elements["query"];
        if ((queryElement != null)&&(queryElement.value.length != 0))
            params += "&query=" + queryElement.value;                               
        
        var offlineElement = document.forms["searchForm"].elements["showoffline"];
        if ((offlineElement != null)&&(offlineElement.checked))
            params += "&showoffline=on";                                
        
        window.location = params;
    }
    function setFocus(type)
    {
        var id = (type != null && type == 'advanced')? 'dir' : 'query';
        var input = document.getElementById(id);
        input.focus();
    }
    function toggle(id)
    {
        var obj = document.getElementById(id);
        obj.style.display = (obj.style.display == "none")? "block" : "none";
    }
    
    var currentThemeIdx = 0;
    var themes = ["orange_blue", "yellow_blue", "emo"];
    function changeTheme()
    {
        currentThemeIdx++;
        if (currentThemeIdx >= themes.length) {
            currentThemeIdx = 0;
        }
        var expires_date = new Date( new Date().getTime() + (1000 * 24 * 60 * 60 * 1000) ); // +1000 days
        document.cookie = "theme=" + escape( themes[currentThemeIdx] ) + ";expires=" + expires_date.toGMTString();
        document.getElementById("theme_css").href="css/" + themes[currentThemeIdx] + ".css";
    }
</script>

<div id="topline">
    <a href="#" onclick="changeTheme()" style="float:right;"><img style="border: 0px;" width="16" height="16" src="images/color_swatch.gif"/></a>
</div>

<div id="logo">	
	<script>
		function changeLogoColors()
		{
			var color = document.getElementById("logo.punk").style.color;
			document.getElementById("logo.punk").style.color = document.getElementById("logo.search").style.color;
			document.getElementById("logo.search").style.color = color;
		}
	</script>	
	<div style="padding-top:10px; font-size: 20px;">
		<span id="logo.punk" onmouseover="changeLogoColors()" style="color:#ffffff;">PUNK</span><span id="logo.search" onmouseover="changeLogoColors()" style="color:#FF7B00;">Search</span>
	</div>
	<span style="font-size: 10px;">
		<a href="http://code.google.com/p/punksearch">project home</a>
		&#160;&#160;
		<a href="http://code.google.com/p/punksearch/issues/list">issues</a>
	</span>
</div>