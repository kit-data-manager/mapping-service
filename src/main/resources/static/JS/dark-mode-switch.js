const darkSwitch = document.getElementById("darkSwitch");

if(document.cookie === "darkMode=true"){
  darkSwitch.checked = true;
  document.cookie = "darkMode=true";
  document.body.setAttribute("data-theme", "dark");
} else if(document.cookie === "darkMode=false"){
  darkSwitch.checked = false;
  document.cookie = "darkMode=false";
  document.body.removeAttribute("data-theme");
} else {
  document.cookie = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? "darkMode=true" : "darkMode=false";
}

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', event => {
  if (event.matches) {
    document.cookie = "darkMode=true";
    document.body.setAttribute("data-theme", "dark");
  } else {
    document.cookie = "darkMode=false";
    document.body.removeAttribute("data-theme");
  }
});

darkSwitch.addEventListener('change', () => {
  if (darkSwitch.checked) {
    document.cookie = "darkMode=true";
    document.body.setAttribute("data-theme", "dark");
  } else {
    document.cookie = "darkMode=false";
    document.body.removeAttribute("data-theme");
  }
});