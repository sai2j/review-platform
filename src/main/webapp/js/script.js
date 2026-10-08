const WEBSITE_API =
    "/review-platform/websites";

const DISCOVERY_API =
    "/review-platform/discovery/search";

const REVIEW_API =
    "/review-platform/reviews";

const RANKING_API =
    "/review-platform/reviews/ranking";

const METADATA_API =
    "/review-platform/metadata/fetch";

const NOTIFICATION_API =
    "/review-platform/notifications";


let allWebsites = [];

let websiteRankings = [];

const RECENT_WEBSITES_KEY =
    "reviewPlatformRecentWebsites";

const MAX_RECENT_WEBSITES = 5;

const MAX_RECENT_REVIEWS = 4;


/* =========================================================
   HOMEPAGE LOGGED-IN USER
========================================================= */

function getHomepageLoggedInUser() {

    try {

        const savedUser =
            localStorage.getItem(
                "loggedInUser"
            );

        if (!savedUser) {
            return null;
        }

        const user =
            JSON.parse(savedUser);

        return user && user.id
            ? user
            : null;

    } catch (error) {

        console.error(
            "Unable to read logged-in user:",
            error
        );

        return null;
    }
}


/* =========================================================
   NOTIFICATIONS
========================================================= */

function loadNotificationUnreadCount() {

    const loggedInUser =
        getHomepageLoggedInUser();

    if (!loggedInUser) {
        return;
    }

    fetch(
        NOTIFICATION_API +
        "/unread-count"
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Unable to load notification count."
                );
            }

            return response.json();
        })

        .then(function (count) {

            updateNotificationBadge(
                Number(count) || 0
            );
        })

        .catch(function (error) {

            console.error(
                "Unable to load notification count:",
                error
            );
        });
}


function updateNotificationBadge(
    count
) {

    const badge =
        document.getElementById(
            "notificationBadge"
        );

    if (!badge) {
        return;
    }

    const safeCount =
        Number(count) || 0;

    if (safeCount <= 0) {

        badge.style.display =
            "none";

        badge.textContent =
            "0";

        return;
    }

    badge.style.display =
        "block";

    badge.textContent =
        safeCount > 99
            ? "99+"
            : String(safeCount);
}


function loadNotifications() {

    const notificationList =
        document.getElementById(
            "notificationList"
        );

    if (!notificationList) {
        return;
    }

    notificationList.innerHTML = `
        <div class="notification-empty">
            Loading notifications...
        </div>
    `;

    fetch(
        NOTIFICATION_API
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Unable to load notifications."
                );
            }

            return response.json();
        })

        .then(function (notifications) {

            displayNotifications(
                notifications || []
            );
        })

        .catch(function (error) {

            console.error(
                "Unable to load notifications:",
                error
            );

            notificationList.innerHTML = `
                <div class="notification-error">
                    Unable to load notifications.
                </div>
            `;
        });
}


function displayNotifications(
    notifications
) {

    const notificationList =
        document.getElementById(
            "notificationList"
        );

    if (!notificationList) {
        return;
    }

    if (
        !Array.isArray(
            notifications
        ) ||
        notifications.length === 0
    ) {

        notificationList.innerHTML = `
            <div class="notification-empty">
                No notifications.
            </div>
        `;

        updateNotificationBadge(0);

        return;
    }

    notificationList.innerHTML = "";

    notifications.forEach(
        function (notification) {

            const item =
                document.createElement(
                    "div"
                );

            item.className =
                "notification-item";

            if (
                Number(
                    notification.isRead
                ) === 0
            ) {

                item.classList.add(
                    "unread"
                );
            }

            const message =
                document.createElement(
                    "p"
                );

            message.className =
                "notification-message";

            message.textContent =
                notification.message ||
                "You have a new notification.";

            const time =
                document.createElement(
                    "p"
                );

            time.className =
                "notification-time";

            time.textContent =
                formatNotificationDate(
                    notification.createdAt
                );

            item.appendChild(
                message
            );

            item.appendChild(
                time
            );

            item.addEventListener(
                "click",
                function () {

                    if (
                        Number(
                            notification.isRead
                        ) === 0
                    ) {

                        markNotificationAsRead(
                            notification.id,
                            item
                        );
                    }
                }
            );

            notificationList.appendChild(
                item
            );
        }
    );

    loadNotificationUnreadCount();
}


