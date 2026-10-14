const REVIEW_API = "/review-platform/reviews";
const WEBSITE_API = "/review-platform/websites";
const REPORT_API = "/review-platform/reports";
const RESPONSE_API = "/review-platform/business-responses";
const VOTE_API = "/review-platform/review-votes";
const BUSINESS_API = "/review-platform/businesses";
const BUSINESS_CLAIM_API = "/review-platform/business-claims";

const REVIEWS_PER_PAGE = 5;         
const REVIEWS_FETCH_SIZE = 100;     
const SHOW_BUSINESS_RESPONSES = true; 

const urlParams = new URLSearchParams(window.location.search);
const websiteId = urlParams.get("websiteId");

let currentWebsiteForStructuredData = null;
let currentReviewsForStructuredData = [];
let currentBusinessForClaim = null;
let currentSummary = null;
let websiteRequest = null;

let allReviews = [];
let businessResponses = {};
let reviewFilter = 0;
let reviewSort = "recent";
let visibleReviewCount = REVIEWS_PER_PAGE;


/* =========================================================
   GET LOGGED-IN USER
   ========================================================= */

function getLoggedInUser() {
    try {
        return JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (error) {
        console.error("Unable to read logged-in user:", error);
        return null;
    }
}

function getUserId() {
    const user = getLoggedInUser();

    if (!user || !user.id) {
        return null;
    }

    return Number(user.id);
}


/* =========================================================
   SMALL HELPERS
   ========================================================= */

function escapeHtml(value) {
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

function escapeAttribute(value) {
    return escapeHtml(value);
}

function safeHref(url) {
    const value = String(url || "").trim();

    if (/^https?:\/\//i.test(value)) {
        return value;
    }

    if (/^[\w-]+(\.[\w-]+)+/i.test(value)) {
        return "https://" + value;
    }

    return "#";
}

function starText(score) {
    const value = Number(score) || 0;
    const full = Math.floor(value);
    const half = value - full >= 0.5 ? 1 : 0;

    return "★".repeat(full) + (half ? "½" : "") + "☆".repeat(5 - full - half);
}

function formatDate(value) {
    if (!value) {
        return "";
    }

    let date;

    if (Array.isArray(value)) {
        date = new Date(value[0], (value[1] || 1) - 1, value[2] || 1);
    } else {
        date = new Date(value);
    }

    if (isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric"
    });
}

function calculatePercentage(count, total) {
    if (!total || total === 0) {
        return 0;
    }

    return Math.round((count / total) * 100);
}

let toastTimer = null;

function showToast(message) {
    const toast = document.getElementById("toast");

    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(toastTimer);

    toastTimer = setTimeout(function () {
        toast.classList.remove("show");
    }, 2600);
}

function ensureHeadElement(selector, tagName, attributes) {
    let element = document.querySelector(selector);

    if (!element) {
        element = document.createElement(tagName);

        Object.keys(attributes).forEach(function (key) {
            element.setAttribute(key, attributes[key]);
        });

        document.head.appendChild(element);
    }

    return element;
}

function setJsonLd(id, data) {
    let script = document.getElementById(id);

    if (!script) {
        script = document.createElement("script");
        script.type = "application/ld+json";
        script.id = id;
        document.head.appendChild(script);
    }

    script.textContent = JSON.stringify(data);
}

function fetchWebsite() {
    if (!websiteId) {
        return Promise.reject(new Error("Website ID not found"));
    }

    if (!websiteRequest) {
        websiteRequest = fetch(WEBSITE_API + "/" + websiteId)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Website not found");
                }

                return response.json();
            });
    }

    return websiteRequest;
}


/* =========================================================
   UPDATE WEBSITE SEO
   ========================================================= */

function updateWebsiteSEO(website, summary) {
    if (!website) {
        return;
    }

    const websiteName = website.name || "Website";

    const seoTitle =
        website.seoTitle ||
        websiteName + " Reviews | Review Platform";

    const seoDescription =
        website.seoDescription ||
        "Read reviews, ratings and user experiences for " +
        websiteName +
        " on Review Platform.";

    document.title = seoTitle;

    ensureHeadElement('meta[name="description"]', "meta", { name: "description" })
        .setAttribute("content", seoDescription);

    const robotsMeta = ensureHeadElement('meta[name="robots"]', "meta", { name: "robots" });

    let isThinPage = false;

    if (summary) {
        const totalReview = Number(summary.totalReview || 0);

        const hasDescription =
            website.description &&
            website.description.trim().length > 0;

        isThinPage = totalReview === 0 && !hasDescription;
    }

    robotsMeta.setAttribute(
        "content",
        isThinPage ? "noindex, follow" : "index, follow"
    );

    const canonicalUrl = website.canonicalUrl || window.location.href;

    ensureHeadElement('link[rel="canonical"]', "link", { rel: "canonical" })
        .setAttribute("href", canonicalUrl);

    function setOpenGraphMeta(property, content) {
        ensureHeadElement(
            'meta[property="' + property + '"]',
            "meta",
            { property: property }
        ).setAttribute("content", content);
    }

    setOpenGraphMeta("og:title", seoTitle);
    setOpenGraphMeta("og:description", seoDescription);
    setOpenGraphMeta("og:url", canonicalUrl);
    setOpenGraphMeta("og:type", "website");

    setJsonLd("breadcrumbStructuredData", {
        "@context": "https://schema.org",
        "@type": "BreadcrumbList",
        "itemListElement": [
            {
                "@type": "ListItem",
                "position": 1,
                "name": "Home",
                "item": window.location.origin + "/review-platform/index.html"
            },
            {
                "@type": "ListItem",
                "position": 2,
                "name": "Websites",
                "item": window.location.origin + "/review-platform/index.html#websites"
            },
            {
                "@type": "ListItem",
                "position": 3,
                "name": websiteName,
                "item": canonicalUrl
            }
        ]
    });
}


