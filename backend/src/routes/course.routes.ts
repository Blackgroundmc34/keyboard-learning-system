const { Router } = require("express");
const {
  getCourses,
  getCourseById,
} = require("../controllers/course.controller");
const { getCourseLessons } = require("../controllers/lesson.controller");
const { enrollInCourse } = require("../controllers/enrollment.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = Router();

router.get("/", getCourses);
router.post("/:id/enroll", authenticate, enrollInCourse);
router.get("/:id/lessons", getCourseLessons);
router.get("/:id", getCourseById);

module.exports = router;
