import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";

function Signup({ onLogin }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: ""
  });
  const [message, setMessage] = useState("");

  function updateField(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  }

  async function submitSignup(event) {
    event.preventDefault();
    setMessage("");

    try {
      const response = await api.post("/auth/signup", form);
      onLogin(response.data);
      navigate("/dashboard");
    } catch (error) {
      setMessage(error.response?.data?.message || "Signup failed.");
    }
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-7 col-lg-6">
          <div className="card shadow-sm border-0">
            <div className="card-body p-4">
              <p className="eyebrow mb-1">Required page</p>
              <h1 className="h3 fw-bold mb-3">Create a student account</h1>
              <p className="text-muted">
                Public signup creates a student account. Admin accounts are seeded by the backend and cannot be created from this form.
              </p>

              {message && <div className="alert alert-danger">{message}</div>}

              <form onSubmit={submitSignup} autoComplete="off">
                <div className="row g-3">
                  <div className="col-12">
                    <label className="form-label">Name</label>
                    <input className="form-control" name="name" value={form.name} onChange={updateField} autoComplete="off" required />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Email</label>
                    <input className="form-control" name="email" type="email" value={form.email} onChange={updateField} autoComplete="off" required />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Password</label>
                    <input className="form-control" name="password" type="password" value={form.password} onChange={updateField} autoComplete="new-password" required />
                  </div>
                </div>
                <button className="btn btn-primary w-100 mt-3" type="submit">
                  Sign Up
                </button>
              </form>

              <p className="small text-muted mt-3 mb-0">
                Already have an account? <Link to="/login">Log in</Link>.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Signup;
