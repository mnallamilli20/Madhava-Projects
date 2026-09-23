import mongoose from "mongoose";

const listingSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: true,
      trim: true
    },
    address: {
      type: String,
      required: true,
      trim: true
    },
    neighborhood: {
      type: String,
      required: true,
      trim: true
    },
    rent: {
      type: Number,
      required: true,
      min: 0
    },
    bedrooms: {
      type: Number,
      required: true,
      min: 0
    },
    bathrooms: {
      type: Number,
      required: true,
      min: 0
    },
    squareFeet: {
      type: Number,
      default: 0,
      min: 0
    },
    propertyType: {
      type: String,
      enum: ["Apartment", "House", "Townhome", "Studio"],
      default: "Apartment"
    },
    availableDate: {
      type: String,
      default: "Fall 2026"
    },
    description: {
      type: String,
      default: "",
      trim: true
    },
    amenities: {
      type: [String],
      default: []
    },
    imageUrl: {
      type: String,
      default: "",
      trim: true
    },
    contactEmail: {
      type: String,
      default: "leasing@example.com",
      trim: true
    },
    status: {
      type: String,
      enum: ["Available", "Pending", "Rented"],
      default: "Available"
    }
  },
  {
    timestamps: true
  }
);

listingSchema.index({ title: "text", address: "text", neighborhood: "text" });

const Listing = mongoose.model("Listing", listingSchema);

export default Listing;