/* =========================================================
   STRUCTURED DATA
   ========================================================= */

function buildOrganizationSchema() {
    return {
        "@type": "Organization",
        "@id": window.location.origin + "/review-platform/#organization",
        "name": "Review Platform",
        "url": window.location.origin + "/review-platform/"
    };
}

function buildWebsiteSchema(website, aggregateRating) {
    const websiteUrl = website.url || window.location.href;

    const schema = {
        "@type": "WebSite",
        "@id": websiteUrl + "#website",
        "name": website.name || "Website",
        "url": websiteUrl
    };

    if (aggregateRating) {
        schema.aggregateRating = aggregateRating;
    } else {
        schema.publisher = {
            "@id": window.location.origin + "/review-platform/#organization"
        };
    }

    return schema;
}

function updateStructuredData(website) {
    if (!website) {
        return;
    }

    setJsonLd("structuredData", {
        "@context": "https://schema.org",
        "@graph": [
            buildOrganizationSchema(),
            buildWebsiteSchema(website, null)
        ]
    });
}

function updateAggregateRatingStructuredData(website, summary) {
    if (!website || !summary) {
        return;
    }

    const ratingValue = Number(summary.averageRating || 0);
    const reviewCount = Number(summary.totalReview || 0);

    if (reviewCount <= 0 || ratingValue <= 0) {
        return;
    }

    const aggregateRating = {
        "@type": "AggregateRating",
        "ratingValue": ratingValue.toFixed(1),
        "bestRating": "5",
        "worstRating": "1",
        "reviewCount": reviewCount
    };

    setJsonLd("structuredData", {
        "@context": "https://schema.org",
        "@graph": [
            buildOrganizationSchema(),
            buildWebsiteSchema(website, aggregateRating)
        ]
    });
}

function updateReviewStructuredData(reviews, website) {
    if (!website) {
        return;
    }

    const reviewList = Array.isArray(reviews) ? reviews : [];
    const websiteName = website.name || "Website";

    const websiteUrl =
        website.canonicalUrl ||
        website.url ||
        window.location.href;

    const validReviews = reviewList.filter(function (review) {
        return (
            review &&
            review.id &&
            Number(review.rating) >= 1 &&
            Number(review.rating) <= 5 &&
            review.comment
        );
    });

    const existingScript = document.getElementById("reviewStructuredData");

    if (validReviews.length === 0) {
        if (existingScript) {
            existingScript.remove();
        }

        return;
    }

    const reviewSchemas = validReviews.map(function (review) {
        return {
            "@type": "Review",
            "@id": websiteUrl + "#review-" + review.id,
            "itemReviewed": {
                "@type": "WebSite",
                "name": websiteName,
                "url": websiteUrl
            },
            "reviewRating": {
                "@type": "Rating",
                "ratingValue": Number(review.rating),
                "bestRating": 5,
                "worstRating": 1
            },
            "reviewBody": review.comment
        };
    });

    setJsonLd("reviewStructuredData", {
        "@context": "https://schema.org",
        "@graph": reviewSchemas
    });
}


/* =========================================================
   SHOW WEBSITE LOGO
   ========================================================= */

function showWebsiteLogo(website) {
    const logoBox = document.querySelector(".website-logo");

    if (!logoBox || !website) {
        return;
    }

    let domain = website.canonicalDomain || "";

    if (!domain && website.url) {
        try {
            domain = new URL(website.url).hostname.replace(/^www\./, "");
        } catch (error) {
            domain = "";
        }
    }

    const initial = (website.name || "W").trim().charAt(0).toUpperCase();

    if (!domain) {
        logoBox.textContent = initial;
        return;
    }

    const image = document.createElement("img");

    image.src =
        "https://www.google.com/s2/favicons?sz=128&domain=" +
        encodeURIComponent(domain);

    image.alt = (website.name || "Website") + " logo";
    image.style.width = "48px";
    image.style.height = "48px";
    image.style.objectFit = "contain";

    image.addEventListener("error", function () {
        logoBox.textContent = initial;
    });

    logoBox.textContent = "";
    logoBox.appendChild(image);
}


