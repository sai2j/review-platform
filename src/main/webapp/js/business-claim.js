const CLAIM_API = "/review-platform/business-claims";
const BUSINESS_API = "/review-platform/businesses";

const urlParams =
    new URLSearchParams(window.location.search);

const businessId =
    urlParams.get("businessId");

const businessIdInput =
    document.getElementById("businessId");

const loggedInUserInput =
    document.getElementById("loggedInUser");

const claimForm =
    document.getElementById("claimForm");

const message =
    document.getElementById("message");

const businessDetails =
    document.getElementById("businessDetails");


/* =========================================================
CHECK LOGGED-IN USER
========================================================= */

const loggedInUser =
    localStorage.getItem("loggedInUser");


if (!loggedInUser) {

    message.innerHTML =
        '<span class="error-message">' +
        'Please login before claiming a business.' +
        '</span>';

    claimForm.style.display = "none";

    if (businessDetails) {

        businessDetails.innerHTML = `
            <p class="error-message">
                Please login to continue.
            </p>

            <a href="login.html">
                Login to continue
            </a>
        `;

    }

} else {

    try {

        const user =
            JSON.parse(loggedInUser);


        if (user.email) {

            loggedInUserInput.value =
                user.email;

        } else if (user.name) {

            loggedInUserInput.value =
                user.name;

        } else {

            loggedInUserInput.value =
                "Logged-in user";

        }

    } catch (error) {

        console.error(
            "Unable to read logged-in user:",
            error
        );


        message.innerHTML =
            '<span class="error-message">' +
            'Unable to read logged-in user information.' +
            '</span>';

        claimForm.style.display = "none";

    }

}


/* =========================================================
SET BUSINESS ID FROM URL
========================================================= */

if (businessId) {

    businessIdInput.value =
        businessId;

} else {

    message.innerHTML =
        '<span class="error-message">' +
        'Business information is missing.' +
        '</span>';

    claimForm.style.display = "none";

}


/* =========================================================
LOAD BUSINESS DETAILS
========================================================= */

function loadBusinessDetails() {

    if (!businessId) {

        return;

    }


    if (!businessDetails) {

        return;

    }


    fetch(
        BUSINESS_API +
        "/" +
        businessId
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business could not be found."
                );

            }

            return response.json();

        })

        .then(function(business) {

            if (!business) {

                throw new Error(
                    "Business information is unavailable."
                );

            }


            const name =
                escapeHtml(
                    business.name ||
                    "Business"
                );


            const description =
                escapeHtml(
                    business.description ||
                    ""
                );


            const officialUrl =
                escapeHtml(
                    business.officialUrl ||
                    ""
                );


            const status =
                escapeHtml(
                    business.status ||
                    "PENDING"
                );


            businessDetails.innerHTML = `

                <div class="business-detail-item">

                    <strong>
                        Business Name
                    </strong>

                    <span>
                        ${name}
                    </span>

                </div>


                <div class="business-detail-item">

                    <strong>
                        Website
                    </strong>

                    <span>
                        ${
                            officialUrl
                                ? `<a
                                    href="${escapeAttribute(business.officialUrl)}"
                                    target="_blank"
                                    rel="noopener noreferrer">
                                    ${officialUrl}
                                  </a>`
                                : "Not available"
                        }
                    </span>

                </div>


                <div class="business-detail-item">

                    <strong>
                        Description
                    </strong>

                    <span>
                        ${description || "No description available."}
                    </span>

                </div>


                <div class="business-detail-item">

                    <strong>
                        Business Status
                    </strong>

                    <span>
                        ${status}
                    </span>

                </div>

            `;

        })

        .catch(function(error) {

            console.error(
                "Unable to load business details:",
                error
            );


            businessDetails.innerHTML = `

                <p class="error-message">

                    Unable to load business information.

                </p>

            `;

        });

}


/* =========================================================
SUBMIT BUSINESS CLAIM
========================================================= */

if (claimForm) {

    claimForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();


            const selectedBusinessId =
                businessIdInput.value;


            if (!selectedBusinessId) {

                message.innerHTML =
                    '<span class="error-message">' +
                    'Business information is required.' +
                    '</span>';

                return;

            }


            const currentUser =
                localStorage.getItem(
                    "loggedInUser"
                );


            if (!currentUser) {

                message.innerHTML =
                    '<span class="error-message">' +
                    'Please login before claiming a business.' +
                    '</span>';

                return;

            }


            /*
             * User ID is intentionally NOT sent
             * from the frontend.
             *
             * Backend identifies the logged-in user
             * from the authenticated session.
             */

            const claim = {

                businessId:
                    Number(selectedBusinessId),

                status:
                    "PENDING"

            };


            const submitButton =
                claimForm.querySelector(
                    'button[type="submit"]'
                );


            if (submitButton) {

                submitButton.disabled =
                    true;

                submitButton.textContent =
                    "Submitting...";

            }


            message.innerHTML =
                '<span>' +
                'Submitting your business claim...' +
                '</span>';


            fetch(
                CLAIM_API,
                {

                    method: "POST",

                    credentials: "include",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify(claim)

                }
            )

                .then(function(response) {

                    if (!response.ok) {

                        return response.text()

                            .then(function(errorText) {

                                throw new Error(
                                    errorText ||
                                    "Business claim could not be submitted."
                                );

                            });

                    }


                    return response.json();

                })


                .then(function(data) {

                    console.log(
                        "Business Claim:",
                        data
                    );


                    message.innerHTML = `

                        <div class="success-message">

                            <strong>
                                Business claim submitted successfully!
                            </strong>

                            <p>
                                Your claim is now pending admin approval.
                            </p>

                            <div class="claim-success-actions">

                                <a
                                    href="user-dashboard.html"
                                    class="dashboard-button">

                                    Go to My Dashboard

                                </a>


                                <a
                                    href="review.html?websiteId=${getWebsiteIdFromBusinessUrl(data)}"
                                    class="profile-button">

                                    View Business Profile

                                </a>

                            </div>

                        </div>

                    `;


                    if (submitButton) {

                        submitButton.disabled =
                            true;

                        submitButton.textContent =
                            "Claim Submitted";

                    }

                })


                .catch(function(error) {

                    console.error(
                        "Business claim error:",
                        error
                    );


                    message.innerHTML = `

                        <span class="error-message">

                            ${
                                error.message ||
                                "Business claim could not be submitted."
                            }

                        </span>

                    `;


                    if (submitButton) {

                        submitButton.disabled =
                            false;

                        submitButton.textContent =
                            "Claim This Business";

                    }

                });

        }
    );

}


/* =========================================================
GET WEBSITE ID FOR PROFILE BUTTON
========================================================= */

function getWebsiteIdFromBusinessUrl(business) {

    /*
     * The claim API response may not contain websiteId.
     *
     * Return a safe fallback so the profile button
     * can still use the current page flow when available.
     */

    if (
        business &&
        business.websiteId
    ) {

        return encodeURIComponent(
            business.websiteId
        );

    }


    return "";

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
PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadBusinessDetails();

    }
);