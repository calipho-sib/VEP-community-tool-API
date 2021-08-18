# VEP-community-tool-API
Mapping service between neXtProt and EnsEMBL protein sequences.

## Setup

This is a spring boot based REST API built with maven.

## Starting the service

### Install the dependencies
`mvn install`

Run the service on Linux
`./mvnw -Dspring-boot.run.profiles=dev spring-boot:run`

### Package
`mvn package spring-boot:repackage -DskipTests -Dspring-boot.run.profiles=dev|prod`

### Start
With the packaged Jar file
`java -jar -Dspring.profiles.active=dev -Dserver.port=9000 vep-0.0.1-SNAPSHOT.jar`

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
