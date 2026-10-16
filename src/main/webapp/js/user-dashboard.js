/* =========================
   LOGGED-IN USER
========================= */

let loggedInUser = null;

try {
    loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
} catch (error) {
    loggedInUser = null;
}

const USER_ID = loggedInUser && loggedInUser.id ? loggedInUser.id : null;

if (!USER_ID) {
    alert("Please login first.");
    window.location.href = "login.html";
}


/* =========================
   API URLs
========================= */

const CLAIM_API = "/review-platform/business-claims";
const BUSINESS_API = "/review-platform/businesses";


/* =========================
   PAGE ELEMENTS
========================= */

const userEmail = document.getElementById("userEmail");
const userAvatar = document.getElementById("userAvatar");
const businessMessage = document.getElementById("businessMessage");
const businessEmpty = document.getElementById("businessEmpty");
const businessInfo = document.getElementById("businessInfo");
const verificationSection = document.getElementById("verificationSection");
const dashboardMessage = document.getElementById("dashboardMessage");

const statBusinesses = document.getElementById("statBusinesses");
const statVerification = document.getElementById("statVerification");
const statStatus = document.getElementById("statStatus");


/* =========================
   SHOW USER
========================= */

if (userEmail) {
    userEmail.textContent =
        loggedInUser && loggedInUser.email ? loggedInUser.email : "User";
}

if (userAvatar) {
    const source =
        loggedInUser && loggedInUser.email ? loggedInUser.email : "User";
    userAvatar.textContent = source.charAt(0).toUpperCase();
}


/* =========================
   SMALL HELPERS
========================= */

function showDashboardError(text) {
    if (dashboardMessage) {
        dashboardMessage.innerHTML =
            "<span class='error-message'>" + escapeHtml(text) + "</span>";
    }
}

function showDashboardSuccess(text) {
    if (dashboardMessage) {
        dashboardMessage.innerHTML =
            "<span class='success-message'>" + escapeHtml(text) + "</span>";
    }
}

function scrollToElement(id) {
    const element = document.getElementById(id);

    if (element) {
        element.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }
}

function getStoredBusiness() {
    const storedBusiness = localStorage.getItem("myBusiness");

    if (!storedBusiness) {
        showDashboardError("Business information is not available.");
        return null;
    }

    let business;

    try {
        business = JSON.parse(storedBusiness);
    } catch (error) {
        console.error(error);
        showDashboardError("Unable to read business information.");
        return null;
    }

    if (!business || !business.id) {
        showDashboardError("Business information is not available.");
        return null;
    }

    return business;
}


/* =========================
   NAVIGATION
========================= */

function goToHome() {
    window.location.href = "index.html";
}

function goToWriteReview() {
    window.location.href = "review.html";
}

function goToWebsites() {
    window.location.href = "website.html";
}

function openMyProfile() {
    window.location.href = "user-profile.html";
}

function scrollToBusiness() {
    scrollToElement("myBusinessSection");
}

function scrollToVerification() {
    scrollToElement("verificationSection");
}

function scrollToClaim() {
    scrollToElement("claimBusinessSection");
}


/* =========================
   SIDEBAR ACTIVE LINK
   (only for links that scroll on this page)
========================= */

function setupSidebarActiveState() {
    const scrollLinks = document.querySelectorAll(".side-link[data-scroll]");

    scrollLinks.forEach(function(link) {
        link.addEventListener("click", function() {
            scrollLinks.forEach(function(other) {
                other.classList.remove("active");
            });

            link.classList.add("active");
        });
    });
}


/* =========================
   STAT CARDS
========================= */

function updateStats(business) {
    if (!business) {
        if (statBusinesses) {
            statBusinesses.textContent = "0";
        }

        if (statVerification) {
            statVerification.textContent = "0/3";
        }

        if (statStatus) {
            statStatus.textContent = "None";
        }

        return;
    }

    const done =
        (business.emailVerified === true ? 1 : 0) +
        (business.metaVerified === true ? 1 : 0) +
        (business.dnsVerified === true ? 1 : 0);

    if (statBusinesses) {
        statBusinesses.textContent = "1";
    }

    if (statVerification) {
        statVerification.textContent = done + "/3";
    }

    if (statStatus) {
        statStatus.textContent = String(business.status || "PENDING")
            .toLowerCase();
    }
}


/* =========================
   LOAD APPROVED BUSINESS
========================= */

