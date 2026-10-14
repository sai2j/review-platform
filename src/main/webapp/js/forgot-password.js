
const form = document.getElementById("forgotPasswordForm");
const emailInput = document.getElementById("email");
const submitBtn = document.getElementById("submitBtn");
const message = document.getElementById("message");

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = emailInput.value.trim();

    if (!email) {
        showMessage("Please enter your email address.", "error");
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Sending...";
    showMessage("", "");

    try {
        const response = await fetch(
            "/review-platform/users/password/forgot",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email: email })
            }
        );

        const result = await response.json();

        if (!response.ok || result.success === false) {
            throw new Error(
                result.message || "Unable to process your request."
            );
        }

        showMessage(
            result.message ||
            "If an account exists for this email, a reset link will be sent.",
            "success"
        );

        form.reset();

    } catch (error) {
        showMessage(
            error.message || "Server error. Please try again later.",
            "error"
        );
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = "Send Reset Link";
    }
});

function showMessage(text, type) {
    message.textContent = text;
    message.className = type;
}