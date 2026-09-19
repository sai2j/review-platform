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

const USER_ID = loggedInUser.id;


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


/* =========================

   LOAD BUSINESS

========================= */

function loadBusiness() {

    fetch(BUSINESS_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error("Business API Error");

            }

            return response.json();

        })

        .then(function(business) {

            displayBusiness(business);

        })

        .catch(function(error) {

            console.error(error);

            const businessInfo =

                document.getElementById("businessInfo");

            if (businessInfo) {

                businessInfo.innerHTML =

                    "<p>Unable to load business information.</p>";

            }

        });

}


/* =========================

   DISPLAY BUSINESS

========================= */

function displayBusiness(business) {

    const businessInfo =

        document.getElementById("businessInfo");

    if (!businessInfo) {

        return;

    }

    if (!business) {

        businessInfo.innerHTML =

            "<p>No approved business found.</p>";

        return;

    }

    businessInfo.innerHTML = `

        <div class="info-card">

            <p>

                <strong>Business ID:</strong>

                ${Number(business.id) || 0}

            </p>

            <p>

                <strong>Name:</strong>

                ${escapeHtml(

                    business.name || "Not available"

                )}

            </p>

            <p>

                <strong>Description:</strong>

                ${escapeHtml(

                    business.description || "Not available"

                )}

            </p>

            <p>

                <strong>Official URL:</strong>

                ${escapeHtml(

                    business.officialUrl || "Not available"

                )}

            </p>

            <p>

                <strong>Status:</strong>

                ${escapeHtml(

                    business.status || "Not available"

                )}

            </p>

        </div>

    `;

}


/* =========================

   LOAD WEBSITE

========================= */

function loadWebsite() {

    fetch(WEBSITE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error("Website API Error");

            }

            return response.json();

        })

        .then(function(website) {

            displayWebsite(website);

        })

        .catch(function(error) {

            console.error(error);

            const websiteInfo =

                document.getElementById("websiteInfo");

            if (websiteInfo) {

                websiteInfo.innerHTML =

                    "<p>Unable to load website information.</p>";

            }

        });

}


/* =========================

   DISPLAY WEBSITE

========================= */

function displayWebsite(website) {

    const websiteInfo =

        document.getElementById("websiteInfo");

    if (!websiteInfo) {

        return;

    }

    if (!website) {

        websiteInfo.innerHTML =

            "<p>No matching website found.</p>";

        return;

    }

    websiteInfo.innerHTML = `

        <div class="info-card">

            <p>

                <strong>Website ID:</strong>

                ${Number(website.id) || 0}

            </p>

            <p>

                <strong>Name:</strong>

                ${escapeHtml(

                    website.name || "Not available"

                )}

            </p>

            <p>

                <strong>URL:</strong>

                ${escapeHtml(

                    website.url || "Not available"

                )}

            </p>

            <p>

                <strong>Description:</strong>

                ${escapeHtml(

                    website.description || "Not available"

                )}

            </p>

            <p>

                <strong>Domain:</strong>

                ${escapeHtml(

                    website.canonicalDomain || "Not available"

                )}

            </p>

        </div>

    `;

}


/* =========================

   LOAD REVIEWS

========================= */

function loadReviews() {

    fetch(REVIEWS_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error("Reviews API Error");

            }

            return response.json();

        })

        .then(function(reviews) {

            displayReviews(reviews);

            calculateSummary(reviews);

        })

        .catch(function(error) {

            console.error(error);

            const reviewList =

                document.getElementById("reviewList");

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

            displayDashboardMetrics(metrics);

        })

        .catch(function(error) {

            console.error(error);

            const responseRate =

                document.getElementById("responseRate");

            const ratingTrend =

                document.getElementById("ratingTrend");

            if (responseRate) {

                responseRate.textContent = "0.0%";

            }

            if (ratingTrend) {

                ratingTrend.innerHTML =

                    "<p>Unable to load rating trend.</p>";

            }

        });

}


