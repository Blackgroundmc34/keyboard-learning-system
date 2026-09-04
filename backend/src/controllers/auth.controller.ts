import type { Request, Response } from "express";
import bcrypt = require("bcryptjs");
import database = require("../config/database");
import jwt = require("../utils/jwt");

const { db } = database;
const { generateToken } = jwt;

async function register(req: Request, res: Response) {
  try {
    const { firstName, lastName, email, password } = req.body;

    if (![firstName, lastName, email, password].every(value => typeof value === "string" && value.trim())) {
      return res.status(400).json({
        success: false,
        message: "All fields are required",
      });
    }
    const normalizedEmail = email.trim().toLowerCase();
    if (firstName.trim().length > 100 || lastName.trim().length > 100 || normalizedEmail.length > 191) {
      return res.status(400).json({ success: false, message: "Name or email is too long" });
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      return res.status(400).json({ success: false, message: "Enter a valid email address" });
    }
    if (password.length < 12 || password.length > 128) {
      return res.status(400).json({ success: false, message: "Password must be between 12 and 128 characters" });
    }

    const [existingUsers]: any = await db.query(
      "SELECT id FROM users WHERE email = ? LIMIT 1",
      [normalizedEmail]
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
      [firstName.trim(), lastName.trim(), normalizedEmail, passwordHash]
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
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: normalizedEmail,
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

    if (typeof email !== "string" || typeof password !== "string" || !email.trim() || !password) {
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
      [email.trim().toLowerCase()]
    );

    if (users.length === 0) {
      await bcrypt.compare(password, "$2b$12$8phC4toPc2kCdlRDWnjRQuTD4S30H4FJiLcJvJ4hD1qT6yJIz4f.e");
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    const user = users[0];

    const passwordMatches = await bcrypt.compare(
      password,
      user.password_hash
    );

    if (!passwordMatches || !user.is_active) {
      return res.status(401).json({
        success: false,
        message: "Invalid email or password",
      });
    }

    await db.query(
      "UPDATE users SET last_login_at = NOW() WHERE id = ?",
      [user.id]
    );
    await db.query(
      `INSERT INTO audit_logs (user_id, action, entity_type, entity_id, ip_address, user_agent)
       VALUES (?, 'LOGIN', 'USER', ?, ?, ?)`,
      [user.id, user.id, req.ip || null, String(req.headers["user-agent"] || "").slice(0, 500) || null]
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

async function updateMe(req: Request, res: Response) {
  const userId = (req as Request & { user?: { userId: number } }).user?.userId;

  if (!userId) {
    return res.status(401).json({ success: false, message: "Authentication required" });
  }

  const { firstName, lastName, email, phone, bio } = req.body;
  if ([firstName, lastName, email].some(value => typeof value !== "string") || !firstName.trim() || !lastName.trim() || !email.trim()) {
    return res.status(400).json({
      success: false,
      message: "First name, last name, and email are required",
    });
  }
  if (firstName.trim().length > 100 || lastName.trim().length > 100 || String(email).trim().length > 191 || String(phone || "").length > 30 || String(bio || "").length > 65535) {
    return res.status(400).json({ success: false, message: "One or more profile fields are too long" });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
    return res.status(400).json({ success: false, message: "Enter a valid email address" });
  }

  try {
    await db.query(
      `UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, bio = ?
       WHERE id = ?`,
      [String(firstName).trim(), String(lastName).trim(), normalizedEmail,
       phone ? String(phone).trim() : null, bio ? String(bio).trim() : null, userId]
    );

    return getMe(req, res);
  } catch (error: any) {
    if (error?.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ success: false, message: "Email address is already in use" });
    }
    console.error(error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
}

module.exports = { register, login, getMe, updateMe };
