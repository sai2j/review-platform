/* =========================
   LOGGED-IN USER
========================= */

const loggedInUser =
    JSON.parse(
        localStorage.getItem("loggedInUser")
    );

if (!loggedInUser || !loggedInUser.id) {

    alert("Please login first.");

    window.location.href =
        "login.html";

}


/* =========================
   USER ID
========================= */

const USER_ID =
    loggedInUser.id;


/* =========================
   API URLs
========================= */

const CLAIM_API =
    "/review-platform/business-claims";

const BUSINESS_API =
    "/review-platform/businesses";


/* =========================
   PAGE ELEMENTS
========================= */

const userInfo =
    document.getElementById(
        "userInfo"
    );


const businessMessage =
    document.getElementById(
        "businessMessage"
    );


const businessInfo =
    document.getElementById(
        "businessInfo"
    );


const verificationSection =
    document.getElementById(
        "verificationSection"
    );


const dashboardMessage =
    document.getElementById(
        "dashboardMessage"
    );


/* =========================
   SHOW USER
========================= */

if (userInfo) {

    if (loggedInUser.email) {

        userInfo.textContent =
            "Logged in as: " +
            loggedInUser.email;

    } else {

        userInfo.textContent =
            "Logged in as: User";

    }

}


/* =========================
   NAVIGATION - HOME
========================= */

function goToHome() {

    window.location.href =
        "index.html";

}


/* =========================
   NAVIGATION - WRITE REVIEW
========================= */

function goToWriteReview() {

    window.location.href =
        "review.html";

}


/* =========================
   NAVIGATION - WEBSITES
========================= */

function goToWebsites() {

    window.location.href =
        "website.html";

}


/* =========================
   SCROLL TO MY BUSINESS
========================= */

function scrollToBusiness() {

    const businessSection =
        document.getElementById(
            "myBusinessSection"
        );


    if (businessSection) {

        businessSection.scrollIntoView({

            behavior: "smooth",

            block: "start"

        });

    }

}


/* =========================
   SCROLL TO VERIFICATION
========================= */

function scrollToVerification() {

    const verification =
        document.getElementById(
            "verificationSection"
        );


    if (verification) {

        verification.scrollIntoView({

            behavior: "smooth",

            block: "start"

        });

    }

}


/* =========================
   SCROLL TO CLAIM BUSINESS
========================= */

function scrollToClaim() {

    const claimSection =
        document.getElementById(
            "claimBusinessSection"
        );


    if (claimSection) {

        claimSection.scrollIntoView({

            behavior: "smooth",

            block: "start"

        });

    }

}


/* =========================
   LOAD APPROVED BUSINESS
========================= */

function loadMyBusiness() {

    fetch(

        CLAIM_API +
        "/user/" +
        USER_ID +
        "/approved"

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "No approved business claim found."

                );

            }


            return response.json();

        })

        .then(function(claim) {

            if (
                !claim ||
                !claim.businessId
            ) {

                throw new Error(

                    "No approved business claim found."

                );

            }


            loadBusiness(

                claim.businessId

            );

        })

        .catch(function(error) {

            console.error(error);


            if (businessMessage) {

                businessMessage.innerHTML =

                    "<p>" +

                    "No approved business claim found." +

                    "</p>";

            }


            if (businessInfo) {

                businessInfo.style.display =
                    "none";

            }


            if (verificationSection) {

                verificationSection.style.display =
                    "none";

            }

        });

}


/* =========================
   LOAD BUSINESS
========================= */

function loadBusiness(businessId) {

    fetch(

        BUSINESS_API +
        "/" +
        businessId

    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(

                    "Business could not be loaded."

                );

            }


            return response.json();

        })

        .then(function(business) {

            if (!business) {

                throw new Error(

                    "Business not found."

                );

            }


            displayBusiness(

                business

            );

        })

        .catch(function(error) {

            console.error(error);


            if (businessMessage) {

                businessMessage.innerHTML =

                    "<p>" +

                    "Unable to load business information." +

                    "</p>";

            }

        });

}