function markNotificationAsRead(
    notificationId,
    notificationElement
) {

    if (!notificationId) {
        return;
    }

    fetch(
        NOTIFICATION_API +
        "/" +
        encodeURIComponent(
            notificationId
        ) +
        "/read",
        {
            method: "PUT"
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Unable to mark notification as read."
                );
            }

            return response.json();
        })

        .then(function () {

            if (
                notificationElement
            ) {

                notificationElement.classList.remove(
                    "unread"
                );
            }

            loadNotificationUnreadCount();
        })

        .catch(function (error) {

            console.error(
                "Unable to mark notification as read:",
                error
            );
        });
}


function markAllNotificationsAsRead() {

    fetch(
        NOTIFICATION_API +
        "/read-all",
        {
            method: "PUT"
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Unable to mark all notifications as read."
                );
            }
        })

        .then(function () {

            const notificationItems =
                document.querySelectorAll(
                    ".notification-item.unread"
                );

            notificationItems.forEach(
                function (item) {

                    item.classList.remove(
                        "unread"
                    );
                }
            );

            updateNotificationBadge(0);
        })

        .catch(function (error) {

            console.error(
                "Unable to mark all notifications as read:",
                error
            );
        });
}


function formatNotificationDate(
    value
) {

    if (!value) {
        return "";
    }

    try {

        const date =
            new Date(value);

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {

            return "";
        }

        return date.toLocaleString(
            "en-IN",
            {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            }
        );

    } catch (error) {

        return "";
    }
}


/* =========================================================
   NOTIFICATION AUTO REFRESH
========================================================= */

setInterval(
    function () {

        const loggedInUser =
            getHomepageLoggedInUser();

        if (
            loggedInUser &&
            loggedInUser.id
        ) {

            loadNotificationUnreadCount();
        }

    },
    30000
);


/* =========================================================
   LOAD RECENT REVIEWS
========================================================= */

function loadRecentReviews() {

    const recentReviewsList =
        document.getElementById(
            "recentReviewsList"
        );

    if (!recentReviewsList) {
        return;
    }

    const loggedInUser =
        getHomepageLoggedInUser();

    if (!loggedInUser) {

        displayDemoRecentReviews();

        return;
    }

    recentReviewsList.innerHTML = `
        <p class="recent-reviews-loading">
            Loading latest reviews...
        </p>
    `;

    Promise.all([

        fetch(
            REVIEW_API +
            "?page=0&size=20&sort=createdAt,desc"
        ),

        fetch(
            WEBSITE_API
        )

    ])

        .then(function (responses) {

            const reviewResponse =
                responses[0];

            const websiteResponse =
                responses[1];

            if (!reviewResponse.ok) {

                throw new Error(
                    "Recent reviews API error"
                );
            }

            if (!websiteResponse.ok) {

                throw new Error(
                    "Website API error"
                );
            }

            return Promise.all([

                reviewResponse.json(),

                websiteResponse.json()

            ]);
        })

        .then(function (data) {

            const reviewData =
                data[0];

            const websites =
                data[1] || [];

            allWebsites =
                websites;

            const reviews =
                reviewData.content || [];

            const approvedReviews =
                reviews
                    .filter(
                        function (review) {

                            return (
                                review.status &&
                                String(
                                    review.status
                                ).toUpperCase() ===
                                "APPROVED"
                            );
                        }
                    )
                    .slice(
                        0,
                        MAX_RECENT_REVIEWS
                    );

            if (
                approvedReviews.length === 0
            ) {

                displayDemoRecentReviews();

                return;
            }

            displayRecentReviews(
                approvedReviews
            );
        })

        .catch(function (error) {

            console.error(
                "Unable to load recent reviews:",
                error
            );

            displayDemoRecentReviews();
        });
}


/* =========================================================
   DISPLAY DEMO RECENT REVIEWS
========================================================= */

