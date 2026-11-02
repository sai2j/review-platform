const PROFILE_API = "/review-platform/users/profile";

const profileForm = document.getElementById("profileForm");
const profileName = document.getElementById("profileName");
const profileEmail = document.getElementById("profileEmail");
const profileBio = document.getElementById("profileBio");
const profileCountry = document.getElementById("profileCountry");
const profileVisibility = document.getElementById("profileVisibility");
const profileDisplayName = document.getElementById("profileDisplayName");
const profileDisplayEmail = document.getElementById("profileDisplayEmail");
const profileAvatar = document.getElementById("profileAvatar");
const profileVisibilityBadge = document.getElementById("profileVisibilityBadge");
const profileMessage = document.getElementById("profileMessage");
const saveProfileButton = document.getElementById("saveProfileButton");
const bioCount = document.getElementById("bioCount");
const avatarFile = document.getElementById("avatarFile");
const uploadAvatarButton = document.getElementById("uploadAvatarButton");

const storedLogin = localStorage.getItem("loggedInUser");
let profileLoggedInUser = null;

try {
    profileLoggedInUser = JSON.parse(storedLogin);
} catch (error) {
    profileLoggedInUser = null;
}

if (!profileLoggedInUser || !profileLoggedInUser.id) {
    alert("Please login first.");
    window.location.href = "login.html";
}

function showProfileMessage(message, type) {
    if (!profileMessage) return;

    profileMessage.textContent = message;
    profileMessage.className = "message " + type;
}

function goToDashboard() {
    window.location.href = "user-dashboard.html";
}

function resolveAvatarUrl(avatarUrl) {
    if (!avatarUrl) return null;

    try {
        const url = new URL(avatarUrl, window.location.href);

        if (url.protocol !== "http:" && url.protocol !== "https:") {
            return null;
        }

        return url.href;
    } catch (error) {
        return null;
    }
}

function updateSidebarUser(profile) {
    const email = profile.email || (profileLoggedInUser && profileLoggedInUser.email) || "";
    const emailEl = document.getElementById("loggedUserEmail");
    const initialEl = document.getElementById("sidebarInitial");

    if (emailEl) emailEl.textContent = email;
    if (initialEl) initialEl.textContent = (email.charAt(0) || "U").toUpperCase();
}

function updateProfileHeader(profile) {
    const name = profile.name || "User";

    currentProfile = profile;
    if (typeof updateStrength === "function") updateStrength();

    updateSidebarUser(profile);

    profileDisplayName.textContent = name;
    profileDisplayEmail.textContent = profile.email || "";

    const avatarUrl = resolveAvatarUrl(profile.avatarUrl);

    if (avatarUrl) {
        const image = document.createElement("img");
        image.src = avatarUrl;
        image.alt = "Profile photo";
        image.className = "profile-avatar-image";

        image.onerror = function () {
            profileAvatar.replaceChildren();
            profileAvatar.textContent =
                name.trim().charAt(0).toUpperCase() || "U";
        };

        profileAvatar.replaceChildren(image);
    } else {
        profileAvatar.replaceChildren();
        profileAvatar.textContent =
            name.trim().charAt(0).toUpperCase() || "U";
    }

    const isPublic = profile.profilePublic !== false;

    profileVisibilityBadge.textContent = isPublic
        ? "PUBLIC PROFILE"
        : "PRIVATE PROFILE";

    profileVisibilityBadge.className = isPublic
        ? "visibility-badge"
        : "visibility-badge private";

    const bioEl = document.getElementById("profileDisplayBio");
    const countryEl = document.getElementById("profileDisplayCountry");

    if (bioEl) {
        bioEl.textContent = profile.bio || "";
        bioEl.hidden = !profile.bio;
    }

    if (countryEl) {
        countryEl.textContent = profile.country ? "\uD83D\uDCCD " + profile.country : "";
        countryEl.hidden = !profile.country;
    }
}

