# User Registration and Authentication

## Project Description

This project implements **user registration and authentication** using **Spring Boot** for the backend and **Next.js** for the web frontend, along with a **React Native mobile app**. Users can register, log in, and access a protected profile page. Authentication is secured with **JWT tokens** and passwords are encrypted using **BCrypt**.

---

## Technologies Used

* **Backend**: Spring Boot, Spring Security, Java, MySQL, JWT, BCrypt
* **Web Frontend**: Next.js, React, TypeScript, Tailwind CSS, ShadCN UI
* **Mobile App**: Kotlin
* **Tools**: Any IDE or tools of your choice for development and testing

---

## Steps to Run Backend

1. Clone the repository:
   git clone <repo-url>
   cd backend

2. Configure MySQL database in `application.properties`:

   spring.datasource.url=jdbc:mysql://localhost:3306/lab1auth
   spring.datasource.username=root
   spring.datasource.password=your_password
   app.jwt.secret=your_256_bit_secret_key_here
   app.jwt.expirationInMs=3600000

3. Build and run the backend:
   ./mvnw clean install
   ./mvnw spring-boot:run

4. Backend will be running at: [http://localhost:8080](http://localhost:8080)

---

## Steps to Run Web App

1. Navigate to the web frontend folder:
   cd frontend

2. Install dependencies:
   npm install

3. Run the development server:
   npm run dev

4. Open your browser at: [http://localhost:3000](http://localhost:3000)

---

## Steps to Run Mobile App

1. Navigate to the mobile app folder:
   cd mobile

2. Install dependencies:
   npm install

3. Start the development server with Expo:
   npm start

---

## List of API Endpoints

| Method | Endpoint              | Description                       | Protected |
| ------ | --------------------- | --------------------------------- | --------- |
| POST   | `/api/users/register` | Register a new user               | No        |
| POST   | `/api/users/login`    | Log in with username and password | No        |
| GET    | `/api/users/me`       | Get logged-in user profile        | Yes       |
| GET    | `/api/users/{userId}` | Get user info by ID               | Yes       |
| GET    | `/api/users`          | List all users                    | Yes       |

---