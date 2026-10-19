"use strict";

/* =========================================================
   SETTINGS
   ========================================================= */

const BASE = "/review-platform";

const API = {
    pending:     BASE + "/reviews/pending",
    hidden:      BASE + "/reviews/hidden",
    allReviews:  BASE + "/reviews?page=0&size=1",     // sirf total count ke liye
    reviewStatus: function (id, status) {
        return BASE + "/reviews/" + id + "/status?status=" + status;
    },
    reports:     BASE + "/reports",
    reportStatus: function (id, status) {
        return BASE + "/reports/" + id + "/status?status=" + status;
    },
    claims:      BASE + "/business-claims",
    claimStatus: function (id, status) {
        return BASE + "/business-claims/" + id + "/status?status=" + status;
    },
    users:       BASE + "/users",
    websites:    BASE + "/websites",
    websiteSeo:  function (id) {
        return BASE + "/websites/" + id + "/seo";
    },
    logs:        BASE + "/audit-logs",
    logout:      BASE + "/users/logout"
};

const PAGE_SIZE = 5;    // har table / list mein ek page par kitne rows

// Har section ki "poore 5 rows wali" height yaad rakhta hai,
// taaki rows kam hone par bhi card chhota na ho.
const sectionHeights = {};

// Jab tak koi poora page nahi dekha, tab tak ka andaaza (px).
const DEFAULT_SECTION_HEIGHTS = {
    queue: 300,
    hidden: 400,
    claims: 400,
    reports: 300,
    users: 330,
    websites: 330,
    logs: 400
};


/* =========================================================
   STATE
   ========================================================= */

const state = {
    queue:    { url: API.pending,  rows: [], page: 1, status: "loading", error: "" },
    hidden:   { url: API.hidden,   rows: [], page: 1, status: "loading", error: "" },
    claims:   { url: API.claims,   rows: [], page: 1, status: "loading", error: "" },
    reports:  { url: API.reports,  rows: [], page: 1, status: "loading", error: "" },
    users:    { url: API.users,    rows: [], page: 1, status: "loading", error: "" },
    websites: { url: API.websites, rows: [], page: 1, status: "loading", error: "" },
    logs:     { url: API.logs,     rows: [], page: 1, status: "loading", error: "" }
};

const SECTIONS = {
    queue:    { body: "queueBody",    pager: "queuePager",    unit: "reviews",  empty: "No reviews are waiting for a decision.", render: renderQueue },
    hidden:   { body: "hiddenBody",   pager: "hiddenPager",   unit: "reviews",  empty: "No hidden reviews.",                      render: renderHidden },
    claims:   { body: "claimsBody",   pager: "claimsPager",   unit: "claims",   empty: "No business claims found.",               render: renderClaims },
    reports:  { body: "reportsBody",  pager: "reportsPager",  unit: "reports",  empty: "No reported reviews.",                    render: renderReports },
    users:    { body: "usersBody",    pager: "usersPager",    unit: "users",    empty: "No users found.",                         render: renderUsers },
    websites: { body: "websitesBody", pager: "websitesPager", unit: "websites", empty: "No websites found.",                      render: renderWebsites },
    logs:     { body: "logsBody",     pager: "logsPager",     unit: "logs",     empty: "No audit logs found.",                    render: renderLogs }
};

let totalReviews = null;
let searchQuery = "";


/* =========================================================
   SMALL HELPERS
   ========================================================= */

function $(id) {
    return document.getElementById(id);
}

function esc(value) {
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

function toArray(data) {
    if (Array.isArray(data)) {
        return data;
    }

    return data && Array.isArray(data.content) ? data.content : [];
}

function safeHref(url) {
    const value = String(url || "").trim();

    if (/^https?:\/\//i.test(value)) {
        return value;
    }

    return "https://" + value;
}

function titleAttr(text) {
    return text ? ' title="' + esc(text) + '"' : "";
}

function titleCase(text) {
    const value = String(text || "").toLowerCase();

    return value.charAt(0).toUpperCase() + value.slice(1);
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    const date = Array.isArray(value)
        ? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0)
        : new Date(value);

    if (isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
    });
}

