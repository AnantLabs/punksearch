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
