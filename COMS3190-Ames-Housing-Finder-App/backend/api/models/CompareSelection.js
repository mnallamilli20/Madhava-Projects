import mongoose from "mongoose";

const compareSelectionSchema = new mongoose.Schema(
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
    compareNote: {
      type: String,
      default: "",
      trim: true
    }
  },
  {
    timestamps: true
  }
);

compareSelectionSchema.index({ userId: 1, listingId: 1 }, { unique: true });

const CompareSelection = mongoose.model(
  "CompareSelection",
  compareSelectionSchema
);

export default CompareSelection;