import express from "express";
import mongoose from "mongoose";
import Favorite from "../models/Favorite.js";
import Listing from "../models/Listing.js";
import User from "../models/User.js";

const router = express.Router();

function cleanId(value) {
  if (!value || value === "undefined" || value === "null") {
    return "";
  }

  return String(value).trim();
}

function requestUserId(req) {
  const body = req.body || {};
  const query = req.query || {};
  const headers = req.headers || {};

  return cleanId(body.userId || query.userId || headers["x-user-id"]);
}

async function getActor(req) {
  const body = req.body || {};
  const query = req.query || {};
  const headers = req.headers || {};

  const actorId = cleanId(
    body.actorId ||
      query.actorId ||
      headers["x-actor-id"] ||
      requestUserId(req)
  );

  if (!actorId || !mongoose.Types.ObjectId.isValid(actorId)) {
    return null;
  }

  return User.findById(actorId);
}

function canUseRecord(actor, record, userId) {
  if (actor?.role === "Admin") {
    return true;
  }

  return (
    actor &&
    userId &&
    record &&
    String(record.userId) === String(userId) &&
    String(actor._id) === String(userId)
  );
}

router.get("/", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);

    if (!actor) {
      return res.status(401).json({ message: "Please log in to view favorites." });
    }

    const query =
      actor.role === "Admin" && req.query?.all === "true"
        ? {}
        : { userId: userId || String(actor._id) };

    const favorites = await Favorite.find(query)
      .populate("listingId")
      .sort({ createdAt: -1 });

    res.json(favorites);
  } catch (error) {
    console.error("Favorites GET error:", error);
    res.status(500).json({ message: "Unable to load favorites." });
  }
});

router.post("/", async (req, res) => {
  try {
    const body = req.body || {};
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const listingId = cleanId(body.listingId);
    const note = body.note || "";
    const label = body.label || "";
    const priority = body.priority || "Medium";

    if (!actor || String(actor._id) !== String(userId)) {
      return res.status(403).json({ message: "Please log in to save favorites." });
    }

    if (!userId || !listingId) {
      return res.status(400).json({ message: "User and listing are required." });
    }

    if (!mongoose.Types.ObjectId.isValid(listingId)) {
      return res.status(400).json({ message: "Choose a valid listing before saving it." });
    }

    const listing = await Listing.findById(listingId);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    const existingFavorite = await Favorite.findOne({
      userId,
      listingId
    });

    if (existingFavorite) {
      return res.status(409).json({ message: "This listing is already saved." });
    }

    const favorite = await Favorite.create({
      userId,
      listingId,
      note,
      label,
      priority
    });

    const populatedFavorite = await favorite.populate("listingId");

    res.status(201).json(populatedFavorite);
  } catch (error) {
    console.error("Favorites POST error:", error);
    res.status(500).json({ message: "Unable to create favorite." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const body = req.body || {};
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const favorite = await Favorite.findById(req.params.id);

    if (!favorite) {
      return res.status(404).json({ message: "Favorite not found." });
    }

    if (!canUseRecord(actor, favorite, userId)) {
      return res.status(403).json({ message: "You can only update your own favorites." });
    }

    if (body.note !== undefined) {
      favorite.note = body.note;
    }

    if (body.label !== undefined) {
      favorite.label = body.label;
    }

    if (body.priority !== undefined) {
      favorite.priority = body.priority;
    }

    await favorite.save();
    await favorite.populate("listingId");

    res.json(favorite);
  } catch (error) {
    console.error("Favorites PUT error:", error);
    res.status(500).json({ message: "Unable to update favorite." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const favorite = await Favorite.findById(req.params.id);

    if (!favorite) {
      return res.status(404).json({ message: "Favorite not found." });
    }

    if (!canUseRecord(actor, favorite, userId)) {
      return res.status(403).json({ message: "You can only delete your own favorites." });
    }

    await Favorite.findByIdAndDelete(req.params.id);

    res.json({ message: "Favorite removed." });
  } catch (error) {
    console.error("Favorites DELETE error:", error);
    res.status(500).json({ message: "Unable to remove favorite." });
  }
});

export default router;