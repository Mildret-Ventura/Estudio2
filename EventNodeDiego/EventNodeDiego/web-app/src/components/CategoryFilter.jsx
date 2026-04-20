function CategoryFilter() {
  const categories = ['Todos', 'Desarrollo', 'IA & Ciencia', 'Diseño UX', 'Marketing', 'Negocios']

  return (
    <div className="d-flex flex-wrap gap-2 mb-4">
      {categories.map((cat, index) => (
        <button
          key={cat}
          className={`btn btn-sm rounded-pill px-3 ${index === 0 ? 'btn-primary' : 'btn-outline-secondary'}`}
        >
          {cat}
        </button>
      ))}
    </div>
  )
}

export default CategoryFilter