function userName(id) {
    const user = state.users.rows.find(function (item) {
        return String(item.id) === String(id);
    });

    return user ? (user.name || user.email || "") : "";
}

function websiteName(id) {
    const website = state.websites.rows.find(function (item) {
        return String(item.id) === String(id);
    });

    return website ? (website.name || website.url || "") : "";
}

function getLoggedInUser() {
    try {
        return JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (error) {
        return null;
    }
}

let toastTimer = null;

function toast(message) {
    const element = $("toast");

    element.textContent = message;
    element.classList.add("show");

    clearTimeout(toastTimer);

    toastTimer = setTimeout(function () {
        element.classList.remove("show");
    }, 3000);
}

function showGate() {
    $("gate").hidden = false;
}

// Server se JSON mangwata hai. 401/403 par login wala popup dikhata hai.
async function fetchJson(url, options) {
    const response = await fetch(
        url,
        Object.assign({ credentials: "same-origin" }, options)
    );

    if (response.status === 401 || response.status === 403) {
        showGate();
        throw new Error("AUTH");
    }

    if (response.status === 429) {
        throw new Error("RATE");
    }

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }

    const text = await response.text();

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return text;
    }
}


/* =========================================================
   ROW HELPERS (local state update)
   ========================================================= */

function findRow(key, id) {
    return state[key].rows.find(function (row) {
        return String(row.id) === String(id);
    });
}

function removeRow(key, id) {
    state[key].rows = state[key].rows.filter(function (row) {
        return String(row.id) !== String(id);
    });
}

function replaceRow(key, id, data) {
    state[key].rows = state[key].rows.map(function (row) {
        return String(row.id) === String(id)
            ? Object.assign({}, row, data)
            : row;
    });
}

function dropReportsOfReview(reviewId) {
    state.reports.rows = state.reports.rows.filter(function (report) {
        return String(report.reviewId) !== String(reviewId);
    });
}


/* =========================================================
   FILTER + PAGINATION
   ========================================================= */

function searchText(key, row) {
    let text = JSON.stringify(row);

    if (key === "queue" || key === "hidden") {
        text += " " + userName(row.userId) + " " + websiteName(row.websiteId);
    }

    if (key === "reports") {
        text += " " + userName(row.userId);
    }

    return text.toLowerCase();
}

function filteredRows(key) {
    const rows = state[key].rows;

    if (!searchQuery) {
        return rows;
    }

    return rows.filter(function (row) {
        return searchText(key, row).indexOf(searchQuery) !== -1;
    });
}

function pageOf(key) {
    const all = filteredRows(key);
    const pages = Math.max(1, Math.ceil(all.length / PAGE_SIZE));
    const section = state[key];

    if (section.page > pages) {
        section.page = pages;
    }

    if (section.page < 1) {
        section.page = 1;
    }

    const start = (section.page - 1) * PAGE_SIZE;

    return {
        all: all,
        pages: pages,
        start: start,
        items: all.slice(start, start + PAGE_SIZE)
    };
}

function pagerHtml(key, view) {
    const page = state[key].page;
    const from = view.start + 1;
    const to = view.start + view.items.length;

    return (
        '<div class="pager">' +
        "<span>Showing " + from + "-" + to + " of " + view.all.length + " " + SECTIONS[key].unit + "</span>" +
        '<div class="pager-buttons">' +
        '<button type="button" data-action="page" data-section="' + key + '" data-dir="-1" aria-label="Previous page"' +
        (page <= 1 ? " disabled" : "") + ">&lsaquo;</button>" +
        "<span>Page " + page + " of " + view.pages + "</span>" +
        '<button type="button" data-action="page" data-section="' + key + '" data-dir="1" aria-label="Next page"' +
        (page >= view.pages ? " disabled" : "") + ">&rsaquo;</button>" +
        "</div></div>"
    );
}


/* =========================================================
   HTML BUILDERS
   ========================================================= */

