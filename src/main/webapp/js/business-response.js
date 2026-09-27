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

let currentResponse = null;


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

        return Promise.reject(
            new Error("Review ID is missing")
        );
    }


    return fetch(
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

            throw error;
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
   LOAD EXISTING RESPONSE
========================= */

function loadExistingResponse() {

    if (!currentBusiness || !currentReview) {

        return;
    }


    return fetch(RESPONSE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business Response API Error"
                );
            }

            return response.json();

        })

        .then(function(responses) {

            const existingResponse =
                responses.find(function(item) {

                    return Number(item.businessId)
                        === Number(currentBusiness.id)
                        &&
                        Number(item.reviewId)
                        === Number(currentReview.id);

                });


            if (existingResponse) {

                currentResponse =
                    existingResponse;


                const responseElement =
                    document.getElementById(
                        "response"
                    );


                if (responseElement) {

                    responseElement.value =
                        existingResponse.response
                        || "";

                }


                const submitButton =
                    document.querySelector(
                        "#responseForm button[type='submit']"
                    );


                if (submitButton) {

                    submitButton.textContent =
                        "Update Response";

                }

            }

        })

        .catch(function(error) {

            console.error(error);

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


                                    if (error.message) {

                                        errorMessage =
                                            error.message;

                                    } else if (error.error) {

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

                    if (currentResponse) {

                        messageElement.textContent =
                            "Response updated successfully!";

                    } else {

                        messageElement.textContent =
                            "Response submitted successfully!";

                    }


                    currentResponse = data;


                    responseElement.value =
                        data.response || responseText;


                    const submitButton =
                        document.querySelector(
                            "#responseForm button[type='submit']"
                        );


                    if (submitButton) {

                        submitButton.textContent =
                            "Update Response";

                    }

                })

                .catch(function(error) {

                    console.error(error);

                    messageElement.textContent =
                        error.message
                        ||
                        "Unable to submit response.";

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

        loadReview()

            .then(function() {

                return loadBusiness();

            })

            .then(function() {

                return loadExistingResponse();

            });

    }
);