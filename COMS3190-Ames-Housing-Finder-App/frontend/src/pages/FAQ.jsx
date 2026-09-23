import { useEffect, useState } from "react";
import api from "../services/api";

function FAQ() {
  const [questions, setQuestions] = useState([]);
  const [message, setMessage] = useState("");

  async function loadFAQ() {
    try {
      const response = await api.get("/resources?category=FAQ");
      setQuestions(response.data);
    } catch (error) {
      setMessage("FAQ content could not be loaded.");
    }
  }

  useEffect(() => {
    loadFAQ();
  }, []);

  return (
    <div className="container py-4">
      <div className="mb-4">
        <p className="eyebrow mb-1">Database-backed FAQ page</p>
        <h1 className="fw-bold">Team Info / FAQ</h1>
        <p className="text-muted mb-0">Project information, team details, and common housing questions loaded from MongoDB.</p>
      </div>

      {message && <div className="alert alert-danger">{message}</div>}

      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <h2 className="h5">Team AL8</h2>
          <div className="row g-3">
            <div className="col-md-6">
              <div className="team-card">
                <h3 className="h6 mb-1">Vikrant Gandotra</h3>
                <p className="text-muted mb-0">NetID: gandotra@iastate.edu</p>
              </div>
            </div>
            <div className="col-md-6">
              <div className="team-card">
                <h3 className="h6 mb-1">Madhava Nallamilli</h3>
                <p className="text-muted mb-0">NetID: mnalla23@iastate.edu</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3">
        {questions.map((item) => (
          <div className="col-md-6" key={item._id}>
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body">
                <span className="badge text-bg-light border mb-2">{item.category}</span>
                <h2 className="h5">{item.title}</h2>
                <p className="text-muted mb-0">{item.body}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default FAQ;
