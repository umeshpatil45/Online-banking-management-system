/**
 * Admin Panel Logic - System Oversight and Account Control
 */

document.addEventListener("DOMContentLoaded", async () => {
  Auth.requireAdmin();
  renderNavbar("admin");

  await loadAdminStats();
  await loadAdminAccounts();
  await loadAdminUsers();
  await loadAdminTransactions();

  setupAdminEventListeners();
});

let adminAccountsList = [];
let adminUsersList = [];
let adminTransactionsList = [];

async function loadAdminStats() {
  try {
    const res = await apiFetch("/admin/stats");
    if (res.success && res.data) {
      const s = res.data;
      document.getElementById("stat-total-users").textContent = s.totalUsers;
      document.getElementById("stat-total-accounts").textContent = s.totalAccounts;
      document.getElementById("stat-total-txns").textContent = s.totalTransactions;
      document.getElementById("stat-deposit-vol").textContent = formatCurrency(s.totalDepositVolume);
      document.getElementById("stat-transfer-vol").textContent = formatCurrency(s.totalTransferVolume);
      document.getElementById("stat-active-accs").textContent = s.activeAccountsCount;
      document.getElementById("stat-blocked-accs").textContent = s.blockedAccountsCount;
    }
  } catch (err) {
    showAlert("admin-alert", err.message, "danger");
  }
}

async function loadAdminAccounts(query = "") {
  try {
    const url = `/admin/accounts${query ? '?query=' + encodeURIComponent(query) : ''}`;
    const res = await apiFetch(url);
    if (res.success && res.data) {
      adminAccountsList = res.data;
      renderAccountsTable(adminAccountsList);
    }
  } catch (err) {
    showAlert("admin-alert", err.message, "danger");
  }
}

function renderAccountsTable(accounts) {
  const tbody = document.getElementById("admin-accounts-tbody");
  if (!tbody) return;

  if (!accounts || accounts.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No accounts found.</td></tr>`;
    return;
  }

  tbody.innerHTML = accounts.map(a => {
    const isBlocked = a.status === "BLOCKED";
    const statusBadge = isBlocked ? `<span class="badge bg-danger">BLOCKED</span>` : `<span class="badge bg-success">ACTIVE</span>`;
    const actionBtn = isBlocked
      ? `<button class="btn btn-sm btn-outline-success" onclick="toggleAccountBlock(${a.id}, 'unblock')"><i class="bi bi-unlock-fill me-1"></i> Unblock</button>`
      : `<button class="btn btn-sm btn-outline-danger" onclick="toggleAccountBlock(${a.id}, 'block')"><i class="bi bi-lock-fill me-1"></i> Block</button>`;

    return `
      <tr>
        <td class="fw-bold text-primary">${a.accountNumber}</td>
        <td>${a.userName}</td>
        <td><small class="text-muted">${a.userEmail}</small></td>
        <td><span class="badge bg-info text-dark">${a.accountType}</span></td>
        <td class="fw-bold">${formatCurrency(a.balance)}</td>
        <td>${statusBadge}</td>
        <td>${actionBtn}</td>
      </tr>
    `;
  }).join("");
}

async function toggleAccountBlock(id, action) {
  if (!confirm(`Are you sure you want to ${action.toUpperCase()} this account?`)) return;

  try {
    const res = await apiFetch(`/admin/accounts/${id}/${action}`, {
      method: "PUT"
    });

    if (res.success) {
      showAlert("admin-alert", `Account successfully ${action}ed.`, "success");
      await loadAdminAccounts();
      await loadAdminStats();
    }
  } catch (err) {
    showAlert("admin-alert", err.message, "danger");
  }
}

async function loadAdminUsers(query = "") {
  try {
    const url = `/admin/users${query ? '?query=' + encodeURIComponent(query) : ''}`;
    const res = await apiFetch(url);
    if (res.success && res.data) {
      adminUsersList = res.data;
      renderUsersTable(adminUsersList);
    }
  } catch (err) {
    showAlert("admin-alert", err.message, "danger");
  }
}

function renderUsersTable(users) {
  const tbody = document.getElementById("admin-users-tbody");
  if (!tbody) return;

  if (!users || users.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No users found.</td></tr>`;
    return;
  }

  tbody.innerHTML = users.map(u => `
    <tr>
      <td>${u.id}</td>
      <td class="fw-semibold">${u.fullName}</td>
      <td>${u.email}</td>
      <td>${u.mobile}</td>
      <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'bg-warning text-dark' : 'bg-primary'}">${u.role}</span></td>
      <td>${u.accountNumber || 'N/A'}</td>
    </tr>
  `).join("");
}

async function loadAdminTransactions() {
  try {
    const type = document.getElementById("admin-txn-type-filter")?.value || "";
    const query = document.getElementById("admin-txn-search")?.value || "";

    const params = new URLSearchParams();
    if (type) params.append("type", type);
    if (query.trim()) params.append("query", query.trim());

    const url = `/admin/transactions${params.toString() ? '?' + params.toString() : ''}`;
    const res = await apiFetch(url);
    if (res.success && res.data) {
      adminTransactionsList = res.data;
      renderAdminTransactionsTable(adminTransactionsList);
    }
  } catch (err) {
    showAlert("admin-alert", err.message, "danger");
  }
}

function renderAdminTransactionsTable(txns) {
  const tbody = document.getElementById("admin-txns-tbody");
  if (!tbody) return;

  if (!txns || txns.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">No transactions found.</td></tr>`;
    return;
  }

  tbody.innerHTML = txns.map(t => {
    let typeBadgeClass = "badge-transfer";
    if (t.transactionType === "DEPOSIT") typeBadgeClass = "badge-deposit";
    if (t.transactionType === "WITHDRAW") typeBadgeClass = "badge-withdraw";

    return `
      <tr>
        <td class="fw-semibold text-primary"><small>${t.transactionReference}</small></td>
        <td><small class="text-muted">${formatDate(t.createdAt)}</small></td>
        <td><span class="badge ${typeBadgeClass}">${t.transactionType}</span></td>
        <td class="fw-bold">${formatCurrency(t.amount)}</td>
        <td><small>${t.senderAccount || '-'}</small></td>
        <td><small>${t.receiverAccount || '-'}</small></td>
        <td><small class="text-muted">${t.description || '-'}</small></td>
        <td><span class="badge ${t.transactionStatus === 'SUCCESS' ? 'badge-status-success' : 'badge-status-failed'}">${t.transactionStatus}</span></td>
      </tr>
    `;
  }).join("");
}

function setupAdminEventListeners() {
  const searchAcc = document.getElementById("admin-search-accounts");
  if (searchAcc) {
    searchAcc.addEventListener("input", (e) => loadAdminAccounts(e.target.value));
  }

  const searchUsr = document.getElementById("admin-search-users");
  if (searchUsr) {
    searchUsr.addEventListener("input", (e) => loadAdminUsers(e.target.value));
  }

  const adminTxnType = document.getElementById("admin-txn-type-filter");
  if (adminTxnType) {
    adminTxnType.addEventListener("change", loadAdminTransactions);
  }

  const adminTxnSearch = document.getElementById("admin-txn-search");
  if (adminTxnSearch) {
    adminTxnSearch.addEventListener("input", (e) => loadAdminTransactions());
  }
}
