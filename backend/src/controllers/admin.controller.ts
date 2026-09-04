import type { Request, Response } from "express";
import bcrypt = require("bcryptjs");
import database = require("../config/database");

const { db } = database;

async function getDashboard(_req: Request, res: Response) {
  try {
    const [totalsResult, rolesResult, recentResult, coursesResult]: any = await Promise.all([
      db.query(`SELECT
        (SELECT COUNT(*) FROM users) AS users,
        (SELECT COUNT(*) FROM courses) AS courses,
        (SELECT COUNT(*) FROM lessons) AS lessons,
        (SELECT COUNT(*) FROM enrollments) AS enrollments`),
      db.query(`SELECT role, COUNT(*) AS total FROM users GROUP BY role`),
      db.query(`SELECT e.id, e.enrolled_at, e.status,
                       u.first_name, u.last_name,
                       c.id AS course_id, c.title AS course_title
                FROM enrollments e
                INNER JOIN users u ON u.id = e.user_id
                INNER JOIN courses c ON c.id = e.course_id
                ORDER BY e.enrolled_at DESC, e.id DESC
                LIMIT 5`),
      db.query(`SELECT c.id, c.title, COUNT(e.id) AS enrollment_count
                FROM courses c
                LEFT JOIN enrollments e ON e.course_id = c.id
                GROUP BY c.id
                ORDER BY enrollment_count DESC, c.title ASC
                LIMIT 5`),
    ]);

    const totals = totalsResult[0][0];
    const roles = { STUDENT: 0, INSTRUCTOR: 0, ADMIN: 0 };
    for (const row of rolesResult[0]) {
      if (row.role in roles) roles[row.role as keyof typeof roles] = Number(row.total);
    }

    return res.status(200).json({
      success: true,
      dashboard: {
        totals: {
          users: Number(totals.users),
          courses: Number(totals.courses),
          lessons: Number(totals.lessons),
          enrollments: Number(totals.enrollments),
        },
        usersByRole: {
          students: roles.STUDENT,
          instructors: roles.INSTRUCTOR,
          admins: roles.ADMIN,
        },
        recentEnrollments: recentResult[0].map((row: any) => ({
          id: row.id,
          studentName: `${row.first_name} ${row.last_name}`,
          courseId: row.course_id,
          courseTitle: row.course_title,
          status: row.status,
          enrolledAt: row.enrolled_at,
        })),
        topCourses: coursesResult[0].map((row: any) => ({
          id: row.id,
          title: row.title,
          enrollmentCount: Number(row.enrollment_count),
        })),
      },
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

async function getCourses(_req: Request, res: Response) {
  try {
    const [courses]: any = await db.query(
      `SELECT c.id, c.title, c.slug, c.description, c.level,
              c.thumbnail_url, c.estimated_duration_minutes,
              c.is_published, c.created_at, c.updated_at,
              COUNT(DISTINCT l.id) AS lesson_count,
              COUNT(DISTINCT e.id) AS enrollment_count,
              GROUP_CONCAT(DISTINCT CONCAT(u.first_name, ' ', u.last_name)
                           ORDER BY u.first_name, u.last_name SEPARATOR ', ') AS instructors,
              GROUP_CONCAT(DISTINCT u.id ORDER BY u.id SEPARATOR ',') AS instructor_ids
       FROM courses c
       LEFT JOIN lessons l ON l.course_id = c.id
       LEFT JOIN enrollments e ON e.course_id = c.id
       LEFT JOIN course_instructors ci ON ci.course_id = c.id
       LEFT JOIN users u ON u.id = ci.instructor_id
       GROUP BY c.id
       ORDER BY c.created_at DESC, c.id DESC`
    );

    const mappedCourses = courses.map((course: any) => ({
      id: course.id,
      title: course.title,
      slug: course.slug,
      description: course.description,
      level: course.level,
      thumbnailUrl: course.thumbnail_url,
      estimatedDurationMinutes: course.estimated_duration_minutes,
      status: course.is_published ? "PUBLISHED" : "DRAFT",
      lessonCount: Number(course.lesson_count),
      enrollmentCount: Number(course.enrollment_count),
      instructors: course.instructors ? course.instructors.split(", ") : [],
      instructorIds: course.instructor_ids ? course.instructor_ids.split(",").map(Number) : [],
      createdAt: course.created_at,
      updatedAt: course.updated_at,
    }));

    return res.status(200).json({
      success: true,
      summary: {
        total: mappedCourses.length,
        published: mappedCourses.filter((course: any) => course.status === "PUBLISHED").length,
        drafts: mappedCourses.filter((course: any) => course.status === "DRAFT").length,
      },
      courses: mappedCourses,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

async function createCourse(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const { title, slug, description, level, thumbnailUrl, estimatedDurationMinutes, isPublished, instructorId } = req.body;
  const allowedLevels = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "ALL_LEVELS"];
  const normalizedTitle = typeof title === "string" ? title.trim() : "";
  const normalizedSlug = typeof slug === "string" ? slug.trim().toLowerCase() : "";

  if (!adminId) return res.status(401).json({ success: false, message: "Authentication required" });
  if (!normalizedTitle || !normalizedSlug || !description?.trim()) {
    return res.status(400).json({ success: false, message: "Title, slug, and description are required" });
  }
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(normalizedSlug)) {
    return res.status(400).json({ success: false, message: "Slug must contain lowercase letters, numbers, and hyphens only" });
  }
  if (!allowedLevels.includes(level)) {
    return res.status(400).json({ success: false, message: "Select a valid course level" });
  }

  const duration = estimatedDurationMinutes === "" || estimatedDurationMinutes == null
    ? null : Number(estimatedDurationMinutes);
  if (duration !== null && (!Number.isSafeInteger(duration) || duration <= 0)) {
    return res.status(400).json({ success: false, message: "Duration must be a positive whole number of minutes" });
  }

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();
    if (instructorId) {
      const [instructors]: any = await connection.query(
        "SELECT id FROM users WHERE id = ? AND role = 'INSTRUCTOR' AND is_active = 1 LIMIT 1",
        [Number(instructorId)]
      );
      if (instructors.length === 0) {
        await connection.rollback();
        return res.status(400).json({ success: false, message: "Select a valid active instructor" });
      }
    }

    const [result]: any = await connection.query(
      `INSERT INTO courses
         (title, slug, description, level, thumbnail_url,
          estimated_duration_minutes, is_published, created_by)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [normalizedTitle, normalizedSlug, String(description).trim(), level,
       thumbnailUrl?.trim() || null, duration, isPublished ? 1 : 0, adminId]
    );

    if (instructorId) {
      await connection.query(
        "INSERT INTO course_instructors (course_id, instructor_id) VALUES (?, ?)",
        [result.insertId, Number(instructorId)]
      );
    }
    await connection.query(
      `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
       VALUES (?, 'CREATE', 'COURSE', ?, ?)`,
      [adminId, result.insertId, JSON.stringify({ title: normalizedTitle, slug: normalizedSlug })]
    );
    await connection.commit();

    return res.status(201).json({
      success: true,
      message: "Course created successfully",
      course: { id: result.insertId, title: normalizedTitle, slug: normalizedSlug },
    });
  } catch (error: any) {
    await connection.rollback();
    if (error?.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ success: false, message: "A course with this slug already exists" });
    }
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  } finally {
    connection.release();
  }
}

async function createUser(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const { firstName, lastName, email, password, role, phone, bio, isActive = true } = req.body;
  const roles = ["STUDENT", "INSTRUCTOR", "ADMIN"];
  const normalizedEmail = typeof email === "string" ? email.trim().toLowerCase() : "";

  if (!adminId) return res.status(401).json({ success: false, message: "Authentication required" });
  if (!firstName?.trim() || !lastName?.trim() || !normalizedEmail || !password) {
    return res.status(400).json({ success: false, message: "First name, last name, email, and password are required" });
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
    return res.status(400).json({ success: false, message: "Enter a valid email address" });
  }
  if (String(password).length < 8) {
    return res.status(400).json({ success: false, message: "Password must contain at least 8 characters" });
  }
  if (!roles.includes(role)) {
    return res.status(400).json({ success: false, message: "Select a valid user role" });
  }

  try {
    const passwordHash = await bcrypt.hash(String(password), 12);
    const [result]: any = await db.query(
      `INSERT INTO users
         (first_name, last_name, email, password_hash, role, phone, bio, is_active)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [String(firstName).trim(), String(lastName).trim(), normalizedEmail, passwordHash,
       role, phone?.trim() || null, bio?.trim() || null, isActive ? 1 : 0]
    );
    await db.query(
      `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
       VALUES (?, 'CREATE', 'USER', ?, ?)`,
      [adminId, result.insertId, JSON.stringify({ email: normalizedEmail, role })]
    );
    return res.status(201).json({
      success: true,
      message: "User created successfully",
      user: { id: result.insertId, firstName:String(firstName).trim(), lastName:String(lastName).trim(), email:normalizedEmail, role, isActive:Boolean(isActive) },
    });
  } catch (error: any) {
    if (error?.code === "ER_DUP_ENTRY") return res.status(409).json({ success: false, message: "Email address is already in use" });
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

async function updateUser(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const userId = typeof req.params.id === "string" ? Number(req.params.id) : NaN;
  const { firstName, lastName, email, role, phone, bio, isActive } = req.body;
  const roles = ["STUDENT", "INSTRUCTOR", "ADMIN"];
  const normalizedEmail = typeof email === "string" ? email.trim().toLowerCase() : "";

  if (!adminId) return res.status(401).json({ success: false, message: "Authentication required" });
  if (!Number.isSafeInteger(userId) || userId <= 0) return res.status(400).json({ success: false, message: "Invalid user ID" });
  if (!firstName?.trim() || !lastName?.trim() || !normalizedEmail || !roles.includes(role) || typeof isActive !== "boolean") {
    return res.status(400).json({ success: false, message: "Valid name, email, role, and account status are required" });
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) return res.status(400).json({ success: false, message: "Enter a valid email address" });
  if (userId === adminId && (role !== "ADMIN" || !isActive)) {
    return res.status(400).json({ success: false, message: "You cannot remove your own admin access or deactivate your account" });
  }

  try {
    const [result]: any = await db.query(
      `UPDATE users SET first_name=?, last_name=?, email=?, role=?, phone=?, bio=?, is_active=?
       WHERE id=?`,
      [String(firstName).trim(), String(lastName).trim(), normalizedEmail, role,
       phone?.trim() || null, bio?.trim() || null, isActive ? 1 : 0, userId]
    );
    if (result.affectedRows === 0) return res.status(404).json({ success: false, message: "User not found" });
    await db.query(
      `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
       VALUES (?, 'UPDATE', 'USER', ?, ?)`,
      [adminId, userId, JSON.stringify({ email: normalizedEmail, role, isActive })]
    );
    return res.status(200).json({ success: true, message: "User updated successfully" });
  } catch (error: any) {
    if (error?.code === "ER_DUP_ENTRY") return res.status(409).json({ success: false, message: "Email address is already in use" });
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

async function deleteUser(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const userId = typeof req.params.id === "string" ? Number(req.params.id) : NaN;
  if (!adminId) return res.status(401).json({ success: false, message: "Authentication required" });
  if (!Number.isSafeInteger(userId) || userId <= 0) return res.status(400).json({ success: false, message: "Invalid user ID" });
  if (userId === adminId) return res.status(400).json({ success: false, message: "You cannot delete your own account" });

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();
    const [users]: any = await connection.query("SELECT email, role FROM users WHERE id=? LIMIT 1 FOR UPDATE", [userId]);
    if (users.length === 0) {
      await connection.rollback();
      return res.status(404).json({ success: false, message: "User not found" });
    }
    if (users[0].role === "ADMIN") {
      const [counts]: any = await connection.query("SELECT COUNT(*) AS total FROM users WHERE role='ADMIN' AND is_active=1");
      if (Number(counts[0].total) <= 1) {
        await connection.rollback();
        return res.status(400).json({ success: false, message: "The last active administrator cannot be deleted" });
      }
    }
    await connection.query("DELETE FROM users WHERE id=?", [userId]);
    await connection.query(
      `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
       VALUES (?, 'DELETE', 'USER', ?, ?)`,
      [adminId, userId, JSON.stringify({ email: users[0].email, role: users[0].role })]
    );
    await connection.commit();
    return res.status(200).json({ success: true, message: "User deleted successfully" });
  } catch (error) {
    await connection.rollback();
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  } finally {
    connection.release();
  }
}

async function updateCourse(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const courseId = typeof req.params.id === "string" ? Number(req.params.id) : NaN;
  const { title, slug, description, level, thumbnailUrl, estimatedDurationMinutes, isPublished, instructorId } = req.body;
  const normalizedSlug = typeof slug === "string" ? slug.trim().toLowerCase() : "";
  if (!adminId) return res.status(401).json({ success: false, message: "Authentication required" });
  if (!Number.isSafeInteger(courseId) || courseId <= 0) return res.status(400).json({ success: false, message: "Invalid course ID" });
  if (!title?.trim() || !normalizedSlug || !description?.trim()) return res.status(400).json({ success: false, message: "Title, slug, and description are required" });
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(normalizedSlug)) return res.status(400).json({ success: false, message: "Enter a valid course slug" });
  if (!["BEGINNER","INTERMEDIATE","ADVANCED","ALL_LEVELS"].includes(level)) return res.status(400).json({ success: false, message: "Select a valid course level" });
  const duration = estimatedDurationMinutes === "" || estimatedDurationMinutes == null ? null : Number(estimatedDurationMinutes);
  if (duration !== null && (!Number.isSafeInteger(duration) || duration <= 0)) return res.status(400).json({ success: false, message: "Duration must be a positive whole number of minutes" });

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();
    if (instructorId) {
      const [instructors]: any = await connection.query("SELECT id FROM users WHERE id=? AND role='INSTRUCTOR' AND is_active=1 LIMIT 1", [Number(instructorId)]);
      if (!instructors.length) { await connection.rollback(); return res.status(400).json({ success:false, message:"Select a valid active instructor" }); }
    }
    const [result]: any = await connection.query(
      `UPDATE courses SET title=?, slug=?, description=?, level=?, thumbnail_url=?,
       estimated_duration_minutes=?, is_published=? WHERE id=?`,
      [String(title).trim(), normalizedSlug, String(description).trim(), level, thumbnailUrl?.trim()||null, duration, isPublished?1:0, courseId]
    );
    if (!result.affectedRows) { await connection.rollback(); return res.status(404).json({ success:false, message:"Course not found" }); }
    await connection.query("DELETE FROM course_instructors WHERE course_id=?", [courseId]);
    if (instructorId) await connection.query("INSERT INTO course_instructors (course_id,instructor_id) VALUES (?,?)", [courseId,Number(instructorId)]);
    await connection.query(`INSERT INTO audit_logs (user_id,action,entity_type,entity_id,details) VALUES (?,'UPDATE','COURSE',?,?)`, [adminId,courseId,JSON.stringify({title:String(title).trim(),slug:normalizedSlug,isPublished:Boolean(isPublished)})]);
    await connection.commit();
    return res.status(200).json({ success:true, message:"Course updated successfully" });
  } catch (error:any) {
    await connection.rollback();
    if (error?.code === "ER_DUP_ENTRY") return res.status(409).json({ success:false, message:"A course with this slug already exists" });
    console.error(error); return res.status(500).json({ success:false, message:"Server error" });
  } finally { connection.release(); }
}

async function deleteCourse(req: Request, res: Response) {
  const adminId = (req as Request & { user?: { userId: number } }).user?.userId;
  const courseId = typeof req.params.id === "string" ? Number(req.params.id) : NaN;
  if (!adminId) return res.status(401).json({ success:false, message:"Authentication required" });
  if (!Number.isSafeInteger(courseId) || courseId <= 0) return res.status(400).json({ success:false, message:"Invalid course ID" });
  try {
    const [courses]:any = await db.query("SELECT title,slug FROM courses WHERE id=? LIMIT 1", [courseId]);
    if (!courses.length) return res.status(404).json({ success:false, message:"Course not found" });
    await db.query("DELETE FROM courses WHERE id=?", [courseId]);
    await db.query(`INSERT INTO audit_logs (user_id,action,entity_type,entity_id,details) VALUES (?,'DELETE','COURSE',?,?)`, [adminId,courseId,JSON.stringify(courses[0])]);
    return res.status(200).json({ success:true, message:"Course deleted successfully" });
  } catch (error) { console.error(error); return res.status(500).json({ success:false, message:"Server error" }); }
}

async function getLessons(_req: Request, res: Response) {
  try {
    const [lessons]:any = await db.query(
      `SELECT l.id, l.course_id, c.title AS course_title, l.title, l.slug,
              l.description, l.content, l.lesson_order, l.duration_minutes,
              l.is_preview, l.is_published, l.created_at, l.updated_at,
              COUNT(DISTINCT lm.id) AS material_count,
              COUNT(DISTINCT la.id) AS activity_count
       FROM lessons l INNER JOIN courses c ON c.id=l.course_id
       LEFT JOIN lesson_materials lm ON lm.lesson_id=l.id
       LEFT JOIN learning_activities la ON la.lesson_id=l.id
       GROUP BY l.id ORDER BY c.title,l.lesson_order,l.id`
    );
    const records=lessons.map((lesson:any)=>({id:lesson.id,courseId:lesson.course_id,courseTitle:lesson.course_title,title:lesson.title,slug:lesson.slug,description:lesson.description,content:lesson.content,lessonOrder:lesson.lesson_order,durationMinutes:lesson.duration_minutes,isPreview:Boolean(lesson.is_preview),status:lesson.is_published?"PUBLISHED":"DRAFT",materialCount:Number(lesson.material_count),activityCount:Number(lesson.activity_count),createdAt:lesson.created_at,updatedAt:lesson.updated_at}));
    return res.status(200).json({success:true,summary:{total:records.length,published:records.filter((item:any)=>item.status==="PUBLISHED").length,drafts:records.filter((item:any)=>item.status==="DRAFT").length,previews:records.filter((item:any)=>item.isPreview).length},lessons:records});
  } catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"});}
}

function lessonInput(body:any){
  const courseId=Number(body.courseId), title=typeof body.title==="string"?body.title.trim():"", slug=typeof body.slug==="string"?body.slug.trim().toLowerCase():"", description=typeof body.description==="string"?body.description.trim():"", content=typeof body.content==="string"?body.content.trim():"";
  const lessonOrder=Number(body.lessonOrder),duration=body.durationMinutes===""||body.durationMinutes==null?null:Number(body.durationMinutes);
  if(!Number.isSafeInteger(courseId)||courseId<=0||!title||!slug||!description)return{error:"Course, title, slug, and description are required"};
  if(!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(slug))return{error:"Enter a valid lesson slug"};
  if(!Number.isSafeInteger(lessonOrder)||lessonOrder<=0)return{error:"Lesson order must be a positive whole number"};
  if(duration!==null&&(!Number.isSafeInteger(duration)||duration<=0))return{error:"Duration must be a positive whole number of minutes"};
  return{value:{courseId,title,slug,description,content:content||null,lessonOrder,duration,isPreview:Boolean(body.isPreview),isPublished:Boolean(body.isPublished)}};
}

async function createLesson(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=lessonInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;
  try{const[courses]:any=await db.query("SELECT id FROM courses WHERE id=? LIMIT 1",[value.courseId]);if(!courses.length)return res.status(404).json({success:false,message:"Course not found"});const[result]:any=await db.query(`INSERT INTO lessons(course_id,title,slug,description,content,lesson_order,duration_minutes,is_preview,is_published,created_by) VALUES(?,?,?,?,?,?,?,?,?,?)`,[value.courseId,value.title,value.slug,value.description,value.content,value.lessonOrder,value.duration,value.isPreview?1:0,value.isPublished?1:0,adminId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','LESSON',?,?)`,[adminId,result.insertId,JSON.stringify({title:value.title,courseId:value.courseId})]);return res.status(201).json({success:true,message:"Lesson created successfully",lesson:{id:result.insertId,title:value.title}})}catch(error:any){if(error?.code==="ER_DUP_ENTRY")return res.status(409).json({success:false,message:"This course already has a lesson with that slug"});console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function updateLesson(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,lessonId=typeof req.params.id==="string"?Number(req.params.id):NaN,input=lessonInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(lessonId)||lessonId<=0)return res.status(400).json({success:false,message:"Invalid lesson ID"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;
  try{const[courses]:any=await db.query("SELECT id FROM courses WHERE id=? LIMIT 1",[value.courseId]);if(!courses.length)return res.status(404).json({success:false,message:"Course not found"});const[result]:any=await db.query(`UPDATE lessons SET course_id=?,title=?,slug=?,description=?,content=?,lesson_order=?,duration_minutes=?,is_preview=?,is_published=? WHERE id=?`,[value.courseId,value.title,value.slug,value.description,value.content,value.lessonOrder,value.duration,value.isPreview?1:0,value.isPublished?1:0,lessonId]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Lesson not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','LESSON',?,?)`,[adminId,lessonId,JSON.stringify({title:value.title,courseId:value.courseId})]);return res.status(200).json({success:true,message:"Lesson updated successfully"})}catch(error:any){if(error?.code==="ER_DUP_ENTRY")return res.status(409).json({success:false,message:"This course already has a lesson with that slug"});console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function deleteLesson(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,lessonId=typeof req.params.id==="string"?Number(req.params.id):NaN;if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(lessonId)||lessonId<=0)return res.status(400).json({success:false,message:"Invalid lesson ID"});
  try{const[lessons]:any=await db.query("SELECT title,course_id AS courseId FROM lessons WHERE id=? LIMIT 1",[lessonId]);if(!lessons.length)return res.status(404).json({success:false,message:"Lesson not found"});await db.query("DELETE FROM lessons WHERE id=?",[lessonId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','LESSON',?,?)`,[adminId,lessonId,JSON.stringify(lessons[0])]);return res.status(200).json({success:true,message:"Lesson deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function getEnrollments(_req:Request,res:Response){
  try{const[rows]:any=await db.query(`SELECT e.id,e.user_id,e.course_id,CONCAT(u.first_name,' ',u.last_name) AS student,u.email,c.title AS course,e.status,e.progress_percentage,e.enrolled_at,e.completed_at,e.updated_at FROM enrollments e INNER JOIN users u ON u.id=e.user_id INNER JOIN courses c ON c.id=e.course_id ORDER BY e.enrolled_at DESC,e.id DESC`);const records=rows.map((row:any)=>({id:row.id,userId:row.user_id,courseId:row.course_id,student:row.student,email:row.email,course:row.course,status:row.status,progressPercentage:Number(row.progress_percentage),enrolledAt:row.enrolled_at,completedAt:row.completed_at,updatedAt:row.updated_at}));return res.status(200).json({success:true,summary:{total:records.length,active:records.filter((item:any)=>item.status==="ACTIVE").length,completed:records.filter((item:any)=>item.status==="COMPLETED").length,cancelled:records.filter((item:any)=>item.status==="CANCELLED").length},enrollments:records})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function createEnrollment(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,userId=Number(req.body.userId),courseId=Number(req.body.courseId);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(userId)||userId<=0||!Number.isSafeInteger(courseId)||courseId<=0)return res.status(400).json({success:false,message:"Select a valid student and course"});
  try{const[users]:any=await db.query("SELECT id FROM users WHERE id=? AND role='STUDENT' AND is_active=1 LIMIT 1",[userId]);if(!users.length)return res.status(400).json({success:false,message:"Select a valid active student"});const[courses]:any=await db.query("SELECT id FROM courses WHERE id=? LIMIT 1",[courseId]);if(!courses.length)return res.status(404).json({success:false,message:"Course not found"});const[result]:any=await db.query("INSERT INTO enrollments(user_id,course_id,status) VALUES(?,?,'ACTIVE')",[userId,courseId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','ENROLLMENT',?,?)`,[adminId,result.insertId,JSON.stringify({userId,courseId})]);return res.status(201).json({success:true,message:"Enrollment created successfully",enrollment:{id:result.insertId}})}catch(error:any){if(error?.code==="ER_DUP_ENTRY")return res.status(409).json({success:false,message:"This student is already enrolled in the course"});console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function updateEnrollment(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,enrollmentId=typeof req.params.id==="string"?Number(req.params.id):NaN,status=req.body.status,percentage=Number(req.body.progressPercentage);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(enrollmentId)||enrollmentId<=0)return res.status(400).json({success:false,message:"Invalid enrollment ID"});if(!["ACTIVE","COMPLETED","CANCELLED"].includes(status)||!Number.isFinite(percentage)||percentage<0||percentage>100)return res.status(400).json({success:false,message:"Select a valid status and progress percentage"});const progress=Math.round(percentage*100)/100;if(status==="COMPLETED"&&progress!==100)return res.status(400).json({success:false,message:"Completed enrollments must have 100% progress"});
  try{const[result]:any=await db.query(`UPDATE enrollments SET status=?,progress_percentage=?,completed_at=IF(?='COMPLETED',IFNULL(completed_at,NOW()),NULL) WHERE id=?`,[status,progress,status,enrollmentId]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Enrollment not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','ENROLLMENT',?,?)`,[adminId,enrollmentId,JSON.stringify({status,progressPercentage:progress})]);return res.status(200).json({success:true,message:"Enrollment updated successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function deleteEnrollment(req:Request,res:Response){
  const adminId=(req as Request&{user?:{userId:number}}).user?.userId,enrollmentId=typeof req.params.id==="string"?Number(req.params.id):NaN;if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(enrollmentId)||enrollmentId<=0)return res.status(400).json({success:false,message:"Invalid enrollment ID"});
  try{const[records]:any=await db.query("SELECT user_id AS userId,course_id AS courseId FROM enrollments WHERE id=? LIMIT 1",[enrollmentId]);if(!records.length)return res.status(404).json({success:false,message:"Enrollment not found"});await db.query("DELETE FROM enrollments WHERE id=?",[enrollmentId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','ENROLLMENT',?,?)`,[adminId,enrollmentId,JSON.stringify(records[0])]);return res.status(200).json({success:true,message:"Enrollment deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function getProgress(_req:Request,res:Response){
  try{const[rows]:any=await db.query(`SELECT lp.id,lp.user_id,lp.lesson_id,l.course_id,CONCAT(u.first_name,' ',u.last_name) AS student,u.email,l.title AS lesson,c.title AS course,lp.status,lp.progress_percentage,lp.started_at,lp.last_accessed_at,lp.completed_at,lp.updated_at FROM lesson_progress lp INNER JOIN users u ON u.id=lp.user_id INNER JOIN lessons l ON l.id=lp.lesson_id INNER JOIN courses c ON c.id=l.course_id ORDER BY lp.updated_at DESC,lp.id DESC`);const records=rows.map((row:any)=>({id:row.id,userId:row.user_id,lessonId:row.lesson_id,courseId:row.course_id,student:row.student,email:row.email,lesson:row.lesson,course:row.course,status:row.status,progressPercentage:Number(row.progress_percentage),startedAt:row.started_at,lastAccessedAt:row.last_accessed_at,completedAt:row.completed_at,updatedAt:row.updated_at}));return res.status(200).json({success:true,summary:{total:records.length,completed:records.filter((item:any)=>item.status==="COMPLETED").length,inProgress:records.filter((item:any)=>item.status==="IN_PROGRESS").length,notStarted:records.filter((item:any)=>item.status==="NOT_STARTED").length},progress:records})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

async function recalculateEnrollment(connection:any,userId:number,courseId:number){const[averages]:any=await connection.query(`SELECT COALESCE(AVG(COALESCE(lp.progress_percentage,0)),0) AS percentage FROM lessons l LEFT JOIN lesson_progress lp ON lp.lesson_id=l.id AND lp.user_id=? WHERE l.course_id=? AND l.is_published=1`,[userId,courseId]);const percentage=Math.round(Number(averages[0].percentage)*100)/100,status=percentage===100?"COMPLETED":"ACTIVE";await connection.query(`UPDATE enrollments SET progress_percentage=?,status=?,completed_at=IF(?='COMPLETED',IFNULL(completed_at,NOW()),NULL) WHERE user_id=? AND course_id=? AND status!='CANCELLED'`,[percentage,status,status,userId,courseId]);return percentage}

async function updateProgressAdmin(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,progressId=typeof req.params.id==="string"?Number(req.params.id):NaN,percentage=Number(req.body.progressPercentage);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(progressId)||progressId<=0)return res.status(400).json({success:false,message:"Invalid progress ID"});if(!Number.isFinite(percentage)||percentage<0||percentage>100)return res.status(400).json({success:false,message:"Progress must be between 0 and 100"});const value=Math.round(percentage*100)/100,status=value===100?"COMPLETED":value>0?"IN_PROGRESS":"NOT_STARTED",connection=await db.getConnection();try{await connection.beginTransaction();const[records]:any=await connection.query(`SELECT lp.user_id AS userId,l.course_id AS courseId FROM lesson_progress lp INNER JOIN lessons l ON l.id=lp.lesson_id WHERE lp.id=? LIMIT 1 FOR UPDATE`,[progressId]);if(!records.length){await connection.rollback();return res.status(404).json({success:false,message:"Progress record not found"})}await connection.query(`UPDATE lesson_progress SET progress_percentage=?,status=?,started_at=IF(? > 0,IFNULL(started_at,NOW()),NULL),last_accessed_at=NOW(),completed_at=IF(?=100,IFNULL(completed_at,NOW()),NULL) WHERE id=?`,[value,status,value,value,progressId]);const courseProgress=await recalculateEnrollment(connection,records[0].userId,records[0].courseId);await connection.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','LESSON_PROGRESS',?,?)`,[adminId,progressId,JSON.stringify({progressPercentage:value,status})]);await connection.commit();return res.status(200).json({success:true,message:"Progress updated successfully",courseProgressPercentage:courseProgress})}catch(error){await connection.rollback();console.error(error);return res.status(500).json({success:false,message:"Server error"})}finally{connection.release()}}

async function deleteProgressAdmin(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,progressId=typeof req.params.id==="string"?Number(req.params.id):NaN;if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(progressId)||progressId<=0)return res.status(400).json({success:false,message:"Invalid progress ID"});const connection=await db.getConnection();try{await connection.beginTransaction();const[records]:any=await connection.query(`SELECT lp.user_id AS userId,lp.lesson_id AS lessonId,l.course_id AS courseId FROM lesson_progress lp INNER JOIN lessons l ON l.id=lp.lesson_id WHERE lp.id=? LIMIT 1 FOR UPDATE`,[progressId]);if(!records.length){await connection.rollback();return res.status(404).json({success:false,message:"Progress record not found"})}await connection.query("DELETE FROM lesson_progress WHERE id=?",[progressId]);const courseProgress=await recalculateEnrollment(connection,records[0].userId,records[0].courseId);await connection.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','LESSON_PROGRESS',?,?)`,[adminId,progressId,JSON.stringify(records[0])]);await connection.commit();return res.status(200).json({success:true,message:"Progress record deleted successfully",courseProgressPercentage:courseProgress})}catch(error){await connection.rollback();console.error(error);return res.status(500).json({success:false,message:"Server error"})}finally{connection.release()}}

async function getActivities(_req:Request,res:Response){
  try{const[rows]:any=await db.query(`SELECT a.id,a.lesson_id,l.course_id,a.title,a.instructions,a.activity_type,a.max_score,a.activity_order,a.is_required,a.created_at,a.updated_at,l.title AS lesson,c.title AS course,COUNT(s.id) AS submission_count FROM learning_activities a INNER JOIN lessons l ON l.id=a.lesson_id INNER JOIN courses c ON c.id=l.course_id LEFT JOIN activity_submissions s ON s.activity_id=a.id GROUP BY a.id ORDER BY c.title,l.lesson_order,a.activity_order,a.id`);const activities=rows.map((row:any)=>({id:row.id,lessonId:row.lesson_id,courseId:row.course_id,title:row.title,instructions:row.instructions,activityType:row.activity_type,maxScore:row.max_score===null?null:Number(row.max_score),activityOrder:row.activity_order,isRequired:Boolean(row.is_required),lesson:row.lesson,course:row.course,submissionCount:Number(row.submission_count),createdAt:row.created_at,updatedAt:row.updated_at}));return res.status(200).json({success:true,summary:{total:activities.length,practice:activities.filter((item:any)=>item.activityType==="PRACTICE").length,assessments:activities.filter((item:any)=>["QUIZ","ASSIGNMENT"].includes(item.activityType)).length,required:activities.filter((item:any)=>item.isRequired).length},activities})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}
}

function activityInput(body:any){const lessonId=Number(body.lessonId),title=typeof body.title==="string"?body.title.trim():"",instructions=typeof body.instructions==="string"?body.instructions.trim():"",activityType=body.activityType,maxScore=body.maxScore===""||body.maxScore==null?null:Number(body.maxScore),activityOrder=Number(body.activityOrder);if(!Number.isSafeInteger(lessonId)||lessonId<=0||!title)return{error:"Lesson and title are required"};if(!["PRACTICE","EXERCISE","QUIZ","ASSIGNMENT"].includes(activityType))return{error:"Select a valid activity type"};if(!Number.isSafeInteger(activityOrder)||activityOrder<=0)return{error:"Activity order must be a positive whole number"};if(maxScore!==null&&(!Number.isFinite(maxScore)||maxScore<0||maxScore>999999.99))return{error:"Maximum score must be between 0 and 999999.99"};return{value:{lessonId,title,instructions:instructions||null,activityType,maxScore:maxScore===null?null:Math.round(maxScore*100)/100,activityOrder,isRequired:Boolean(body.isRequired)}}}

async function createActivity(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=activityInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{const[lessons]:any=await db.query("SELECT id FROM lessons WHERE id=? LIMIT 1",[value.lessonId]);if(!lessons.length)return res.status(404).json({success:false,message:"Lesson not found"});const[result]:any=await db.query(`INSERT INTO learning_activities(lesson_id,title,instructions,activity_type,max_score,activity_order,is_required) VALUES(?,?,?,?,?,?,?)`,[value.lessonId,value.title,value.instructions,value.activityType,value.maxScore,value.activityOrder,value.isRequired?1:0]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','LEARNING_ACTIVITY',?,?)`,[adminId,result.insertId,JSON.stringify({title:value.title,lessonId:value.lessonId,activityType:value.activityType})]);return res.status(201).json({success:true,message:"Learning activity created successfully",activity:{id:result.insertId,title:value.title}})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function updateActivity(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,activityId=typeof req.params.id==="string"?Number(req.params.id):NaN,input=activityInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(activityId)||activityId<=0)return res.status(400).json({success:false,message:"Invalid activity ID"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{const[lessons]:any=await db.query("SELECT id FROM lessons WHERE id=? LIMIT 1",[value.lessonId]);if(!lessons.length)return res.status(404).json({success:false,message:"Lesson not found"});const[result]:any=await db.query(`UPDATE learning_activities SET lesson_id=?,title=?,instructions=?,activity_type=?,max_score=?,activity_order=?,is_required=? WHERE id=?`,[value.lessonId,value.title,value.instructions,value.activityType,value.maxScore,value.activityOrder,value.isRequired?1:0,activityId]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Learning activity not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','LEARNING_ACTIVITY',?,?)`,[adminId,activityId,JSON.stringify({title:value.title,lessonId:value.lessonId,activityType:value.activityType})]);return res.status(200).json({success:true,message:"Learning activity updated successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function deleteActivity(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,activityId=typeof req.params.id==="string"?Number(req.params.id):NaN;if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(activityId)||activityId<=0)return res.status(400).json({success:false,message:"Invalid activity ID"});try{const[records]:any=await db.query("SELECT title,lesson_id AS lessonId FROM learning_activities WHERE id=? LIMIT 1",[activityId]);if(!records.length)return res.status(404).json({success:false,message:"Learning activity not found"});await db.query("DELETE FROM learning_activities WHERE id=?",[activityId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','LEARNING_ACTIVITY',?,?)`,[adminId,activityId,JSON.stringify(records[0])]);return res.status(200).json({success:true,message:"Learning activity deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getAnnouncements(_req:Request,res:Response){try{const[rows]:any=await db.query(`SELECT a.id,a.course_id,a.title,a.message,a.is_published,a.published_at,a.created_at,a.updated_at,COALESCE(c.title,'All courses') AS course,CONCAT(u.first_name,' ',u.last_name) AS created_by_name FROM announcements a LEFT JOIN courses c ON c.id=a.course_id INNER JOIN users u ON u.id=a.created_by ORDER BY a.created_at DESC,a.id DESC`);const announcements=rows.map((row:any)=>({id:row.id,courseId:row.course_id,title:row.title,message:row.message,isPublished:Boolean(row.is_published),publishedAt:row.published_at,createdAt:row.created_at,updatedAt:row.updated_at,course:row.course,createdBy:row.created_by_name}));return res.status(200).json({success:true,summary:{total:announcements.length,published:announcements.filter((item:any)=>item.isPublished).length,drafts:announcements.filter((item:any)=>!item.isPublished).length,global:announcements.filter((item:any)=>item.courseId===null).length},announcements})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
function announcementInput(body:any){const rawCourse=body.courseId,courseId=rawCourse===""||rawCourse==null?null:Number(rawCourse),title=typeof body.title==="string"?body.title.trim():"",message=typeof body.message==="string"?body.message.trim():"";if(courseId!==null&&(!Number.isSafeInteger(courseId)||courseId<=0))return{error:"Select a valid course"};if(!title||!message)return{error:"Title and message are required"};return{value:{courseId,title,message,isPublished:Boolean(body.isPublished)}}}
async function createAnnouncement(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=announcementInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{if(value.courseId){const[courses]:any=await db.query("SELECT id FROM courses WHERE id=? LIMIT 1",[value.courseId]);if(!courses.length)return res.status(404).json({success:false,message:"Course not found"})}const[result]:any=await db.query(`INSERT INTO announcements(course_id,created_by,title,message,is_published,published_at) VALUES(?,?,?,?,?,IF(?=1,NOW(),NULL))`,[value.courseId,adminId,value.title,value.message,value.isPublished?1:0,value.isPublished?1:0]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','ANNOUNCEMENT',?,?)`,[adminId,result.insertId,JSON.stringify({title:value.title,courseId:value.courseId,isPublished:value.isPublished})]);return res.status(201).json({success:true,message:"Announcement created successfully",announcement:{id:result.insertId,title:value.title}})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function updateAnnouncement(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id),input=announcementInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid announcement ID"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{if(value.courseId){const[courses]:any=await db.query("SELECT id FROM courses WHERE id=? LIMIT 1",[value.courseId]);if(!courses.length)return res.status(404).json({success:false,message:"Course not found"})}const[result]:any=await db.query(`UPDATE announcements SET course_id=?,title=?,message=?,is_published=?,published_at=IF(?=1,IFNULL(published_at,NOW()),NULL) WHERE id=?`,[value.courseId,value.title,value.message,value.isPublished?1:0,value.isPublished?1:0,id]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Announcement not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','ANNOUNCEMENT',?,?)`,[adminId,id,JSON.stringify({title:value.title,courseId:value.courseId,isPublished:value.isPublished})]);return res.status(200).json({success:true,message:"Announcement updated successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function deleteAnnouncement(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid announcement ID"});try{const[rows]:any=await db.query("SELECT title FROM announcements WHERE id=? LIMIT 1",[id]);if(!rows.length)return res.status(404).json({success:false,message:"Announcement not found"});await db.query("DELETE FROM announcements WHERE id=?",[id]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','ANNOUNCEMENT',?,?)`,[adminId,id,JSON.stringify(rows[0])]);return res.status(200).json({success:true,message:"Announcement deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getMessages(_req:Request,res:Response){try{const[rows]:any=await db.query(`SELECT m.id,m.conversation_id,m.sender_id,m.message_type,m.message_text,m.attachment_url,m.is_deleted,m.created_at,m.updated_at,cv.conversation_type,cv.title AS conversation_title,CONCAT(u.first_name,' ',u.last_name) AS sender,(SELECT GROUP_CONCAT(CONCAT(mu.first_name,' ',mu.last_name) ORDER BY mu.first_name SEPARATOR ', ') FROM conversation_members cm INNER JOIN users mu ON mu.id=cm.user_id WHERE cm.conversation_id=m.conversation_id AND cm.user_id<>m.sender_id) AS recipients FROM messages m INNER JOIN conversations cv ON cv.id=m.conversation_id INNER JOIN users u ON u.id=m.sender_id ORDER BY m.created_at DESC,m.id DESC`);const messages=rows.map((row:any)=>({id:row.id,conversationId:row.conversation_id,senderId:row.sender_id,messageType:row.message_type,messageText:row.message_text,attachmentUrl:row.attachment_url,isDeleted:Boolean(row.is_deleted),createdAt:row.created_at,updatedAt:row.updated_at,conversationType:row.conversation_type,conversationTitle:row.conversation_title,sender:row.sender,recipients:row.recipients||"No other members"}));return res.status(200).json({success:true,summary:{total:messages.length,text:messages.filter((item:any)=>item.messageType==="TEXT").length,attachments:messages.filter((item:any)=>item.messageType!=="TEXT").length,deleted:messages.filter((item:any)=>item.isDeleted).length},messages})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
function messageInput(body:any){const recipientId=Number(body.recipientId),messageType=body.messageType,text=typeof body.messageText==="string"?body.messageText.trim():"",attachmentUrl=typeof body.attachmentUrl==="string"?body.attachmentUrl.trim():"",title=typeof body.conversationTitle==="string"?body.conversationTitle.trim():"";if(!Number.isSafeInteger(recipientId)||recipientId<=0)return{error:"Select a recipient"};if(!["TEXT","IMAGE","AUDIO","FILE"].includes(messageType))return{error:"Select a valid message type"};if(messageType==="TEXT"&&!text)return{error:"Message text is required"};if(messageType!=="TEXT"&&!attachmentUrl)return{error:"Attachment URL is required"};return{value:{recipientId,messageType,text:text||null,attachmentUrl:attachmentUrl||null,title:title||null}}}
async function createMessage(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=messageInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;if(value.recipientId===adminId)return res.status(400).json({success:false,message:"Select another user as recipient"});const connection=await db.getConnection();try{await connection.beginTransaction();const[users]:any=await connection.query("SELECT id FROM users WHERE id=? AND is_active=1 LIMIT 1",[value.recipientId]);if(!users.length){await connection.rollback();return res.status(404).json({success:false,message:"Recipient not found"})}const[conversation]:any=await connection.query("INSERT INTO conversations(conversation_type,title,created_by) VALUES('DIRECT',?,?)",[value.title,adminId]);await connection.query("INSERT INTO conversation_members(conversation_id,user_id) VALUES(?,?),(?,?)",[conversation.insertId,adminId,conversation.insertId,value.recipientId]);const[result]:any=await connection.query("INSERT INTO messages(conversation_id,sender_id,message_type,message_text,attachment_url) VALUES(?,?,?,?,?)",[conversation.insertId,adminId,value.messageType,value.text,value.attachmentUrl]);await connection.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','MESSAGE',?,?)`,[adminId,result.insertId,JSON.stringify({recipientId:value.recipientId,messageType:value.messageType})]);await connection.commit();return res.status(201).json({success:true,message:"Message sent successfully",messageRecord:{id:result.insertId}})}catch(error){await connection.rollback();console.error(error);return res.status(500).json({success:false,message:"Server error"})}finally{connection.release()}}
async function updateMessage(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id),text=typeof req.body.messageText==="string"?req.body.messageText.trim():"",attachmentUrl=typeof req.body.attachmentUrl==="string"?req.body.attachmentUrl.trim():"";if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid message ID"});try{const[rows]:any=await db.query("SELECT message_type AS messageType FROM messages WHERE id=? AND is_deleted=0 LIMIT 1",[id]);if(!rows.length)return res.status(404).json({success:false,message:"Message not found"});if(rows[0].messageType==="TEXT"&&!text)return res.status(400).json({success:false,message:"Message text is required"});if(rows[0].messageType!=="TEXT"&&!attachmentUrl)return res.status(400).json({success:false,message:"Attachment URL is required"});await db.query("UPDATE messages SET message_text=?,attachment_url=? WHERE id=?",[text||null,attachmentUrl||null,id]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','MESSAGE',?,?)`,[adminId,id,JSON.stringify({messageType:rows[0].messageType})]);return res.status(200).json({success:true,message:"Message updated successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function deleteMessage(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid message ID"});try{const[result]:any=await db.query("UPDATE messages SET is_deleted=1,message_text=NULL,attachment_url=NULL WHERE id=? AND is_deleted=0",[id]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Message not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id) VALUES(?,'DELETE','MESSAGE',?)`,[adminId,id]);return res.status(200).json({success:true,message:"Message deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getNotifications(_req:Request,res:Response){try{const[rows]:any=await db.query(`SELECT n.id,n.user_id,n.notification_type,n.title,n.message,n.data_json,n.is_read,n.read_at,n.created_at,CONCAT(u.first_name,' ',u.last_name) AS recipient,u.email FROM notifications n INNER JOIN users u ON u.id=n.user_id ORDER BY n.created_at DESC,n.id DESC`);const notifications=rows.map((row:any)=>({id:row.id,userId:row.user_id,notificationType:row.notification_type,title:row.title,message:row.message,dataJson:row.data_json,isRead:Boolean(row.is_read),readAt:row.read_at,createdAt:row.created_at,recipient:row.recipient,email:row.email}));return res.status(200).json({success:true,summary:{total:notifications.length,unread:notifications.filter((item:any)=>!item.isRead).length,read:notifications.filter((item:any)=>item.isRead).length,recipients:new Set(notifications.map((item:any)=>item.userId)).size},notifications})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
function notificationInput(body:any){const userId=Number(body.userId),type=typeof body.notificationType==="string"?body.notificationType.trim().toUpperCase():"",title=typeof body.title==="string"?body.title.trim():"",message=typeof body.message==="string"?body.message.trim():"",dataJson=typeof body.dataJson==="string"?body.dataJson.trim():"";if(!Number.isSafeInteger(userId)||userId<=0||!type||!title)return{error:"Recipient, type, and title are required"};if(type.length>50||title.length>200)return{error:"Notification type or title is too long"};if(dataJson){try{JSON.parse(dataJson)}catch{return{error:"Extra data must be valid JSON"}}}return{value:{userId,type,title,message:message||null,dataJson:dataJson||null}}}
async function createNotification(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=notificationInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{const[users]:any=await db.query("SELECT id FROM users WHERE id=? AND is_active=1 LIMIT 1",[value.userId]);if(!users.length)return res.status(404).json({success:false,message:"Active recipient not found"});const[result]:any=await db.query("INSERT INTO notifications(user_id,notification_type,title,message,data_json) VALUES(?,?,?,?,?)",[value.userId,value.type,value.title,value.message,value.dataJson]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','NOTIFICATION',?,?)`,[adminId,result.insertId,JSON.stringify({userId:value.userId,type:value.type,title:value.title})]);return res.status(201).json({success:true,message:"Notification created successfully",notification:{id:result.insertId}})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function updateNotification(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id),isRead=req.body.isRead;if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid notification ID"});if(typeof isRead!=="boolean")return res.status(400).json({success:false,message:"Read status must be true or false"});try{const[result]:any=await db.query("UPDATE notifications SET is_read=?,read_at=IF(?=1,IFNULL(read_at,NOW()),NULL) WHERE id=?",[isRead?1:0,isRead?1:0,id]);if(!result.affectedRows)return res.status(404).json({success:false,message:"Notification not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','NOTIFICATION',?,?)`,[adminId,id,JSON.stringify({isRead})]);return res.status(200).json({success:true,message:`Notification marked ${isRead?"read":"unread"}`})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function deleteNotification(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid notification ID"});try{const[rows]:any=await db.query("SELECT title,user_id AS userId FROM notifications WHERE id=? LIMIT 1",[id]);if(!rows.length)return res.status(404).json({success:false,message:"Notification not found"});await db.query("DELETE FROM notifications WHERE id=?",[id]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','NOTIFICATION',?,?)`,[adminId,id,JSON.stringify(rows[0])]);return res.status(200).json({success:true,message:"Notification deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getReports(_req:Request,res:Response){try{const[[summaryRows],[courseRows],[roleRows]]:any=await Promise.all([db.query(`SELECT (SELECT COUNT(*) FROM users WHERE is_active=1) AS activeUsers,(SELECT COUNT(*) FROM courses) AS totalCourses,(SELECT COUNT(*) FROM enrollments) AS totalEnrollments,(SELECT COUNT(*) FROM enrollments WHERE status='COMPLETED') AS completedEnrollments,(SELECT ROUND(COALESCE(AVG(progress_percentage),0),2) FROM enrollments) AS averageProgress,(SELECT COUNT(*) FROM activity_submissions WHERE status IN ('SUBMITTED','REVIEWED','COMPLETED')) AS submissions`),db.query(`SELECT c.id,c.title,c.level,c.is_published AS isPublished,c.created_at AS createdAt,(SELECT COUNT(*) FROM lessons l WHERE l.course_id=c.id) AS lessons,(SELECT COUNT(*) FROM lessons l WHERE l.course_id=c.id AND l.is_published=1) AS publishedLessons,(SELECT COUNT(*) FROM enrollments e WHERE e.course_id=c.id) AS enrollments,(SELECT COUNT(*) FROM enrollments e WHERE e.course_id=c.id AND e.status='ACTIVE') AS activeEnrollments,(SELECT COUNT(*) FROM enrollments e WHERE e.course_id=c.id AND e.status='COMPLETED') AS completedEnrollments,(SELECT COUNT(*) FROM enrollments e WHERE e.course_id=c.id AND e.status='CANCELLED') AS cancelledEnrollments,(SELECT ROUND(COALESCE(AVG(e.progress_percentage),0),2) FROM enrollments e WHERE e.course_id=c.id) AS averageProgress,(SELECT COUNT(*) FROM learning_activities a INNER JOIN lessons l ON l.id=a.lesson_id WHERE l.course_id=c.id) AS activities,(SELECT COUNT(*) FROM activity_submissions s INNER JOIN learning_activities a ON a.id=s.activity_id INNER JOIN lessons l ON l.id=a.lesson_id WHERE l.course_id=c.id) AS submissions FROM courses c ORDER BY enrollments DESC,c.title`),db.query(`SELECT role,COUNT(*) AS total,SUM(is_active=1) AS active FROM users GROUP BY role ORDER BY role`)]);const raw=summaryRows[0],summary={activeUsers:Number(raw.activeUsers),totalCourses:Number(raw.totalCourses),totalEnrollments:Number(raw.totalEnrollments),completedEnrollments:Number(raw.completedEnrollments),averageProgress:Number(raw.averageProgress),submissions:Number(raw.submissions),completionRate:Number(raw.totalEnrollments)?Math.round(Number(raw.completedEnrollments)/Number(raw.totalEnrollments)*10000)/100:0};const courses=courseRows.map((row:any)=>({...row,isPublished:Boolean(row.isPublished),lessons:Number(row.lessons),publishedLessons:Number(row.publishedLessons),enrollments:Number(row.enrollments),activeEnrollments:Number(row.activeEnrollments),completedEnrollments:Number(row.completedEnrollments),cancelledEnrollments:Number(row.cancelledEnrollments),averageProgress:Number(row.averageProgress),activities:Number(row.activities),submissions:Number(row.submissions)}));const roles=roleRows.map((row:any)=>({role:row.role,total:Number(row.total),active:Number(row.active)}));return res.status(200).json({success:true,generatedAt:new Date().toISOString(),summary,courses,roles})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getSettings(_req:Request,res:Response){try{const[rows]:any=await db.query(`SELECT s.id,s.setting_key,s.setting_value,s.description,s.updated_by,s.created_at,s.updated_at,COALESCE(CONCAT(u.first_name,' ',u.last_name),'System') AS updated_by_name FROM system_settings s LEFT JOIN users u ON u.id=s.updated_by ORDER BY s.setting_key`);const settings=rows.map((row:any)=>({id:row.id,settingKey:row.setting_key,settingValue:row.setting_value,description:row.description,updatedById:row.updated_by,updatedBy:row.updated_by_name,category:(String(row.setting_key).split(/[._-]/)[0]??"general").toUpperCase(),createdAt:row.created_at,updatedAt:row.updated_at}));return res.status(200).json({success:true,summary:{total:settings.length,configured:settings.filter((item:any)=>item.settingValue!==null&&item.settingValue!=="").length,categories:new Set(settings.map((item:any)=>item.category)).size,modifiedByAdmins:settings.filter((item:any)=>item.updatedById!==null).length},settings})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
function settingInput(body:any){const key=typeof body.settingKey==="string"?body.settingKey.trim().toLowerCase():"",value=typeof body.settingValue==="string"?body.settingValue.trim():"",description=typeof body.description==="string"?body.description.trim():"";if(!key)return{error:"Setting key is required"};if(!/^[a-z0-9]+(?:[._-][a-z0-9]+)*$/.test(key))return{error:"Use lowercase letters, numbers, dots, underscores, or hyphens in the setting key"};if(key.length>191||description.length>500)return{error:"Setting key or description is too long"};return{value:{key,value:value||null,description:description||null}}}
async function createSetting(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,input=settingInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{const[result]:any=await db.query("INSERT INTO system_settings(setting_key,setting_value,description,updated_by) VALUES(?,?,?,?)",[value.key,value.value,value.description,adminId]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'CREATE','SYSTEM_SETTING',?,?)`,[adminId,result.insertId,JSON.stringify({settingKey:value.key})]);return res.status(201).json({success:true,message:"System setting created successfully",setting:{id:result.insertId,settingKey:value.key}})}catch(error:any){if(error?.code==="ER_DUP_ENTRY")return res.status(409).json({success:false,message:"A setting with this key already exists"});console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function updateSetting(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id),input=settingInput(req.body);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid setting ID"});if(input.error)return res.status(400).json({success:false,message:input.error});const value=input.value!;try{const[result]:any=await db.query("UPDATE system_settings SET setting_key=?,setting_value=?,description=?,updated_by=? WHERE id=?",[value.key,value.value,value.description,adminId,id]);if(!result.affectedRows)return res.status(404).json({success:false,message:"System setting not found"});await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'UPDATE','SYSTEM_SETTING',?,?)`,[adminId,id,JSON.stringify({settingKey:value.key})]);return res.status(200).json({success:true,message:"System setting updated successfully"})}catch(error:any){if(error?.code==="ER_DUP_ENTRY")return res.status(409).json({success:false,message:"A setting with this key already exists"});console.error(error);return res.status(500).json({success:false,message:"Server error"})}}
async function deleteSetting(req:Request,res:Response){const adminId=(req as Request&{user?:{userId:number}}).user?.userId,id=Number(req.params.id);if(!adminId)return res.status(401).json({success:false,message:"Authentication required"});if(!Number.isSafeInteger(id)||id<=0)return res.status(400).json({success:false,message:"Invalid setting ID"});try{const[rows]:any=await db.query("SELECT setting_key AS settingKey FROM system_settings WHERE id=? LIMIT 1",[id]);if(!rows.length)return res.status(404).json({success:false,message:"System setting not found"});await db.query("DELETE FROM system_settings WHERE id=?",[id]);await db.query(`INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,'DELETE','SYSTEM_SETTING',?,?)`,[adminId,id,JSON.stringify(rows[0])]);return res.status(200).json({success:true,message:"System setting deleted successfully"})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

async function getAuditLogs(_req:Request,res:Response){try{const[rows]:any=await db.query(`SELECT al.id,al.user_id,al.action,al.entity_type,al.entity_id,al.details,al.ip_address,al.user_agent,al.created_at,COALESCE(CONCAT(u.first_name,' ',u.last_name),'System') AS actor,COALESCE(u.email,'—') AS actor_email FROM audit_logs al LEFT JOIN users u ON u.id=al.user_id ORDER BY al.created_at DESC,al.id DESC LIMIT 1000`);const logs=rows.map((row:any)=>({id:row.id,userId:row.user_id,actor:row.actor,actorEmail:row.actor_email,action:row.action,entityType:row.entity_type,entityId:row.entity_id,details:row.details,ipAddress:row.ip_address,userAgent:row.user_agent,createdAt:row.created_at}));const now=Date.now(),day=24*60*60*1000;return res.status(200).json({success:true,summary:{total:logs.length,today:logs.filter((item:any)=>now-new Date(item.createdAt).getTime()<day).length,administrators:new Set(logs.filter((item:any)=>item.userId!==null).map((item:any)=>item.userId)).size,security:logs.filter((item:any)=>/LOGIN|AUTH|PASSWORD|USER/i.test(`${item.action} ${item.entityType}`)).length},logs,limited:logs.length===1000})}catch(error){console.error(error);return res.status(500).json({success:false,message:"Server error"})}}

const resourceQueries: Record<string, { title: string; columns: string[]; query: string }> = {
  users: {
    title: "Users",
    columns: ["id", "name", "email", "role", "isActive", "lastLoginAt", "createdAt"],
    query: `SELECT id, first_name AS firstName, last_name AS lastName,
                   CONCAT(first_name, ' ', last_name) AS name, email, role, phone, bio,
                   is_active AS isActive, email_verified_at AS emailVerifiedAt,
                   last_login_at AS lastLoginAt, created_at AS createdAt
            FROM users ORDER BY created_at DESC LIMIT 200`,
  },
  lessons: {
    title: "Lessons",
    columns: ["id", "title", "course", "lessonOrder", "durationMinutes", "isPublished", "updatedAt"],
    query: `SELECT l.id, l.title, c.title AS course, l.lesson_order AS lessonOrder,
                   l.duration_minutes AS durationMinutes, l.is_published AS isPublished,
                   l.updated_at AS updatedAt
            FROM lessons l INNER JOIN courses c ON c.id = l.course_id
            ORDER BY c.title, l.lesson_order LIMIT 200`,
  },
  enrollments: {
    title: "Enrollments",
    columns: ["id", "student", "course", "status", "progressPercentage", "enrolledAt", "completedAt"],
    query: `SELECT e.id, CONCAT(u.first_name, ' ', u.last_name) AS student,
                   c.title AS course, e.status, e.progress_percentage AS progressPercentage,
                   e.enrolled_at AS enrolledAt, e.completed_at AS completedAt
            FROM enrollments e INNER JOIN users u ON u.id = e.user_id
            INNER JOIN courses c ON c.id = e.course_id
            ORDER BY e.enrolled_at DESC LIMIT 200`,
  },
  progress: {
    title: "Progress",
    columns: ["id", "student", "lesson", "course", "status", "progressPercentage", "lastAccessedAt"],
    query: `SELECT lp.id, CONCAT(u.first_name, ' ', u.last_name) AS student,
                   l.title AS lesson, c.title AS course, lp.status,
                   lp.progress_percentage AS progressPercentage, lp.last_accessed_at AS lastAccessedAt
            FROM lesson_progress lp INNER JOIN users u ON u.id = lp.user_id
            INNER JOIN lessons l ON l.id = lp.lesson_id
            INNER JOIN courses c ON c.id = l.course_id
            ORDER BY lp.updated_at DESC LIMIT 200`,
  },
  activities: {
    title: "Learning Activities",
    columns: ["id", "title", "lesson", "activityType", "maxScore", "isRequired", "updatedAt"],
    query: `SELECT a.id, a.title, l.title AS lesson, a.activity_type AS activityType,
                   a.max_score AS maxScore, a.is_required AS isRequired, a.updated_at AS updatedAt
            FROM learning_activities a INNER JOIN lessons l ON l.id = a.lesson_id
            ORDER BY a.updated_at DESC LIMIT 200`,
  },
  messages: {
    title: "Messages",
    columns: ["id", "sender", "message", "conversationId", "createdAt"],
    query: `SELECT m.id, CONCAT(u.first_name, ' ', u.last_name) AS sender,
                   LEFT(m.message_text, 120) AS message, m.conversation_id AS conversationId,
                   m.created_at AS createdAt
            FROM messages m INNER JOIN users u ON u.id = m.sender_id
            ORDER BY m.created_at DESC LIMIT 200`,
  },
  announcements: {
    title: "Announcements",
    columns: ["id", "title", "course", "createdBy", "isPublished", "publishedAt"],
    query: `SELECT a.id, a.title, COALESCE(c.title, 'All courses') AS course,
                   CONCAT(u.first_name, ' ', u.last_name) AS createdBy,
                   a.is_published AS isPublished, a.published_at AS publishedAt
            FROM announcements a LEFT JOIN courses c ON c.id = a.course_id
            INNER JOIN users u ON u.id = a.created_by
            ORDER BY a.created_at DESC LIMIT 200`,
  },
  notifications: {
    title: "Notifications",
    columns: ["id", "recipient", "title", "notificationType", "isRead", "createdAt"],
    query: `SELECT n.id, CONCAT(u.first_name, ' ', u.last_name) AS recipient,
                   n.title, n.notification_type AS notificationType,
                   n.is_read AS isRead, n.created_at AS createdAt
            FROM notifications n INNER JOIN users u ON u.id = n.user_id
            ORDER BY n.created_at DESC LIMIT 200`,
  },
  reports: {
    title: "Reports",
    columns: ["course", "level", "lessons", "enrollments", "averageProgress"],
    query: `SELECT c.title AS course, c.level, COUNT(DISTINCT l.id) AS lessons,
                   COUNT(DISTINCT e.id) AS enrollments,
                   ROUND(COALESCE(AVG(e.progress_percentage), 0), 2) AS averageProgress
            FROM courses c LEFT JOIN lessons l ON l.course_id = c.id
            LEFT JOIN enrollments e ON e.course_id = c.id
            GROUP BY c.id ORDER BY enrollments DESC, c.title LIMIT 200`,
  },
  settings: {
    title: "System Settings",
    columns: ["id", "settingKey", "settingValue", "description", "updatedBy", "updatedAt"],
    query: `SELECT id, setting_key AS settingKey, setting_value AS settingValue,
                   description, updated_by AS updatedBy, updated_at AS updatedAt
            FROM system_settings ORDER BY setting_key LIMIT 200`,
  },
  "audit-logs": {
    title: "Audit Logs",
    columns: ["id", "user", "action", "entityType", "entityId", "ipAddress", "createdAt"],
    query: `SELECT al.id, COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'System') AS user,
                   al.action, al.entity_type AS entityType, al.entity_id AS entityId,
                   al.ip_address AS ipAddress, al.created_at AS createdAt
            FROM audit_logs al LEFT JOIN users u ON u.id = al.user_id
            ORDER BY al.created_at DESC LIMIT 200`,
  },
};

async function getResource(req: Request, res: Response) {
  const resource = typeof req.params.resource === "string" ? req.params.resource : "";
  const definition = resourceQueries[resource];

  if (!definition) {
    return res.status(404).json({ success: false, message: "Admin page not found" });
  }

  try {
    const [records]: any = await db.query(definition.query);
    return res.status(200).json({
      success: true,
      resource,
      title: definition.title,
      columns: definition.columns,
      total: records.length,
      records,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

export = { getDashboard, getCourses, createCourse, updateCourse, deleteCourse, getLessons, createLesson, updateLesson, deleteLesson, getEnrollments, createEnrollment, updateEnrollment, deleteEnrollment, getProgress, updateProgressAdmin, deleteProgressAdmin, getActivities, createActivity, updateActivity, deleteActivity, getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, getMessages, createMessage, updateMessage, deleteMessage, getNotifications, createNotification, updateNotification, deleteNotification, getReports, getSettings, createSetting, updateSetting, deleteSetting, getAuditLogs, createUser, updateUser, deleteUser, getResource };
