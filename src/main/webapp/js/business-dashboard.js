/* =========================
   GET LOGGED-IN USER
========================= */

const loggedInUser =
    JSON.parse(localStorage.getItem("loggedInUser"));

if (!loggedInUser || !loggedInUser.id) {

    alert("Please login first.");

    window.location.href = "login.html";

}


/* =========================
   USER ID
========================= */

const USER_ID =
    loggedInUser.id;


/* =========================
   API URLs
========================= */

const BUSINESS_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID;

const WEBSITE_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID
    + "/website";

const REVIEWS_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID
    + "/reviews";

const METRICS_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID
    + "/metrics";

const RESPONSE_API =
    "/review-platform/business-responses";

const SUBSCRIPTION_API =
    "/review-platform/subscriptions/business/";


/* =========================
   PAGINATION VARIABLES
========================= */
let allReviews = [];
let allResponses = [];
let currentPage = 1;
const itemsPerPage = 5;


/* =========================
   LOGOUT
========================= */

function logoutUser() {

    localStorage.removeItem("loggedInUser");

    localStorage.removeItem("myBusiness");

    window.location.href = "login.html";

}


/* =========================
   LOAD BUSINESS
========================= */

function loadBusiness() {

    fetch(BUSINESS_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business API Error"
                );

            }

            return response.json();

        })

        .then(function(business) {

            if (business && business.id) {

                localStorage.setItem(
                    "myBusiness",
                    JSON.stringify(business)
                );

            }

            displayBusiness(business);

            if (business && business.id) {

                loadSubscription(
                    business.id
                );

            } else {

                displayNoSubscription();

            }

        })

        .catch(function(error) {

            console.error(error);

            const businessInfo =
                document.getElementById(
                    "businessInfoCard"
                );

            if (businessInfo) {

                businessInfo.innerHTML =
                    "<p>Unable to load business information.</p>";

            }

            displayNoSubscription();

        });

}


/* =========================
   DISPLAY BUSINESS
========================= */

function displayBusiness(business) {

    const bizId = document.getElementById("bizId");
    const bizName = document.getElementById("bizName");
    const bizDesc = document.getElementById("bizDesc");
    const bizUrl = document.getElementById("bizUrl");
    const bizStatus = document.getElementById("bizStatus");

    if (!business) {
        return;
    }

    localStorage.setItem(
        "myBusiness",
        JSON.stringify(business)
    );

    if (bizId) bizId.textContent = Number(business.id) || 0;
    if (bizName) bizName.textContent = business.name || "Not available";
    if (bizDesc) bizDesc.textContent = business.description || "Not available";
    if (bizUrl) {
        bizUrl.href = business.officialUrl || "#";
        bizUrl.textContent = business.officialUrl || "Not available";
    }
    if (bizStatus) bizStatus.textContent = business.status || "Not available";

}


/* =========================
   LOAD SUBSCRIPTION
========================= */

function loadSubscription(businessId) {

    const subscriptionStatusText =
        document.getElementById(
            "subscriptionStatusText"
        );

    if (!subscriptionStatusText) {

        return;

    }

    fetch(
        SUBSCRIPTION_API
        + Number(businessId)
    )

        .then(function(response) {

            if (response.status === 404) {

                return null;

            }

            if (!response.ok) {

                throw new Error(
                    "Subscription API Error"
                );

            }

            return response.json();

        })

        .then(function(subscription) {

            if (!subscription) {

                displayNoSubscription();

                return;

            }

            if (
                !subscription.status ||
                subscription.status.toUpperCase()
                    !== "ACTIVE"
            ) {

                displayNoSubscription();

                return;

            }

            displaySubscription(
                subscription
            );

        })

        .catch(function(error) {

            console.error(error);

            if (subscriptionStatusText) {

                subscriptionStatusText.innerHTML = `
                    Unable to load subscription information.
                `;

            }

        });

}


/* =========================
   DISPLAY ACTIVE SUBSCRIPTION
========================= */

function displaySubscription(
    subscription
) {

    const subscriptionStatusText =
        document.getElementById(
            "subscriptionStatusText"
        );

    if (!subscriptionStatusText) {

        return;

    }

    const planName =
        subscription.planName ||
        "Not available";

    const amount =
        Number(subscription.amount || 0);

    subscriptionStatusText.innerHTML = `
        <strong>Current Plan:</strong> ${escapeHtml(planName)} (₹${amount.toFixed(2)})
    `;

}


/* =========================
   NO ACTIVE SUBSCRIPTION
========================= */

function displayNoSubscription() {

    const subscriptionStatusText =
        document.getElementById(
            "subscriptionStatusText"
        );

    if (!subscriptionStatusText) {

        return;

    }

    subscriptionStatusText.textContent = "No active subscription found.";

}


