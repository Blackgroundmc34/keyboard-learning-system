import type { NextFunction, Request, Response } from "express";
import jwt = require("../utils/jwt");
import database = require("../config/database");
const { db } = database;

interface AuthenticatedRequest extends Request {
  user?: {
    userId: number;
    role: string;
  };
}

async function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  const authorization = req.headers.authorization;

  if (!authorization?.startsWith("Bearer ")) {
    return res.status(401).json({
      success: false,
      message: "Authentication token is required",
    });
  }

  const token = authorization.slice("Bearer ".length).trim();

  if (!token) {
    return res.status(401).json({
      success: false,
      message: "Authentication token is required",
    });
  }

  try {
    const payload = jwt.verifyToken(token);
    const [users]: any = await db.query("SELECT id, role, is_active FROM users WHERE id = ? LIMIT 1", [payload.userId]);
    if (!users.length || !users[0].is_active || users[0].role !== payload.role) {
      return res.status(401).json({ success: false, message: "Authentication session is no longer valid" });
    }
    req.user = { userId: users[0].id, role: users[0].role };
    return next();
  } catch {
    return res.status(401).json({
      success: false,
      message: "Invalid or expired authentication token",
    });
  }
}

function authorizeRoles(...roles: string[]) {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({ success: false, message: "Authentication required" });
    }

    if (!roles.includes(req.user.role)) {
      return res.status(403).json({ success: false, message: "Insufficient permissions" });
    }

    return next();
  };
}

export = { authenticate, authorizeRoles };
