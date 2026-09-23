import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import ListingCard from "../components/ListingCard";
import { userPayload } from "../services/session";

function Home({ currentUser }) {
  const [listings, setListings] = useState([]);
  const [resources, setResources] = useState([]);
  const [message, setMessage] = useState("");

  async function loadHomeData() {
    try {
      const [listingsResponse, resourcesResponse] = await Promise.all([
        api.get("/listings?status=Available&sort=newest"),
        api.get("/resources")
      ]);
      setListings(listingsResponse.data.slice(0, 3));
      setResources(resourcesResponse.data.filter((resource) => resource.isFeatured).slice(0, 3));
    } catch (error) {
      setMessage("Home data could not be loaded. Start the backend and check MongoDB.");
    }
  }

  async function saveFavorite(listingId) {
    if (!currentUser) {
      setMessage("Please log in to save favorites.");
      return;
    }

    try {
      await api.post("/favorites", {
        ...userPayload(currentUser),
        listingId,
        priority: "Medium",
        label: "From home page",
        note: "Saved from the featured listings section."
      });
      setMessage("Listing saved to favorites.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be saved.");
    }
  }

  async function addToCompare(listingId) {
    if (!currentUser) {
      setMessage("Please log in to compare listings.");
      return;
    }

    try {
      await api.post("/compare", {
        ...userPayload(currentUser),
        listingId,
        compareNote: "Added from home page."
      });
      setMessage("Listing added to compare.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be added to compare.");
    }
  }

  useEffect(() => {
    loadHomeData();
  }, []);

  return (
    <div>
      <section className="hero-section">
        <div className="container py-5">
          <div className="row align-items-center g-4">
            <div className="col-lg-7">
              <p className="eyebrow">Student housing search for Ames</p>
              <h1 className="display-5 fw-bold mb-3">Find, save, compare, and tour housing near Iowa State.</h1>
              <p className="lead text-muted mb-4">
                Guests can browse Home, Listings, Listing Details, and Info / FAQ. Students can use all pages and manage their own saved data and reviews. Admins can manage the app, but cannot edit reviews written by other users.
              </p>
              <div className="d-flex gap-2 flex-wrap">
                <Link className="btn btn-primary btn-lg" to="/listings">Browse Listings</Link>
                {currentUser ? (
                  <Link className="btn btn-outline-dark btn-lg" to="/dashboard">Open Dashboard</Link>
                ) : (
                  <Link className="btn btn-outline-dark btn-lg" to="/login">Log In</Link>
                )}
              </div>
            </div>
            <div className="col-lg-5">
              <div className="card shadow border-0 summary-card">
                <div className="card-body p-4">
                  <h2 className="h5 mb-3">Role summary</h2>
                  <div className="list-group list-group-flush">
                    <div className="list-group-item px-0"><strong>Guest:</strong> Home, Listings, Listing Details, Info / FAQ</div>
                    <div className="list-group-item px-0"><strong>Student:</strong> all pages, but no listing edit/delete and no editing or deleting other users’ reviews</div>
                    <div className="list-group-item px-0"><strong>Admin:</strong> full app management except editing reviews written by other users</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="container py-4">
        {message && <div className="alert alert-info">{message}</div>}

        <section className="mb-5">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h2 className="fw-bold mb-1">Featured available listings</h2>
              <p className="text-muted mb-0">These cards are loaded from MongoDB through the backend listings API.</p>
            </div>
            <Link className="btn btn-outline-primary" to="/listings">See All</Link>
          </div>

          <div className="row g-3">
            {listings.map((listing) => (
              <div className="col-md-6 col-xl-4" key={listing._id}>
                <ListingCard listing={listing} onFavorite={currentUser ? saveFavorite : null} onCompare={currentUser ? addToCompare : null} />
              </div>
            ))}
          </div>
        </section>

        {currentUser && (
          <section className="row g-3">
            {resources.map((resource) => (
              <div className="col-md-4" key={resource._id}>
                <div className="card h-100 shadow-sm border-0">
                  <div className="card-body">
                    <span className="badge text-bg-light border mb-2">{resource.category}</span>
                    <h3 className="h5">{resource.title}</h3>
                    <p className="text-muted mb-0">{resource.body}</p>
                  </div>
                </div>
              </div>
            ))}
          </section>
        )}
      </div>
    </div>
  );
}

export default Home;
