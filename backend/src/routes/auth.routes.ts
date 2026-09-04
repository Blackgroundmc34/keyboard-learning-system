const { Router } = require("express");
const { register, login, getMe, updateMe } = require("../controllers/auth.controller");
const { authenticate } = require("../middleware/auth.middleware");
const { authRateLimit } = require("../middleware/security.middleware");

const router = Router();

router.post("/register", authRateLimit, register);
router.post("/login", authRateLimit, login);
router.get("/me", authenticate, getMe);
router.patch("/me", authenticate, updateMe);

module.exports = router;