/* =========================================================
   LOAD WEBSITE INFORMATION
   ========================================================= */

function loadWebsiteInfo() {
    const websiteInfo = document.getElementById("websiteInfo");

    if (!websiteId) {
        if (websiteInfo) {
            websiteInfo.innerHTML = '<p class="error-message">Website ID not found.</p>';
        }

        return;
    }

    fetchWebsite()
        .then(function (website) {
            currentWebsiteForStructuredData = website;

            updateWebsiteSEO(website);
            updateStructuredData(website);

            updateReviewStructuredData(
                currentReviewsForStructuredData,
                currentWebsiteForStructuredData
            );

            showWebsiteLogo(website);

            const nameElement = document.getElementById("websiteName");

            if (nameElement) {
                nameElement.textContent = website.name || "Website";
            }

            const visitButton = document.getElementById("visitWebsiteButton");

            if (visitButton && website.url) {
                visitButton.href = safeHref(website.url);
                visitButton.hidden = false;
            }

            if (websiteInfo) {
                websiteInfo.innerHTML =
                    (website.description
                        ? "<p>" + escapeHtml(website.description) + "</p>"
                        : "") +
                    '<div id="claimStatus">Checking business verification...</div>';
            }

            renderCompanyDetails();
            loadClaimStatus();
        })
        .catch(function (error) {
            console.error(error);

            if (websiteInfo) {
                websiteInfo.innerHTML =
                    '<p class="error-message">Unable to load website information.</p>';
            }
        });
}


/* =========================================================
   COMPANY DETAILS
   ========================================================= */

function renderCompanyDetails() {
    const list = document.getElementById("websiteDetails");

    if (!list) {
        return;
    }

    const website = currentWebsiteForStructuredData;
    let html = "";

    if (website && website.url) {
        html +=
            "<dt>Website</dt><dd><a href=\"" +
            escapeAttribute(safeHref(website.url)) +
            "\" target=\"_blank\" rel=\"noopener noreferrer\">" +
            escapeHtml(website.url) +
            "</a></dd>";
    }

    const category =
        website && website.category && typeof website.category === "object"
            ? website.category.name
            : website && website.category;

    if (category) {
        html += "<dt>Category</dt><dd>" + escapeHtml(category) + "</dd>";
    }

    if (currentSummary) {
        html +=
            "<dt>Total reviews</dt><dd>" +
            Number(currentSummary.totalReview || 0) +
            "</dd>";
    }

    list.innerHTML = html;
}


/* =========================================================
   LOAD BUSINESS CLAIM STATUS
   ========================================================= */

function loadClaimStatus() {
    if (!websiteId) {
        return;
    }

    const claimStatus = document.getElementById("claimStatus");

    if (!claimStatus) {
        return;
    }

    fetch(BUSINESS_API + "/website/" + websiteId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Business not found");
            }

            return response.json();
        })
        .then(function (business) {
            if (!business || !business.id) {
                currentBusinessForClaim = null;
                claimStatus.innerHTML = "";
                return;
            }

            currentBusinessForClaim = business;

            if (String(business.status || "").toUpperCase() === "VERIFIED") {
                const slot = document.getElementById("verifiedSlot");
                const badge = '<span class="claimed-badge">✓ Verified Business</span>';

                if (slot) {
                    slot.innerHTML = badge;
                    claimStatus.innerHTML = "";
                } else {
                    claimStatus.innerHTML = badge;
                }

                return;
            }

            const user = getLoggedInUser();

            if (!user || !user.id) {
                renderClaimBusinessBox();
                return;
            }

            fetch(BUSINESS_CLAIM_API + "/user/" + user.id)
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("Unable to load claim status");
                    }

                    return response.json();
                })
                .then(function (claim) {
                    if (
                        !claim ||
                        !claim.id ||
                        Number(claim.businessId) !== Number(business.id)
                    ) {
                        renderClaimBusinessBox();
                        return;
                    }

                    const status = String(claim.status || "").toUpperCase();

                    if (status === "PENDING") {
                        claimStatus.innerHTML = `
                            <div
                                class="claim-business-box"
                                style="margin-top: 16px; padding: 20px; border: 1px solid #fde68a; border-radius: 10px; background: #fffbeb;">
                                <h3>Claim Pending</h3>
                                <p>Your claim request is waiting for admin approval.</p>
                                <button
                                    type="button"
                                    class="write-review-button"
                                    data-claim-navigation="dashboard">
                                    View My Dashboard
                                </button>
                            </div>
                        `;

                        setupClaimNavigationButton();
                        return;
                    }

                    if (status === "APPROVED") {
                        claimStatus.innerHTML = `
                            <div
                                class="claim-business-box"
                                style="margin-top: 16px; padding: 20px; border: 1px solid #a7f3d0; border-radius: 10px; background: #ecfdf5;">
                                <h3>Claim Approved</h3>
                                <p>Your ownership claim is approved. Complete business verification from your dashboard.</p>
                                <button
                                    type="button"
                                    class="write-review-button"
                                    data-claim-navigation="verification">
                                    Open Verification
                                </button>
                            </div>
                        `;

                        setupClaimNavigationButton();
                        return;
                    }

                    renderClaimBusinessBox();
                })
                .catch(function (error) {
                    console.error("Unable to load claim status:", error);
                    renderClaimBusinessBox();
                });
        })
        .catch(function (error) {
            console.error("Unable to load business verification status:", error);

            currentBusinessForClaim = null;
            claimStatus.innerHTML = "";
        });
}

