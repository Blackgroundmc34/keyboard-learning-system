const { Router } = require("express");
const { register, login, getMe } = require("../controllers/auth.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = Router();

router.post("/register", register);
router.post("/login", login);
router.get("/me", authenticate, getMe);

module.exports = router;
