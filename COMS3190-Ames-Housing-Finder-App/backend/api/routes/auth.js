import express from "express";
import crypto from "crypto";
import User from "../models/User.js";

const router = express.Router();

function hashPassword(password) {
  return crypto.createHash("sha256").update(password).digest("hex");
}

function publicUser(user) {
  return {
    _id: user._id,
    name: user.name,
    email: user.email,
    role: user.role
  };
}

router.post("/signup", async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Name, email, and password are required." });
    }

    const user = await User.create({
      name,
      email,
      passwordHash: hashPassword(password),
      role: "Student"
    });

    res.status(201).json(publicUser(user));
  } catch (error) {
    if (error.code === 11000) {
      return res.status(409).json({ message: "An account with this email already exists." });
    }

    res.status(500).json({ message: "Unable to create account." });
  }
});

router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ message: "Email and password are required." });
    }

    const user = await User.findOne({ email: email.toLowerCase().trim() });

    if (!user || user.passwordHash !== hashPassword(password)) {
      return res.status(401).json({ message: "Invalid email or password." });
    }

    res.json(publicUser(user));
  } catch (error) {
    res.status(500).json({ message: "Unable to log in." });
  }
});

export default router;