/* =========================
   DISPLAY BUSINESS
========================= */

function displayBusiness(business) {

    if (businessMessage) {

        businessMessage.innerHTML = "";

    }


    if (businessInfo) {

        businessInfo.style.display =
            "block";

    }


    if (verificationSection) {

        verificationSection.style.display =
            "block";

    }


    /* =========================
       BUSINESS INFORMATION
    ========================== */

    const businessIdElement =
        document.getElementById(
            "businessId"
        );


    if (businessIdElement) {

        businessIdElement.textContent =
            business.id || "N/A";

    }


    const businessNameElement =
        document.getElementById(
            "businessName"
        );


    if (businessNameElement) {

        businessNameElement.textContent =
            business.name || "N/A";

    }


    const officialUrlElement =
        document.getElementById(
            "officialUrl"
        );


    if (officialUrlElement) {

        officialUrlElement.textContent =
            business.officialUrl || "N/A";

    }


    const businessStatus =
        document.getElementById(
            "businessStatus"
        );


    if (businessStatus) {

        businessStatus.textContent =
            business.status || "PENDING";


        updateBusinessStatusClass(

            businessStatus,

            business.status

        );

    }


    /* =========================
       EMAIL VERIFICATION
    ========================== */

    const emailVerified =
        business.emailVerified === true;


    updateVerificationUI(

        "emailVerificationStatus",

        "emailBadge",

        emailVerified

    );


    const emailForm =
        document.getElementById(
            "emailVerificationForm"
        );


    const businessEmail =
        document.getElementById(
            "businessEmail"
        );


    if (
        businessEmail &&
        business.businessEmail
    ) {

        businessEmail.value =
            business.businessEmail;

    }


    if (emailForm) {

        if (emailVerified) {

            emailForm.style.display =
                "none";

        } else {

            emailForm.style.display =
                "block";

        }

    }


    /* =========================
       META VERIFICATION
    ========================== */

    const metaVerified =
        business.metaVerified === true;


    updateVerificationUI(

        "metaStatus",

        "metaBadge",

        metaVerified

    );


    /* =========================
       DNS VERIFICATION
    ========================== */

    const dnsVerified =
        business.dnsVerified === true;


    updateVerificationUI(

        "dnsStatus",

        "dnsBadge",

        dnsVerified

    );


    /* =========================
       BUSINESS VERIFIED
    ========================== */

    const isVerified =
        String(

            business.status || ""

        ).toUpperCase() ===

        "VERIFIED";


    const verifiedMessage =
        document.getElementById(
            "verifiedMessage"
        );


    if (verifiedMessage) {

        if (isVerified) {

            verifiedMessage.style.display =
                "block";

        } else {

            verifiedMessage.style.display =
                "none";

        }

    }


    /* =========================
       SAVE BUSINESS
    ========================== */

    localStorage.setItem(

        "myBusiness",

        JSON.stringify(business)

    );

}


/* =========================
   VERIFICATION UI
========================= */

function updateVerificationUI(

    statusElementId,

    badgeElementId,

    verified

) {

    const statusElement =
        document.getElementById(
            statusElementId
        );


    const badgeElement =
        document.getElementById(
            badgeElementId
        );


    if (!statusElement || !badgeElement) {

        return;

    }


    if (verified) {

        statusElement.textContent =
            "Verified";


        badgeElement.textContent =
            "VERIFIED";


        badgeElement.className =
            "badge verified";

    } else {

        statusElement.textContent =
            "Not Verified";


        badgeElement.textContent =
            "NOT VERIFIED";


        badgeElement.className =
            "badge not-verified";

    }

}


/* =========================
   BUSINESS STATUS CLASS
========================= */

