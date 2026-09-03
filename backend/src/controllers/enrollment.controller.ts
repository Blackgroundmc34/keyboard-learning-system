import type { Request, Response } from "express";
import database = require("../config/database");

const { db } = database;

type AuthenticatedRequest = Request & {
  user?: { userId: number; role: string };
};

function getAuthenticatedUserId(req: Request) {
  return (req as AuthenticatedRequest).user?.userId;
}

function parseCourseId(value: string | string[] | undefined) {
  if (typeof value !== "string") {
    return null;
  }

  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

async function enrollInCourse(req: Request, res: Response) {
  const userId = getAuthenticatedUserId(req);
  const courseId = parseCourseId(req.params.id);

  if (!userId) {
    return res.status(401).json({
      success: false,
      message: "Authentication required",
    });
  }

  if (!courseId) {
    return res.status(400).json({
      success: false,
      message: "Invalid course ID",
    });
  }

  try {
    const [courses]: any = await db.query(
      "SELECT id, title FROM courses WHERE id = ? AND is_published = 1 LIMIT 1",
      [courseId]
    );

    if (courses.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Course not found",
      });
    }

    const [existingEnrollments]: any = await db.query(
      `SELECT id, status, progress_percentage, enrolled_at
       FROM enrollments
       WHERE user_id = ? AND course_id = ?
       LIMIT 1`,
      [userId, courseId]
    );

    if (existingEnrollments.length > 0) {
      return res.status(409).json({
        success: false,
        message: "Already enrolled in this course",
      });
    }

    const [result]: any = await db.query(
      `INSERT INTO enrollments (user_id, course_id, status)
       VALUES (?, ?, 'ACTIVE')`,
      [userId, courseId]
    );

    return res.status(201).json({
      success: true,
      message: "Enrollment successful",
      enrollment: {
        id: result.insertId,
        userId,
        courseId,
        courseTitle: courses[0].title,
        status: "ACTIVE",
        progressPercentage: 0,
      },
    });
  } catch (error: any) {
    if (error?.code === "ER_DUP_ENTRY") {
      return res.status(409).json({
        success: false,
        message: "Already enrolled in this course",
      });
    }

    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

async function getMyEnrollments(req: Request, res: Response) {
  const userId = getAuthenticatedUserId(req);

  if (!userId) {
    return res.status(401).json({
      success: false,
      message: "Authentication required",
    });
  }

  try {
    const [enrollments]: any = await db.query(
      `SELECT e.id, e.course_id, e.status, e.progress_percentage,
              e.enrolled_at, e.completed_at, e.updated_at,
              c.title, c.slug, c.description, c.level,
              c.thumbnail_url, c.estimated_duration_minutes
       FROM enrollments e
       INNER JOIN courses c ON c.id = e.course_id
       WHERE e.user_id = ?
       ORDER BY e.enrolled_at DESC, e.id DESC`,
      [userId]
    );

    return res.status(200).json({
      success: true,
      enrollments: enrollments.map((enrollment: any) => ({
        id: enrollment.id,
        status: enrollment.status,
        progressPercentage: Number(enrollment.progress_percentage),
        enrolledAt: enrollment.enrolled_at,
        completedAt: enrollment.completed_at,
        updatedAt: enrollment.updated_at,
        course: {
          id: enrollment.course_id,
          title: enrollment.title,
          slug: enrollment.slug,
          description: enrollment.description,
          level: enrollment.level,
          thumbnailUrl: enrollment.thumbnail_url,
          estimatedDurationMinutes: enrollment.estimated_duration_minutes,
        },
      })),
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

export = { enrollInCourse, getMyEnrollments };
