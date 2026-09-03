import type { Request, Response } from "express";
import database = require("../config/database");

const { db } = database;

type AuthenticatedRequest = Request & {
  user?: { userId: number; role: string };
};

function getUserId(req: Request) {
  return (req as AuthenticatedRequest).user?.userId;
}

function parseId(value: string | string[] | undefined) {
  if (typeof value !== "string") {
    return null;
  }

  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function parsePercentage(value: unknown) {
  if (typeof value !== "number" || !Number.isFinite(value)) {
    return null;
  }

  return value >= 0 && value <= 100 ? Math.round(value * 100) / 100 : null;
}

function statusFor(percentage: number) {
  if (percentage === 100) return "COMPLETED";
  if (percentage > 0) return "IN_PROGRESS";
  return "NOT_STARTED";
}

async function updateLessonProgress(req: Request, res: Response) {
  const userId = getUserId(req);
  const lessonId = parseId(req.params.id);
  const requestedPercentage = parsePercentage(req.body?.progressPercentage);

  if (!userId) {
    return res.status(401).json({ success: false, message: "Authentication required" });
  }

  if (!lessonId) {
    return res.status(400).json({ success: false, message: "Invalid lesson ID" });
  }

  if (requestedPercentage === null) {
    return res.status(400).json({
      success: false,
      message: "progressPercentage must be a number between 0 and 100",
    });
  }

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [lessons]: any = await connection.query(
      `SELECT l.id, l.course_id, l.title
       FROM lessons l
       INNER JOIN courses c ON c.id = l.course_id
       WHERE l.id = ? AND l.is_published = 1 AND c.is_published = 1
       LIMIT 1`,
      [lessonId]
    );

    if (lessons.length === 0) {
      await connection.rollback();
      return res.status(404).json({ success: false, message: "Lesson not found" });
    }

    const lesson = lessons[0];
    const [enrollments]: any = await connection.query(
      `SELECT id FROM enrollments
       WHERE user_id = ? AND course_id = ? AND status != 'CANCELLED'
       LIMIT 1 FOR UPDATE`,
      [userId, lesson.course_id]
    );

    if (enrollments.length === 0) {
      await connection.rollback();
      return res.status(403).json({
        success: false,
        message: "Enroll in the course before updating lesson progress",
      });
    }

    const [existingProgress]: any = await connection.query(
      `SELECT progress_percentage FROM lesson_progress
       WHERE user_id = ? AND lesson_id = ?
       LIMIT 1 FOR UPDATE`,
      [userId, lessonId]
    );
    const previousPercentage = existingProgress.length
      ? Number(existingProgress[0].progress_percentage)
      : 0;
    const progressPercentage = Math.max(previousPercentage, requestedPercentage);
    const status = statusFor(progressPercentage);

    await connection.query(
      `INSERT INTO lesson_progress
         (user_id, lesson_id, status, progress_percentage,
          started_at, last_accessed_at, completed_at)
       VALUES (?, ?, ?, ?,
               IF(? > 0, NOW(), NULL), NOW(), IF(? = 100, NOW(), NULL))
       ON DUPLICATE KEY UPDATE
         status = VALUES(status),
         progress_percentage = VALUES(progress_percentage),
         started_at = IFNULL(started_at, VALUES(started_at)),
         last_accessed_at = NOW(),
         completed_at = IF(VALUES(progress_percentage) = 100,
                           IFNULL(completed_at, NOW()), completed_at)`,
      [userId, lessonId, status, progressPercentage, progressPercentage, progressPercentage]
    );

    const [courseProgressRows]: any = await connection.query(
      `SELECT COALESCE(AVG(COALESCE(lp.progress_percentage, 0)), 0) AS percentage
       FROM lessons l
       LEFT JOIN lesson_progress lp
         ON lp.lesson_id = l.id AND lp.user_id = ?
       WHERE l.course_id = ? AND l.is_published = 1`,
      [userId, lesson.course_id]
    );
    const courseProgress = Math.round(Number(courseProgressRows[0].percentage) * 100) / 100;
    const enrollmentStatus = courseProgress === 100 ? "COMPLETED" : "ACTIVE";

    await connection.query(
      `UPDATE enrollments
       SET progress_percentage = ?, status = ?,
           completed_at = IF(? = 'COMPLETED', IFNULL(completed_at, NOW()), NULL)
       WHERE id = ?`,
      [courseProgress, enrollmentStatus, enrollmentStatus, enrollments[0].id]
    );

    await connection.commit();

    return res.status(200).json({
      success: true,
      message: "Lesson progress updated",
      progress: {
        lessonId,
        lessonTitle: lesson.title,
        status,
        progressPercentage,
        courseProgressPercentage: courseProgress,
      },
    });
  } catch (error) {
    await connection.rollback();
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  } finally {
    connection.release();
  }
}

async function getMyProgress(req: Request, res: Response) {
  const userId = getUserId(req);

  if (!userId) {
    return res.status(401).json({ success: false, message: "Authentication required" });
  }

  try {
    const [rows]: any = await db.query(
      `SELECT e.id AS enrollment_id, e.status AS enrollment_status,
              e.progress_percentage AS course_progress, e.enrolled_at,
              c.id AS course_id, c.title AS course_title, c.slug AS course_slug,
              l.id AS lesson_id, l.title AS lesson_title,
              l.lesson_order, l.duration_minutes,
              COALESCE(lp.status, 'NOT_STARTED') AS lesson_status,
              COALESCE(lp.progress_percentage, 0) AS lesson_progress,
              lp.started_at, lp.last_accessed_at, lp.completed_at
       FROM enrollments e
       INNER JOIN courses c ON c.id = e.course_id
       LEFT JOIN lessons l ON l.course_id = c.id AND l.is_published = 1
       LEFT JOIN lesson_progress lp
         ON lp.lesson_id = l.id AND lp.user_id = e.user_id
       WHERE e.user_id = ?
       ORDER BY e.enrolled_at DESC, e.id DESC, l.lesson_order ASC, l.id ASC`,
      [userId]
    );

    const courses = new Map<number, any>();

    for (const row of rows) {
      if (!courses.has(row.enrollment_id)) {
        courses.set(row.enrollment_id, {
          enrollmentId: row.enrollment_id,
          status: row.enrollment_status,
          progressPercentage: Number(row.course_progress),
          enrolledAt: row.enrolled_at,
          course: {
            id: row.course_id,
            title: row.course_title,
            slug: row.course_slug,
          },
          lessons: [],
        });
      }

      if (row.lesson_id) {
        courses.get(row.enrollment_id).lessons.push({
          id: row.lesson_id,
          title: row.lesson_title,
          lessonOrder: row.lesson_order,
          durationMinutes: row.duration_minutes,
          status: row.lesson_status,
          progressPercentage: Number(row.lesson_progress),
          startedAt: row.started_at,
          lastAccessedAt: row.last_accessed_at,
          completedAt: row.completed_at,
        });
      }
    }

    return res.status(200).json({ success: true, progress: [...courses.values()] });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

export = { updateLessonProgress, getMyProgress };
