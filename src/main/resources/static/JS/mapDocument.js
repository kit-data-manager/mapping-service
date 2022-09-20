const execUrl = location.protocol + "//" + location.host + "/api/v1/mappingExecution/"
const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration/";
load()

function load() {
    const data = JSON.parse(window.sessionStorage.getItem("data"))
    if (data != null && data.id != null) {
        console.log("Received data from session storage: " + data)
        document.getElementById("id").value = "" + data.id
    }
}

function map() {
    const id = document.getElementById("id").value
    const file = document.getElementById("document").files[0]
    if (file == null) {
        document.getElementById("errorMessage").textContent = "Please select a file."
        document.getElementById("errorMessage").hidden = false
        return
    }

    document.getElementById("progress").hidden = false
    document.getElementById("downloadButton").hidden = true
    document.getElementById("submit").disabled = true

    console.log(file.size)
    let formData = new FormData()
    formData.append("document", file)

    const http = new XMLHttpRequest();
    http.open("POST", execUrl + id)
    http.send(formData)
    http.onload = () => {
        console.log(http.responseText)
        document.getElementById("downloadButton").setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(http.responseText));
        document.getElementById("downloadButton").setAttribute('download', "result.json");
        document.getElementById("progress").hidden = true
        document.getElementById("downloadButton").hidden = false
        document.getElementById("submit").disabled = false
        const downloadHTTP = new XMLHttpRequest();
        downloadHTTP.open("GET", apiUrl + id);
        downloadHTTP.send();
        downloadHTTP.onload = () => {
            const element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(http.responseText));
            element.setAttribute('download', "result.json");
            element.style.display = 'none';
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
        }

    }
    http.onprogress = () => {
        document.getElementById("progress").hidden = false
        document.getElementById("downloadButton").hidden = true
        document.getElementById("submit").disabled = true
    }
    http.ontimeout = () => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("downloadButton").hidden = false
        document.getElementById("submit").disabled = false
        document.getElementById("errorMessage").textContent = "Timeout! Please try later again."
        document.getElementById("errorMessage").hidden = false
    }
    http.onerror = () => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("downloadButton").hidden = false
        document.getElementById("submit").disabled = false
        document.getElementById("errorMessage").textContent = "ERROR " + http.status + " (" + http.statusText + ") Please try later again."
        document.getElementById("errorMessage").hidden = false
    }
}