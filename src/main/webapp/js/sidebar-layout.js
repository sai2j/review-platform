/* =========================================================
   SIDEBAR LAYOUT (same sidebar as My Business / My Profile)

   Logged-in users: the old black top navbar is hidden and the
   dashboard sidebar is shown on the left of the page.
   Visitors (not logged in): page stays exactly as it was.

   Add this in <head> of the page, after navbar.css:
   <link rel="stylesheet" href="css/sidebar-layout.css">
   <script src="js/sidebar-layout.js"></script>

   Phone view (up to 900px): a top bar (logo + avatar + menu button)
   and the sidebar becomes a full-height drawer from the right.
   Pages that already have their own <aside class="sidebar">
   (My Profile, My Business, Claim Business, Admin) only get the
   phone drawer - their desktop view is not touched.
========================================================= */

(function () {
    "use strict";

    var user = null;

    try {
        user = JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (error) {
        user = null;
    }

    // Pages that already contain their own sidebar: only add the phone drawer
    var OWN_SIDEBAR_PAGES = [
        "user-profile.html",
        "user-dashboard.html",
        "business-claim.html",
        "admin.html"
    ];

    var currentPage = (
        window.location.pathname.split("/").pop() || "index.html"
    ).toLowerCase();

    if (OWN_SIDEBAR_PAGES.indexOf(currentPage) !== -1) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", function () {
                initDrawer(document.querySelector("aside.sidebar"));
            });
        } else {
            initDrawer(document.querySelector("aside.sidebar"));
        }
        return;
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

        initDrawer(sidebar);

        var logoutButton = document.getElementById("rpLogoutButton");

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                localStorage.removeItem("loggedInUser");
                localStorage.removeItem("myBusiness");
                window.location.href = "login.html";
            });
        }
    }

    // ---------- Phone view: top bar + right side drawer ----------
    function initDrawer(aside) {
        if (!aside || !document.body || aside.classList.contains("rp-drawer")) {
            return;
        }

        var body = document.body;

        body.classList.add("rp-dw");
        aside.classList.add("rp-drawer");

        if (!aside.id) {
            aside.id = "rpDrawer";
        }

        // Top bar (logo + avatar + menu button)
        var tagEl = aside.querySelector(".brand small");
        var tagHtml = tagEl ? '<em class="rp-top-tag">' + escapeHtml(tagEl.textContent) + "</em>" : "";

        var topbar = document.createElement("header");
        topbar.className = "rp-topbar";
        topbar.innerHTML =
            '<a class="rp-top-brand" href="index.html">Review<span>Platform</span>' + tagHtml + "</a>" +
            '<div class="rp-top-actions">' +
            '<span class="rp-top-avatar" aria-hidden="true">U</span>' +
            '<button type="button" class="rp-burger" aria-label="Open menu" aria-expanded="false" aria-controls="' + aside.id + '">' +
            "<span></span><span></span><span></span></button>" +
            "</div>";
        body.insertBefore(topbar, body.firstChild);

        // Close button (X) inside the drawer, on the right of the logo
        var closeBtn = document.createElement("button");
        closeBtn.type = "button";
        closeBtn.className = "rp-close";
        closeBtn.setAttribute("aria-label", "Close menu");
        closeBtn.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg>';
        aside.insertBefore(closeBtn, aside.firstChild);

        // Dim background
        var overlay = document.createElement("div");
        overlay.className = "rp-overlay";
        body.appendChild(overlay);

        // Keep the top bar avatar in sync with the sidebar avatar
        var avatarSource = aside.querySelector(".rp-avatar, .user-avatar, .avatar, .sf-avatar");
        var topAvatar = topbar.querySelector(".rp-top-avatar");

        function syncAvatar() {
            var text = avatarSource ? avatarSource.textContent.trim() : "";

            if (!text && user && user.email) {
                text = String(user.email).charAt(0);
            }

            topAvatar.textContent = (text || "U").slice(0, 2).toUpperCase();
        }

        syncAvatar();

        if (avatarSource && window.MutationObserver) {
            new MutationObserver(syncAvatar).observe(avatarSource, {
                childList: true,
                characterData: true,
                subtree: true
            });
        }

        var burger = topbar.querySelector(".rp-burger");

        function setOpen(open) {
            aside.classList.toggle("rp-open", open);
            overlay.classList.toggle("rp-open", open);
            body.classList.toggle("rp-lock", open);
            burger.setAttribute("aria-expanded", open ? "true" : "false");
        }

        burger.addEventListener("click", function () {
            setOpen(true);
        });

        topAvatar.addEventListener("click", function () {
            setOpen(true);
        });

        closeBtn.addEventListener("click", function () {
            setOpen(false);
        });

        overlay.addEventListener("click", function () {
            setOpen(false);
        });

        // Tapping any link / button inside the drawer closes it
        aside.addEventListener("click", function (event) {
            if (event.target.closest("a, .side-link, .logout-btn, .rp-logout, button.btn")) {
                setOpen(false);
            }
        });

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                setOpen(false);
            }
        });

        window.addEventListener("resize", function () {
            if (window.innerWidth > 900) {
                setOpen(false);
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", buildLayout);
    } else {
        buildLayout();
    }
})();