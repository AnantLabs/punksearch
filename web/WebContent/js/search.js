function changeSearchType(type) {
    var params = "?type=" + type;

    var form = document.forms["searchForm"];
    if (form) {
        var queryElement = form.elements["query"];
        if ((queryElement != null) && (queryElement.value.length != 0)) {
            params += "&query=" + queryElement.value.replace(/\+/g, "%2B");
        }
    }
    /*
     var offlineElement = document.forms["searchForm"].elements["showoffline"];
     if ((offlineElement != null)&&(offlineElement.checked)) {
     params += "&showoffline=on";
     }
     */
    window.location = params;
}

function setFocus(type) {
    var id = (type != null && type == 'advanced') ? 'dir' : 'query';
    var input = document.getElementById(id);
    if (input) {
        input.focus();
    }
}

$(function () {
//    $('.path').click(function () {alert($(this).text())});

    ZeroClipboard.setMoviePath('js/zeroclipboard/ZeroClipboard.swf');

    $.each($('.path'), function (i, path) {
        var container_id = 'clip_container_' + i;
        var button_id = 'clip_button_' + i;

        var path = $(path);
        path.after($('<div class="clip_container" id="' + container_id + '">' +
            '<div id="' + button_id + '">[Copy to Clipboard]</div></div>'));


        var clip = new ZeroClipboard.Client();
        clip.setText($.trim(path.text()));
        clip.glue(button_id, container_id);
    });
});
