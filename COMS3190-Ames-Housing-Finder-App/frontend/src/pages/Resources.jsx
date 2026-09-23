import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import { authPayload, canManageResources } from "../services/session";

const emptyResource = {
  category: "FAQ",
  title: "",
  body: "",
  link: "",
  isFeatured: false
};

function Resources({ currentUser }) {
  const [resources, setResources] = useState([]);
  const [form, setForm] = useState(emptyResource);
  const [editing, setEditing] = useState(null);
  const [category, setCategory] = useState("");
  const [message, setMessage] = useState("");
  const adminCanManage = canManageResources(currentUser);

  async function loadResources() {
    if (!currentUser) {
      setResources([]);
      return;
    }

    try {
      const response = await api.get(`/resources${category ? `?category=${encodeURIComponent(category)}` : ""}`);
      setResources(response.data);
    } catch (error) {
      setMessage("Resources could not be loaded.");
    }
  }

  useEffect(() => {
    loadResources();
  }, [category, currentUser]);

  function updateField(event) {
    const { name, value, type, checked } = event.target;

    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value
    });
  }

  function startEdit(resource) {
    if (!adminCanManage) {
      setMessage("Only admins can edit housing resources.");
      return;
    }

    setEditing(resource);
    setForm({
      category: resource.category || "FAQ",
      title: resource.title || "",
      body: resource.body || "",
      link: resource.link || "",
      isFeatured: Boolean(resource.isFeatured)
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditing(null);
    setForm(emptyResource);
  }

  async function submitResource(event) {
    event.preventDefault();

    if (!adminCanManage) {
      setMessage("Only admins can create or update housing resources.");
      return;
    }

    try {
      if (editing) {
        await api.put(`/resources/${editing._id}`, {
          ...form,
          ...authPayload(currentUser)
        });
        setMessage("Resource updated.");
      } else {
        await api.post("/resources", {
          ...form,
          ...authPayload(currentUser)
        });
        setMessage("Resource created.");
      }

      resetForm();
      await loadResources();
    } catch (error) {
      setMessage(error.response?.data?.message || "Resource could not be saved.");
    }
  }

  async function deleteResource(id) {
    if (!adminCanManage) {
      setMessage("Only admins can delete housing resources.");
      return;
    }

    if (!window.confirm("Delete this housing resource?")) {
      return;
    }

    try {
      await api.delete(`/resources/${id}`, {
        data: authPayload(currentUser)
      });
      setMessage("Resource removed.");
      await loadResources();
    } catch (error) {
      setMessage(error.response?.data?.message || "Resource could not be removed.");
    }
  }

  if (!currentUser) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">Resources are available after login. Guests can still use Home, Listings, Listing Details, and Info / FAQ.</p>
          <Link className="btn btn-primary" to="/login">Go to Login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Admin-managed support content</p>
        <h1 className="fw-bold">Housing Resources</h1>
        <p className="text-muted mb-0">
          Students can view resources. Admins can create, edit, and delete housing resource records.
        </p>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      {adminCanManage ? (
        <form className="card shadow-sm border-0 mb-4" onSubmit={submitResource}>
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2 className="h5 mb-0">{editing ? "Edit resource" : "Create resource"}</h2>
              {editing && <button className="btn btn-outline-secondary btn-sm" type="button" onClick={resetForm}>Cancel Edit</button>}
            </div>

            <div className="row g-3">
              <div className="col-md-3">
                <label className="form-label">Category</label>
                <select className="form-select" name="category" value={form.category} onChange={updateField}>
                  <option>FAQ</option>
                  <option>Budget</option>
                  <option>Lease</option>
                  <option>Moving</option>
                  <option>Campus</option>
                </select>
              </div>
              <div className="col-md-5">
                <label className="form-label">Title</label>
                <input className="form-control" name="title" value={form.title} onChange={updateField} required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Link</label>
                <input className="form-control" name="link" value={form.link} onChange={updateField} />
              </div>
              <div className="col-12">
                <label className="form-label">Body</label>
                <textarea className="form-control" name="body" rows="2" value={form.body} onChange={updateField} required />
              </div>
              <div className="col-12">
                <div className="form-check">
                  <input className="form-check-input" type="checkbox" name="isFeatured" checked={form.isFeatured} onChange={updateField} id="featuredResource" />
                  <label className="form-check-label" htmlFor="featuredResource">Feature on dashboard</label>
                </div>
              </div>
            </div>

            <button className="btn btn-primary mt-3" type="submit">
              {editing ? "Update Resource" : "Create Resource"}
            </button>
          </div>
        </form>
      ) : (
        <div className="alert alert-light border mb-4">Resource management is admin-only. You can view the records below.</div>
      )}

      <div className="d-flex flex-wrap gap-2 mb-4">
        {["", "FAQ", "Budget", "Lease", "Moving", "Campus"].map((item) => (
          <button
            className={`btn btn-sm ${category === item ? "btn-primary" : "btn-outline-primary"}`}
            key={item || "All"}
            onClick={() => setCategory(item)}
          >
            {item || "All"}
          </button>
        ))}
      </div>

      <div className="row g-3">
        {resources.map((resource) => (
          <div className="col-md-6" key={resource._id}>
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body">
                <div className="d-flex justify-content-between gap-2">
                  <span className="badge text-bg-info mb-2">{resource.category}</span>
                  {resource.isFeatured && <span className="badge text-bg-warning mb-2">Featured</span>}
                </div>
                <h2 className="h5">{resource.title}</h2>
                <p className="text-muted">{resource.body}</p>
                {resource.link && <a href={resource.link} target="_blank" rel="noreferrer">Open resource</a>}
                {adminCanManage && (
                  <div className="d-flex gap-2 mt-3">
                    <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(resource)}>Edit</button>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => deleteResource(resource._id)}>Delete</button>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Resources;