function setupClaimNavigationButton() {
    document
        .querySelectorAll("[data-claim-navigation]")
        .forEach(function (button) {
            button.addEventListener("click", function () {
                window.location.href = "user-dashboard.html";
            });
        });
}


/* =========================================================
   RENDER CLAIM BUSINESS BOX
   ========================================================= */

function renderClaimBusinessBox() {
    const claimStatus = document.getElementById("claimStatus");

    if (!claimStatus) {
        return;
    }

    claimStatus.innerHTML = `
        <div
            class="claim-business-box"
            style="margin-top: 16px; padding: 20px; border: 1px solid #e5e7eb; border-radius: 10px; background: #ffffff;">
            <h3>Is this your business?</h3>
            <p>Claim this business profile to manage your business information and respond to customer reviews.</p>
            <button
                type="button"
                id="claimBusinessButton"
                class="write-review-button">
                Claim This Business
            </button>
            <p id="claimBusinessMessage" class="claim-business-message"></p>
        </div>
    `;

    setupClaimBusinessButton();
}


/* =========================================================
   CLAIM BUSINESS BUTTON
   ========================================================= */

function setupClaimBusinessButton() {
    const claimBusinessButton = document.getElementById("claimBusinessButton");

    if (!claimBusinessButton) {
        return;
    }

    claimBusinessButton.addEventListener("click", function () {
        const message = document.getElementById("claimBusinessMessage");

        if (!currentBusinessForClaim || !currentBusinessForClaim.id) {
            if (message) {
                message.innerHTML = `
                    <span class="error-message">
                        Business information is not available.
                    </span>
                `;
            }

            return;
        }

        const user = getLoggedInUser();

        if (!user || !user.id) {
            if (message) {
                message.innerHTML = `
                    <span class="error-message">
                        Please login before claiming this business.
                    </span>
                    <br>
                    <a href="login.html">Log in to continue</a>
                `;
            }

            return;
        }

        window.location.href =
            "business-claim.html?businessId=" +
            encodeURIComponent(currentBusinessForClaim.id);
    });
}


/* =========================================================
   LOAD RATING SUMMARY
   ========================================================= */

function loadRatingSummary() {
    if (!websiteId) {
        return;
    }

    fetch(REVIEW_API + "/website/" + websiteId + "/summary")
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Rating Summary API Error");
            }

            return response.json();
        })
        .then(function (summary) {
            currentSummary = summary;

            fetchWebsite()
                .then(function (website) {
                    updateWebsiteSEO(website, summary);
                    updateAggregateRatingStructuredData(website, summary);
                })
                .catch(function (error) {
                    console.error("Unable to update aggregate rating structured data:", error);
                });

            renderRatingSummary(summary);
            renderCompanyDetails();
        })
        .catch(function (error) {
            console.error(error);

            const averageElement = document.getElementById("averageRating");

            if (averageElement) {
                averageElement.innerHTML =
                    '<p class="error-message">Unable to load rating.</p>';
            }

            const totalElement = document.getElementById("totalReviews");

            if (totalElement) {
                totalElement.innerHTML = "";
            }
        });
}

function renderRatingSummary(summary) {
    const averageRating = Number(summary.averageRating || 0);
    const totalReview = Number(summary.totalReview || 0);

    const counts = {
        five: Number(summary.fivestar ?? summary.fiveStar ?? 0),
        four: Number(summary.fourStar || 0),
        three: Number(summary.threeStar || 0),
        two: Number(summary.twoStar || 0),
        one: Number(summary.oneStar || 0)
    };

    const stars = starText(averageRating);
    const label = averageRating.toFixed(1) + " out of 5 stars";

    const averageElement = document.getElementById("averageRating");

    if (averageElement) {
        averageElement.innerHTML =
            '<span class="score-number">' + averageRating.toFixed(1) + "</span>" +
            '<span class="score-out-of">/5</span>';
    }

    const scoreStars = document.getElementById("scoreStars");

    if (scoreStars) {
        scoreStars.textContent = stars;
        scoreStars.setAttribute("aria-label", label);
    }

    const totalElement = document.getElementById("totalReviews");

    if (totalElement) {
        totalElement.textContent =
            "Based on " + totalReview + " review" + (totalReview === 1 ? "" : "s");
    }

    const headRating = document.getElementById("headRating");

    if (headRating) {
        headRating.innerHTML =
            '<span class="stars" role="img" aria-label="' + label + '">' + stars + "</span>" +
            "<strong>" + averageRating.toFixed(1) + "</strong>" +
            "<span>· " + totalReview + " review" + (totalReview === 1 ? "" : "s") + "</span>";
    }

    Object.keys(counts).forEach(function (name) {
        const countElement = document.getElementById(name + "StarCount");
        const barElement = document.getElementById(name + "StarBar");

        if (countElement) {
            countElement.textContent = counts[name];
        }

        if (barElement) {
            barElement.style.width =
                calculatePercentage(counts[name], totalReview) + "%";
        }
    });
}


