const REPORT_API = "/review-platform/reports";

const REVIEW_API = "/review-platform/reviews";

const CLAIM_API = "/review-platform/business-claims";

const USER_API = "/review-platform/users";

const WEBSITE_API = "/review-platform/websites";

const AUDIT_LOG_API = "/review-platform/audit-logs";


// ==============================
// ADMIN DASHBOARD METRICS
// ==============================

function loadDashboardMetrics() {

    Promise.all([
        fetch(USER_API),
        fetch(WEBSITE_API),
        fetch(REVIEW_API)
    ])

        .then(function(responses) {

            responses.forEach(function(response) {

                if (!response.ok) {
                    throw new Error("Dashboard API Error");
                }

            });

            return Promise.all(
                responses.map(function(response) {
                    return response.json();
                })
            );

        })

        .then(function(data) {

            const users = data[0];

            const websites = data[1];

            const reviews = data[2];

            const pendingReviews = reviews.filter(function(review) {

                return (review.status || "PENDING")
                    .toUpperCase() === "PENDING";

            });

            document.getElementById("totalUsers").textContent =
                users.length;

            document.getElementById("totalWebsites").textContent =
                websites.length;

            document.getElementById("totalReviews").textContent =
                reviews.length;

            document.getElementById("pendingReviews").textContent =
                pendingReviews.length;

        })

        .catch(function(error) {

            console.error(error);

            const totalUsers =
                document.getElementById("totalUsers");

            const totalWebsites =
                document.getElementById("totalWebsites");

            const totalReviews =
                document.getElementById("totalReviews");

            const pendingReviews =
                document.getElementById("pendingReviews");

            if (totalUsers) {
                totalUsers.textContent = "Error";
            }

            if (totalWebsites) {
                totalWebsites.textContent = "Error";
            }

            if (totalReviews) {
                totalReviews.textContent = "Error";
            }

            if (pendingReviews) {
                pendingReviews.textContent = "Error";
            }

        });

}


// ==============================
// LOAD REVIEW MODERATION QUEUE
// ==============================

function loadModerationQueue() {

    fetch(REVIEW_API + "/pending")

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Pending Review API Error");
            }

            return response.json();

        })

        .then(function(reviews) {

            displayModerationQueue(reviews);

        })

        .catch(function(error) {

            console.error(error);

            const moderationList =
                document.getElementById("moderationList");

            if (moderationList) {

                moderationList.innerHTML =
                    `<p class="error-message">
                        Unable to load pending reviews.
                    </p>`;

            }

        });

}


// ==============================
// DISPLAY REVIEW MODERATION QUEUE
// ==============================

function displayModerationQueue(reviews) {

    const moderationList =
        document.getElementById("moderationList");

    if (!moderationList) {
        return;
    }

    moderationList.innerHTML = "";

    if (reviews.length === 0) {

        moderationList.innerHTML =
            `<p>No pending reviews.</p>`;

        return;
    }

    reviews.forEach(function(review) {

        moderationList.innerHTML += `

            <div class="report-card">

                <p>
                    <strong>Review ID:</strong>
                    ${review.id}
                </p>

                <p>
                    <strong>User ID:</strong>
                    ${review.userId}
                </p>

                <p>
                    <strong>Website ID:</strong>
                    ${review.websiteId}
                </p>

                <p>
                    <strong>Rating:</strong>
                    ${review.rating}
                </p>

                <p>
                    <strong>Comment:</strong>
                    ${escapeAdminHtml(review.comment || "")}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${escapeAdminHtml(review.status || "PENDING")}
                </p>

                <button
                    onclick="updateReviewStatus(${review.id}, 'APPROVED')">
                    Approve
                </button>

                <button
                    onclick="updateReviewStatus(${review.id}, 'REJECTED')">
                    Reject
                </button>

                <button
                    onclick="updateReviewStatus(${review.id}, 'HIDDEN')">
                    Hide
                </button>

            </div>

        `;

    });

}


