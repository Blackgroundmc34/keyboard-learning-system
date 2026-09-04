# Keyboard Learning System

A mobile learning platform built with Kotlin, Node.js, TypeScript, MySQL, and REST APIs for managing keyboard courses, lessons, student progress, and communication.

## Technology stack

- Android frontend: Kotlin
- Backend: Node.js, TypeScript, and Express
- Database: MySQL/MariaDB through XAMPP
- Authentication: JWT and bcryptjs

## Backend prerequisites

Install the following before running the backend:

- Node.js and npm
- XAMPP with MySQL/MariaDB
- The `keyboard_learning` database and project schema

## Backend setup

From the project root, install the backend dependencies:

```bash
cd backend
npm install
```

Create `backend/.env` and configure it for your local environment:

```dotenv
PORT=3000
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=
DB_NAME=keyboard_learning
JWT_SECRET=replace_with_at_least_32_random_characters
ADMIN_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Never commit `backend/.env`. It contains local credentials and secrets and is ignored by Git. You can verify this from the project root with:

```bash
git check-ignore -v backend/.env
git ls-files backend/.env
```

The first command should show the matching `.gitignore` rule. The second command should print nothing, confirming that the file is not tracked.

For production, use a dedicated database account with only the permissions the application needs instead of the MySQL root account. Generate a unique JWT secret of at least 32 characters and set `ADMIN_ORIGINS` to the exact HTTPS origin serving the dashboard.

## Create the database schema

[`backend/database/keyboard_learning.sql`](backend/database/keyboard_learning.sql) is the complete structure-only export for all 18 application tables. It contains no users, passwords, tokens, enrollments, messages, or other live records.

Start MySQL in XAMPP, then create the database and import the schema from the project root:

```bash
/Applications/XAMPP/xamppfiles/bin/mysql \
  --user=root \
  -e "CREATE DATABASE IF NOT EXISTS keyboard_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

/Applications/XAMPP/xamppfiles/bin/mysql \
  --user=root keyboard_learning < backend/database/keyboard_learning.sql
```

If the root database user requires a password, add `--password` and enter it when prompted.

The schema file contains `DROP TABLE IF EXISTS` statements so it can recreate tables in the correct foreign-key structure. Import it into a new or disposable local database. Importing it over a populated database deletes existing application data; back up important data first. Use `seed.sql` below when you only want to add the reusable development course without recreating the schema.

## Check TypeScript

Run the compiler before starting the API:

```bash
cd backend
npm run build
```

A successful build finishes without TypeScript errors and writes the compiled files to `backend/dist`.

## Run the backend locally

First, open the XAMPP Manager and start MySQL. Then run:

```bash
cd backend
npm run dev
```

Leave this terminal running. Successful startup displays:

```text
MySQL database connected successfully
API running on http://localhost:3000
```

Use `Ctrl+C` when you want to stop the server. Do not use `Ctrl+Z`, because that suspends the process instead of stopping it.

## Load development seed data

The repository includes [`backend/database/seed.sql`](backend/database/seed.sql) for testing the complete learning flow. It creates or updates:

- One published beginner course: **Keyboard Fundamentals**
- Three published lessons
- One text material for the first lesson

The script is idempotent: running it again updates the same records instead of creating duplicate courses or lessons. It does not delete existing data. From the project root, run:

```bash
/Applications/XAMPP/xamppfiles/bin/mysql \
  --user=root keyboard_learning < backend/database/seed.sql
```

If the local root user requires a password, add `--password` and enter it when prompted. This data is intended for local development; review the script before running it against any shared database.

After seeding, confirm the course and lessons through the API:

```bash
curl http://localhost:3000/api/courses
curl http://localhost:3000/api/courses/1/lessons
```

Do not rely on ID `1` outside a fresh database. Read the actual course ID from the first response and use it in later requests.

## Test the API

Keep the server running in the first terminal and open a second terminal for the following requests.

### Health check

```bash
curl -i http://localhost:3000/api/health
```

Expected status and response:

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8
```