function badge(status) {
    const value = String(status || "PENDING").toUpperCase();
    let cls = "badge";

    if (["APPROVED", "REVIEWED", "VERIFIED", "ACTIVE"].indexOf(value) !== -1) {
        cls += " badge-ok";
    } else if (["REJECTED", "HIDDEN", "DELETED", "BLOCKED", "SUSPENDED"].indexOf(value) !== -1) {
        cls += " badge-bad";
    }

    return '<span class="' + cls + '">' + esc(value) + "</span>";
}

function roleBadge(role) {
    const value = String(role || "USER").toUpperCase();
    let cls = "badge";

    if (value === "ADMIN") {
        cls += " badge-admin";
    } else if (value === "MODERATOR") {
        cls += " badge-moderator";
    }

    return '<span class="' + cls + '">' + esc(value) + "</span>";
}

function actionButton(label, action, data, cls) {
    const attrs = Object.keys(data).map(function (name) {
        return " data-" + name + '="' + esc(data[name]) + '"';
    }).join("");

    return (
        '<button type="button" class="btn btn-small ' + (cls || "btn-soft") +
        '" data-action="' + action + '"' + attrs + ">" + esc(label) + "</button>"
    );
}

function table(headers, rows) {
    return (
        '<div class="table-wrap"><table><thead><tr>' +
        headers.map(function (header) {
            return '<th scope="col">' + header + "</th>";
        }).join("") +
        "</tr></thead><tbody>" + rows.join("") + "</tbody></table></div>"
    );
}

function ratingCell(rating) {
    return (
        '<span class="star" aria-hidden="true">&#9733;</span> ' + esc(rating) +
        '<span class="sr-only"> out of 5</span>'
    );
}


/* =========================================================
   SECTION RENDERERS
   ========================================================= */

function renderQueue(items) {
    return table(
        ["Review", "User", "Website", "Rating", "Comment", "Status", "Actions"],
        items.map(function (review) {
            const id = review.id;

            return (
                "<tr>" +
                "<td><strong>#" + esc(id) + "</strong></td>" +
                "<td" + titleAttr(userName(review.userId)) + ">" + esc(review.userId) + "</td>" +
                "<td" + titleAttr(websiteName(review.websiteId)) + ">" + esc(review.websiteId) + "</td>" +
                "<td>" + ratingCell(review.rating) + "</td>" +
                '<td class="comment-cell">' + esc(review.comment) + "</td>" +
                "<td>" + badge(review.status) + "</td>" +
                '<td><div class="actions">' +
                actionButton("Approve", "review-status", { id: id, status: "APPROVED" }) +
                actionButton("Reject", "review-status", { id: id, status: "REJECTED" }) +
                actionButton("Hide", "review-status", { id: id, status: "HIDDEN" }) +
                "</div></td></tr>"
            );
        })
    );
}

function renderHidden(items) {
    return '<ul class="list">' + items.map(function (review) {
        return (
            '<li class="list-row"><div class="list-main">' +
            "<strong>Review #" + esc(review.id) + " &middot; " + esc(review.rating) + " of 5</strong>" +
            (review.comment ? '<p class="list-text">' + esc(review.comment) + "</p>" : "") +
            '<span class="meta"' + titleAttr(userName(review.userId)) + ">User " + esc(review.userId) +
            " &middot; Website " + esc(review.websiteId) + "</span>" +
            "</div>" +
            actionButton("Restore", "restore", { id: review.id }) +
            "</li>"
        );
    }).join("") + "</ul>";
}

function renderClaims(items) {
    return '<ul class="list">' + items.map(function (claim) {
        const status = String(claim.status || "").toUpperCase();
        let buttons = "";

        if (status !== "APPROVED") {
            buttons += actionButton("Approve", "claim-status", { id: claim.id, status: "APPROVED" });
        }

        if (status !== "REJECTED") {
            buttons += actionButton("Reject", "claim-status", { id: claim.id, status: "REJECTED" });
        }

        buttons += actionButton("Delete claim", "claim-delete", { id: claim.id }, "btn-outline");

        return (
            '<li class="list-row"><div class="list-main">' +
            "<strong>Claim #" + esc(claim.id) + " &middot; Business " + esc(claim.businessId) +
            " &middot; User " + esc(claim.userId) + "</strong>" +
            '<div class="actions mt">' + buttons + "</div>" +
            "</div>" +
            '<div class="list-side">' + badge(claim.status) + "</div>" +
            "</li>"
        );
    }).join("") + "</ul>";
}

