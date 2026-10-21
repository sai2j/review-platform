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