function displayDemoRecentReviews() {

    const recentReviewsList =
        document.getElementById(
            "recentReviewsList"
        );

    if (!recentReviewsList) {
        return;
    }

    const demoReviews = [

        {
            userName:
                "Demo User 1",

            rating:
                5,

            comment:
                "Very easy to use and the information was helpful. I found what I was looking for quickly.",

            websiteName:
                "Example Business",

            websiteDomain:
                "example.com"
        },

        {
            userName:
                "Demo User 2",

            rating:
                4,

            comment:
                "The website profile was clear and useful. The review section made it easier to understand the service.",

            websiteName:
                "Sample Store",

            websiteDomain:
                "samplestore.com"
        },

        {
            userName:
                "Demo User 3",

            rating:
                5,

            comment:
                "Good experience overall. I liked having customer feedback available in one place.",

            websiteName:
                "Demo Services",

            websiteDomain:
                "demoservices.com"
        },

        {
            userName:
                "Demo User 4",

            rating:
                4,

            comment:
                "Simple layout and useful information. It was easy to read the customer experiences.",

            websiteName:
                "Test Company",

            websiteDomain:
                "testcompany.com"
        }

    ];

    recentReviewsList.innerHTML =
        "";

    demoReviews.forEach(
        function (review) {

            const rating =
                Math.max(
                    0,
                    Math.min(
                        5,
                        Number(
                            review.rating
                        ) || 0
                    )
                );

            let stars =
                "";

            for (
                let i = 1;
                i <= 5;
                i++
            ) {

                stars += `
                    <span class="recent-review-star ${
                        i <= rating
                            ? ""
                            : "empty"
                    }">
                        ★
                    </span>
                `;
            }

            const initial =
                getWebsiteInitial(
                    review.userName
                );

            recentReviewsList.innerHTML += `
                <div class="recent-review-card">

                    <div class="recent-review-content">

                        <div class="recent-review-user">

                            <div class="recent-review-avatar">
                                ${escapeHtml(
                                    initial
                                )}
                            </div>

                            <div class="recent-review-user-info">

                                <p class="recent-review-user-name">
                                    ${escapeHtml(
                                        review.userName
                                    )}
                                </p>

                                <div class="recent-review-rating">
                                    ${stars}
                                </div>

                            </div>

                        </div>

                        <p class="recent-review-text">
                            ${escapeHtml(
                                review.comment
                            )}
                        </p>

                    </div>

                    <div class="recent-review-website">

                        <div class="recent-review-website-logo">
                            ${escapeHtml(
                                getWebsiteInitial(
                                    review.websiteName
                                )
                            )}
                        </div>

                        <div class="recent-review-website-info">

                            <p class="recent-review-website-name">
                                ${escapeHtml(
                                    review.websiteName
                                )}
                            </p>

                            <p class="recent-review-website-domain">
                                ${escapeHtml(
                                    review.websiteDomain
                                )}
                            </p>

                        </div>

                    </div>

                </div>
            `;
        }
    );
}


/* =========================================================
   DISPLAY RECENT REVIEWS
========================================================= */

function displayRecentReviews(
    reviews
) {

    const recentReviewsList =
        document.getElementById(
            "recentReviewsList"
        );

    if (!recentReviewsList) {
        return;
    }

    const reviewList =
        (reviews || []).slice(
            0,
            MAX_RECENT_REVIEWS
        );

    if (
        reviewList.length === 0
    ) {

        displayDemoRecentReviews();

        return;
    }

    recentReviewsList.innerHTML =
        "";

    reviewList.forEach(
        function (review) {

            const rating =
                Math.max(
                    0,
                    Math.min(
                        5,
                        Number(
                            review.rating
                        ) || 0
                    )
                );

            let stars =
                "";

            for (
                let i = 1;
                i <= 5;
                i++
            ) {

                stars += `
                    <span class="recent-review-star ${
                        i <= rating
                            ? ""
                            : "empty"
                    }">
                        ★
                    </span>
                `;
            }

            const website =
                allWebsites.find(
                    function (item) {

                        return (
                            String(
                                item.id
                            ) ===
                            String(
                                review.websiteId
                            )
                        );
                    }
                );

            const websiteName =
                website &&
                website.name
                    ? website.name
                    : "Website";

            const websiteDomain =
                website
                    ? getWebsiteDomain(
                        website.url
                    )
                    : "";

            const userName =
                review.userId
                    ? "User #" +
                      review.userId
                    : "User";

            const initial =
                getWebsiteInitial(
                    userName
                );

            const reviewComment =
                review.comment ||
                "No review comment.";

            recentReviewsList.innerHTML += `
                <div class="recent-review-card">

                    <div class="recent-review-content">

                        <div class="recent-review-user">

                            <div class="recent-review-avatar">
                                ${escapeHtml(
                                    initial
                                )}
                            </div>

                            <div class="recent-review-user-info">

                                <p class="recent-review-user-name">
                                    ${escapeHtml(
                                        userName
                                    )}
                                </p>

                                <div class="recent-review-rating">
                                    ${stars}
                                </div>

                            </div>

                        </div>

                        <p class="recent-review-text">
                            ${escapeHtml(
                                reviewComment
                            )}
                        </p>

                    </div>

                    <div class="recent-review-website">

                        <div class="recent-review-website-logo">
                            ${escapeHtml(
                                getWebsiteInitial(
                                    websiteName
                                )
                            )}
                        </div>

                        <div class="recent-review-website-info">

                            <p class="recent-review-website-name">
                                ${escapeHtml(
                                    websiteName
                                )}
                            </p>

                            <p class="recent-review-website-domain">
                                ${escapeHtml(
                                    websiteDomain
                                )}
                            </p>

                        </div>

                    </div>

                </div>
            `;
        }
    );
}


