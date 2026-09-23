import { Link } from "react-router-dom";

function NotFound() {
  return (
    <div className="container py-5 text-center">
      <p className="eyebrow mb-1">Required error page</p>
      <h1 className="display-3 fw-bold">404</h1>
      <p className="text-muted">The page you are looking for does not exist.</p>
      <Link className="btn btn-primary" to="/">
        Return Home
      </Link>
    </div>
  );
}

export default NotFound;
