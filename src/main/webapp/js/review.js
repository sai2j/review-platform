const REVIEW_API = "/review-platform/reviews";

const WEBSITE_API = "/review-platform/websites";

const REPORT_API = "/review-platform/reports";

const RESPONSE_API = "/review-platform/business-responses";

const VOTE_API = "/review-platform/review-votes";

const BUSINESS_API = "/review-platform/businesses";

const BUSINESS_CLAIM_API =
    "/review-platform/business-claims";


const urlParams =
    new URLSearchParams(window.location.search);

const websiteId =
    urlParams.get("websiteId");


/* =========================================================

   GET LOGGED-IN USER

========================================================= */

function getLoggedInUser() {

    try {

        return JSON.parse(

            localStorage.getItem("loggedInUser")

        );

    } catch (error) {

        console.error(

            "Unable to read logged-in user:",

            error

        );

        return null;

    }

}


function getUserId() {

    const user =
        getLoggedInUser();

    if (!user || !user.id) {

        return null;

    }

    return Number(user.id);

}


/* =========================================================

   LOAD WEBSITE INFORMATION

========================================================= */

function loadWebsiteInfo() {

    if (!websiteId) {

        return;

    }

    fetch(

        WEBSITE_API + "/" + websiteId

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Website not found"

                );

            }

            return response.json();

        })

        .then(function(website) {

            const websiteInfo =
                document.getElementById(
                    "websiteInfo"
                );

            if (!websiteInfo) {

                return;

            }

            websiteInfo.innerHTML = `

                <h2>

                    ${escapeHtml(
                        website.name || "Website"
                    )}

                </h2>

                <p>

                    ${escapeHtml(
                        website.description || ""
                    )}

                </p>

                <p>

                    <strong>Website:</strong>

                    <a

                        href="${escapeAttribute(
                            website.url
                        )}"

                        target="_blank"

                        rel="noopener noreferrer">

                        ${escapeHtml(
                            website.url
                        )}

                    </a>

                </p>

                <div id="claimStatus">

                    Checking business verification...

                </div>

            `;

            loadClaimStatus();

        })

        .catch(function(error) {

            console.error(error);

            const websiteInfo =
                document.getElementById(
                    "websiteInfo"
                );

            if (websiteInfo) {

                websiteInfo.innerHTML = `

                    <p class="error-message">

                        Unable to load website information.

                    </p>

                `;

            }

        });

}


/* =========================================================

   LOAD BUSINESS CLAIM STATUS

========================================================= */

function loadClaimStatus() {

    if (!websiteId) {

        return;

    }

    const claimStatus =
        document.getElementById(
            "claimStatus"
        );

    if (!claimStatus) {

        return;

    }

    fetch(

        BUSINESS_API +

        "/website/" +

        websiteId

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business not found"
                );

            }

            return response.json();

        })

        .then(function(business) {

            if (!business || !business.id) {

                claimStatus.innerHTML = "";

                return;

            }

            return fetch(

                BUSINESS_CLAIM_API +

                "/business/" +

                business.id +

                "/approved"

            )

                .then(function(response) {

                    if (!response.ok) {

                        throw new Error(
                            "Claim status API error"
                        );

                    }

                    return response.json();

                })

                .then(function(claim) {

                    if (claim && claim.id) {

                        claimStatus.innerHTML = `

                            <div class="claimed-badge">

                                ✓ Claimed & Verified

                            </div>

                        `;

                    } else {

                        claimStatus.innerHTML = "";

                    }

                });

        })

        .catch(function(error) {

            console.error(

                "Unable to load business claim status:",

                error

            );

            claimStatus.innerHTML = "";

        });

}


/* =========================================================

   LOAD RATING SUMMARY

========================================================= */