/* =========================================================
   INTERNAL SEARCH SEO
========================================================= */

function updateSearchRobotsMeta(
    isSearchPage
) {

    let robotsMeta =
        document.querySelector(
            'meta[name="robots"]'
        );

    if (!robotsMeta) {

        robotsMeta =
            document.createElement(
                "meta"
            );

        robotsMeta.setAttribute(
            "name",
            "robots"
        );

        document.head.appendChild(
            robotsMeta
        );
    }

    if (isSearchPage) {

        robotsMeta.setAttribute(
            "content",
            "noindex, follow"
        );

    } else {

        robotsMeta.setAttribute(
            "content",
            "index, follow"
        );
    }
}


/* =========================================================
   UPDATE SEARCH URL
========================================================= */

function updateSearchUrl(
    keyword
) {

    const url =
        new URL(
            window.location.href
        );

    if (
        keyword &&
        keyword.trim() !== ""
    ) {

        url.searchParams.set(
            "q",
            keyword.trim()
        );

        updateSearchRobotsMeta(
            true
        );

    } else {

        url.searchParams.delete(
            "q"
        );

        updateSearchRobotsMeta(
            false
        );
    }

    window.history.pushState(
        {},
        "",
        url
    );
}


/* =========================================================
   LOAD SAVED WEBSITES + RANKING
========================================================= */

function loadWebsites(
    shouldDisplay = true
) {

    Promise.all([

        fetch(
            WEBSITE_API
        ),

        fetch(
            RANKING_API
        )

    ])

        .then(function (responses) {

            const websiteResponse =
                responses[0];

            const rankingResponse =
                responses[1];

            if (!websiteResponse.ok) {

                throw new Error(
                    "Website API error"
                );
            }

            if (!rankingResponse.ok) {

                throw new Error(
                    "Website ranking API error"
                );
            }

            return Promise.all([

                websiteResponse.json(),

                rankingResponse.json()

            ]);
        })

        .then(function (data) {

            allWebsites =
                data[0] || [];

            websiteRankings =
                data[1] || [];

            if (shouldDisplay) {

                displayWebsites(
                    allWebsites
                );
            }
        })

        .catch(function (error) {

            console.error(
                error
            );

            const websiteList =
                document.getElementById(
                    "websiteList"
                );

            if (
                shouldDisplay &&
                websiteList
            ) {

                websiteList.innerHTML = `
                    <p class="error-message">
                        Unable to load websites.
                    </p>
                `;
            }
        });
}


/* =========================================================
   FIND WEBSITE RANKING
========================================================= */

function getWebsiteRanking(
    websiteId
) {

    return websiteRankings.find(
        function (ranking) {

            return (
                String(
                    ranking.websiteId
                ) ===
                String(
                    websiteId
                )
            );
        }
    ) || null;
}


/* =========================================================
   SORT WEBSITES BY RANKING
========================================================= */

