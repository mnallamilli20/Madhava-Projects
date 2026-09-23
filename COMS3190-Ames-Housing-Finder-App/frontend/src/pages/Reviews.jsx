import { useEffect, useState } from "react";
import api from "../services/api";
import { authPayload, isAdmin, userPayload, userId } from "../services/session";

const emptyReview = {
  listingId: "",
  authorName: "",
  rating: "5",
  title: "",
  comment: ""
};

function ownsReview(currentUser, review) {
  return Boolean(currentUser?._id && review?.userId && String(review.userId) === String(userId(currentUser)));
}

function canEditReview(currentUser, review) {
  return ownsReview(currentUser, review);
}

function canDeleteReview(currentUser, review) {
  return ownsReview(currentUser, review) || isAdmin(currentUser);
}

function Reviews({ currentUser }) {
  const [reviews, setReviews] = useState([]);
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState(emptyReview);
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState("");

  async function loadData() {
    try {
      const [reviewsResponse, listingsResponse] = await Promise.all([
        api.get("/reviews"),
        api.get("/listings")
      ]);
      setReviews(reviewsResponse.data);
      setListings(listingsResponse.data);
    } catch (error) {
      setMessage("Reviews could not be loaded.");
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (!editing) {
      setForm({
        ...emptyReview,
        authorName: currentUser?.name || ""
      });
    }
  }, [currentUser, editing]);

  function updateField(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  }

  function startEdit(review) {
    if (!canEditReview(currentUser, review)) {
      setMessage("You can only edit reviews that you created.");
      return;
    }

    setEditing(review);
    setForm({
      listingId: review.listingId?._id || review.listingId || "",
      authorName: review.authorName || "",
      rating: review.rating || "5",
      title: review.title || "",
      comment: review.comment || ""
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditing(null);
    setForm({
      ...emptyReview,
      authorName: currentUser?.name || ""
    });
  }

  async function submitReview(event) {
    event.preventDefault();

    if (!currentUser?._id) {
      setMessage("Please log in to create a review.");
      return;
    }

    try {
      if (editing) {
        if (!canEditReview(currentUser, editing)) {
          setMessage("You can only update reviews that you created.");
          return;
        }

        await api.put(`/reviews/${editing._id}`, {
          ...form,
          ...authPayload(currentUser)
        });
        setMessage("Review updated.");
      } else {
        await api.post("/reviews", {
          ...form,
          ...userPayload(currentUser)
        });
        setMessage("Review created.");
      }

      resetForm();
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Review could not be saved.");
    }
  }

  async function deleteReview(review) {
    if (!canDeleteReview(currentUser, review)) {
      setMessage("You can only delete your own reviews. Admins can delete reviews for moderation.");
      return;
    }

    try {
      await api.delete(`/reviews/${review._id}`, {
        data: authPayload(currentUser)
      });
      setMessage("Review removed.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Review could not be removed.");
    }
  }

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Student reviews and admin moderation</p>
        <h1 className="fw-bold">Reviews</h1>
        <p className="text-muted mb-0">
          Students can create, edit, and delete their own reviews. Admins can delete reviews for moderation, but cannot edit reviews written by other users.
        </p>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      <form className="card shadow-sm border-0 mb-4" onSubmit={submitReview}>
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h5 mb-0">{editing ? "Edit your review" : "Create review"}</h2>
            {editing && (
              <button className="btn btn-outline-secondary btn-sm" type="button" onClick={resetForm}>Cancel Edit</button>
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
            <div className="col-md-2">
              <label className="form-label">Rating</label>
              <select className="form-select" name="rating" value={form.rating} onChange={updateField}>
                <option value="5">5</option>
                <option value="4">4</option>
                <option value="3">3</option>
                <option value="2">2</option>
                <option value="1">1</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Author</label>
              <input className="form-control" name="authorName" value={form.authorName} onChange={updateField} required />
            </div>
            <div className="col-md-3">
              <label className="form-label">Title</label>
              <input className="form-control" name="title" value={form.title} onChange={updateField} required />
            </div>
            <div className="col-12">
              <label className="form-label">Comment</label>
              <textarea className="form-control" name="comment" rows="2" value={form.comment} onChange={updateField} required />
            </div>
          </div>

          <button className="btn btn-primary mt-3" type="submit">
            {editing ? "Update Review" : "Create Review"}
          </button>
        </div>
      </form>

      <div className="row g-3">
        {reviews.map((review) => {
          const editAllowed = canEditReview(currentUser, review);
          const deleteAllowed = canDeleteReview(currentUser, review);

          return (
            <div className="col-md-6" key={review._id}>
              <div className="card h-100 shadow-sm border-0">
                <div className="card-body">
                  <div className="d-flex justify-content-between gap-3">
                    <div>
                      <h2 className="h5 mb-1">{review.title}</h2>
                      <p className="text-muted mb-2">{review.listingId?.title || "Deleted listing"}</p>
                    </div>
                    <span className="badge text-bg-warning align-self-start">{review.rating}/5</span>
                  </div>
                  <p className="mb-1"><strong>Author:</strong> {review.authorName}</p>
                  <p className="text-muted">{review.comment}</p>
                  {(editAllowed || deleteAllowed) && (
                    <div className="d-flex gap-2">
                      {editAllowed && (
                        <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(review)}>Edit</button>
                      )}
                      {deleteAllowed && (
                        <button className="btn btn-outline-danger btn-sm" onClick={() => deleteReview(review)}>Delete</button>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Reviews;
