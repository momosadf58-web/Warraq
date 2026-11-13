const searchForm = document.querySelector("#search-form");
const searchInput = document.querySelector("#book-search");
const cards = [...document.querySelectorAll(".book-card")];
const emptyState = document.querySelector("#empty-state");
const dialog = document.querySelector("#book-dialog");
const savedBooks = new Set(JSON.parse(localStorage.getItem("warraq-saved-books") || "[]"));

function filterBooks(query) {
  const normalizedQuery = query.trim().toLowerCase();
  let visibleCards = 0;

  cards.forEach((card) => {
    const searchableText = `${card.dataset.title} ${card.dataset.category} ${card.dataset.author}`.toLowerCase();
    const matches = !normalizedQuery || searchableText.includes(normalizedQuery);
    card.hidden = !matches;
    if (matches) visibleCards += 1;
  });

  emptyState.hidden = visibleCards > 0;
}

function updateSavedButton(card) {
  const button = card.querySelector(".save-button");
  const saved = savedBooks.has(card.dataset.title);
  button.textContent = saved ? "♥" : "♡";
  button.setAttribute("aria-label", saved ? `${card.dataset.title} aus Merkliste entfernen` : `${card.dataset.title} merken`);
}

function persistSavedBooks() {
  localStorage.setItem("warraq-saved-books", JSON.stringify([...savedBooks]));
}

function openBookDetails(card) {
  document.querySelector("#dialog-title").textContent = card.dataset.title;
  document.querySelector("#dialog-category").textContent = card.dataset.category;
  document.querySelector("#dialog-author").textContent = `von ${card.dataset.author}`;
  document.querySelector("#dialog-description").textContent = card.dataset.description;
  document.querySelector("#dialog-format").textContent = card.dataset.format;
  document.querySelector("#dialog-year").textContent = card.dataset.year;
  const coverClass = [...card.querySelector(".cover").classList].find((name) => name.startsWith("cover-"));
  const dialogCover = document.querySelector("#dialog-cover");
  dialogCover.className = `dialog-cover ${coverClass}`;
  dialogCover.textContent = card.dataset.title;
  dialog.showModal();
}

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  filterBooks(searchInput.value);
  document.querySelector("#entdecken").scrollIntoView({ behavior: "smooth" });
});

document.querySelectorAll("[data-search]").forEach((button) => {
  button.addEventListener("click", () => {
    searchInput.value = button.dataset.search;
    filterBooks(button.dataset.search);
    document.querySelector("#entdecken").scrollIntoView({ behavior: "smooth" });
  });
});

document.querySelector("#clear-search").addEventListener("click", () => {
  searchInput.value = "";
  filterBooks("");
});

document.querySelectorAll("[data-scroll-to]").forEach((button) => {
  button.addEventListener("click", () => {
    document.querySelector(`#${button.dataset.scrollTo}`).scrollIntoView({ behavior: "smooth" });
  });
});

cards.forEach((card) => {
  updateSavedButton(card);
  card.querySelector(".details-button").addEventListener("click", () => openBookDetails(card));
  card.querySelector(".save-button").addEventListener("click", () => {
    if (savedBooks.has(card.dataset.title)) savedBooks.delete(card.dataset.title);
    else savedBooks.add(card.dataset.title);
    updateSavedButton(card);
    persistSavedBooks();
  });
});

document.querySelector(".dialog-close").addEventListener("click", () => dialog.close());
dialog.addEventListener("click", (event) => {
  if (event.target === dialog) dialog.close();
});

document.querySelector(".menu-toggle").addEventListener("click", (event) => {
  const menu = document.querySelector("#main-nav");
  const isOpen = menu.classList.toggle("is-open");
  event.currentTarget.setAttribute("aria-expanded", String(isOpen));
});

document.querySelectorAll(".main-nav a").forEach((link) => {
  link.addEventListener("click", () => {
    document.querySelector("#main-nav").classList.remove("is-open");
    document.querySelector(".menu-toggle").setAttribute("aria-expanded", "false");
  });
});

document.querySelector("#newsletter-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const email = document.querySelector("#email");
  const message = document.querySelector("#form-message");
  message.textContent = `Danke – wir schicken die nächste Auswahl an ${email.value}.`;
  email.value = "";
});
