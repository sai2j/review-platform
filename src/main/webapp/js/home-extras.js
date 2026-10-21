/* =========================================================
   HOME PAGE EXTRAS
   - stats strip, category counts
   - Top rated websites cards
   - New design for "Recent reviews" cards
   (load this file AFTER js/script.js)
========================================================= */
(function () {
	"use strict";

	/* ---------- helpers ---------- */

	function esc(v) {
		return String(v == null ? "" : v)
			.replace(/&/g, "&amp;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;");
	}

	function domainOf(url) {
		try {
			var u = String(url || "");
			if (!/^https?:\/\//i.test(u)) { u = "https://" + u; }
			return new URL(u).hostname.replace(/^www\./, "");
		} catch (e) {
			return "";
		}
	}

	function initialOf(text) {
		var t = String(text || "").trim();
		return t ? t.charAt(0).toUpperCase() : "W";
	}

	function stars(value) {
		var full = Math.max(0, Math.min(5, Math.round(Number(value) || 0)));
		var out = "";
		for (var i = 1; i <= 5; i++) { out += i <= full ? "\u2605" : "\u2606"; }
		return out;
	}

	function formatDate(value) {
		if (!value) { return ""; }
		var d = new Date(value);
		if (isNaN(d.getTime())) { return ""; }
		return d.toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
	}

	function getJson(url) {
		return fetch(url).then(function (r) {
			if (!r.ok) { throw new Error("Request failed: " + url); }
			return r.json();
		});
	}

	function setText(id, text) {
		var el = document.getElementById(id);
		if (el) { el.textContent = text; }
	}

	function websitesList() {
		/* "allWebsites" is declared in script.js */
		return (typeof allWebsites !== "undefined" && allWebsites) ? allWebsites : [];
	}

	function maxRecent() {
		return (typeof MAX_RECENT_REVIEWS !== "undefined") ? MAX_RECENT_REVIEWS : 4;
	}

	/* =========================================================
	   RECENT REVIEWS: new card design
	   (replaces the two functions that script.js uses)
	========================================================= */

	function reviewCardHtml(r) {
		var comment = String(r.comment || "").trim();
		var verified = String(r.verificationStatus || "").toUpperCase() === "VERIFIED";
		var date = formatDate(r.createdAt);

		var siteInner =
			'<div class="rv-logo">' + esc(initialOf(r.websiteName)) + '</div>' +
			'<div><b>' + esc(r.websiteName) + '</b><small>' + esc(r.websiteDomain) + '</small></div>' +
			'<span class="rv-arrow">\u2192</span>';

		var siteHtml = r.websiteId
			? '<a class="rv-site" href="review.html?websiteId=' + encodeURIComponent(r.websiteId) + '">' + siteInner + '</a>'
			: '<div class="rv-site">' + siteInner + '</div>';

		return '' +
			'<div class="rv-card">' +
			'<div class="rv-quote">\u201C</div>' +
			'<div class="rv-stars">' + stars(r.rating) + '</div>' +
			(comment
				? '<p class="rv-text">' + esc(comment) + '</p>'
				: '<p class="rv-text rv-empty">Rated without a written comment.</p>') +
			'<div class="rv-user">' +
			'<div class="rv-av">' + esc(initialOf(r.userName)) + '</div>' +
			'<div><b>' + esc(r.userName) + '</b>' +
			(verified ? '<small class="ok">\u2714 Verified</small>' : '<small>Customer review</small>') +
			(date ? '<span class="rv-date">' + esc(date) + '</span>' : '') +
			'</div></div>' +
			siteHtml +
			'</div>';
	}

	window.displayDemoRecentReviews = function () {
		var list = document.getElementById("recentReviewsList");
		if (!list) { return; }

		var demo = [
			{ userName: "Demo User 1", rating: 5, comment: "Very easy to use and the information was helpful. I found what I was looking for quickly.", websiteName: "Example Business", websiteDomain: "example.com" },
			{ userName: "Demo User 2", rating: 4, comment: "The website profile was clear and useful. The review section made it easier to understand the service.", websiteName: "Sample Store", websiteDomain: "samplestore.com" },
			{ userName: "Demo User 3", rating: 5, comment: "Good experience overall. I liked having customer feedback available in one place.", websiteName: "Demo Services", websiteDomain: "demoservices.com" },
			{ userName: "Demo User 4", rating: 4, comment: "Simple layout and useful information. It was easy to read the customer experiences.", websiteName: "Test Company", websiteDomain: "testcompany.com" }
		];

		list.innerHTML = demo.map(reviewCardHtml).join("");
	};

	window.displayRecentReviews = function (reviews) {
		var list = document.getElementById("recentReviewsList");
		if (!list) { return; }

		var items = (reviews || []).slice(0, maxRecent());

		if (items.length === 0) {
			window.displayDemoRecentReviews();
			return;
		}

		var sites = websitesList();

		list.innerHTML = items.map(function (review) {
			var website = null;
			for (var i = 0; i < sites.length; i++) {
				if (String(sites[i].id) === String(review.websiteId)) { website = sites[i]; break; }
			}

			var userName = (review.userName && String(review.userName).trim())
				? String(review.userName).trim()
				: (review.userId ? "User #" + review.userId : "User");

			return reviewCardHtml({
				userName: userName,
				rating: review.rating,
				comment: review.comment,
				createdAt: review.createdAt,
				verificationStatus: review.verificationStatus,
				websiteId: review.websiteId,
				websiteName: website && website.name ? website.name : "Website",
				websiteDomain: website ? domainOf(website.url) : ""
			});
		}).join("");
	};

	/* =========================================================
	   STATS, CATEGORY COUNTS, TOP RATED
	========================================================= */

	function renderTopRated(websites, rankings) {
		var section = document.getElementById("topRatedSection");
		var list = document.getElementById("topRatedList");
		if (!section || !list) { return; }

		var byId = {};
		websites.forEach(function (w) { byId[w.id] = w; });

		var top = rankings
			.filter(function (r) { return r.reviewCount > 0 && byId[r.websiteId]; })
			.sort(function (a, b) {
				return (b.averageRating - a.averageRating) || (b.reviewCount - a.reviewCount);
			})
			.slice(0, 4);

		if (top.length === 0) { return; }

		list.innerHTML = top.map(function (r, i) {
			var w = byId[r.websiteId];
			var letter = esc(initialOf(w.name));
			var avg = Number(r.averageRating) || 0;

			return '' +
				'<a class="tp-card c' + (i + 1) + '" href="review.html?websiteId=' + encodeURIComponent(w.id) + '">' +
				'<div class="tp-head">' +
				'<span class="tp-rank">#' + (i + 1) + '</span>' +
				'<span class="tp-score">' + avg.toFixed(1) + ' <span>\u2605</span></span>' +
				'<span class="tp-big">' + letter + '</span>' +
				'</div>' +
				'<div class="tp-logo">' + letter + '</div>' +
				'<div class="tp-body">' +
				(w.category ? '<span class="tp-chip">' + esc(w.category) + '</span>' : '') +
				'<h3>' + esc(w.name) + '</h3>' +
				'<p>' + esc(domainOf(w.url)) + '</p>' +
				'<div class="tp-stars">' + stars(avg) + '<small>' + r.reviewCount + (r.reviewCount === 1 ? ' review' : ' reviews') + '</small></div>' +
				'<div class="tp-link"><span>Read reviews</span><span>\u2192</span></div>' +
				'</div>' +
				'</a>';
		}).join("");

		section.style.display = "";
	}

	function init() {
		Promise.all([
			getJson("/review-platform/websites"),
			getJson("/review-platform/reviews/ranking").catch(function () { return []; }),
			getJson("/review-platform/reviews?size=1").catch(function () { return null; })
		]).then(function (data) {
			var websites = data[0] || [];
			var rankings = data[1] || [];
			var reviewPage = data[2];

			/* stats strip */
			var totalReviews = 0;
			if (reviewPage && typeof reviewPage.totalElements === "number") {
				totalReviews = reviewPage.totalElements;
			} else {
				rankings.forEach(function (r) { totalReviews += r.reviewCount || 0; });
			}
			var cats = {};
			websites.forEach(function (w) { if (w.category) { cats[w.category] = true; } });
			setText("statReviews", totalReviews);
			setText("statWebsites", websites.length);
			setText("statCategories", Object.keys(cats).length);

			/* category counts */
			document.querySelectorAll(".category-item").forEach(function (btn) {
				var key = (btn.dataset.category || "").toLowerCase();
				var n = websites.filter(function (w) {
					var c = (w.category || "").toLowerCase();
					return c && (c === key || c.indexOf(key) !== -1 || key.indexOf(c) !== -1);
				}).length;
				var small = btn.querySelector(".category-count");
				if (small && n > 0) { small.textContent = n + (n === 1 ? " website" : " websites"); }
			});

			/* top rated */
			renderTopRated(websites, rankings);
		}).catch(function (e) {
			console.error("Home extras failed:", e);
			var strip = document.getElementById("statsStrip");
			if (strip) { strip.style.display = "none"; }
		});
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();