const WEBSITE_API = "/review-platform/websites";
const DISCOVERY_API = "/review-platform/discovery/search";
const REVIEW_API = "/review-platform/reviews";
const METADATA_API = "/review-platform/metadata/fetch";

let allWebsites = [];


/* =========================================================
   LOAD SAVED WEBSITES
========================================================= */

function loadWebsites() {

    fetch(WEBSITE_API)

        .then(function(response) {

            if (!response.ok) {
                throw new Error("Website API error");
            }

            return response.json();
        })

        .then(function(websites) {

            allWebsites = websites;

            displayWebsites(websites);

        })

        .catch(function(error) {

            console.error(error);

            document.getElementById("websiteList").innerHTML = `

                <p class="error-message">
                    Unable to load websites.
                </p>

            `;
        });
}


/* =========================================================
   DISPLAY SAVED WEBSITES
========================================================= */

function displayWebsites(websites) {

    const websiteList =
        document.getElementById("websiteList");

    if (!websiteList) {
        return;
    }

    websiteList.innerHTML = "";

    if (websites.length === 0) {

        websiteList.innerHTML = `

            <p>
                No websites available.
            </p>

        `;

        return;
    }

    websites.forEach(function(website) {

        websiteList.innerHTML += `

            <div class="website-card">

                <h3>
                    ${escapeHtml(website.name)}
                </h3>

                <p>
                    ${escapeHtml(
                        website.description || ""
                    )}
                </p>

                <p>

                    <strong>
                        URL:
                    </strong>

                    <a
                        href="${escapeAttribute(website.url)}"
                        target="_blank">

                        ${escapeHtml(website.url)}

                    </a>

                </p>

                <button
                    onclick="viewWebsite(${website.id})">

                    View Website & Review

                </button>

            </div>

        `;
    });
}


/* =========================================================
   SEARCH WEBSITES
========================================================= */

function searchWebsites() {

    const searchInput =
        document.getElementById("searchInput");

    const keyword =
        searchInput.value.trim();

    if (keyword === "") {

        alert("Please enter a website name.");

        return;
    }

    const websiteList =
        document.getElementById("websiteList");

    websiteList.innerHTML = `

        <p>
            Searching for <strong>${escapeHtml(keyword)}</strong>...
        </p>

    `;

    fetch(
        DISCOVERY_API +
        "?keyword=" +
        encodeURIComponent(keyword)
    )

        .then(function(response) {

            if (!response.ok) {
                throw new Error(
                    "Website search failed"
                );
            }

            return response.json();
        })

        .then(function(results) {

            displaySearchResults(results);

        })

        .catch(function(error) {

            console.error(error);

            websiteList.innerHTML = `

                <p class="error-message">

                    Unable to search website.

                </p>

            `;
        });
}


/* =========================================================
   DISPLAY SEARCH RESULT
========================================================= */

function displaySearchResults(results) {

    const websiteList =
        document.getElementById("websiteList");

    websiteList.innerHTML = `

        <h2>
            Website Found
        </h2>

    `;

    if (!results || results.length === 0) {

        websiteList.innerHTML += `

            <p>
                No website found.
            </p>

        `;

        return;
    }

    // Only one result
    const result = results[0];

    websiteList.innerHTML += `

        <p>
            Result for your search
        </p>

        <div class="website-card">

            <h3>
                ${escapeHtml(result.title)}
            </h3>

            <p>
                ${escapeHtml(
                    result.description || ""
                )}
            </p>

            <p>

                <strong>
                    URL:
                </strong>

                <a
                    href="${escapeAttribute(result.url)}"
                    target="_blank">

                    ${escapeHtml(result.url)}

                </a>

            </p>

            <button
                onclick="openWebsiteForReview(
                    '${escapeJs(result.title)}',
                    '${escapeJs(result.url)}',
                    '${escapeJs(result.description || "")}'
                )">

                View Website & Review

            </button>

        </div>

    `;
}


/* =========================================================
   OPEN WEBSITE FOR REVIEW
========================================================= */

function openWebsiteForReview(
    title,
    url,
    description
) {

    const existingWebsite =
        findExistingWebsite(url);

    /*
     * Website already exists in database
     */
    if (existingWebsite) {

        window.location.href =
            "review.html?websiteId=" +
            existingWebsite.id;

        return;
    }


    /*
     * Website does not exist.
     * Automatically save it to database.
     */

    const website = {

        name: title,

        url: url,

        description: description

    };

    fetch(
        WEBSITE_API,
        {

            method: "POST",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(website)

        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Website could not be saved"
                );
            }

            return response.json();

        })

        .then(function(savedWebsite) {

            /*
             * Open review page using
             * newly generated website ID
             */

            window.location.href =
                "review.html?websiteId=" +
                savedWebsite.id;

        })

        .catch(function(error) {

            console.error(error);

            alert(
                "Unable to open website review page."
            );

        });
}


