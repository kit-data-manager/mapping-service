// const apiUrl = location.protocol + "//" + location.host + "/api/v1/mappingAdministration/";
const apiUrl = "http://localhost:8095/api/v1/mappingAdministration/";

var records = new Map();
getRecords();

function getRecords() {
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl);
    http.send();
    http.onload=(e)=> {
        const results = JSON.parse(http.responseText);
        for (let i = 0; i < results.length; i++) {
            console.log(results[i].mappingId)
            const schemaHttp = new XMLHttpRequest()
            var schema
            var ETAG

            schemaHttp.open("GET", apiUrl + results[i].mappingId + "/" + results[i].mappingType)
            schemaHttp.setRequestHeader("Content-Type", "application/json")
            schemaHttp.send()
            schemaHttp.onload=(e)=> {
                schema = JSON.parse(schemaHttp.responseText)
                ETAG = '"' + schemaHttp.getResponseHeader("If-Match") + '"'
                addListElement(results[i].mappingId, results[i].mappingType, schema.title, schema.description)
                console.log({
                    "record": results[i],
                    "schema": schema,
                    "ETAG": ETAG
                })
                records.set(results[i].mappingId, {
                    "record": results[i],
                    "schema": schema,
                    "ETAG": ETAG
                })
                console.log(records)
            }
        }
    }
}

function viewMapping(id, type) {

}

function editMapping(id, type) {

}

function downloadMapping(id, type){
    const http = new XMLHttpRequest();
    http.open("GET", apiUrl + id + "/" + type);
    http.send();
    http.onload=(e)=> {
        const element = document.createElement('a');
        element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(http.responseText));
        element.setAttribute('download', id + "_schema.json");
        element.style.display = 'none';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
    }
}

function deleteMapping(id, type) {
    console.log("ID,TYPE: " + id + type)
    let mapEntry = records.get(id);
    console.log(mapEntry)
    if(mapEntry != null && mapEntry.record.mappingId === id && mapEntry.record.mappingType === type){
        const http = new XMLHttpRequest();
        http.open("DELETE", apiUrl + id + "/" + type)
        http.setRequestHeader("If-Match", mapEntry.ETAG)
        http.send();
        http.onload=(e)=> {
            console.log("successfully removed mapping")
            records.delete(id)
            location.reload()
        }
    }
}

function addListElement(id, type, title, description) {
    let element =
        `<li class="list-group-item">
            <div class="row align-items-center clearfix">
                <div class="me-auto float-start col-auto row align-items-center">
                    <div class="me-auto col-auto row align-items-center">
                        <h4 class="p-1 col-auto">${type}</h4>
                        <span class="col-auto p-1 text-muted">${id}</span>
                    </div>
                    <div class="me-auto col-auto row align-items-center">
                        <h4 class="p-1 col-auto">${title}</h4>
                        <span class="col-auto p-1 text-muted">${description}</span>
                    </div>
                </div>
    
                <div class="float-end col-auto ms-auto">
                    <button class="btn btn-primary col-auto m-1" onclick="viewMapping('${id}', '${type}')" disabled>
                        <svg class="bi me-1" fill="currentColor" width="16" height="16">
                            <use xlink:href="#viewButton"/>
                        </svg>
                        View
                    </button>
                    <button class="btn btn-primary col-auto m-1" onclick="editMapping('${id}', '${type}')" disabled>
                        <svg class="bi me-1" fill="currentColor" width="16" height="16">
                            <use xlink:href="#editButton"/>
                        </svg>
                        Edit
                    </button>
                    <a class="btn btn-primary col-auto m-1"
                       href="mapDocument.html/?id=${id}&type=${type}">
                        <svg class="bi me-1" fill="currentColor" width="16" height="16">
                            <use xlink:href="#mapButton"/>
                        </svg>
                        Map document
                    </a>
                    <button class="btn btn-primary col-auto m-1" onclick="downloadMapping('${id}', '${type}')">
                        <svg class="bi me-1" fill="currentColor" width="16" height="16">
                            <use xlink:href="#downloadButton"/>
                        </svg>
                        Download
                    </button>
                    <button class="btn btn-danger col-auto m-1" onclick="deleteMapping('${id}', '${type}')">
                        <svg class="bi me-1" fill="currentColor" width="16" height="16">
                            <use xlink:href="#deleteButton"/>
                        </svg>
                        Delete
                    </button>
                </div>
            </div>
        </li>`;

    let html = document.getElementById('list');
    console.log(html)
    html.innerHTML += element;
    console.log(html)
}