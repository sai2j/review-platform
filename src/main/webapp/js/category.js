const CATEGORY_FILTER_API =
    "/review-platform/websites/filter";

const CATEGORY_PAGE_SIZE = 10;

const CATEGORY_OPTIONS = [
    { value: "Bank", label: "Banks", icon: "🏦" },
    { value: "Shopping", label: "Shopping", icon: "🛍️" },
    { value: "Travel", label: "Travel", icon: "✈️" },
    { value: "Food", label: "Food", icon: "🍔" },
    { value: "Technology", label: "Technology", icon: "💻" },
    { value: "Education", label: "Education", icon: "🎓" }
];

let categoryWebsites = [];
let categoryVisibleCount = CATEGORY_PAGE_SIZE;

function categoryEscapeHtml(value) {

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

function categoryGetDomain(website) {

    if (website.canonicalDomain) {
        return website.canonicalDomain;
    }

    try {
        return new URL(website.url).hostname.replace(/^www\./, "");
    } catch (error) {
        return website.url || "";
    }
}

function categoryStars(rating) {

    const safeRating =
        Math.max(0, Math.min(5, Number(rating) || 0));

    let html = "";

    for (let i = 1; i <= 5; i++) {
        html += '<span class="cat-star' +
            (i <= Math.round(safeRating) ? " filled" : "") +
            '">★</span>';
    }

    return html;
}

function categoryRatingLabel(rating) {

    const value = Number(rating) || 0;

    if (value >= 4.5) return "Excellent";
    if (value >= 4) return "Great";
    if (value >= 3) return "Average";
    if (value >= 2) return "Poor";

    return "Bad";
}

function getSelectedCategory() {

    const params =
        new URLSearchParams(window.location.search);

    return (params.get("category") || "").trim();
}

function findCategoryOption(category) {

    return CATEGORY_OPTIONS.find(function (item) {
        return item.value.toLowerCase() === category.toLowerCase();
    }) || null;
}

function renderCategoryChips(selectedCategory) {

    const chips = document.getElementById("categoryChips");

    if (!chips) {
        return;
    }

    chips.innerHTML = CATEGORY_OPTIONS.map(function (item) {

        const active =
            item.value.toLowerCase() === selectedCategory.toLowerCase();

        return '<a class="category-chip' +
            (active ? " active" : "") +
            '" href="category.html?category=' +
            encodeURIComponent(item.value) + '">' +
            item.icon + " " + categoryEscapeHtml(item.label) +
            "</a>";

    }).join("");
}

function renderCategoryWebsites() {

    const list =
        document.getElementById("categoryWebsiteList");

    const showMore =
        document.getElementById("categoryShowMore");

    if (!list) {
        return;
    }

    const visible =
        categoryWebsites.slice(0, categoryVisibleCount);

    list.innerHTML = visible.map(function (website, index) {

        const domain = categoryGetDomain(website);
        const reviewCount = Number(website.reviewCount) || 0;
        const rating = Number(website.averageRating) || 0;

        const ratingBlock = reviewCount > 0
            ? '<div class="cat-rating">' +
                '<span class="cat-rating-score">' + rating.toFixed(1) + "</span>" +
                '<span class="cat-stars">' + categoryStars(rating) + "</span>" +
                '<span class="cat-rating-label">' +
                    categoryEscapeHtml(categoryRatingLabel(rating)) + "</span>" +
                '<span class="cat-review-count">' + reviewCount +
                    (reviewCount === 1 ? " review" : " reviews") + "</span>" +
              "</div>"
            : '<div class="cat-rating">' +
                '<span class="cat-stars">' + categoryStars(0) + "</span>" +
                '<span class="cat-review-count">No reviews yet</span>' +
              "</div>";

        const initial =
            (website.name || "W").trim().charAt(0).toUpperCase();

        const favicon =
            "https://www.google.com/s2/favicons?sz=64&domain=" +
            encodeURIComponent(domain);

        return '<article class="cat-card">' +
            '<div class="cat-rank">#' + (index + 1) + "</div>" +
            '<div class="cat-logo" data-initial="' +
                categoryEscapeHtml(initial) + '">' +
                '<img src="' + categoryEscapeHtml(favicon) + '" alt="" loading="lazy">' +
            "</div>" +
            '<div class="cat-body">' +
                '<h3 class="cat-name">' + categoryEscapeHtml(website.name) + "</h3>" +
                '<p class="cat-domain">' + categoryEscapeHtml(domain) + "</p>" +
                ratingBlock +
                '<p class="cat-description">' +
                    categoryEscapeHtml(website.description || "") + "</p>" +
            "</div>" +
            '<a class="cat-view-button" href="review.html?websiteId=' +
                encodeURIComponent(website.id) + '">View reviews</a>' +
            "</article>";

    }).join("");

    /* If a logo image fails to load, show the first letter instead. */
    list.querySelectorAll(".cat-logo img").forEach(function (img) {

        img.addEventListener("error", function () {

            const logo = img.parentElement;

            logo.textContent = logo.dataset.initial || "W";
            logo.classList.add("cat-logo-fallback");
        });
    });

    if (showMore) {

        showMore.style.display =
            categoryVisibleCount < categoryWebsites.length
                ? "inline-block"
                : "none";
    }
}

function sortByTrustRating(websites) {

    return websites.slice().sort(function (a, b) {

        const ratingDiff =
            (Number(b.averageRating) || 0) - (Number(a.averageRating) || 0);

        if (ratingDiff !== 0) {
            return ratingDiff;
        }

        return (Number(b.reviewCount) || 0) - (Number(a.reviewCount) || 0);
    });
}

function loadCategoryPage() {

    const category = getSelectedCategory();

    const title = document.getElementById("categoryTitle");
    const subtitle = document.getElementById("categorySubtitle");
    const list = document.getElementById("categoryWebsiteList");

    renderCategoryChips(category);

    if (!category) {

        title.textContent = "Choose a category";
        subtitle.textContent = "Pick a category above to see its websites.";
        list.innerHTML = "";

        return;
    }

    const option = findCategoryOption(category);
    const label = option ? option.label : category;

    title.textContent = label;
    subtitle.textContent = "Best rated " + label.toLowerCase() + " websites";
    document.title = label + " - Review Platform";

    list.innerHTML = '<p class="cat-message">Loading websites...</p>';

    fetch(CATEGORY_FILTER_API + "?category=" + encodeURIComponent(category))

        .then(function (response) {

            if (!response.ok) {
                throw new Error("Unable to load category websites");
            }

            return response.json();
        })

        .then(function (websites) {

            categoryWebsites = sortByTrustRating(websites || []);
            categoryVisibleCount = CATEGORY_PAGE_SIZE;

            if (categoryWebsites.length === 0) {

                list.innerHTML =
                    '<p class="cat-message">No websites found in ' +
                    categoryEscapeHtml(label) +
                    ' yet.</p>';

                document.getElementById("categoryShowMore").style.display =
                    "none";

                return;
            }

            subtitle.textContent =
                categoryWebsites.length + " " + label.toLowerCase() +
                " websites, best rated first";

            renderCategoryWebsites();
        })

        .catch(function (error) {

            console.error(error);

            list.innerHTML =
                '<p class="cat-message cat-error">Unable to load websites. Please try again.</p>';
        });
}

document.addEventListener("DOMContentLoaded", function () {

    const showMore = document.getElementById("categoryShowMore");

    if (showMore) {

        showMore.addEventListener("click", function () {

            categoryVisibleCount += CATEGORY_PAGE_SIZE;
            renderCategoryWebsites();
        });
    }

    loadCategoryPage();
});