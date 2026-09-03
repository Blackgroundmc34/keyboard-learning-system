import type { Request, Response } from "express";
import database = require("../config/database");

const { db } = database;

function mapCourse(course: any) {
  return {
    id: course.id,
    title: course.title,
    slug: course.slug,
    description: course.description,
    level: course.level,
    thumbnailUrl: course.thumbnail_url,
    estimatedDurationMinutes: course.estimated_duration_minutes,
    lessonCount: Number(course.lesson_count),
    createdAt: course.created_at,
    updatedAt: course.updated_at,
  };
}

async function getCourses(_req: Request, res: Response) {
  try {
    const [courses]: any = await db.query(
      `SELECT c.id, c.title, c.slug, c.description, c.level,
              c.thumbnail_url, c.estimated_duration_minutes,
              c.created_at, c.updated_at,
              COUNT(l.id) AS lesson_count
       FROM courses c
       LEFT JOIN lessons l
         ON l.course_id = c.id AND l.is_published = 1
       WHERE c.is_published = 1
       GROUP BY c.id
       ORDER BY c.created_at DESC, c.id DESC`
    );

    return res.status(200).json({
      success: true,
      courses: courses.map(mapCourse),
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

async function getCourseById(req: Request, res: Response) {
  const courseId = Number(req.params.id);

  if (!Number.isSafeInteger(courseId) || courseId <= 0) {
    return res.status(400).json({
      success: false,
      message: "Invalid course ID",
    });
  }

  try {
    const [courses]: any = await db.query(
      `SELECT c.id, c.title, c.slug, c.description, c.level,
              c.thumbnail_url, c.estimated_duration_minutes,
              c.created_at, c.updated_at,
              COUNT(l.id) AS lesson_count
       FROM courses c
       LEFT JOIN lessons l
         ON l.course_id = c.id AND l.is_published = 1
       WHERE c.id = ? AND c.is_published = 1
       GROUP BY c.id
       LIMIT 1`,
      [courseId]
    );

    if (courses.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Course not found",
      });
    }

    return res.status(200).json({
      success: true,
      course: mapCourse(courses[0]),
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

export = { getCourses, getCourseById };