async function loadMyProfile() {
    try {
        showProfileMessage("Loading your profile...", "info");

        const response = await fetch(PROFILE_API, {
            method: "GET",
            credentials: "include",
            headers: { "Accept": "application/json" }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error(
                    "Your session may have expired. Please login again."
                );
            }

            throw new Error("Unable to load your profile.");
        }

        const profile = await response.json();

        profileName.value = profile.name || "";
        profileEmail.value = profile.email || "";
        profileBio.value = profile.bio || "";
        profileCountry.value = profile.country || "";

        profileVisibility.value =
            profile.profilePublic === false ? "false" : "true";

        bioCount.textContent = profileBio.value.length;

        updateProfileHeader(profile);
        profileMessage.textContent = "";
        profileMessage.className = "message";

    } catch (error) {
        console.error("Profile loading error:", error);

        showProfileMessage(
            error.message || "Unable to load your profile.",
            "error"
        );
    }
}

profileBio.addEventListener("input", function () {
    bioCount.textContent = profileBio.value.length;
});

profileForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = profileName.value.trim();
    const bio = profileBio.value.trim();
    const country = profileCountry.value.trim();

    if (!name) {
        showProfileMessage("Name is required.", "error");
        profileName.focus();
        return;
    }

    if (name.length > 100) {
        showProfileMessage("Name cannot exceed 100 characters.", "error");
        profileName.focus();
        return;
    }

    if (bio.length > 500) {
        showProfileMessage("Bio cannot exceed 500 characters.", "error");
        profileBio.focus();
        return;
    }

    if (country.length > 100) {
        showProfileMessage("Country cannot exceed 100 characters.", "error");
        profileCountry.focus();
        return;
    }

    const profileData = {
        name: name,
        bio: bio,
        country: country,
        profilePublic: profileVisibility.value === "true"
    };

    saveProfileButton.disabled = true;
    saveProfileButton.textContent = "Saving...";

    try {
        showProfileMessage("Saving your changes...", "info");

        const response = await fetch(PROFILE_API, {
            method: "PUT",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(profileData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Unable to save your profile.");
        }

        const updatedProfile = await response.json();

        profileName.value = updatedProfile.name || "";
        profileBio.value = updatedProfile.bio || "";
        profileCountry.value = updatedProfile.country || "";
        profileVisibility.value =
            updatedProfile.profilePublic === false ? "false" : "true";

        bioCount.textContent = profileBio.value.length;
        updateProfileHeader(updatedProfile);

        const storedUser = JSON.parse(
            localStorage.getItem("loggedInUser") || "null"
        );

        if (storedUser) {
            storedUser.name = updatedProfile.name;

            localStorage.setItem(
                "loggedInUser",
                JSON.stringify(storedUser)
            );
        }

        showProfileMessage(
            "Profile updated successfully.",
            "success"
        );

        closeEditPanel();

    } catch (error) {
        console.error("Profile update error:", error);

        showProfileMessage(
            error.message || "Unable to save your profile.",
            "error"
        );

    } finally {
        saveProfileButton.disabled = false;
        saveProfileButton.textContent = "Save Changes";
    }
});

uploadAvatarButton.addEventListener("click", async function () {
    const file = avatarFile.files[0];

    if (!file) {
        showProfileMessage("Please select a photo first.", "error");
        return;
    }

    if (file.size > 2 * 1024 * 1024) {
        showProfileMessage(
            "Image size must not exceed 2 MB.",
            "error"
        );
        return;
    }

    const allowedTypes = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ];

    if (!allowedTypes.includes(file.type)) {
        showProfileMessage(
            "Choose a JPG, PNG or WebP image.",
            "error"
        );
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    uploadAvatarButton.disabled = true;
    uploadAvatarButton.textContent = "Uploading...";

    try {
        const response = await fetch(
            PROFILE_API + "/avatar",
            {
                method: "POST",
                credentials: "include",
                body: formData
            }
        );

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(
                errorText || "Unable to upload profile photo."
            );
        }

        const updatedProfile = await response.json();

        updateProfileHeader(updatedProfile);

        // Refresh the image URL after replacing a photo.
        const image = profileAvatar.querySelector("img");

        if (image) {
            const url = resolveAvatarUrl(updatedProfile.avatarUrl);

            if (url) {
                image.src = url + "?v=" + Date.now();
            }
        }

        avatarFile.value = "";

        showProfileMessage(
            "Profile photo updated successfully.",
            "success"
        );

    } catch (error) {
        console.error("Avatar upload error:", error);

        showProfileMessage(
            error.message || "Unable to upload profile photo.",
            "error"
        );

    } finally {
        uploadAvatarButton.disabled = false;
        uploadAvatarButton.textContent = "Upload Photo";
    }
});