// ==============================
// LOAD HIDDEN REVIEWS
// ==============================

function loadHiddenReviews() {

    fetch(REVIEW_API)

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Review API Error");
            }

            return response.json();

        })

        .then(function(reviews) {

            const hiddenReviews = reviews.filter(function(review) {

                return (review.status || "")
                    .toUpperCase() === "HIDDEN";

            });

            displayHiddenReviews(hiddenReviews);

        })

        .catch(function(error) {

            console.error(error);

            const hiddenReviewList =
                document.getElementById("hiddenReviewList");

            if (hiddenReviewList) {

                hiddenReviewList.innerHTML =
                    `<p class="error-message">
                        Unable to load hidden reviews.
                    </p>`;

            }

        });

}


// ==============================
// DISPLAY HIDDEN REVIEWS
// ==============================

function displayHiddenReviews(reviews) {

    const hiddenReviewList =
        document.getElementById("hiddenReviewList");

    if (!hiddenReviewList) {
        return;
    }

    hiddenReviewList.innerHTML = "";

    if (reviews.length === 0) {

        hiddenReviewList.innerHTML =
            `<p>No hidden reviews.</p>`;

        return;
    }

    reviews.forEach(function(review) {

        hiddenReviewList.innerHTML += `

            <div class="report-card">

                <p>
                    <strong>Review ID:</strong>
                    ${review.id}
                </p>

                <p>
                    <strong>User ID:</strong>
                    ${review.userId}
                </p>

                <p>
                    <strong>Website ID:</strong>
                    ${review.websiteId}
                </p>

                <p>
                    <strong>Rating:</strong>
                    ${review.rating}
                </p>

                <p>
                    <strong>Comment:</strong>
                    ${escapeAdminHtml(review.comment || "")}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${escapeAdminHtml(review.status || "HIDDEN")}
                </p>

                <button
                    onclick="restoreReview(${review.id})">
                    Restore
                </button>

            </div>

        `;

    });

}


// ==============================
// RESTORE REVIEW
// ==============================

function restoreReview(reviewId) {

    if (!confirm("Restore this review?")) {
        return;
    }

    fetch(
        REVIEW_API +
        "/" +
        reviewId +
        "/status?status=APPROVED",
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Review restore failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert("Review restored successfully.");

            loadHiddenReviews();

            loadModerationQueue();

            loadDashboardMetrics();

            loadAuditLogs();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Review could not be restored."
            );

        });

}


// ==============================
// UPDATE REVIEW STATUS
// ==============================

function updateReviewStatus(reviewId, status) {

    let message = "";

    if (status === "APPROVED") {

        message = "Approve this review?";

    } else if (status === "REJECTED") {

        message = "Reject this review?";

    } else if (status === "HIDDEN") {

        message = "Hide this review?";

    } else if (status === "PENDING") {

        message = "Restore this review to pending?";

    }

    if (!confirm(message)) {
        return;
    }

    fetch(
        REVIEW_API +
        "/" +
        reviewId +
        "/status?status=" +
        status,
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Review status update failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert(
                "Review status updated to " + status
            );

            loadModerationQueue();

            loadHiddenReviews();

            loadDashboardMetrics();

            loadAuditLogs();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Review status could not be updated."
            );

        });

}


// ==============================
// LOAD REPORTS
// ==============================

function loadReports() {

    fetch(REPORT_API)

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Report API Error");
            }

            return response.json();

        })

        .then(function(reports) {

            displayReports(reports);

        })

        .catch(function(error) {

            console.error(error);

            document.getElementById("reportList").innerHTML =
                `<p class="error-message">
                    Unable to load reports.
                </p>`;

        });

}


// ==============================
// DISPLAY REPORTS
// ==============================

