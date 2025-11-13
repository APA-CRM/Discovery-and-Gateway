# Discovery-and-Gateway

## Overview

Discovery-and-Gateway is the centralized API gateway and service discovery component for the APA-CRM platform. It handles routing, authentication, authorization, and integration with multiple backend microservices such as authentication, main, file, and notification services. Built with **Java**, **Spring Boot**, and **Spring Cloud Gateway**, it orchestrates and secures API traffic between external clients and internal microservices.

## Features

- Centralized API routing and access management
- Authentication and authorization using custom filters
- Support for file access checks (organization-level validation)
- WebSocket support for notification service
- Service discovery through Eureka
- CI/CD workflow and best practices for microservice integration

## Technology Stack

- **Java**
- **Spring Boot**
- **Spring Cloud Gateway**
- **Eureka (Service Discovery)**
- **Docker** (deployment)
- **Maven** (build system)

## Directory Structure

- `api-gateway/` – Main gateway application (Java/Spring Boot)
- `eureka-service/` – Eureka Service Discovery (Java/Spring Boot)
- `.github/` – Workflow and CI/CD configurations

## Prerequisites

- Java 17+ (recommended)
- Maven 3.8+
- Docker (optional for containerization)
- Git

## Build Instructions

1. **Clone the Repository**
   ```sh
   git clone https://github.com/APA-CRM/Discovery-and-Gateway.git
   cd Discovery-and-Gateway
   ```

2. **Build with Maven**
   Compile and package all services:
   ```sh
   ./mvnw clean install
   ```

3. **Run Eureka Service**
   Start the service discovery component:
   ```sh
   cd eureka-service
   ../mvnw spring-boot:run
   ```

4. **Run API Gateway**
   Start the API Gateway (ensure Eureka is running first):
   ```sh
   cd ../api-gateway
   ../mvnw spring-boot:run
   ```

5. **(Optional) Docker Usage**
   Build and run Docker containers if `Dockerfile` is present:
   ```sh
   docker build -t discovery-gateway-api-gateway ./api-gateway
   docker run -p 8080:8080 discovery-gateway-api-gateway
   ```

## Configuration

- Environment variables and configuration files should be set for:
  - Service URLs (auth, main, file, notification)
  - Eureka server location
  - Security/authentication endpoints
- See `application.properties` or reference documentation in relevant service folders.

## Usage

- API traffic enters through the gateway, which routes requests based on configured paths and applies appropriate authentication/authorization filters.
- Standard endpoints: `/api/*`, `/ws-notifications/*` etc.
- Inter-service communication is managed via Eureka discovery.

## Contribution Guidelines

We welcome contributions! To get started:

### Development

- Fork this repository and clone your fork locally.
- Create a feature branch:
  ```sh
  git checkout -b feature/your-feature-name
  ```
- Write your code following [Google Java Styleguide](https://google.github.io/styleguide/javaguide.html) or project-specific conventions.
- Include meaningful commit messages and comments.

### Pull Requests

- Open a Pull Request (PR) against the `develop` branch.
- Fill out the PR template with context about your change.
- Ensure your PR passes CI/CD checks (see workflows in `.github/`).
- Respond to code review feedback promptly.

### Issues

- Use [GitHub Issues](https://github.com/APA-CRM/Discovery-and-Gateway/issues) for bugs or feature requests.
- Please include reproduction steps and environment details for bugs.

### Code of Conduct

- Be respectful and constructive in issues, PRs, and discussions.
- Follow the [Contributor Covenant](https://www.contributor-covenant.org/) where applicable.

## License

This project is licensed under the terms found in the [LICENSE](./LICENSE) file.

## Contact & Support

For help or questions, please open an issue or contact the maintainers via GitHub.

---

_For more documentation and architecture details, consult source code comments or ask the maintainers._
