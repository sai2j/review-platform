const BROWSE_API =
    "/review-platform/websites";

const BROWSE_PAGE_SIZE = 10;

const BROWSE_CATEGORIES = [
    { value: "", label: "All", icon: "🌐" },
    { value: "Bank", label: "Banks", icon: "🏦" },
    { value: "Shopping", label: "Shopping", icon: "🛍️" },
    { value: "Travel", label: "Travel", icon: "✈️" },
    { value: "Food", label: "Food", icon: "🍔" },
    { value: "Technology", label: "Technology", icon: "💻" },
    { value: "Education", label: "Education", icon: "🎓" }
];

let browseAllSites = [];
let browseSelectedCategory = "";
let browseVisibleCount = BROWSE_PAGE_SIZE;

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

    chips.innerHTML = BROWSE_CATEGORIES.map(function (item) {

        const active =
            item.value.toLowerCase() === browseSelectedCategory.toLowerCase();

        return '<button type="button" class="category-chip' +
            (active ? " active" : "") +
            '" data-category="' + browseEscape(item.value) + '">' +
            item.icon + " " + browseEscape(item.label) + "</button>";

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
                ? '<div class="cat-rating">' +
                    '<span class="cat-rating-score">' + rating.toFixed(1) + "</span>" +
                    '<span class="cat-stars">' + browseStars(rating) + "</span>" +
                    '<span class="cat-rating-label">' +
                        browseEscape(browseRatingLabel(rating)) + "</span>" +
                    '<span class="cat-review-count">' + reviewCount +
                        (reviewCount === 1 ? " review" : " reviews") + "</span>" +
                  "</div>"
                : '<div class="cat-rating">' +
                    '<span class="cat-stars">' + browseStars(0) + "</span>" +
                    '<span class="cat-review-count">No reviews yet</span>' +
                  "</div>";

            const initial =
                (website.name || "W").trim().charAt(0).toUpperCase();

            const favicon =
                "https://www.google.com/s2/favicons?sz=64&domain=" +
                encodeURIComponent(domain);

            const categoryTag = website.category
                ? '<span class="browse-category-tag">' +
                    browseEscape(website.category) + "</span>"
                : "";

            return '<article class="cat-card">' +
                '<div class="cat-rank">#' + (index + 1) + "</div>" +
                '<div class="cat-logo" data-initial="' + browseEscape(initial) + '">' +
                    '<img src="' + browseEscape(favicon) + '" alt="" loading="lazy">' +
                "</div>" +
                '<div class="cat-body">' +
                    '<h3 class="cat-name">' + browseEscape(website.name) +
                        categoryTag + "</h3>" +
                    '<p class="cat-domain">' + browseEscape(domain) + "</p>" +
                    ratingBlock +
                    '<p class="cat-description">' +
                        browseEscape(website.description || "") + "</p>" +
                "</div>" +
                '<a class="cat-view-button" href="review.html?websiteId=' +
                    encodeURIComponent(website.id) + '">View reviews</a>' +
                "</article>";

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

function refreshBrowse() {

    browseVisibleCount = BROWSE_PAGE_SIZE;
    renderBrowseChips();
    renderBrowseList();
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

    document.getElementById("browseChips")
        .addEventListener("click", function (event) {

            const button = event.target.closest(".category-chip");

            if (!button) {
                return;
            }

            browseSelectedCategory = button.dataset.category || "";
            refreshBrowse();
        });

    document.getElementById("browseShowMore")
        .addEventListener("click", function () {

            browseVisibleCount += BROWSE_PAGE_SIZE;
            renderBrowseList();
        });

    renderBrowseChips();
    loadBrowseWebsites();
});