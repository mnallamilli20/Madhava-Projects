import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import { userPayload, userQuery } from "../services/session";

function Favorites({ currentUser }) {
  const [favorites, setFavorites] = useState([]);
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState({
    listingId: "",
    note: "",
    label: "",
    priority: "Medium"
  });
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState("");

  async function loadData() {
    if (!currentUser?._id) {
      setFavorites([]);
      setListings([]);
      return;
    }

    setMessage("");

    try {
      const listingsResponse = await api.get("/listings");
      setListings(listingsResponse.data);
    } catch (error) {
      setListings([]);
      setMessage(error.response?.data?.message || "Unable to load listings for favorites.");
    }

    try {
      const favoritesResponse = await api.get(`/favorites?${userQuery(currentUser)}`);
      setFavorites(favoritesResponse.data);
    } catch (error) {
      setFavorites([]);
      setMessage(error.response?.data?.message || "Unable to load favorites.");
    }
  }

  useEffect(() => {
    loadData();
  }, [currentUser]);

  function updateField(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  }

  function startEdit(favorite) {
    setEditing(favorite);
    setForm({
      listingId: favorite.listingId?._id || favorite.listingId || "",
      note: favorite.note || "",
      label: favorite.label || "",
      priority: favorite.priority || "Medium"
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditing(null);
    setForm({
      listingId: "",
      note: "",
      label: "",
      priority: "Medium"
    });
  }

  async function submitFavorite(event) {
    event.preventDefault();

    if (!currentUser?._id) {
      setMessage("Please log in to save favorites.");
      return;
    }

    try {
      if (editing) {
        await api.put(`/favorites/${editing._id}`, {
          ...form,
          ...userPayload(currentUser)
        });
        setMessage("Favorite updated.");
      } else {
        await api.post("/favorites", {
          ...form,
          ...userPayload(currentUser)
        });
        setMessage("Favorite saved.");
      }

      resetForm();
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Favorite could not be saved.");
    }
  }

  async function togglePriority(favorite) {
    const nextPriority = favorite.priority === "High" ? "Medium" : "High";

    try {
      await api.put(`/favorites/${favorite._id}`, {
        ...userPayload(currentUser),
        note: favorite.note,
        label: favorite.label,
        priority: nextPriority
      });
      setMessage("Favorite priority updated.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Favorite could not be updated.");
    }
  }

  async function deleteFavorite(id) {
    try {
      await api.delete(`/favorites/${id}`, {
        data: userPayload(currentUser)
      });
      setMessage("Favorite removed.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Favorite could not be removed.");
    }
  }

  if (!currentUser?._id) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">Favorites are personalized records. Please log in to save, update, and delete your favorite listings.</p>
          <Link className="btn btn-primary" to="/login">Go to Login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Full CRUD feature</p>
        <h1 className="fw-bold">Favorites</h1>
        <p className="text-muted mb-0">Save, update, prioritize, and remove your saved housing listings.</p>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      <form className="card shadow-sm border-0 mb-4" onSubmit={submitFavorite}>
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h5 mb-0">{editing ? "Edit favorite" : "Save a listing"}</h2>
            {editing && (
              <button className="btn btn-outline-secondary btn-sm" type="button" onClick={resetForm}>
                Cancel Edit
              </button>
            )}
          </div>

          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label">Listing</label>
              <select className="form-select" name="listingId" value={form.listingId} onChange={updateField} required disabled={Boolean(editing)}>
                <option value="">Choose listing</option>
                {listings.map((listing) => (
                  <option value={listing._id} key={listing._id}>{listing.title}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Label</label>
              <input className="form-control" name="label" value={form.label} onChange={updateField} placeholder="Top choice" />
            </div>
            <div className="col-md-2">
              <label className="form-label">Priority</label>
              <select className="form-select" name="priority" value={form.priority} onChange={updateField}>
                <option>Low</option>
                <option>Medium</option>
                <option>High</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Note</label>
              <input className="form-control" name="note" value={form.note} onChange={updateField} />
            </div>
          </div>

          <button className="btn btn-primary mt-3" type="submit">
            {editing ? "Update Favorite" : "Save Listing"}
          </button>
        </div>
      </form>

      {favorites.length === 0 ? (
        <div className="alert alert-light border">No listings are currently saved in your favorites.</div>
      ) : (
        <div className="row g-3">
          {favorites.map((favorite) => (
            <div className="col-md-6" key={favorite._id}>
              <div className="card h-100 shadow-sm border-0">
                <div className="card-body">
                  <div className="d-flex justify-content-between gap-3">
                    <div>
                      <h2 className="h5 mb-1">{favorite.listingId?.title || "Deleted listing"}</h2>
                      <p className="text-muted mb-2">{favorite.listingId?.address || "Listing no longer exists"}</p>
                    </div>
                    <span className="badge text-bg-info align-self-start">{favorite.priority}</span>
                  </div>
                  <p className="mb-1"><strong>Label:</strong> {favorite.label || "None"}</p>
                  <p className="text-muted">{favorite.note || "No note added."}</p>
                  <div className="d-flex flex-wrap gap-2">
                    {favorite.listingId?._id && <Link className="btn btn-outline-dark btn-sm" to={`/listings/${favorite.listingId._id}`}>Details</Link>}
                    <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(favorite)}>Edit</button>
                    <button className="btn btn-outline-secondary btn-sm" onClick={() => togglePriority(favorite)}>Toggle High</button>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => deleteFavorite(favorite._id)}>Remove</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Favorites;
