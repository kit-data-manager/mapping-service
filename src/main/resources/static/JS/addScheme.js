// const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration";
const apiUrl = "http://localhost:8095/api/v1/mappingAdministration";

let acl = new Map
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
    const urlParams = new URLSearchParams(window.location.search);
    if (!urlParams.has("edit")) {
        clearForm()
    }

    data = JSON.parse(window.sessionStorage.getItem("data"))
    console.log(data)

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
        if (data != null && data.record != null && data.schema != null && data.ETAG != null) {
            isEdit = true
            changeUIMode()
            document.getElementById("id").value = data.record.mappingId
            document.getElementById("type").value = data.record.mappingType
            for (let i = 0; i < data.record.acl.length; i++) {
                acl.set(data.record.acl[i].id, {
                    "id": data.record.acl[i].id,
                    "sid": data.record.acl[i].sid,
                    "permission": data.record.acl[i].permission
                })
                addACLElement(data.record.acl[i].id, data.record.acl[i].sid, data.record.acl[i].permission)
            }
            editor.set(data.schema)
            editor.expandAll()
        }
    }
}

function addACL() {
    const uid = document.getElementById("uid").value
    const sid = document.getElementById("sid").value
    const permission = document.getElementById("permission").value
    if (aclEdit != null) {
        deleteACL(aclEdit)
        aclEdit = null
    }
    if (acl.has(uid)) {
        deleteACL(uid)
    }
    acl.set(uid, {
        "id": uid,
        "sid": sid,
        "permission": permission
    })
    addACLElement(uid, sid, permission)
}

function addACLElement(uid, sid, permission) {
    const element =
        `<div class="border p-3 border rounded-3 m-3 col-auto" id="${uid}">
            <div class="row align-items-center col-auto mx-auto">
                <button type="button" class="btn btn-primary col-auto m-1" onclick="editACL('${uid}')">
                    <svg class="bi me-1" fill="currentColor" width="16" height="16">
                        <use xlink:href="#editButton"/>
                    </svg>
                    Edit
                </button>
                <button type="button" class="btn btn-outline-danger col-auto m-1" onclick="deleteACL('${uid}')">
                    <svg class="bi me-1" fill="currentColor" width="16" height="16">
                        <use xlink:href="#deleteButton"/>
                    </svg>
                    Delete ACL
                </button>
            </div>
            <div class="mb-3">
                <label for="uid" class="col-form-label">User ID</label>
                <input type="number" id="uidDisplay" class="form-control" disabled value="${uid}">
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

function editACL(id) {
    console.log(id)
    console.log(acl)
    console.log(acl.has(id))
    document.getElementById("uid").value = id
    document.getElementById("sid").value = acl.get(id).sid
    document.getElementById("permission").value = acl.get(id).permission
    document.getElementById("addACLButton").click()
    aclEdit = uid
}

function deleteACL(uid) {
    document.getElementById(uid).remove()
    acl.delete(uid)
}

function createMapping() {
    const id = document.getElementById("id").value
    const type = document.getElementById("type").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}") {
        alert("Please define a schema.")
        return
    }

    let resultACLs = []
    acl.forEach((value) => {
        resultACLs.push(value)
    })
    console.log(resultACLs)
    const record = {
        "mappingId": id,
        "mappingType": type,
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    const documentBlob = new Blob([editor.getText()], {type: "application/json"})

    console.log(record)
    console.log(recordBlob.text())
    console.log("RESULT: " + JSON.stringify(record))
    console.log(documentBlob)
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("POST", apiUrl)
    http.send(formData)
    http.onload = (e) => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        if (http.status <= 300) {
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
        alert("Timeout! Try later again.")
    }
    http.onerror = () => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        alert("Error (" + http.status + "-" + http.statusText + "! Try later again.")
    }
}

function updateMapping() {
    const id = document.getElementById("id").value
    const type = document.getElementById("type").value
    const file = document.getElementById("schema").files[0]
    if (editor.getText() === "{}") {
        alert("Please define a schema.")
        return
    }

    let resultACLs = []
    acl.forEach((value) => {
        resultACLs.push(value)
    })
    console.log(resultACLs)
    const record = {
        "mappingId": id,
        "mappingType": type,
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], {type: "application/json"});
    const documentBlob = new Blob([editor.getText()], {type: "application/json"})

    console.log(record)
    console.log(recordBlob.text())
    console.log("RESULT: " + JSON.stringify(record))
    console.log(documentBlob)
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", documentBlob)

    const http = new XMLHttpRequest();
    http.open("PUT", apiUrl + "/" + id + "/" + type)
    http.setRequestHeader("If-Match", data.ETAG)
    http.send(formData)
    http.onload = (e) => {
        console.log(http.responseText)
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
        alert("Timeout! Try later again.")
    }
    http.onerror = () => {
        console.log(http.responseText)
        document.getElementById("editProgress").hidden = true
        document.getElementById("update").disabled = false
        alert("Error (" + http.status + "-" + http.statusText + "! Try later again.")
    }
}

function clearForm() {
    console.log("Cancel editing! isEdit = " + isEdit)
    document.getElementById("form").reset()
    document.getElementById("successDisplay").hidden = true
    document.getElementById("editSuccessDisplay").hidden = true
    acl.forEach((value, key) => {
        deleteACL(key)
    })
    acl.clear()
    editor.set({})
    window.sessionStorage.clear()
    if (isEdit) {
        isEdit = false
        window.location = "showSchemes.html"
    }
    isEdit = false
    window.load()
}

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