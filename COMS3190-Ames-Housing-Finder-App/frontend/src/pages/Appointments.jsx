import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import { isAdmin, userPayload, userQuery, actorQuery } from "../services/session";

function buildEmptyAppointment(user) {
  return {
    listingId: "",
    visitorName: user?.name || "",
    visitorEmail: user?.email || "",
    tourDate: "",
    status: "Requested",
    note: ""
  };
}

function Appointments({ currentUser }) {
  const [appointments, setAppointments] = useState([]);
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState(buildEmptyAppointment(currentUser));
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState("");

  async function loadData() {
    if (!currentUser?._id) {
      setAppointments([]);
      setListings([]);
      setForm(buildEmptyAppointment(null));
      return;
    }

    setMessage("");

    try {
      const listingsResponse = await api.get("/listings");
      setListings(listingsResponse.data);
    } catch (error) {
      setListings([]);
      setMessage(error.response?.data?.message || "Unable to load listings for appointments.");
    }

    try {
      const query = isAdmin(currentUser) ? `${actorQuery(currentUser)}&all=true` : userQuery(currentUser);
      const appointmentsResponse = await api.get(`/appointments?${query}`);
      setAppointments(appointmentsResponse.data);
    } catch (error) {
      setAppointments([]);
      setMessage(error.response?.data?.message || "Unable to load appointments.");
    }
  }

  useEffect(() => {
    setForm(buildEmptyAppointment(currentUser));
    setEditing(null);
    loadData();
  }, [currentUser]);

  function updateField(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  }

  function startEdit(appointment) {
    setEditing(appointment);
    setForm({
      listingId: appointment.listingId?._id || appointment.listingId || "",
      visitorName: appointment.visitorName || "",
      visitorEmail: appointment.visitorEmail || "",
      tourDate: appointment.tourDate || "",
      status: appointment.status || "Requested",
      note: appointment.note || ""
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditing(null);
    setForm(buildEmptyAppointment(currentUser));
  }

  async function submitAppointment(event) {
    event.preventDefault();

    if (!currentUser?._id) {
      setMessage("Please log in to schedule a tour.");
      return;
    }

    try {
      if (editing) {
        const payload = isAdmin(currentUser)
          ? { ...form, ...userPayload({ _id: editing.userId || currentUser._id }), actorId: currentUser._id }
          : { ...form, ...userPayload(currentUser) };

        await api.put(`/appointments/${editing._id}`, payload);
        setMessage("Appointment updated.");
      } else {
        await api.post("/appointments", {
          ...form,
          ...userPayload(currentUser)
        });
        setMessage("Appointment requested.");
      }

      resetForm();
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Appointment could not be saved.");
    }
  }

  async function updateStatus(appointment, status) {
    try {
      const payload = isAdmin(currentUser)
        ? { userId: appointment.userId || currentUser._id, actorId: currentUser._id, status }
        : { ...userPayload(currentUser), status };

      await api.put(`/appointments/${appointment._id}`, payload);
      setMessage("Appointment status updated.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Appointment could not be updated.");
    }
  }

  async function deleteAppointment(appointment) {
    try {
      const payload = isAdmin(currentUser)
        ? { userId: appointment.userId || currentUser._id, actorId: currentUser._id }
        : userPayload(currentUser);

      await api.delete(`/appointments/${appointment._id}`, {
        data: payload
      });
      setMessage("Appointment removed.");
      await loadData();
    } catch (error) {
      setMessage(error.response?.data?.message || "Appointment could not be removed.");
    }
  }

  if (!currentUser?._id) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning shadow-sm">
          <h1 className="h4">Login required</h1>
          <p className="mb-3">Tours are account-specific. Please log in to schedule, update, and remove appointments.</p>
          <Link className="btn btn-primary" to="/login">Go to Login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Full CRUD feature</p>
        <h1 className="fw-bold">Tour Appointments</h1>
        <p className="text-muted mb-0">Create, update, confirm, cancel, and delete housing tour requests.</p>
      </div>

      {message && <div className="alert alert-info">{message}</div>}

      <form className="card shadow-sm border-0 mb-4" onSubmit={submitAppointment}>
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h5 mb-0">{editing ? "Edit appointment" : "Schedule a tour"}</h2>
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
            <div className="col-md-3">
              <label className="form-label">Name</label>
              <input className="form-control" name="visitorName" value={form.visitorName} onChange={updateField} required />
            </div>
            <div className="col-md-3">
              <label className="form-label">Email</label>
              <input className="form-control" name="visitorEmail" type="email" value={form.visitorEmail} onChange={updateField} required />
            </div>
            <div className="col-md-2">
              <label className="form-label">Status</label>
              <select className="form-select" name="status" value={form.status} onChange={updateField}>
                <option>Requested</option>
                <option>Confirmed</option>
                <option>Completed</option>
                <option>Canceled</option>
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label">Tour Date</label>
              <input className="form-control" name="tourDate" type="datetime-local" value={form.tourDate} onChange={updateField} required />
            </div>
            <div className="col-md-8">
              <label className="form-label">Note</label>
              <input className="form-control" name="note" value={form.note} onChange={updateField} />
            </div>
          </div>

          <button className="btn btn-primary mt-3" type="submit">
            {editing ? "Update Appointment" : "Request Tour"}
          </button>
        </div>
      </form>

      {appointments.length === 0 ? (
        <div className="alert alert-light border">No tour appointments are currently saved.</div>
      ) : (
        <div className="row g-3">
          {appointments.map((appointment) => (
            <div className="col-md-6" key={appointment._id}>
              <div className="card h-100 shadow-sm border-0">
                <div className="card-body">
                  <div className="d-flex justify-content-between gap-3">
                    <div>
                      <h2 className="h5">{appointment.listingId?.title || "Deleted listing"}</h2>
                      <p className="text-muted mb-1">{appointment.tourDate}</p>
                    </div>
                    <span className="badge text-bg-success align-self-start">{appointment.status}</span>
                  </div>
                  <p className="mb-1"><strong>Visitor:</strong> {appointment.visitorName}</p>
                  <p className="mb-1"><strong>Email:</strong> {appointment.visitorEmail}</p>
                  {isAdmin(currentUser) && <p className="mb-1 small text-muted"><strong>User ID:</strong> {appointment.userId}</p>}
                  <p className="text-muted">{appointment.note || "No note added."}</p>
                  <div className="d-flex flex-wrap gap-2">
                    <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(appointment)}>Edit</button>
                    <button className="btn btn-outline-success btn-sm" onClick={() => updateStatus(appointment, "Confirmed")}>Confirm</button>
                    <button className="btn btn-outline-secondary btn-sm" onClick={() => updateStatus(appointment, "Canceled")}>Cancel</button>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => deleteAppointment(appointment)}>Delete</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Appointments;
