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
  if (secret.length < 32) {
    throw new Error("JWT_SECRET must be at least 32 characters long");
  }

  return secret;
}

function generateToken(payload: AuthTokenPayload) {
  return jwt.sign(
    payload,
    getJwtSecret(),
    { expiresIn: "8h", algorithm: "HS256", issuer: "keyboard-learning-api", audience: "keyboard-learning-web" }
  );
}

function verifyToken(token: string): AuthTokenPayload {
  const payload = jwt.verify(token, getJwtSecret(), { algorithms: ["HS256"], issuer: "keyboard-learning-api", audience: "keyboard-learning-web" });

  if (
    typeof payload === "string" ||
    typeof payload.userId !== "number" ||
    !["STUDENT", "INSTRUCTOR", "ADMIN"].includes(String(payload.role))
  ) {
    throw new Error("Invalid token payload");
  }

  return {
    userId: payload.userId,
    role: payload.role,
  };
}

export = { generateToken, verifyToken };
