Introduction
============

In this documentation, the basics of the KIT Data Manager RESTful API of
the Indexing Service are described. You will be guided through the first
steps of register an XML/JSON Mapping schema and update it.

This documentation assumes, that you have an instance of elasticsearch
up and running on localhost port 9200. If the repository is running on
another host or port you should change hostname and/or port accordingly.
Furthermore, the examples assume that you are using the repository
without authentication and authorization, which is provided by another
service. If you plan to use this optional service, please refer to its
documentation first to see how the examples in this documentation have
to be modified in order to work with authentication. Typically, this
should be achieved by simple adding an additional header entry.

The example structure is identical for all examples below. Each example
starts with a CURL command that can be run by copy&paste to your
console/terminal window. The second part shows the HTTP request sent to
the server including arguments and required headers. Finally, the third
block shows the response comming from the server. In between, special
characteristics of the calls are explained together with additional,
optional arguments or alternative responses.

**Note**

For technical reasons, all mapping records shown in the examples
contain all fields, e.g. also empty lists or fields with value 'null'.
You may ignore most of them as long as they are not needed. Some of
them will be assigned by the server, others remain empty or null as
long as you don’t assign any value to them. All fields mandatory at
creation time are explained in the resource creation example.

Mapping Registration
====================

Mapping Registration and Management
-----------------------------------

In this first section, the handling of mapping resources is explained.
It all starts with creating your first xml mapping resource. As Gemma
supports both (JSON and XML as source) it’s used here to show how it
works. It’s similar for other mapping technologies. The model of a
mapping schema record looks like this:

    {
      "mappingId" : "...",
      "mappingType" : "...",
      "acl" : [ {
        "id" : 1,
        "sid" : "...",
        "permission" : "..."
      } ],
      "mappingDocumentUri" : "...",
      "documentHash" : "..."
    }

At least the following elements are expected to be provided by the user:

-   mappingId: A unique label for the schema.

-   mappingType: The resource type must be assigned by the user. For XSD
    schemas this should be *application/xml*

In addition, ACL may be useful to make schema editable by others. (This
will be of interest while updating an existing schema)

### Registering an XML Mapping

The following example shows the creation of the first xsd schema only
providing mandatory fields mentioned above:

    record-xml.json:
    {
      "mappingId":"my_first_xsd",
      "mappingType":"GEMMA"
    }

    my_first_xsd4gemma.mapping:
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "metadata.publisher.#text",
       "type": "string"
       },
       "Publication Date":{
       "path": "metadata.publisher.@publicationDate",
       "type": "string"
       }
      }
    }

    $ curl 'http://localhost:8080/api/v1/mapping/' -i -X POST \
        -H 'Content-Type: multipart/form-data' \
        -F 'record=@record_xml.json;type=application/json' \
        -F 'document=@my_first_xsd4gemma.mapping;type=application/json'

You can see, that most of the sent mapping record is empty. Only
mappingId and mappingType are provided by the user. HTTP-wise the call
looks as follows:

    POST /api/v1/mapping/ HTTP/1.1
    Content-Type: multipart/form-data; boundary=6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Host: localhost:8080

    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=record; filename=record_xml.json
    Content-Type: application/json

    {"mappingId":"my_first_xsd","mappingType":"GEMMA","acl":[],"mappingDocumentUri":null,"documentHash":null}
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=document; filename=my_first_xsd4gemma.mapping
    Content-Type: application/json

    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "metadata.publisher.#text",
       "type": "string"
       },
       "Publication Date":{
       "path": "metadata.publisher.@publicationDate",
       "type": "string"
       }
      }
    }
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm--

As Content-Type only 'application/json' is supported and should be
provided. The other headers are typically set by the HTTP client. After
validating the provided document, adding missing information where
possible and persisting the created resource, the result is sent back to
the user and will look that way:

    HTTP/1.1 201 Created
    Location: http://localhost:8080/api/v1/mapping/my_first_xsd/GEMMA
    ETag: "966142371"
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    Content-Length: 302

    {
      "mappingId" : "my_first_xsd",
      "mappingType" : "GEMMA",
      "acl" : [ {
        "id" : 1,
        "sid" : "SELF",
        "permission" : "ADMINISTRATE"
      } ],
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_xsd/GEMMA",
      "documentHash" : "sha1:1c340fb431e6da4e8c3661191e5abbe2b96f840c"
    }

What you see is, that the mapping record looks different from the
original document. All remaining elements received a value by the
server. Furthermore, you’ll find an ETag header with the current ETag of
the resource. This value is returned by POST, GET and PUT calls and must
be provided for all calls modifying the resource, e.g. POST, PUT and
DELETE, in order to avoid conflicts.

### Registering an JSON Mapping

