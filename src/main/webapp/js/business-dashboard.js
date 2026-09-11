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
    "/review-platform/business-dashboard/user/" + USER_ID;

const WEBSITE_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID
    + "/website";

const REVIEWS_API =
    "/review-platform/business-dashboard/user/"
    + USER_ID
    + "/reviews";

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
                ${business.id}
            </p>

            <p>
                <strong>Name:</strong>
                ${business.name || "Not available"}
            </p>

            <p>
                <strong>Description:</strong>
                ${business.description || "Not available"}
            </p>

            <p>
                <strong>Official URL:</strong>
                ${business.officialUrl || "Not available"}
            </p>

            <p>
                <strong>Status:</strong>
                ${business.status || "Not available"}
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
                ${website.id}
            </p>

            <p>
                <strong>Name:</strong>
                ${website.name || "Not available"}
            </p>

            <p>
                <strong>URL:</strong>
                ${website.url || "Not available"}
            </p>

            <p>
                <strong>Description:</strong>
                ${website.description || "Not available"}
            </p>

            <p>
                <strong>Domain:</strong>
                ${website.canonicalDomain || "Not available"}
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


                if (response) {

                    responseHTML = `

                        <div class="business-response">

                            <h4>Business Response</h4>

                            <p>
                                ${response.response || ""}
                            </p>

                            <button
                                onclick="updateResponse(${review.id})">
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
                                onclick="respondToReview(${review.id})">
                                Respond
                            </button>

                        </div>

                    `;

                }


                reviewList.innerHTML += `

                    <div class="review-card">

                        <h3>
                            ⭐ ${review.rating}/5
                        </h3>

                        <p>
                            ${review.comment || "No comment"}
                        </p>

                        <p>
                            <strong>User ID:</strong>
                            ${review.userId}
                        </p>

                        <p>
                            <strong>Review ID:</strong>
                            ${review.id}
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
        + reviewId;

}


/* =========================
   UPDATE RESPONSE
========================= */

function updateResponse(reviewId) {

    window.location.href =
        "business-response.html?reviewId="
        + reviewId;

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

    }
);