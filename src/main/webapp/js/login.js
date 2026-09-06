const loginForm = document.getElementById("loginForm");
loginForm.addEventListener("submit", async function(event) {
    event.preventDefault();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const message = document.getElementById("message");
    message.textContent = "";
    try {
        const response = await fetch(
            "/review-platform/users/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );
        if (!response.ok) {
            message.textContent ="Invalid email or password";
            return;
        }
        const data = await response.json();
        localStorage.setItem("loggedInUser",
            JSON.stringify(data.user)
        );
        localStorage.setItem("isAdmin",
            data.admin
        );
        if (data.admin === true) {
            message.textContent ="Admin login successful!";
            setTimeout(function() {
                window.location.href = "admin.html";
            }, 500);
        } else {
            message.textContent ="Login successful!";
            setTimeout(function() {
                window.location.href = "index.html";
            }, 500);
        }
    } catch (error) {
        console.error(error);
        message.textContent ="Server error. Please try again.";
    }
});