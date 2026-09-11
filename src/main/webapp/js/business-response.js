/* =========================
   API URLs
========================= */

const REVIEW_API =
    "/review-platform/reviews";

const RESPONSE_API =
    "/review-platform/business-responses";


/* =========================
   GET REVIEW ID
========================= */

const urlParams =
    new URLSearchParams(window.location.search);

const reviewId =
    urlParams.get("reviewId");


/* =========================
   GET LOGGED-IN USER
========================= */

const loggedInUser =
    JSON.parse(localStorage.getItem("loggedInUser"));


let currentReview = null;

let currentBusiness = null;


/* =========================
   LOAD REVIEW
========================= */

function loadReview() {

    if (!reviewId) {

        document.getElementById(
            "reviewText"
        ).textContent =
            "Review ID is missing.";

        document.getElementById(
            "reviewRating"
        ).textContent =
            "Rating: Not available";

        return;

    }


    fetch(
        REVIEW_API + "/" + reviewId
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Review API Error"
                );

            }

            return response.json();

        })

        .then(function(review) {

            currentReview = review;


            document.getElementById(
                "reviewText"
            ).textContent =
                review.comment || "No comment";


            document.getElementById(
                "reviewRating"
            ).textContent =
                "Rating: "
                + review.rating
                + "/5";

        })

        .catch(function(error) {

            console.error(error);

            document.getElementById(
                "reviewText"
            ).textContent =
                "Unable to load review.";

            document.getElementById(
                "reviewRating"
            ).textContent =
                "Rating: Not available";

        });

}


/* =========================
   LOAD BUSINESS FOR USER
========================= */

function loadBusiness() {

    if (!loggedInUser || !loggedInUser.id) {

        document.getElementById(
            "message"
        ).textContent =
            "Please login first.";

        return Promise.reject(
            new Error("User is not logged in")
        );

    }


    const businessAPI =
        "/review-platform/business-dashboard/user/"
        + loggedInUser.id;


    return fetch(businessAPI)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business API Error"
                );

            }

            return response.json();

        })

        .then(function(business) {

            if (!business || !business.id) {

                throw new Error(
                    "No approved business found"
                );

            }

            currentBusiness = business;

            return business;

        })

        .catch(function(error) {

            console.error(error);

            document.getElementById(
                "message"
            ).textContent =
                "Unable to find your business.";

            throw error;

        });

}


/* =========================
   SUBMIT BUSINESS RESPONSE
========================= */

const responseForm =
    document.getElementById(
        "responseForm"
    );


if (responseForm) {

    responseForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();


            if (!currentReview) {

                document.getElementById(
                    "message"
                ).textContent =
                    "Review information is not available.";

                return;

            }


            if (!currentBusiness) {

                document.getElementById(
                    "message"
                ).textContent =
                    "Business information is not available.";

                return;

            }


            const responseElement =
                document.getElementById(
                    "response"
                );


            const messageElement =
                document.getElementById(
                    "message"
                );


            const responseText =
                responseElement.value.trim();


            if (responseText === "") {

                messageElement.textContent =
                    "Please write a response.";

                return;

            }


            const businessResponse = {

                businessId:
                    currentBusiness.id,

                reviewId:
                    currentReview.id,

                response:
                    responseText

            };


            fetch(
                RESPONSE_API,
                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify(
                            businessResponse
                        )

                }
            )

                .then(function(response) {

                    if (!response.ok) {

                        return response.text()
                            .then(function(errorText) {

                                let errorMessage =
                                    "Response submission failed";

                                try {

                                    const error =
                                        JSON.parse(
                                            errorText
                                        );

                                    if (error.error) {

                                        errorMessage =
                                            error.error;

                                    }

                                } catch (e) {

                                    if (errorText) {

                                        errorMessage =
                                            errorText;

                                    }

                                }

                                throw new Error(
                                    errorMessage
                                );

                            });

                    }

                    return response.json();

                })

                .then(function(data) {

                    messageElement.textContent =
                        "Response submitted successfully!";

                    responseElement.value = "";

                })

                .catch(function(error) {

                    console.error(error);

                    messageElement.textContent =
                        error.message
                        || "Unable to submit response.";

                });

        }
    );

}


/* =========================
   PAGE LOAD
========================= */

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadReview();

        loadBusiness();

    }
);