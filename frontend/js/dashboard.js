/**
 * Dashboard Logic - Online Banking Management System
 */

document.addEventListener("DOMContentLoaded", async () => {
  Auth.requireAuth();
  renderNavbar("dashboard");

  const user = Auth.getUser();
  if (user && document.getElementById("welcome-name")) {
    document.getElementById("welcome-name").textContent = user.fullName;
  }

  await loadDashboardData();
});

let currentAccountData = null;
let isAccountMasked = true;

async function loadDashboardData() {
  try {
    // 1. Fetch Account Details
    const accRes = await apiFetch("/accounts/me");
    if (accRes.success && accRes.data) {
      currentAccountData = accRes.data;
      renderAccountCard(currentAccountData);
    }

    // 2. Fetch Recent Transactions
    const txnRes = await apiFetch("/transactions");
    if (txnRes.success && txnRes.data) {
      renderRecentTransactions(txnRes.data.slice(0, 5));
      renderChart(txnRes.data);
    }
  } catch (err) {
    showAlert("dashboard-alert", err.message, "danger");
  }
}

function renderAccountCard(acc) {
  const balanceEl = document.getElementById("account-balance");
  const numberEl = document.getElementById("account-number");
  const typeEl = document.getElementById("account-type");
  const statusEl = document.getElementById("account-status");

  if (balanceEl) balanceEl.textContent = formatCurrency(acc.balance);
  if (typeEl) typeEl.textContent = `${acc.accountType} ACCOUNT`;

  if (statusEl) {
    const isBlocked = acc.status === "BLOCKED";
    statusEl.textContent = acc.status;
    statusEl.className = isBlocked ? "badge bg-danger" : "badge bg-success";
  }

  updateAccountNumberDisplay();
}

function toggleAccountMask() {
  isAccountMasked = !isAccountMasked;
  updateAccountNumberDisplay();
}

function updateAccountNumberDisplay() {
  const numberEl = document.getElementById("account-number");
  const eyeIcon = document.getElementById("eye-icon");
  if (!numberEl || !currentAccountData) return;

  if (isAccountMasked) {
    numberEl.textContent = maskAccountNumber(currentAccountData.accountNumber);
    if (eyeIcon) eyeIcon.className = "bi bi-eye";
  } else {
    numberEl.textContent = currentAccountData.accountNumber;
    if (eyeIcon) eyeIcon.className = "bi bi-eye-slash";
  }
}

function renderRecentTransactions(transactions) {
  const tbody = document.getElementById("recent-txns-tbody");
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No recent transactions found.</td></tr>`;
    return;
  }

  const userAcc = currentAccountData ? currentAccountData.accountNumber : "";

  tbody.innerHTML = transactions.map(t => {
    let typeBadgeClass = "badge-transfer";
    if (t.transactionType === "DEPOSIT") typeBadgeClass = "badge-deposit";
    if (t.transactionType === "WITHDRAW") typeBadgeClass = "badge-withdraw";

    const isCredit = (t.transactionType === "DEPOSIT") || (t.transactionType === "TRANSFER" && t.receiverAccount === userAcc);
    const amountClass = isCredit ? "text-success fw-bold" : "text-danger fw-bold";
    const amountPrefix = isCredit ? "+" : "-";

    return `
      <tr>
        <td class="fw-semibold text-primary"><small>${t.transactionReference}</small></td>
        <td><span class="badge ${typeBadgeClass}">${t.transactionType}</span></td>
        <td>${t.description || '-'}</td>
        <td><small class="text-muted">${formatDate(t.createdAt)}</small></td>
        <td class="${amountClass}">${amountPrefix}${formatCurrency(t.amount)}</td>
        <td>
          <span class="badge ${t.transactionStatus === 'SUCCESS' ? 'badge-status-success' : 'badge-status-failed'}">
            ${t.transactionStatus}
          </span>
        </td>
      </tr>
    `;
  }).join("");
}

function renderChart(transactions) {
  const chartCanvas = document.getElementById("transactionChart");
  if (!chartCanvas || typeof Chart === "undefined") return;

  let totalDeposits = 0;
  let totalWithdrawals = 0;
  let totalTransfers = 0;

  const userAcc = currentAccountData ? currentAccountData.accountNumber : "";

  transactions.forEach(t => {
    if (t.transactionStatus === "SUCCESS") {
      const amt = Number(t.amount) || 0;
      if (t.transactionType === "DEPOSIT") totalDeposits += amt;
      else if (t.transactionType === "WITHDRAW") totalWithdrawals += amt;
      else if (t.transactionType === "TRANSFER") {
        if (t.senderAccount === userAcc) totalWithdrawals += amt;
        else totalDeposits += amt;
      }
    }
  });

  new Chart(chartCanvas, {
    type: 'doughnut',
    data: {
      labels: ['Inflow / Deposits', 'Outflow / Debits'],
      datasets: [{
        data: [totalDeposits, totalWithdrawals],
        backgroundColor: ['#10b981', '#ef4444'],
        hoverOffset: 4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom' }
      }
    }
  });
}
