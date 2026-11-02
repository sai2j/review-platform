/* =========================================================
   REVIEW PAGE EXTRAS
   Adds (without touching the main script in review.html):
   - square green stars, score label (Excellent / Good ...)
   - claimed / paid badges, company details, contact info
   - review summary (50+ words), topic cards (20-25 words),
     "Reviews shaping this summary" carousel
   - sticky tabs bar (shows after the header scrolls away)
   - sticky left filter panel next to the review list
   - keyword search, top mentions, more filters
   - "People also looked at"
   Load AFTER the main <script> block in review.html.
========================================================= */

(function () {
    "use strict";

    var keyword = "";
    var mentionsBuilt = false;
    var similarLoaded = false;
    var seenReviewIds = {};
    var onlyVerified = false;
    var onlyReplied = false;

    var STOP = ("this that with have from they them were what when your very just also been will would " +
        "there their about which than then these those into over some more much only other after before " +
        "really because could should while where being such even still well good great").split(" ");

    function $(id) {
        return document.getElementById(id);
    }

    function esc(value) {
        return window.escapeHtml ? window.escapeHtml(value) : String(value == null ? "" : value);
    }

    function fmt(number) {
        return Number(number || 0).toLocaleString("en-IN");
    }

    function wordsOf(text) {
        return String(text || "").trim().split(/\s+/).filter(Boolean);
    }

    function clip(text, limit) {
        var list = wordsOf(text);
        return list.length <= limit ? list.join(" ") : list.slice(0, limit).join(" ") + "...";
    }

    function joinList(items) {
        if (items.length <= 1) { return items.join(""); }
        return items.slice(0, -1).join(", ") + " and " + items[items.length - 1];
    }

    function cap(word) {
        return word.charAt(0).toUpperCase() + word.slice(1);
    }

    function getReviews() {
        return typeof allReviews !== "undefined" ? allReviews : [];
    }

    function getReplies() {
        return typeof businessResponses !== "undefined" ? businessResponses : {};
    }

    function getWebsite() {
        return typeof currentWebsiteForStructuredData !== "undefined" ? currentWebsiteForStructuredData : null;
    }

    function reviewerName(review) {
        return String(review.userName || review.username || review.reviewerName || "Customer");
    }

    /* ---------- Square stars (green boxes, like Trustpilot) ---------- */

    function squareStars(score) {

        var value = Number(score) || 0;
        var html = "";

        for (var i = 1; i <= 5; i++) {
            var fill = value >= i ? 100 : (value > i - 1 ? Math.round((value - (i - 1)) * 100) : 0);
            html += '<span class="sq" style="--fill:' + fill + '%"><svg viewBox="0 0 24 24" aria-hidden="true">' +
                '<path d="M12 2.5l2.9 6.3 6.9.8-5.1 4.7 1.4 6.8L12 17.7 5.9 21.1l1.4-6.8L2.2 9.6l6.9-.8z"/></svg></span>';
        }

        return '<span class="sq-row" role="img" aria-label="' + value.toFixed(1) + ' out of 5 stars">' + html + "</span>";
    }

    function relativeDate(review) {

        var value = review.createdAt || review.reviewDate || review.date;

        if (!value) { return ""; }

        var time = Array.isArray(value) ? new Date(value[0], (value[1] || 1) - 1, value[2] || 1).getTime() : Date.parse(value);

        if (isNaN(time)) { return ""; }

        var days = Math.floor((Date.now() - time) / 86400000);

        if (days <= 0) { return "Today"; }
        if (days === 1) { return "1 day ago"; }
        if (days < 7) { return days + " days ago"; }
        if (days < 14) { return "1 week ago"; }
        if (days < 30) { return Math.floor(days / 7) + " weeks ago"; }
        if (days < 60) { return "1 month ago"; }
        if (days < 365) { return Math.floor(days / 30) + " months ago"; }

        return new Date(time).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });
    }

    function reviewTime(review) {
        var value = review.createdAt || review.reviewDate || review.date;
        if (!value) { return 0; }
        var time = Array.isArray(value) ? new Date(value[0], (value[1] || 1) - 1, value[2] || 1).getTime() : Date.parse(value);
        return isNaN(time) ? 0 : time;
    }

    /* ---------- Score label + rating panels ---------- */

    function scoreLabel(average, total) {

        if (!total) { return "No reviews yet"; }
        if (average >= 4.5) { return "Excellent"; }
        if (average >= 3.5) { return "Good"; }
        if (average >= 2.5) { return "Average"; }
        if (average >= 1.5) { return "Poor"; }

        return "Bad";
    }

    function decorateSummary(summary) {

        var average = Number(summary.averageRating || 0);
        var total = Number(summary.totalReview || 0);
        var label = scoreLabel(average, total);

        var averageBox = $("averageRating");

        if (averageBox) {

            var badge = $("rgScoreLabel");

            if (!badge) {
                badge = document.createElement("div");
                badge.id = "rgScoreLabel";
                badge.className = "rg-label";
                averageBox.insertAdjacentElement("afterend", badge);
            }

            badge.textContent = label;
        }

        var head = $("headRating");

        if (head) {
            head.innerHTML = "<span>Reviews " + fmt(total) + "</span>" + squareStars(average) +
                "<strong>" + average.toFixed(1) + "</strong>";
        }

        var big = $("scoreStars");

        if (big) { big.innerHTML = squareStars(average); }

        var counts = {
            five: Number(summary.fivestar != null ? summary.fivestar : (summary.fiveStar || 0)),
            four: Number(summary.fourStar || 0),
            three: Number(summary.threeStar || 0),
            two: Number(summary.twoStar || 0),
            one: Number(summary.oneStar || 0)
        };

        function pctText(count) {
            if (!total) { return "0%"; }
            var pct = Math.round((count / total) * 100);
            return count > 0 && pct === 0 ? "<1%" : pct + "%";
        }

        Object.keys(counts).forEach(function (name) {

            var el = $(name + "StarCount");

            if (!el) { return; }

            el.textContent = pctText(counts[name]);
            el.title = counts[name] + (counts[name] === 1 ? " review" : " reviews");
        });

        /* left panel next to the review list */
        if ($("arBig")) { $("arBig").textContent = average.toFixed(1); }
        if ($("arLabel")) { $("arLabel").innerHTML = squareStars(average) + "<span>" + esc(label) + "</span>"; }
        if ($("arCount")) { $("arCount").textContent = "(" + fmt(total) + ")"; }

        var bars = $("arBars");

        if (bars) {
            var order = [["five", 5], ["four", 4], ["three", 3], ["two", 2], ["one", 1]];

            bars.innerHTML = order.map(function (item) {
                var count = counts[item[0]];
                var width = total ? Math.round((count / total) * 100) : 0;

                return '<button type="button" class="ar-row" data-v="' + item[1] + '" aria-pressed="false">' +
                    '<span class="ar-box"></span><span class="ar-name">' + item[1] + "-star</span>" +
                    '<span class="ar-track"><i style="width:' + width + '%"></i></span>' +
                    '<span class="ar-pct">' + pctText(count) + "</span></button>";
            }).join("");

            syncRatingFilter();
        }

        buildNotices();
    }

    /* ---------- Rating filter (left panel + top card) ---------- */

    function currentFilter() {
        return typeof reviewFilter !== "undefined" ? Number(reviewFilter) : 0;
    }

    function syncRatingFilter() {

        var active = currentFilter();

        document.querySelectorAll(".ar-row").forEach(function (row) {
            var on = Number(row.dataset.v) === active;
            row.classList.toggle("on", on);
            row.setAttribute("aria-pressed", String(on));
        });
    }

    function toggleRating(value, scroll) {

        var next = currentFilter() === value ? 0 : value;
        var chip = document.querySelector('.filter-chip[data-filter="' + next + '"]');

        if (chip) { chip.click(); }

        syncRatingFilter();

        if (scroll) {
            var target = $("allReviews");
            if (target) { target.scrollIntoView({ behavior: "smooth", block: "start" }); }
        }
    }

    function wireRatingRows() {

        document.addEventListener("click", function (event) {
            var row = event.target.closest(".ar-row");

            if (row) { toggleRating(Number(row.dataset.v), false); }
        });

        document.querySelectorAll(".rating-row").forEach(function (row, index) {

            row.classList.add("rg-clickable");
            row.setAttribute("role", "button");
            row.setAttribute("tabindex", "0");
            row.title = "Show only " + (5 - index) + " star reviews";

            function go() { toggleRating(5 - index, true); }

            row.addEventListener("click", go);
            row.addEventListener("keydown", function (event) {
                if (event.key === "Enter" || event.key === " ") { event.preventDefault(); go(); }
            });
        });

        var write = $("arWrite");

        if (write) {
            write.addEventListener("click", function () {
                var main = $("writeReviewButton");
                if (main) { main.click(); }
            });
        }
    }

    /* ---------- Sticky tabs bar (appears after the header scrolls away) ---------- */

    function buildTabs() {

        var header = document.querySelector(".profile-header");

        if (!header || $("rgTabs")) { return; }

        var tabs = [
            { label: "Summary", target: "#rvSummary" },
            { label: "About", target: "#aboutCard" },
            { label: "Reviews", target: "#allReviews" }
        ];

        var nav = document.createElement("nav");
        nav.id = "rgTabs";
        nav.className = "rg-tabs";
        nav.setAttribute("aria-label", "Page sections");

        nav.innerHTML = '<div class="rg-tabs-in"><div class="rg-tab-list">' +
            tabs.map(function (tab, index) {
                return '<button type="button" class="rg-tab' + (index === 0 ? " active" : "") +
                    '" data-target="' + tab.target + '">' + tab.label + "</button>";
            }).join("") +
            '</div><div class="rg-tab-actions"><a class="rg-tab-visit" id="rgTabVisit" target="_blank" rel="noopener noreferrer" hidden>Visit website</a>' +
            '<button type="button" class="rg-tab-write" id="rgTabWrite">Write a review</button></div></div>';

        document.body.appendChild(nav);

        nav.addEventListener("click", function (event) {

            var button = event.target.closest(".rg-tab");

            if (!button) { return; }

            var target = document.querySelector(button.dataset.target);

            if (target) { target.scrollIntoView({ behavior: "smooth", block: "start" }); }
        });

        $("rgTabWrite").addEventListener("click", function () {
            var main = $("writeReviewButton");
            if (main) { main.click(); }
        });

        if (!("IntersectionObserver" in window)) {
            nav.classList.add("stuck");
            return;
        }

        new IntersectionObserver(function (entries) {
            nav.classList.toggle("stuck", !entries[0].isIntersecting);
        }, { rootMargin: "-80px 0px 0px 0px" }).observe(header);

        var observer = new IntersectionObserver(function (entries) {

            entries.forEach(function (entry) {

                if (!entry.isIntersecting) { return; }

                nav.querySelectorAll(".rg-tab").forEach(function (button) {
                    button.classList.toggle("active",
                        document.querySelector(button.dataset.target) === entry.target);
                });
            });

        }, { rootMargin: "-35% 0px -55% 0px" });

        tabs.forEach(function (tab) {

            var el = document.querySelector(tab.target);

            if (el) { observer.observe(el); }
        });
    }

    /* ---------- Search + top mentions + more filters ---------- */

    function buildSearchBox() {

        var heading = document.querySelector(".reviews-heading");

        if (!heading || $("rgTools")) { return; }

        var box = document.createElement("div");
        box.id = "rgTools";
        box.className = "rg-tools";

        box.innerHTML =
            '<label class="rg-search">' +
                '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" ' +
                    'stroke-width="2.2" stroke-linecap="round" aria-hidden="true">' +
                    '<circle cx="11" cy="11" r="7"></circle><path d="M20 20l-4-4"></path></svg>' +
                '<input id="rgSearchInput" type="search" placeholder="Search by keyword" ' +
                    'aria-label="Search reviews" autocomplete="off">' +
            "</label>" +
            '<div id="rgMentions" class="rg-mentions" hidden></div>';

        var tools = heading.querySelector(".reviews-tools");

        if (tools) {
            tools.insertAdjacentElement("beforebegin", box);
        } else {
            heading.insertAdjacentElement("beforeend", box);
        }

        $("rgSearchInput").addEventListener("input", function (event) {
            applyKeyword(event.target.value);
        });
    }

    function applyKeyword(value) {

        keyword = String(value || "").trim().toLowerCase();

        if (typeof visibleReviewCount !== "undefined") {
            visibleReviewCount = REVIEWS_PER_PAGE;
        }

        document.querySelectorAll(".rg-chip").forEach(function (chip) {
            chip.classList.toggle("active", chip.dataset.word === keyword);
        });

        window.displayReviews();
    }

    function countWords(reviews) {

        var stats = {};

        reviews.forEach(function (review) {

            var seen = {};
            var list = String(review.comment || "").toLowerCase().match(/[a-z]{4,}/g) || [];

            list.forEach(function (word) {

                if (STOP.indexOf(word) > -1 || seen[word]) { return; }

                seen[word] = true;
                stats[word] = stats[word] || { count: 0, sum: 0 };
                stats[word].count++;
                stats[word].sum += Number(review.rating) || 0;
            });
        });

        return stats;
    }

    function buildMentions() {

        var box = $("rgMentions");
        var reviews = getReviews();

        if (!box || mentionsBuilt || !reviews.length) { return; }

        mentionsBuilt = true;

        var stats = countWords(reviews);

        var top = Object.keys(stats)
            .filter(function (word) { return stats[word].count >= 2; })
            .sort(function (a, b) { return stats[b].count - stats[a].count; })
            .slice(0, 8);

        if (!top.length) { return; }

        box.hidden = false;

        box.innerHTML = "<span>Top mentions:</span>" + top.map(function (word) {
            return '<button type="button" class="rg-chip" data-word="' + esc(word) + '">' + esc(cap(word)) + "</button>";
        }).join("");

        box.addEventListener("click", function (event) {

            var chip = event.target.closest(".rg-chip");

            if (!chip) { return; }

            var input = $("rgSearchInput");
            var next = chip.classList.contains("active") ? "" : chip.dataset.word;

            input.value = next;
            applyKeyword(next);
        });
    }

    function buildMoreFilters() {

        var tools = document.querySelector(".reviews-tools");

        if (!tools || $("rgMore")) { return; }

        var box = document.createElement("div");
        box.id = "rgMore";
        box.className = "rg-more";
        box.innerHTML = '<label><input type="checkbox" id="rgVerified"> Verified only</label>' +
            '<label><input type="checkbox" id="rgReplied"> With company reply</label>';

        tools.insertAdjacentElement("afterend", box);

        box.addEventListener("change", function () {
            onlyVerified = $("rgVerified").checked;
            onlyReplied = $("rgReplied").checked;
            if (typeof visibleReviewCount !== "undefined") { visibleReviewCount = REVIEWS_PER_PAGE; }
            window.displayReviews();
        });
    }

    /* ---------- Review cards ---------- */

    function decorateCards() {

        document.querySelectorAll("#reviewList .review-card").forEach(function (card, index) {

            var id = card.dataset.reviewId;

            if (seenReviewIds[id]) {
                card.classList.add("rg-static");
            } else {
                card.style.animationDelay = (index * 0.07) + "s";
                seenReviewIds[id] = true;
            }

            var starsEl = card.querySelector(".rating .stars");
            var ratingEl = card.querySelector(".rating strong");

            if (starsEl && !starsEl.dataset.sq) {
                starsEl.dataset.sq = "1";
                starsEl.innerHTML = squareStars(parseFloat(ratingEl ? ratingEl.textContent : 0));
                if (ratingEl) { ratingEl.remove(); }
            }

            var reply = card.querySelector(".business-response strong");

            if (reply) { reply.textContent = "Company replied"; }

            var review = getReviews().find(function (item) {
                return String(item.id) === String(id);
            });

            var status = review ? String(review.verificationStatus || "").toUpperCase() : "";
            var head = card.querySelector(".review-head");

            if (head && status === "VERIFIED" && !head.querySelector(".rg-tag")) {
                var menu = head.querySelector(".menu-wrap");
                var tag = '<span class="rg-tag">Verified</span>';

                if (menu) { menu.insertAdjacentHTML("beforebegin", tag); }
                else { head.insertAdjacentHTML("beforeend", tag); }
            }

            var dateEl = card.querySelector(".review-date");

            if (review && dateEl) {
                var rel = relativeDate(review);
                if (rel) { dateEl.textContent = rel; }
            }
        });

        if (keyword && document.querySelector("#reviewList .no-review")) {
            document.querySelector("#reviewList .no-review").textContent = "No reviews match your search.";
        }
    }

    /* ---------- Header badges, company details, contact info ---------- */

    function decorateDetails() {

        var website = getWebsite();

        if (!website) { return; }

        /* Claimed / Paid badges (small box above the name) */
        var badges = $("phBadges");

        if (badges) {
            badges.innerHTML =
                '<span class="ph-badge ' + (website.claimed ? "ok" : "") + '">' +
                    (website.claimed ? "&#10003; Claimed profile" : "Unclaimed profile") + "</span>" +
                (website.paid ? '<span class="ph-badge paid">&#9733; Paid subscription</span>' : "");
        }

        var slot = $("verifiedSlot");

        if (slot && website.verified && !slot.firstChild) {
            slot.innerHTML = '<span class="rv-verified"><svg viewBox="0 0 24 24" width="13" height="13" aria-hidden="true">' +
                '<path d="M9.5 16.2L5.3 12l-1.4 1.4 5.6 5.6L21 7.5l-1.4-1.4z" fill="currentColor"/></svg> Verified</span>';
        }

        var visit = $("rgTabVisit");

        if (visit && website.url) {
            visit.href = $("visitWebsiteButton") ? $("visitWebsiteButton").href : website.url;
            visit.hidden = false;
        }

        /* Company details */
        var cats = $("aboutCats");

        if (cats) {
            cats.innerHTML = website.category
                ? '<a class="ph-cat" href="category.html?category=' + encodeURIComponent(website.category) + '">' + esc(website.category) + "</a>"
                : "";
        }

        var about = $("aboutText");

        if (about) {
            var text = website.description ? String(website.description).trim() : "";

            if (website.category) {
                text += (text ? " " : "") + (website.name || "This website") + " is listed under " + website.category +
                    (website.country ? " in " + website.country : "") + ".";
            }

            about.textContent = text || "No description has been added yet.";
            clampToggle(about, $("aboutMore"));
        }

        /* Contact info */
        var list = $("websiteDetails");

        if (list) {
            var html = "";

            if (website.url) {
                var href = typeof safeHref === "function" ? safeHref(website.url) : website.url;
                html += '<dt>Website</dt><dd><a href="' + esc(href) + '" target="_blank" rel="noopener noreferrer">' + esc(website.url) + "</a></dd>";
            }

            if (website.country) { html += "<dt>Country</dt><dd>" + esc(website.country) + "</dd>"; }
            if (website.category) { html += "<dt>Category</dt><dd>" + esc(website.category) + "</dd>"; }

            list.innerHTML = html || "<dt>Contact</dt><dd>No contact details added yet.</dd>";
        }

        buildBreadcrumb();

        if (website.category && !similarLoaded) {
            similarLoaded = true;
            loadSimilar(website);
        }
    }

    function clampToggle(element, button) {

        if (!element || !button) { return; }

        element.classList.add("clamp");
        button.hidden = false;
        button.textContent = "See more";

        requestAnimationFrame(function () {
            if (element.scrollHeight <= element.clientHeight + 2) { button.hidden = true; }
        });

        button.onclick = function () {
            var open = element.classList.toggle("clamp");
            button.textContent = open ? "See more" : "See less";
        };
    }

    function loadSimilar(website) {

        fetch(WEBSITE_API + "/filter?category=" + encodeURIComponent(website.category))
            .then(function (response) { return response.ok ? response.json() : []; })
            .then(function (list) {

                var others = (Array.isArray(list) ? list : [])
                    .filter(function (item) { return String(item.id) !== String(website.id); })
                    .sort(function (a, b) { return (Number(b.reviewCount) || 0) - (Number(a.reviewCount) || 0); })
                    .slice(0, 4);

                var slot = $("similarSlot");

                if (!others.length || !slot) { return; }

                slot.innerHTML = '<section class="block rg-similar"><h3 class="block-sub">People also looked at</h3><div class="rg-sim-grid">' +
                    others.map(function (item) {

                        var domain = item.canonicalDomain || "";
                        var rating = Number(item.averageRating) || 0;
                        var count = Number(item.reviewCount) || 0;
                        var initial = esc(String(item.name || "W").trim().charAt(0).toUpperCase());

                        return '<a class="rg-sim" href="review.html?websiteId=' + encodeURIComponent(item.id) + '">' +
                            '<span class="rg-sim-logo">' + initial + "</span>" +
                            '<strong>' + esc(item.name) + "</strong><small>" + esc(domain) + "</small>" +
                            '<span class="rg-sim-score">' + (count ? squareStars(rating) + " <em>" + rating.toFixed(1) + " (" + fmt(count) + ")</em>" : "No reviews yet") +
                            "</span></a>";
                    }).join("") + "</div></section>";
            })
            .catch(function (error) { console.error("Unable to load similar websites:", error); });
    }

    /* ---------- Notices next to the rating card ---------- */

    function buildNotices() {

        var box = $("noticeList");

        if (!box) { return; }

        var reviews = getReviews();
        var replies = getReplies();
        var replied = reviews.filter(function (r) { return replies[Number(r.id)]; }).length;
        var verified = reviews.filter(function (r) { return String(r.verificationStatus || "").toUpperCase() === "VERIFIED"; }).length;

        var items = [];

        if (reviews.length) {
            items.push(["&#8617;", "Replied to " + Math.round((replied / reviews.length) * 100) + "% of reviews",
                replied + " of " + reviews.length + " reviews have a reply from the company."]);
            items.push(["&#10003;", fmt(verified) + " verified review" + (verified === 1 ? "" : "s"),
                "Written by customers whose experience was verified."]);
        } else {
            items.push(["&#8617;", "No reviews yet", "Be the first to share your experience."]);
        }

        items.push(["&#9432;", "How reviews work", "Reviews are written by users and go through moderation."]);

        box.innerHTML = items.map(function (item) {
            return '<div class="notice"><span class="notice-ico">' + item[0] + "</span><div><strong>" +
                esc(item[1]) + "</strong><small>" + esc(item[2]) + "</small></div></div>";
        }).join("");
    }

    function updateRecent() {

        var el = $("arRecent");

        if (!el) { return; }

        var limit = Date.now() - 365 * 86400000;
        var count = getReviews().filter(function (r) { return reviewTime(r) >= limit; }).length;

        el.textContent = fmt(count) + " review" + (count === 1 ? "" : "s") + " in the last 12 months";
    }

    /* ---------- Review summary (computed from the real reviews) ---------- */

    function composeSummary(reviews) {

        var website = getWebsite();
        var name = website && website.name ? website.name : "this website";
        var total = currentSummaryTotal(reviews);
        var average = currentSummaryAverage(reviews);
        var label = scoreLabel(average, total).toLowerCase();

        var good = reviews.filter(function (r) { return Number(r.rating) >= 4; }).length;
        var bad = reviews.filter(function (r) { return Number(r.rating) <= 2; }).length;
        var goodPct = Math.round(good / reviews.length * 100);
        var badPct = Math.round(bad / reviews.length * 100);
        var tone = goodPct >= 70 ? "mostly positive" : (badPct >= 40 ? "mostly negative" : "mixed");

        var stats = countWords(reviews);
        var top = Object.keys(stats)
            .filter(function (word) { return stats[word].count >= 2; })
            .sort(function (a, b) { return stats[b].count - stats[a].count; })
            .slice(0, 3);

        var commented = reviews.filter(function (r) { return String(r.comment || "").trim(); });
        var best = commented.filter(function (r) { return Number(r.rating) >= 4; })
            .sort(function (a, b) { return String(b.comment).length - String(a.comment).length; })[0];
        var worst = commented.filter(function (r) { return Number(r.rating) <= 3; })
            .sort(function (a, b) { return Number(a.rating) - Number(b.rating); })[0];

        var replies = getReplies();
        var replied = reviews.filter(function (r) { return replies[Number(r.id)]; }).length;
        var verified = reviews.filter(function (r) { return String(r.verificationStatus || "").toUpperCase() === "VERIFIED"; }).length;

        var parts = [];

        parts.push("Evaluating " + fmt(total) + " review" + (total === 1 ? "" : "s") + ", reviewers rate " + name + " " +
            average.toFixed(1) + " out of 5, which is considered " + label + ".");

        parts.push(goodPct + "% of customers gave 4 or 5 stars, while " + badPct +
            "% gave 2 stars or lower, so overall experiences are " + tone + ".");

        parts.push(top.length
            ? "Customers most often talk about " + joinList(top) + "."
            : "Customers describe a range of different experiences with this website.");

        if (best) {
            parts.push("Happy customers say: “" + clip(best.comment, 14) + "”");
        }

        if (worst) {
            parts.push("Others were less satisfied: “" + clip(worst.comment, 14) + "”");
        } else {
            parts.push("No serious complaints stand out in the reviews written so far.");
        }

        parts.push(replied
            ? "The company has replied to " + Math.round(replied / reviews.length * 100) + "% of reviews, and " +
                fmt(verified) + " review" + (verified === 1 ? " is" : "s are") + " from verified customers."
            : "The company has not replied to reviews yet, and " + fmt(verified) + " review" +
                (verified === 1 ? " is" : "s are") + " from verified customers.");

        return parts.join(" ");
    }

    function currentSummaryTotal(reviews) {
        return typeof currentSummary !== "undefined" && currentSummary && Number(currentSummary.totalReview)
            ? Number(currentSummary.totalReview) : reviews.length;
    }

    function currentSummaryAverage(reviews) {
        if (typeof currentSummary !== "undefined" && currentSummary && Number(currentSummary.averageRating)) {
            return Number(currentSummary.averageRating);
        }
        return reviews.reduce(function (sum, r) { return sum + (Number(r.rating) || 0); }, 0) / reviews.length;
    }

    /* "2 of 4 reviewers mention sound, with an average of 5.0 stars. One says: ..." -> 20 to 25 words */
    function topicText(word, stat, total, reviews) {

        var base = stat.count + " of " + total + " reviewer" + (total === 1 ? "" : "s") + " mention " + word +
            ", with an average rating of " + (stat.sum / stat.count).toFixed(1) + " stars.";

        var match = reviews.filter(function (r) {
            return String(r.comment || "").toLowerCase().indexOf(word) > -1;
        }).sort(function (a, b) { return Number(b.rating) - Number(a.rating); })[0];

        var snippet = "";

        if (match) {
            var sentences = String(match.comment).split(/[.!?]+\s+/);
            snippet = sentences.filter(function (s) { return s.toLowerCase().indexOf(word) > -1; })[0] || match.comment;
        }

        var room = Math.max(6, 24 - wordsOf(base).length - 2);
        var text = base + (snippet ? " One says: “" + clip(snippet, room) + "”" : "");

        if (wordsOf(text).length < 20) {
            text += " Read all reviews to see more.";
        }

        return text;
    }

    function buildSummary() {

        var text = $("rvSummaryText");
        var topics = $("rvTopics");

        if (!text || !topics) { return; }

        var reviews = getReviews();

        if (!reviews.length) {
            text.textContent = "There are no reviews yet, so there is nothing to summarise. Be the first to write one and help other people decide.";
            topics.innerHTML = "";
            $("rvTopicsBlock").hidden = true;
            if ($("rvSummaryMore")) { $("rvSummaryMore").hidden = true; }
            return;
        }

        $("rvTopicsBlock").hidden = false;
        text.textContent = composeSummary(reviews);
        clampToggle(text, $("rvSummaryMore"));

        var stats = countWords(reviews);
        var words = Object.keys(stats).sort(function (a, b) {
            return (stats[b].count >= 2) - (stats[a].count >= 2) || stats[b].count - stats[a].count;
        }).slice(0, 5);

        topics.innerHTML = words.map(function (word) {

            var avg = stats[word].sum / stats[word].count;
            var tone = avg >= 3.5 ? "pos" : (avg < 2.5 ? "neg" : "mid");

            return '<div class="rv-topic ' + tone + '"><strong>' + esc(cap(word)) + "</strong><p>" +
                esc(topicText(word, stats[word], reviews.length, reviews)) + "</p></div>";
        }).join("") || '<p class="rv-muted">Not enough repeated topics in the reviews yet.</p>';
    }

    /* ---------- Reviews shaping this summary (carousel) ---------- */

    function buildShaping() {

        var box = $("rvShaping");
        var reviews = getReviews();

        if (!box) { return; }

        if (!reviews.length) {
            box.innerHTML = "";
            return;
        }

        var replies = getReplies();
        var total = currentSummaryTotal(reviews);

        var picks = reviews.filter(function (r) { return String(r.comment || "").trim(); })
            .sort(function (a, b) { return reviewTime(b) - reviewTime(a); })
            .slice(0, 6);

        box.innerHTML = '<h3 class="block-sub">Reviews shaping this summary</h3>' +
            '<div class="carousel"><div class="rv-shape-row" id="rvShapeRow">' +
            picks.map(function (r) {

                var name = reviewerName(r);
                var verified = String(r.verificationStatus || "").toUpperCase() === "VERIFIED";

                return '<article class="rv-shape"><div class="rv-shape-head"><span class="avatar">' +
                    esc(name.charAt(0).toUpperCase()) + "</span><div><strong>" + esc(name) + "</strong><small>" +
                    esc(relativeDate(r)) + "</small></div></div>" +
                    '<div class="rv-shape-stars">' + squareStars(Number(r.rating) || 0) +
                    (verified ? '<span class="rg-tag-inline">&#10003; Verified</span>' : "") + "</div>" +
                    "<p>" + esc(clip(r.comment, 38)) + "</p>" +
                    (replies[Number(r.id)] ? '<div class="rv-shape-reply">&#8617; Company replied</div>' : "") +
                    "</article>";
            }).join("") +
            '</div><button type="button" class="car-arrow" data-for="rvShapeRow" aria-label="Next">&rsaquo;</button></div>' +
            '<button type="button" class="ar-see" id="rvSeeAll">See all ' + fmt(total) + " reviews</button>";

        $("rvSeeAll").addEventListener("click", function () {
            var target = $("allReviews");
            if (target) { target.scrollIntoView({ behavior: "smooth", block: "start" }); }
        });
    }

    function wireCarousels() {

        document.addEventListener("click", function (event) {

            var arrow = event.target.closest(".car-arrow");

            if (!arrow) { return; }

            var row = $(arrow.dataset.for);

            if (row) { row.scrollBy({ left: row.clientWidth * 0.8, behavior: "smooth" }); }
        });
    }

    /* ---------- Hook into the main script ---------- */

    var baseRenderRatingSummary = window.renderRatingSummary;
    var baseGetFilteredReviews = window.getFilteredReviews;
    var baseDisplayReviews = window.displayReviews;
    var baseRenderCompanyDetails = window.renderCompanyDetails;

    if (typeof baseRenderRatingSummary === "function") {
        window.renderRatingSummary = function (summary) {
            baseRenderRatingSummary(summary);
            decorateSummary(summary);

            if (getReviews().length) {
                buildSummary();
                buildShaping();
            }
        };
    }

    if (typeof baseGetFilteredReviews === "function") {
        window.getFilteredReviews = function () {

            var list = baseGetFilteredReviews();

            if (onlyVerified) {
                list = list.filter(function (r) { return String(r.verificationStatus || "").toUpperCase() === "VERIFIED"; });
            }

            if (onlyReplied) {
                list = list.filter(function (r) { return getReplies()[Number(r.id)]; });
            }

            if (!keyword) { return list; }

            return list.filter(function (review) {
                return String(review.comment || "").toLowerCase().indexOf(keyword) > -1 ||
                    String(review.userName || "").toLowerCase().indexOf(keyword) > -1;
            });
        };
    }

    if (typeof baseDisplayReviews === "function") {
        window.displayReviews = function () {
            baseDisplayReviews();
            buildSummary();
            buildShaping();
            buildNotices();
            updateRecent();
            buildMentions();
            decorateCards();
            syncRatingFilter();
        };
    }

    if (typeof baseRenderCompanyDetails === "function") {
        window.renderCompanyDetails = function () {
            baseRenderCompanyDetails();
            decorateDetails();
        };
    }

    /* ---------- Breadcrumb ---------- */

    function buildBreadcrumb() {

        var website = getWebsite();
        var grid = document.querySelector(".top-grid");

        if (!grid || !website) { return; }

        var crumb = $("rgCrumb");

        if (!crumb) {
            crumb = document.createElement("nav");
            crumb.id = "rgCrumb";
            crumb.className = "rg-crumb";
            crumb.setAttribute("aria-label", "Breadcrumb");
            grid.insertAdjacentElement("beforebegin", crumb);
        }

        crumb.innerHTML = '<a href="index.html">Home</a>' +
            (website.category ? '<span>&rsaquo;</span><a href="category.html?category=' + encodeURIComponent(website.category) + '">' + esc(website.category) + "</a>" : "") +
            "<span>&rsaquo;</span><strong>" + esc(website.name || "Website") + "</strong>";
    }

    function setNavHeight() {
        var nav = document.querySelector(".top-navbar");
        if (nav) { document.documentElement.style.setProperty("--nav-h", nav.offsetHeight + "px"); }
    }

    setNavHeight();
    window.addEventListener("resize", setNavHeight);

    buildTabs();
    buildSearchBox();
    buildMoreFilters();
    wireRatingRows();
    wireCarousels();
})();