import dotenv from "dotenv";
import mongoose from "mongoose";
import crypto from "crypto";
import Listing from "./models/Listing.js";
import Favorite from "./models/Favorite.js";
import CompareSelection from "./models/CompareSelection.js";
import TourAppointment from "./models/TourAppointment.js";
import ListingReview from "./models/ListingReview.js";
import HousingResource from "./models/HousingResource.js";
import User from "./models/User.js";

if (!process.env.MONGO_URI) {
  dotenv.config();
}

function hashPassword(password) {
  return crypto.createHash("sha256").update(password).digest("hex");
}

export async function seedDatabase() {
  await Promise.all([
    Listing.deleteMany({}),
    Favorite.deleteMany({}),
    CompareSelection.deleteMany({}),
    TourAppointment.deleteMany({}),
    ListingReview.deleteMany({}),
    HousingResource.deleteMany({}),
    User.deleteMany({})
  ]);

  const [student, admin] = await User.insertMany([
    {
      name: "Vikrant Gandotra",
      email: "gandotra@iastate.edu",
      passwordHash: hashPassword("password123"),
      role: "Student"
    },
    {
      name: "Admin User",
      email: "admin@iastate.edu",
      passwordHash: hashPassword("password123"),
      role: "Admin"
    }
  ]);

  const listings = await Listing.insertMany([
    {
      title: "Campus View Apartments",
      address: "120 Lincoln Way, Ames, IA",
      neighborhood: "Campustown",
      rent: 875,
      bedrooms: 2,
      bathrooms: 1,
      squareFeet: 780,
      propertyType: "Apartment",
      availableDate: "August 2026",
      description: "Walkable student apartment close to Iowa State classes, dining, and CyRide stops.",
      amenities: ["Laundry", "Parking", "CyRide", "Pet Friendly"],
      imageUrl: "",
      contactEmail: "campusview@example.com",
      status: "Available"
    },
    {
      title: "South Duff Townhome",
      address: "415 S Duff Ave, Ames, IA",
      neighborhood: "South Ames",
      rent: 1325,
      bedrooms: 3,
      bathrooms: 2,
      squareFeet: 1280,
      propertyType: "Townhome",
      availableDate: "July 2026",
      description: "Larger townhome with garage access and quick routes to grocery stores and restaurants.",
      amenities: ["Garage", "Washer Dryer", "Dishwasher", "Back Patio"],
      imageUrl: "",
      contactEmail: "southduff@example.com",
      status: "Available"
    },
    {
      title: "West Ames Studio",
      address: "2210 Mortensen Rd, Ames, IA",
      neighborhood: "West Ames",
      rent: 690,
      bedrooms: 0,
      bathrooms: 1,
      squareFeet: 430,
      propertyType: "Studio",
      availableDate: "June 2026",
      description: "Budget-friendly studio for students who want a simple private space near west campus.",
      amenities: ["Internet", "Parking", "Fitness Room"],
      imageUrl: "",
      contactEmail: "weststudio@example.com",
      status: "Available"
    },
    {
      title: "North Ames House",
      address: "3128 Hoover Ave, Ames, IA",
      neighborhood: "North Ames",
      rent: 1650,
      bedrooms: 4,
      bathrooms: 2,
      squareFeet: 1760,
      propertyType: "House",
      availableDate: "August 2026",
      description: "Shared student house with a yard, extra storage, and space for roommates.",
      amenities: ["Yard", "Basement", "Washer Dryer", "Parking"],
      imageUrl: "",
      contactEmail: "northhouse@example.com",
      status: "Pending"
    },
    {
      title: "Downtown Ames Loft",
      address: "504 Main St, Ames, IA",
      neighborhood: "Downtown",
      rent: 1125,
      bedrooms: 1,
      bathrooms: 1,
      squareFeet: 700,
      propertyType: "Apartment",
      availableDate: "May 2026",
      description: "Updated loft near downtown restaurants, coffee shops, and local events.",
      amenities: ["Elevator", "Internet", "Dishwasher", "Secure Entry"],
      imageUrl: "",
      contactEmail: "downtownloft@example.com",
      status: "Available"
    }
  ]);

  await Favorite.insertMany([
    {
      userId: String(student._id),
      listingId: listings[0]._id,
      note: "Best location for walking to class.",
      label: "Top choice",
      priority: "High"
    },
    {
      userId: String(student._id),
      listingId: listings[2]._id,
      note: "Affordable option if I live alone.",
      label: "Budget",
      priority: "Medium"
    }
  ]);

  await CompareSelection.insertMany([
    {
      userId: String(student._id),
      listingId: listings[0]._id,
      compareNote: "Great distance to campus."
    },
    {
      userId: String(student._id),
      listingId: listings[4]._id,
      compareNote: "Better downtown location."
    }
  ]);

  await TourAppointment.insertMany([
    {
      userId: String(student._id),
      listingId: listings[0]._id,
      visitorName: student.name,
      visitorEmail: student.email,
      tourDate: "2026-05-09T15:30",
      status: "Confirmed",
      note: "Ask about parking and lease start date."
    },
    {
      userId: String(student._id),
      listingId: listings[4]._id,
      visitorName: student.name,
      visitorEmail: student.email,
      tourDate: "2026-05-10T11:00",
      status: "Requested",
      note: "Check noise level and internet options."
    }
  ]);

  await ListingReview.insertMany([
    {
      userId: String(student._id),
      listingId: listings[0]._id,
      authorName: "Vikrant",
      rating: 5,
      title: "Very convenient",
      comment: "The location is the strongest part because it is close to campus and transit."
    },
    {
      userId: String(student._id),
      listingId: listings[2]._id,
      authorName: "Vikrant",
      rating: 4,
      title: "Good value",
      comment: "The rent is low compared with other options, but the space is smaller."
    },
    {
      userId: String(admin._id),
      listingId: listings[1]._id,
      authorName: "Admin User",
      rating: 5,
      title: "Large and useful",
      comment: "This is a good example review for admin testing."
    }
  ]);

  await HousingResource.insertMany([
    {
      category: "FAQ",
      title: "What does Ames Housing Finder do?",
      body: "It helps students browse listings, save favorites, compare choices, schedule tours, and review housing options.",
      isFeatured: true
    },
    {
      category: "Budget",
      title: "Compare rent with utilities",
      body: "A lower listed rent can still be more expensive if internet, parking, or utilities are not included.",
      isFeatured: true
    },
    {
      category: "Lease",
      title: "Check lease dates before signing",
      body: "Students should confirm move-in date, renewal rules, subleasing rules, pet policies, and maintenance expectations.",
      isFeatured: true
    },
    {
      category: "Moving",
      title: "Plan move-in early",
      body: "Before moving, confirm keys, parking permits, utilities, and roommate responsibilities.",
      isFeatured: false
    },
    {
      category: "Campus",
      title: "Think about CyRide access",
      body: "Transit access can matter as much as distance, especially during winter or early morning classes.",
      isFeatured: false
    }
  ]);

  console.log(`Seeded ${listings.length} listings, student ${student.email}, and admin ${admin.email}`);
}

async function runSeed() {
  await mongoose.connect(process.env.MONGO_URI);
  await seedDatabase();
  await mongoose.disconnect();
}

if (process.argv[1]?.endsWith("seed.js")) {
  runSeed().catch(async (error) => {
    console.error(error.message);
    await mongoose.disconnect();
    process.exit(1);
  });
}