function renderReports(items) {
    return table(
        ["Report", "Review", "Reported by", "Reason", "Status", "Actions"],
        items.map(function (report) {
            const status = String(report.status || "PENDING").toUpperCase();
            let buttons = "";

            if (status === "PENDING") {
                buttons += actionButton("Mark reviewed", "report-status", { id: report.id, status: "REVIEWED" });
                buttons += actionButton("Reject report", "report-status", { id: report.id, status: "REJECTED" });
            }

            buttons += actionButton("Delete review", "report-delete-review", { id: report.reviewId }, "btn-outline");

            return (
                "<tr>" +
                "<td><strong>#" + esc(report.id) + "</strong></td>" +
                "<td>#" + esc(report.reviewId) + "</td>" +
                "<td" + titleAttr(userName(report.userId)) + ">User " + esc(report.userId) + "</td>" +
                '<td class="comment-cell">' + esc(report.reason) + "</td>" +
                "<td>" + badge(status) + "</td>" +
                '<td><div class="actions">' + buttons + "</div></td>" +
                "</tr>"
            );
        })
    );
}

function renderUsers(items) {
    return table(
        ["User", "Role", "Actions"],
        items.map(function (user) {
            return (
                "<tr>" +
                '<td><span class="cell-title">' + esc(user.name || "No name") + "</span>" +
                '<span class="cell-sub">' + esc(user.email) + " &middot; ID " + esc(user.id) + "</span></td>" +
                "<td>" + roleBadge(user.role) + "</td>" +
                '<td><div class="actions">' +
                actionButton("Delete user", "user-delete", { id: user.id }, "btn-outline") +
                "</div></td></tr>"
            );
        })
    );
}

function renderWebsites(items) {
    return table(
        ["Website", "Description", "Actions"],
        items.map(function (website) {
            return (
                "<tr>" +
                '<td><span class="cell-title">' + esc(website.name || "Unnamed website") + "</span>" +
                (website.url
                    ? '<span class="cell-sub"><a href="' + esc(safeHref(website.url)) +
                      '" target="_blank" rel="noopener noreferrer">' + esc(website.url) + "</a></span>"
                    : "") +
                "</td>" +
                '<td class="comment-cell">' + esc(website.description) + "</td>" +
                '<td><div class="actions">' +
                actionButton("Delete website", "website-delete", { id: website.id }, "btn-outline") +
                "</div></td></tr>"
            );
        })
    );
}

function actionLabel(log) {
    let action = String(log.action || "").toUpperCase();
    const type = String(log.entityType || "").toUpperCase();

    if (type && action.indexOf(type + "_") === 0) {
        action = action.slice(type.length + 1);
    }

    return action.toLowerCase().replace(/_/g, " ");
}

function renderLogs(items) {
    return '<ul class="list">' + items.map(function (log) {
        const details = String(log.details || "").replace(/->/g, "\u2192");
        const by = log.performedBy !== null && log.performedBy !== undefined
            ? "performed by user #" + log.performedBy
            : "performed by system";

        return (
            '<li class="list-row"><div class="list-main">' +
            "<strong>" + esc(titleCase(String(log.entityType || "").replace(/_/g, " "))) + " #" + esc(log.entityId) +
            " &middot; " + esc(actionLabel(log)) + "</strong>" +
            (details ? '<p class="list-text">' + esc(details) + "</p>" : "") +
            '<span class="meta">Log #' + esc(log.id) + " &middot; " + esc(by) + "</span>" +
            "</div>" +
            '<span class="list-time">' + esc(formatDateTime(log.createdAt)) + "</span>" +
            "</li>"
        );
    }).join("") + "</ul>";
}


