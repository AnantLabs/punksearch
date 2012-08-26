function toggle(id) {
	var obj = document.getElementById(id);
	obj.style.display = (obj.style.display == "none")? "block" : "none";

    add_copy_to_clipboard($(obj).find('.path'));
}

function toggleTable(id) {
	var obj = document.getElementById(id);
	obj.style.display = (obj.style.display == "none")? "table" : "none";
}

function changeLogoColors()
{
	var color = document.getElementById("logo.punk").style.color;
	document.getElementById("logo.punk").style.color = document.getElementById("logo.search").style.color;
	document.getElementById("logo.search").style.color = color;
}

