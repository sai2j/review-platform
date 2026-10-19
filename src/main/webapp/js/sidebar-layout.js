/* =========================================================
   SIDEBAR LAYOUT (same sidebar as My Business / My Profile)

   Logged-in users: the old black top navbar is hidden and the
   dashboard sidebar is shown on the left of the page.
   Visitors (not logged in): page stays exactly as it was.

   Add this in <head> of the page, after navbar.css:
   <link rel="stylesheet" href="css/sidebar-layout.css">
   <script src="js/sidebar-layout.js"></script>
========================================================= */

(function () {
    "use strict";

    var user = null;

    try {
        user = JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (error) {
        user = null;
    }

    // Visitors keep the normal top navbar
    if (!user || !user.id) {
        return;
    }

    // Hide the old top navbar right away (no flicker)
    document.documentElement.classList.add("rp-has-sidebar");

    var page = (
        window.location.pathname.split("/").pop() || "index.html"
    ).toLowerCase();

    // Same colourful icons as the My Profile page sidebar
    var ICONS = {
        home: "🏠",
        write: "✏️",
        browse: "🌐",
        profile: "👤",
        business: "🏢",
        claim: "🛡️",
        dashboard: "📊",
        logout: "🚪"
    };

    var LINKS = [
        { href: "index.html", label: "Home", icon: "home", pages: ["index.html"] },
        { href: "review.html", label: "Write a Review", icon: "write", pages: ["review.html"] },
        { href: "browse.html", label: "Browse Websites", icon: "browse", pages: ["browse.html", "category.html", "website.html"] },
        { href: "user-profile.html", label: "My Profile", icon: "profile", pages: ["user-profile.html"] },
        { href: "user-dashboard.html", label: "My Business", icon: "business", pages: ["user-dashboard.html"] },
        { href: "business-claim.html", label: "Claim Business", icon: "claim", pages: ["business-claim.html"] },
        { href: "business-dashboard.html", label: "Business Dashboard", icon: "dashboard", pages: ["business-dashboard.html", "subscription.html"] }
    ];

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function icon(name) {
        return '<span class="rp-icon" aria-hidden="true">' + ICONS[name] + "</span>";
    }

    function buildSidebarHtml() {
        var email = user.email ? String(user.email) : "User";
        var initial = email.charAt(0).toUpperCase();

        var links = LINKS.map(function (item) {
            var active = item.pages.indexOf(page) !== -1;

            return (
                '<a href="' + item.href + '" class="rp-link' + (active ? " active" : "") + '">' +
                icon(item.icon) + item.label +
                "</a>"
            );
        }).join("");

        return (
            '<div class="rp-brand">Review<span>Platform</span></div>' +
            '<nav class="rp-nav" aria-label="Dashboard navigation">' + links + "</nav>" +
            '<div class="rp-footer">' +
            '<div class="rp-user-card">' +
            '<div class="rp-avatar">' + escapeHtml(initial) + "</div>" +
            '<div class="rp-user-text">' +
            '<span class="rp-user-label">Logged in as</span>' +
            '<span class="rp-user-email">' + escapeHtml(email) + "</span>" +
            "</div></div>" +
            '<button type="button" class="rp-logout" id="rpLogoutButton">' +
            icon("logout") + "Logout</button>" +
            "</div>"
        );
    }

    function buildLayout() {
        if (!document.body || document.querySelector(".rp-shell")) {
            return;
        }

        var shell = document.createElement("div");
        shell.className = "rp-shell";

        var sidebar = document.createElement("aside");
        sidebar.className = "rp-sidebar";
        sidebar.innerHTML = buildSidebarHtml();

        var main = document.createElement("div");
        main.className = "rp-main";

        // Move the whole existing page into the right-hand side
        while (document.body.firstChild) {
            main.appendChild(document.body.firstChild);
        }

        shell.appendChild(sidebar);
        shell.appendChild(main);
        document.body.appendChild(shell);

        var logoutButton = document.getElementById("rpLogoutButton");

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                localStorage.removeItem("loggedInUser");
                localStorage.removeItem("myBusiness");
                window.location.href = "login.html";
            });
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", buildLayout);
    } else {
        buildLayout();
    }
})();