/* =========================================================
   RENDER
   ========================================================= */

function renderSection(key) {
    const section = state[key];
    const config = SECTIONS[key];
    const body = $(config.body);
    const pager = $(config.pager);

    if (!body || !pager) {
        return;
    }

    if (section.status === "loading") {
        body.innerHTML = '<p class="state">Loading...</p>';
        pager.innerHTML = "";
        return;
    }

    if (section.status === "error") {
        body.innerHTML =
            '<p class="state error">' + esc(section.error) + " " +
            '<button type="button" class="btn btn-small btn-outline" data-action="retry" data-section="' +
            key + '">Retry</button></p>';
        pager.innerHTML = "";
        return;
    }

    const view = pageOf(key);

    if (view.all.length === 0) {
        body.innerHTML =
            '<p class="state">' +
            esc(searchQuery && section.rows.length ? "Nothing matches your search." : config.empty) +
            "</p>";
        pager.innerHTML = "";
        keepSectionHeight(key, 0);
        return;
    }

    body.innerHTML = config.render(view.items);
    pager.innerHTML = pagerHtml(key, view);
    keepSectionHeight(key, view.items.length);
}

// Rows kam ho ya na ho, section ki height pehle jaisi hi rakhta hai.
function keepSectionHeight(key, rowCount) {
    const body = $(SECTIONS[key].body);

    if (!body) {
        return;
    }

    body.style.minHeight = "";

    const naturalHeight = body.offsetHeight;

    if (rowCount >= PAGE_SIZE) {
        sectionHeights[key] = Math.max(sectionHeights[key] || 0, naturalHeight);
    } else if (rowCount > 0) {
        const rows = body.querySelectorAll("tbody tr, .list-row");

        if (rows.length) {
            let total = 0;

            rows.forEach(function (row) {
                total += row.offsetHeight;
            });

            const estimated = naturalHeight + (PAGE_SIZE - rowCount) * (total / rows.length);

            sectionHeights[key] = Math.max(sectionHeights[key] || 0, estimated);
        }
    }

    const target = sectionHeights[key] || DEFAULT_SECTION_HEIGHTS[key] || 0;

    if (target) {
        body.style.minHeight = Math.ceil(target) + "px";
    }
}

function setText(id, value) {
    const element = $(id);

    if (element) {
        element.textContent = value;
    }
}

function renderStats() {
    const ready = function (key) {
        return state[key].status === "ok";
    };

    setText("totalUsers", ready("users") ? state.users.rows.length : "-");
    setText("totalWebsites", ready("websites") ? state.websites.rows.length : "-");
    setText("totalReviews", totalReviews === null ? "-" : totalReviews);
    setText("pendingReviews", ready("queue") ? state.queue.rows.length : "-");

    setText("countQueue", ready("queue") ? state.queue.rows.length : 0);
    setText("countHidden", ready("hidden") ? state.hidden.rows.length : 0);
    setText("countClaims", ready("claims") ? state.claims.rows.length : 0);
    setText("countReports", ready("reports") ? state.reports.rows.length : 0);
    setText("countUsers", ready("users") ? state.users.rows.length : 0);
    setText("countWebsites", ready("websites") ? state.websites.rows.length : 0);

    if (ready("queue")) {
        const count = state.queue.rows.length;

        setText(
            "queueNote",
            count + (count === 1 ? " review" : " reviews") + " waiting for a decision"
        );
    } else {
        setText("queueNote", "");
    }
}

function renderAll() {
    Object.keys(SECTIONS).forEach(renderSection);
    renderStats();
    fillSeoSelect();
}


/* =========================================================
   LOADING DATA
   ========================================================= */

