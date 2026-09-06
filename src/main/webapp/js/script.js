const WEBSITE_API = "/review-platform/websites";
const REVIEW_API = "/review-platform/reviews";
let allWebsites = [];
function loadWebsites() {
    fetch(WEBSITE_API)
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Website API Error");
            }
            return response.json();
        })
        .then(function(websites) {
            allWebsites = websites;
            displayWebsites(websites);
        })
        .catch(function(error) {
            console.error(error);
            document.getElementById("websiteList").innerHTML =
                `<p class="error-message">
                    Unable to load websites.
                </p>`;
        });
}
function displayWebsites(websites) {
    const websiteList =
        document.getElementById("websiteList");
    websiteList.innerHTML = "";
    if (websites.length === 0) {
        websiteList.innerHTML = `
            <div class="no-websites">
                <h3>No Websites Found</h3>
                <p>
                    Add a website to get started.
                </p>
            </div>
        `;
        return;
    }
    websites.forEach(function(website) {
        websiteList.innerHTML += `
            <div class="website-card">
                <h3>${website.name}</h3>
                <p>
                    ${website.description || ""}
                </p>
                <p>
                    <strong>URL:</strong>
                    ${website.url}
                </p>
                <div id="latest-review-${website.id}">
                    Loading reviews...
                </div>
                <br>
                <a class="review-button"
                    href="review.html?websiteId=${website.id}">
                    View Reviews
                </a>
                <br>
                <br>
                <a class="claim-button"
                    href="business-claim.html?businessId=${website.id}">
                    Claim Business
                </a>
            </div>
        `;
        loadLatestReview(website.id);
    });
}
function loadLatestReview(websiteId) {
    fetch(
        REVIEW_API +
        "/website/" +
        websiteId
    )
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Review API Error");
            }
            return response.json();
        })
        .then(function(reviews) {
            const reviewBox =
                document.getElementById(
                    "latest-review-" + websiteId
                );
            if (reviews.length === 0) {
                reviewBox.innerHTML =
                    `<p>No reviews yet.</p>`;
                return;
            }
            const latestReview =
                reviews[reviews.length - 1];
            reviewBox.innerHTML = `
                <div class="home-review">
                    <strong>
                        ${latestReview.rating}/5 ⭐
                    </strong>
                    <p>
                        ${latestReview.comment || ""}
                    </p>
                </div>
            `;
        })
        .catch(function(error) {
            console.error(error);
            const reviewBox =
                document.getElementById(
                    "latest-review-" + websiteId
                );
            if (reviewBox) {
                reviewBox.innerHTML =
                    `<p>No reviews available.</p>`;
            }
        });
}
function searchWebsites() {
    const searchInput =
        document.getElementById("searchInput");
    const searchText =
        searchInput.value.trim().toLowerCase();
    const filteredWebsites =
        allWebsites.filter(function(website) {
            return (
                website.name
                    .toLowerCase()
                    .includes(searchText)
                ||
                (website.description || "")
                    .toLowerCase()
                    .includes(searchText)
            );
        });
    displayWebsites(filteredWebsites);
}
document
    .getElementById("searchButton")
    .addEventListener(
        "click",
        searchWebsites
    );
document
    .getElementById("searchInput")
    .addEventListener(
        "keyup",
        function(event) {
            if (event.key === "Enter") {
                searchWebsites();
            }
        }
    );

document.getElementById("websiteForm")
    .addEventListener(
        "submit",
        function(event) {
            event.preventDefault();
            const name =document.getElementById("websiteName").value.trim();
            const url =document.getElementById("websiteUrl").value.trim();
            const description =document.getElementById("websiteDescription").value.trim();
            const message =document.getElementById(
                    "websiteMessage"
                );
            if (
                name === "" ||
                url === "" ||
                description === ""
            ) {
                message.innerHTML =
                    `<span class="error-message">
                        Please fill all fields.
                    </span>`;
                return;
            }
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
                    console.log(
                        "Website Added:",
                        data
                    );
                    message.innerHTML =
                        `<span class="success-message">
                            Website added successfully!
                        </span>`;
                    document
                        .getElementById("websiteForm")
                        .reset();
                    loadWebsites();
                })
                .catch(function(error) {
                    console.error(error);
                    message.innerHTML =
                        `<span class="error-message">
                            Website could not be added.
                        </span>`;
                });
        }
    );
document.addEventListener(
    "DOMContentLoaded",
    function() {
        loadWebsites();
    }
);