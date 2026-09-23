# AL_8 Ames Housing Finder

Team members:

- Vikrant Gandotra, gandotra@iastate.edu
- Madhava Nallamilli, mnalla23@iastate.edu

Ames Housing Finder is a React, Node.js, Express, and MongoDB single page app for browsing housing listings near Iowa State, saving favorites, comparing listings, scheduling tours, writing reviews, and managing housing information.

## Role behavior

- Guests can view Home, Listings, Listing Details, and Info / FAQ. Login and Signup remain available so guests can enter the app.
- Students can view every page, save favorites, compare listings, schedule tours, create reviews, and edit or delete only their own reviews. They cannot create, edit, or delete listings.
- Admins can manage listings, resources, tours, favorites, and compare records. Admins can delete reviews for moderation but cannot edit reviews written by other users.
- Public signup always creates a Student account. Admin accounts must be seeded or created directly in the database.

## Setup

Open two terminals from the project root.

Backend:

```bash
cd backend
npm install
copy .env.example .env
npm run seed
npm run dev
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Open the Vite URL shown in the frontend terminal, usually http://localhost:5173.

## Seeded demo accounts

Student account:

- Email: gandotra@iastate.edu
- Password: password123

Admin account:

- Email: admin@iastate.edu
- Password: password123

## Required local tools

- Node.js
- MongoDB running locally at mongodb://127.0.0.1:27017/amesHousingFinder, or update backend/.env to your MongoDB Atlas connection string.

## Main folders

- frontend: React SPA
- backend: Express API and MongoDB models
- Documents: report, video script, and demo checklist drafts