Now we register a mapping for JSON documents. This looks quite similar:

    record-json.json:
    {
      "mappingId":"my_first_json",
      "mappingType":"GEMMA"
    }

    my_first_json4gemma.mapping:
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "publisher",
       "type": "string"
       },
       "Publication Date":{
       "path": "publicationDate",
       "type": "string"
       }
      }
    }

    $ curl 'http://localhost:8080/api/v1/mapping/' -i -X POST \
        -H 'Content-Type: multipart/form-data' \
        -F 'record=@record_json.json;type=application/json' \
        -F 'document=@my_first_json4gemma.mapping;type=application/json'

You can see, that most of the sent mapping record is empty. Only
mappingId and mappingType are provided by the user. HTTP-wise the call
looks as follows:

    POST /api/v1/mapping/ HTTP/1.1
    Content-Type: multipart/form-data; boundary=6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Host: localhost:8080

    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=record; filename=record_json.json
    Content-Type: application/json

    {"mappingId":"my_first_json","mappingType":"GEMMA","acl":[],"mappingDocumentUri":null,"documentHash":null}
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=document; filename=my_first_json4gemma.mapping
    Content-Type: application/json

    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "publisher",
       "type": "string"
       },
       "Publication Date":{
       "path": "publicationDate",
       "type": "string"
       }
      }
    }
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm--

As Content-Type only 'application/json' is supported and should be
provided. The other headers are typically set by the HTTP client. After
validating the provided document, adding missing information where
possible and persisting the created resource, the result is sent back to
the user and will look that way:

    HTTP/1.1 201 Created
    Location: http://localhost:8080/api/v1/mapping/my_first_json/GEMMA
    ETag: "-210353687"
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    Content-Length: 304

    {
      "mappingId" : "my_first_json",
      "mappingType" : "GEMMA",
      "acl" : [ {
        "id" : 2,
        "sid" : "SELF",
        "permission" : "ADMINISTRATE"
      } ],
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_json/GEMMA",
      "documentHash" : "sha1:0240a32da4afdf43e1654901f992ccbccd74e430"
    }

What you see is, that the mapping record looks different from the
original document. All remaining elements received a value by the
server. Furthermore, you’ll find an ETag header with the current ETag of
the resource. This value is returned by POST, GET and PUT calls and must
be provided for all calls modifying the resource, e.g. POST, PUT and
DELETE, in order to avoid conflicts.

### Getting Mapping Record

**Note**

To access the mapping record you have to provide a special content
type ('application/vnd.datamanager.mapping-record+json').

Accessing the just ingested record do the following:

    $ curl 'http://localhost:8080/api/v1/mapping/my_first_json/GEMMA' -i -X GET \
        -H 'Accept: application/vnd.datamanager.mapping-record+json'

In the actual HTTP request there is nothing special. You just access the
path of the resource using the base path plus mappingId and mappingType.

    GET /api/v1/mapping/my_first_json/GEMMA HTTP/1.1
    Accept: application/vnd.datamanager.mapping-record+json
    Host: localhost:8080

As a result, you receive the mapping record shown already after your
post.

    HTTP/1.1 200 OK
    ETag: "-210353687"
    Content-Type: application/vnd.datamanager.mapping-record+json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    Content-Length: 304

    {
      "mappingId" : "my_first_json",
      "mappingType" : "GEMMA",
      "acl" : [ {
        "id" : 2,
        "sid" : "SELF",
        "permission" : "ADMINISTRATE"
      } ],
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_json/GEMMA",
      "documentHash" : "sha1:0240a32da4afdf43e1654901f992ccbccd74e430"
    }

### Getting a List of Mapping Records

Obtaining all accessible mapping records.

    $ curl 'http://localhost:8080/api/v1/mapping/' -i -X GET

In the actual HTTP request there is nothing special. You just access the
path of the resource using the base path.

    GET /api/v1/mapping/ HTTP/1.1
    Host: localhost:8080

As a result, you receive a list of mapping records.

    HTTP/1.1 200 OK
    Content-Range: 0-19/2
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    Content-Length: 612

    [ {
      "mappingId" : "my_first_xsd",
      "mappingType" : "GEMMA",
      "acl" : [ {
        "id" : 1,
        "sid" : "SELF",
        "permission" : "ADMINISTRATE"
      } ],
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_xsd/GEMMA",
      "documentHash" : "sha1:1c340fb431e6da4e8c3661191e5abbe2b96f840c"
    }, {
      "mappingId" : "my_first_json",
      "mappingType" : "GEMMA",
      "acl" : [ {
        "id" : 2,
        "sid" : "SELF",
        "permission" : "ADMINISTRATE"
      } ],
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_json/GEMMA",
      "documentHash" : "sha1:0240a32da4afdf43e1654901f992ccbccd74e430"
    } ]

**Note**

The header contains the field 'Content-Range" which displays delivered
indices and the maximum number of available schema records. If there
are more than 20 schemata registered you have to provide page and/or
size as additional query parameters.

-   page: Number of the page you want to get **(starting with page 0)**

-   size: Number of entries per page.

The modified HTTP request with pagination looks like follows:

    GET /api/v1/mapping/?page=0&size=20 HTTP/1.1
    Host: localhost:8080

