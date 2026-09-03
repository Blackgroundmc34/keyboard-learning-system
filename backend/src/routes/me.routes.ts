const { Router } = require("express");
const { authenticate } = require("../middleware/auth.middleware");
const { getMyEnrollments } = require("../controllers/enrollment.controller");
const { getMyProgress } = require("../controllers/progress.controller");

const router = Router();

router.get("/enrollments", authenticate, getMyEnrollments);
router.get("/progress", authenticate, getMyProgress);

module.exports = router;
