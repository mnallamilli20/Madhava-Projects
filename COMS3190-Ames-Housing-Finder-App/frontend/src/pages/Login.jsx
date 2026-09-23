import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";

function Login({ onLogin }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
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

  async function submitLogin(event) {
    event.preventDefault();
    setMessage("");

    try {
      const response = await api.post("/auth/login", form);
      onLogin(response.data);
      navigate("/dashboard");
    } catch (error) {
      setMessage(error.response?.data?.message || "Login failed.");
    }
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-5">
          <div className="card shadow-sm border-0">
            <div className="card-body p-4">
              <p className="eyebrow mb-1">Required page</p>
              <h1 className="h3 fw-bold mb-3">Log in</h1>
              <p className="text-muted">
                Log in to use favorites, compare, tours, resources, dashboard tools, and admin-only management.
              </p>

              {message && <div className="alert alert-danger">{message}</div>}

              <form onSubmit={submitLogin} autoComplete="off">
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input className="form-control" name="email" type="email" value={form.email} onChange={updateField} autoComplete="off" required />
                </div>

                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input className="form-control" name="password" type="password" value={form.password} onChange={updateField} autoComplete="new-password" required />
                </div>

                <button className="btn btn-primary w-100" type="submit">
                  Log In
                </button>
              </form>

              

              <p className="small text-muted mt-3 mb-0">
                Need an account? <Link to="/signup">Create one</Link>.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