async function loadSection(key) {
    const section = state[key];

    section.status = "loading";
    renderSection(key);

    try {
        const data = await fetchJson(section.url);

        section.rows = toArray(data);

        if (key === "logs") {
            sortLogs();
        }

        section.status = "ok";
        section.error = "";
    } catch (error) {
        if (error.message === "AUTH") {
            return;
        }

        section.status = "error";
        section.error = error.message === "RATE"
            ? "Too many requests. Please wait a minute and retry."
            : "Unable to load " + SECTIONS[key].unit + ".";
    }

    renderSection(key);
    renderStats();

    if (key === "websites") {
        fillSeoSelect();
    }
}

function sortLogs() {
    state.logs.rows.sort(function (a, b) {
        return Number(b.id) - Number(a.id);
    });
}

async function refreshLogs() {
    try {
        state.logs.rows = toArray(await fetchJson(API.logs));
        sortLogs();
        state.logs.status = "ok";
        renderSection("logs");
    } catch (error) {
        /* logs refresh fail ho to purana data hi dikhta rahega */
    }
}

async function loadTotalReviews() {
    try {
        const data = await fetchJson(API.allReviews);

        if (data && typeof data.totalElements === "number") {
            totalReviews = data.totalElements;
        } else if (Array.isArray(data)) {
            totalReviews = data.length;
        }
    } catch (error) {
        totalReviews = null;
    }

    renderStats();
}

async function loadAll() {
    await Promise.all(Object.keys(state).map(loadSection));

    renderAll();
    loadTotalReviews();
}


/* =========================================================
   ACTIONS
   ========================================================= */

const FAIL_TEXT = {
    "user-delete": "User could not be deleted. They may still have reviews or other data linked.",
    "website-delete": "Website could not be deleted. It may still have reviews or a business linked."
};

const handlers = {

    "review-status": async function (button) {
        const id = Number(button.dataset.id);
        const status = button.dataset.status;

        const question = {
            APPROVED: "Approve this review?",
            HIDDEN: "Hide this review? It will no longer appear on the public page.",
            REJECTED: "Reject this review? This permanently deletes the review, its votes and its reports."
        }[status];

        if (!id || !question || !confirm(question)) {
            return;
        }

        const row = findRow("queue", id);
        const result = await fetchJson(API.reviewStatus(id, status), { method: "PUT" });

        removeRow("queue", id);

        if (status === "HIDDEN") {
            const hiddenRow = result && result.id
                ? result
                : Object.assign({}, row, { status: "HIDDEN" });

            state.hidden.rows.unshift(hiddenRow);
        }

        if (status === "REJECTED") {
            removeRow("hidden", id);
            dropReportsOfReview(id);

            if (totalReviews !== null && totalReviews > 0) {
                totalReviews--;
            }
        }

        toast({
            APPROVED: "Review approved.",
            HIDDEN: "Review hidden.",
            REJECTED: "Review rejected and deleted."
        }[status]);

        afterChange();
    },

    "restore": async function (button) {
        const id = Number(button.dataset.id);

        if (!id || !confirm("Restore this review? It will appear on the public page again.")) {
            return;
        }

        await fetchJson(API.reviewStatus(id, "APPROVED"), { method: "PUT" });

        removeRow("hidden", id);
        toast("Review restored.");
        afterChange();
    },

    "report-status": async function (button) {
        const id = Number(button.dataset.id);
        const status = button.dataset.status;
        const question = status === "REVIEWED" ? "Mark this report as reviewed?" : "Reject this report?";

        if (!id || !confirm(question)) {
            return;
        }

        const result = await fetchJson(API.reportStatus(id, status), { method: "PUT" });

        replaceRow("reports", id, result && result.id ? result : { status: status });

        toast(status === "REVIEWED" ? "Report marked as reviewed." : "Report rejected.");
        afterChange();
    },

    "report-delete-review": async function (button) {
        const reviewId = Number(button.dataset.id);

        if (!reviewId || !confirm("Delete this reported review? This permanently deletes the review and every report about it.")) {
            return;
        }

        await fetchJson(API.reviewStatus(reviewId, "REJECTED"), { method: "PUT" });

        removeRow("queue", reviewId);
        removeRow("hidden", reviewId);
        dropReportsOfReview(reviewId);

        if (totalReviews !== null && totalReviews > 0) {
            totalReviews--;
        }

        toast("Review deleted.");
        afterChange();
    },

    "claim-status": async function (button) {
        const id = Number(button.dataset.id);
        const status = button.dataset.status;
        const question = status === "APPROVED" ? "Approve this business claim?" : "Reject this business claim?";

        if (!id || !confirm(question)) {
            return;
        }

        const result = await fetchJson(API.claimStatus(id, status), { method: "PUT" });

        replaceRow("claims", id, result && result.id ? result : { status: status });

        toast(status === "APPROVED" ? "Business claim approved." : "Business claim rejected.");
        afterChange();
    },

    "claim-delete": async function (button) {
        const id = Number(button.dataset.id);

        if (!id || !confirm("Delete this business claim?")) {
            return;
        }

        await fetchJson(BASE + "/business-claims/" + id, { method: "DELETE" });

        removeRow("claims", id);
        toast("Business claim deleted.");
        afterChange();
    },

    "user-delete": async function (button) {
        const id = Number(button.dataset.id);
        const me = getLoggedInUser();

        if (!id) {
            return;
        }

        if (me && String(me.id) === String(id)) {
            toast("You cannot delete your own account from the admin panel.");
            return;
        }

        if (!confirm("Delete this user? This cannot be undone.")) {
            return;
        }

        await fetchJson(BASE + "/users/" + id, { method: "DELETE" });

        removeRow("users", id);
        toast("User deleted.");
        afterChange();
    },

    "website-delete": async function (button) {
        const id = Number(button.dataset.id);

        if (!id || !confirm("Delete this website? This cannot be undone.")) {
            return;
        }

        await fetchJson(BASE + "/websites/" + id, { method: "DELETE" });

        removeRow("websites", id);
        toast("Website deleted.");
        afterChange();
    }
};

