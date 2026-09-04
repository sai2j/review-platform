const WEBSITE_API = "/review-platform/websites";
function loadWebsites() {
    fetch(WEBSITE_API).then(response => {
        if (!response.ok) {
            throw new Error("Website API error");
        }
        return response.json();
    })
        .then(websites => {
            displayWebsites(websites);
        })
        .catch(error => {
            console.error(error);
            document.getElementById("websiteList").innerHTML = `
                <p class="text-danger">
                    Unable to load websites.
                </p>
            `;
        });
}
function displayWebsites(websites) {
    const websiteList =
        document.getElementById("websiteList");
    websiteList.innerHTML = "";
    if (websites.length === 0) {
        websiteList.innerHTML = `
            <div class="col-12">
                <p class="text-muted">
                    No websites available.
                </p>
            </div>
        `;
        return;
    }
    websites.forEach(website => {
        websiteList.innerHTML += `
            <div class="col-md-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">
                            ${website.name}
                        </h5>
                        <p class="card-text">
                            ${website.description || ""}
                        </p>
                        <p class="text-muted">
                            ${website.url}
                        </p>
                        <button
                            class="btn btn-primary"
                            onclick="openWebsite(${website.id})">
                            View Reviews
                        </button>
                    </div>
                </div>
            </div>
        `;
    });
}
function searchWebsite() {
    const searchText =
        document.getElementById("searchInput")
            .value
            .trim()
            .toLowerCase();
    fetch(WEBSITE_API)
        .then(response => response.json())
        .then(websites => {
            if (searchText === "") {
                displayWebsites(websites);
                return;
            }
            const result =
                websites.filter(website => {
                    const name =
                        (website.name || "")
                            .toLowerCase();
                    const url =
                        (website.url || "")
                            .toLowerCase();
                    const description =
                        (website.description || "")
                            .toLowerCase();
                    return name.includes(searchText)
                        || url.includes(searchText)
                        || description.includes(searchText);
                });
            displayWebsites(result);
        })
        .catch(error => {
            console.error(error);
        });
}
document.getElementById("websiteForm")
    .addEventListener("submit", function(event) {
        event.preventDefault();
        const website = {
            name:
                document.getElementById("websiteName")
                    .value
                    .trim(),
            url:
                document.getElementById("websiteUrl")
                    .value
                    .trim(),
            description:
                document.getElementById("websiteDescription")
                    .value
                    .trim()
        };
        fetch(WEBSITE_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(website)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(
                        "Website could not be added"
                    );
                }
                return response.json();
            })
            .then(data => {
                document.getElementById(
                    "websiteMessage"
                ).innerHTML = `
                <span class="text-success">
                    Website added successfully.
                </span>
            `;
                document.getElementById(
                    "websiteForm"
                ).reset();
                loadWebsites();
            })
            .catch(error => {
                console.error(error);
                document.getElementById(
                    "websiteMessage"
                ).innerHTML = `
                <span class="text-danger">
                    Website could not be added.
                </span>
            `;
            });
    });
function openWebsite(websiteId) {
    console.log(
        "Selected Website ID:",
        websiteId
    );
}
document.addEventListener(
    "DOMContentLoaded",
    function() {
        loadWebsites();
    }
);