// Auto-upload as soon as a photo is chosen
avatarFile.addEventListener("change", function () {
    if (avatarFile.files[0]) uploadAvatarButton.click();
});

// Show the logged-in email immediately (before the API responds)
updateSidebarUser({});

document.addEventListener("DOMContentLoaded", loadMyProfile);

/* =========================================================
   LOGOUT
========================================================= */
const logoutButton = document.getElementById("logoutButton");

if (logoutButton) {
    logoutButton.addEventListener("click", async function () {
        try {
            await fetch("/review-platform/users/logout", {
                method: "POST",
                credentials: "include"
            });
        } catch (error) {
            // ignore - we still clear the local login below
        }

        localStorage.removeItem("loggedInUser");
        localStorage.removeItem("isAdmin");
        window.location.href = "login.html";
    });
}

/* =========================================================
   EDIT PROFILE PANEL
   "Edit profile" button opens the form at the top of the page
========================================================= */
const panelEdit = document.getElementById("panelEdit");
const editProfileBtn = document.getElementById("editProfileBtn");
const cancelEditButton = document.getElementById("cancelEditButton");

function openEditPanel() {
    if (!panelEdit) return;

    panelEdit.hidden = false;
    document.body.style.overflow = "hidden";

    if (editProfileBtn) editProfileBtn.setAttribute("aria-expanded", "true");

    if (profileName) {
        setTimeout(function () { profileName.focus(); }, 50);
    }
}

function closeEditPanel() {
    if (!panelEdit) return;

    panelEdit.hidden = true;
    document.body.style.overflow = "";

    if (editProfileBtn) editProfileBtn.setAttribute("aria-expanded", "false");
}

if (editProfileBtn) {
    editProfileBtn.addEventListener("click", openEditPanel);
}

if (cancelEditButton) {
    cancelEditButton.addEventListener("click", function () {
        loadMyProfile();
        closeEditPanel();
    });
}

const editProfileClose = document.getElementById("editProfileClose");
if (editProfileClose) editProfileClose.addEventListener("click", function () { loadMyProfile(); closeEditPanel(); });

if (panelEdit) {
    panelEdit.addEventListener("click", function (event) {
        if (event.target === panelEdit) closeEditPanel();
    });
}

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && panelEdit && !panelEdit.hidden) closeEditPanel();
});

/* =========================================================
   PROFILE STRENGTH + PUBLIC PROFILE LINK
========================================================= */
let currentProfile = null;

function updateStrength() {
    const list = document.getElementById("strengthList");
    if (!list || !currentProfile) return;

    const done = {
        name: !!(currentProfile.name && currentProfile.name.trim()),
        bio: !!(currentProfile.bio && currentProfile.bio.trim()),
        country: !!(currentProfile.country && currentProfile.country.trim()),
        photo: !!currentProfile.avatarUrl,
        review: typeof myReviews !== "undefined" && myReviews.length > 0
    };

    const keys = Object.keys(done);
    const count = keys.filter(function (k) { return done[k]; }).length;
    const pct = Math.round((count / keys.length) * 100);

    list.querySelectorAll("li").forEach(function (li) {
        li.classList.toggle("done", !!done[li.dataset.key]);
    });

    document.getElementById("strengthRing").style.setProperty("--pct", pct);
    document.getElementById("strengthPct").textContent = pct + "%";

    const title = document.getElementById("strengthTitle");
    const hint = document.getElementById("strengthHint");

    if (pct === 100) {
        title.textContent = "Great job!";
        hint.textContent = "Your profile is complete.";
    } else {
        const next = keys.find(function (k) { return !done[k]; });
        const tips = {
            name: "Add your name to continue.",
            bio: "Add a short bio to reach the next step.",
            country: "Add your country to reach the next step.",
            photo: "Add a profile photo to reach the next step.",
            review: "Write your first review to reach 100%."
        };
        title.textContent = pct >= 60 ? "Almost there!" : "Let's get started";
        hint.textContent = tips[next];
    }
}