function loadRatingSummary() {

    if (!websiteId) {

        return;

    }

    fetch(

        REVIEW_API +

        "/website/" +

        websiteId +

        "/summary"

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Rating Summary API Error"

                );

            }

            return response.json();

        })

        .then(function(summary) {

            const averageRating =
                Number(
                    summary.averageRating || 0
                );

            const totalReview =
                Number(
                    summary.totalReview || 0
                );

            const fiveStar =
                Number(
                    summary.fivestar || 0
                );

            const fourStar =
                Number(
                    summary.fourStar || 0
                );

            const threeStar =
                Number(
                    summary.threeStar || 0
                );

            const twoStar =
                Number(
                    summary.twoStar || 0
                );

            const oneStar =
                Number(
                    summary.oneStar || 0
                );


            const averageElement =
                document.getElementById(
                    "averageRating"
                );

            if (averageElement) {

                averageElement.innerHTML = `

                    <h3>

                        ⭐ ${averageRating.toFixed(1)}/5

                    </h3>

                `;

            }


            const totalElement =
                document.getElementById(
                    "totalReviews"
                );

            if (totalElement) {

                totalElement.innerHTML = `

                    <p>

                        Total Reviews:

                        <strong>

                            ${totalReview}

                        </strong>

                    </p>

                `;

            }


            const fiveElement =
                document.getElementById(
                    "fiveStarCount"
                );

            if (fiveElement) {

                fiveElement.textContent =
                    fiveStar;

            }


            const fourElement =
                document.getElementById(
                    "fourStarCount"
                );

            if (fourElement) {

                fourElement.textContent =
                    fourStar;

            }


            const threeElement =
                document.getElementById(
                    "threeStarCount"
                );

            if (threeElement) {

                threeElement.textContent =
                    threeStar;

            }


            const twoElement =
                document.getElementById(
                    "twoStarCount"
                );

            if (twoElement) {

                twoElement.textContent =
                    twoStar;

            }


            const oneElement =
                document.getElementById(
                    "oneStarCount"
                );

            if (oneElement) {

                oneElement.textContent =
                    oneStar;

            }


            const fiveBar =
                document.getElementById(
                    "fiveStarBar"
                );

            if (fiveBar) {

                fiveBar.style.width =
                    calculatePercentage(
                        fiveStar,
                        totalReview
                    ) + "%";

            }


            const fourBar =
                document.getElementById(
                    "fourStarBar"
                );

            if (fourBar) {

                fourBar.style.width =
                    calculatePercentage(
                        fourStar,
                        totalReview
                    ) + "%";

            }


            const threeBar =
                document.getElementById(
                    "threeStarBar"
                );

            if (threeBar) {

                threeBar.style.width =
                    calculatePercentage(
                        threeStar,
                        totalReview
                    ) + "%";

            }


            const twoBar =
                document.getElementById(
                    "twoStarBar"
                );

            if (twoBar) {

                twoBar.style.width =
                    calculatePercentage(
                        twoStar,
                        totalReview
                    ) + "%";

            }


            const oneBar =
                document.getElementById(
                    "oneStarBar"
                );

            if (oneBar) {

                oneBar.style.width =
                    calculatePercentage(
                        oneStar,
                        totalReview
                    ) + "%";

            }

        })

        .catch(function(error) {

            console.error(error);

            const averageElement =
                document.getElementById(
                    "averageRating"
                );

            if (averageElement) {

                averageElement.innerHTML = `

                    <p class="error-message">

                        Unable to load rating.

                    </p>

                `;

            }


            const totalElement =
                document.getElementById(
                    "totalReviews"
                );

            if (totalElement) {

                totalElement.innerHTML = "";

            }

        });

}


/* =========================================================

   CALCULATE PERCENTAGE

========================================================= */

function calculatePercentage(

    count,

    total

) {

    if (!total || total === 0) {

        return 0;

    }

    return Math.round(

        (count / total) * 100

    );

}


/* =========================================================

   LOAD REVIEWS

========================================================= */

function loadReviews() {

    if (!websiteId) {

        const reviewList =
            document.getElementById(
                "reviewList"
            );

        if (reviewList) {

            reviewList.innerHTML = `

                <p class="error-message">

                    Website ID not found.

                </p>

            `;

        }

        return;

    }


    fetch(

        REVIEW_API +

        "/website/" +

        websiteId

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Review API error"

                );

            }

            return response.json();

        })

        .then(function(reviews) {

            displayReviews(reviews);

        })

        .catch(function(error) {

            console.error(error);

            const reviewList =
                document.getElementById(
                    "reviewList"
                );

            if (reviewList) {

                reviewList.innerHTML = `

                    <p class="error-message">

                        Unable to load reviews.

                    </p>

                `;

            }

        });

}


