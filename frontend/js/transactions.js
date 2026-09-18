/**
 * Transactions Logic - Search, Filter, and Export
 */

document.addEventListener("DOMContentLoaded", async () => {
  Auth.requireAuth();
  renderNavbar("transactions");

  await initTransactionsPage();
});

let allTransactions = [];
let userAccountNumber = "";

async function initTransactionsPage() {
  try {
    const accRes = await apiFetch("/accounts/me");
    if (accRes.success && accRes.data) {
      userAccountNumber = accRes.data.accountNumber;
    }
    await loadTransactions();
  } catch (err) {
    showAlert("txn-alert", err.message, "danger");
  }

  // Setup event listeners for filters
  const searchInput = document.getElementById("search-query");
  const typeFilter = document.getElementById("type-filter");
  const startDateInput = document.getElementById("start-date");
  const endDateInput = document.getElementById("end-date");
  const resetBtn = document.getElementById("reset-filter-btn");

  if (searchInput) searchInput.addEventListener("input", debounce(loadTransactions, 350));
  if (typeFilter) typeFilter.addEventListener("change", loadTransactions);
  if (startDateInput) startDateInput.addEventListener("change", loadTransactions);
  if (endDateInput) endDateInput.addEventListener("change", loadTransactions);

  if (resetBtn) {
    resetBtn.addEventListener("click", () => {
      if (searchInput) searchInput.value = "";
      if (typeFilter) typeFilter.value = "";
      if (startDateInput) startDateInput.value = "";
      if (endDateInput) endDateInput.value = "";
      loadTransactions();
    });
  }
}

async function loadTransactions() {
  try {
    const query = document.getElementById("search-query")?.value || "";
    const type = document.getElementById("type-filter")?.value || "";
    const startDate = document.getElementById("start-date")?.value || "";
    const endDate = document.getElementById("end-date")?.value || "";

    const params = new URLSearchParams();
    if (query.trim()) params.append("query", query.trim());
    if (type.trim()) params.append("type", type.trim());
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);

    const url = `/transactions${params.toString() ? '?' + params.toString() : ''}`;
    const res = await apiFetch(url);

    if (res.success && res.data) {
      allTransactions = res.data;
      renderTransactionTable(allTransactions);
    }
  } catch (err) {
    showAlert("txn-alert", err.message, "danger");
  }
}

function renderTransactionTable(transactions) {
  const tbody = document.getElementById("transactions-tbody");
  const countBadge = document.getElementById("txn-count-badge");
  if (countBadge) countBadge.textContent = `${transactions.length} record(s)`;
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="9" class="text-center text-muted py-5">
          <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
          No transactions match your criteria.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = transactions.map(t => {
    let typeBadgeClass = "badge-transfer";
    if (t.transactionType === "DEPOSIT") typeBadgeClass = "badge-deposit";
    if (t.transactionType === "WITHDRAW") typeBadgeClass = "badge-withdraw";

    const isCredit = (t.transactionType === "DEPOSIT") || (t.transactionType === "TRANSFER" && t.receiverAccount === userAccountNumber);
    const amountClass = isCredit ? "text-success fw-bold" : "text-danger fw-bold";
    const amountPrefix = isCredit ? "+" : "-";

    return `
      <tr>
        <td class="fw-semibold text-primary">
          <a href="javascript:void(0)" onclick="viewTransactionDetail(${t.id})" class="text-decoration-none">
            ${t.transactionReference}
          </a>
        </td>
        <td><small class="text-muted">${formatDate(t.createdAt)}</small></td>
        <td><span class="badge ${typeBadgeClass}">${t.transactionType}</span></td>
        <td class="${amountClass}">${amountPrefix}${formatCurrency(t.amount)}</td>
        <td><small>${t.senderAccount || '-'}</small></td>
        <td><small>${t.receiverAccount || '-'}</small></td>
        <td><small class="text-muted">${t.description || '-'}</small></td>
        <td class="text-end fw-semibold">${formatCurrency(t.balanceAfterTransaction)}</td>
        <td>
          <span class="badge ${t.transactionStatus === 'SUCCESS' ? 'badge-status-success' : 'badge-status-failed'}">
            ${t.transactionStatus}
          </span>
        </td>
      </tr>
    `;
  }).join("");
}

function viewTransactionDetail(id) {
  const txn = allTransactions.find(t => t.id === id);
  if (!txn) return;

  document.getElementById("modal-txn-ref").textContent = txn.transactionReference;
  document.getElementById("modal-txn-type").textContent = txn.transactionType;
  document.getElementById("modal-txn-date").textContent = formatDate(txn.createdAt);
  document.getElementById("modal-txn-amount").textContent = formatCurrency(txn.amount);
  document.getElementById("modal-txn-sender").textContent = txn.senderAccount || "N/A";
  document.getElementById("modal-txn-receiver").textContent = txn.receiverAccount || "N/A";
  document.getElementById("modal-txn-balance").textContent = formatCurrency(txn.balanceAfterTransaction);
  document.getElementById("modal-txn-status").textContent = txn.transactionStatus;
  document.getElementById("modal-txn-desc").textContent = txn.description || "N/A";

  const modalEl = document.getElementById("txnDetailModal");
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
}

function exportTransactionsToCSV() {
  if (!allTransactions || allTransactions.length === 0) {
    alert("No transactions available to export.");
    return;
  }

  const headers = ["Reference,Date,Type,Amount,Sender,Receiver,Description,Balance After,Status"];
  const rows = allTransactions.map(t => [
    `"${t.transactionReference}"`,
    `"${t.createdAt}"`,
    `"${t.transactionType}"`,
    t.amount,
    `"${t.senderAccount || ''}"`,
    `"${t.receiverAccount || ''}"`,
    `"${(t.description || '').replace(/"/g, '""')}"`,
    t.balanceAfterTransaction,
    `"${t.transactionStatus}"`
  ].join(","));

  const csvContent = "data:text/csv;charset=utf-8," + [headers, ...rows].join("\n");
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `Transactions_${new Date().toISOString().slice(0,10)}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}