/* =========================================================
   MY REVIEWS
========================================================= */
const REVIEW_API = "/review-platform/reviews";
const WEBSITE_API = "/review-platform/websites";
const VOTE_API = "/review-platform/review-votes";

const myReviewList = document.getElementById("myReviewList");
const mrFilters = document.getElementById("mrFilters");

let myReviews = [];
let websiteCache = {};
let helpfulCache = {};
let activeFilter = "all";

const STAR_PATH = "M12 2l2.9 6.9 7.1.6-5.4 4.7 1.7 7-6.3-3.8-6.3 3.8 1.7-7L2 9.5l7.1-.6z";

function starSvg() {
    return '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="' + STAR_PATH + '"/></svg>';
}

function buildStars(rating) {
    const row = document.createElement("span");
    row.className = "sq-row";
    row.setAttribute("role", "img");
    row.setAttribute("aria-label", rating + " out of 5 stars");

    for (let i = 1; i <= 5; i++) {
        const box = document.createElement("span");
        box.className = "sq";
        box.style.setProperty("--fill", rating >= i ? "100%" : "0%");
        box.innerHTML = starSvg();
        row.appendChild(box);
    }

    return row;
}

function formatDate(value) {
    if (!value) return "";

    const date = new Date(value);

    if (isNaN(date.getTime())) return "";

    return date.toLocaleDateString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric"
    });
}

function statusLabel(status) {
    const value = String(status || "").toUpperCase();

    if (value === "APPROVED" || value === "VISIBLE" || value === "PUBLISHED") {
        return { text: "Published", cls: "approved" };
    }

    if (value === "PENDING") {
        return { text: "Pending review", cls: "pending" };
    }

    if (value === "REJECTED") {
        return { text: "Rejected", cls: "rejected" };
    }

    if (value === "HIDDEN") {
        return { text: "Hidden", cls: "hidden" };
    }

    return { text: value ? value.charAt(0) + value.slice(1).toLowerCase() : "Published", cls: "approved" };
}

function siteHost(site) {
    if (!site || !site.url) return "";

    try {
        const url = new URL(/^https?:/i.test(site.url) ? site.url : "https://" + site.url);
        return url.hostname.replace(/^www\./, "");
    } catch (error) {
        return "";
    }
}

function updateStats() {
    const total = myReviews.length;
    const sum = myReviews.reduce(function (acc, r) { return acc + (r.rating || 0); }, 0);
    const sites = new Set(myReviews.map(function (r) { return r.websiteId; })).size;
    const helpful = myReviews.reduce(function (acc, r) { return acc + (helpfulCache[r.id] || 0); }, 0);

    document.getElementById("statReviews").textContent = total;
    document.getElementById("statAvg").textContent = total ? (sum / total).toFixed(1) : "-";
    document.getElementById("statHelpful").textContent = helpful;
    document.getElementById("statSites").textContent = sites;

    const countBadge = document.getElementById("reviewsCount");
    if (countBadge) countBadge.textContent = total;

    updateStrength();
}

function renderMyReviews() {
    myReviewList.replaceChildren();

    const visible = myReviews.filter(function (r) {
        if (activeFilter === "high") return r.rating >= 4;
        if (activeFilter === "low") return r.rating <= 3;
        return true;
    });

    if (visible.length === 0) {
        const empty = document.createElement("p");
        empty.className = "mr-empty";

        if (myReviews.length === 0) {
            empty.innerHTML = 'You have not written any reviews yet. <a href="browse.html">Browse websites</a> to write your first one.';
        } else {
            empty.textContent = "No reviews match this filter.";
        }

        myReviewList.appendChild(empty);
        return;
    }

    visible.forEach(function (review) {
        myReviewList.appendChild(buildReviewCard(review));
    });
}