/* =========================

   DISPLAY DASHBOARD METRICS

========================= */

function displayDashboardMetrics(metrics) {

    if (!metrics) {

        return;

    }

    const responseRate =

        document.getElementById("responseRate");

    if (responseRate) {

        responseRate.textContent =

            Number(metrics.responseRate || 0)

                .toFixed(1) + "%";

    }

    const averageRating =

        document.getElementById("averageRating");

    if (averageRating) {

        averageRating.textContent =

            Number(metrics.averageRating || 0)

                .toFixed(1);

    }

    const reviewCount =

        document.getElementById("reviewCount");

    if (reviewCount) {

        reviewCount.textContent =

            Number(metrics.reviewCount || 0);

    }

    displayRatingTrend(metrics.ratingTrend || []);

}


/* =========================

   DISPLAY RATING TREND

========================= */

function displayRatingTrend(trend) {

    const ratingTrend =

        document.getElementById("ratingTrend");

    if (!ratingTrend) {

        return;

    }

    if (!trend || trend.length === 0) {

        ratingTrend.innerHTML =

            "<p>No rating trend data available yet.</p>";

        return;

    }

    ratingTrend.innerHTML = "";

    trend.forEach(function(item) {

        ratingTrend.innerHTML += `

            <div class="info-card">

                <p>

                    <strong>Month:</strong>

                    ${escapeHtml(item.month || "")}

                </p>

                <p>

                    <strong>Average Rating:</strong>

                    ${Number(item.averageRating || 0)

                        .toFixed(1)}

                    / 5

                </p>

                <p>

                    <strong>Reviews:</strong>

                    ${Number(item.reviewCount || 0)}

                </p>

            </div>

        `;

    });

}


/* =========================

   LOAD BUSINESS RESPONSES

========================= */

function loadResponses() {

    return fetch(RESPONSE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Business Response API Error"

                );

            }

            return response.json();

        })

        .catch(function(error) {

            console.error(error);

            return [];

        });

}


/* =========================

   DISPLAY REVIEWS

========================= */

function displayReviews(reviews) {

    const reviewList =

        document.getElementById("reviewList");

    if (!reviewList) {

        return;

    }

    reviewList.innerHTML = "";

    if (!reviews || reviews.length === 0) {

        reviewList.innerHTML =

            "<p>No customer reviews found.</p>";

        return;

    }

    loadResponses()

        .then(function(responses) {

            reviews.forEach(function(review) {

                const response =

                    responses.find(function(item) {

                        return Number(item.reviewId)

                            === Number(review.id);

                    });

                let responseHTML = "";

                const reviewId =

                    Number(review.id) || 0;

                const userId =

                    Number(review.userId) || 0;

                const rating =

                    Number(review.rating) || 0;

                if (response) {

                    responseHTML = `

                        <div class="business-response">

                            <h4>Business Response</h4>

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

                reviewList.innerHTML += `

                    <div class="review-card">

                        <h3>

                            ⭐ ${rating}/5

                        </h3>

                        <p>

                            ${escapeHtml(

                                review.comment || "No comment"

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

        });

}


/* =========================

   CALCULATE SUMMARY

========================= */

function calculateSummary(reviews) {

    const reviewCount =

        reviews ? reviews.length : 0;

    const reviewCountElement =

        document.getElementById("reviewCount");

    const averageRatingElement =

        document.getElementById("averageRating");

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

            Number(review.rating) || 0;

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

        .replace(/&/g, "&amp;")

        .replace(/</g, "&lt;")

        .replace(/>/g, "&gt;")

        .replace(/"/g, "&quot;")

        .replace(/'/g, "&#039;");

}


/* =========================

   PAGE LOAD

========================= */

document.addEventListener(

    "DOMContentLoaded",

    function() {

        loadBusiness();

        loadWebsite();

        loadReviews();

        loadDashboardMetrics();

    }

);