# VEP-community-tool-API
Mapping service between neXtProt and EnsEMBL protein sequences.

## Setup

This is a spring boot based REST API built with maven.

## Starting the service

Install the dependencies
`mvn install`

Run the service on Linux
`./mvnw spring-boot:run`

## Endpoints
Please refer to the postman collection.

## Example Usage

1. VEP Results

POST /vep-results
payload
``
{
    "isoform" : "NX_P52701-1",
    "variants" : [
        {
            "position" : 2,
            "original-amino-acid" : "S",
            "variant-amino-acid" : "L"
        },
        {
            "position" : 96,
            "original-amino-acid" : "V",
            "variant-amino-acid" : "L"
        }
    ]`
}
``

2. Mapping isoform service
GET /mapping-isoforms/{entry}