function updateBusinessStatusClass(

    element,

    status

) {

    if (!element) {

        return;

    }


    element.className =
        "status";


    if (

        String(status || "")

            .toUpperCase() ===

        "VERIFIED"

    ) {

        element.classList.add(

            "status-verified"

        );

    } else {

        element.classList.add(

            "status-pending"

        );

    }

}


/* =========================
   SEND EMAIL VERIFICATION
========================= */

function sendEmailVerification() {

    const storedBusiness =
        localStorage.getItem(
            "myBusiness"
        );


    if (!storedBusiness) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business information is not available." +

                "</span>";

        }

        return;

    }


    let business;


    try {

        business =
            JSON.parse(
                storedBusiness
            );

    } catch (error) {

        console.error(error);


        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Unable to read business information." +

                "</span>";

        }

        return;

    }


    if (
        !business ||
        !business.id
    ) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business information is not available." +

                "</span>";

        }

        return;

    }


    const emailElement =
        document.getElementById(
            "businessEmail"
        );


    if (!emailElement) {

        return;

    }


    const email =
        emailElement.value.trim();


    if (!email) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business email is required." +

                "</span>";

        }

        return;

    }


    if (dashboardMessage) {

        dashboardMessage.textContent =
            "Sending verification email...";

    }


    const url =
        BUSINESS_API +
        "/" +
        business.id +
        "/verify-email?email=" +
        encodeURIComponent(email);


    fetch(

        url,

        {

            method: "POST",

            credentials: "include"

        }

    )

        .then(function(response) {

            if (!response.ok) {

                return response.text()

                    .then(function(errorText) {

                        throw new Error(

                            errorText ||

                            "Unable to send verification email."

                        );

                    });

            }


            return response.text();

        })

        .then(function(responseMessage) {

            if (dashboardMessage) {

                dashboardMessage.innerHTML =

                    "<span class='success-message'>" +

                    escapeHtml(

                        responseMessage

                    ) +

                    "</span>";

            }

        })

        .catch(function(error) {

            console.error(error);


            if (dashboardMessage) {

                dashboardMessage.innerHTML =

                    "<span class='error-message'>" +

                    escapeHtml(

                        error.message ||

                        "Unable to send verification email."

                    ) +

                    "</span>";

            }

        });

}


/* =========================
   OPEN BUSINESS DASHBOARD
========================= */

function openBusinessDashboard() {

    const storedBusiness =
        localStorage.getItem(
            "myBusiness"
        );


    if (!storedBusiness) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business information is not available." +

                "</span>";

        }

        return;

    }


    let business;


    try {

        business =
            JSON.parse(
                storedBusiness
            );

    } catch (error) {

        console.error(error);


        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Unable to read business information." +

                "</span>";

        }

        return;

    }


    if (
        !business ||
        !business.id
    ) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business information is not available." +

                "</span>";

        }

        return;

    }


    const isVerified =
        String(

            business.status || ""

        ).toUpperCase() ===

        "VERIFIED";


    if (!isVerified) {

        if (dashboardMessage) {

            dashboardMessage.innerHTML =

                "<span class='error-message'>" +

                "Business verification is required before opening the Business Dashboard." +

                "</span>";

        }


        scrollToVerification();

        return;

    }


    window.location.href =
        "business-dashboard.html";

}


/* =========================
   CLAIM BUSINESS
   Manual Business ID flow removed.
   Users should claim from
   Business Profile.
========================= */

function claimBusiness() {

    if (dashboardMessage) {

        dashboardMessage.innerHTML =

            "<span class='error-message'>" +

            "Please open a business profile and use the Claim This Business button." +

            "</span>";

    }


    goToHome();

}


/* =========================
   LOGOUT
========================= */

function logoutUser() {

    localStorage.removeItem(

        "loggedInUser"

    );


    localStorage.removeItem(

        "myBusiness"

    );


    window.location.href =
        "login.html";

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

        loadMyBusiness();

    }

);