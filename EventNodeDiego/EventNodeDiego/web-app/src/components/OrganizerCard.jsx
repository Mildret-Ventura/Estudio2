function OrganizerCard({ name, role }) {
  return (
    <div className="d-flex align-items-center gap-3">
      <div className="rounded-circle bg-light d-flex align-items-center justify-content-center flex-shrink-0"
        style={{ width: '44px', height: '44px' }}>
        <i className="bi bi-person-fill text-secondary fs-5"></i>
      </div>
      <div>
        <div className="fw-semibold small">{name}</div>
        <div className="text-secondary small">{role}</div>
      </div>
    </div>
  )
}

export default OrganizerCard
