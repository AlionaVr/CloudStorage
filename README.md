# Cloud storage

A multi-module Spring Boot application for secure cloud file storage with JWT-based authentication.

## Technology Stack

- Java 17
- Spring Boot 3.5.4
- Spring Security: JWT-based authentication
- Build Tool: Maven
- Database: PostgreSQL (auth-service), MongoDB (file-service)
- Testing: JUnit 5, Mockito, Testcontainers
- The service is containerized using Docker.
- Lombok
- OpenFeign

## Shema:

https://miro.com/app/board/uXjVLKPM7aI=/

## Description
This service provides a fully implemented API based on the methods defined in
the [ original YAML specification](https://github.com/netology-code/jd-homeworks/blob/master/diploma/CloudServiceSpecification.yaml).

#### Auth Service (Port: 8081)

* User authentication and registration
* JWT token generation and validation
* PostgreSQL database for user data

#### File Service (Port: 8080)

* File upload/download operations
* MongoDB for file metadata storage
* Integration with auth-service via OpenFeign
* File size limits: 10MB per file/request

#### Security Library

* Shared JWT utilities and security configurations
* CORS configuration allowing http://localhost:8082
* Common security components for both services

# Running the Application

## Build and Run

* Clone the repository

```
clone https://github.com/AlionaVr/CloudStorage.git
cd CloudStorageDiploma 
```

* Build the project

``` 
mvn clean install
```

* Run services using Docker

```
docker compose up -d
```

* download frontend
  https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend
* Check .env
  ```VUE_APP_BASE_URL=http://localhost:8080/cloud```
* install nodeJS and run frontend

```
cd "*:\***\netology-diplom-frontend"
docker run -it --rm -v ${PWD}:/app -w /app -p 8082:8080 node:18-alpine sh -c "npm install --legacy-peer-deps && npm run serve -- --host 0.0.0.0"
```

App running at  http://localhost:8082/

#### DON'T FORGET to register your user

```
curl --location 'http://localhost:8080/cloud/register' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--data-raw '{
"login": "user@gmail.com",
"password": "user"
}' 
```

### Future improvements

1) Load more than 16MB files (need to consider other sources to store files OR use MongoGridFS OR store files partially
2) Implement sharing function for files
3) Implement expiration dates for files, so they are getting deleted automagically
4) Implement folders, so files can be stored in its own folders
5) Implement registration page
   ...