/* =========================================================
   LOAD BUSINESS RESPONSES
   ========================================================= */

function loadBusinessResponses() {
    if (!SHOW_BUSINESS_RESPONSES) {
        return Promise.resolve({});
    }

    return fetch(RESPONSE_API)
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Business response API error");
            }

            return response.json();
        })
        .then(function (data) {
            const list = Array.isArray(data) ? data : (data.content || []);
            const map = {};

            list.forEach(function (item) {
                if (item && item.reviewId !== undefined && item.response) {
                    map[Number(item.reviewId)] = item.response;
                }
            });

            return map;
        })
        .catch(function (error) {
            console.error("Unable to load business responses:", error);
            return {};
        });
}


/* =========================================================
   LOAD REVIEWS
   ========================================================= */

function loadReviews() {
    const reviewList = document.getElementById("reviewList");

    if (!websiteId) {
        if (reviewList) {
            reviewList.innerHTML =
                '<p class="error-message">Website ID not found.</p>';
        }

        return;
    }

    Promise.all([
        fetch(
            REVIEW_API + "/website/" + websiteId +
            "?page=0&size=" + REVIEWS_FETCH_SIZE
        ).then(function (response) {
            if (!response.ok) {
                throw new Error("Review API error");
            }

            return response.json();
        }),
        loadBusinessResponses()
    ])
        .then(function (results) {
            const data = results[0];

            allReviews = Array.isArray(data) ? data : (data.content || []);
            businessResponses = results[1];
            currentReviewsForStructuredData = allReviews;

            displayReviews();

            updateReviewStructuredData(
                currentReviewsForStructuredData,
                currentWebsiteForStructuredData
            );
        })
        .catch(function (error) {
            console.error(error);

            if (reviewList) {
                reviewList.innerHTML =
                    '<p class="error-message">Unable to load reviews.</p>';
            }
        });
}


/* =========================================================
   FILTER + SORT
   ========================================================= */

function getReviewTime(review) {
    const value = review.createdAt || review.reviewDate || review.date;

    if (!value) {
        return 0;
    }

    const time = Array.isArray(value)
        ? new Date(value[0], (value[1] || 1) - 1, value[2] || 1).getTime()
        : Date.parse(value);

    return isNaN(time) ? 0 : time;
}

function getFilteredReviews() {
    const list = reviewFilter
        ? allReviews.filter(function (review) {
            return Number(review.rating) === reviewFilter;
        })
        : allReviews.slice();

    const sorters = {
        recent: function (a, b) {
            return getReviewTime(b) - getReviewTime(a) || Number(b.id) - Number(a.id);
        },
        high: function (a, b) {
            return Number(b.rating) - Number(a.rating) || Number(b.id) - Number(a.id);
        },
        low: function (a, b) {
            return Number(a.rating) - Number(b.rating) || Number(b.id) - Number(a.id);
        }
    };

    return list.sort(sorters[reviewSort] || sorters.recent);
}


/* =========================================================
   DISPLAY REVIEWS
   ========================================================= */

function displayReviews() {
    const reviewList = document.getElementById("reviewList");
    const shownText = document.getElementById("reviewsShown");
    const loadMore = document.getElementById("loadMoreButton");

    if (!reviewList) {
        return;
    }

    function setFooter(total, shown) {
        if (shownText) {
            shownText.textContent = total
                ? "Showing " + shown + " of " + total + " reviews"
                : "";
        }

        if (loadMore) {
            loadMore.hidden = total <= shown;
        }
    }

    if (!allReviews.length) {
        reviewList.innerHTML =
            '<p class="no-review">No reviews available yet.</p>';
        setFooter(0, 0);
        return;
    }

    const list = getFilteredReviews();

    if (!list.length) {
        reviewList.innerHTML =
            '<p class="no-review">No reviews with this rating.</p>';
        setFooter(0, 0);
        return;
    }

    const visible = list.slice(0, visibleReviewCount);

    reviewList.innerHTML = visible.map(buildReviewCard).join("");

    visible.forEach(function (review) {
        loadVoteCounts(review.id);
    });

    setFooter(list.length, visible.length);
}