/* =========================================================
   FIND EXISTING WEBSITE
========================================================= */

function findExistingWebsite(url) {

    if (!url || allWebsites.length === 0) {

        return null;
    }

    const searchedHost =
        normalizeWebsiteUrl(url);

    for (
        let i = 0;
        i < allWebsites.length;
        i++
    ) {

        const savedHost =
            normalizeWebsiteUrl(
                allWebsites[i].url
            );

        if (searchedHost === savedHost) {

            return allWebsites[i];

        }
    }

    return null;
}


/* =========================================================
   NORMALIZE WEBSITE URL
========================================================= */

function normalizeWebsiteUrl(url) {

    try {

        const parsedUrl =
            new URL(url);

        let hostname =
            parsedUrl.hostname.toLowerCase();

        if (hostname.startsWith("www.")) {

            hostname =
                hostname.substring(4);

        }

        return hostname;

    } catch (error) {

        return url
            .toLowerCase()
            .replace(/^https?:\/\//, "")
            .replace(/^www\./, "")
            .replace(/\/$/, "");
    }
}


/* =========================================================
   VIEW SAVED WEBSITE
========================================================= */

function viewWebsite(websiteId) {

    window.location.href =
        "review.html?websiteId=" +
        websiteId;
}


/* =========================================================
   SELECT WEBSITE
========================================================= */

function selectWebsite(
    title,
    url,
    description
) {

    document.getElementById(
        "websiteName"
    ).value = title;

    document.getElementById(
        "websiteUrl"
    ).value = url;

    document.getElementById(
        "websiteDescription"
    ).value = description || "";

}


/* =========================================================
   ADD WEBSITE
========================================================= */

function addWebsite(event) {

    event.preventDefault();

    const name =
        document.getElementById(
            "websiteName"
        ).value.trim();

    const url =
        document.getElementById(
            "websiteUrl"
        ).value.trim();

    const description =
        document.getElementById(
            "websiteDescription"
        ).value.trim();

    const message =
        document.getElementById(
            "websiteMessage"
        );

    const website = {

        name: name,

        url: url,

        description: description

    };

    fetch(
        WEBSITE_API,
        {

            method: "POST",

            headers: {

                "Content-Type":
                    "application/json"

            },

            body:
                JSON.stringify(website)

        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Website could not be added"
                );
            }

            return response.json();

        })

        .then(function(data) {

            message.innerHTML = `

                <span class="success-message">

                    Website added successfully!

                </span>

            `;

            document
                .getElementById("websiteForm")
                .reset();

            loadWebsites();

        })

        .catch(function(error) {

            console.error(error);

            message.innerHTML = `

                <span class="error-message">

                    Website could not be added.

                </span>

            `;
        });
}


/* =========================================================
   ESCAPE HTML
========================================================= */

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


/* =========================================================
   ESCAPE ATTRIBUTE
========================================================= */

function escapeAttribute(value) {

    return escapeHtml(value);
}


/* =========================================================
   ESCAPE JAVASCRIPT
========================================================= */

function escapeJs(value) {

    if (value === null || value === undefined) {

        return "";

    }

    return String(value)

        .replace(/\\/g, "\\\\")

        .replace(/'/g, "\\'")

        .replace(/"/g, '\\"')

        .replace(/\n/g, "\\n")

        .replace(/\r/g, "\\r");
}


/* =========================================================
   SEARCH BUTTON
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function() {

        const searchButton =
            document.getElementById(
                "searchButton"
            );

        if (searchButton) {

            searchButton.addEventListener(
                "click",
                searchWebsites
            );

        }


        const searchInput =
            document.getElementById(
                "searchInput"
            );

        if (searchInput) {

            searchInput.addEventListener(
                "keypress",
                function(event) {

                    if (event.key === "Enter") {

                        searchWebsites();

                    }

                }
            );

        }


        /*
         * Add Website form is no longer
         * present in the new index.html.
         * So we only add the listener
         * if it exists.
         */

        const websiteForm =
            document.getElementById(
                "websiteForm"
            );

        if (websiteForm) {

            websiteForm.addEventListener(
                "submit",
                addWebsite
            );

        }


        loadWebsites();

    }
);