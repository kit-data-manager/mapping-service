const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration";
// const apiUrl = "http://localhost:8095/api/v1/mappingAdministration";
let acl = new Map
load()

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
    }
}

function addACL(){
    const uid = document.getElementById("uid").value
    const sid = document.getElementById("sid").value
    const permission = document.getElementById("permission").value
    if(acl.has(uid)){
        deleteACL(uid)
    }
    acl.set(uid, {
        "id": uid,
        "sid": sid,
        "permission": permission
    })

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

function editACL(uid){
    document.getElementById("uid").value = acl.get(uid).id
    document.getElementById("sid").value = acl.get(uid).sid
    document.getElementById("permission").value = acl.get(uid).permission
    document.getElementById("addACLButton").click()
}

function deleteACL(uid){
    document.getElementById(uid).remove()
    acl.delete(uid)
}

function createMapping(){
    const id =  document.getElementById("id").value
    const type =  document.getElementById("type").value
    const file =  document.getElementById("schema").files[0]
    if (file == null) {
        alert("Please select a file.")
        return
    }

    let resultACLs = []
    acl.forEach((value) =>{
        resultACLs.push(value)
    })
    console.log(resultACLs)
    const record = {
        "mappingId": id,
        "mappingType": type,
        "acl": resultACLs
    }
    const recordBlob = new Blob([JSON.stringify(record)], { type: "application/json"});

    console.log(record)
    console.log(recordBlob.text())
    console.log("RESULT: " + JSON.stringify(record))
    let formData = new FormData()
    formData.append("record", recordBlob)
    formData.append("document", file)

    const http = new XMLHttpRequest();
    http.open("POST", apiUrl)
    http.send(formData)
    http.onload = (e) => {
        console.log(http.responseText)
        document.getElementById("progress").hidden = true
        document.getElementById("submit").disabled = false
        if(http.status <=300) {
            document.getElementById("successDisplay").hidden = false
            setTimeout(() => {
                document.getElementById("successDisplay").hidden = true
                document.getElementById("form").reset()
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