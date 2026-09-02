import jwt = require("jsonwebtoken");

function generateToken(payload: object) {
  return jwt.sign(
    payload,
    process.env.JWT_SECRET as string,
    { expiresIn: "1d" }
  );
}

export = { generateToken };