const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration";

let acl = []
const container = document.getElementById("jsoneditor")
const options = {
    mode: 'tree',
    modes: ['code', 'text', 'tree', 'view']
}
const editor = new JSONEditor(container, options)
let isEdit = false
let aclEdit
let data
load()

function loadFile() {
    const reader = new FileReader();
    reader.onload = (event) => {
        const obj = JSON.parse(event.target.result);
        console.log(obj)
        editor.set(obj)
        editor.expandAll()
    };
    reader.readAsText(document.getElementById("schema").files[0]);
}

function load() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + "/types")
    http.send();
    http.onload = (e) => {
        const result = JSON.parse(http.responseText)
        for (let i = 0; i < result.length; i++) {
            let option = document.createElement("option");
            option.value = result[i]
            option.text = result[i]
            document.getElementById("type").add(option)
        }
        data = JSON.parse(window.sessionStorage.getItem("data"))
        if (data != null && data.record != null && data.schema != null && data.ETAG != null) {
            console.log("Received data from session storage: " + data)
            isEdit = true
            changeUIMode()
            document.getElementById("id").value = data.record.mappingId
            document.getElementById("type").value = data.record.mappingType
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
    if (aclEdit != null) {
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
    document.getElementById("sid").value = acl[index].sid
    document.getElementById("permission").value = acl[index].permission
    document.getElementById("addACLButton").click()
    aclEdit = index
}

function deleteACL(index) {
    document.getElementById(index).remove()
    delete acl[index]
    console.log("Successfully deleted ACL with index of " + index)
}

function createMapping() {
    const id = document.getElementById("id").value
    const type = document.getElementById("type").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}") {
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
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    const documentBlob = new Blob([editor.getText()], {type: "application/json"})

    console.log("Sending record:" + JSON.stringify(record))
    console.log("Sending document:" + editor.getText())
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("POST", apiUrl)
    http.send(formData)
    http.onload = (e) => {
        console.log("Response: " + http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        if (http.status <= 300) {
            document.getElementById("errorMessage").hidden = true
            const element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(editor.getText()));
            if (file != null) element.setAttribute('download', file.name);
            else element.setAttribute('download', id + "_schema.json");
            element.style.display = 'none';
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
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
    const type = document.getElementById("type").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}") {
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
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    const documentBlob = new Blob([editor.getText()], {type: "application/json"})

    console.log("Sending record:" + JSON.stringify(record))
    console.log("Sending document:" + editor.getText())
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("PUT", apiUrl + "/" + id + "/" + type)
    http.setRequestHeader("If-Match", data.ETAG)
    http.send(formData)
    http.onload = (e) => {
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