function buildReviewMenu(review) {
    const id = review.id;
    
    return `
        <div class="menu-wrap">
            <button type="button" class="menu-toggle" data-action="toggle-menu" data-review-id="${id}" aria-label="More actions" aria-haspopup="menu" aria-expanded="false">•••</button>
            <div class="review-menu" role="menu" hidden>
                <button type="button" role="menuitem" data-action="edit" data-review-id="${id}">Edit review</button>
                <button type="button" role="menuitem" data-action="delete" data-review-id="${id}">Delete review</button>
                <button type="button" role="menuitem" data-action="show-report" data-review-id="${id}">Report review</button>
            </div>
        </div>
    `;
}

function buildReviewCard(review) {
    const id = review.id;
    const rating = Number(review.rating) || 0;

    const name = String(
        review.userName || review.username || review.reviewerName || "Customer"
    );

    const dateText = formatDate(review.createdAt || review.reviewDate || review.date);
    const reply = businessResponses[Number(id)];

    return `
        <article class="review-card" id="review-${id}" data-review-id="${id}">

            <div class="review-head">
                <div class="avatar" aria-hidden="true">${escapeHtml(name.charAt(0).toUpperCase())}</div>
                <div class="review-who">
                    <strong>${escapeHtml(name)}</strong>
                    ${dateText ? `<div class="review-date">${escapeHtml(dateText)}</div>` : ""}
                </div>
                ${buildReviewMenu(review)}
            </div>

            <div class="rating">
                <span class="stars" role="img" aria-label="${rating} out of 5 stars">${starText(rating)}</span>
                <strong>${rating}/5</strong>
            </div>

            ${review.comment ? `<div class="comment">${escapeHtml(review.comment)}</div>` : ""}

            ${reply ? `
                <div class="business-response">
                    <strong>Business Response</strong>
                    <p>${escapeHtml(reply)}</p>
                </div>` : ""}

            <div class="review-actions">
                <button type="button" class="review-btn" data-action="helpful" data-review-id="${id}">
                    Helpful · <span id="helpful-${id}">0</span>
                </button>
                <button type="button" class="review-btn" data-action="not-helpful" data-review-id="${id}">
                    Not Helpful · <span id="not-helpful-${id}">0</span>
                </button>
                <button type="button" class="review-btn" data-action="share" data-review-id="${id}">
                    Share
                </button>
            </div>

            <div id="reportForm-${id}" class="report-form" style="display: none;">
                <label for="reportReason-${id}">Report Reason</label>
                <select id="reportReason-${id}">
                    <option value="">Select reason</option>
                    <option value="Spam">Spam</option>
                    <option value="Fake Review">Fake Review</option>
                    <option value="Offensive Content">Offensive Content</option>
                    <option value="Other">Other</option>
                </select>
                <button type="button" class="submit-report-button" data-action="submit-report" data-review-id="${id}">
                    Submit Report
                </button>
                <p id="reportMessage-${id}" class="report-message"></p>
            </div>

        </article>
    `;
}


/* =========================================================
   3-DOT MENU
   ========================================================= */

function closeReviewMenus() {
    document.querySelectorAll(".review-menu").forEach(function (menu) {
        menu.hidden = true;
    });

    document.querySelectorAll(".menu-toggle").forEach(function (button) {
        button.setAttribute("aria-expanded", "false");
    });
}

function toggleReviewMenu(button) {
    const menu = button.nextElementSibling;

    if (!menu) {
        return;
    }

    const willOpen = menu.hidden;

    closeReviewMenus();

    if (willOpen) {
        menu.hidden = false;
        button.setAttribute("aria-expanded", "true");

        const firstItem = menu.querySelector("button");

        if (firstItem) {
            firstItem.focus();
        }
    }
}


/* =========================================================
   VOTES
   ========================================================= */

function submitVote(reviewId, voteType) {
    const userId = getUserId();

    if (!userId) {
        alert("Please login before voting.");
        return;
    }

    const vote = {
        reviewId: Number(reviewId),
        userId: Number(userId),
        voteType: voteType
    };

    fetch(VOTE_API, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(vote)
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Vote failed");
            }

            return response.json();
        })
        .then(function () {
            loadVoteCounts(reviewId);
        })
        .catch(function (error) {
            console.error(error);
            alert("Unable to submit vote.");
        });
}

function markHelpful(reviewId) {
    submitVote(reviewId, "HELPFUL");
}

function markNotHelpful(reviewId) {
    submitVote(reviewId, "NOT_HELPFUL");
}

function loadVoteCounts(reviewId) {
    fetch(VOTE_API + "/" + reviewId + "/count")
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Vote count API error");
            }

            return response.json();
        })
        .then(function (data) {
            const helpfulElement = document.getElementById("helpful-" + reviewId);
            const notHelpfulElement = document.getElementById("not-helpful-" + reviewId);

            if (helpfulElement) {
                helpfulElement.textContent = data.helpful || 0;
            }

            if (notHelpfulElement) {
                notHelpfulElement.textContent = data.notHelpful || 0;
            }
        })
        .catch(function (error) {
            console.error("Unable to load vote counts:", error);
        });
}


