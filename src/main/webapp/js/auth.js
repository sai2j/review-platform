/* =========================================================
   LOG IN OR SIGN UP (one page)
   - Continue with Google   (Google Identity Services)
   - Continue with Facebook (Facebook JS SDK)
   - Continue with email    (checks if the account exists, then
                             asks for a password or creates one)
   Open as:  auth.html   or   auth.html?next=write-review.html%3FwebsiteId%3D1
========================================================= */

(function () {
    "use strict";

    var USER_API = "/review-platform/users";

    var email = "";
    var busy = false;
    var facebookReady = false;

    function $(id) {
        return document.getElementById(id);
    }

    /* ---------- Where to go after login ---------- */

    function safeNext() {

        var next = new URLSearchParams(window.location.search).get("next") || "";

        /* only a plain page of this site, e.g. review.html?websiteId=3 */
        return /^[A-Za-z0-9_-]+\.html(\?[A-Za-z0-9_=&%.-]*)?$/.test(next) ? next : "";
    }

    function finishLogin(data) {

        if (!data || !data.user) {
            showMessage("Invalid response from the server.");
            return;
        }

        localStorage.setItem("loggedInUser", JSON.stringify(data.user));
        localStorage.setItem("isAdmin", String(data.admin === true));

        showMessage("You are logged in.", true);

        if (data.admin === true) {
            go("admin.html");
            return;
        }

        var next = safeNext();

        if (next) {
            go(next);
            return;
        }

        fetch("/review-platform/business-dashboard/user/" + data.user.id)
            .then(function (response) {
                return response.ok ? response.json() : null;
            })
            .then(function (business) {
                go(business && business.id ? "business-dashboard.html" : "index.html");
            })
            .catch(function () {
                go("index.html");
            });
    }

    function go(page) {
        setTimeout(function () {
            window.location.href = page;
        }, 400);
    }

    /* ---------- Small UI helpers ---------- */

    function showMessage(text, ok) {

        var box = $("auMessage");

        box.textContent = text || "";
        box.className = "au-message" + (ok ? " ok" : "");
    }

    function showStep(id) {

        ["stepChoose", "stepEmail", "stepPassword", "stepSignup"].forEach(function (step) {
            $(step).hidden = step !== id;
        });

        showMessage("");

        var focusId = { stepEmail: "emailInput", stepPassword: "loginPassword", stepSignup: "signupName" }[id];

        if (focusId) {
            setTimeout(function () { $(focusId).focus(); }, 60);
        }
    }

    function setBusy(button, isBusy, label) {

        busy = isBusy;
        button.disabled = isBusy;

        if (label) {
            button.dataset.label = button.dataset.label || button.textContent;
            button.textContent = isBusy ? label : button.dataset.label;
        }
    }

    function postJson(path, body) {

        return fetch(USER_API + path, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        }).then(function (response) {

            return response.json().catch(function () { return {}; }).then(function (data) {
                return { ok: response.ok, status: response.status, data: data || {} };
            });
        });
    }

    function errorText(result, fallback) {

        if (result.status === 429) {
            return "Too many attempts. Please wait a minute and try again.";
        }

        var data = result.data;

        if (typeof data.error === "string" && data.error) { return data.error; }
        if (typeof data.message === "string" && data.message) { return data.message; }

        return fallback;
    }

    /* ---------- Email flow ---------- */

    function validEmail(value) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
    }

    function onEmailSubmit(event) {

        event.preventDefault();

        if (busy) { return; }

        var value = $("emailInput").value.trim().toLowerCase();

        $("emailInput").classList.remove("invalid");

        if (!validEmail(value)) {
            $("emailInput").classList.add("invalid");
            showMessage("Please enter a valid email address.");
            return;
        }

        var button = $("emailNext");

        setBusy(button, true, "Checking...");
        showMessage("");

        postJson("/auth/check-email", { email: value })
            .then(function (result) {

                setBusy(button, false, "Checking...");

                if (!result.ok) {
                    showMessage(errorText(result, "Something went wrong. Please try again."));
                    return;
                }

                email = value;

                document.querySelectorAll("[data-email]").forEach(function (el) {
                    el.textContent = email;
                });

                showStep(result.data.exists ? "stepPassword" : "stepSignup");
            })
            .catch(function () {
                setBusy(button, false, "Checking...");
                showMessage("Could not reach the server. Please try again.");
            });
    }

    function onLogin(event) {

        event.preventDefault();

        if (busy) { return; }

        var password = $("loginPassword").value;

        if (!password) {
            $("loginPassword").classList.add("invalid");
            showMessage("Please enter your password.");
            return;
        }

        var button = $("loginSubmit");

        setBusy(button, true, "Logging in...");
        showMessage("");

        postJson("/login", { email: email, password: password })
            .then(function (result) {

                setBusy(button, false, "Logging in...");

                if (!result.ok) {
                    showMessage(result.status === 401
                        ? "That password is not correct."
                        : errorText(result, "Login failed. Please try again."));
                    return;
                }

                finishLogin(result.data);
            })
            .catch(function () {
                setBusy(button, false, "Logging in...");
                showMessage("Could not reach the server. Please try again.");
            });
    }

    function onSignup(event) {

        event.preventDefault();

        if (busy) { return; }

        var name = $("signupName").value.trim();
        var password = $("signupPassword").value;

        $("signupPassword").classList.remove("invalid");

        if (password.length < 8) {
            $("signupPassword").classList.add("invalid");
            showMessage("Your password must be at least 8 characters.");
            return;
        }

        var button = $("signupSubmit");

        setBusy(button, true, "Creating account...");
        showMessage("");

        postJson("/register", { name: name, email: email, password: password })
            .then(function (result) {

                if (!result.ok) {
                    setBusy(button, false, "Creating account...");
                    showMessage(errorText(result, "Could not create your account. Please try again."));
                    return null;
                }

                return postJson("/login", { email: email, password: password });
            })
            .then(function (loginResult) {

                if (!loginResult) { return; }

                setBusy(button, false, "Creating account...");

                if (!loginResult.ok) {
                    showMessage("Your account was created. Please log in.", true);
                    showStep("stepPassword");
                    return;
                }

                finishLogin(loginResult.data);
            })
            .catch(function () {
                setBusy(button, false, "Creating account...");
                showMessage("Could not reach the server. Please try again.");
            });
    }

    /* ---------- Google ---------- */

    function loadScript(src, onload) {

        var script = document.createElement("script");

        script.src = src;
        script.async = true;
        script.defer = true;
        script.onload = onload;
        script.onerror = function () { console.warn("Could not load " + src); };

        document.head.appendChild(script);
    }

    function startGoogle(clientId) {

        loadScript("https://accounts.google.com/gsi/client", function () {

            if (!window.google || !window.google.accounts) { return; }

            window.google.accounts.id.initialize({
                client_id: clientId,
                callback: function (response) {
                    social("/auth/google", { credential: response.credential });
                }
            });

            window.google.accounts.id.renderButton($("googleBtn"), {
                theme: "outline",
                size: "large",
                shape: "pill",
                text: "continue_with",
                logo_alignment: "left",
                width: 320
            });

            $("googleBtn").hidden = false;
        });
    }

    /* ---------- Facebook ---------- */

    function startFacebook(appId) {

        window.fbAsyncInit = function () {

            window.FB.init({ appId: appId, cookie: false, xfbml: false, version: "v21.0" });

            facebookReady = true;
            $("facebookBtn").hidden = false;
        };

        loadScript("https://connect.facebook.net/en_US/sdk.js");

        $("facebookBtn").addEventListener("click", function () {

            if (!facebookReady || busy) { return; }

            window.FB.login(function (response) {

                if (response && response.authResponse && response.authResponse.accessToken) {
                    social("/auth/facebook", { accessToken: response.authResponse.accessToken });
                } else {
                    showMessage("Facebook login was cancelled.");
                }
            }, { scope: "public_profile,email" });
        });
    }

    /* ---------- Send a Google / Facebook token to our server ---------- */

    function social(path, body) {

        if (busy) { return; }

        busy = true;
        showMessage("Signing you in...", true);

        postJson(path, body)
            .then(function (result) {

                busy = false;

                if (!result.ok) {
                    showMessage(errorText(result, "Sign in failed. Please try again."));
                    return;
                }

                finishLogin(result.data);
            })
            .catch(function () {
                busy = false;
                showMessage("Could not reach the server. Please try again.");
            });
    }

    /* ---------- Start ---------- */

    document.addEventListener("DOMContentLoaded", function () {

        $("emailStart").addEventListener("click", function () { showStep("stepEmail"); });

        $("stepEmail").addEventListener("submit", onEmailSubmit);
        $("stepPassword").addEventListener("submit", onLogin);
        $("stepSignup").addEventListener("submit", onSignup);

        document.querySelectorAll("[data-back]").forEach(function (button) {
            button.addEventListener("click", function () { showStep(button.dataset.back); });
        });

        document.querySelectorAll("[data-show]").forEach(function (button) {

            button.addEventListener("click", function () {

                var input = $(button.dataset.show);
                var show = input.type === "password";

                input.type = show ? "text" : "password";
                button.textContent = show ? "Hide" : "Show";
            });
        });

        /* only show the social buttons that are switched on in the server settings */
        fetch(USER_API + "/auth/config")
            .then(function (response) {
                return response.ok ? response.json() : {};
            })
            .then(function (config) {

                if (config.googleClientId) { startGoogle(config.googleClientId); }
                if (config.facebookAppId) { startFacebook(config.facebookAppId); }
            })
            .catch(function () { /* email login still works */ });
    });
})();