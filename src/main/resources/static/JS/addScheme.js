const apiUrl = "./api/v1/mappingAdministration";

let acl = []
let types = []
const container = document.getElementById("jsoneditor")
const options = {
    mode: 'tree',
    modes: ['code', 'text', 'tree', 'view']
}
const editor = new JSONEditor(container, options)
let isEdit = false
let aclEdit
let data
let selectedType = null
let isJSONInput = true
load()

function loadFile() {
    const reader = new FileReader();
    reader.onload = (event) => {
        try {
            const obj = JSON.parse(event.target.result);
            console.log(obj)
            editor.set(obj)
            editor.expandAll()
        } catch (e) {
            console.log("Error parsing JSON: " + e)
            editor.set({})
            isJSONInput = false
        }
    };
    reader.readAsText(document.getElementById("schema").files[0]);
}

function load() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + "/types")
    http.send();
    http.onload = () => {
        const result = JSON.parse(http.responseText)
        for (let i = 0; i < result.length; i++) {
            types.push(result[i].id)
            addType(result[i].id, result[i].name, result[i].description, result[i].version, result[i].uri, result[i].inputTypes.toString(), result[i].outputTypes.toString())
        }
        data = JSON.parse(window.sessionStorage.getItem("data"))
        if (data != null && data.record != null && data.schema != null && data.ETAG != null) {
            console.log("Received data from session storage: " + data)
            isEdit = true
            changeUIMode()
            document.getElementById("id").value = data.record.mappingId
            document.getElementById("id").disabled = true
            document.getElementById("id").ariaReadOnly = "true"
            // document.getElementById("type").value = data.record.mappingType
            selectType(data.record.mappingType)
            document.getElementById("title").value = data.record.title
            document.getElementById("descr").value = data.record.description
            for (let i = 0; i < data.record.acl.length; i++) {
                acl.push({
                    "id": data.record.acl[i].id,
                    "sid": data.record.acl[i].sid,
                    "permission": data.record.acl[i].permission
                })
                addACLElement(acl.length - 1, data.record.acl[i].sid, data.record.acl[i].permission)
            }
            editor.set(data.schema)
            editor.expandAll()
        }
    }
}

function addACL() {
    const sid = document.getElementById("sid").value
    const permission = document.getElementById("permission").value
    if (aclEdit != null && sid !== acl[0].sid) {
        deleteACL(aclEdit)
        aclEdit = null
    }
    acl.push({
        "id": "",
        "sid": sid,
        "permission": permission
    })
    addACLElement(acl.length - 1, sid, permission)
    console.log("Successfully added ACL: " + acl[acl.length - 1])
}

function addACLElement(index, sid, permission) {
    const element =
        `<div class="border p-3 border rounded-3 m-3 col-auto" id="${index}">
            <div class="row align-items-center col-auto mx-auto">
                <button type="button" class="btn btn-primary col-auto m-1" onclick="editACL('${index}')">
                    <svg class="bi me-1" fill="currentColor" width="16" height="16">
                        <use xlink:href="#editButton"/>
                    </svg>
                    Edit
                </button>
                <button type="button" class="btn btn-outline-danger col-auto m-1" onclick="deleteACL('${index}')">
                    <svg class="bi me-1" fill="currentColor" width="16" height="16">
                        <use xlink:href="#deleteButton"/>
                    </svg>
                    Delete ACL
                </button>
            </div>
            <div class="mb-3">
                <label for="sid" class="col-form-label">User String</label>
                <input type="text" id="sidDisplay" class="form-control" disabled value="${sid}">
            </div>
            <div class="mb-3">
                <label for="permission" class="col-form-label">Permissions</label>
                <select id="permissionDisplay" class="form-select" disabled>
                    <option value="${permission}" selected>${permission}</option>
                </select>
            </div>
        </div>`;
    const html = document.getElementById('acls');
    html.innerHTML += element;
}

function editACL(index) {
    console.log("Editing ACL " + acl[index])
    document.getElementById("addACLButton").click()
    document.getElementById("sid").value = acl[index].sid
    document.getElementById("permission").value = acl[index].permission
    showSID(true)
    aclEdit = index
}

function showSID(edit){
    if(isEdit && edit) {
        console.log("Disabling input!")
        document.getElementById("sid").disabled = true
        document.getElementById("sid").ariaReadOnly = "true"
    } else {
        console.log("Enabling input!")
        if(!edit) document.getElementById("sid").value = ""
        document.getElementById("sid").disabled = false
        document.getElementById("sid").ariaReadOnly = "false"
    }
}

function deleteACL(index) {
    document.getElementById(index).remove()
    delete acl[index]
    console.log("Successfully deleted ACL with index of " + index)
}

