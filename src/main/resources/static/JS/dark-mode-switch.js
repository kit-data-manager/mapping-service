const darkSwitch = document.getElementById("darkSwitch");
actualizeDarkMode()

function actualizeDarkMode() {
    if (document.cookie === "darkMode=true") {
        darkSwitch.checked = true;
        document.cookie = "darkMode=true";
        document.body.setAttribute("data-theme", "dark");
    } else if (document.cookie === "darkMode=false") {
        darkSwitch.checked = false;
        document.cookie = "darkMode=false";
        document.body.removeAttribute("data-theme");
    } else {
        document.cookie = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? "darkMode=true" : "darkMode=false";
        actualizeDarkMode()
    }
}


window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', event => {
    document.cookie = event.matches ? "darkMode=true" : "darkMode=false";
    actualizeDarkMode()
});

darkSwitch.addEventListener('change', () => {
    document.cookie = darkSwitch.checked ? "darkMode=true" : "darkMode=false";
    actualizeDarkMode()
});