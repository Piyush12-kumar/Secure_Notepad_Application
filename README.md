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
    git clone [https://github.com/https://github.com/Piyush12-kumar/Secure_Notepad_Application.git](https://github.com/Piyush12-kumar/Secure_Notepad_Application.git)
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd Secure_Notepad_Application
    ```
3.  **Configure the application:**
    - Create an `application.properties` file in `src/main/resources`.
    - Use `application.properties.example` as a template to add your database credentials, JWT secret key, and email settings.

4.  **Build and run the application:**
    ```bash
    mvn spring-boot:run
    ```

## API Endpoints

### Authentication
- `POST /api/auth/register` — Register a new user
- `POST /api/auth/login` — Login and receive JWT
- `POST /api/auth/logout` — Logout user
- `POST /api/auth/password/reset` — Request password reset
- `POST /api/auth/password/change` — Change password

### User Profile
- `GET /api/user/profile` — Get user profile
- `PUT /api/user/profile` — Update user profile

### Notes
- `POST /api/notes/create` — Create a new note
- `GET /api/notes/all` — Get all notes for the user
- `GET /api/notes/{id}` — Get a specific note
- `PUT /api/notes/{id}` — Update a note
- `DELETE /api/notes/delete/{id}` — Soft delete a note (move to Trash)
- `POST /api/notes/restore/{id}` — Restore a note from Trash
- `GET /api/notes/search` — Search notes by title or keyword

### Note Sharing
- `POST /api/notes/share/{noteId}` — Share a note with another user

### Attachments
- `POST /api/notes/{id}/attachments` — Add attachment to a note
- `GET /api/notes/{id}/attachments` — List attachments for a note
- `DELETE /api/attachments/{attachmentId}` — Remove an attachment

### Tags & Categories
- `POST /api/tags` — Create a tag
- `GET /api/tags` — List all tags
- `POST /api/categories` — Create a category
- `GET /api/categories` — List all categories
- `PUT /api/notes/{id}/tags` — Add or update tags for a note
- `PUT /api/notes/{id}/categories` — Add or update categories for a note
