/**
 * Online Banking Management System - Auth & API Helper
 */

const API_BASE_URL = "http://localhost:8080/api";

const Auth = {
  getToken() {
    return localStorage.getItem("bank_jwt_token");
  },

  getUser() {
    const userStr = localStorage.getItem("bank_user_info");
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      return null;
    }
  },

  setAuthData(authData) {
    localStorage.setItem("bank_jwt_token", authData.token);
    localStorage.setItem("bank_user_info", JSON.stringify({
      userId: authData.userId,
      fullName: authData.fullName,
      email: authData.email,
      role: authData.role,
      accountNumber: authData.accountNumber
    }));
  },

  clearAuthData() {
    localStorage.removeItem("bank_jwt_token");
    localStorage.removeItem("bank_user_info");
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  isAdmin() {
    const user = this.getUser();
    return user && (user.role === "ROLE_ADMIN" || user.role === "ADMIN");
  },

  requireAuth() {
    if (!this.isLoggedIn()) {
      window.location.href = "login.html?msg=unauthorized";
    }
  },

  requireAdmin() {
    this.requireAuth();
    if (!this.isAdmin()) {
      window.location.href = "dashboard.html?msg=forbidden";
    }
  },

  redirectIfLoggedIn() {
    if (this.isLoggedIn()) {
      if (this.isAdmin()) {
        window.location.href = "admin.html";
      } else {
        window.location.href = "dashboard.html";
      }
    }
  },

  logout() {
    this.clearAuthData();
    window.location.href = "login.html?msg=logout";
  }
};

/**
 * Reusable fetch wrapper with automatic JWT header and 401 interceptor
 */
async function apiFetch(endpoint, options = {}) {
  const url = endpoint.startsWith("http") ? endpoint : `${API_BASE_URL}${endpoint}`;
  
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  const token = Auth.getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers
    });

    if (response.status === 401) {
      Auth.clearAuthData();
      window.location.href = "login.html?msg=expired";
      throw new Error("Session expired. Please login again.");
    }

    const data = await response.json().catch(() => ({
      success: response.ok,
      message: response.statusText
    }));

    if (!response.ok) {
      throw new Error(data.message || `Request failed with status ${response.status}`);
    }

    return data;
  } catch (error) {
    throw error;
  }
}

/**
 * Common Currency and Date Formatters
 */
function formatCurrency(amount) {
  const num = Number(amount) || 0;
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2
  }).format(num);
}

function formatDate(dateStr) {
  if (!dateStr) return "N/A";
  const date = new Date(dateStr);
  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true
  });
}

function maskAccountNumber(accountNumber) {
  if (!accountNumber || accountNumber.length < 4) return accountNumber || "N/A";
  const lastFour = accountNumber.slice(-4);
  return `XXXX XXXX ${lastFour}`;
}

/**
 * Notification / Alert helper
 */
function showAlert(elementId, message, type = "danger") {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.className = `alert alert-${type} alert-dismissible fade show`;
  el.innerHTML = `
    <span>${message}</span>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  `;
  el.style.display = "block";
  // Auto scroll to alert
  el.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

/**
 * Shared Navbar Component Renderer
 */
function renderNavbar(activePage = "") {
  const container = document.getElementById("navbar-container");
  if (!container) return;

  const user = Auth.getUser();
  const isLoggedIn = Auth.isLoggedIn();
  const isAdmin = Auth.isAdmin();

  let navLinks = "";
  let userActions = "";

  if (isLoggedIn) {
    navLinks = `
      <li class="nav-item">
        <a class="nav-link ${activePage === 'dashboard' ? 'active' : ''}" href="dashboard.html">
          <i class="bi bi-grid-1x2"></i> Dashboard
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link ${activePage === 'account' ? 'active' : ''}" href="account.html">
          <i class="bi bi-wallet2"></i> Account
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link ${activePage === 'transfer' ? 'active' : ''}" href="transfer.html">
          <i class="bi bi-arrow-left-right"></i> Transfer
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link ${activePage === 'transactions' ? 'active' : ''}" href="transactions.html">
          <i class="bi bi-receipt"></i> Transactions
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link ${activePage === 'profile' ? 'active' : ''}" href="profile.html">
          <i class="bi bi-person-circle"></i> Profile
        </a>
      </li>
      ${isAdmin ? `
      <li class="nav-item">
        <a class="nav-link ${activePage === 'admin' ? 'active' : ''}" href="admin.html">
          <i class="bi bi-shield-lock-fill text-warning"></i> Admin Panel
        </a>
      </li>` : ''}
    `;

    userActions = `
      <div class="d-flex align-items-center gap-3">
        <span class="user-badge-nav">
          <i class="bi bi-person-fill"></i>
          <span>${user ? user.fullName.split(' ')[0] : 'User'}</span>
          ${isAdmin ? '<span class="badge bg-warning text-dark ms-1">Admin</span>' : ''}
        </span>
        <button class="btn btn-outline-light btn-sm" onclick="Auth.logout()">
          <i class="bi bi-box-arrow-right"></i> Logout
        </button>
      </div>
    `;
  } else {
    navLinks = `
      <li class="nav-item">
        <a class="nav-link ${activePage === 'home' ? 'active' : ''}" href="index.html">Home</a>
      </li>
    `;
    userActions = `
      <div class="d-flex align-items-center gap-2">
        <a href="login.html" class="btn btn-outline-light btn-sm">Login</a>
        <a href="register.html" class="btn btn-primary btn-sm bg-gradient">Open Account</a>
      </div>
    `;
  }

  container.innerHTML = `
    <nav class="navbar navbar-expand-lg banking-navbar">
      <div class="container">
        <a class="navbar-brand" href="${isLoggedIn ? 'dashboard.html' : 'index.html'}">
          <i class="bi bi-bank2"></i> Online Banking
        </a>
        <button class="navbar-toggler text-white border-light" type="button" data-bs-toggle="collapse" data-bs-target="#bankingNav">
          <span class="navbar-toggler-icon" style="filter: invert(1);"></span>
        </button>
        <div class="collapse navbar-collapse" id="bankingNav">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            ${navLinks}
          </ul>
          ${userActions}
        </div>
      </div>
    </nav>
  `;
}
