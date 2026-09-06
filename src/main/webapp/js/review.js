const REVIEW_API = "/review-platform/reviews";
const WEBSITE_API = "/review-platform/websites";
const REPORT_API = "/review-platform/reports";
const RESPONSE_API = "/review-platform/business-responses";
const urlParams = new URLSearchParams(window.location.search);
const websiteId = urlParams.get("websiteId");
function loadWebsiteInfo() {
    if (!websiteId) {return;}
    fetch(WEBSITE_API + "/" + websiteId)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Website not found");
            }
            return response.json();
        })
        .then(function(website) {
            document.getElementById("websiteInfo").innerHTML = `
                <h2>${website.name}</h2>
                <p> ${website.description || ""}</p>
                <p><strong>Website:</strong>
                    ${website.url}
                </p>
            `;
        })
        .catch(function(error) {
            console.error(error);
            document.getElementById("websiteInfo").innerHTML = `
                <p class="error-message">
                    Unable to load website information.
                </p>
            `;
        });
}
function loadReviews() {
    if (!websiteId) {
        document.getElementById("reviewList").innerHTML = `
            <p class="error-message">
                Website ID not found.
            </p>
        `;
        return;
    }
    fetch(REVIEW_API + "/website/" + websiteId)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Review API error");
            }
            return response.json();
        })
        .then(function(reviews) {
            displayReviews(reviews);
        })
        .catch(function(error) {
            console.error(error);
            document.getElementById("reviewList").innerHTML = `
                <p class="error-message">
                    Unable to load reviews.
                </p>
            `;
        });
}
function displayReviews(reviews) {
    const reviewList =document.getElementById("reviewList");
    reviewList.innerHTML = "";
    if (reviews.length === 0) {
        reviewList.innerHTML = `
            <p class="no-review">
                No reviews available yet.
            </p>
        `;
        return;
    }
    reviews.forEach(function(review) {
        let stars = "";
        for (let i = 1; i <= 5; i++) {
            if (i <= review.rating) {
                stars += "⭐";
            }
        }
        reviewList.innerHTML += `
            <div class="review-card">
                <div class="rating">
                    ${stars}
                    <strong>
                        ${review.rating}/5
                    </strong>
                </div>
                <div class="comment">
                    ${review.comment || ""}
                </div>
                <button
                    class="edit-button"
                    onclick="editReview(${review.id})">
                    Edit Review
                </button>
                <button
                    class="delete-button"
                    onclick="deleteReview(${review.id})">
                    Delete Review
                </button>
                <button
                    class="response-button"
                    onclick="openBusinessResponse(
                        ${review.id},
                        ${websiteId}
                    )">
                    Business Response
                </button>
                <button
                    class="report-button"
                    onclick="showReportForm(${review.id})">
                    Report Review
                </button>
                <div
                    id="reportForm-${review.id}"
                    class="report-form"
                    style="display: none;">
                    <label>
                        Report Reason
                    </label>
                    <select id="reportReason-${review.id}">
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
                    <button class="submit-report-button"
                        onclick="submitReport(${review.id})">
                        Submit Report
                    </button>
                    <p
                        id="reportMessage-${review.id}"
                        class="report-message">
                    </p>
                </div>
                <div
                    id="response-${review.id}"
                    class="business-response">
                    Loading business response...
                </div>
            </div>
        `;
        loadBusinessResponse(review.id);
    });
}
function loadBusinessResponse(reviewId) {
    fetch(RESPONSE_API)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Response API error");
            }
            return response.json();
        })
        .then(function(responses) {
            const responseBox =
                document.getElementById(
                    "response-" + reviewId
                );
            const businessResponse =
                responses.find(function(item) {
                    return item.reviewId == reviewId;
                });
            if (!businessResponse) {
                responseBox.innerHTML = `
                    <p>
                        No business response yet.
                    </p>
                `;
                return;
            }
            responseBox.innerHTML = `
                <div>
                    <strong>
                        Business Response:
                    </strong>
                    <p>
                        ${businessResponse.response}
                    </p>
                </div>
            `;
        })
        .catch(function(error) {
            console.error(error);
            const responseBox =
                document.getElementById(
                    "response-" + reviewId
                );
            if (responseBox) {
                responseBox.innerHTML = "";
            }
        });
}
function openBusinessResponse(reviewId, businessId) {
    window.location.href ="business-response.html?reviewId="+ reviewId+ "&businessId="+ businessId;
}