function createMapping() {
    const id = document.getElementById("id").value
    const type = selectedType
    const title = document.getElementById("title").value
    const description = document.getElementById("descr").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}" && isJSONInput) {
        document.getElementById("errorMessage").textContent = "Please define a schema document."
        document.getElementById("errorMessage").hidden = false
        return
    }

    let resultACLs = []
    acl.forEach((value) => {
        resultACLs.push(value)
    })
    const record = {
        "mappingId": id,
        "mappingType": type,
        "title": title,
        "description": description,
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    let documentBlob
    if(isJSONInput) documentBlob = new Blob([editor.getText()], {type: "application/json"})
    else documentBlob = file

    console.log("Sending record:" + JSON.stringify(record))
    console.log("Sending document:" + editor.getText())
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("POST", apiUrl)
    http.send(formData)
    http.onload = () => {
        console.log("Response: " + http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        if (http.status <= 300) {
            document.getElementById("errorMessage").hidden = true
            document.getElementById("successDisplay").hidden = false
            setTimeout(() => {
                clearForm()
            }, 10000)
        }
    }
    http.onprogress = () => {
        document.getElementById("progress").hidden = false
        document.getElementById("submit").disabled = true
    }
    http.ontimeout = () => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        document.getElementById("errorMessage").textContent = "Timeout! Please try later again."
        document.getElementById("errorMessage").hidden = false

    }
    http.onerror = () => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        document.getElementById("errorMessage").textContent = "ERROR " + http.status + " (" + http.statusText + ") Please try later again."
        document.getElementById("errorMessage").hidden = false

    }
}

function updateMapping() {
    const id = document.getElementById("id").value
    const type = selectedType
    const title = document.getElementById("title").value
    const description = document.getElementById("descr").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}" && isJSONInput) {
        document.getElementById("errorMessage").textContent = "Please define a schema document."
        document.getElementById("errorMessage").hidden = false
        return
    }

    let resultACLs = []
    acl.forEach((value) => {
        resultACLs.push(value)
    })
    const record = {
        "mappingId": id,
        "mappingType": type,
        "title": title,
        "description": description,
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    let documentBlob
    if(isJSONInput) documentBlob = new Blob([editor.getText()], {type: "application/json"})
    else documentBlob = new Blob([file], {type: "unknown"})

    console.log("Sending record:" + JSON.stringify(record))
    console.log("Sending document:" + editor.getText())
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("PUT", apiUrl + "/" + id)
    http.setRequestHeader("If-Match", data.ETAG)
    http.send(formData)
    http.onload = () => {
        console.log("Response: " + http.responseText)
        document.getElementById("editProgress").hidden = true
        document.getElementById("update").disabled = false
        if (http.status <= 300) {
            const element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(editor.getText()));
            if (file != null) element.setAttribute('download', file.name);
            else element.setAttribute('download', id + "_schema.json");
            element.style.display = 'none';
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
            document.getElementById("editSuccessDisplay").hidden = false
            setTimeout(() => {
                document.getElementById("editSuccessDisplay").hidden = true
                clearForm()
            }, 10000)
        }
    }
    http.onprogress = () => {
        document.getElementById("editProgress").hidden = false
        document.getElementById("update").disabled = true
    }
    http.ontimeout = () => {
        console.log(http.responseText)
        document.getElementById("editProgress").hidden = true
        document.getElementById("update").disabled = false
        document.getElementById("errorMessage").textContent = "Timeout! Please try later again."
        document.getElementById("errorMessage").hidden = false

    }
    http.onerror = () => {
        console.log(http.responseText)
        document.getElementById("editProgress").hidden = true
        document.getElementById("update").disabled = false
        document.getElementById("errorMessage").textContent = "ERROR " + http.status + " (" + http.statusText + ") Please try later again."
        document.getElementById("errorMessage").hidden = false

    }
}

function clearForm() {
    document.getElementById("form").reset()
    document.getElementById("successDisplay").hidden = true
    document.getElementById("editSuccessDisplay").hidden = true
    document.getElementById("errorMessage").hidden = true
    unselect()
    acl.forEach((value, key) => {
        deleteACL(key)
    })
    acl = []
    editor.set({})
    window.sessionStorage.clear()
    if (isEdit) {
        isEdit = false
        window.location = "showSchemes.html"
    }
    isEdit = false
    console.log("Successfully cleared the form.")
}

window.addEventListener('beforeunload', () => {
    clearForm()
})

function changeUIMode() {
    document.getElementById("description").hidden = isEdit
    document.getElementById("editDescription").hidden = !isEdit
    document.getElementById("save").hidden = isEdit
    document.getElementById("editSave").hidden = !isEdit
    document.getElementById("editNavAdd").hidden = !isEdit
    document.getElementById("navAdd").hidden = isEdit
    document.getElementById("editNavShow").hidden = !isEdit
    document.getElementById("navShow").hidden = isEdit
}

function addType(typeID, typeName, typeDescription, typeVersion, typeLink, typeMIMEInput, typeMIMEOutput) {
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
    http.onload = () => {
        location.reload()
    }
}