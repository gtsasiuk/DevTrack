document.addEventListener("DOMContentLoaded", function () {
    const totalPriceInput = document.getElementById("totalPrice");
    const advancePaymentInput = document.getElementById("advancePayment");
    const advanceWarning = document.getElementById("advanceWarning");

    function validateAdvancePayment() {
        const totalPrice = parseFloat(totalPriceInput.value) || 0;
        const advancePayment = parseFloat(advancePaymentInput.value) || 0;

        const submitBtn = document.querySelector(".btn-project-submit");

        if (advancePayment > totalPrice) {
            advanceWarning.style.display = "block";

            advancePaymentInput.classList.add("is-invalid");
            totalPriceInput.classList.add("is-invalid");

            if (submitBtn) {
                submitBtn.disabled = true;
            }
        } else {
            advanceWarning.style.display = "none";

            advancePaymentInput.classList.remove("is-invalid");
            totalPriceInput.classList.remove("is-invalid");

            if (submitBtn) {
                submitBtn.disabled = false;
            }
        }
    }

    totalPriceInput.addEventListener("input", validateAdvancePayment);
    advancePaymentInput.addEventListener("input", validateAdvancePayment);

    validateAdvancePayment();
});