async function runAction(button) {
    const action = button.dataset.action;
    const handler = handlers[action];

    if (!handler) {
        return;
    }

    button.disabled = true;

    try {
        await handler(button);
    } catch (error) {
        if (error.message === "AUTH") {
            return;
        }

        if (error.message === "RATE") {
            toast("Too many requests. Please wait a minute and try again.");
        } else {
            toast(FAIL_TEXT[action] || "Action could not be completed. Please try again.");
        }
    } finally {
        button.disabled = false;
    }
}

function afterChange() {
    renderAll();
    refreshLogs();
}

function changePage(button) {
    const key = button.dataset.section;
    const direction = Number(button.dataset.dir);

    if (!state[key]) {
        return;
    }

    state[key].page += direction;
    renderSection(key);

    const target = document.querySelector(
        '[data-action="page"][data-section="' + key + '"][data-dir="' + direction + '"]:not([disabled])'
    ) || document.querySelector(
        '[data-action="page"][data-section="' + key + '"]:not([disabled])'
    );

    if (target) {
        target.focus();
    }
}


/* =========================================================
   SEO CONTROLS
   ========================================================= */

function setSeoMessage(text, type) {
    const message = $("seoMessage");

    message.textContent = text;
    message.className = "form-message" + (type ? " " + type : "");
}

function fillSeoSelect() {
    const select = $("seoWebsite");

    if (!select) {
        return;
    }

    const selected = select.value;

    select.innerHTML = '<option value="">Select a website</option>' +
        state.websites.rows.map(function (website) {
            return '<option value="' + esc(website.id) + '">' +
                esc(website.name || website.url || "Website " + website.id) + "</option>";
        }).join("");

    if (selected && findRow("websites", selected)) {
        select.value = selected;
    } else {
        select.value = "";
        fillSeoFields();
    }
}

function fillSeoFields() {
    const website = findRow("websites", $("seoWebsite").value);

    $("seoTitle").value = website ? (website.seoTitle || "") : "";
    $("seoDescription").value = website ? (website.seoDescription || "") : "";
    $("canonicalUrl").value = website ? (website.canonicalUrl || "") : "";
}

