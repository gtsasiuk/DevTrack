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