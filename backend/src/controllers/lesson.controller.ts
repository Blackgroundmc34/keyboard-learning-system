import type { Request, Response } from "express";
import database = require("../config/database");

const { db } = database;

function parseId(value: string | string[] | undefined) {
  if (typeof value !== "string") {
    return null;
  }

  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function mapLesson(lesson: any) {
  return {
    id: lesson.id,
    courseId: lesson.course_id,
    title: lesson.title,
    slug: lesson.slug,
    description: lesson.description,
    content: lesson.content,
    lessonOrder: lesson.lesson_order,
    durationMinutes: lesson.duration_minutes,
    isPreview: Boolean(lesson.is_preview),
    createdAt: lesson.created_at,
    updatedAt: lesson.updated_at,
  };
}

async function getCourseLessons(req: Request, res: Response) {
  const courseId = parseId(req.params.id);

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

    const [lessons]: any = await db.query(
      `SELECT id, course_id, title, slug, description, content,
              lesson_order, duration_minutes, is_preview,
              created_at, updated_at
       FROM lessons
       WHERE course_id = ? AND is_published = 1
       ORDER BY lesson_order ASC, id ASC`,
      [courseId]
    );

    return res.status(200).json({
      success: true,
      course: {
        id: courses[0].id,
        title: courses[0].title,
      },
      lessons: lessons.map(mapLesson),
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

async function getLessonById(req: Request, res: Response) {
  const lessonId = parseId(req.params.id);

  if (!lessonId) {
    return res.status(400).json({
      success: false,
      message: "Invalid lesson ID",
    });
  }

  try {
    const [lessons]: any = await db.query(
      `SELECT l.id, l.course_id, l.title, l.slug, l.description,
              l.content, l.lesson_order, l.duration_minutes,
              l.is_preview, l.created_at, l.updated_at,
              c.title AS course_title, c.slug AS course_slug
       FROM lessons l
       INNER JOIN courses c ON c.id = l.course_id
       WHERE l.id = ? AND l.is_published = 1 AND c.is_published = 1
       LIMIT 1`,
      [lessonId]
    );

    if (lessons.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Lesson not found",
      });
    }

    const [materials]: any = await db.query(
      `SELECT id, material_type, title, description, file_url,
              text_content, mime_type, material_order,
              is_downloadable, created_at, updated_at
       FROM lesson_materials
       WHERE lesson_id = ?
       ORDER BY material_order ASC, id ASC`,
      [lessonId]
    );

    const lesson = lessons[0];

    return res.status(200).json({
      success: true,
      lesson: {
        ...mapLesson(lesson),
        course: {
          id: lesson.course_id,
          title: lesson.course_title,
          slug: lesson.course_slug,
        },
        materials: materials.map((material: any) => ({
          id: material.id,
          type: material.material_type,
          title: material.title,
          description: material.description,
          fileUrl: material.file_url,
          textContent: material.text_content,
          mimeType: material.mime_type,
          order: material.material_order,
          isDownloadable: Boolean(material.is_downloadable),
          createdAt: material.created_at,
          updatedAt: material.updated_at,
        })),
      },
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

export = { getCourseLessons, getLessonById };
