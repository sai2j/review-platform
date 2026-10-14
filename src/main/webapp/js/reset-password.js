
const form = document.getElementById("resetPasswordForm");
const passwordInput = document.getElementById("newPassword");
const confirmPasswordInput = document.getElementById("confirmPassword");
const submitBtn = document.getElementById("submitBtn");
const message = document.getElementById("message");

// Read the reset token from the email link URL.
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get("token");

if (!token || !token.trim()) {
    showMessage(
        "Reset link is missing or invalid. Please request a new password reset link.",
        "error"
    );
    submitBtn.disabled = true;
}

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    if (!token || !token.trim()) {
        showMessage("Please request a new password reset link.", "error");
        return;
    }

    const newPassword = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (newPassword.length < 8 || newPassword.length > 100) {
        showMessage(
            "Password must be between 8 and 100 characters.",
            "error"
        );
        return;
    }

    if (newPassword !== confirmPassword) {
        showMessage("Passwords do not match.", "error");
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Resetting...";
    showMessage("", "");

    try {
        const response = await fetch(
            "/review-platform/users/password/reset",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    token: token.trim(),
                    newPassword: newPassword
                })
            }
        );

        const result = await response.json();

        if (!response.ok || result.success === false) {
            throw new Error(
                result.message || "Unable to reset password."
            );
        }

        showMessage(
            result.message || "Password reset successfully. You can now log in.",
            "success"
        );

        form.reset();
        passwordInput.disabled = true;
        confirmPasswordInput.disabled = true;
        submitBtn.textContent = "Password Reset Successful";

    } catch (error) {
        showMessage(
            error.message || "Server error. Please try again later.",
            "error"
        );

        submitBtn.disabled = false;
        submitBtn.textContent = "Reset Password";
    }
});

function showMessage(text, type) {
    message.textContent = text;
    message.className = type;
}