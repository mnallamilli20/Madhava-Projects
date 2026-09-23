import { Link } from "react-router-dom";

function ListingCard({ listing, onFavorite, onCompare, onEdit, onDelete }) {
  return (
    <div className="card h-100 listing-card shadow-sm border-0">
      <div className="card-body d-flex flex-column">
        <div className="d-flex justify-content-between gap-3">
          <div>
            <h2 className="h5 mb-1">{listing.title}</h2>
            <p className="text-muted mb-2">{listing.address}</p>
          </div>
          <span className="badge text-bg-primary align-self-start">{listing.status}</span>
        </div>

        <div className="row g-2 my-2 small">
          <div className="col-6">
            <span className="stat-pill">${listing.rent}/mo</span>
          </div>
          <div className="col-6">
            <span className="stat-pill">{listing.bedrooms} bed</span>
          </div>
          <div className="col-6">
            <span className="stat-pill">{listing.bathrooms} bath</span>
          </div>
          <div className="col-6">
            <span className="stat-pill">{listing.propertyType}</span>
          </div>
        </div>

        <p className="text-muted flex-grow-1">{listing.description}</p>

        <div className="d-flex flex-wrap gap-2 mb-3">
          {(listing.amenities || []).slice(0, 4).map((amenity) => (
            <span className="badge rounded-pill text-bg-light border" key={amenity}>
              {amenity}
            </span>
          ))}
        </div>

        <div className="d-flex flex-wrap gap-2 mt-auto">
          <Link className="btn btn-primary btn-sm" to={`/listings/${listing._id}`}>
            Details
          </Link>
          {onFavorite && (
            <button className="btn btn-outline-primary btn-sm" onClick={() => onFavorite(listing._id)}>
              Save
            </button>
          )}
          {onCompare && (
            <button className="btn btn-outline-dark btn-sm" onClick={() => onCompare(listing._id)}>
              Compare
            </button>
          )}
          {onEdit && (
            <button className="btn btn-outline-primary btn-sm" onClick={() => onEdit(listing)}>
              Edit
            </button>
          )}
          {onDelete && (
            <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(listing._id)}>
              Delete
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default ListingCard;