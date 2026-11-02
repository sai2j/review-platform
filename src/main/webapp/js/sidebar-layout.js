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
        home: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>',
        write: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/></svg>',
        browse: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/></svg>',
        profile: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>',
        business: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="3" width="16" height="18" rx="1"/><path d="M9 7h2M13 7h2M9 11h2M13 11h2M10 21v-4h4v4"/></svg>',
        claim: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M9 12l2 2 4-4"/></svg>',
        dashboard: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg>',
        logout: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5M21 12H9"/></svg>'
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