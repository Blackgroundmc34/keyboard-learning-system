# Keyboard Learning System Admin Dashboard

Dependency-free admin frontend served as static HTML, CSS, and JavaScript.

## Run locally

From the project root:

```bash
cd admin-dashboard
python3 -m http.server 5173
```

Then open [http://localhost:5173](http://localhost:5173). The backend must be running on `http://localhost:3000` for login requests.

In a separate terminal, start the backend:

```bash
cd backend
npm run build
npm start
```

After a successful administrator login, the browser opens `dashboard.html`. The dashboard uses a shared sidebar and global header and loads live platform totals, user roles, recent enrollments, and top courses from `GET /api/admin/dashboard`.

Only accounts with the `ADMIN` role can open the protected dashboard API. Development credentials are created locally and must not be committed to this repository.

Create development administrator credentials locally and keep them outside source control. Never add passwords, JWTs, or `.env` contents to this README.

## Security controls

- Admin API requests require a signed eight-hour JWT and an active database account whose current role is still `ADMIN`.
- Authentication endpoints are limited to 10 attempts per IP address every 15 minutes.
- CORS accepts only origins listed in `ADMIN_ORIGINS`; the local defaults are ports 5173 on `localhost` and `127.0.0.1`.
- API responses include restrictive security and no-cache headers, and JSON request bodies are limited to 100 KB.
- Dashboard pages use a Content Security Policy and keep the login token in `sessionStorage`, so closing the browser tab clears the session.
- Destructive actions require confirmation, database inputs use parameterized queries, and administrator changes are recorded in immutable audit logs.

## Reusable admin layout

`components/admin-layout.js` supplies the global sidebar, mobile menu behavior, active navigation state, and bottom logout action. `components/admin-topbar.js` independently supplies the reusable header, menu trigger, global search, live notification count, administrator profile, and keyboard shortcut. `components/admin-auth.js` supplies shared administrator session validation. New pages reuse these components through the layout's `page-title` and `active-page` attributes.

Update `components/admin-topbar.js` once to change the header across every admin page.

## Courses page

Open [http://localhost:5173/courses.html](http://localhost:5173/courses.html) after signing in. It loads real course records from the protected `GET /api/admin/courses` endpoint and supports text search plus status and level filters.

The global sidebar uses `assets/images/logo_admin.png` as its logo.

The Add New Course button opens a reusable form modal and saves validated course data through `POST /api/admin/courses`. View, Edit, publish/draft changes, instructor reassignment, and Delete actions are connected to the backend. Successful changes refresh the live table. Deletion displays a destructive-action confirmation because related course data is removed through database cascades.

Modal behavior is centralized in `components/admin-modal.js`, including forms, success confirmations, and destructive confirmations, so future admin pages use the same interaction and styling.

## Users page

Open [http://localhost:5173/users.html](http://localhost:5173/users.html) to manage real user accounts. Administrators can search and filter users, create student/instructor/admin accounts, update account details and roles, and activate or deactivate accounts.

New passwords are hashed with bcrypt before storage. Edit and Delete controls are connected to the backend. The API prevents duplicate emails and prevents the signed-in administrator from removing their own admin role, deactivating their own account, or deleting themselves. It also protects the last active administrator. Create, update, and delete actions are recorded in `audit_logs`.

## Lessons page

Open [http://localhost:5173/lessons.html](http://localhost:5173/lessons.html) to manage lessons across real courses. The page supports search plus course/status filters, and provides working View, Add, Edit, preview/publish, and Delete actions.

Lesson forms manage course assignment, title, slug, description, content, order, duration, preview access, and publication status. Slugs are unique within each course. Deleting a lesson requires confirmation and removes related materials, activities, submissions, and progress through the configured database relationships. All changes are recorded in `audit_logs`.

## Enrollments page

Open [http://localhost:5173/enrollments.html](http://localhost:5173/enrollments.html) to manage real course enrollments. The page includes live totals, search, course/status filters, progress visualization, and working View, Add, Edit, complete/cancel, and Delete actions.

Only active students can be enrolled, duplicate course enrollment is rejected, and completed enrollment requires 100% progress. Deleting an enrollment requires confirmation and removes associated lesson progress through the database relationship. All changes are recorded in `audit_logs`.

## Progress page

Open [http://localhost:5173/progress.html](http://localhost:5173/progress.html) to monitor real lesson progress. The page shows live totals, search, course and status filters, percentage bars, and the last-accessed and completion dates.

Administrators can inspect a record, correct its percentage from 0 to 100, or reset it after confirming the action. A correction automatically derives the lesson status and recalculates the related course enrollment from all published lessons. Progress changes are recorded in `audit_logs`.

## Learning Activities page

Open [http://localhost:5173/activities.html](http://localhost:5173/activities.html) to manage the real activities attached to lessons. The page provides live totals, search, course/type/requirement filters, submission counts, and working View, Add, Edit, and Delete actions.

Activity forms support practice, exercise, quiz, and assignment types, lesson assignment, ordering, optional maximum scores, required/optional state, and instructions. Deleting an activity requires confirmation and also removes related submissions through the database relationship. All changes are recorded in `audit_logs`.

## Announcements page

Open [http://localhost:5173/announcements.html](http://localhost:5173/announcements.html) to create platform-wide or course-specific announcements. The page includes live published/draft totals, audience and status filters, and working View, Add, Edit, publish/unpublish, and Delete actions.

Publishing sets the publication date automatically. Drafting clears it. Announcement changes are recorded in `audit_logs`.

## Messages page

Open [http://localhost:5173/messages.html](http://localhost:5173/messages.html) to review messages and create a direct conversation with any active platform user. Administrators can send text, image, audio, or file messages, inspect conversation details, edit active content, and delete messages.

Message deletion is a soft delete: its content and attachment are cleared while the conversation history remains. Message changes are recorded in `audit_logs`.

## Notifications page

Open [http://localhost:5173/notifications.html](http://localhost:5173/notifications.html) to send and manage real user notifications. The page includes live total/read/unread/recipient statistics, search and filters, and working View, Create, Mark Read/Unread, and Delete actions.

Notifications support a custom type, message, and optional validated JSON data. The global header badge shows the current unread count and links directly to this page. Notification changes are recorded in `audit_logs`.

## Reports page

Open [http://localhost:5173/reports.html](http://localhost:5173/reports.html) for live platform and course analytics. Reports include active users, enrollment completion rate, average progress, activity submissions, user roles, and per-course lesson, activity, enrollment, completion, and progress metrics.

Administrators can search and filter reports, inspect a detailed course summary, and export the complete course report as a dated CSV file.

## System Settings page

Open [http://localhost:5173/settings.html](http://localhost:5173/settings.html) to manage real application configuration without manually editing SQL. The page includes live totals, category grouping, search, and working View, Add, Edit, and Delete actions.

Setting keys use lowercase letters, numbers, dots, underscores, or hyphens. Values can hold text, numbers, booleans, or serialized JSON. Each update records the administrator and timestamp, and all changes are written to `audit_logs`.

## Admin Audit Logs page

Open [http://localhost:5173/audit-logs.html](http://localhost:5173/audit-logs.html) to review up to the 1,000 most recent administrator and system events. The page includes live totals, last-24-hour and security statistics, full-text search, and action, entity, and actor filters.

Administrators can inspect full event details and export the currently filtered history as a dated CSV file. Audit history is intentionally read-only and cannot be edited or deleted from the dashboard.

## Admin management pages

All sidebar destinations use specialized pages backed by protected admin API endpoints, including courses, users, lessons, enrollments, progress, learning activities, announcements, messages, notifications, reports, system settings, and audit logs. Empty database tables display a valid empty state instead of sample records.

The global menu button behaves responsively:

- On desktop, it hides or shows the entire sidebar and remembers the preference.
- On mobile, it opens the sidebar over the page and closes it through the backdrop.

## Administrator profile

Selecting the administrator avatar or name in the global header opens `profile.html`. The page loads the authenticated account from `GET /api/auth/me` and saves editable name, email, phone, and biography fields through `PATCH /api/auth/me`. The bottom sidebar control remains the dedicated sign-out action.