```json
{
  "success": true,
  "message": "Keyboard Learning API is running"
}
```

### Register a student

```bash
curl -i -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "Student",
    "email": "student2@test.com",
    "password": "Password123!"
  }'
```

A successful registration returns `HTTP/1.1 201 Created`, the student's public details, and a JWT in the `token` field. The response must not contain the password or password hash.

Each student must have a unique email address. Reusing an existing email returns `HTTP/1.1 409 Conflict`; change the test email before trying again.

Example successful response (token shortened):

```json
{
  "success": true,
  "message": "Registration successful",
  "user": {
    "id": 1,
    "firstName": "Test",
    "lastName": "Student",
    "email": "student2@test.com",
    "role": "STUDENT"
  },
  "token": "eyJ..."
}
```

### Verify the registered student in MySQL

Open another terminal and connect to the local database. With the default XAMPP installation on macOS, run:

```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u root keyboard_learning
```

If the database user has a password, add `-p` and enter the password when prompted:

```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u root -p keyboard_learning
```

At the MySQL prompt, replace the email below with the address used for registration and run:

```sql
SELECT
  id,
  first_name,
  last_name,
  email,
  role,
  is_active,
  LEFT(password_hash, 4) AS hash_prefix,
  CHAR_LENGTH(password_hash) AS hash_length,
  password_hash = 'Password123!' AS is_plaintext
FROM users
WHERE email = 'student2@test.com'
LIMIT 1;
```

For a correctly stored bcrypt password, verify that:

- The student record is returned with the expected email and `STUDENT` role.
- `is_active` is `1`.
- `hash_prefix` is normally `$2a$` or `$2b$`.
- `hash_length` is `60`.
- `is_plaintext` is `0`.

Do not select, copy, log, or share the complete `password_hash`. Type `exit` to close the MySQL prompt.

### Log in and verify JWT generation

Use the same email and password that were used during registration:

```bash
curl -i -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student2@test.com",
    "password": "Password123!"
  }'
```

A successful login returns `HTTP/1.1 200 OK`, the student's public details, and a JWT in the `token` field. A JWT consists of three dot-separated segments:

```text
header.payload.signature
```

The server must verify the signature with `JWT_SECRET` before trusting the token. Decoding a token alone does not prove that it is authentic. Do not commit or publicly share real JWTs.

Expected response shape (token shortened):

```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 1,
    "firstName": "Test",
    "lastName": "Student",
    "email": "student2@test.com",
    "role": "STUDENT"
  },
  "token": "eyJ..."
}
```

Incorrect credentials should return `HTTP/1.1 401 Unauthorized`. An inactive account should return `HTTP/1.1 403 Forbidden`.

### Get the authenticated user's profile

`GET /api/auth/me` is protected by JWT authentication. First log in and copy the returned token into a temporary shell variable:

```bash
TOKEN='paste_the_login_token_here'
```

Then send the token using the Bearer authentication scheme:

