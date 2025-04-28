/** Password view **/
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("toggle-password").addEventListener("click", function () {
        const passwordInput = document.getElementById("password");
        const eyeCross = document.querySelector(".eye-cross");

        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            eyeCross.style.opacity = "1";
        } else {
            passwordInput.type = "password";
            eyeCross.style.opacity = "0";
        }
    });
});

/** Invalid error input change **/
document.addEventListener("DOMContentLoaded", function () {
    const inputs = document.querySelectorAll(".form-control");

    inputs.forEach(input => {
        input.addEventListener("input", () => {
            if (input.classList.contains("is-invalid")) {
                input.classList.remove("is-invalid");

                const feedback = input.closest(".auth-form-item, .profile-form-item").querySelector(".invalid-feedback");
                if (feedback) {
                    feedback.style.display = "none";
                }
            }
        });
    });
});