function displayReports(reports) {

    const reportList =
        document.getElementById("reportList");

    reportList.innerHTML = "";

    if (reports.length === 0) {

        reportList.innerHTML =
            `<p>No reported reviews.</p>`;

        return;
    }

    reports.forEach(function(report) {

        const status = report.status || "PENDING";

        reportList.innerHTML += `

            <div class="report-card">

                <p>
                    <strong>Report ID:</strong>
                    ${report.id}
                </p>

                <p>
                    <strong>Review ID:</strong>
                    ${report.reviewId}
                </p>

                <p>
                    <strong>User ID:</strong>
                    ${report.userId}
                </p>

                <p>
                    <strong>Reason:</strong>
                    ${escapeAdminHtml(report.reason || "")}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${escapeAdminHtml(status)}
                </p>

                <button
                    onclick="markReportReviewed(${report.id})">
                    Mark Reviewed
                </button>

                <button
                    onclick="rejectReport(${report.id})">
                    Reject Report
                </button>

                <button
                    onclick="deleteReportedReview(
                        ${report.reviewId},
                        ${report.id}
                    )">
                    Delete Review
                </button>

            </div>

        `;

    });

}


// ==============================
// MARK REPORT AS REVIEWED
// ==============================

function markReportReviewed(reportId) {

    if (!confirm("Mark this report as reviewed?")) {
        return;
    }

    fetch(
        REPORT_API +
        "/" +
        reportId +
        "/status?status=REVIEWED",
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Report status update failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert("Report marked as reviewed.");

            loadReports();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Report status could not be updated."
            );

        });

}


// ==============================
// REJECT REPORT
// ==============================

function rejectReport(reportId) {

    if (!confirm("Reject this report?")) {
        return;
    }

    fetch(
        REPORT_API +
        "/" +
        reportId +
        "/status?status=REJECTED",
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Report rejection failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert("Report rejected.");

            loadReports();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Report could not be rejected."
            );

        });

}


// ==============================
// DELETE REPORTED REVIEW
// ==============================

function deleteReportedReview(reviewId, reportId) {

    if (!confirm("Delete this reported review?")) {
        return;
    }

    fetch(
        REPORT_API + "/" + reportId,
        {
            method: "DELETE"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Report delete failed"
                );

            }

            return fetch(
                REVIEW_API + "/" + reviewId,
                {
                    method: "DELETE"
                }
            );

        })

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Review delete failed"
                );

            }

            alert(
                "Review deleted successfully!"
            );

            loadReports();

            loadDashboardMetrics();

            loadModerationQueue();

            loadHiddenReviews();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Review could not be deleted."
            );

        });

}


// ==============================
// LOAD BUSINESS CLAIMS
// ==============================

function loadBusinessClaims() {

    fetch(CLAIM_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Business Claim API Error"
                );

            }

            return response.json();

        })

        .then(function(claims) {

            displayBusinessClaims(claims);

        })

        .catch(function(error) {

            console.error(error);

            document.getElementById("claimList").innerHTML =
                `<p class="error-message">
                    Unable to load business claims.
                </p>`;

        });

}


// ==============================
// DISPLAY BUSINESS CLAIMS
// ==============================

function displayBusinessClaims(claims) {

    const claimList =
        document.getElementById("claimList");

    claimList.innerHTML = "";

    if (claims.length === 0) {

        claimList.innerHTML =
            `<p>No business claims found.</p>`;

        return;
    }

    claims.forEach(function(claim) {

        claimList.innerHTML += `

            <div class="claim-card">

                <p>
                    <strong>Claim ID:</strong>
                    ${claim.id}
                </p>

                <p>
                    <strong>Business ID:</strong>
                    ${claim.businessId}
                </p>

                <p>
                    <strong>User ID:</strong>
                    ${claim.userId}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${escapeAdminHtml(
                        claim.status || ""
                    )}
                </p>

                <button
                    onclick="approveClaim(${claim.id})">
                    Approve
                </button>

                <button
                    onclick="rejectClaim(${claim.id})">
                    Reject
                </button>

                <button
                    onclick="deleteBusinessClaim(${claim.id})">
                    Delete Claim
                </button>

            </div>

        `;

    });

}