```bash
curl -i http://localhost:3000/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

A valid token returns `HTTP/1.1 200 OK` and the current user's profile:

```json
{
  "success": true,
  "user": {
    "id": 1,
    "firstName": "Test",
    "lastName": "Student",
    "email": "student2@test.com",
    "role": "STUDENT",
    "phone": null,
    "profileImageUrl": null,
    "bio": null
  }
}
```

The response never includes `password_hash`. Missing, malformed, expired, or incorrectly signed tokens return `HTTP/1.1 401 Unauthorized`.

To confirm that the endpoint rejects requests without a token, run:

```bash
curl -i http://localhost:3000/api/auth/me
```

Expected response:

```json
{
  "success": false,
  "message": "Authentication token is required"
}
```

Clear the temporary token when finished:

```bash
unset TOKEN
```

## Courses API

The public Courses API returns only published courses. Its lesson count includes only published lessons.

### List courses

```bash
curl -i http://localhost:3000/api/courses
```

Expected response when no courses have been published:

```json
{
  "success": true,
  "courses": []
}
```

Each published course has this response shape:

```json
{
  "id": 1,
  "title": "Keyboard Fundamentals",
  "slug": "keyboard-fundamentals",
  "description": "Learn the fundamentals of keyboard playing.",
  "level": "BEGINNER",
  "thumbnailUrl": null,
  "estimatedDurationMinutes": 120,
  "lessonCount": 5,
  "createdAt": "2026-09-03T12:00:00.000Z",
  "updatedAt": "2026-09-03T12:00:00.000Z"
}
```

### Get one course

Replace `1` with an existing published course ID:

```bash
curl -i http://localhost:3000/api/courses/1
```

A valid published course returns `HTTP/1.1 200 OK`. An unpublished or nonexistent course returns `HTTP/1.1 404 Not Found`. A malformed or non-positive ID returns `HTTP/1.1 400 Bad Request`.

## Lessons API

The public Lessons API exposes only published lessons belonging to published courses. Lessons are returned in `lessonOrder`, then ID order.

### List a course's lessons

Replace `1` with an existing published course ID:

```bash
curl -i http://localhost:3000/api/courses/1/lessons
```

Example response:

```json
{
  "success": true,
  "course": {
    "id": 1,
    "title": "Keyboard Fundamentals"
  },
  "lessons": [
    {
      "id": 1,
      "courseId": 1,
      "title": "Introduction to the Keyboard",
      "slug": "introduction-to-the-keyboard",
      "description": "Learn the keyboard layout.",
      "content": "Lesson content",
      "lessonOrder": 1,
      "durationMinutes": 15,
      "isPreview": true
    }
  ]
}
```

An existing course with no published lessons returns an empty `lessons` array. A nonexistent or unpublished course returns `404 Not Found`, and an invalid course ID returns `400 Bad Request`.

### Get one lesson and its materials

Replace `1` with an existing published lesson ID:

```bash
curl -i http://localhost:3000/api/lessons/1
```

The response includes the lesson, its parent course summary, and its ordered materials. A material can contain text, a file URL, or both depending on its `type`.

```json
{
  "success": true,
  "lesson": {
    "id": 1,
    "courseId": 1,
    "title": "Introduction to the Keyboard",
    "lessonOrder": 1,
    "isPreview": true,
    "course": {
      "id": 1,
      "title": "Keyboard Fundamentals",
      "slug": "keyboard-fundamentals"
    },
    "materials": [
      {
        "id": 1,
        "type": "DOCUMENT",
        "title": "Keyboard layout guide",
        "fileUrl": "https://example.com/keyboard-layout.pdf",
        "order": 1,
        "isDownloadable": true
      }
    ]
  }
}
```

An unpublished or nonexistent lesson, or a lesson whose course is unpublished, returns `404 Not Found`. An invalid lesson ID returns `400 Bad Request`.

## Enrollments API

Enrollment endpoints require a valid JWT. Set the `TOKEN` variable using the token returned by login before running these examples.

### Enroll in a course

Replace `1` with an existing published course ID:

```bash
curl -i -X POST http://localhost:3000/api/courses/1/enroll \
  -H "Authorization: Bearer $TOKEN"
```

A successful enrollment returns `HTTP/1.1 201 Created`:

```json
{
  "success": true,
  "message": "Enrollment successful",
  "enrollment": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseTitle": "Keyboard Fundamentals",
    "status": "ACTIVE",
    "progressPercentage": 0
  }
}
```

The authenticated user cannot enroll twice in the same course. A duplicate request returns `409 Conflict`. A missing or unpublished course returns `404 Not Found`, and an invalid ID returns `400 Bad Request`.

### List my enrollments

```bash
curl -i http://localhost:3000/api/me/enrollments \
  -H "Authorization: Bearer $TOKEN"
