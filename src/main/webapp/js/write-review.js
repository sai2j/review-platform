/* =========================================================
   WRITE A REVIEW PAGE
   Open as:  write-review.html?websiteId=1
             write-review.html?websiteId=1&rating=4
========================================================= */

(function () {
    "use strict";

    var WEBSITE_API = "/review-platform/websites";
    var REVIEW_API = "/review-platform/reviews";

    var params = new URLSearchParams(window.location.search);
    var websiteId = params.get("websiteId");
    var rating = 0;

    function $(id) {
        return document.getElementById(id);
    }

    function getUser() {

        try {
            var user = JSON.parse(localStorage.getItem("loggedInUser"));
            return user && user.id ? user : null;
        } catch (error) {
            return null;
        }
    }

	/* log in on the combined page, then come back here */
	function loginUrl() {
	    return "auth.html?next=" +
	        encodeURIComponent("write-review.html" + window.location.search);
	}
    function reviewPageUrl() {
        return websiteId
            ? "review.html?websiteId=" + encodeURIComponent(websiteId)
            : "index.html";
    }

    /* ---------- Website card ---------- */

    function loadWebsite() {

        if (!websiteId) {

            $("wrName").textContent = "Website not found";
            $("wrDomain").textContent = "Open this page from a website's review page.";
            $("wrRateBlock").hidden = true;

            return;
        }

        $("wrCompany").href = reviewPageUrl();
        $("wrSeeReviews").href = reviewPageUrl();

        fetch(WEBSITE_API + "/" + encodeURIComponent(websiteId))
            .then(function (response) {

                if (!response.ok) {
                    throw new Error("Website not found");
                }

                return response.json();
            })
            .then(function (website) {

                var domain = website.canonicalDomain || "";

                if (!domain && website.url) {
                    try {
                        domain = new URL(website.url).hostname.replace(/^www\./, "");
                    } catch (error) {
                        domain = website.url;
                    }
                }

                $("wrName").textContent = website.name || domain || "Website";
                $("wrDomain").textContent = domain;
                document.title = "Rate " + (website.name || domain) + " - Review Platform";

                var logo = $("wrLogo");
                var initial = String(website.name || domain || "W").trim().charAt(0).toUpperCase();

                logo.textContent = initial;

                if (domain) {

                    var img = new Image();

                    img.alt = "";

                    img.onload = function () {
                        logo.textContent = "";
                        logo.appendChild(img);
                    };

                    img.src = "https://www.google.com/s2/favicons?sz=64&domain=" +
                        encodeURIComponent(domain);
                }
            })
            .catch(function () {
                $("wrName").textContent = "Website not found";
                $("wrDomain").textContent = "";
                $("wrRateBlock").hidden = true;
            });
    }

    /* ---------- Stars ---------- */

    function paintStars(value) {

        document.querySelectorAll(".wr-star").forEach(function (star) {
            star.classList.toggle("on", Number(star.dataset.value) <= value);
        });
    }

    function setRating(value, animate) {

        rating = value;

        paintStars(value);

        var stars = document.querySelectorAll(".wr-star");

        stars.forEach(function (star) {

            var number = Number(star.dataset.value);

            star.setAttribute("aria-checked", number === value ? "true" : "false");
            star.classList.remove("pop");

            if (animate && number <= value) {

                /* the stars light up one after another, left to right */
                star.style.animationDelay = ((number - 1) * 0.07) + "s";
                void star.offsetWidth;
                star.classList.add("pop");
            }
        });

        var wrap = $("wrFormWrap");

        if (wrap.hidden) {

            wrap.hidden = false;
            void wrap.offsetWidth;
            wrap.classList.add("open");

            if (animate) {

                setTimeout(function () {
                    wrap.scrollIntoView({ behavior: "smooth", block: "nearest" });
                }, 250);

                setTimeout(function () {
                    $("wrComment").focus({ preventScroll: true });
                }, 750);
            }
        }
    }

    function buildStars() {

        var box = $("wrStars");

        box.addEventListener("mouseover", function (event) {

            var star = event.target.closest(".wr-star");

            if (star) {
                paintStars(Number(star.dataset.value));
            }
        });

        box.addEventListener("mouseleave", function () {
            paintStars(rating);
        });

        box.addEventListener("click", function (event) {

            var star = event.target.closest(".wr-star");

            if (star) {
                setRating(Number(star.dataset.value), true);
            }
        });
    }

    /* ---------- Chips ---------- */

    function buildChips() {

        $("wrChips").addEventListener("click", function (event) {

            var chip = event.target.closest(".wr-chip");

            if (!chip) {
                return;
            }

            var box = $("wrComment");
            var text = chip.textContent.trim() + ": ";

            box.value = box.value.trim()
                ? box.value.replace(/\s+$/, "") + "\n" + text
                : text;

            box.focus();
            box.setSelectionRange(box.value.length, box.value.length);
        });
    }

    /* ---------- Submit ---------- */

    function showMessage(html, isError) {

        var box = $("wrMessage");

        box.className = "wr-message" + (isError ? " error" : "");
        box.innerHTML = html;
    }

    function submitReview(event) {

        event.preventDefault();

        var comment = $("wrComment").value.trim();
        var consent = $("wrConsent").checked;
        var user = getUser();

        $("wrComment").classList.remove("invalid");
        $("wrConsent").closest(".wr-check").classList.remove("invalid");

        if (!rating) {
            showMessage("Please select a star rating.", true);
            return;
        }

        if (!comment) {
            $("wrComment").classList.add("invalid");
            showMessage("Please tell us about your experience.", true);
            return;
        }

        if (!consent) {
            $("wrConsent").closest(".wr-check").classList.add("invalid");
            showMessage("Please confirm that your review is genuine.", true);
            return;
        }

        if (!user) {
                        showMessage('Please <a href="' + loginUrl() + '">log in</a> before submitting a review.', true);
            return;
        }

        var button = $("wrSubmit");

        button.disabled = true;
        button.textContent = "Submitting...";
        showMessage("", false);

        fetch(REVIEW_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                rating: rating,
                comment: comment,
                userId: Number(user.id),
                websiteId: Number(websiteId)
            })
        })
            .then(function (response) {

                if (!response.ok) {
                    throw new Error("Review could not be added.");
                }

                return response.json();
            })
            .then(function () {

                $("wrRateBlock").hidden = true;
                $("wrFormWrap").hidden = true;
                $("wrLoginNotice").hidden = true;
                $("wrSuccess").hidden = false;

                window.scrollTo({ top: 0, behavior: "smooth" });
            })
            .catch(function () {

                button.disabled = false;
                button.textContent = "Submit review";

                showMessage("Review could not be added. Please try again.", true);
            });
    }

    /* ---------- Start ---------- */

    document.addEventListener("DOMContentLoaded", function () {

		if (!getUser()) {

		            var noticeLink = $("wrLoginNotice").querySelector("a");

		            if (noticeLink) {
		                noticeLink.href = loginUrl();
		            }

		            $("wrLoginNotice").hidden = false;
		        }

        loadWebsite();
        buildStars();
        buildChips();

        $("wrForm").addEventListener("submit", submitReview);

        var start = Number(params.get("rating"));

        if (websiteId && start >= 1 && start <= 5) {
            setRating(start, false);
        }
    });
})();