/* =========================================================
   SHARE
   ========================================================= */

function shareReview(reviewId) {
    const url =
        window.location.origin +
        window.location.pathname +
        window.location.search +
        "#review-" + reviewId;

    if (navigator.share) {
        navigator.share({ title: document.title, url: url }).catch(function () { });
        return;
    }

    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url)
            .then(function () {
                showToast("Link copied.");
            })
            .catch(function () {
                window.prompt("Copy this link:", url);
            });

        return;
    }

    window.prompt("Copy this link:", url);
}


/* =========================================================
   EDIT REVIEW
   ========================================================= */

function editReview(reviewId) {
    const userId = getUserId();

    if (!userId) {
        alert("Please login before editing a review.");
        return;
    }

    const current = allReviews.find(function (item) {
        return Number(item.id) === Number(reviewId);
    });

    const newRating = prompt(
        "Enter new rating (1-5):",
        current ? current.rating : ""
    );

    if (newRating === null) {
        return;
    }

    const rating = Number(newRating);

    if (!rating || rating < 1 || rating > 5) {
        alert("Please enter a valid rating between 1 and 5.");
        return;
    }

    const newComment = prompt(
        "Enter new review:",
        current ? current.comment || "" : ""
    );

    if (newComment === null) {
        return;
    }

    const comment = newComment.trim();

    if (!comment) {
        alert("Review cannot be empty.");
        return;
    }

    const review = {
        rating: rating,
        comment: comment,
        userId: Number(userId),
        websiteId: Number(websiteId)
    };

    fetch(REVIEW_API + "/" + reviewId, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(review)
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorText) {
                    console.error("Edit review API error:", errorText);
                    throw new Error("Review could not be updated");
                });
            }

            return response.json();
        })
        .then(function () {
            alert("Review updated successfully!");

            loadReviews();
            loadRatingSummary();
        })
        .catch(function (error) {
            console.error(error);
            alert("Review could not be updated.");
        });
}


/* =========================================================
   DELETE REVIEW
   ========================================================= */

function deleteReview(reviewId) {
    const userId = getUserId();

    if (!userId) {
        alert("Please login before deleting a review.");
        return;
    }

    if (!confirm("Are you sure you want to delete this review?")) {
        return;
    }

    fetch(REVIEW_API + "/" + reviewId, {
        method: "DELETE"
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Delete review failed");
            }

            return response.text();
        })
        .then(function () {
            alert("Review deleted successfully!");

            loadReviews();
            loadRatingSummary();
        })
        .catch(function (error) {
            console.error(error);
            alert("Review could not be deleted.");
        });
}


/* =========================================================
   REPORT REVIEW
   ========================================================= */

function showReportForm(reviewId) {
    const reportForm = document.getElementById("reportForm-" + reviewId);

    if (!reportForm) {
        return;
    }

    if (reportForm.style.display === "none" || reportForm.style.display === "") {
        reportForm.style.display = "block";
    } else {
        reportForm.style.display = "none";
    }
}

function submitReport(reviewId) {
    const reasonElement = document.getElementById("reportReason-" + reviewId);
    const message = document.getElementById("reportMessage-" + reviewId);

    if (!reasonElement || !message) {
        return;
    }

    const reason = reasonElement.value;

    if (reason === "") {
        message.innerHTML = `
            <span class="error-message">Please select a reason.</span>
        `;
        return;
    }

    const userId = getUserId();

    if (!userId) {
        message.innerHTML = `
            <span class="error-message">Please login before reporting a review.</span>
        `;
        return;
    }

    const report = {
        userId: Number(userId),
        reviewId: Number(reviewId),
        reason: reason
    };

    fetch(REPORT_API, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(report)
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorText) {
                    console.error("Report API error:", errorText);
                    throw new Error("Report could not be submitted");
                });
            }

            return response.json();
        })
        .then(function () {
            message.innerHTML = `
                <span class="success-message">Review reported successfully!</span>
            `;

            reasonElement.value = "";
        })
        .catch(function (error) {
            console.error(error);

            message.innerHTML = `
                <span class="error-message">Report could not be submitted.</span>
            `;
        });
}


/* =========================================================
   ADD REVIEW
   ========================================================= */

