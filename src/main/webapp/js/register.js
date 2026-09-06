const registerForm =document.getElementById("registerForm");
registerForm.addEventListener("submit",
    async function(event) {
        event.preventDefault();
        const email =document.getElementById("email").value.trim();
        const password =document.getElementById("password").value;
        const message =document.getElementById("message");
        message.textContent = "";
        try {
            const response =
                await fetch("/review-platform/users/register",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type":
                                "application/json"
                        },
                        body: JSON.stringify({
                            email: email,
                            password: password
                        })
                    }
                );
            if (response.ok) {
                message.textContent ="Registration successful!";
                registerForm.reset();
                setTimeout(function() {
                    window.location.href =
                        "index.html";
                }, 1000);
            }
            else {
                const error =await response.text();
                message.textContent =error ||"Registration failed";
            }
        }
        catch (error) {
            console.error("Registration Error:",error);
            message.textContent ="Server error. Please try again.";
        }
    }
);