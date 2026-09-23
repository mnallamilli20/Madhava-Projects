import express from "express";
import ListingReview from "../models/ListingReview.js";
import Listing from "../models/Listing.js";
import User from "../models/User.js";

const router = express.Router();

function cleanId(value) {
  if (!value || value === "undefined" || value === "null") {
    return "";
  }

  return String(value).trim();
}

async function getActor(req) {
  const actorId = cleanId(req.body.actorId || req.query.actorId || req.headers["x-user-id"] || req.headers["x-actor-id"]);

  if (!actorId) {
    return null;
  }

  return User.findById(actorId);
}

function ownsReview(actor, review) {
  return Boolean(actor?._id && review?.userId && String(review.userId) === String(actor._id));
}

function canDeleteReview(actor, review) {
  return ownsReview(actor, review) || actor?.role === "Admin";
}

router.get("/", async (req, res) => {
  try {
    const query = {};

    if (req.query.listingId) {
      query.listingId = req.query.listingId;
    }

    const reviews = await ListingReview.find(query).populate("listingId").sort({ createdAt: -1 });

    res.json(reviews);
  } catch (error) {
    res.status(500).json({ message: "Unable to load reviews." });
  }
});

router.post("/", async (req, res) => {
  try {
    const actor = await getActor(req);
    const { userId, listingId, authorName, rating, title, comment } = req.body;

    if (!actor || String(actor._id) !== String(userId)) {
      return res.status(403).json({ message: "Please log in to create a review." });
    }

    if (!listingId || !authorName || !rating || !title || !comment) {
      return res.status(400).json({ message: "All review fields are required." });
    }

    const listing = await Listing.findById(listingId);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    const review = await ListingReview.create({ userId, listingId, authorName, rating, title, comment });
    const savedReview = await review.populate("listingId");

    res.status(201).json(savedReview);
  } catch (error) {
    res.status(500).json({ message: "Unable to create review." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const actor = await getActor(req);
    const review = await ListingReview.findById(req.params.id);

    if (!review) {
      return res.status(404).json({ message: "Review not found." });
    }

    if (!ownsReview(actor, review)) {
      return res.status(403).json({ message: "You can only edit reviews that you created." });
    }

    const { authorName, rating, title, comment } = req.body;

    if (authorName !== undefined) {
      review.authorName = authorName;
    }

    if (rating !== undefined) {
      review.rating = rating;
    }

    if (title !== undefined) {
      review.title = title;
    }

    if (comment !== undefined) {
      review.comment = comment;
    }

    await review.save();
    await review.populate("listingId");

    res.json(review);
  } catch (error) {
    res.status(500).json({ message: "Unable to update review." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const actor = await getActor(req);
    const review = await ListingReview.findById(req.params.id);

    if (!review) {
      return res.status(404).json({ message: "Review not found." });
    }

    if (!canDeleteReview(actor, review)) {
      return res.status(403).json({ message: "You can only delete your own reviews. Admins can delete reviews for moderation." });
    }

    await ListingReview.findByIdAndDelete(req.params.id);

    res.json({ message: "Review removed." });
  } catch (error) {
    res.status(500).json({ message: "Unable to remove review." });
  }
});

export default router;
