import type { NextFunction, Request, Response } from "express";
import jwt = require("../utils/jwt");

interface AuthenticatedRequest extends Request {
  user?: {
    userId: number;
    role: string;
  };
}

function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
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
    req.user = jwt.verifyToken(token);
    return next();
  } catch {
    return res.status(401).json({
      success: false,
      message: "Invalid or expired authentication token",
    });
  }
}

export = { authenticate };
