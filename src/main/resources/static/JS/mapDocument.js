const params = new URLSearchParams(window.location.search);
const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration/";
const execUrl = location.protocol + "//" + location.host + "/api/v1/mappingExecution/";

let types = []
let selectedType = null
load()

function load() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + "types")
    http.send();
    http.onload = (e) => {
        const result = JSON.parse(http.responseText)
        for (let i = 0; i < result.length; i++) {
            types.push(result[i].id)
            addType(result[i].id, result[i].name, result[i].description, result[i].version, result[i].uri, result[i].inputTypes.toString(), result[i].outputTypes.toString())
        }
        data = JSON.parse(window.sessionStorage.getItem("data"))
        if (data != null && data.id != null && data.type != null) {
            console.log("Received data from session storage: " + data)
            document.getElementById("id").value = data.id
            selectType(data.type)
        }
    }
}

function addType(typeID, typeName, typeDescription, typeVersion, typeLink, typeMIMEOutput, typeMIMEInput) {
    const element =
        `<div class="accordion-item row align-items-center clearfix">
            <div class="accordion-header" id="heading_${typeID}">
                <div class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#collapse_${typeID}" aria-expanded="false" aria-controls="collapse_${typeID}">
                    <div class="row align-items-center me-3 col-auto ">
                        <button class="btn btn-primary" onclick="selectType('${typeID}')" id="select.${typeID}">
                            <i class="bi bi-check2-square me-1"></i> Select
                        </button>
                        <button class="btn btn-outline-danger" onclick="unselect()" id="unselect.${typeID}" hidden>
                            <i class="bi bi-x-square me-1"></i> Unselect
                        </button>
                    </div>
                    <div class="vr me-4"></div>
                    <div class="col-auto row align-items-center">
                        <h4 class="p-1 col-auto text-black">${typeName}</h4>
                        <span class="col-auto text-muted">ID: ${typeID}</span>
                    </div>
                </div>
            </div>
            <div id="collapse_${typeID}" class="accordion-collapse collapse show" aria-labelledby="heading_${typeID}" data-bs-parent="#typeAccordion">
                <div class="accordion-body me-auto col-auto row align-items-center">
                    <div class="me-auto col-auto row align-items-center">
                        <h5 class="p-1 col-auto">Description: </h5>
                        <span class="p-1 col-auto text-muted">${typeDescription}</span>
                    </div>
                    <div class="me-auto col-auto row align-items-center">
                        <h5 class="p-1 col-auto"><a href="${typeLink}" target="_blank">Link to the project</a></h5>
                    </div>
                    <div class="me-auto col-auto row align-items-center">
                        <h5 class="p-1 col-auto">Input types: </h5>
                        <span class="col-auto text-muted font-monospace">${typeMIMEInput}</span>
                    </div>
                    <div class="me-auto col-auto row align-items-center">
                        <h5 class="p-1 col-auto">Output types: </h5>
                        <span class="col-auto text-muted font-monospace">${typeMIMEOutput}</span>
                    </div>
                    <div class="me-auto col-auto row align-items-center">
                        <h5 class="p-1 col-auto">Version: </h5>
                        <span class="col-auto text-muted font-monospace">${typeVersion}</span>
                    </div>
                </div>
            </div>
        </div>`
    const html = document.getElementById('typeAccordion')
    html.innerHTML += element
}

function selectType(typeID) {
    unselect()
    selectedType = typeID
    document.getElementById("select." + typeID).hidden = true
    document.getElementById("unselect." + typeID).hidden = false
}

function unselect() {
    for (let i = 0; i < types.length; i++) {
        document.getElementById("select." + types[i]).hidden = false
        document.getElementById("select." + types[i]).disabled = false
        document.getElementById("unselect." + types[i]).hidden = true
    }
}

function reloadTypes() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + "/reloadTypes")
    http.send()
    http.onload = (e) => {
        location.reload()
    }
}

function map() {
    const id = document.getElementById("id").value
    const type = selectedType
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