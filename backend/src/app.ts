const express = require("express");
const cors = require("cors");
const authRoutes = require("./routes/auth.routes");
const courseRoutes = require("./routes/course.routes");
const lessonRoutes = require("./routes/lesson.routes");
const meRoutes = require("./routes/me.routes");
import type { Request, Response } from "express";

const app = express();

app.use(cors());
app.use(express.json());

app.get("/api/health", (_req: Request, res: Response) => {
  res.status(200).json({
    success: true,
    message: "Keyboard Learning API is running",
  });
});

app.use("/api/auth", authRoutes);
app.use("/api/courses", courseRoutes);
app.use("/api/lessons", lessonRoutes);
app.use("/api/me", meRoutes);

module.exports = app;