function buildReviewCard(review) {
    const site = websiteCache[review.websiteId] || null;
    const siteName = (site && site.name) || "Website #" + review.websiteId;
    const host = siteHost(site);
    const status = statusLabel(review.status);

    const card = document.createElement("article");
    card.className = "mr-item";

    const logo = document.createElement("a");
    logo.className = "mr-logo";
    logo.href = "review.html?id=" + encodeURIComponent(review.websiteId);
    const letter = siteName.charAt(0).toUpperCase();
    logo.textContent = letter;

    if (host) {
        const img = document.createElement("img");
        img.src = "https://www.google.com/s2/favicons?sz=128&domain=" + encodeURIComponent(host);
        img.alt = "";
        img.loading = "lazy";
        img.addEventListener("error", function () {
            logo.textContent = letter;
        });
        logo.replaceChildren(img);
    }

    const body = document.createElement("div");
    body.className = "mr-body";

    const top = document.createElement("div");
    top.className = "mr-top";

    const siteLink = document.createElement("a");
    siteLink.className = "mr-site";
    siteLink.href = logo.href;

    const strong = document.createElement("strong");
    strong.textContent = siteName;
    siteLink.appendChild(strong);

    if (host) {
        const small = document.createElement("small");
        small.textContent = host;
        siteLink.appendChild(small);
    }

    const badge = document.createElement("span");
    badge.className = "mr-status " + status.cls;
    badge.textContent = status.text;

    top.append(siteLink, badge);

    const meta = document.createElement("div");
    meta.className = "mr-meta";
    meta.appendChild(buildStars(review.rating || 0));

    const dateText = formatDate(review.createdAt);

    if (dateText) {
        const date = document.createElement("span");
        date.textContent = dateText;
        meta.appendChild(date);
    }

    const text = document.createElement("p");
    text.className = "mr-text";
    text.textContent = review.comment || "";

    const actions = document.createElement("div");
    actions.className = "mr-actions";

    const helpful = document.createElement("span");
    helpful.textContent = "\uD83D\uDC4D " + (helpfulCache[review.id] || 0) + " helpful";

    const editBtn = document.createElement("button");
    editBtn.type = "button";
    editBtn.textContent = "Edit";
    editBtn.addEventListener("click", function () { openEditModal(review); });

    const deleteBtn = document.createElement("button");
    deleteBtn.type = "button";
    deleteBtn.className = "mr-delete";
    deleteBtn.textContent = "Delete";
    deleteBtn.addEventListener("click", function () { deleteMyReview(review); });

    actions.append(helpful, editBtn, deleteBtn);
    body.append(top, meta, text, actions);
    card.append(logo, body);

    return card;
}

async function fetchWebsite(id) {
    if (websiteCache[id] !== undefined) return;

    try {
        const response = await fetch(WEBSITE_API + "/" + id, { credentials: "include" });
        websiteCache[id] = response.ok ? await response.json() : null;
    } catch (error) {
        websiteCache[id] = null;
    }
}

async function fetchHelpful(id) {
    try {
        const response = await fetch(VOTE_API + "/" + id + "/count", { credentials: "include" });

        if (response.ok) {
            const data = await response.json();
            helpfulCache[id] = Number(data.helpful) || 0;
        }
    } catch (error) {
        helpfulCache[id] = 0;
    }
}

async function loadMyReviews() {
    try {
        const response = await fetch(REVIEW_API + "/my", {
            credentials: "include",
            headers: { "Accept": "application/json" }
        });

        if (!response.ok) {
            throw new Error("Unable to load your reviews.");
        }

        myReviews = await response.json();

        const siteIds = Array.from(new Set(myReviews.map(function (r) { return r.websiteId; })));

        await Promise.all(
            siteIds.map(fetchWebsite).concat(myReviews.map(function (r) { return fetchHelpful(r.id); }))
        );

        updateStats();
        renderMyReviews();

    } catch (error) {
        console.error("My reviews error:", error);
        myReviewList.replaceChildren();

        const failed = document.createElement("p");
        failed.className = "mr-empty";
        failed.textContent = error.message || "Unable to load your reviews.";
        myReviewList.appendChild(failed);
    }
}

