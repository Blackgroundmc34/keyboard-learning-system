import jwt = require("jsonwebtoken");

interface AuthTokenPayload {
  userId: number;
  role: string;
}

function getJwtSecret() {
  const secret = process.env.JWT_SECRET;

  if (!secret) {
    throw new Error("JWT_SECRET is not configured");
  }

  return secret;
}

function generateToken(payload: AuthTokenPayload) {
  return jwt.sign(
    payload,
    getJwtSecret(),
    { expiresIn: "1d" }
  );
}

function verifyToken(token: string): AuthTokenPayload {
  const payload = jwt.verify(token, getJwtSecret());

  if (
    typeof payload === "string" ||
    typeof payload.userId !== "number" ||
    typeof payload.role !== "string"
  ) {
    throw new Error("Invalid token payload");
  }

  return {
    userId: payload.userId,
    role: payload.role,
  };
}

export = { generateToken, verifyToken };