/* =========================================================

   DISPLAY REVIEWS

========================================================= */

function displayReviews(reviews) {

    const reviewList =
        document.getElementById(
            "reviewList"
        );

    if (!reviewList) {

        return;

    }


    reviewList.innerHTML = "";


    if (!reviews || reviews.length === 0) {

        reviewList.innerHTML = `

            <p class="no-review">

                No reviews available yet.

            </p>

        `;

        return;

    }


    reviews.forEach(function(review) {

        let stars = "";


        for (

            let i = 1;

            i <= 5;

            i++

        ) {

            if (i <= review.rating) {

                stars += "⭐";

            }

        }


        reviewList.innerHTML += `

            <div

                class="review-card"

                data-review-id="${review.id}">

                <!-- Rating -->

                <div class="rating">

                    ${stars}

                    <strong>

                        ${review.rating}/5

                    </strong>

                </div>


                <!-- Comment -->

                <div class="comment">

                    ${escapeHtml(
                        review.comment || ""
                    )}

                </div>


                <!-- Helpful -->

                <button

                    type="button"

                    class="helpful-button"

                    data-action="helpful"

                    data-review-id="${review.id}">

                    👍 Helpful

                </button>


                <!-- Not Helpful -->

                <button

                    type="button"

                    class="not-helpful-button"

                    data-action="not-helpful"

                    data-review-id="${review.id}">

                    👎 Not Helpful

                </button>


                <!-- Vote Count -->

                <span class="vote-count">

                    👍

                    <span

                        id="helpful-${review.id}">

                        0

                    </span>

                    &nbsp;&nbsp;

                    👎

                    <span

                        id="not-helpful-${review.id}">

                        0

                    </span>

                </span>


                <br>

                <br>


                <!-- Edit -->

                <button

                    type="button"

                    class="edit-button"

                    data-action="edit"

                    data-review-id="${review.id}">

                    Edit Review

                </button>


                <!-- Delete -->

                <button

                    type="button"

                    class="delete-button"

                    data-action="delete"

                    data-review-id="${review.id}">

                    Delete Review

                </button>


                <!-- Report -->

                <button

                    type="button"

                    class="report-button"

                    data-action="show-report"

                    data-review-id="${review.id}">

                    Report Review

                </button>


                <!-- Report Form -->

                <div

                    id="reportForm-${review.id}"

                    class="report-form"

                    style="display: none;">

                    <label>

                        Report Reason

                    </label>


                    <select

                        id="reportReason-${review.id}">

                        <option value="">

                            Select reason

                        </option>

                        <option value="Spam">

                            Spam

                        </option>

                        <option value="Fake Review">

                            Fake Review

                        </option>

                        <option value="Offensive Content">

                            Offensive Content

                        </option>

                        <option value="Other">

                            Other

                        </option>

                    </select>


                    <button

                        type="button"

                        class="submit-report-button"

                        data-action="submit-report"

                        data-review-id="${review.id}">

                        Submit Report

                    </button>


                    <p

                        id="reportMessage-${review.id}"

                        class="report-message">

                    </p>

                </div>


            </div>

        `;


        /*

         * Business response loading is intentionally

         * not called here.

         */

        loadVoteCounts(

            review.id

        );

    });

}


/* =========================================================

   LOAD BUSINESS RESPONSE

========================================================= */

function loadBusinessResponse(reviewId) {

    const responseElement =
        document.getElementById(
            "response-" + reviewId
        );

    if (!responseElement) {

        return;

    }


    fetch(RESPONSE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Business response API error"

                );

            }

            return response.json();

        })

        .then(function(responses) {

            const businessResponse =
                responses.find(function(item) {

                    return Number(item.reviewId) ===
                        Number(reviewId);

                });


            if (businessResponse) {

                responseElement.innerHTML = `

                    <strong>

                        Business Response:

                    </strong>

                    <p>

                        ${escapeHtml(

                            businessResponse.response || ""

                        )}

                    </p>

                `;

            } else {

                responseElement.innerHTML = `

                    <p>

                        No business response yet.

                    </p>

                `;

            }

        })

        .catch(function(error) {

            console.error(error);

            responseElement.innerHTML = `

                <p>

                    No business response available.

                </p>

            `;

        });

}


