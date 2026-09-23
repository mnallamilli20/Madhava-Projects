import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import connectDB from "./config/db.js";
import authRoutes from "./api/routes/auth.js";
import listingsRoutes from "./api/routes/listings.js";
import favoritesRoutes from "./api/routes/favorites.js";
import compareRoutes from "./api/routes/compare.js";
import appointmentsRoutes from "./api/routes/appointments.js";
import reviewsRoutes from "./api/routes/reviews.js";
import resourcesRoutes from "./api/routes/resources.js";
import dashboardRoutes from "./api/routes/dashboard.js";

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json());

connectDB();

app.get("/api/health", (req, res) => {
  res.json({
    status: "ok",
    service: "Ames Housing Finder API"
  });
});

app.use("/api/auth", authRoutes);
app.use("/api/listings", listingsRoutes);
app.use("/api/favorites", favoritesRoutes);
app.use("/api/compare", compareRoutes);
app.use("/api/appointments", appointmentsRoutes);
app.use("/api/reviews", reviewsRoutes);
app.use("/api/resources", resourcesRoutes);
app.use("/api/dashboard", dashboardRoutes);

app.use((req, res) => {
  res.status(404).json({
    message: "Route not found."
  });
});

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