function sortWebsitesByRanking(
    websites
) {

    if (
        !Array.isArray(
            websites
        )
    ) {

        return [];
    }

    return websites
        .map(
            function (website, originalIndex) {

                const ranking =
                    getWebsiteRanking(
                        website.id
                    );

                return {
                    website:
                        website,

                    ranking:
                        ranking,

                    originalIndex:
                        originalIndex
                };
            }
        )
        .sort(
            function (a, b) {

                /*
                 * Ranked websites first.
                 */
                if (
                    a.ranking &&
                    !b.ranking
                ) {

                    return -1;
                }

                if (
                    !a.ranking &&
                    b.ranking
                ) {

                    return 1;
                }

                /*
                 * Both are ranked.
                 * Lower rank number comes first.
                 */
                if (
                    a.ranking &&
                    b.ranking
                ) {

                    const rankA =
                        Number(
                            a.ranking.rank
                        );

                    const rankB =
                        Number(
                            b.ranking.rank
                        );

                    if (
                        rankA !==
                        rankB
                    ) {

                        return (
                            rankA -
                            rankB
                        );
                    }

                    /*
                     * Safety fallback:
                     * higher rating first.
                     */
                    const ratingA =
                        Number(
                            a.ranking.averageRating
                        ) || 0;

                    const ratingB =
                        Number(
                            b.ranking.averageRating
                        ) || 0;

                    if (
                        ratingA !==
                        ratingB
                    ) {

                        return (
                            ratingB -
                            ratingA
                        );
                    }

                    /*
                     * Safety fallback:
                     * higher review count first.
                     */
                    const countA =
                        Number(
                            a.ranking.reviewCount
                        ) || 0;

                    const countB =
                        Number(
                            b.ranking.reviewCount
                        ) || 0;

                    if (
                        countA !==
                        countB
                    ) {

                        return (
                            countB -
                            countA
                        );
                    }
                }

                /*
                 * Preserve original API order
                 * when there is no ranking.
                 */
                return (
                    a.originalIndex -
                    b.originalIndex
                );
            }
        )
        .map(
            function (item) {

                return item.website;
            }
        );
}


/* =========================================================
   DISPLAY SAVED WEBSITES
========================================================= */

function displayWebsites(
    websites
) {

    const websiteList =
        document.getElementById(
            "websiteList"
        );

    if (!websiteList) {
        return;
    }

    websiteList.innerHTML =
        "";

    if (
        !Array.isArray(
            websites
        ) ||
        websites.length === 0
    ) {

        websiteList.innerHTML = `
            <p>
                No websites available.
            </p>
        `;

        return;
    }

    /*
     * IMPORTANT:
     * Display websites according to ranking.
     *
     * Example:
     * #1
     * #2
     * #3
     * #4
     * ...
     */
    const sortedWebsites =
        sortWebsitesByRanking(
            websites
        );

    websiteList.innerHTML += `
        <div class="website-ranking-heading">

            <h2>
                Website Rankings
            </h2>

            <p>
                Ranked by approved reviews, average rating and review count.
            </p>

        </div>
    `;

    sortedWebsites.forEach(
        function (website) {

            const ranking =
                getWebsiteRanking(
                    website.id
                );

            const rankText =
                ranking
                    ? "#" +
                      ranking.rank
                    : "Not ranked";

            const averageRating =
                ranking
                    ? Number(
                        ranking.averageRating
                    ).toFixed(1)
                    : "0.0";

            const reviewCount =
                ranking
                    ? Number(
                        ranking.reviewCount
                    ) || 0
                    : 0;

            websiteList.innerHTML += `
                <div
                    class="website-card"
                    data-website-id="${escapeAttribute(
                        website.id
                    )}"
                >

                    <div class="website-ranking-info">

                        <strong>
                            Rank ${escapeHtml(
                                rankText
                            )}
                        </strong>

                        <span>
                            ★ ${escapeHtml(
                                averageRating
                            )}
                        </span>

                        <span>
                            ${escapeHtml(
                                reviewCount
                            )}
                            Reviews
                        </span>

                    </div>

                    <h3>
                        ${escapeHtml(
                            website.name
                        )}
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
                            href="${escapeAttribute(
                                website.url
                            )}"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            ${escapeHtml(
                                website.url
                            )}
                        </a>

                    </p>

                    <button
                        type="button"
                        class="view-website-button"
                        data-website-id="${escapeAttribute(
                            website.id
                        )}"
                    >
                        View Website & Review
                    </button>

                </div>
            `;
        }
    );
}


/* =========================================================
   SEARCH WEBSITES
========================================================= */