function editReview(reviewId) {
    const newRating =prompt("Enter new rating (1-5):");
    if (newRating === null) {
        return;
    }
    const rating =
        Number(newRating);
    if (isNaN(rating) || rating < 1 || rating > 5) {
        alert(
            "Rating must be between 1 and 5."
        );
        return;
    }
    const newComment =   prompt("Enter new comment:");
    if (newComment === null) {
        return;
    }
    const updatedReview = {
        rating: rating,
        comment: newComment
    };
    fetch(REVIEW_API + "/" + reviewId, {
        method: "PUT",
        headers: {
            "Content-Type":
                "application/json"
        },
        body:
            JSON.stringify(updatedReview)
    })
        .then(function(response) {
            if (!response.ok) {
                throw new Error(
                    "Review could not be updated"
                );
            }
            return response.json();
        })
        .then(function(data) {
            alert("Review updated successfully!");
            loadReviews();
        })
        .catch(function(error) {
            console.error(error);
            alert("Review could not be updated.");
        });
}
function deleteReview(reviewId) {
    const confirmDelete =confirm("Are you sure you want to delete this review?");
    if (!confirmDelete) {
        return;
    }
    fetch(REVIEW_API + "/" + reviewId, {
        method: "DELETE"
    })
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Review could not be deleted");
            }
            alert( "Review deleted successfully!");
            loadReviews();
        })
        .catch(function(error) {
            console.error(error);
            alert("Review could not be deleted.");
        });
}
function showReportForm(reviewId) {
    const form = document.getElementById(
            "reportForm-" + reviewId
        );
    if (form.style.display === "none") {
        form.style.display = "block";
    } else {
        form.style.display = "none";
    }
}
function submitReport(reviewId) {
    const reason =
        document.getElementById(
            "reportReason-" + reviewId
        ).value;
    const message =
        document.getElementById(
            "reportMessage-" + reviewId
        );
    if (reason === "") {
        message.innerHTML = `
            <span class="error-message">
                Please select a reason.
            </span>
        `;
        return;
    }
    const report = {
        userId: 1,
        reviewId: reviewId,
        reason: reason
    };
    fetch(REPORT_API, {
        method: "POST",
        headers: {
            "Content-Type":
                "application/json"
        },
        body:
            JSON.stringify(report)
    })
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Report could not be submitted");
            }
            return response.json();
        })
        .then(function(data) {
            message.innerHTML = `
                <span class="success-message">
                    Review reported successfully!
                </span>
            `;
            document.getElementById(
                "reportReason-" + reviewId
            ).value = "";
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
document
    .getElementById("reviewForm")
    .addEventListener(
        "submit",
        function(event) {
            event.preventDefault();
            const rating =
                document.getElementById(
                    "rating"
                ).value;
            const comment =
                document.getElementById(
                    "comment"
                ).value.trim();
            const message =
                document.getElementById(
                    "message"
                );
            if (!websiteId) {
                message.innerHTML = `
                    <span class="error-message">
                        Website ID not found.
                    </span>
                `;
                return;
            }
            const review = {
                rating: Number(rating),
                comment: comment,
                userId: 1,
                websiteId: Number(websiteId)
            };
            fetch(REVIEW_API, {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json"
                },
                body:
                    JSON.stringify(review)
            })
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error(
                            "Review could not be added"
                        );
                    }
                    return response.json();
                })
                .then(function(data) {
                    message.innerHTML = `
                        <span class="success-message">
                            Review added successfully!
                        </span>
                    `;
                    document
                        .getElementById("reviewForm")
                        .reset();
                    loadReviews();
                })
                .catch(function(error) {
                    console.error(error);
                    message.innerHTML = `
                        <span class="error-message">
                            Review could not be added.
                        </span>
                    `;
                });
        }
    );
document.addEventListener(
    "DOMContentLoaded",
    function() {
        loadWebsiteInfo();
        loadReviews();
    }
);