/* =========================================================

   OPEN BUSINESS RESPONSE PAGE

========================================================= */

function openBusinessResponse(

    reviewId,

    businessId

) {

    window.location.href =

        "business-response.html" +

        "?reviewId=" +

        reviewId +

        "&businessId=" +

        businessId;

}


/* =========================================================

   MARK REVIEW HELPFUL

========================================================= */

function markHelpful(reviewId) {

    const userId =
        getUserId();


    if (!userId) {

        alert(

            "Please login before voting."

        );

        return;

    }


    const vote = {

        reviewId:
            Number(reviewId),

        userId:
            Number(userId),

        voteType:
            "HELPFUL"

    };


    fetch(

        VOTE_API,

        {

            method: "POST",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(vote)

        }

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Helpful vote failed"

                );

            }

            return response.json();

        })

        .then(function() {

            loadVoteCounts(

                reviewId

            );

        })

        .catch(function(error) {

            console.error(error);

            alert(

                "Unable to submit vote."

            );

        });

}


/* =========================================================

   MARK REVIEW NOT HELPFUL

========================================================= */

function markNotHelpful(reviewId) {

    const userId =
        getUserId();


    if (!userId) {

        alert(

            "Please login before voting."

        );

        return;

    }


    const vote = {

        reviewId:
            Number(reviewId),

        userId:
            Number(userId),

        voteType:
            "NOT_HELPFUL"

    };


    fetch(

        VOTE_API,

        {

            method: "POST",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(vote)

        }

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Not helpful vote failed"

                );

            }

            return response.json();

        })

        .then(function() {

            loadVoteCounts(

                reviewId

            );

        })

        .catch(function(error) {

            console.error(error);

            alert(

                "Unable to submit vote."

            );

        });

}


/* =========================================================

   LOAD VOTE COUNTS

========================================================= */

function loadVoteCounts(reviewId) {

    fetch(

        VOTE_API +

        "/" +

        reviewId +

        "/count"

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Vote count API error"

                );

            }

            return response.json();

        })

        .then(function(data) {

            const helpfulElement =
                document.getElementById(
                    "helpful-" + reviewId
                );

            const notHelpfulElement =
                document.getElementById(
                    "not-helpful-" + reviewId
                );


            if (helpfulElement) {

                helpfulElement.textContent =
                    data.helpful || 0;

            }


            if (notHelpfulElement) {

                notHelpfulElement.textContent =
                    data.notHelpful || 0;

            }

        })

        .catch(function(error) {

            console.error(

                "Unable to load vote counts:",

                error

            );

        });

}


/* =========================================================

   EDIT REVIEW

========================================================= */

function editReview(reviewId) {

    const userId =
        getUserId();


    if (!userId) {

        alert(

            "Please login before editing a review."

        );

        return;

    }


    const newRating =
        prompt(

            "Enter new rating (1-5):"

        );


    if (newRating === null) {

        return;

    }


    const rating =
        Number(newRating);


    if (

        !rating ||

        rating < 1 ||

        rating > 5

    ) {

        alert(

            "Please enter a valid rating between 1 and 5."

        );

        return;

    }


    const newComment =
        prompt(

            "Enter new review:",

            ""

        );


    if (newComment === null) {

        return;

    }


    const comment =
        newComment.trim();


    if (!comment) {

        alert(

            "Review cannot be empty."

        );

        return;

    }


    const review = {

        rating:
            rating,

        comment:
            comment,

        userId:
            Number(userId),

        websiteId:
            Number(websiteId)

    };


    fetch(

        REVIEW_API +

        "/" +

        reviewId,

        {

            method: "PUT",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(review)

        }

    )

        .then(function(response) {

            if (!response.ok) {

                return response.text()

                    .then(function(errorText) {

                        console.error(

                            "Edit review API error:",

                            errorText

                        );

                        throw new Error(

                            "Review could not be updated"

                        );

                    });

            }

            return response.json();

        })

        .then(function() {

            alert(

                "Review updated successfully!"

            );

            loadReviews();

            loadRatingSummary();

        })

        .catch(function(error) {

            console.error(error);

            alert(

                "Review could not be updated."

            );

        });

}


