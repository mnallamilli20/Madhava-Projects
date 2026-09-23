import express from "express";
import mongoose from "mongoose";
import TourAppointment from "../models/TourAppointment.js";
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
      return res.status(401).json({ message: "Please log in to view tour appointments." });
    }

    const query =
      actor.role === "Admin" && req.query?.all === "true"
        ? {}
        : { userId: userId || String(actor._id) };

    const appointments = await TourAppointment.find(query)
      .populate("listingId")
      .sort({ tourDate: 1 });

    res.json(appointments);
  } catch (error) {
    console.error("Appointments GET error:", error);
    res.status(500).json({ message: "Unable to load appointments." });
  }
});

router.post("/", async (req, res) => {
  try {
    const body = req.body || {};
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const listingId = cleanId(body.listingId);
    const visitorName = body.visitorName || "";
    const visitorEmail = body.visitorEmail || "";
    const tourDate = body.tourDate || "";
    const status = body.status || "Requested";
    const note = body.note || "";

    if (!actor || String(actor._id) !== String(userId)) {
      return res.status(403).json({ message: "Please log in to schedule a tour." });
    }

    if (!userId || !listingId || !visitorName || !visitorEmail || !tourDate) {
      return res.status(400).json({
        message: "User, listing, visitor name, email, and date are required."
      });
    }

    if (!mongoose.Types.ObjectId.isValid(listingId)) {
      return res.status(400).json({ message: "Choose a valid listing before scheduling a tour." });
    }

    const listing = await Listing.findById(listingId);

    if (!listing) {
      return res.status(404).json({ message: "Listing not found." });
    }

    const appointment = await TourAppointment.create({
      userId,
      listingId,
      visitorName,
      visitorEmail,
      tourDate,
      status,
      note
    });

    const savedAppointment = await appointment.populate("listingId");

    res.status(201).json(savedAppointment);
  } catch (error) {
    console.error("Appointments POST error:", error);
    res.status(500).json({ message: "Unable to create appointment." });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const body = req.body || {};
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const appointment = await TourAppointment.findById(req.params.id);

    if (!appointment) {
      return res.status(404).json({ message: "Appointment not found." });
    }

    if (!canUseRecord(actor, appointment, userId)) {
      return res.status(403).json({ message: "You can only update your own appointments." });
    }

    if (body.visitorName !== undefined) {
      appointment.visitorName = body.visitorName;
    }

    if (body.visitorEmail !== undefined) {
      appointment.visitorEmail = body.visitorEmail;
    }

    if (body.tourDate !== undefined) {
      appointment.tourDate = body.tourDate;
    }

    if (body.status !== undefined) {
      appointment.status = body.status;
    }

    if (body.note !== undefined) {
      appointment.note = body.note;
    }

    await appointment.save();
    await appointment.populate("listingId");

    res.json(appointment);
  } catch (error) {
    console.error("Appointments PUT error:", error);
    res.status(500).json({ message: "Unable to update appointment." });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const userId = requestUserId(req);
    const actor = await getActor(req);
    const appointment = await TourAppointment.findById(req.params.id);

    if (!appointment) {
      return res.status(404).json({ message: "Appointment not found." });
    }

    if (!canUseRecord(actor, appointment, userId)) {
      return res.status(403).json({ message: "You can only delete your own appointments." });
    }

    await TourAppointment.findByIdAndDelete(req.params.id);

    res.json({ message: "Appointment removed." });
  } catch (error) {
    console.error("Appointments DELETE error:", error);
    res.status(500).json({ message: "Unable to remove appointment." });
  }
});

export default router;