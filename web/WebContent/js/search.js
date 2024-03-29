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

function next_clip_id() {
    return arguments.callee.clip_id++;
}
next_clip_id.clip_id = 0;

function add_copy_to_clipboard(paths) {
    $.each(paths, function (i, path) {
        if (!path.clipboard_clip) {
            var clip_id = next_clip_id();

            var container_id = 'clip_container_' + clip_id;
            var button_id = 'clip_button_' + clip_id;

            var clip = new ZeroClipboard.Client();
            path.clipboard_clip = clip;

            path = $(path);
            path.after($('<div class="clip_container" id="' + container_id + '">' +
                '<a id="' + button_id + '">[Copy to Clipboard]</a></div>' +
                '<div class="clipboard_ok_container"><div class="clipboard_ok" style="display: none;" /></div>'));

            clip.setText($.trim(path.text()));
            clip.glue(button_id, container_id);
            clip.addEventListener('onComplete', function (client, text) {
//                alert('ok');
                var ok_icon = $('#' + container_id).next().find('.clipboard_ok');
                ok_icon.fadeIn(300, function () {
                    ok_icon.fadeOut(2000);
                });
            });
        }
    });
}

$(function () {
    ZeroClipboard.setMoviePath('js/zeroclipboard/ZeroClipboard.swf');

    add_copy_to_clipboard($('.path').not(':hidden'));
});
