<div id="header">
	<div style="height:50px; background-color: #004368; border-top: 1px solid white;" ></div>
	<div style="height:50px; background-color: #FF7B00; border-bottom: 1px solid white; border-top: 1px solid white;" ></div>
	<div style="background-color:white; position:absolute; left:100px; top:0px; height:103px; width:1px; font-size:0px;" ></div>
	
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
</div>