function showNoBusiness() {
    if (businessMessage) {
        businessMessage.innerHTML = "";
    }

    if (businessEmpty) {
        businessEmpty.style.display = "flex";
    }

    if (businessInfo) {
        businessInfo.style.display = "none";
    }

    if (verificationSection) {
        verificationSection.style.display = "none";
    }

    updateStats(null);
}

function readClaimResponse(response) {
    if (!response.ok) {
        return null;
    }

    return response.text().then(function(text) {
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return null;
        }
    });
}

function loadMyBusiness() {
    fetch(CLAIM_API + "/user/" + USER_ID + "/approved")
        .then(readClaimResponse)
        .then(function(claim) {
            if (claim && claim.businessId) {
                loadBusiness(claim.businessId);
                return;
            }

            // No approved claim: check for a pending ownership request.
            loadPendingBusiness();
        })
        .catch(function(error) {
            console.error(error);
            showNoBusiness();
        });
}


/* =========================
   LOAD PENDING BUSINESS
   (ownership request waiting for admin approval)
========================= */

function loadPendingBusiness() {
    fetch(CLAIM_API + "/user/" + USER_ID + "/pending")
        .then(readClaimResponse)
        .then(function(claim) {
            if (!claim || !claim.businessId) {
                showNoBusiness();
                return;
            }

            return fetch(BUSINESS_API + "/" + claim.businessId)
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error("Business could not be loaded.");
                    }

                    return response.json();
                })
                .then(function(business) {
                    displayPendingBusiness(business);
                });
        })
        .catch(function(error) {
            console.error(error);
            showNoBusiness();
        });
}


/* =========================
   LOAD BUSINESS
========================= */

function loadBusiness(businessId) {
    fetch(BUSINESS_API + "/" + businessId)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Business could not be loaded.");
            }

            return response.json();
        })
        .then(function(business) {
            if (!business) {
                throw new Error("Business not found.");
            }

            displayBusiness(business);
        })
        .catch(function(error) {
            console.error(error);

            if (businessMessage) {
                businessMessage.innerHTML =
                    "<p>Unable to load business information.</p>";
            }
        });
}


/* =========================
   DISPLAY BUSINESS
========================= */

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function displayPendingBusiness(business) {
    if (!business) {
        showNoBusiness();
        return;
    }

    if (businessMessage) {
        businessMessage.innerHTML = "";
    }

    if (businessEmpty) {
        businessEmpty.style.display = "none";
    }

    if (businessInfo) {
        businessInfo.style.display = "block";

        const actions = businessInfo.querySelector(".dashboard-actions");

        if (actions) {
            actions.style.display = "none";
        }
    }

    // Verification and dashboard unlock only after admin approval.
    if (verificationSection) {
        verificationSection.style.display = "none";
    }

    setText("businessId", business.id || "N/A");
    setText("businessName", business.name || "N/A");
    setText("officialUrl", business.officialUrl || "N/A");

    const businessStatus = document.getElementById("businessStatus");

    if (businessStatus) {
        businessStatus.textContent = "PENDING APPROVAL";
        updateBusinessStatusClass(businessStatus, "PENDING");
    }

    if (statBusinesses) {
        statBusinesses.textContent = "0";
    }

    if (statVerification) {
        statVerification.textContent = "0/3";
    }

    if (statStatus) {
        statStatus.textContent = "pending";
    }

    showDashboardSuccess(
        "Your business profile is created. Your ownership request is waiting for admin approval."
    );
}

