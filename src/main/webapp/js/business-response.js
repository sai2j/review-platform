const RESPONSE_API = "/review-platform/business-responses";
const REVIEW_API = "/review-platform/reviews";
const urlParams = new URLSearchParams(window.location.search);
const reviewId = urlParams.get("reviewId");
const businessId = urlParams.get("businessId");

function loadReview() {
    if (!reviewId) {
        document.getElementById("reviewText").innerHTML = "Review ID not found.";
        return;
    }
    fetch(REVIEW_API + "/" + reviewId)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Review not found");
            }
            return response.json();
        })
        .then(function(review) {
            document.getElementById("reviewText").innerHTML =
                review.comment || "No comment";
            document.getElementById("reviewRating").innerHTML =
                "Rating: " + review.rating + "/5 ⭐";
        })
        .catch(function(error) {
            console.error(error);
            document.getElementById("reviewText").innerHTML =
                "Unable to load review.";
        });
}
document
    .getElementById("responseForm").addEventListener("submit", function(event) {
        event.preventDefault();
        const responseText =
            document.getElementById("response").value.trim();
        const message =
            document.getElementById("message");
        if (!reviewId || !businessId) {
            message.innerHTML =
                '<span class="error-message">' +
                'Review ID or Business ID not found.' +
                '</span>';

            return;
        }
        if (responseText === "") {
            message.innerHTML =
                '<span class="error-message">' +
                'Please write a response.' +
                '</span>';
            return;
        }
        const businessResponse = {
            businessId: Number(businessId),
            reviewId: Number(reviewId),
            response: responseText
        };
        fetch(RESPONSE_API, {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/json"
            },
            body: JSON.stringify(businessResponse)
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error(
                        "Response could not be submitted"
                    );
                }
                return response.json();
            })
            .then(function(data) {
                console.log(
                    "Business Response:",
                    data
                );
                message.innerHTML =
                    '<span class="success-message">' + 'Response submitted successfully!' + '</span>';
                document
                    .getElementById("responseForm")
                    .reset();
            })
            .catch(function(error) {
                console.error(error);
                message.innerHTML =
                    '<span class="error-message">' + 'Response could not be submitted.' + '</span>';
            });
    });
document.addEventListener(
    "DOMContentLoaded",
    function() {
        loadReview();
    }
);