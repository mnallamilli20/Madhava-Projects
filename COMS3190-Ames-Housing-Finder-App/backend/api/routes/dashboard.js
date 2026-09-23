import express from "express";
import Listing from "../models/Listing.js";
import Favorite from "../models/Favorite.js";
import CompareSelection from "../models/CompareSelection.js";
import TourAppointment from "../models/TourAppointment.js";
import ListingReview from "../models/ListingReview.js";
import HousingResource from "../models/HousingResource.js";
import User from "../models/User.js";

const router = express.Router();

async function getActor(req) {
  const actorId = req.query.actorId || req.headers["x-user-id"];

  if (!actorId) {
    return null;
  }

  return User.findById(actorId);
}

router.get("/", async (req, res) => {
  try {
    const actor = await getActor(req);

    if (!actor) {
      return res.status(401).json({ message: "Please log in to view the dashboard." });
    }

    const userQuery = actor.role === "Admin" ? {} : { userId: String(actor._id) };

    const [listings, favorites, compare, appointments, reviews, resources, featuredListings, recentFavorites, recentAppointments, featuredResources] = await Promise.all([
      Listing.countDocuments(),
      Favorite.countDocuments(userQuery),
      CompareSelection.countDocuments(userQuery),
      TourAppointment.countDocuments(userQuery),
      ListingReview.countDocuments(actor.role === "Admin" ? {} : { userId: String(actor._id) }),
      HousingResource.countDocuments(),
      Listing.find().sort({ createdAt: -1 }).limit(3),
      Favorite.find(userQuery).populate("listingId").sort({ createdAt: -1 }).limit(3),
      TourAppointment.find(userQuery).populate("listingId").sort({ tourDate: 1 }).limit(3),
      HousingResource.find({ isFeatured: true }).sort({ category: 1 }).limit(3)
    ]);

    res.json({
      counts: {
        listings,
        favorites,
        compare,
        appointments,
        reviews,
        resources
      },
      featuredListings,
      recentFavorites,
      recentAppointments,
      featuredResources
    });
  } catch (error) {
    res.status(500).json({ message: "Unable to load dashboard data." });
  }
});

export default router;
