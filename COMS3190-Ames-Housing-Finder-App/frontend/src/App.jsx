import { useState } from "react";
import { Link, Routes, Route } from "react-router-dom";
import AppNavbar from "./components/AppNavbar";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Listings from "./pages/Listings";
import ListingDetails from "./pages/ListingDetails";
import Favorites from "./pages/Favorites";
import Compare from "./pages/Compare";
import Appointments from "./pages/Appointments";
import Reviews from "./pages/Reviews";
import Resources from "./pages/Resources";
import FAQ from "./pages/FAQ";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import NotFound from "./pages/NotFound";
import { clearStoredUser, getStoredUser, saveStoredUser } from "./services/session";

function ProtectedPage({ currentUser, pageName, children }) {
  if (!currentUser?._id) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">
            Guests can only view Home, Listings, Listing Details, and Team Info / FAQ. Please log in to open {pageName}.
          </p>
          <div className="d-flex gap-2 flex-wrap">
            <Link className="btn btn-primary" to="/login">Go to Login</Link>
            <Link className="btn btn-outline-primary" to="/listings">Browse Listings</Link>
          </div>
        </div>
      </div>
    );
  }

  return children;
}

function App() {
  const [currentUser, setCurrentUser] = useState(getStoredUser());

  function handleLogin(user) {
    saveStoredUser(user);
    setCurrentUser(user);
  }

  function handleLogout() {
    clearStoredUser();
    setCurrentUser(null);
  }

  return (
    <div className="d-flex flex-column min-vh-100 app-shell">
      <AppNavbar currentUser={currentUser} onLogout={handleLogout} />

      <main className="flex-grow-1">
        <Routes>
          <Route path="/" element={<Home currentUser={currentUser} />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedPage currentUser={currentUser} pageName="the dashboard">
                <Dashboard currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route path="/listings" element={<Listings currentUser={currentUser} />} />
          <Route path="/listings/:id" element={<ListingDetails currentUser={currentUser} />} />
          <Route
            path="/favorites"
            element={
              <ProtectedPage currentUser={currentUser} pageName="Favorites">
                <Favorites currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route
            path="/compare"
            element={
              <ProtectedPage currentUser={currentUser} pageName="Compare">
                <Compare currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route
            path="/appointments"
            element={
              <ProtectedPage currentUser={currentUser} pageName="Tour Appointments">
                <Appointments currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route
            path="/reviews"
            element={
              <ProtectedPage currentUser={currentUser} pageName="Reviews">
                <Reviews currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route
            path="/resources"
            element={
              <ProtectedPage currentUser={currentUser} pageName="Housing Info Resources">
                <Resources currentUser={currentUser} />
              </ProtectedPage>
            }
          />
          <Route path="/faq" element={<FAQ currentUser={currentUser} />} />
          <Route path="/login" element={<Login onLogin={handleLogin} />} />
          <Route path="/signup" element={<Signup onLogin={handleLogin} />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>

      <Footer />
    </div>
  );
}

export default App;