async function saveSeo() {
    const id = $("seoWebsite").value;
    const button = $("seoSaveButton");

    if (!id) {
        setSeoMessage("Please select a website.", "error");
        return;
    }

    const params = new URLSearchParams();
    const values = {
        seoTitle: $("seoTitle").value.trim(),
        seoDescription: $("seoDescription").value.trim(),
        canonicalUrl: $("canonicalUrl").value.trim()
    };

    Object.keys(values).forEach(function (name) {
        if (values[name] !== "") {
            params.append(name, values[name]);
        }
    });

    if (Array.from(params).length === 0) {
        setSeoMessage("Enter at least one SEO value.", "error");
        return;
    }

    button.disabled = true;
    setSeoMessage("Saving...", "");

    try {
        const website = await fetchJson(
            API.websiteSeo(id) + "?" + params.toString(),
            { method: "PUT" }
        );

        replaceRow("websites", id, website && website.id ? website : values);
        fillSeoFields();
        setSeoMessage("SEO settings updated successfully.", "success");
        refreshLogs();
    } catch (error) {
        if (error.message !== "AUTH") {
            setSeoMessage("SEO settings could not be updated.", "error");
        }
    } finally {
        button.disabled = false;
    }
}


/* =========================================================
   ADMIN BOX, LOGOUT, NAVIGATION
   ========================================================= */

function initials(name) {
    const words = String(name).trim().split(/\s+/).filter(Boolean);

    if (words.length >= 2) {
        return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }

    return String(name).trim().slice(0, 2).toUpperCase() || "AD";
}

function setupAdminBox() {
    const user = getLoggedInUser();
    const name = (user && (user.name || user.email)) || "Admin";

    setText("adminName", name);
    setText("adminAvatar", initials(name));
}

async function logout() {
    try {
        await fetch(API.logout, { method: "POST", credentials: "same-origin" });
    } catch (error) {
        /* logout fail bhi ho to local data hata dete hain */
    }

    localStorage.removeItem("loggedInUser");
    localStorage.removeItem("isAdmin");

    window.location.href = "login.html";
}

let scrollTicking = false;

function updateActiveNav() {
    const links = Array.from(document.querySelectorAll(".nav-link"));
    const ids = links.map(function (link) {
        return link.dataset.target;
    });

    let current = ids[0];

    ids.forEach(function (id) {
        const section = $(id);

        if (section && section.getBoundingClientRect().top <= 140) {
            current = id;
        }
    });

    if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 4) {
        current = ids[ids.length - 1];
    }

    links.forEach(function (link) {
        const active = link.dataset.target === current;

        link.classList.toggle("active", active);

        if (active) {
            link.setAttribute("aria-current", "true");
        } else {
            link.removeAttribute("aria-current");
        }
    });

    scrollTicking = false;
}

function setupEvents() {
    document.addEventListener("click", function (event) {
        const button = event.target.closest("[data-action]");

        if (!button || button.disabled) {
            return;
        }

        const action = button.dataset.action;

        if (action === "page") {
            changePage(button);
        } else if (action === "retry") {
            loadSection(button.dataset.section);
        } else {
            runAction(button);
        }
    });

    $("logoutButton").addEventListener("click", logout);
    $("seoWebsite").addEventListener("change", fillSeoFields);
    $("seoSaveButton").addEventListener("click", saveSeo);

    let searchTimer = null;

    $("globalSearch").addEventListener("input", function (event) {
        clearTimeout(searchTimer);

        searchTimer = setTimeout(function () {
            searchQuery = event.target.value.trim().toLowerCase();

            Object.keys(state).forEach(function (key) {
                state[key].page = 1;
            });

            Object.keys(SECTIONS).forEach(renderSection);
        }, 150);
    });

    window.addEventListener("scroll", function () {
        if (!scrollTicking) {
            scrollTicking = true;
            requestAnimationFrame(updateActiveNav);
        }
    }, { passive: true });
}


/* =========================================================
   PAGE LOAD
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    setupAdminBox();
    setupEvents();
    loadAll();
});