function searchWebsites() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );

    if (!searchInput) {
        return;
    }

    const keyword =
        searchInput.value.trim();

    if (
        keyword === ""
    ) {

        alert(
            "Please enter a website name."
        );

        updateSearchUrl(
            ""
        );

        const websiteList =
            document.getElementById(
                "websiteList"
            );

        if (websiteList) {

            websiteList.innerHTML =
                "";
        }

        return;
    }

    updateSearchUrl(
        keyword
    );

    const websiteList =
        document.getElementById(
            "websiteList"
        );

    if (websiteList) {

        websiteList.innerHTML = `
            <p>
                Searching for
                <strong>
                    ${escapeHtml(
                        keyword
                    )}
                </strong>...
            </p>
        `;
    }

    fetch(
        DISCOVERY_API +
        "?keyword=" +
        encodeURIComponent(
            keyword
        )
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Website search failed"
                );
            }

            return response.json();
        })

        .then(function (results) {

            displaySearchResults(
                results
            );
        })

        .catch(function (error) {

            console.error(
                error
            );

            if (websiteList) {

                websiteList.innerHTML = `
                    <p class="error-message">
                        Unable to search website.
                    </p>
                `;
            }
        });
}


/* =========================================================
   DISPLAY SEARCH RESULT
========================================================= */