### Getting Mapping File

Accessing the just ingested mapping file do the following:

    $ curl 'http://localhost:8080/api/v1/mapping/my_first_json/GEMMA' -i -X GET

In the actual HTTP request there is nothing special. You just access the
path of the resource using the base path plus mappingId and mappingType.

    GET /api/v1/mapping/my_first_json/GEMMA HTTP/1.1
    Host: localhost:8080

As a result, you receive the mapping record shown already after your
post.

    HTTP/1.1 200 OK
    Content-Length: 409
    Accept-Ranges: bytes
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY

    {
      "$schema" : "http://json-schema.org/draft-07/schema#",
      "$id" : "http://example.com/product.schema.json",
      "title" : "Simple Mapping",
      "description" : "Data resource mapping from json",
      "type" : "object",
      "properties" : {
        "Publisher" : {
          "path" : "publisher",
          "type" : "string"
        },
        "Publication Date" : {
          "path" : "publicationDate",
          "type" : "string"
        }
      }
    }

### Updating a Mapping File

**Warning**

This should be used with extreme caution. The new mapping should only
add optional elements otherwise otherwise it will break old mapping
records. Therefor it’s not accessible from remote.

For updating an existing mapping record/file a valid ETag is needed. The
actual ETag is available via the HTTP GET call of the mapping record.
(see above) Just send an HTTP POST with the updated mapping file and/or
mapping record (Only for changing ACL which is not supported right now
as this service should not be accessible by public).

    record-json.json
    {
      "mappingId":"my_first_json",
      "mappingType":"GEMMA"
    }

    my_first_json4gemma_v2.mapping:
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping Version 2",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "publisher",
       "type": "string"
       },
       "PublicationDate":{
       "path": "publicationDate",
       "type": "string"
       }
      }
    }

    $ curl 'http://localhost:8080/api/v1/mapping/my_first_json/GEMMA' -i -X PUT \
        -H 'Content-Type: multipart/form-data' \
        -H 'If-Match: "-210353687"' \
        -F 'record=@record_json.json;type=application/json' \
        -F 'document=@my_first_json4gemma_v2.mapping;type=application/json'

In the actual HTTP request there is nothing special. You just access the
path of the resource using the base path with mappingId and mappingType.

    PUT /api/v1/mapping/my_first_json/GEMMA HTTP/1.1
    Content-Type: multipart/form-data; boundary=6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    If-Match: "-210353687"
    Host: localhost:8080

    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=record; filename=record_json.json
    Content-Type: application/json

    {"mappingId":"my_first_json","mappingType":"GEMMA","acl":[],"mappingDocumentUri":null,"documentHash":null}
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm
    Content-Disposition: form-data; name=document; filename=my_first_json4gemma_v2.mapping
    Content-Type: application/json

    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "$id": "http://example.com/product.schema.json",
      "title": "Simple Mapping Version 2",
      "description": "Data resource mapping from json",
      "type": "object",
      "properties":{
      "Publisher":{
       "path": "publisher",
       "type": "string"
       },
       "PublicationDate":{
       "path": "publicationDate",
       "type": "string"
       }
      }
    }
    --6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm--

As a result, you receive the updated mapping record and in the HTTP
response header the (old) location URL and the new ETag.

    HTTP/1.1 200 OK
    Location: http://localhost:8080/api/v1/mapping/my_first_json/GEMMA
    ETag: "-143055406"
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    Content-Length: 215

    {
      "mappingId" : "my_first_json",
      "mappingType" : "GEMMA",
      "mappingDocumentUri" : "http://localhost:8080/api/v1/mapping/my_first_json/GEMMA",
      "documentHash" : "sha1:d70b187d04c55c751fc780f3f3328e78a8c30229"
    }

### Getting new Version of Mapping File

To get the new version of the mapping file just send an HTTP GET with
the linked 'mappingId' amd 'mappingType':

    $ curl 'http://localhost:8080/api/v1/mapping/my_first_json/GEMMA' -i -X GET

In the actual HTTP request there is nothing special. You just access the
path of the resource using the base path and the 'schemaId'.

    GET /api/v1/mapping/my_first_json/GEMMA HTTP/1.1
    Host: localhost:8080

As a result, you receive the XSD schema send before.

    HTTP/1.1 200 OK
    Content-Length: 418
    Accept-Ranges: bytes
    Content-Type: application/json
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY

    {
      "$schema" : "http://json-schema.org/draft-07/schema#",
      "$id" : "http://example.com/product.schema.json",
      "title" : "Simple Mapping Version 2",
      "description" : "Data resource mapping from json",
      "type" : "object",
      "properties" : {
        "Publisher" : {
          "path" : "publisher",
          "type" : "string"
        },
        "PublicationDate" : {
          "path" : "publicationDate",
          "type" : "string"
        }
      }
    }

Mapping Files and Ingest them to Elasticsearch
==============================================

The mapping will be done via messages. See separate documentation.
