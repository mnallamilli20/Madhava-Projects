import { useEffect, useMemo, useState } from "react";
import api from "../services/api";
import ListingCard from "../components/ListingCard";
import { authPayload, canManageListings, userPayload } from "../services/session";

const emptyListing = {
  title: "",
  address: "",
  neighborhood: "",
  rent: "",
  bedrooms: "1",
  bathrooms: "1",
  squareFeet: "",
  propertyType: "Apartment",
  availableDate: "August 2026",
  description: "",
  amenities: "",
  imageUrl: "",
  contactEmail: "leasing@example.com",
  status: "Available"
};

function Listings({ currentUser }) {
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState(emptyListing);
  const [editingId, setEditingId] = useState("");
  const [message, setMessage] = useState("");
  const [filters, setFilters] = useState({
    search: "",
    minRent: "",
    maxRent: "",
    bedrooms: "",
    propertyType: "",
    status: "",
    sort: "newest"
  });

  const adminCanManage = canManageListings(currentUser);

  const queryString = useMemo(() => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        params.append(key, value);
      }
    });

    return params.toString();
  }, [filters]);

  async function loadListings() {
    try {
      const response = await api.get(`/listings${queryString ? `?${queryString}` : ""}`);
      setListings(response.data);
    } catch (error) {
      setMessage("Listings could not be loaded.");
    }
  }

  useEffect(() => {
    loadListings();
  }, [queryString]);

  function updateField(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  }

  function updateFilter(event) {
    setFilters({
      ...filters,
      [event.target.name]: event.target.value
    });
  }

  function startEdit(listing) {
    if (!adminCanManage) {
      setMessage("Only admins can edit listings.");
      return;
    }

    setEditingId(listing._id);
    setForm({
      title: listing.title || "",
      address: listing.address || "",
      neighborhood: listing.neighborhood || "",
      rent: listing.rent || "",
      bedrooms: listing.bedrooms || "1",
      bathrooms: listing.bathrooms || "1",
      squareFeet: listing.squareFeet || "",
      propertyType: listing.propertyType || "Apartment",
      availableDate: listing.availableDate || "August 2026",
      description: listing.description || "",
      amenities: (listing.amenities || []).join(", "),
      imageUrl: listing.imageUrl || "",
      contactEmail: listing.contactEmail || "leasing@example.com",
      status: listing.status || "Available"
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditingId("");
    setForm(emptyListing);
  }

  async function submitListing(event) {
    event.preventDefault();

    if (!adminCanManage) {
      setMessage("Only admins can create or update listings.");
      return;
    }

    try {
      if (editingId) {
        await api.put(`/listings/${editingId}`, {
          ...form,
          ...authPayload(currentUser)
        });
        setMessage("Listing updated.");
      } else {
        await api.post("/listings", {
          ...form,
          ...authPayload(currentUser)
        });
        setMessage("Listing created.");
      }

      resetForm();
      await loadListings();
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be saved.");
    }
  }

  async function deleteListing(id) {
    if (!adminCanManage) {
      setMessage("Only admins can delete listings.");
      return;
    }

    if (!window.confirm("Remove this listing from the database?")) {
      return;
    }

    try {
      await api.delete(`/listings/${id}`, {
        data: authPayload(currentUser)
      });
      setMessage("Listing removed.");
      await loadListings();
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be removed.");
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
        label: "Listing page",
        note: "Saved from browse listings."
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
        compareNote: "Added from browse listings."
      });
      setMessage("Listing added to compare.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be added to compare.");
    }
  }

  return (
    <div className="container py-4">
      <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-end mb-4">
        <div>
          <p className="eyebrow mb-1">Public browsing and admin CRUD</p>
          <h1 className="fw-bold mb-1">Listings</h1>
          <p className="text-muted mb-0">
            Guests can browse listings and listing details. Students can save and compare listings. Admins can create, edit, and delete listings.
          </p>
        </div>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      {adminCanManage ? (
        <form className="card shadow-sm border-0 mb-4" onSubmit={submitListing}>
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center gap-3 mb-3">
              <h2 className="h5 mb-0">{editingId ? "Edit listing" : "Create listing"}</h2>
              {editingId && (
                <button className="btn btn-outline-secondary btn-sm" type="button" onClick={resetForm}>
                  Cancel Edit
                </button>
              )}
            </div>

            <div className="row g-3">
              <div className="col-md-4">
                <label className="form-label">Title</label>
                <input className="form-control" name="title" value={form.title} onChange={updateField} required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Address</label>
                <input className="form-control" name="address" value={form.address} onChange={updateField} required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Neighborhood</label>
                <input className="form-control" name="neighborhood" value={form.neighborhood} onChange={updateField} required />
              </div>
              <div className="col-md-2">
                <label className="form-label">Rent</label>
                <input className="form-control" name="rent" type="number" min="0" value={form.rent} onChange={updateField} required />
              </div>
              <div className="col-md-2">
                <label className="form-label">Bedrooms</label>
                <input className="form-control" name="bedrooms" type="number" min="0" value={form.bedrooms} onChange={updateField} required />
              </div>
              <div className="col-md-2">
                <label className="form-label">Bathrooms</label>
                <input className="form-control" name="bathrooms" type="number" min="0" step="0.5" value={form.bathrooms} onChange={updateField} required />
              </div>
              <div className="col-md-2">
                <label className="form-label">Square Feet</label>
                <input className="form-control" name="squareFeet" type="number" min="0" value={form.squareFeet} onChange={updateField} />
              </div>
              <div className="col-md-2">
                <label className="form-label">Type</label>
                <select className="form-select" name="propertyType" value={form.propertyType} onChange={updateField}>
                  <option>Apartment</option>
                  <option>House</option>
                  <option>Townhome</option>
                  <option>Studio</option>
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label">Status</label>
                <select className="form-select" name="status" value={form.status} onChange={updateField}>
                  <option>Available</option>
                  <option>Pending</option>
                  <option>Rented</option>
                </select>
              </div>
              <div className="col-md-3">
                <label className="form-label">Available Date</label>
                <input className="form-control" name="availableDate" value={form.availableDate} onChange={updateField} />
              </div>
              <div className="col-md-3">
                <label className="form-label">Contact Email</label>
                <input className="form-control" name="contactEmail" value={form.contactEmail} onChange={updateField} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Amenities</label>
                <input className="form-control" name="amenities" value={form.amenities} onChange={updateField} placeholder="Laundry, Parking, CyRide" />
              </div>
              <div className="col-12">
                <label className="form-label">Description</label>
                <textarea className="form-control" name="description" rows="2" value={form.description} onChange={updateField} />
              </div>
            </div>

            <button className="btn btn-primary mt-3" type="submit">
              {editingId ? "Update Listing" : "Create Listing"}
            </button>
          </div>
        </form>
      ) : (
        <div className="alert alert-light border mb-4">
          Listing management is admin-only. Guests and students can browse listings, and students can save or compare them after logging in.
        </div>
      )}

      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <h2 className="h5 mb-3">Search, filter, and sort</h2>
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">Search</label>
              <input className="form-control" name="search" value={filters.search} onChange={updateFilter} placeholder="Title, area, keyword" />
            </div>
            <div className="col-md-2">
              <label className="form-label">Min Rent</label>
              <input className="form-control" name="minRent" type="number" value={filters.minRent} onChange={updateFilter} />
            </div>
            <div className="col-md-2">
              <label className="form-label">Max Rent</label>
              <input className="form-control" name="maxRent" type="number" value={filters.maxRent} onChange={updateFilter} />
            </div>
            <div className="col-md-2">
              <label className="form-label">Bedrooms</label>
              <select className="form-select" name="bedrooms" value={filters.bedrooms} onChange={updateFilter}>
                <option value="">Any</option>
                <option value="1">1+</option>
                <option value="2">2+</option>
                <option value="3">3+</option>
                <option value="4">4+</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Sort</label>
              <select className="form-select" name="sort" value={filters.sort} onChange={updateFilter}>
                <option value="newest">Newest</option>
                <option value="rentAsc">Rent Low to High</option>
                <option value="rentDesc">Rent High to Low</option>
                <option value="bedrooms">Most Bedrooms</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Type</label>
              <select className="form-select" name="propertyType" value={filters.propertyType} onChange={updateFilter}>
                <option value="">Any</option>
                <option>Apartment</option>
                <option>House</option>
                <option>Townhome</option>
                <option>Studio</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Status</label>
              <select className="form-select" name="status" value={filters.status} onChange={updateFilter}>
                <option value="">Any</option>
                <option>Available</option>
                <option>Pending</option>
                <option>Rented</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3">
        {listings.map((listing) => (
          <div className="col-md-6 col-xl-4" key={listing._id}>
            <ListingCard
              listing={listing}
              onFavorite={currentUser ? saveFavorite : null}
              onCompare={currentUser ? addToCompare : null}
              onEdit={adminCanManage ? startEdit : null}
              onDelete={adminCanManage ? deleteListing : null}
            />
          </div>
        ))}
      </div>
    </div>
  );
}

export default Listings;
