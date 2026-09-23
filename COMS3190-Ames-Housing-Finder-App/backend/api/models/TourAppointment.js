import mongoose from "mongoose";

const tourAppointmentSchema = new mongoose.Schema(
  {
    userId: {
      type: String,
      required: true,
      trim: true
    },
    listingId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Listing",
      required: true
    },
    visitorName: {
      type: String,
      required: true,
      trim: true
    },
    visitorEmail: {
      type: String,
      required: true,
      trim: true
    },
    tourDate: {
      type: String,
      required: true
    },
    status: {
      type: String,
      enum: ["Requested", "Confirmed", "Completed", "Canceled"],
      default: "Requested"
    },
    note: {
      type: String,
      default: "",
      trim: true
    }
  },
  {
    timestamps: true
  }
);

const TourAppointment = mongoose.model(
  "TourAppointment",
  tourAppointmentSchema
);

export default TourAppointment;
