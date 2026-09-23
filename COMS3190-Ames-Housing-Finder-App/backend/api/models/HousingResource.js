import mongoose from "mongoose";

const housingResourceSchema = new mongoose.Schema(
  {
    category: {
      type: String,
      enum: ["FAQ", "Budget", "Lease", "Moving", "Campus"],
      default: "FAQ"
    },
    title: {
      type: String,
      required: true,
      trim: true
    },
    body: {
      type: String,
      required: true,
      trim: true
    },
    link: {
      type: String,
      default: "",
      trim: true
    },
    isFeatured: {
      type: Boolean,
      default: false
    }
  },
  {
    timestamps: true
  }
);

const HousingResource = mongoose.model("HousingResource", housingResourceSchema);

export default HousingResource;
