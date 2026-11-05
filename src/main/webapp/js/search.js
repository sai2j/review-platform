/* =========================================================
   SEARCH / CATEGORY RESULTS PAGE (Trustpilot style list)
   Works with:  search.html?q=spotify
                search.html?category=Travel
========================================================= */

const SP_SEARCH_API = "/review-platform/websites/search";
const SP_FILTER_API = "/review-platform/websites/filter";
const SP_PAGE_SIZE = 10;

const SP_ICONS = {
    bank: '<path d="M3 10l9-6 9 6"/><path d="M5 10v8M9.5 10v8M14.5 10v8M19 10v8"/><path d="M3 20h18"/>',
    cart: '<circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.6 12.4a1 1 0 0 0 1 .8h9.2a1 1 0 0 0 1-.8L21 7H6"/>',
    plane: '<path d="M10.5 13.5L3 11l1.5-1.5 8-1L16 4.5a1.8 1.8 0 0 1 2.5 2.5l-4 3.5-1 8L12 20l-2.5-6.5z"/>',
    food: '<path d="M7 3v8M4 3v5a3 3 0 0 0 6 0V3M7 11v10"/><path d="M17 3c-2 2-3 4.5-3 7h3v11"/>',
    laptop: '<rect x="4" y="5" width="16" height="11" rx="1.5"/><path d="M2 19h20"/>',
    book: '<path d="M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2z"/><path d="M4 21V5"/><path d="M9 7h6"/>'
};

const SP_CATEGORIES = [
    { value: "Bank", label: "Banks", sub: "Finance & Insurance", icon: "bank" },
    { value: "Shopping", label: "Shopping", sub: "Shopping & Fashion", icon: "cart" },
    { value: "Travel", label: "Travel", sub: "Travel & Vacation", icon: "plane" },
    { value: "Food", label: "Food", sub: "Food & Beverage", icon: "food" },
    { value: "Technology", label: "Technology", sub: "Electronics & Technology", icon: "laptop" },
    { value: "Education", label: "Education", sub: "Education & Training", icon: "book" }
];

let spAll = [];
let spPage = 1;
let spLabel = "";