/* =========================
   LOAD WEBSITE
========================= */

function loadWebsite() {

    fetch(WEBSITE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Website API Error"
                );

            }

            return response.json();

        })

        .then(function(website) {

            displayWebsite(website);

        })

        .catch(function(error) {

            console.error(error);

            const websiteInfoCard =
                document.getElementById(
                    "websiteInfoCard"
                );

            if (websiteInfoCard) {

                websiteInfoCard.innerHTML =
                    "<p>Unable to load website information.</p>";

            }

        });

}


/* =========================
   DISPLAY WEBSITE
========================= */

function displayWebsite(website) {

    const webId = document.getElementById("webId");
    const webName = document.getElementById("webName");
    const webUrl = document.getElementById("webUrl");
    const webDesc = document.getElementById("webDesc");
    const webDomain = document.getElementById("webDomain");

    if (!website) {
        return;
    }

    if (webId) webId.textContent = Number(website.id) || 0;
    if (webName) webName.textContent = website.name || "Not available";
    if (webUrl) {
        webUrl.href = website.url || "#";
        webUrl.textContent = website.url || "Not available";
    }
    if (webDesc) webDesc.textContent = website.description || "Not available";
    if (webDomain) webDomain.textContent = website.canonicalDomain || "Not available";

}


/* =========================
   LOAD REVIEWS
========================= */

function loadReviews() {

    Promise.all([

        fetch(REVIEWS_API)

            .then(function(response) {

                if (!response.ok) {

                    throw new Error(
                        "Reviews API Error"
                    );

                }

                return response.json();

            }),

        fetch(BUSINESS_API)

            .then(function(response) {

                if (!response.ok) {

                    throw new Error(
                        "Business API Error"
                    );

                }

                return response.json();

            })

    ])

    .then(function(results) {

        allReviews = results[0] || [];

        const business = results[1];

        if (!business || !business.id) {

            throw new Error(
                "Business ID not found"
            );

        }

        localStorage.setItem(
            "myBusiness",
            JSON.stringify(business)
        );

        const businessResponseAPI =
            RESPONSE_API
            + "/business/"
            + business.id;

        return fetch(
            businessResponseAPI
        )

            .then(function(response) {

                if (!response.ok) {

                    throw new Error(
                        "Business Response API Error"
                    );

                }

                return response.json();

            })

            .then(function(responses) {

                allResponses = responses || [];

                renderReviewsPaginated();

                calculateSummary(
                    allReviews
                );

            });

    })

    .catch(function(error) {

        console.error(error);

        const reviewList =
            document.getElementById(
                "customerReviewsContainer"
            );

        if (reviewList) {

            reviewList.innerHTML =
                "<p>Unable to load reviews.</p>";

        }

    });

}


/* =========================
   LOAD DASHBOARD METRICS
========================= */

function loadDashboardMetrics() {

    fetch(METRICS_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Dashboard Metrics API Error"
                );

            }

            return response.json();

        })

        .then(function(metrics) {

            displayDashboardMetrics(
                metrics
            );

        })

        .catch(function(error) {

            console.error(error);

            const responseRate =
                document.getElementById(
                    "responseRate"
                );

            if (responseRate) {

                responseRate.textContent =
                    "0.0%";

            }

        });

}


/* =========================
   DISPLAY DASHBOARD METRICS
========================= */

function displayDashboardMetrics(
    metrics
) {

    if (!metrics) {

        return;

    }

    const responseRate =
        document.getElementById(
            "responseRate"
        );

    if (responseRate) {

        responseRate.textContent =
            Number(
                metrics.responseRate || 0
            ).toFixed(1)
            + "%";

    }

    const avgRating =
        document.getElementById(
            "avgRating"
        );

    if (avgRating) {

        avgRating.textContent =
            Number(
                metrics.averageRating || 0
            ).toFixed(1);

    }

    const totalReviews =
        document.getElementById(
            "totalReviews"
        );

    if (totalReviews) {

        totalReviews.textContent =
            Number(
                metrics.reviewCount || 0
            );

    }

}


/* =========================
   RENDER REVIEWS PAGINATED (5 per view)
========================= */