/* =========================================================

   DELETE REVIEW

========================================================= */

function deleteReview(reviewId) {

    const userId =
        getUserId();


    if (!userId) {

        alert(

            "Please login before deleting a review."

        );

        return;

    }


    const confirmDelete =
        confirm(

            "Are you sure you want to delete this review?"

        );


    if (!confirmDelete) {

        return;

    }


    fetch(

        REVIEW_API +

        "/" +

        reviewId,

        {

            method: "DELETE"

        }

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Delete review failed"

                );

            }

            return response.text();

        })

        .then(function() {

            alert(

                "Review deleted successfully!"

            );

            loadReviews();

            loadRatingSummary();

        })

        .catch(function(error) {

            console.error(error);

            alert(

                "Review could not be deleted."

            );

        });

}


/* =========================================================

   SHOW / HIDE REPORT FORM

========================================================= */

function showReportForm(reviewId) {

    const reportForm =
        document.getElementById(

            "reportForm-" + reviewId

        );


    if (!reportForm) {

        return;

    }


    if (

        reportForm.style.display === "none" ||

        reportForm.style.display === ""

    ) {

        reportForm.style.display =
            "block";

    } else {

        reportForm.style.display =
            "none";

    }

}


/* =========================================================

   SUBMIT REPORT

========================================================= */

function submitReport(reviewId) {

    const reasonElement =
        document.getElementById(

            "reportReason-" + reviewId

        );


    const message =
        document.getElementById(

            "reportMessage-" + reviewId

        );


    if (!reasonElement || !message) {

        return;

    }


    const reason =
        reasonElement.value;


    if (reason === "") {

        message.innerHTML = `

            <span class="error-message">

                Please select a reason.

            </span>

        `;

        return;

    }


    const userId =
        getUserId();


    if (!userId) {

        message.innerHTML = `

            <span class="error-message">

                Please login before reporting a review.

            </span>

        `;

        return;

    }


    const report = {

        userId:
            Number(userId),

        reviewId:
            Number(reviewId),

        reason:
            reason

    };


    fetch(

        REPORT_API,

        {

            method: "POST",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(report)

        }

    )

        .then(function(response) {

            if (!response.ok) {

                return response.text()

                    .then(function(errorText) {

                        console.error(

                            "Report API error:",

                            errorText

                        );

                        throw new Error(

                            "Report could not be submitted"

                        );

                    });

            }

            return response.json();

        })

        .then(function() {

            message.innerHTML = `

                <span class="success-message">

                    Review reported successfully!

                </span>

            `;


            reasonElement.value = "";

        })

        .catch(function(error) {

            console.error(error);

            message.innerHTML = `

                <span class="error-message">

                    Report could not be submitted.

                </span>

            `;

        });

}


/* =========================================================

   ADD REVIEW

========================================================= */

