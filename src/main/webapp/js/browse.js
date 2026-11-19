const BROWSE_API =
    "/review-platform/websites";

const BROWSE_PAGE_SIZE = 10;

const BROWSE_ICONS = {
    all: '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/>',
    bank: '<path d="M3 10l9-6 9 6"/><path d="M5 10v8M9.5 10v8M14.5 10v8M19 10v8"/><path d="M3 20h18"/>',
    cart: '<circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.6 12.4a1 1 0 0 0 1 .8h9.2a1 1 0 0 0 1-.8L21 7H6"/>',
    plane: '<path d="M10.5 13.5L3 11l1.5-1.5 8-1L16 4.5a1.8 1.8 0 0 1 2.5 2.5l-4 3.5-1 8L12 20l-2.5-6.5z"/>',
    food: '<path d="M7 3v8M4 3v5a3 3 0 0 0 6 0V3M7 11v10"/><path d="M17 3c-2 2-3 4.5-3 7h3v11"/>',
    laptop: '<rect x="4" y="5" width="16" height="11" rx="1.5"/><path d="M2 19h20"/>',
    book: '<path d="M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2z"/><path d="M4 21V5"/><path d="M9 7h6"/>'
};

const BROWSE_CATEGORIES = [
    { value: "", label: "All", icon: "all" },
    { value: "Bank", label: "Banks", icon: "bank" },
    { value: "Shopping", label: "Shopping", icon: "cart" },
    { value: "Travel", label: "Travel", icon: "plane" },
    { value: "Food", label: "Food", icon: "food" },
    { value: "Technology", label: "Technology", icon: "laptop" },
    { value: "Education", label: "Education", icon: "book" }
];

let browseAllSites = [];
let browseSelectedCategory = "";
let browseVisibleCount = BROWSE_PAGE_SIZE;
let browseMinRating = 0;

const BROWSE_RATING_OPTIONS = [
    { value: 0, label: "Any" },
    { value: 4, label: "4+ \u2605" },
    { value: 3, label: "3+ \u2605" },
    { value: 2, label: "2+ \u2605" }
];

const BROWSE_SORT_OPTIONS = [
    { value: "rating", label: "Best rated" },
    { value: "reviews", label: "Most reviewed" },
    { value: "name", label: "A to Z" }
];