mrFilters.addEventListener("click", function (event) {
    const chip = event.target.closest(".mr-chip");

    if (!chip) return;

    activeFilter = chip.dataset.filter;

    mrFilters.querySelectorAll(".mr-chip").forEach(function (c) {
        c.classList.toggle("active", c === chip);
    });

    renderMyReviews();
});

/* =========================================================
   EDIT / DELETE REVIEW
========================================================= */
const editModal = document.getElementById("editModal");
const editStars = document.getElementById("editStars");
const editComment = document.getElementById("editComment");
const editError = document.getElementById("editError");
const editSave = document.getElementById("editSave");

let editingReview = null;
let editRating = 0;

function paintEditStars() {
    editStars.querySelectorAll("button").forEach(function (button) {
        button.classList.toggle("on", Number(button.dataset.value) <= editRating);
        button.setAttribute("aria-checked", Number(button.dataset.value) === editRating ? "true" : "false");
    });
}

for (let i = 1; i <= 5; i++) {
    const button = document.createElement("button");
    button.type = "button";
    button.dataset.value = i;
    button.setAttribute("role", "radio");
    button.setAttribute("aria-label", i + " star" + (i > 1 ? "s" : ""));
    button.innerHTML = starSvg();
    button.addEventListener("click", function () {
        editRating = i;
        paintEditStars();
    });
    editStars.appendChild(button);
}

function openEditModal(review) {
    editingReview = review;
    editRating = review.rating || 0;

    const site = websiteCache[review.websiteId];

    document.getElementById("editModalSite").textContent =
        (site && site.name) || "Website #" + review.websiteId;

    editComment.value = review.comment || "";
    editError.textContent = "";
    paintEditStars();
    editModal.hidden = false;
    editComment.focus();
}

function closeEditModal() {
    editModal.hidden = true;
    editingReview = null;
}

document.getElementById("editCancel").addEventListener("click", closeEditModal);

editModal.addEventListener("click", function (event) {
    if (event.target === editModal) closeEditModal();
});

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && !editModal.hidden) closeEditModal();
});

async function readError(response, fallback) {
    try {
        const text = await response.text();

        try {
            const json = JSON.parse(text);
            return json.message || json.error || fallback;
        } catch (parseError) {
            return text || fallback;
        }
    } catch (error) {
        return fallback;
    }
}

editSave.addEventListener("click", async function () {
    if (!editingReview) return;

    const comment = editComment.value.trim();

    if (!editRating) {
        editError.textContent = "Please choose a star rating.";
        return;
    }

    if (!comment) {
        editError.textContent = "Please write your review.";
        return;
    }

    editSave.disabled = true;
    editSave.textContent = "Saving...";
    editError.textContent = "";

    try {
        const response = await fetch(REVIEW_API + "/" + editingReview.id, {
            method: "PUT",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({
                rating: editRating,
                comment: comment,
                websiteId: editingReview.websiteId
            })
        });

        if (!response.ok) {
            throw new Error(await readError(response, "Unable to update your review."));
        }

        const updated = await response.json();
        const index = myReviews.findIndex(function (r) { return r.id === editingReview.id; });

        if (index !== -1) {
            myReviews[index] = Object.assign({}, myReviews[index], updated);
        }

        closeEditModal();
        updateStats();
        renderMyReviews();
        showProfileMessage("Review updated successfully.", "success");

    } catch (error) {
        editError.textContent = error.message || "Unable to update your review.";

    } finally {
        editSave.disabled = false;
        editSave.textContent = "Save changes";
    }
});

async function deleteMyReview(review) {
    if (!window.confirm("Delete this review? This cannot be undone.")) return;

    try {
        const response = await fetch(REVIEW_API + "/" + review.id, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error(await readError(response, "Unable to delete your review."));
        }

        myReviews = myReviews.filter(function (r) { return r.id !== review.id; });
        updateStats();
        renderMyReviews();
        showProfileMessage("Review deleted.", "success");

    } catch (error) {
        showProfileMessage(error.message || "Unable to delete your review.", "error");
    }
}

document.addEventListener("DOMContentLoaded", loadMyReviews);