function setupReviewForm() {

    const reviewForm =
        document.getElementById("reviewForm");


    if (!reviewForm) {

        return;

    }


    reviewForm.addEventListener(

        "submit",

        function(event) {

            event.preventDefault();


            const ratingElement =
                document.getElementById("rating");


            const commentElement =
                document.getElementById("comment");


            const message =
                document.getElementById("message");


            if (

                !ratingElement ||

                !commentElement ||

                !message

            ) {

                console.error(

                    "Review form elements not found."

                );

                return;

            }


            const rating =
                Number(ratingElement.value);


            const comment =
                commentElement.value.trim();


            /* =========================

               WEBSITE CHECK

            ========================= */

            if (!websiteId) {

                message.innerHTML = `

                    <span class="error-message">

                        Website ID not found.

                    </span>

                `;

                return;

            }


            /* =========================

               LOGIN CHECK

            ========================= */

            const user =
                getLoggedInUser();


            if (!user || !user.id) {

                message.innerHTML = `

                    <span class="error-message">

                        Please login before submitting a review.

                    </span>

                `;

                return;

            }


            /* =========================

               RATING CHECK

            ========================= */

            if (

                !rating ||

                rating < 1 ||

                rating > 5

            ) {

                message.innerHTML = `

                    <span class="error-message">

                        Please select a valid rating.

                    </span>

                `;

                return;

            }


            /* =========================

               COMMENT CHECK

            ========================= */

            if (!comment) {

                message.innerHTML = `

                    <span class="error-message">

                        Please enter your review.

                    </span>

                `;

                return;

            }


            /* =========================

               REVIEW OBJECT

            ========================= */

            const review = {

                rating:
                    rating,

                comment:
                    comment,

                userId:
                    Number(user.id),

                websiteId:
                    Number(websiteId)

            };


            console.log(

                "Sending review:",

                review

            );


            /* =========================

               SEND REVIEW

            ========================= */

            fetch(

                REVIEW_API,

                {

                    method: "POST",

                    credentials: "include",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify(review)

                }

            )

                .then(function(response) {

                    console.log(

                        "Review response status:",

                        response.status

                    );


                    if (!response.ok) {

                        return response.text()

                            .then(function(errorText) {

                                console.error(

                                    "Review API error:",

                                    errorText

                                );

                                throw new Error(

                                    errorText ||

                                    "Review could not be added"

                                );

                            });

                    }


                    return response.json();

                })

                .then(function(data) {

                    console.log(

                        "Review saved:",

                        data

                    );


                    message.innerHTML = `

                        <span class="success-message">

                            Review added successfully!

                        </span>

                    `;


                    reviewForm.reset();


                    loadReviews();

                    loadRatingSummary();

                })

                .catch(function(error) {

                    console.error(

                        "Review submit error:",

                        error

                    );


                    message.innerHTML = `

                        <span class="error-message">

                            Review could not be added.

                        </span>

                    `;

                });

        }

    );

}


/* =========================================================

   REVIEW BUTTON EVENT HANDLERS

   No inline onclick handlers

========================================================= */

function setupReviewButtonHandlers() {

    const reviewList =
        document.getElementById("reviewList");


    if (!reviewList) {

        return;

    }


    reviewList.addEventListener(

        "click",

        function(event) {

            const button =
                event.target.closest(
                    "button[data-action]"
                );


            if (!button) {

                return;

            }


            const action =
                button.dataset.action;


            const reviewId =
                button.dataset.reviewId;


            if (!reviewId) {

                return;

            }


            if (action === "helpful") {

                markHelpful(reviewId);

                return;

            }


            if (action === "not-helpful") {

                markNotHelpful(reviewId);

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

                return;

            }

        }

    );

}


/* =========================================================

   WRITE REVIEW BUTTON

========================================================= */

function setupWriteReviewButton() {

    const writeReviewButton =
        document.getElementById(

            "writeReviewButton"

        );


    if (!writeReviewButton) {

        return;

    }


    writeReviewButton.addEventListener(

        "click",

        function() {

            const reviewForm =
                document.getElementById(

                    "reviewForm"

                );


            if (reviewForm) {

                reviewForm.scrollIntoView({

                    behavior: "smooth"

                });

            }

        }

    );

}


/* =========================================================

   ESCAPE HTML

========================================================= */

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


/* =========================================================

   ESCAPE ATTRIBUTE

========================================================= */

function escapeAttribute(value) {

    return escapeHtml(value);

}


/* =========================================================

   ESCAPE JAVASCRIPT

========================================================= */

function escapeJs(value) {

    if (

        value === null ||

        value === undefined

    ) {

        return "";

    }


    return String(value)

        .replace(/\\/g, "\\\\")

        .replace(/'/g, "\\'")

        .replace(/"/g, '\\"')

        .replace(/\n/g, "\\n")

        .replace(/\r/g, "\\r");

}


/* =========================================================

   PAGE LOAD

========================================================= */

document.addEventListener(

    "DOMContentLoaded",

    function() {

        loadWebsiteInfo();

        loadRatingSummary();

        loadReviews();

        setupReviewForm();

        setupReviewButtonHandlers();

        setupWriteReviewButton();

    }

);