function browseEscape(value) {

    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function browseDomain(website) {

    if (website.canonicalDomain) {
        return website.canonicalDomain;
    }

    try {
        return new URL(website.url).hostname.replace(/^www\./, "");
    } catch (error) {
        return website.url || "";
    }
}

function browseStars(rating) {

    const safe = Math.max(0, Math.min(5, Number(rating) || 0));

    let html = "";

    for (let i = 1; i <= 5; i++) {
        html += '<span class="cat-star' +
            (i <= Math.round(safe) ? " filled" : "") + '">★</span>';
    }

    return html;
}

function browseRatingLabel(rating) {

    const value = Number(rating) || 0;

    if (value >= 4.5) return "Excellent";
    if (value >= 4) return "Great";
    if (value >= 3) return "Average";
    if (value >= 2) return "Poor";

    return "Bad";
}

function renderBrowseChips() {

    const chips = document.getElementById("browseChips");

    if (!chips) {
        return;
    }

    chips.innerHTML = BROWSE_CATEGORIES.map(function (item) {

        const active =
            item.value.toLowerCase() === browseSelectedCategory.toLowerCase();

        return '<button type="button" class="category-chip' +
            (active ? " active" : "") +
            '" data-category="' + browseEscape(item.value) + '">' +
            '<svg viewBox="0 0 24 24" aria-hidden="true">' + BROWSE_ICONS[item.icon] + "</svg>" +
            browseEscape(item.label) + "</button>";

    }).join("");
}

function getBrowseFilteredSites() {

    const keyword =
        document.getElementById("browseSearch").value
            .trim().toLowerCase();

    const sort =
        document.getElementById("browseSort").value;

    let sites = browseAllSites.filter(function (website) {

        if (
            browseSelectedCategory &&
            (website.category || "").toLowerCase() !==
                browseSelectedCategory.toLowerCase()
        ) {
            return false;
        }

        if (
            browseMinRating > 0 &&
            (Number(website.reviewCount) || 0) > 0 &&
            (Number(website.averageRating) || 0) < browseMinRating
        ) {
            return false;
        }

        if (browseMinRating > 0 && !(Number(website.reviewCount) > 0)) {
            return false;
        }

        if (!keyword) {
            return true;
        }

        const text =
            ((website.name || "") + " " + browseDomain(website))
                .toLowerCase();

        return text.includes(keyword);
    });

    sites = sites.slice();

    sites.sort(function (a, b) {

        if (sort === "name") {
            return (a.name || "").localeCompare(b.name || "");
        }

        const ratingDiff =
            (Number(b.averageRating) || 0) - (Number(a.averageRating) || 0);

        const countDiff =
            (Number(b.reviewCount) || 0) - (Number(a.reviewCount) || 0);

        if (sort === "reviews") {
            return countDiff !== 0 ? countDiff : ratingDiff;
        }

        return ratingDiff !== 0 ? ratingDiff : countDiff;
    });

    return sites;
}

function renderBrowseList() {

    const list = document.getElementById("browseList");
    const subtitle = document.getElementById("browseSubtitle");
    const showMore = document.getElementById("browseShowMore");

    const sites = getBrowseFilteredSites();

    subtitle.textContent =
        sites.length + (sites.length === 1 ? " website" : " websites");

    const countLabel = document.getElementById("bwCount");

    if (countLabel) {
        countLabel.textContent = "(" + sites.length + ")";
    }

    const showResults = document.getElementById("bwShowResults");

    if (showResults) {
        showResults.textContent =
            "Show " + sites.length + (sites.length === 1 ? " website" : " websites");
    }

    if (sites.length === 0) {

        list.innerHTML =
            '<p class="cat-message">No websites found.</p>';

        showMore.style.display = "none";

        return;
    }

    list.innerHTML = sites.slice(0, browseVisibleCount)
        .map(function (website, index) {

            const domain = browseDomain(website);
            const reviewCount = Number(website.reviewCount) || 0;
            const rating = Number(website.averageRating) || 0;

            const ratingBlock = reviewCount > 0
                ? '<div class="bw-stars cat-stars">' + browseStars(rating) + "</div>" +
                  '<div class="bw-rate"><strong>' + rating.toFixed(1) + "</strong>" +
                    '<span class="bw-dot">\u2022</span>' +
                    '<span class="bw-reviews">' + reviewCount +
                        (reviewCount === 1 ? " review" : " reviews") + "</span></div>"
                : '<div class="bw-stars cat-stars">' + browseStars(0) + "</div>" +
                  '<div class="bw-rate"><span class="bw-reviews">No reviews yet</span></div>';

            const initial =
                (website.name || "W").trim().charAt(0).toUpperCase();

            const favicon =
                "https://www.google.com/s2/favicons?sz=64&domain=" +
                encodeURIComponent(domain);

            const categoryTag = website.category
                ? '<span class="bw-tag">' + browseEscape(website.category) + "</span>"
                : "";

            return '<a class="bw-row" href="review.html?websiteId=' +
                    encodeURIComponent(website.id) + '">' +
                '<div class="cat-logo" data-initial="' + browseEscape(initial) + '">' +
                    '<img src="' + browseEscape(favicon) + '" alt="" loading="lazy">' +
                "</div>" +
                '<div class="bw-body">' +
                    '<h3 class="bw-name">' + browseEscape(website.name) + "</h3>" +
                    '<p class="bw-domain">' + browseEscape(domain) + "</p>" +
                    ratingBlock + categoryTag +
                "</div>" +
                "</a>";

        }).join("");

    list.querySelectorAll(".cat-logo img").forEach(function (img) {

        img.addEventListener("error", function () {

            const logo = img.parentElement;

            logo.textContent = logo.dataset.initial || "W";
            logo.classList.add("cat-logo-fallback");
        });
    });

    showMore.style.display =
        browseVisibleCount < sites.length ? "inline-block" : "none";
}

function renderSheetOptions(containerId, options, current, attribute) {

    const box = document.getElementById(containerId);

    if (!box) {
        return;
    }

    box.innerHTML = options.map(function (item) {

        const active = String(item.value).toLowerCase() === String(current).toLowerCase();

        return '<button type="button" class="bw-opt' + (active ? " active" : "") +
            '" data-' + attribute + '="' + browseEscape(item.value) + '">' +
            browseEscape(item.label) + "</button>";

    }).join("");
}

function syncBrowseControls() {

    const sort = document.getElementById("browseSort");
    const rating = document.getElementById("browseRating");
    const badge = document.getElementById("bwFilterBadge");

    const activeCount =
        (browseSelectedCategory ? 1 : 0) + (browseMinRating > 0 ? 1 : 0);

    if (rating) {
        rating.value = String(browseMinRating);
        rating.parentElement.classList.toggle("on", browseMinRating > 0);
    }

    if (badge) {
        badge.textContent = activeCount;
        badge.hidden = activeCount === 0;
    }

    renderSheetOptions(
        "bwSheetCats",
        BROWSE_CATEGORIES.map(function (c) { return { value: c.value, label: c.label }; }),
        browseSelectedCategory, "category");

    renderSheetOptions("bwSheetRating", BROWSE_RATING_OPTIONS, browseMinRating, "rating");

    renderSheetOptions("bwSheetSort", BROWSE_SORT_OPTIONS,
        sort ? sort.value : "rating", "sort");
}

function refreshBrowse() {

    browseVisibleCount = BROWSE_PAGE_SIZE;
    renderBrowseChips();
    syncBrowseControls();
    renderBrowseList();
}

function openBrowseSheet() {

    const sheet = document.getElementById("bwSheet");

    sheet.hidden = false;
    document.body.classList.add("bw-lock");
}

function closeBrowseSheet() {

    const sheet = document.getElementById("bwSheet");

    sheet.hidden = true;
    document.body.classList.remove("bw-lock");
}

function loadBrowseWebsites() {

    const list = document.getElementById("browseList");

    list.innerHTML = '<p class="cat-message">Loading websites...</p>';

    fetch(BROWSE_API)

        .then(function (response) {

            if (!response.ok) {
                throw new Error("Unable to load websites");
            }

            return response.json();
        })

        .then(function (websites) {

            browseAllSites = websites || [];
            refreshBrowse();
        })

        .catch(function (error) {

            console.error(error);

            list.innerHTML =
                '<p class="cat-message cat-error">Unable to load websites. Please try again.</p>';

            document.getElementById("browseSubtitle").textContent = "";
        });
}

document.addEventListener("DOMContentLoaded", function () {

    const params = new URLSearchParams(window.location.search);

    browseSelectedCategory = (params.get("category") || "").trim();

    document.getElementById("browseSearch")
        .addEventListener("input", refreshBrowse);

    document.getElementById("browseSort")
        .addEventListener("change", refreshBrowse);


    document.getElementById("browseRating")
        .addEventListener("change", function (event) {

            browseMinRating = Number(event.target.value) || 0;
            refreshBrowse();
        });

    document.getElementById("bwOpenFilters")
        .addEventListener("click", openBrowseSheet);

    document.getElementById("bwSheetClose")
        .addEventListener("click", closeBrowseSheet);

    document.getElementById("bwShowResults")
        .addEventListener("click", closeBrowseSheet);

    document.getElementById("bwSheet")
        .addEventListener("click", function (event) {

            if (event.target === event.currentTarget) {
                closeBrowseSheet();
                return;
            }

            const cat = event.target.closest("[data-category]");
            const rating = event.target.closest("[data-rating]");
            const sort = event.target.closest("[data-sort]");

            if (cat) {
                browseSelectedCategory = cat.dataset.category || "";
            } else if (rating) {
                browseMinRating = Number(rating.dataset.rating) || 0;
            } else if (sort) {
                document.getElementById("browseSort").value = sort.dataset.sort;
            } else {
                return;
            }

            refreshBrowse();
        });

    document.getElementById("bwClearAll")
        .addEventListener("click", function () {

            browseSelectedCategory = "";
            browseMinRating = 0;
            document.getElementById("browseSort").value = "rating";
            document.getElementById("browseSearch").value = "";
            refreshBrowse();
        });

    document.addEventListener("keydown", function (event) {

        if (event.key === "Escape") {
            closeBrowseSheet();
        }
    });

    document.getElementById("browseShowMore")
        .addEventListener("click", function () {

            browseVisibleCount += BROWSE_PAGE_SIZE;
            renderBrowseList();
        });

    renderBrowseChips();
    syncBrowseControls();
    loadBrowseWebsites();
});


/* =========================================================
   COMMON RESPONSIVE NAVBAR
   Adds a search icon + menu (hamburger) button to the black
   navbar on phones. Desktop (769px+) is not changed.
   (Header code lives in this file - no extra file needed.)
========================================================= */
(function () {

    "use strict";

    var SEARCH_ICON =
        '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>';
    var CLOSE_ICON =
        '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 5l14 14M19 5L5 19"/></svg>';
    var MENU_ICON =
        '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18M3 12h18M3 18h18"/></svg>';

    function closeAll(nav) {

        nav.classList.remove("nav-open", "nav-search-open");

        var buttons = nav.querySelectorAll(".nav-icon-btn");

        buttons.forEach(function (button) {
            button.setAttribute("aria-expanded", "false");
        });
    }

    function enhance(nav) {

        var links = nav.querySelector(".nav-links");

        if (!links || nav.classList.contains("has-mobile-nav")) {
            return;
        }

        nav.classList.add("has-mobile-nav");

        /* the dropdown is positioned against the navbar */
        if (window.getComputedStyle(nav).position === "static") {
            nav.classList.add("nav-rel");
        }

        var tools = document.createElement("div");
        tools.className = "nav-tools";

        tools.innerHTML =
            '<button type="button" class="nav-icon-btn nav-search-btn" aria-label="Search" aria-expanded="false">' +
                '<span class="ic-open">' + SEARCH_ICON + "</span>" +
                '<span class="ic-close">' + CLOSE_ICON + "</span>" +
            "</button>" +
            '<button type="button" class="nav-icon-btn nav-menu-btn" aria-label="Menu" aria-expanded="false">' +
                '<span class="ic-open">' + MENU_ICON + "</span>" +
                '<span class="ic-close">' + CLOSE_ICON + "</span>" +
            "</button>";

        var search = document.createElement("form");
        search.className = "nav-search";
        search.setAttribute("action", "search.html");
        search.setAttribute("method", "get");
        search.setAttribute("role", "search");
        search.innerHTML =
            '<input type="search" name="q" placeholder="Search by name or domain..." ' +
            'aria-label="Search websites" autocomplete="off">';

        nav.appendChild(tools);
        nav.appendChild(search);

        var searchButton = tools.querySelector(".nav-search-btn");
        var menuButton = tools.querySelector(".nav-menu-btn");

        menuButton.addEventListener("click", function () {

            var open = !nav.classList.contains("nav-open");

            closeAll(nav);

            if (open) {
                nav.classList.add("nav-open");
                menuButton.setAttribute("aria-expanded", "true");
            }
        });

        searchButton.addEventListener("click", function () {

            var open = !nav.classList.contains("nav-search-open");

            closeAll(nav);

            if (open) {
                nav.classList.add("nav-search-open");
                searchButton.setAttribute("aria-expanded", "true");
                search.querySelector("input").focus();
            }
        });

        /* tap on a link, or on the dimmed area, closes the menu */
        links.addEventListener("click", function (event) {
            if (event.target.closest("a")) {
                closeAll(nav);
            }
        });

        nav.addEventListener("click", function (event) {
            if (event.target === nav) {
                closeAll(nav);
            }
        });
    }

    function init() {

        var navs = document.querySelectorAll("nav.navbar, nav.top-navbar");

        navs.forEach(enhance);

        document.addEventListener("keydown", function (event) {

            if (event.key !== "Escape") {
                return;
            }

            document.querySelectorAll(".has-mobile-nav").forEach(closeAll);
        });

        window.addEventListener("resize", function () {

            if (window.innerWidth > 768) {
                document.querySelectorAll(".has-mobile-nav").forEach(closeAll);
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();