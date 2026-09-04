import type { NextFunction, Request, Response } from "express";

type Attempt = { count: number; resetAt: number };
const attempts = new Map<string, Attempt>();

function securityHeaders(_req: Request, res: Response, next: NextFunction) {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("X-Frame-Options", "DENY");
  res.setHeader("Referrer-Policy", "no-referrer");
  res.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
  res.setHeader("Cross-Origin-Resource-Policy", "same-site");
  res.setHeader("Cache-Control", "no-store");
  return next();
}

function authRateLimit(req: Request, res: Response, next: NextFunction) {
  const now = Date.now();
  const windowMs = 15 * 60 * 1000;
  const maximum = 10;
  const key = req.ip || req.socket.remoteAddress || "unknown";
  const current = attempts.get(key);
  const attempt = !current || current.resetAt <= now ? { count: 0, resetAt: now + windowMs } : current;
  attempt.count += 1;
  attempts.set(key, attempt);
  res.setHeader("RateLimit-Limit", String(maximum));
  res.setHeader("RateLimit-Remaining", String(Math.max(0, maximum - attempt.count)));
  res.setHeader("RateLimit-Reset", String(Math.ceil(attempt.resetAt / 1000)));
  if (attempt.count > maximum) {
    res.setHeader("Retry-After", String(Math.ceil((attempt.resetAt - now) / 1000)));
    return res.status(429).json({ success: false, message: "Too many authentication attempts. Try again later." });
  }
  if (attempts.size > 5000) {
    for (const [storedKey, stored] of attempts) if (stored.resetAt <= now) attempts.delete(storedKey);
  }
  return next();
}

export = { securityHeaders, authRateLimit };
