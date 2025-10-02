# Notepad Application API

This is a full-featured REST API for a notepad application built with Java and Spring Boot.

## Features

- User Authentication (Register, Login, Logout) with JWT
- Secure Password Management (Password Reset, Change Password)
- Full CRUD Operations for Notes
- Note Sharing with Read-only/Read-write Permissions
- Soft Deletes with a "Trash" and Restore feature
- File Attachments for Notes
- Tagging and Categorization for Notes
- User Profile Management

## Technologies Used

- **Backend:** Java, Spring Boot
- **Security:** Spring Security, JWT
- **Database:** Spring Data JPA, MySQL (or your database)
- **Email:** Spring Mail

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd your-repo-name
    ```
3.  **Configure the application:**
    - Create an `application.properties` file in `src/main/resources`.
    - Use `application.properties.example` as a template to add your database credentials, JWT secret key, and email settings.

4.  **Build and run the application:**
    ```bash
    mvn spring-boot:run
    ```

## API Endpoints

The API includes endpoints for authentication, notes, sharing, and more. A full collection for tools like Postman can be provided.
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/notes/create`
- `GET /api/notes/all`
  ...and more.