// ==============================
// APPROVE BUSINESS CLAIM
// ==============================

function approveClaim(claimId) {

    if (!confirm("Approve this business claim?")) {
        return;
    }

    fetch(
        CLAIM_API +
        "/" +
        claimId +
        "/status?status=APPROVED",
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Claim approval failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert(
                "Business claim approved successfully!"
            );

            loadBusinessClaims();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Business claim could not be approved."
            );

        });

}


// ==============================
// REJECT BUSINESS CLAIM
// ==============================

function rejectClaim(claimId) {

    if (!confirm("Reject this business claim?")) {
        return;
    }

    fetch(
        CLAIM_API +
        "/" +
        claimId +
        "/status?status=REJECTED",
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Claim rejection failed"
                );

            }

            return response.json();

        })

        .then(function(data) {

            alert("Business claim rejected!");

            loadBusinessClaims();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Business claim could not be rejected."
            );

        });

}


// ==============================
// DELETE BUSINESS CLAIM
// ==============================

function deleteBusinessClaim(claimId) {

    if (!confirm("Delete this business claim?")) {
        return;
    }

    fetch(
        CLAIM_API + "/" + claimId,
        {
            method: "DELETE"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Claim delete failed"
                );

            }

            alert(
                "Business claim deleted successfully!"
            );

            loadBusinessClaims();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Business claim could not be deleted."
            );

        });

}


// ==============================
// LOAD USERS
// ==============================

function loadUsers() {

    fetch(USER_API)

        .then(function(response) {

            if (!response.ok) {
                throw new Error("User API Error");
            }

            return response.json();

        })

        .then(function(users) {

            displayUsers(users);

        })

        .catch(function(error) {

            console.error(error);

            const userList =
                document.getElementById("userList");

            if (userList) {

                userList.innerHTML =
                    `<p class="error-message">
                        Unable to load users.
                    </p>`;

            }

        });

}


// ==============================
// DISPLAY USERS
// ==============================

function displayUsers(users) {

    const userList =
        document.getElementById("userList");

    if (!userList) {
        return;
    }

    userList.innerHTML = "";

    if (users.length === 0) {

        userList.innerHTML =
            `<p>No users found.</p>`;

        return;
    }

    users.forEach(function(user) {

        userList.innerHTML += `

            <div class="user-card">

                <p>
                    <strong>User ID:</strong>
                    ${user.id}
                </p>

                <p>
                    <strong>Name:</strong>
                    ${escapeAdminHtml(
                        user.name || ""
                    )}
                </p>

                <p>
                    <strong>Email:</strong>
                    ${escapeAdminHtml(
                        user.email || ""
                    )}
                </p>

                <p>
                    <strong>Role:</strong>
                    ${escapeAdminHtml(
                        user.role || "USER"
                    )}
                </p>

                <button
                    onclick="deleteUser(${user.id})">
                    Delete User
                </button>

            </div>

        `;

    });

}


// ==============================
// DELETE USER
// ==============================

function deleteUser(userId) {

    if (!confirm("Delete this user?")) {
        return;
    }

    fetch(
        USER_API + "/" + userId,
        {
            method: "DELETE"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "User delete failed"
                );

            }

            return response.text();

        })

        .then(function(data) {

            alert("User deleted successfully!");

            loadUsers();

            loadDashboardMetrics();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "User could not be deleted."
            );

        });

}


// ==============================
// ESCAPE ADMIN HTML
// ==============================