function displayBusiness(business) {
    if (businessMessage) {
        businessMessage.innerHTML = "";
    }

    if (businessEmpty) {
        businessEmpty.style.display = "none";
    }

    if (businessInfo) {
        businessInfo.style.display = "block";

        const actions = businessInfo.querySelector(".dashboard-actions");

        if (actions) {
            actions.style.display = "";
        }
    }

    if (verificationSection) {
        verificationSection.style.display = "block";
    }


    /* BUSINESS INFORMATION */

    setText("businessId", business.id || "N/A");
    setText("businessName", business.name || "N/A");
    setText("officialUrl", business.officialUrl || "N/A");

    const businessStatus = document.getElementById("businessStatus");

    if (businessStatus) {
        businessStatus.textContent = business.status || "PENDING";
        updateBusinessStatusClass(businessStatus, business.status);
    }


    /* EMAIL VERIFICATION */

    const emailVerified = business.emailVerified === true;

    updateVerificationUI("emailVerificationStatus", "emailBadge", emailVerified);

    const emailForm = document.getElementById("emailVerificationForm");
    const businessEmail = document.getElementById("businessEmail");

    if (businessEmail && business.businessEmail) {
        businessEmail.value = business.businessEmail;
    }

    if (emailForm) {
        emailForm.style.display = emailVerified ? "none" : "block";
    }


    /* META VERIFICATION */

    updateVerificationUI(
        "metaStatus",
        "metaBadge",
        business.metaVerified === true
    );


    /* DNS VERIFICATION */

    updateVerificationUI(
        "dnsStatus",
        "dnsBadge",
        business.dnsVerified === true
    );


    /* BUSINESS VERIFIED */

    const isVerified =
        String(business.status || "").toUpperCase() === "VERIFIED";

    const verifiedMessage = document.getElementById("verifiedMessage");

    if (verifiedMessage) {
        verifiedMessage.style.display = isVerified ? "block" : "none";
    }


    /* STATS + SAVE BUSINESS */

    updateStats(business);

    localStorage.setItem("myBusiness", JSON.stringify(business));
}


/* =========================
   VERIFICATION UI
========================= */

function updateVerificationUI(statusElementId, badgeElementId, verified) {
    const statusElement = document.getElementById(statusElementId);
    const badgeElement = document.getElementById(badgeElementId);

    if (!statusElement || !badgeElement) {
        return;
    }

    if (verified) {
        statusElement.textContent = "Verified";
        badgeElement.textContent = "VERIFIED";
        badgeElement.className = "badge verified";
    } else {
        statusElement.textContent = "Not Verified";
        badgeElement.textContent = "NOT VERIFIED";
        badgeElement.className = "badge not-verified";
    }
}


/* =========================
   BUSINESS STATUS CLASS
========================= */

function updateBusinessStatusClass(element, status) {
    if (!element) {
        return;
    }

    element.className = "status";

    if (String(status || "").toUpperCase() === "VERIFIED") {
        element.classList.add("status-verified");
    } else {
        element.classList.add("status-pending");
    }
}


/* =========================
   SEND EMAIL VERIFICATION
========================= */

function sendEmailVerification() {
    const business = getStoredBusiness();

    if (!business) {
        return;
    }

    const emailElement = document.getElementById("businessEmail");

    if (!emailElement) {
        return;
    }

    const email = emailElement.value.trim();

    if (!email) {
        showDashboardError("Business email is required.");
        return;
    }

    if (dashboardMessage) {
        dashboardMessage.textContent = "Sending verification email...";
    }

    const url =
        BUSINESS_API +
        "/" +
        business.id +
        "/verify-email?email=" +
        encodeURIComponent(email);

    fetch(url, {
        method: "POST",
        credentials: "include"
    })
        .then(function(response) {
            if (!response.ok) {
                return response.text().then(function(errorText) {
                    throw new Error(
                        errorText || "Unable to send verification email."
                    );
                });
            }

            return response.text();
        })
        .then(function(responseMessage) {
            showDashboardSuccess(responseMessage);
        })
        .catch(function(error) {
            console.error(error);

            showDashboardError(
                error.message || "Unable to send verification email."
            );
        });
}


/* =========================
   OPEN BUSINESS DASHBOARD
========================= */

function openBusinessDashboard() {
    const business = getStoredBusiness();

    if (!business) {
        return;
    }

    const isVerified =
        String(business.status || "").toUpperCase() === "VERIFIED";

    if (!isVerified) {
        showDashboardError(
            "Business verification is required before opening the Business Dashboard."
        );

        scrollToVerification();

        return;
    }

    window.location.href = "business-dashboard.html";
}


/* =========================
   CLAIM BUSINESS
   Users claim from the Business Profile page.
========================= */

function claimBusiness() {
    showDashboardError(
        "Please open a business profile and use the Claim This Business button."
    );

    goToHome();
}


/* =========================
   LOGOUT
========================= */

function logoutUser() {
    localStorage.removeItem("loggedInUser");
    localStorage.removeItem("myBusiness");

    window.location.href = "login.html";
}


/* =========================
   ESCAPE HTML
========================= */

function escapeHtml(value) {
    if (value === null || value === undefined) {
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

document.addEventListener("DOMContentLoaded", function() {
    setupSidebarActiveState();

    if (USER_ID) {
        loadMyBusiness();
    }
});