```

The response contains only the authenticated user's enrollments and their course summaries:

```json
{
  "success": true,
  "enrollments": [
    {
      "id": 1,
      "status": "ACTIVE",
      "progressPercentage": 0,
      "enrolledAt": "2026-09-03T12:00:00.000Z",
      "completedAt": null,
      "course": {
        "id": 1,
        "title": "Keyboard Fundamentals",
        "slug": "keyboard-fundamentals",
        "level": "BEGINNER"
      }
    }
  ]
}
```

When the user has no enrollments, `enrollments` is an empty array. Missing or invalid authentication returns `401 Unauthorized`.

## Progress API

Progress endpoints require a valid JWT. A student must be enrolled in the lesson's course before recording progress.

### Update lesson progress

Replace `1` with a published lesson ID and provide a percentage from `0` to `100`:

```bash
curl -i -X POST http://localhost:3000/api/lessons/1/progress \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "progressPercentage": 50
  }'
```

Example response:

```json
{
  "success": true,
  "message": "Lesson progress updated",
  "progress": {
    "lessonId": 1,
    "lessonTitle": "Introduction to the Keyboard",
    "status": "IN_PROGRESS",
    "progressPercentage": 50,
    "courseProgressPercentage": 25
  }
}
```

Lesson status is calculated from the percentage:

- `0` becomes `NOT_STARTED`.
- A value greater than `0` and less than `100` becomes `IN_PROGRESS`.
- `100` becomes `COMPLETED`.

Progress cannot move backward. Course progress is recalculated as the average across every published lesson in the course. When all published lessons reach `100`, the enrollment becomes `COMPLETED`.

Invalid percentages or IDs return `400 Bad Request`. A missing or unpublished lesson returns `404 Not Found`. A student who is not enrolled receives `403 Forbidden`.

### Get my course and lesson progress

```bash
curl -i http://localhost:3000/api/me/progress \
  -H "Authorization: Bearer $TOKEN"
```

The result is grouped by enrollment and includes every published lesson. Lessons without a progress record are returned as `NOT_STARTED` with `0` percent.

```json
{
  "success": true,
  "progress": [
    {
      "enrollmentId": 1,
      "status": "ACTIVE",
      "progressPercentage": 25,
      "course": {
        "id": 1,
        "title": "Keyboard Fundamentals",
        "slug": "keyboard-fundamentals"
      },
      "lessons": [
        {
          "id": 1,
          "title": "Introduction to the Keyboard",
          "lessonOrder": 1,
          "status": "IN_PROGRESS",
          "progressPercentage": 50
        }
      ]
    }
  ]
}
```

When the student has no enrollments, `progress` is an empty array. Missing or invalid authentication returns `401 Unauthorized`.

### End-to-end development check

After loading the seed data, verify the complete student flow in this order:

1. Register or log in as a student and save the JWT in `TOKEN`.
2. Call `GET /api/courses` and note the seeded course ID.
3. Call `GET /api/courses/:id/lessons` and note each lesson ID.
4. Call `GET /api/lessons/:id` and confirm that the first lesson contains one material.
5. Call `POST /api/courses/:id/enroll` with the Bearer token.
6. Call `POST /api/lessons/:id/progress` for each lesson with `progressPercentage` set to `100`.
7. Call `GET /api/me/progress` and confirm that the course and all three lessons are `COMPLETED` with `100` percent progress.

Repeating enrollment for the same student and course returns `409 Conflict`, which is the expected duplicate-protection behavior.

## Verify the database tables

Connect to the database using the instructions above, then run:

```sql
SHOW TABLES;
```

The `keyboard_learning` database should contain these 18 tables:

- `activity_submissions`
- `announcements`
- `audit_logs`
- `conversation_members`
- `conversations`
- `course_instructors`
- `courses`
- `enrollments`
- `learning_activities`
- `lesson_materials`
- `lesson_progress`
- `lessons`
- `messages`
- `notifications`
- `password_reset_tokens`
- `refresh_tokens`
- `system_settings`
- `users`

You can also count them with:

```sql
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'keyboard_learning';
```

The expected `table_count` is `18`.

## Git workflow

Changes move through the following branches:

```text
feature/* -> develop -> staging -> main
```

Do not work directly on `main`.