function spEscape(value) {

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

function spDomain(website) {

    if (website.canonicalDomain) {
        return website.canonicalDomain;
    }

    try {
        return new URL(website.url).hostname.replace(/^www\./, "");
    } catch (error) {
        return website.url || "";
    }
}

function spStars(rating) {

    const filled = Math.round(Math.max(0, Math.min(5, Number(rating) || 0)));
    let html = "";

    for (let i = 1; i <= 5; i++) {
        html += '<span class="sp-star' + (i <= filled ? " filled" : "") + '">★</span>';
    }

    return '<span class="sp-stars" aria-label="Rated ' +
        (Number(rating) || 0).toFixed(1) + ' out of 5">' + html + "</span>";
}

function spParams() {

    const params = new URLSearchParams(window.location.search);

    return {
        q: (params.get("q") || "").trim(),
        category: (params.get("category") || "").trim()
    };
}

function spFindCategory(value) {

    return SP_CATEGORIES.find(function (item) {
        return item.value.toLowerCase() === String(value).toLowerCase() ||
            item.label.toLowerCase() === String(value).toLowerCase();
    }) || null;
}

/* ---------- Smart (fuzzy) matching ----------
   "spotify" also finds spotify.fr, Shopify, same-category websites, etc. */

function spNorm(text) {
    return String(text || "").toLowerCase().replace(/[^a-z0-9]/g, "");
}

function spDistance(a, b) {

    if (a === b) { return 0; }
    if (!a.length) { return b.length; }
    if (!b.length) { return a.length; }

    let prev = [];
    for (let j = 0; j <= b.length; j++) { prev[j] = j; }

    for (let i = 1; i <= a.length; i++) {

        const cur = [i];

        for (let j = 1; j <= b.length; j++) {
            cur[j] = Math.min(
                prev[j] + 1,
                cur[j - 1] + 1,
                prev[j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1)
            );
        }

        prev = cur;
    }

    return prev[b.length];
}

function spSimilarity(a, b) {

    if (!a || !b) { return 0; }

    return 1 - spDistance(a, b) / Math.max(a.length, b.length);
}

/* returns 0 (no match) .. 100 (exact) */
function spScore(website, query) {

    const q = spNorm(query);

    if (!q) { return 0; }

    const name = spNorm(website.name);
    const domain = spDomain(website).toLowerCase();
    const label = spNorm(domain.split(".")[0]);
    const category = spNorm(website.category);
    const host = spNorm(domain);

    let best = 0;

    if (name === q || label === q) { best = 100; }
    else if (name.indexOf(q) === 0 || label.indexOf(q) === 0) { best = 90; }
    else if (name.indexOf(q) !== -1 || host.indexOf(q) !== -1) { best = 78; }
    else if (category && (category === q || category.indexOf(q) !== -1 || q.indexOf(category) !== -1)) { best = 70; }

    /* similar spelling (typos, look-alike names) */
    if (q.length >= 3) {

        const sim = Math.max(
            spSimilarity(q, name),
            spSimilarity(q, label),
            spSimilarity(q, name.slice(0, q.length)),
            spSimilarity(q, label.slice(0, q.length))
        );

        if (sim >= 0.6) {
            best = Math.max(best, Math.round(30 + sim * 40));
        }
    }

    return best;
}

function spFetchJson(url) {

    return fetch(url, { credentials: "include" }).then(function (response) {

        if (!response.ok) {
            throw new Error("Request failed: " + url);
        }

        return response.json();
    });
}

/* ---------- Load data ---------- */

function spLoad() {

    const params = spParams();
    const list = document.getElementById("spList");
    const input = document.getElementById("spSearchInput");

    if (input) {
        input.value = params.q;
    }

    spRenderCategories(params.category || (spFindCategory(params.q) || {}).value || "");

    let request;

    if (params.category) {

        const cat = spFindCategory(params.category);
        spLabel = cat ? cat.label : params.category;

        request = spFetchJson(
            SP_FILTER_API + "?category=" + encodeURIComponent(params.category)
        );

        document.title = "Best in " + spLabel + " - Review Platform";

    } else if (params.q) {

        spLabel = params.q;

        /* smart search: name, domain, similar spelling and category */
        request = Promise.all([

            spFetchJson(SP_SEARCH_API),

            spFetchJson(SP_SEARCH_API + "?q=" + encodeURIComponent(params.q))
                .catch(function () { return []; })

        ]).then(function (results) {

            const fromServer = {};

            results[1].forEach(function (website) { fromServer[website.id] = true; });

            const scored = [];

            results[0].forEach(function (website) {

                let score = spScore(website, params.q);

                if (fromServer[website.id]) { score = Math.max(score, 80); }

                if (score >= 45) {
                    website.spScore = score;
                    scored.push(website);
                }
            });

            scored.sort(function (a, b) { return b.spScore - a.spScore; });

            return scored;
        });

        document.title = "Results for " + params.q + " - Review Platform";

    } else {

        spLabel = "All websites";
        request = spFetchJson(SP_SEARCH_API);
    }

    list.innerHTML = '<p class="sp-message">Loading websites...</p>';

    request
        .then(function (websites) {

            spAll = Array.isArray(websites) ? websites : [];
            spPage = 1;
            spRender();
            spLoadRelated();

            if (spParams().q && spAll.length) {
                spRenderCategories("", spResultCategories());
            }

            spShowAddCompany();
        })
        .catch(function (error) {

            console.error(error);

            list.innerHTML =
                '<p class="sp-message sp-error">Unable to load websites. Please try again.</p>';
        });
}

/* ---------- Filter + sort ---------- */

function spVisibleList() {

    const minRating =
        Number(document.getElementById("spRatingFilter").value) || 0;

    const sort = document.getElementById("spSort").value;

    const filtered = spAll.filter(function (website) {

        if (minRating === 0) {
            return true;
        }

        return (Number(website.reviewCount) || 0) > 0 &&
            (Number(website.averageRating) || 0) >= minRating;
    });

    filtered.sort(function (a, b) {

        if (sort === "match" && (a.spScore || b.spScore)) {
            return ((b.spScore || 0) - (a.spScore || 0)) ||
                ((Number(b.reviewCount) || 0) - (Number(a.reviewCount) || 0));
        }

        if (sort === "name") {
            return String(a.name || "").localeCompare(String(b.name || ""));
        }

        const ratingA = Number(a.averageRating) || 0;
        const ratingB = Number(b.averageRating) || 0;
        const countA = Number(a.reviewCount) || 0;
        const countB = Number(b.reviewCount) || 0;

        if (sort === "rating") {
            return (ratingB - ratingA) || (countB - countA);
        }

        return (countB - countA) || (ratingB - ratingA);
    });

    return filtered;
}

/* ---------- One result row (used by the list and the related list) ---------- */

function spRowHtml(website) {

        const domain = spDomain(website);
        const reviewCount = Number(website.reviewCount) || 0;
        const rating = Number(website.averageRating) || 0;
        const initial = (website.name || domain || "W").trim().charAt(0).toUpperCase();

        const favicon =
            "https://www.google.com/s2/favicons?sz=64&domain=" +
            encodeURIComponent(domain);

        const ratingText = reviewCount > 0
            ? "<strong>" + rating.toFixed(1) + "</strong> | " +
              reviewCount + (reviewCount === 1 ? " review" : " reviews")
            : "No reviews yet";

        const tags =
            (website.category
                ? '<span class="sp-tag">' + spEscape(website.category) + "</span>"
                : "") +
            '<span class="sp-tag">' + spEscape(domain) + "</span>" +
            (website.verified
                ? '<span class="sp-tag sp-tag-green">Verified</span>'
                : "");

        return '<a class="sp-row" href="review.html?websiteId=' +
                encodeURIComponent(website.id) + '">' +

            '<div class="sp-logo" data-initial="' + spEscape(initial) + '">' +
                '<img src="' + spEscape(favicon) + '" alt="" loading="lazy">' +
            "</div>" +

            '<div class="sp-info">' +
                '<h3 class="sp-name">' + spEscape(website.name || domain) + "</h3>" +
                '<p class="sp-domain">' + spEscape(domain) + "</p>" +
                '<div class="sp-rating">' + spStars(rating) +
                    '<span class="sp-rating-text">' + ratingText + "</span></div>" +
                '<div class="sp-tags">' + tags + "</div>" +
            "</div>" +

            '<div class="sp-right">' +
                (website.country
                    ? '<span class="sp-country">' + spEscape(website.country) + "</span>"
                    : "") +
                '<span class="sp-view">View reviews →</span>' +
            "</div>" +

        "</a>";

}

/* ---------- Render list ---------- */

function spRender() {

    const list = document.getElementById("spList");
    const count = document.getElementById("spCount");
    const items = spVisibleList();

    count.textContent = "Companies (" + items.length + ")";

    if (items.length === 0) {

        list.innerHTML =
            '<p class="sp-message">No websites found. Try a different name or category.</p>';

        document.getElementById("spPagination").innerHTML = "";
        return;
    }

    const totalPages = Math.ceil(items.length / SP_PAGE_SIZE);

    if (spPage > totalPages) {
        spPage = totalPages;
    }

    const start = (spPage - 1) * SP_PAGE_SIZE;
    const pageItems = items.slice(start, start + SP_PAGE_SIZE);

    list.innerHTML = pageItems.map(spRowHtml).join("");

    spLogoFallback(list);

    spRenderPagination(totalPages);
}

function spLogoFallback(root) {

    /* If the logo image fails, show the first letter instead. */
    root.querySelectorAll(".sp-logo img").forEach(function (img) {

        img.addEventListener("error", function () {

            const logo = img.parentElement;
            logo.textContent = logo.dataset.initial || "W";
            logo.classList.add("sp-logo-fallback");
        });
    });
}

function spRenderPagination(totalPages) {

    const nav = document.getElementById("spPagination");

    if (totalPages <= 1) {
        nav.innerHTML = "";
        return;
    }

    const pages = [];

    for (let i = 1; i <= totalPages; i++) {

        if (i === 1 || i === totalPages || Math.abs(i - spPage) <= 1) {
            pages.push(i);
        } else if (pages[pages.length - 1] !== "...") {
            pages.push("...");
        }
    }

    let html = '<button type="button" data-page="' + (spPage - 1) + '"' +
        (spPage === 1 ? " disabled" : "") + ">Previous</button>";

    pages.forEach(function (page) {

        html += page === "..."
            ? '<span class="sp-dots">…</span>'
            : '<button type="button" data-page="' + page + '"' +
              (page === spPage ? ' class="active"' : "") + ">" + page + "</button>";
    });

    html += '<button type="button" data-page="' + (spPage + 1) + '"' +
        (spPage === totalPages ? " disabled" : "") + ">Next page</button>";

    nav.innerHTML = html;
}

/* ---------- Same category: "Similar websites" ---------- */

function spRelatedBox() {

    let box = document.getElementById("spRelated");

    if (!box) {
        box = document.createElement("section");
        box.id = "spRelated";
        box.className = "sp-related";
        document.getElementById("spPagination").insertAdjacentElement("afterend", box);
    }

    return box;
}

function spLoadRelated() {

    const box = spRelatedBox();
    box.innerHTML = "";

    /* only for a name/keyword search - a category page is already "same category" */
    if (spParams().category || spAll.length === 0) {
        return;
    }

    const inResults = {};
    const categories = [];

    spAll.forEach(function (website) {

        inResults[website.id] = true;

        if (website.category && categories.indexOf(website.category) === -1) {
            categories.push(website.category);
        }
    });

    if (categories.length === 0) {
        return;
    }

    Promise.all(categories.slice(0, 3).map(function (category) {

        return spFetchJson(SP_FILTER_API + "?category=" + encodeURIComponent(category))
            .catch(function () { return []; });

    })).then(function (groups) {

        const seen = {};
        const related = [];

        groups.forEach(function (group) {

            (Array.isArray(group) ? group : []).forEach(function (website) {

                if (!inResults[website.id] && !seen[website.id]) {
                    seen[website.id] = true;
                    related.push(website);
                }
            });
        });

        if (related.length === 0) {
            return;
        }

        related.sort(function (a, b) {
            return (Number(b.reviewCount) || 0) - (Number(a.reviewCount) || 0);
        });

        box.innerHTML =
            '<h2 class="sp-related-title">Similar websites you may like</h2>' +
            '<p class="sp-related-sub">More from ' +
                categories.slice(0, 3).map(spEscape).join(", ") + "</p>" +
            '<div class="sp-list">' + related.slice(0, 6).map(spRowHtml).join("") + "</div>";

        spLogoFallback(box);
    });
}

/* ---------- Sidebar ---------- */

function spCatHtml(item, active, href) {

    return '<a class="sp-cat' + (active ? " active" : "") + '" href="' + href + '">' +
        '<span class="sp-cat-icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" ' +
            'stroke="currentColor" stroke-width="1.8" stroke-linecap="round" ' +
            'stroke-linejoin="round" aria-hidden="true">' + SP_ICONS[item.icon] + "</svg></span>" +
        '<span class="sp-cat-text"><strong>' + spEscape(item.label) + "</strong>" +
        "<small>" + spEscape(item.sub) + "</small></span></a>";
}

function spRenderCategories(activeValue, list) {

    const box = document.getElementById("spCategories");
    const title = box.parentElement.querySelector("h2");

    const items = list || SP_CATEGORIES;

    if (title) {
        title.textContent = list ? "Categories (" + items.length + ")" : "Categories";
    }

    box.innerHTML = items.map(function (item) {

        const active = String(activeValue).toLowerCase() === item.value.toLowerCase();

        return spCatHtml(item, active,
            "search.html?category=" + encodeURIComponent(item.value));

    }).join("");
}

/* categories that appear in the current results (like Trustpilot) */
function spResultCategories() {

    const seen = {};
    const out = [];

    spAll.forEach(function (website) {

        const value = website.category;

        if (!value || seen[value.toLowerCase()]) { return; }

        seen[value.toLowerCase()] = true;

        out.push(spFindCategory(value) ||
            { value: value, label: value, sub: "Category", icon: "book" });
    });

    return out;
}

function spShowAddCompany() {

    let box = document.getElementById("spAdd");

    if (!box) {
        box = document.createElement("div");
        box.id = "spAdd";
        box.className = "sp-add";
        box.innerHTML =
            "<h3>Can't find a company?</h3>" +
            "<p>It might not be listed yet. Add it and be the first to write a review.</p>" +
            '<a class="sp-add-btn" href="write-review.html">Add company</a>';

        document.getElementById("spList").parentElement.appendChild(box);
    }
}

/* ---------- Events ---------- */

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("spSearchForm").addEventListener("submit", function (event) {

        event.preventDefault();

        const keyword = document.getElementById("spSearchInput").value.trim();

        window.location.href = keyword
            ? "search.html?q=" + encodeURIComponent(keyword)
            : "search.html";
    });

    document.getElementById("spRatingFilter").addEventListener("change", function () {
        spPage = 1;
        spRender();
    });

    document.getElementById("spSort").addEventListener("change", function () {
        spPage = 1;
        spRender();
    });

    document.getElementById("spPagination").addEventListener("click", function (event) {

        const button = event.target.closest("button[data-page]");

        if (!button || button.disabled) {
            return;
        }

        spPage = Number(button.dataset.page);
        spRender();
        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    spLoad();
});