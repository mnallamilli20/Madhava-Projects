import { NavLink, Link, useNavigate } from "react-router-dom";

function AppNavbar({ currentUser, onLogout }) {
  const navigate = useNavigate();
  const isLoggedIn = Boolean(currentUser?._id);

  function logout() {
    onLogout();
    navigate("/");
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm">
      <div className="container">
        <Link className="navbar-brand fw-bold" to="/">
          Ames Housing Finder
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNavigation"
          aria-controls="mainNavigation"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="mainNavigation">
          <div className="navbar-nav ms-auto align-items-lg-center gap-lg-1">
            <NavLink className="nav-link" to="/">
              Home
            </NavLink>
            <NavLink className="nav-link" to="/listings">
              Listings
            </NavLink>
            <NavLink className="nav-link" to="/faq">
              Info / FAQ
            </NavLink>

            {isLoggedIn && (
              <>
                <NavLink className="nav-link" to="/dashboard">
                  Dashboard
                </NavLink>
                <NavLink className="nav-link" to="/favorites">
                  Favorites
                </NavLink>
                <NavLink className="nav-link" to="/compare">
                  Compare
                </NavLink>
                <NavLink className="nav-link" to="/appointments">
                  Tours
                </NavLink>
                <NavLink className="nav-link" to="/reviews">
                  Reviews
                </NavLink>
                <NavLink className="nav-link" to="/resources">
                  Resources
                </NavLink>
              </>
            )}

            {isLoggedIn ? (
              <>
                <span className="navbar-text small d-none d-xl-inline ms-2">
                  {currentUser.name} ({currentUser.role})
                </span>
                <button className="btn btn-outline-light btn-sm ms-lg-2" onClick={logout}>
                  Log Out
                </button>
              </>
            ) : (
              <>
                <NavLink className="nav-link" to="/login">
                  Login
                </NavLink>
                <NavLink className="btn btn-outline-light btn-sm ms-lg-2" to="/signup">
                  Sign Up
                </NavLink>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

export default AppNavbar;
