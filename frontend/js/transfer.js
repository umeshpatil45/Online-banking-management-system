/**
 * Transfer Money Logic - Confirmation and Verification Flow
 */

document.addEventListener("DOMContentLoaded", async () => {
  Auth.requireAuth();
  renderNavbar("transfer");

  await loadSenderAccount();
  setupTransferEventListeners();
});

let senderAccount = null;
let verifiedReceiver = null;

async function loadSenderAccount() {
  try {
    const res = await apiFetch("/accounts/me");
    if (res.success && res.data) {
      senderAccount = res.data;
      document.getElementById("sender-balance-badge").textContent = formatCurrency(senderAccount.balance);
      document.getElementById("sender-acc-num").textContent = senderAccount.accountNumber;
    }
  } catch (err) {
    showAlert("transfer-alert", err.message, "danger");
  }
}

function setupTransferEventListeners() {
  const receiverInput = document.getElementById("receiver-acc");
  const amountInput = document.getElementById("transfer-amount");
  const transferForm = document.getElementById("transfer-form");
  const confirmBtn = document.getElementById("btn-confirm-transfer");

  if (receiverInput) {
    receiverInput.addEventListener("blur", verifyReceiverAccount);
  }

  if (transferForm) {
    transferForm.addEventListener("submit", (e) => {
      e.preventDefault();
      handleTransferPrecheck();
    });
  }

  if (confirmBtn) {
    confirmBtn.addEventListener("click", executeTransfer);
  }
}

async function verifyReceiverAccount() {
  const accNum = document.getElementById("receiver-acc").value.trim();
  const verifyFeedback = document.getElementById("receiver-verify-feedback");
  if (!verifyFeedback) return;

  if (!accNum) {
    verifyFeedback.style.display = "none";
    verifiedReceiver = null;
    return;
  }

  if (senderAccount && accNum === senderAccount.accountNumber) {
    verifyFeedback.className = "small text-danger mt-1";
    verifyFeedback.innerHTML = '<i class="bi bi-exclamation-triangle"></i> You cannot transfer money to your own account.';
    verifyFeedback.style.display = "block";
    verifiedReceiver = null;
    return;
  }

  try {
    const res = await apiFetch(`/accounts/${accNum}`);
    if (res.success && res.data) {
      verifiedReceiver = res.data;
      verifyFeedback.className = "small text-success mt-1";
      verifyFeedback.innerHTML = `<i class="bi bi-check-circle-fill"></i> Verified Beneficiary: <strong>${verifiedReceiver.userName}</strong> (${verifiedReceiver.accountType})`;
      verifyFeedback.style.display = "block";
    }
  } catch (err) {
    verifiedReceiver = null;
    verifyFeedback.className = "small text-danger mt-1";
    verifyFeedback.innerHTML = `<i class="bi bi-x-circle-fill"></i> Receiver account not found.`;
    verifyFeedback.style.display = "block";
  }
}

function handleTransferPrecheck() {
  const receiverAcc = document.getElementById("receiver-acc").value.trim();
  const amount = Number(document.getElementById("transfer-amount").value);
  const desc = document.getElementById("transfer-desc").value.trim();

  if (!receiverAcc) {
    showAlert("transfer-alert", "Please enter receiver account number.", "warning");
    return;
  }

  if (!amount || amount <= 0) {
    showAlert("transfer-alert", "Please enter a valid transfer amount greater than 0.", "warning");
    return;
  }

  if (senderAccount && amount > Number(senderAccount.balance)) {
    showAlert("transfer-alert", "Insufficient balance for this transaction.", "danger");
    return;
  }

  // Populate confirmation modal
  document.getElementById("confirm-modal-amount").textContent = formatCurrency(amount);
  document.getElementById("confirm-modal-receiver").textContent = receiverAcc;
  document.getElementById("confirm-modal-receiver-name").textContent = verifiedReceiver ? verifiedReceiver.userName : "Verified Account Holder";
  document.getElementById("confirm-modal-desc").textContent = desc || "Fund Transfer";

  const confirmModal = new bootstrap.Modal(document.getElementById("transferConfirmModal"));
  confirmModal.show();
}

async function executeTransfer() {
  const receiverAcc = document.getElementById("receiver-acc").value.trim();
  const amount = Number(document.getElementById("transfer-amount").value);
  const desc = document.getElementById("transfer-desc").value.trim();

  const confirmModalEl = document.getElementById("transferConfirmModal");
  const confirmModal = bootstrap.Modal.getInstance(confirmModalEl);
  if (confirmModal) confirmModal.hide();

  const submitBtn = document.getElementById("btn-transfer-submit");
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> Processing...`;
  }

  try {
    const res = await apiFetch("/transactions/transfer", {
      method: "POST",
      body: JSON.stringify({
        receiverAccountNumber: receiverAcc,
        amount: amount,
        description: desc
      })
    });

    if (res.success && res.data) {
      // Show Success Receipt Card
      document.getElementById("transfer-form-card").style.display = "none";
      const receiptCard = document.getElementById("transfer-receipt-card");
      receiptCard.style.display = "block";

      document.getElementById("receipt-ref").textContent = res.data.transactionReference;
      document.getElementById("receipt-amount").textContent = formatCurrency(res.data.amount);
      document.getElementById("receipt-receiver").textContent = res.data.receiverAccount;
      document.getElementById("receipt-date").textContent = formatDate(res.data.createdAt);
      document.getElementById("receipt-new-balance").textContent = formatCurrency(res.data.balanceAfterTransaction);
      document.getElementById("receipt-desc").textContent = res.data.description || "-";

      // Refresh sender balance in header
      await loadSenderAccount();
    }
  } catch (err) {
    showAlert("transfer-alert", err.message, "danger");
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<i class="bi bi-arrow-right-circle me-1"></i> Transfer Money`;
    }
  }
}

function resetTransferForm() {
  document.getElementById("transfer-form").reset();
  document.getElementById("receiver-verify-feedback").style.display = "none";
  document.getElementById("transfer-form-card").style.display = "block";
  document.getElementById("transfer-receipt-card").style.display = "none";
  verifiedReceiver = null;
}
