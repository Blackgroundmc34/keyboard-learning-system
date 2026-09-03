const { Router } = require("express");
const { getLessonById } = require("../controllers/lesson.controller");
const { updateLessonProgress } = require("../controllers/progress.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = Router();

router.post("/:id/progress", authenticate, updateLessonProgress);
router.get("/:id", getLessonById);

module.exports = router;
