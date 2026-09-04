const express = require("express");
const cors = require("cors");
const authRoutes = require("./routes/auth.routes");
const courseRoutes = require("./routes/course.routes");
const lessonRoutes = require("./routes/lesson.routes");
const meRoutes = require("./routes/me.routes");
const adminRoutes = require("./routes/admin.routes");
const { securityHeaders } = require("./middleware/security.middleware");
import type { Request, Response } from "express";

const app = express();

app.disable("x-powered-by");
const allowedOrigins = (process.env.ADMIN_ORIGINS || "http://localhost:5173,http://127.0.0.1:5173")
  .split(",").map((origin: string) => origin.trim()).filter(Boolean);
app.use(securityHeaders);
app.use(cors({
  origin(origin: string | undefined, callback: (error: Error | null, allowed?: boolean) => void) {
    if (!origin || allowedOrigins.includes(origin)) return callback(null, true);
    return callback(new Error("Origin is not allowed"));
  },
  methods: ["GET", "POST", "PATCH", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type", "Authorization"],
  maxAge: 600,
}));
app.use(express.json({ limit: "100kb", strict: true }));

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
app.use("/api/admin", adminRoutes);

app.use((_req: Request, res: Response) => res.status(404).json({ success: false, message: "Endpoint not found" }));
app.use((error: Error, _req: Request, res: Response, _next: unknown) => {
  if (error.message === "Origin is not allowed") return res.status(403).json({ success: false, message: error.message });
  if ((error as Error & { type?: string }).type === "entity.too.large") return res.status(413).json({ success: false, message: "JSON body exceeds the 100 KB limit" });
  if (error instanceof SyntaxError) return res.status(400).json({ success: false, message: "Invalid JSON body" });
  console.error(error);
  return res.status(500).json({ success: false, message: "Server error" });
});

module.exports = app;
