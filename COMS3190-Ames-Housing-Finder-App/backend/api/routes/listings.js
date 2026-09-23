import express from "express";
import Listing from "../models/Listing.js";
import User from "../models/User.js";

const router = express.Router();

function parseAmenities(value) {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean);
  }

  if (typeof value === "string") {
    return value.split(",").map((item) => item.trim()).filter(Boolean);
  }

  return [];
}

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
    res.status(403).json({ message: "Only admins can manage listings." });
    return null;
  }

  return actor;
}

function buildListingPayload(body) {
  return {
    title: body.title,
    address: body.address,
    neighborhood: body.neighborhood,
    rent: Number(body.rent),
    bedrooms: Number(body.bedrooms),
    bathrooms: Number(body.bathrooms),
    squareFeet: Number(body.squareFeet || 0),
    propertyType: body.propertyType,
    availableDate: body.availableDate,
    description: body.description,
    amenities: parseAmenities(body.amenities),
    imageUrl: body.imageUrl,
    contactEmail: body.contactEmail,
    status: body.status
  };
}

router.get("/", async (req, res) => {
  try {
    const { search, minRent, maxRent, bedrooms, propertyType, status, sort } = req.query;
    const query = {};

    if (search) {
      query.$or = [
        { title: { $regex: search, $options: "i" } },
        { address: { $regex: search, $options: "i" } },
        { neighborhood: { $regex: search, $options: "i" } },
        { description: { $regex: search, $options: "i" } }
      ];
    }

    if (minRent || maxRent) {
      query.rent = {};

      if (minRent) {
        query.rent.$gte = Number(minRent);
      }

      if (maxRent) {
        query.rent.$lte = Number(maxRent);
      }
    }

    if (bedrooms) {
      query.bedrooms = { $gte: Number(bedrooms) };
    }

    if (propertyType) {
      query.propertyType = propertyType;
    }

    if (status) {
      query.status = status;
    }

    const sortMap = {
      rentAsc: { rent: 1 },
      rentDesc: { rent: -1 },
      newest: { createdAt: -1 },
      bedrooms: { bedrooms: -1 }
    };

    const listings = await Listing.find(query).sort(sortMap[sort] || { createdAt: -1 });

    res.json(listings);
  } catch (error) {
    res.status(500).json({ message: "Unable to load listings." });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const listing = await Listing.findById(req.params.id);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    res.json(listing);
  } catch (error) {
    res.status(500).json({ message: "Unable to load listing." });
  }
});

router.post("/", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const { title, address, neighborhood, rent, bedrooms, bathrooms } = req.body;

    if (!title || !address || !neighborhood || rent === undefined || bedrooms === undefined || bathrooms === undefined) {
      return res.status(400).json({ message: "Title, address, neighborhood, rent, bedrooms, and bathrooms are required." });
    }

    const listing = await Listing.create(buildListingPayload(req.body));

    res.status(201).json(listing);
  } catch (error) {
    res.status(500).json({ message: "Unable to create listing." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const listing = await Listing.findByIdAndUpdate(
      req.params.id,
      buildListingPayload(req.body),
      { new: true, runValidators: true }
    );

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    res.json(listing);
  } catch (error) {
    res.status(500).json({ message: "Unable to update listing." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const actor = await requireAdmin(req, res);

    if (!actor) {
      return;
    }

    const listing = await Listing.findByIdAndDelete(req.params.id);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    res.json({ message: "Listing removed." });
  } catch (error) {
    res.status(500).json({ message: "Unable to remove listing." });
  }
});

export default router;