function escapeAdminHtml(value) {

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


// ==============================
// LOAD WEBSITES
// ==============================

function loadWebsites() {

    fetch(WEBSITE_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Website API Error"
                );

            }

            return response.json();

        })

        .then(function(websites) {

            displayWebsites(websites);

            loadSeoWebsiteOptions(websites);

        })

        .catch(function(error) {

            console.error(error);

            const websiteList =
                document.getElementById("websiteList");

            if (websiteList) {

                websiteList.innerHTML =
                    `<p class="error-message">
                        Unable to load websites.
                    </p>`;

            }

        });

}


// ==============================
// DISPLAY WEBSITES
// ==============================

function displayWebsites(websites) {

    const websiteList =
        document.getElementById("websiteList");

    if (!websiteList) {
        return;
    }

    websiteList.innerHTML = "";

    if (websites.length === 0) {

        websiteList.innerHTML =
            `<p>No websites found.</p>`;

        return;
    }

    websites.forEach(function(website) {

        websiteList.innerHTML += `

            <div class="website-card">

                <p>
                    <strong>Website ID:</strong>
                    ${website.id}
                </p>

                <p>
                    <strong>Name:</strong>
                    ${escapeAdminHtml(
                        website.name || ""
                    )}
                </p>

                <p>
                    <strong>URL:</strong>
                    ${escapeAdminHtml(
                        website.url || ""
                    )}
                </p>

                <p>
                    <strong>Description:</strong>
                    ${escapeAdminHtml(
                        website.description || ""
                    )}
                </p>

                <button
                    onclick="deleteWebsite(${website.id})">
                    Delete Website
                </button>

            </div>

        `;

    });

}


// ==============================
// DELETE WEBSITE
// ==============================

function deleteWebsite(websiteId) {

    if (!confirm("Delete this website?")) {
        return;
    }

    fetch(
        WEBSITE_API + "/" + websiteId,
        {
            method: "DELETE"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Website delete failed"
                );

            }

            return response.text();

        })

        .then(function(data) {

            alert(
                "Website deleted successfully!"
            );

            loadWebsites();

            loadDashboardMetrics();

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Website could not be deleted."
            );

        });

}


// ==============================
// SEO WEBSITE OPTIONS
// ==============================

function loadSeoWebsiteOptions(websites) {

    const seoWebsite =
        document.getElementById("seoWebsite");

    if (!seoWebsite) {
        return;
    }

    seoWebsite.innerHTML =
        `<option value="">
            Select a website
        </option>`;

    websites.forEach(function(website) {

        const option =
            document.createElement("option");

        option.value = website.id;

        option.textContent =
            website.name || website.url;

        option.dataset.seoTitle =
            website.seoTitle || "";

        option.dataset.seoDescription =
            website.seoDescription || "";

        option.dataset.canonicalUrl =
            website.canonicalUrl || "";

        seoWebsite.appendChild(option);

    });

}


// ==============================
// LOAD SELECTED SEO DATA
// ==============================

function loadSelectedSeoData() {

    const seoWebsite =
        document.getElementById("seoWebsite");

    if (!seoWebsite) {
        return;
    }

    const selectedOption =
        seoWebsite.options[seoWebsite.selectedIndex];

    const seoTitle =
        document.getElementById("seoTitle");

    const seoDescription =
        document.getElementById("seoDescription");

    const canonicalUrl =
        document.getElementById("canonicalUrl");

    if (!selectedOption || !selectedOption.value) {

        seoTitle.value = "";

        seoDescription.value = "";

        canonicalUrl.value = "";

        return;
    }

    seoTitle.value =
        selectedOption.dataset.seoTitle || "";

    seoDescription.value =
        selectedOption.dataset.seoDescription || "";

    canonicalUrl.value =
        selectedOption.dataset.canonicalUrl || "";

}


// ==============================
// UPDATE SEO SETTINGS
// ==============================

