import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../services/api";
import { userPayload } from "../services/session";

function ListingDetails({ currentUser }) {
  const { id } = useParams();
  const [listing, setListing] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [message, setMessage] = useState("");

  async function loadDetails() {
    try {
      const [listingResponse, reviewsResponse] = await Promise.all([
        api.get(`/listings/${id}`),
        api.get(`/reviews?listingId=${id}`)
      ]);
      setListing(listingResponse.data);
      setReviews(reviewsResponse.data);
    } catch (error) {
      setMessage("Listing details could not be loaded.");
    }
  }

  async function saveFavorite() {
    if (!currentUser) {
      setMessage("Please log in to save favorites.");
      return;
    }

    try {
      await api.post("/favorites", {
        ...userPayload(currentUser),
        listingId: id,
        label: "Details page",
        priority: "Medium",
        note: "Saved from the details page."
      });
      setMessage("Listing saved to favorites.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be saved.");
    }
  }

  async function addToCompare() {
    if (!currentUser) {
      setMessage("Please log in to compare listings.");
      return;
    }

    try {
      await api.post("/compare", {
        ...userPayload(currentUser),
        listingId: id,
        compareNote: "Added from details page."
      });
      setMessage("Listing added to compare.");
    } catch (error) {
      setMessage(error.response?.data?.message || "Listing could not be added to compare.");
    }
  }

  useEffect(() => {
    loadDetails();
  }, [id]);

  if (!listing && !message) {
    return (
      <div className="container py-5">
        <div className="alert alert-info">Loading listing details...</div>
      </div>
    );
  }

  if (!listing) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger">{message}</div>
        <Link className="btn btn-primary" to="/listings">Back to Listings</Link>
      </div>
    );
  }

  const averageRating = reviews.length
    ? (reviews.reduce((sum, review) => sum + Number(review.rating || 0), 0) / reviews.length).toFixed(1)
    : "No reviews yet";

  return (
    <div className="container py-4">
      <Link className="btn btn-outline-secondary btn-sm mb-3" to="/listings">Back to Listings</Link>

      {message && <div className="alert alert-info">{message}</div>}

      <div className="row g-4">
        <div className="col-lg-8">
          <div className="card shadow-sm border-0">
            <div className="card-body p-4">
              <div className="d-flex flex-column flex-md-row justify-content-between gap-3 mb-3">
                <div>
                  <p className="eyebrow mb-1">Listing details</p>
                  <h1 className="fw-bold mb-1">{listing.title}</h1>
                  <p className="text-muted mb-0">{listing.address}</p>
                </div>
                <span className="badge text-bg-primary align-self-start">{listing.status}</span>
              </div>

              <div className="row g-3 mb-4">
                <div className="col-6 col-md-3"><div className="metric-box text-start"><span>${listing.rent}</span><small>Monthly Rent</small></div></div>
                <div className="col-6 col-md-3"><div className="metric-box text-start"><span>{listing.bedrooms}</span><small>Bedrooms</small></div></div>
                <div className="col-6 col-md-3"><div className="metric-box text-start"><span>{listing.bathrooms}</span><small>Bathrooms</small></div></div>
                <div className="col-6 col-md-3"><div className="metric-box text-start"><span>{listing.squareFeet || 0}</span><small>Sq. Ft.</small></div></div>
              </div>

              <h2 className="h5">Overview</h2>
              <p className="text-muted">{listing.description}</p>

              <h2 className="h5">Amenities</h2>
              <div className="d-flex flex-wrap gap-2 mb-4">
                {(listing.amenities || []).map((amenity) => (
                  <span className="badge rounded-pill text-bg-light border" key={amenity}>{amenity}</span>
                ))}
              </div>

              <div className="d-flex flex-wrap gap-2">
                {currentUser ? (
                  <>
                    <button className="btn btn-primary" onClick={saveFavorite}>Save Favorite</button>
                    <button className="btn btn-outline-dark" onClick={addToCompare}>Add to Compare</button>
                    <Link className="btn btn-outline-primary" to="/appointments">Schedule Tour</Link>
                  </>
                ) : (
                  <Link className="btn btn-primary" to="/login">Log in to save, compare, or schedule a tour</Link>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card shadow-sm border-0 mb-3">
            <div className="card-body">
              <h2 className="h5">Quick facts</h2>
              <p className="mb-1"><strong>Neighborhood:</strong> {listing.neighborhood}</p>
              <p className="mb-1"><strong>Type:</strong> {listing.propertyType}</p>
              <p className="mb-1"><strong>Available:</strong> {listing.availableDate}</p>
              <p className="mb-0"><strong>Contact:</strong> {listing.contactEmail}</p>
            </div>
          </div>

          <div className="card shadow-sm border-0">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h2 className="h5 mb-0">Reviews</h2>
                <span className="badge text-bg-success">{averageRating}</span>
              </div>
              {reviews.length === 0 ? (
                <p className="text-muted mb-0">No reviews have been added for this listing.</p>
              ) : (
                reviews.map((review) => (
                  <div className="border-top pt-3 mt-3" key={review._id}>
                    <h3 className="h6 mb-1">{review.title}</h3>
                    <p className="small text-muted mb-1">{review.authorName} rated {review.rating}/5</p>
                    <p className="mb-0">{review.comment}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ListingDetails;
