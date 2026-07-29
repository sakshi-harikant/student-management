# 🎓 Student Information Management System (SIMS)

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.0-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)

A simple and secure full-stack web application designed to manage student registration, authentication, and profile records.

---

## ✨ Key Features

* **Student Registration:** Registers students and automatically generates a unique Student ID (e.g., `EG-XXXXX`).
* **Secure Login:** Encrypts user passwords using **SHA-256** before storing them in the database.
* **Password Reset:** Allows students to recover their account using their Student ID and email address.
* **Admin Dashboard:** Enables administrators to view and search student profiles in real time.

---

## 🛠️ Tech Stack

* **Backend:** Java, Spring Boot (MVC)
* **Frontend:** Thymeleaf, HTML, Tailwind CSS
* **Database:** MySQL
* **ORM:** Spring Data JPA (Hibernate)

---

## 🚀 How to Run the Project

### 1. Prerequisites

* Java JDK 17 or later
* MySQL Server and MySQL Workbench
* Eclipse IDE or IntelliJ IDEA
* Maven

### 2. Database Setup

Open **MySQL Workbench** and execute the following SQL command:

```sql
CREATE DATABASE student_management;
```

### 3. Configure the Application

Navigate to `src/main/resources/application.properties` and update your MySQL credentials:

```properties
# Server Configuration
server.port=8080

# Database Connection Settings
spring.datasource.url=jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate Properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. Run the Application

1. Import the project into **Eclipse** or **IntelliJ IDEA** as an **Existing Maven Project**.
2. Wait for Maven to download all dependencies.
3. Run `StudentManagementApplication.java` as a **Java Application**.
4. Open your browser and visit:

```text
http://localhost:8080
```