function setupReviewForm() {
    const reviewForm = document.getElementById("reviewForm");

    if (!reviewForm) {
        return;
    }

    reviewForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const ratingElement = document.getElementById("rating");
        const commentElement = document.getElementById("comment");
        const message = document.getElementById("message");

        if (!ratingElement || !commentElement || !message) {
            console.error("Review form elements not found.");
            return;
        }

        const rating = Number(ratingElement.value);
        const comment = commentElement.value.trim();

        if (!websiteId) {
            message.innerHTML = `
                <span class="error-message">Website ID not found.</span>
            `;
            return;
        }

        const user = getLoggedInUser();

        if (!user || !user.id) {
            message.innerHTML = `
                <span class="error-message">Please login before submitting a review.</span>
            `;
            return;
        }

        if (!rating || rating < 1 || rating > 5) {
            message.innerHTML = `
                <span class="error-message">Please select a valid rating.</span>
            `;
            return;
        }

        if (!comment) {
            message.innerHTML = `
                <span class="error-message">Please enter your review.</span>
            `;
            return;
        }

        const review = {
            rating: rating,
            comment: comment,
            userId: Number(user.id),
            websiteId: Number(websiteId)
        };

        fetch(REVIEW_API, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(review)
        })
            .then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (errorText) {
                        console.error("Review API error:", errorText);

                        throw new Error(errorText || "Review could not be added");
                    });
                }

                return response.json();
            })
            .then(function () {
                message.innerHTML = `
                    <span class="success-message">Review added successfully!</span>
                `;

                reviewForm.reset();

                loadReviews();
                loadRatingSummary();
            })
            .catch(function (error) {
                console.error("Review submit error:", error);

                message.innerHTML = `
                    <span class="error-message">Review could not be added.</span>
                `;
            });
    });
}


/* =========================================================
   EVENT HANDLERS
   ========================================================= */

function setupReviewButtonHandlers() {
    const reviewList = document.getElementById("reviewList");

    if (!reviewList) {
        return;
    }

    reviewList.addEventListener("click", function (event) {
        const button = event.target.closest("button[data-action]");

        if (!button) {
            return;
        }

        const action = button.dataset.action;
        const reviewId = button.dataset.reviewId;

        if (!reviewId) {
            return;
        }

        if (action === "toggle-menu") {
            toggleReviewMenu(button);
            return;
        }

        if (button.closest(".review-menu")) {
            closeReviewMenus();
        }

        if (action === "helpful") {
            markHelpful(reviewId);
            return;
        }

        if (action === "not-helpful") {
            markNotHelpful(reviewId);
            return;
        }

        if (action === "share") {
            shareReview(reviewId);
            return;
        }

        if (action === "edit") {
            editReview(reviewId);
            return;
        }

        if (action === "delete") {
            deleteReview(reviewId);
            return;
        }

        if (action === "show-report") {
            showReportForm(reviewId);
            return;
        }

        if (action === "submit-report") {
            submitReport(reviewId);
        }
    });

    document.addEventListener("click", function (event) {
        if (!event.target.closest(".menu-wrap")) {
            closeReviewMenus();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeReviewMenus();
        }
    });
}

function setupReviewControls() {
    const chips = document.querySelectorAll(".filter-chip");

    chips.forEach(function (chip) {
        chip.addEventListener("click", function () {
            reviewFilter = Number(chip.dataset.filter);
            visibleReviewCount = REVIEWS_PER_PAGE;

            chips.forEach(function (other) {
                const active = other === chip;

                other.classList.toggle("active", active);
                other.setAttribute("aria-pressed", String(active));
            });

            displayReviews();
        });
    });

    const sortSelect = document.getElementById("reviewSort");

    if (sortSelect) {
        sortSelect.addEventListener("change", function () {
            reviewSort = sortSelect.value;
            displayReviews();
        });
    }

    const loadMore = document.getElementById("loadMoreButton");

    if (loadMore) {
        loadMore.addEventListener("click", function () {
            visibleReviewCount += REVIEWS_PER_PAGE;
            displayReviews();
        });
    }
}

function setupWriteReviewButton() {
    const writeReviewButton = document.getElementById("writeReviewButton");
    const writeReviewSection = document.querySelector(".write-review-section");

    if (!writeReviewButton || !writeReviewSection) {
        return;
    }

    writeReviewButton.addEventListener("click", function () {
        writeReviewSection.classList.add("show");

        writeReviewSection.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    });
}


/* =========================================================
   ACCOUNT NAVIGATION
   ========================================================= */

function setupAccountNavigation() {
    const navLinks = document.querySelector(".nav-links");

    if (!navLinks) {
        return;
    }

    const existingLink = document.getElementById("accountNavLink");

    if (existingLink) {
        existingLink.remove();
    }

    let loggedInUser = null;

    try {
        loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (error) {
        loggedInUser = null;
    }

    if (!loggedInUser || !loggedInUser.id) {
        return;
    }

    const accountLink = document.createElement("a");

    accountLink.href = "user-dashboard.html";
    accountLink.id = "accountNavLink";
    accountLink.textContent = "My Dashboard";

    navLinks.appendChild(accountLink);
}


/* =========================================================
   PAGE LOAD
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    setupAccountNavigation();

    loadWebsiteInfo();
    loadRatingSummary();
    loadReviews();

    setupReviewForm();
    setupReviewButtonHandlers();
    setupWriteReviewButton();
    setupReviewControls();
});