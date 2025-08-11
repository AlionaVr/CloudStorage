# Cloud storage

## Technology Stack

- Spring Boot,
- Maven
- The service is containerized using Docker.
- Docker Compose is used for orchestrating multi-container setups and simplifying local development.
- Unit tests are written using Mockito
- Integration tests are implemented with Testcontainers

## Shema:

https://miro.com/app/board/uXjVLKPM7aI=/

## Description

Service provides a REST interface for uploading files and displaying a list of the user's already uploaded files.

All requests to the service must be authorized. A pre-prepared web application (FRONT) connects to the service without
modification, and also uses the FRONT functionality to authorize, download, and list user files.

This service provides a fully implemented API based on the methods defined in
the [ original YAML specification](https://github.com/netology-code/jd-homeworks/blob/master/diploma/CloudServiceSpecification.yaml).
It offers robust file management capabilities along with secure user authentication.

### Implemented Features

- File Listing: Returns a list of all available files in the system.
- File Upload: Allows users to add new files to the service.
- File Deletion: Enables authorized users to remove files.
- User Authorization: Secure login mechanism using predefined credentials.

- ## Configuration
- All service settings are loaded from a dedicated YAML configuration file, ensuring flexibility and easy environment
  setup.

- ## Data Storage
- User Credentials: Login information for authorization is stored securely in a relational database.
- File Metadata: All file-related data is also managed through the database, enabling efficient access and scalability.


