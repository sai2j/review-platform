const REPORT_API = "/review-platform/reports";

const REVIEW_API = "/review-platform/reviews";

const CLAIM_API = "/review-platform/business-claims";


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

    const reportList = document.getElementById("reportList");

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
                    ${report.reason}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${status}
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
                throw new Error("Report status update failed");
            }

            return response.json();

        })

        .then(function(data) {

            alert("Report marked as reviewed.");

            loadReports();

        })

        .catch(function(error) {

            console.error(error);

            alert("Report status could not be updated.");

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
                throw new Error("Report rejection failed");
            }

            return response.json();

        })

        .then(function(data) {

            alert("Report rejected.");

            loadReports();

        })

        .catch(function(error) {

            console.error(error);

            alert("Report could not be rejected.");

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
        REVIEW_API + "/" + reviewId,
        {
            method: "DELETE"
        }
    )

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Review delete failed");
            }

            return fetch(
                REPORT_API + "/" + reportId,
                {
                    method: "DELETE"
                }
            );

        })

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Report delete failed");
            }

            alert("Review deleted successfully!");

            loadReports();

        })

        .catch(function(error) {

            console.error(error);

            alert("Review could not be deleted.");

        });

}


// ==============================
// LOAD BUSINESS CLAIMS
// ==============================

function loadBusinessClaims() {

    fetch(CLAIM_API)

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Business Claim API Error");
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
                    ${claim.status}
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
                throw new Error("Claim approval failed");
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
                throw new Error("Claim rejection failed");
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
                throw new Error("Claim delete failed");
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
// PAGE LOAD
// ==============================

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadReports();

        loadBusinessClaims();

    }
);