function displaySearchResults(
    results
) {

    const websiteList =
        document.getElementById(
            "websiteList"
        );

    if (!websiteList) {
        return;
    }

    websiteList.innerHTML = `
        <h2>
            Website Found
        </h2>
    `;

    if (
        !results ||
        results.length === 0
    ) {

        websiteList.innerHTML += `
            <p>
                No website found.
            </p>
        `;

        return;
    }

    const result =
        results[0];

    websiteList.innerHTML += `
        <p>
            Result for your search
        </p>

        <div class="website-card">

            <h3>
                ${escapeHtml(
                    result.title
                )}
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
                    href="${escapeAttribute(
                        result.url
                    )}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    ${escapeHtml(
                        result.url
                    )}
                </a>

            </p>

            <button
                type="button"
                class="view-website-button search-website-button"
                data-title="${escapeAttribute(
                    result.title
                )}"
                data-url="${escapeAttribute(
                    result.url
                )}"
                data-description="${escapeAttribute(
                    result.description || ""
                )}"
            >
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
        findExistingWebsite(
            url
        );

    if (existingWebsite) {

        saveRecentWebsite(
            existingWebsite
        );

        window.location.href =
            "website.html?websiteId=" +
            existingWebsite.id;

        return;
    }

    const website = {

        name:
            title,

        url:
            url,

        description:
            description
    };

    fetch(
        WEBSITE_API,
        {
            method:
                "POST",

            headers: {
                "Content-Type":
                    "application/json"
            },

            body:
                JSON.stringify(
                    website
                )
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Website could not be saved"
                );
            }

            return response.json();
        })

        .then(function (savedWebsite) {

            window.location.href =
                "website.html?websiteId=" +
                savedWebsite.id;
        })

        .catch(function (error) {

            console.error(
                error
            );

            alert(
                "Unable to open website review page."
            );
        });
}


/* =========================================================
   FIND EXISTING WEBSITE
========================================================= */

function findExistingWebsite(
    url
) {

    if (
        !url ||
        allWebsites.length === 0
    ) {

        return null;
    }

    const searchedHost =
        normalizeWebsiteUrl(
            url
        );

    for (
        let i = 0;
        i < allWebsites.length;
        i++
    ) {

        const savedHost =
            normalizeWebsiteUrl(
                allWebsites[i].url
            );

        if (
            searchedHost ===
            savedHost
        ) {

            return allWebsites[i];
        }
    }

    return null;
}


/* =========================================================
   NORMALIZE WEBSITE URL
========================================================= */

function normalizeWebsiteUrl(
    url
) {

    try {

        const parsedUrl =
            new URL(
                url
            );

        let hostname =
            parsedUrl.hostname.toLowerCase();

        if (
            hostname.startsWith(
                "www."
            )
        ) {

            hostname =
                hostname.substring(
                    4
                );
        }

        return hostname;

    } catch (error) {

        return url
            .toLowerCase()
            .replace(
                /^https?:\/\//,
                ""
            )
            .replace(
                /^www\./,
                ""
            )
            .replace(
                /\/$/,
                ""
            );
    }
}


/* =========================================================
   VIEW SAVED WEBSITE
========================================================= */

function viewWebsite(
    websiteId
) {

    const website =
        allWebsites.find(
            function (item) {

                return (
                    String(
                        item.id
                    ) ===
                    String(
                        websiteId
                    )
                );
            }
        );

    if (website) {

        saveRecentWebsite(
            website
        );
    }

    window.location.href =
        "website.html?websiteId=" +
        websiteId;
}


/* =========================================================
   RECENTLY VISITED WEBSITES
========================================================= */

function saveRecentWebsite(
    website
) {

    if (
        !website ||
        !website.id
    ) {

        return;
    }

    let recentWebsites = [];

    try {

        const saved =
            localStorage.getItem(
                RECENT_WEBSITES_KEY
            );

        if (saved) {

            recentWebsites =
                JSON.parse(
                    saved
                );
        }

    } catch (error) {

        console.error(
            "Unable to read recent websites.",
            error
        );

        recentWebsites =
            [];
    }

    if (
        !Array.isArray(
            recentWebsites
        )
    ) {

        recentWebsites =
            [];
    }

    recentWebsites =
        recentWebsites.filter(
            function (item) {

                return (
                    String(
                        item.id
                    ) !==
                    String(
                        website.id
                    )
                );
            }
        );

    recentWebsites.unshift(
        website
    );

    recentWebsites =
        recentWebsites.slice(
            0,
            MAX_RECENT_WEBSITES
        );

    try {

        localStorage.setItem(
            RECENT_WEBSITES_KEY,
            JSON.stringify(
                recentWebsites
            )
        );

    } catch (error) {

        console.error(
            "Unable to save recent websites.",
            error
        );
    }
}


function getRecentWebsites() {

    try {

        const saved =
            localStorage.getItem(
                RECENT_WEBSITES_KEY
            );

        if (!saved) {
            return [];
        }

        const recentWebsites =
            JSON.parse(
                saved
            );

        if (
            !Array.isArray(
                recentWebsites
            )
        ) {

            return [];
        }

        return recentWebsites.slice(
            0,
            MAX_RECENT_WEBSITES
        );

    } catch (error) {

        console.error(
            "Unable to load recent websites.",
            error
        );

        return [];
    }
}


function getWebsiteInitial(
    name
) {

    if (
        !name ||
        name.trim() === ""
    ) {

        return "W";
    }

    return name
        .trim()
        .charAt(
            0
        )
        .toUpperCase();
}


function getWebsiteDomain(
    url
) {

    if (
        !url ||
        url.trim() === ""
    ) {

        return "";
    }

    try {

        const parsedUrl =
            new URL(
                url
            );

        return parsedUrl.hostname.replace(
            /^www\./,
            ""
        );

    } catch (error) {

        return url;
    }
}


function displayRecentWebsites() {

    const continueList =
        document.getElementById(
            "continueList"
        );

    if (!continueList) {
        return;
    }

    const recentWebsites =
        getRecentWebsites();

    if (
        recentWebsites.length === 0
    ) {

        continueList.innerHTML = `
            <p class="recent-empty-message">
                No recently visited websites.
            </p>
        `;

        return;
    }

    continueList.innerHTML =
        "";

    recentWebsites.forEach(
        function (website) {

            continueList.innerHTML += `
                <div class="continue-card">

                    <div class="website-logo-placeholder">
                        ${escapeHtml(
                            getWebsiteInitial(
                                website.name
                            )
                        )}
                    </div>

                    <div class="continue-card-content">

                        <h3>
                            ${escapeHtml(
                                website.name
                            )}
                        </h3>

                        <p class="website-domain">
                            ${escapeHtml(
                                getWebsiteDomain(
                                    website.url
                                )
                            )}
                        </p>

                        <button
                            type="button"
                            class="continue-button"
                            data-recent-website-id="${escapeAttribute(
                                website.id
                            )}"
                        >
                            View Website
                        </button>

                    </div>

                </div>
            `;
        }
    );
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
    ).value =
        title;

    document.getElementById(
        "websiteUrl"
    ).value =
        url;

    document.getElementById(
        "websiteDescription"
    ).value =
        description || "";
}


/* =========================================================
   ADD WEBSITE
========================================================= */

function addWebsite(
    event
) {

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

        name:
            name,

        url:
            url,

        description:
            description
    };

    fetch(
        WEBSITE_API,
        {
            method:
                "POST",

            headers: {
                "Content-Type":
                    "application/json"
            },

            body:
                JSON.stringify(
                    website
                )
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Website could not be added"
                );
            }

            return response.json();
        })

        .then(function (data) {

            if (message) {

                message.innerHTML = `
                    <span class="success-message">
                        Website added successfully!
                    </span>
                `;
            }

            const websiteForm =
                document.getElementById(
                    "websiteForm"
                );

            if (websiteForm) {

                websiteForm.reset();
            }

            loadWebsites();
        })

        .catch(function (error) {

            console.error(
                error
            );

            if (message) {

                message.innerHTML = `
                    <span class="error-message">
                        Website could not be added.
                    </span>
                `;
            }
        });
}


/* =========================================================
   ESCAPE HTML
========================================================= */

function escapeHtml(
    value
) {

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


/* =========================================================
   ESCAPE ATTRIBUTE
========================================================= */

function escapeAttribute(
    value
) {

    return escapeHtml(
        value
    );
}


/* =========================================================
   ESCAPE JAVASCRIPT
========================================================= */

function escapeJs(
    value
) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";
    }

    return String(value)

        .replace(
            /\\/g,
            "\\\\"
        )

        .replace(
            /'/g,
            "\\'"
        )

        .replace(
            /"/g,
            '\\"'
        )

        .replace(
            /\n/g,
            "\\n"
        )

        .replace(
            /\r/g,
            "\\r"
        );
}


/* =========================================================
   HANDLE URL SEARCH
========================================================= */

function loadSearchFromUrl() {

    const urlParams =
        new URLSearchParams(
            window.location.search
        );

    const query =
        urlParams.get(
            "q"
        );

    const searchInput =
        document.getElementById(
            "searchInput"
        );

    if (
        query &&
        query.trim() !== ""
    ) {

        updateSearchRobotsMeta(
            true
        );

        if (searchInput) {

            searchInput.value =
                query;
        }

        searchWebsites();

    } else {

        updateSearchRobotsMeta(
            false
        );
    }
}


/* =========================================================
   BROWSER BACK / FORWARD
========================================================= */

window.addEventListener(
    "popstate",
    function () {

        loadSearchFromUrl();
    }
);


/* =========================================================
   SEARCH BUTTON + WEBSITE BUTTONS
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

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
                function (event) {

                    if (
                        event.key ===
                        "Enter"
                    ) {

                        searchWebsites();
                    }
                }
            );
        }


        /* =================================================
           WEBSITES NAVBAR BUTTON
        ================================================= */

        const websitesNavLink =
            document.getElementById(
                "websitesNavLink"
            );

        if (websitesNavLink) {

            websitesNavLink.addEventListener(
                "click",
                function () {

                    /*
                     * Load fresh website ranking
                     * when user clicks Websites.
                     */
                    loadWebsites(
                        true
                    );
                }
            );
        }


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


        const websiteList =
            document.getElementById(
                "websiteList"
            );

        if (websiteList) {

            websiteList.addEventListener(
                "click",
                function (event) {

                    const button =
                        event.target.closest(
                            ".view-website-button"
                        );

                    if (!button) {
                        return;
                    }

                    const websiteId =
                        button.dataset.websiteId;

                    if (websiteId) {

                        viewWebsite(
                            websiteId
                        );

                        return;
                    }

                    const title =
                        button.dataset.title;

                    const url =
                        button.dataset.url;

                    const description =
                        button.dataset.description ||
                        "";

                    if (
                        title &&
                        url
                    ) {

                        openWebsiteForReview(
                            title,
                            url,
                            description
                        );
                    }
                }
            );
        }


        const continueList =
            document.getElementById(
                "continueList"
            );

        if (continueList) {

            continueList.addEventListener(
                "click",
                function (event) {

                    const button =
                        event.target.closest(
                            ".continue-button"
                        );

                    if (!button) {
                        return;
                    }

                    const websiteId =
                        button.dataset.recentWebsiteId;

                    if (websiteId) {

                        viewWebsite(
                            websiteId
                        );
                    }
                }
            );
        }


        displayRecentWebsites();

        loadSearchFromUrl();

        /*
         * Load websites and ranking in background.
         * They will display when the Websites
         * navbar is clicked.
         */
        loadWebsites(false);

        loadRecentReviews();
    }
);