const params = new URLSearchParams(window.location.search);
const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration/";
const execUrl = location.protocol + "//" + location.host + "/api/v1/mappingExecution/";

let availableTypes = []
load()

function load() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + "types")
    http.send();
    http.onload = (e) => {
        const result = JSON.parse(http.responseText)
        for (let i = 0; i < result.length; i++) {
            let option = document.createElement("option");
            option.value = result[i]
            option.text = result[i]
            document.getElementById("type").add(option)
        }
        availableTypes = result
        if (params.has("id") && params.has("type")) {
            const id = params.get("id")
            const type = params.get("type")
            document.getElementById("id").value = id
            if (result.indexOf(type) >= 0) {
                document.getElementById('type').value = type;
            }
        }
    }
}

function map() {
    const id = document.getElementById("id").value
    const type = document.getElementById("type").value
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
    http.open("POST", execUrl + id + "/" + type)
    http.send(formData)
    http.onload = (e) => {
        console.log(http.responseText)
        document.getElementById("downloadButton").setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(http.responseText));
        document.getElementById("downloadButton").setAttribute('download', "result.json");
        document.getElementById("progress").hidden = true
        document.getElementById("downloadButton").hidden = false
        document.getElementById("submit").disabled = false
        const downloadHTTP = new XMLHttpRequest();
        downloadHTTP.open("GET", apiUrl + id + "/" + type);
        downloadHTTP.send();
        downloadHTTP.onload = (e) => {
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