function updateSeo() {

    const seoWebsite =
        document.getElementById("seoWebsite");

    const seoTitle =
        document.getElementById("seoTitle").value;

    const seoDescription =
        document.getElementById("seoDescription").value;

    const canonicalUrl =
        document.getElementById("canonicalUrl").value;

    const seoMessage =
        document.getElementById("seoMessage");

    if (!seoWebsite.value) {

        seoMessage.textContent =
            "Please select a website.";

        seoMessage.className =
            "message error-message";

        return;
    }

    const websiteId =
        seoWebsite.value;

    const params = new URLSearchParams();

    if (seoTitle.trim() !== "") {
        params.append("seoTitle", seoTitle);
    }

    if (seoDescription.trim() !== "") {
        params.append(
            "seoDescription",
            seoDescription
        );
    }

    if (canonicalUrl.trim() !== "") {
        params.append(
            "canonicalUrl",
            canonicalUrl
        );
    }

    fetch(
        WEBSITE_API +
        "/" +
        websiteId +
        "/seo?" +
        params.toString(),
        {
            method: "PUT"
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "SEO update failed"
                );

            }

            return response.json();

        })

        .then(function(website) {

            seoMessage.textContent =
                "SEO settings updated successfully.";

            seoMessage.className =
                "message success-message";

            const selectedOption =
                seoWebsite.options[
                    seoWebsite.selectedIndex
                ];

            selectedOption.dataset.seoTitle =
                website.seoTitle || "";

            selectedOption.dataset.seoDescription =
                website.seoDescription || "";

            selectedOption.dataset.canonicalUrl =
                website.canonicalUrl || "";

        })

        .catch(function(error) {

            console.error(error);

            seoMessage.textContent =
                "SEO settings could not be updated.";

            seoMessage.className =
                "message error-message";

        });

}


// ==============================
// LOAD AUDIT LOGS
// ==============================

function loadAuditLogs() {

    fetch(AUDIT_LOG_API)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Audit Log API Error"
                );

            }

            return response.json();

        })

        .then(function(logs) {

            displayAuditLogs(logs);

        })

        .catch(function(error) {

            console.error(error);

            const auditLogList =
                document.getElementById("auditLogList");

            if (auditLogList) {

                auditLogList.innerHTML =
                    `<p class="error-message">
                        Unable to load audit logs.
                    </p>`;

            }

        });

}


// ==============================
// DISPLAY AUDIT LOGS
// ==============================

function displayAuditLogs(logs) {

    const auditLogList =
        document.getElementById("auditLogList");

    if (!auditLogList) {
        return;
    }

    auditLogList.innerHTML = "";

    if (logs.length === 0) {

        auditLogList.innerHTML =
            `<p>No audit logs found.</p>`;

        return;
    }

    logs.forEach(function(log) {

        auditLogList.innerHTML += `

            <div class="report-card">

                <p>
                    <strong>Log ID:</strong>
                    ${log.id}
                </p>

                <p>
                    <strong>Action:</strong>
                    ${escapeAdminHtml(log.action || "")}
                </p>

                <p>
                    <strong>Entity Type:</strong>
                    ${escapeAdminHtml(log.entityType || "")}
                </p>

                <p>
                    <strong>Entity ID:</strong>
                    ${log.entityId}
                </p>

                <p>
                    <strong>Performed By:</strong>
                    ${log.performedBy}
                </p>

                <p>
                    <strong>Details:</strong>
                    ${escapeAdminHtml(log.details || "")}
                </p>

                <p>
                    <strong>Date & Time:</strong>
                    ${escapeAdminHtml(log.createdAt || "")}
                </p>

            </div>

        `;

    });

}


// ==============================
// PAGE LOAD
// ==============================

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadDashboardMetrics();

        loadModerationQueue();

        loadHiddenReviews();

        loadReports();

        loadBusinessClaims();

        loadUsers();

        loadWebsites();

        loadAuditLogs();

        const seoWebsite =
            document.getElementById("seoWebsite");

        if (seoWebsite) {

            seoWebsite.addEventListener(
                "change",
                loadSelectedSeoData
            );

        }

    }
);