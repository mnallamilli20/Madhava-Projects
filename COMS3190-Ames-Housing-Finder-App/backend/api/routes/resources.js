import express from "express";
import HousingResource from "../models/HousingResource.js";
import User from "../models/User.js";

const router = express.Router();

async function getActor(req) {
  const actorId = req.body.actorId || req.query.actorId || req.headers["x-user-id"];

  if (!actorId) {
    return null;
  }

  return User.findById(actorId);
}

async function requireAdmin(req, res) {
  const actor = await getActor(req);

  if (actor?.role !== "Admin") {
    res.status(403).json({ message: "Only admins can manage housing resources." });
    return null;
  }

  return actor;
}

router.get("/", async (req, res) => {
  try {
    const query = {};

    if (req.query.category) {
      query.category = req.query.category;
    }

    const resources = await HousingResource.find(query).sort({ isFeatured: -1, category: 1, title: 1 });

    res.json(resources);
  } catch (error) {
    res.status(500).json({ message: "Unable to load resources." });
  }
});

router.post("/", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const { category, title, body, link, isFeatured } = req.body;

    if (!title || !body) {
      return res.status(400).json({ message: "Title and body are required." });
    }

    const resource = await HousingResource.create({ category, title, body, link, isFeatured });

    res.status(201).json(resource);
  } catch (error) {
    res.status(500).json({ message: "Unable to create resource." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const { category, title, body, link, isFeatured } = req.body;
    const resource = await HousingResource.findByIdAndUpdate(
      req.params.id,
      { category, title, body, link, isFeatured },
      { new: true, runValidators: true }
    );

    if (!resource) {
      return res.status(404).json({ message: "Resource not found." });
    }

    res.json(resource);
  } catch (error) {
    res.status(500).json({ message: "Unable to update resource." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const resource = await HousingResource.findByIdAndDelete(req.params.id);

    if (!resource) {
      return res.status(404).json({ message: "Resource not found." });
    }

    res.json({ message: "Resource removed." });
  } catch (error) {
    res.status(500).json({ message: "Unable to remove resource." });
  }
});

export default router;
