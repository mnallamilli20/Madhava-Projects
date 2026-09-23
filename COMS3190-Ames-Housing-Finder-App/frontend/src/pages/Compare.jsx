import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import { userPayload, userQuery } from "../services/session";

function normalizeArray(data) {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.compareSelections)) {
    return data.compareSelections;
  }

  if (Array.isArray(data?.selections)) {
    return data.selections;
  }

  if (Array.isArray(data?.data)) {
    return data.data;
  }

  return [];
}

function findListing(listings, listingId) {
  return listings.find((listing) => String(listing._id) === String(listingId));
}

function Compare({ currentUser }) {
  const [selections, setSelections] = useState([]);
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState({
    listingId: "",
    compareNote: ""
  });
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState("");

  async function loadListings() {
    try {
      const listingsResponse = await api.get("/listings");
      setListings(normalizeArray(listingsResponse.data));
    } catch (error) {
      setListings([]);
      setMessage(error.response?.data?.message || "Unable to load listings for compare.");
    }
  }

  async function loadSelections() {
    if (!currentUser?._id) {
      setSelections([]);
      return;
    }

    try {
      const compareResponse = await api.get(`/compare?${userQuery(currentUser)}`);
      setSelections(normalizeArray(compareResponse.data));
    } catch (error) {
      setSelections([]);
      setMessage(error.response?.data?.message || "Unable to load compare selections.");
    }
  }

  async function loadData() {
    setMessage("");
    await loadListings();
    await loadSelections();
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

  function startEdit(selection) {
    setEditing(selection);
    setForm({
      listingId: selection.listingId?._id || selection.listingId || "",
      compareNote: selection.compareNote || ""
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditing(null);
    setForm({
      listingId: "",
      compareNote: ""
    });
  }

  async function submitSelection(event) {
    event.preventDefault();

    if (!currentUser?._id) {
      setMessage("Please log in to compare listings.");
      return;
    }

    if (!form.listingId) {
      setMessage("Choose a listing before adding it to compare.");
      return;
    }

    try {
      if (editing) {
        const response = await api.put(`/compare/${editing._id}`, {
          ...userPayload(currentUser),
          compareNote: form.compareNote
        });

        const updatedSelection = response.data;

        setSelections((currentSelections) =>
          currentSelections.map((selection) =>
            selection._id === editing._id ? updatedSelection : selection
          )
        );

        setMessage("Compare note updated.");
      } else {
        const response = await api.post("/compare", {
          ...userPayload(currentUser),
          listingId: form.listingId,
          compareNote: form.compareNote
        });

        const createdSelection = response.data;
        const selectedListing = findListing(listings, form.listingId);

        setSelections((currentSelections) => [
          {
            ...createdSelection,
            listingId: createdSelection.listingId || selectedListing
          },
          ...currentSelections
        ]);

        setMessage("Listing added to compare.");
      }

      resetForm();
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Compare selection could not be saved.");
    }
  }

  async function deleteSelection(id) {
    try {
      await api.delete(`/compare/${id}`, {
        data: userPayload(currentUser)
      });

      setSelections((currentSelections) =>
        currentSelections.filter((selection) => selection._id !== id)
      );

      setMessage("Compare selection removed.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Compare selection could not be removed.");
    }
  }

  if (!currentUser?._id) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">
            Compare selections are tied to an account. Please log in to add, update, and remove comparison records.
          </p>
          <Link className="btn btn-primary" to="/login">
            Go to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Full CRUD feature</p>
        <h1 className="fw-bold">Compare Listings</h1>
        <p className="text-muted mb-0">
          Add up to three listings, update comparison notes, and remove listings from your compare list.
        </p>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      <form className="card shadow-sm border-0 mb-4" onSubmit={submitSelection}>
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h5 mb-0">{editing ? "Edit compare note" : "Add listing to compare"}</h2>
            {editing && (
              <button className="btn btn-outline-secondary btn-sm" type="button" onClick={resetForm}>
                Cancel Edit
              </button>
            )}
          </div>

          <div className="row g-3">
            <div className="col-md-5">
              <label className="form-label">Listing</label>
              <select
                className="form-select"
                name="listingId"
                value={form.listingId}
                onChange={updateField}
                required
                disabled={Boolean(editing)}
              >
                <option value="">Choose listing</option>
                {listings.map((listing) => (
                  <option value={listing._id} key={listing._id}>
                    {listing.title}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-7">
              <label className="form-label">Compare Note</label>
              <input
                className="form-control"
                name="compareNote"
                value={form.compareNote}
                onChange={updateField}
                placeholder="Why compare this option?"
              />
            </div>
          </div>

          <button className="btn btn-primary mt-3" type="submit" disabled={listings.length === 0}>
            {editing ? "Update Compare Note" : "Add to Compare"}
          </button>
        </div>
      </form>

      {selections.length === 0 ? (
        <div className="alert alert-light border">No listings are currently in your compare list.</div>
      ) : (
        <div className="row g-3">
          {selections.map((selection) => {
            const listing = selection.listingId;

            return (
              <div className="col-md-4" key={selection._id}>
                <div className="card h-100 shadow-sm border-0 compare-card">
                  <div className="card-body">
                    <span className="badge text-bg-primary mb-2">{listing?.status || "Unavailable"}</span>
                    <h2 className="h5">{listing?.title || "Deleted listing"}</h2>
                    <p className="text-muted">{listing?.address || "Listing no longer exists"}</p>

                    {listing && (
                      <div className="table-responsive">
                        <table className="table table-sm align-middle">
                          <tbody>
                            <tr>
                              <th>Rent</th>
                              <td>${listing.rent}</td>
                            </tr>
                            <tr>
                              <th>Beds</th>
                              <td>{listing.bedrooms}</td>
                            </tr>
                            <tr>
                              <th>Baths</th>
                              <td>{listing.bathrooms}</td>
                            </tr>
                            <tr>
                              <th>Area</th>
                              <td>{listing.neighborhood}</td>
                            </tr>
                            <tr>
                              <th>Type</th>
                              <td>{listing.propertyType}</td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    )}

                    <p className="text-muted">{selection.compareNote || "No compare note added."}</p>

                    <div className="d-flex gap-2">
                      <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(selection)}>
                        Edit Note
                      </button>
                      <button className="btn btn-outline-danger btn-sm" onClick={() => deleteSelection(selection._id)}>
                        Remove
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default Compare;