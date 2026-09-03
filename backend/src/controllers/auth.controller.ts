import type { Request, Response } from "express";
import bcrypt = require("bcryptjs");
import database = require("../config/database");
import jwt = require("../utils/jwt");

const { db } = database;
const { generateToken } = jwt;

async function register(req: Request, res: Response) {
  try {
    const { firstName, lastName, email, password } = req.body;

    if (!firstName || !lastName || !email || !password) {
      return res.status(400).json({
        success: false,
        message: "All fields are required",
      });
    }

    const [existingUsers]: any = await db.query(
      "SELECT id FROM users WHERE email = ? LIMIT 1",
      [email]
    );

    if (existingUsers.length > 0) {
      return res.status(409).json({
        success: false,
        message: "Email already registered",
      });
    }

    const passwordHash = await bcrypt.hash(password, 12);

    const [result]: any = await db.query(
      `INSERT INTO users
       (first_name, last_name, email, password_hash, role)
       VALUES (?, ?, ?, ?, 'STUDENT')`,
      [firstName, lastName, email, passwordHash]
    );

    const token = generateToken({
      userId: result.insertId,
      role: "STUDENT",
    });

    return res.status(201).json({
      success: true,
      message: "Registration successful",
      user: {
        id: result.insertId,
        firstName,
        lastName,
        email,
        role: "STUDENT",
      },
      token,
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

/**
 * Handles user login.
 * @param req - The request object containing email and password.
 * @param res - The response object to send the result.
 */

async function login(req: Request, res: Response) {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: "Email and password are required",
      });
    }

    const [users]: any = await db.query(
      `SELECT id, first_name, last_name, email,
              password_hash, role, is_active
       FROM users
       WHERE email = ?
       LIMIT 1`,
      [email]
    );

    if (users.length === 0) {
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    const user = users[0];

    if (!user.is_active) {
      return res.status(403).json({
        success: false,
        message: "Account is inactive",
      });
    }

    const passwordMatches = await bcrypt.compare(
      password,
      user.password_hash
    );

    if (!passwordMatches) {
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    await db.query(
      "UPDATE users SET last_login_at = NOW() WHERE id = ?",
      [user.id]
    );

    const token = generateToken({
      userId: user.id,
      role: user.role,
    });

    return res.status(200).json({
      success: true,
      message: "Login successful",
      user: {
        id: user.id,
        firstName: user.first_name,
        lastName: user.last_name,
        email: user.email,
        role: user.role,
      },
      token,
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

async function getMe(req: Request, res: Response) {
  try {
    const authenticatedRequest = req as Request & {
      user?: { userId: number; role: string };
    };
    const userId = authenticatedRequest.user?.userId;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Authentication required",
      });
    }

    const [users]: any = await db.query(
      `SELECT id, first_name, last_name, email, role, phone,
              profile_image_url, bio, is_active, email_verified_at,
              last_login_at, created_at, updated_at
       FROM users
       WHERE id = ?
       LIMIT 1`,
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    const user = users[0];

    if (!user.is_active) {
      return res.status(403).json({
        success: false,
        message: "Account is inactive",
      });
    }

    return res.status(200).json({
      success: true,
      user: {
        id: user.id,
        firstName: user.first_name,
        lastName: user.last_name,
        email: user.email,
        role: user.role,
        phone: user.phone,
        profileImageUrl: user.profile_image_url,
        bio: user.bio,
        emailVerifiedAt: user.email_verified_at,
        lastLoginAt: user.last_login_at,
        createdAt: user.created_at,
        updatedAt: user.updated_at,
      },
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      success: false,
      message: "Server error",
    });
  }
}

module.exports = { register, login, getMe };
