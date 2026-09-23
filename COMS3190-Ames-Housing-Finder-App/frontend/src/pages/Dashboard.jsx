import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

function Dashboard({ currentUser }) {
  const [dashboard, setDashboard] = useState(null);
  const [message, setMessage] = useState("");

  async function loadDashboard() {
    if (!currentUser) {
      setDashboard(null);
      return;
    }

    try {
      const response = await api.get(`/dashboard?actorId=${encodeURIComponent(currentUser._id)}`);
      setDashboard(response.data);
    } catch (error) {
      setMessage(error.response?.data?.message || "Dashboard could not be loaded.");
    }
  }

  useEffect(() => {
    loadDashboard();
  }, [currentUser]);

  if (!currentUser) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">The dashboard shows account-specific activity. Please log in to view it.</p>
          <Link className="btn btn-primary" to="/login">Go to Login</Link>
        </div>
      </div>
    );
  }

  const counts = dashboard?.counts || {};
  const recentFavorites = dashboard?.recentFavorites || [];
  const recentAppointments = dashboard?.recentAppointments || [];
  const featuredResources = dashboard?.featuredResources || [];

  return (
    <div className="container py-4">
      <div className="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-md-end mb-4">
        <div>
          <p className="eyebrow mb-1">Account dashboard</p>
          <h1 className="fw-bold mb-1">Welcome, {currentUser.name}</h1>
          <p className="text-muted mb-0">Track listings, favorites, compare items, tours, reviews, and resources from one database-backed page. Students can view all pages, while admin-only controls stay protected.</p>
        </div>
        <Link className="btn btn-primary" to="/listings">
          Browse Listings
        </Link>
      </div>

      {message && <div className="alert alert-danger">{message}</div>}

      <div className="row g-3 mb-4">
        {[
          ["Listings", counts.listings || 0, "/listings"],
          ["Favorites", counts.favorites || 0, "/favorites"],
          ["Compared", counts.compare || 0, "/compare"],
          ["Tours", counts.appointments || 0, "/appointments"],
          ["Reviews", counts.reviews || 0, "/reviews"],
          ["Resources", counts.resources || 0, "/resources"]
        ].map(([label, value, path]) => (
          <div className="col-6 col-lg" key={label}>
            <Link className="text-decoration-none" to={path}>
              <div className="metric-box h-100 text-start">
                <span>{value}</span>
                <small>{label}</small>
              </div>
            </Link>
          </div>
        ))}
      </div>

      <div className="row g-4">
        <div className="col-lg-4">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-body">
              <h2 className="h5 mb-3">Recent favorites</h2>
              {recentFavorites.length === 0 ? (
                <p className="text-muted mb-0">No favorites saved yet.</p>
              ) : (
                <div className="list-group list-group-flush">
                  {recentFavorites.map((favorite) => (
                    <div className="list-group-item px-0" key={favorite._id}>
                      <h3 className="h6 mb-1">{favorite.listingId?.title || "Deleted listing"}</h3>
                      <p className="small text-muted mb-0">{favorite.note || "No note added."}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-body">
              <h2 className="h5 mb-3">Upcoming tours</h2>
              {recentAppointments.length === 0 ? (
                <p className="text-muted mb-0">No tour appointments scheduled yet.</p>
              ) : (
                <div className="list-group list-group-flush">
                  {recentAppointments.map((appointment) => (
                    <div className="list-group-item px-0" key={appointment._id}>
                      <h3 className="h6 mb-1">{appointment.listingId?.title || "Deleted listing"}</h3>
                      <p className="small text-muted mb-0">{appointment.tourDate}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-body">
              <h2 className="h5 mb-3">Featured resources</h2>
              {featuredResources.length === 0 ? (
                <p className="text-muted mb-0">No featured resources yet.</p>
              ) : (
                <div className="list-group list-group-flush">
                  {featuredResources.map((resource) => (
                    <div className="list-group-item px-0" key={resource._id}>
                      <h3 className="h6 mb-1">{resource.title}</h3>
                      <p className="small text-muted mb-0">{resource.category}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
