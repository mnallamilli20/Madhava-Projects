import express from "express";
import mongoose from "mongoose";
import CompareSelection from "../models/CompareSelection.js";
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

async function hydrateSelections(selections) {
  const plainSelections = selections.map((selection) => {
    if (typeof selection.toObject === "function") {
      return selection.toObject();
    }

    return selection;
  });

  const listingIds = plainSelections
    .map((selection) => cleanId(selection.listingId))
    .filter((listingId) => mongoose.Types.ObjectId.isValid(listingId));

  const listings = await Listing.find({ _id: { $in: listingIds } }).lean();
  const listingsById = new Map(
    listings.map((listing) => [String(listing._id), listing])
  );

  return plainSelections.map((selection) => ({
    ...selection,
    listingId: listingsById.get(String(selection.listingId)) || null
  }));
}

router.get("/", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);

    if (!actor) {
      return res.status(401).json({ message: "Please log in to compare listings." });
    }

    const query =
      actor.role === "Admin" && req.query?.all === "true"
        ? {}
        : { userId: userId || String(actor._id) };

    const selections = await CompareSelection.find(query).sort({ createdAt: -1 });
    const hydratedSelections = await hydrateSelections(selections);

    res.json(hydratedSelections);
  } catch (error) {
    console.error("Compare GET error:", error);
    res.status(500).json({ message: "Unable to load compare selections." });
  }
});

router.post("/", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const listingId = cleanId(req.body?.listingId);
    const compareNote = req.body?.compareNote || "";

    if (!actor || String(actor._id) !== String(userId)) {
      return res.status(403).json({ message: "Please log in to compare listings." });
    }

    if (!userId || !listingId) {
      return res.status(400).json({ message: "User and listing are required." });
    }

    if (!mongoose.Types.ObjectId.isValid(listingId)) {
      return res.status(400).json({ message: "Choose a valid listing before adding it to compare." });
    }

    const listing = await Listing.findById(listingId);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    const existingSelection = await CompareSelection.findOne({
      userId,
      listingId
    });

    if (existingSelection) {
      return res.status(409).json({ message: "This listing is already in compare." });
    }

    const count = await CompareSelection.countDocuments({ userId });

    if (count >= 3) {
      return res.status(400).json({ message: "You can compare up to three listings at a time." });
    }

    const selection = await CompareSelection.create({
      userId,
      listingId,
      compareNote
    });

    const hydratedSelection = (await hydrateSelections([selection]))[0];

    res.status(201).json(hydratedSelection);
  } catch (error) {
    console.error("Compare POST error:", error);
    res.status(500).json({ message: "Unable to create compare selection." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const selection = await CompareSelection.findById(req.params.id);

    if (!selection) {
      return res.status(404).json({ message: "Compare selection not found." });
    }

    if (!canUseRecord(actor, selection, userId)) {
      return res.status(403).json({ message: "You can only update your own compare selections." });
    }

    selection.compareNote = req.body?.compareNote || "";
    await selection.save();

    const hydratedSelection = (await hydrateSelections([selection]))[0];

    res.json(hydratedSelection);
  } catch (error) {
    console.error("Compare PUT error:", error);
    res.status(500).json({ message: "Unable to update compare selection." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const selection = await CompareSelection.findById(req.params.id);

    if (!selection) {
      return res.status(404).json({ message: "Compare selection not found." });
    }

    if (!canUseRecord(actor, selection, userId)) {
      return res.status(403).json({ message: "You can only delete your own compare selections." });
    }

    await CompareSelection.findByIdAndDelete(req.params.id);

    res.json({ message: "Compare selection removed." });
  } catch (error) {
    console.error("Compare DELETE error:", error);
    res.status(500).json({ message: "Unable to remove compare selection." });
  }
});

export default router;