function renderReviewsPaginated() {

    const container =
        document.getElementById(
            "customerReviewsContainer"
        );

    const dotsContainer =
        document.getElementById(
            "paginationDots"
        );

    const prevBtn =
        document.getElementById(
            "prevReviewBtn"
        );

    const nextBtn =
        document.getElementById(
            "nextReviewBtn"
        );


    if (!container) {

        return;

    }


    container.innerHTML = "";


    if (!allReviews || allReviews.length === 0) {

        container.innerHTML =
            "<div class=\"info-card\"><p>No customer reviews found.</p></div>";

        if (dotsContainer) dotsContainer.innerHTML = "";
        if (prevBtn) prevBtn.style.display = "none";
        if (nextBtn) nextBtn.style.display = "none";

        return;

    }


    if (prevBtn) prevBtn.style.display = "inline-block";
    if (nextBtn) nextBtn.style.display = "inline-block";


    const totalPages =
        Math.ceil(allReviews.length / itemsPerPage);


    if (currentPage > totalPages) {

        currentPage = totalPages;

    }

    if (currentPage < 1) {

        currentPage = 1;

    }


    const start =
        (currentPage - 1) * itemsPerPage;

    const end =
        start + itemsPerPage;

    const paginatedReviews =
        allReviews.slice(start, end);


    paginatedReviews.forEach(function(review) {

        const response =
            allResponses.find(function(item) {

                return Number(
                    item.reviewId
                )
                ===
                Number(
                    review.id
                );

            });


        const reviewId =
            Number(review.id) || 0;

        const userId =
            Number(review.userId) || 0;

        const rating =
            Number(review.rating) || 0;


        let responseHTML = "";


        if (response) {

            responseHTML = `

                <div class="business-response">

                    <h4>
                        Business Response
                    </h4>

                    <p>

                        ${escapeHtml(
                            response.response || ""
                        )}

                    </p>

                    <button
                        onclick="updateResponse(${reviewId})">

                        Update Response

                    </button>

                </div>

            `;

        } else {

            responseHTML = `

                <div class="business-response">

                    <p>
                        No response added yet.
                    </p>

                    <button
                        onclick="respondToReview(${reviewId})">

                        Respond

                    </button>

                </div>

            `;

        }


        container.innerHTML += `

            <div class="review-card info-card" style="margin-bottom: 12px;">

                <h3>
                    ⭐ ${rating}/5
                </h3>

                <p>

                    ${escapeHtml(
                        review.comment ||
                        "No comment"
                    )}

                </p>

                <p>

                    <strong>User ID:</strong>

                    ${userId}

                </p>

                <p>

                    <strong>Review ID:</strong>

                    ${reviewId}

                </p>

                ${responseHTML}

            </div>

        `;

    });


    /* RENDER DOTS */

    if (dotsContainer) {

        dotsContainer.innerHTML = "";

        for (let i = 1; i <= totalPages; i++) {

            const dot =
                document.createElement("div");

            dot.className =
                `dot ${i === currentPage ? "active" : ""}`;

            dot.onclick = function() {

                currentPage = i;

                renderReviewsPaginated();

            };

            dotsContainer.appendChild(dot);

        }

    }


    /* BUTTON STATES */

    if (prevBtn) {

        prevBtn.disabled =
            currentPage === 1;

        prevBtn.onclick = function() {

            if (currentPage > 1) {

                currentPage--;

                renderReviewsPaginated();

            }

        };

    }


    if (nextBtn) {

        nextBtn.disabled =
            currentPage === totalPages;

        nextBtn.onclick = function() {

            if (currentPage < totalPages) {

                currentPage++;

                renderReviewsPaginated();

            }

        };

    }

}


/* =========================
   CALCULATE SUMMARY
========================= */

function calculateSummary(reviews) {

    const reviewCount =
        reviews
            ? reviews.length
            : 0;

    const reviewCountElement =
        document.getElementById(
            "totalReviews"
        );

    const averageRatingElement =
        document.getElementById(
            "avgRating"
        );

    if (reviewCountElement) {

        reviewCountElement.textContent =
            reviewCount;

    }

    if (reviewCount === 0) {

        if (averageRatingElement) {

            averageRatingElement.textContent =
                "0.0";

        }

        return;

    }

    let totalRating = 0;

    reviews.forEach(function(review) {

        totalRating +=
            Number(
                review.rating
            ) || 0;

    });

    const averageRating =
        totalRating / reviewCount;

    if (averageRatingElement) {

        averageRatingElement.textContent =
            averageRating.toFixed(1);

    }

}


/* =========================
   RESPOND TO REVIEW
========================= */

function respondToReview(reviewId) {

    window.location.href =
        "business-response.html?reviewId="
        + Number(reviewId);

}


/* =========================
   UPDATE RESPONSE
========================= */

function updateResponse(reviewId) {

    window.location.href =
        "business-response.html?reviewId="
        + Number(reviewId);

}


/* =========================
   ESCAPE HTML
========================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }

    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );

}


/* =========================
   PAGE LOAD
========================= */

document.addEventListener(
    "DOMContentLoaded",
    function() {

        const logoutButton =
            document.getElementById(
                "logoutBtn"
            );

        if (logoutButton) {

            logoutButton.addEventListener(
                "click",
                logoutUser
            );

        }

        loadBusiness();

        loadWebsite();

        loadReviews();

        loadDashboardMetrics();

    }
);