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

	/* Website logo: real favicon, falls back to the first letter */
	function logoHtml(name, domain, cls) {
		var letter = esc(initialOf(name));
		if (!domain) { return '<div class="' + cls + '">' + letter + '</div>'; }
		var src = "https://www.google.com/s2/favicons?sz=128&domain=" + encodeURIComponent(domain);
		return '<div class="' + cls + ' has-logo" data-letter="' + letter + '">' +
			'<img src="' + esc(src) + '" alt="" loading="lazy"></div>';
	}

	document.addEventListener("error", function (event) {
		var img = event.target;
		if (!img || img.tagName !== "IMG") { return; }
		var box = img.parentElement;
		if (box && box.classList.contains("has-logo")) {
			box.textContent = box.getAttribute("data-letter") || "W";
			box.classList.remove("has-logo");
		}
	}, true);

	function stars(value, small) {
		var full = Math.max(0, Math.min(5, Math.round(Number(value) || 0)));
		var svg = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2l2.9 6.9 7.1.6-5.4 4.7 1.7 7-6.3-3.8-6.3 3.8 1.7-7L2 9.5l7.1-.6z"/></svg>';
		var out = '<span class="sqs' + (small ? ' sm' : '') + '" role="img" aria-label="' + full + ' out of 5 stars">';
		for (var i = 1; i <= 5; i++) { out += '<i class="' + (i <= full ? '' : 'e') + '">' + svg + '</i>'; }
		return out + '</span>';
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
			logoHtml(r.websiteName, r.websiteDomain, "rv-logo") +
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
			{ userName: "Priya Sharma", rating: 5, comment: "Very easy to use and the information was helpful. I found what I was looking for quickly.", websiteName: "Example Business", websiteDomain: "example.com" },
			{ userName: "Rahul Verma", rating: 4, comment: "The website profile was clear and useful. The review section made it easier to understand the service.", websiteName: "Sample Store", websiteDomain: "samplestore.com" },
			{ userName: "Ananya Iyer", rating: 5, comment: "Good experience overall. I liked having customer feedback available in one place.", websiteName: "Demo Services", websiteDomain: "demoservices.com" },
			{ userName: "Mohit Patil", rating: 4, comment: "Simple layout and useful information. It was easy to read the customer experiences.", websiteName: "Test Company", websiteDomain: "testcompany.com" }
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
				'<span class="tp-rank">#' + (i + 1) + '</span>' +
				logoHtml(w.name, domainOf(w.url), "tp-logo") +
				'<div class="tp-body">' +
				'<h3>' + esc(w.name) + '</h3>' +
				'<p>' + esc(domainOf(w.url)) + '</p>' +
				'<div class="tp-stars"><b>' + avg.toFixed(1) + '</b>' + stars(avg, true) + '<small>' + r.reviewCount + (r.reviewCount === 1 ? ' review' : ' reviews') + '</small></div>' +
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
/* =========================================================
   SEARCH SUGGESTIONS DROPDOWN
   Opens when the search box is clicked (like Trustpilot):
   - empty box  -> recent searches + suggested websites/categories
   - typing     -> matching websites and categories
   Keyboard: Up / Down / Enter / Esc
========================================================= */
(function () {
	"use strict";

	var RECENT_KEY = "recentSearches";
	var input, box, panel, items = [], active = -1, sites = null;

	function esc(v) {
		return String(v == null ? "" : v).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
	}

	function domainOf(url) {
		try {
			var u = String(url || "");
			if (!/^https?:\/\//i.test(u)) { u = "https://" + u; }
			return new URL(u).hostname.replace(/^www\./, "");
		} catch (e) { return ""; }
	}

	function getRecent() {
		try { return JSON.parse(localStorage.getItem(RECENT_KEY) || "[]").slice(0, 4); } catch (e) { return []; }
	}

	function saveRecent(q) {
		q = String(q || "").trim();
		if (!q) { return; }
		try {
			var list = getRecent().filter(function (x) { return x.toLowerCase() !== q.toLowerCase(); });
			list.unshift(q);
			localStorage.setItem(RECENT_KEY, JSON.stringify(list.slice(0, 4)));
		} catch (e) { /* storage blocked - ignore */ }
	}

	function clearRecent() {
		try { localStorage.removeItem(RECENT_KEY); } catch (e) { /* ignore */ }
	}

	function iconSearch() {
		return '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>';
	}
	function iconClock() {
		return '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>';
	}
	function iconTag() {
		return '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12V4h8l10 10-8 8z"/><circle cx="7.5" cy="8.5" r="1.2"/></svg>';
	}

	function siteLogo(name, domain) {
		var letter = esc((String(name || "W").trim().charAt(0) || "W").toUpperCase());
		if (!domain) { return '<span class="ss-logo">' + letter + '</span>'; }
		var src = "https://www.google.com/s2/favicons?sz=64&domain=" + encodeURIComponent(domain);
		return '<span class="ss-logo has-logo" data-letter="' + letter + '"><img src="' + esc(src) + '" alt=""></span>';
	}

	function categories() {
		var seen = {}, out = [];
		(sites || []).forEach(function (w) {
			if (w.category && !seen[w.category]) { seen[w.category] = true; out.push(w.category); }
		});
		return out;
	}

	function row(kind, value, html) {
		return '<button type="button" class="ss-row" role="option" data-kind="' + kind + '" data-value="' + esc(value) + '">' + html + '</button>';
	}

	function render() {
		var q = input.value.trim().toLowerCase();
		var html = "";

		if (!q) {
			var recent = getRecent();
			if (recent.length) {
				html += '<div class="ss-head"><span>Recent searches</span><button type="button" class="ss-clear" id="ssClear">Clear</button></div>';
				recent.forEach(function (r) {
					html += row("query", r, '<span class="ss-ico">' + iconClock() + '</span><span class="ss-text">' + esc(r) + '</span>');
				});
			}

			var list = (sites || []).slice(0, 5);
			if (list.length) {
				html += '<div class="ss-head"><span>Suggested websites</span></div>';
				list.forEach(function (w) {
					html += row("site", w.name, siteLogo(w.name, domainOf(w.url)) +
						'<span class="ss-text"><b>' + esc(w.name) + '</b><small>' + esc(domainOf(w.url)) + '</small></span>');
				});
			}

			var cats = categories().slice(0, 4);
			if (cats.length) {
				html += '<div class="ss-head"><span>Browse by category</span></div><div class="ss-chips">';
				cats.forEach(function (c) {
					html += '<button type="button" class="ss-chip" role="option" data-kind="query" data-value="' + esc(c) + '">' + esc(c) + '</button>';
				});
				html += '</div>';
			}
		} else {
			var found = (sites || []).filter(function (w) {
				return String(w.name || "").toLowerCase().indexOf(q) !== -1 ||
					domainOf(w.url).toLowerCase().indexOf(q) !== -1;
			}).slice(0, 5);

			var catHits = categories().filter(function (c) { return c.toLowerCase().indexOf(q) !== -1; }).slice(0, 3);

			if (found.length) {
				html += '<div class="ss-head"><span>Websites</span></div>';
				found.forEach(function (w) {
					html += row("site", w.name, siteLogo(w.name, domainOf(w.url)) +
						'<span class="ss-text"><b>' + esc(w.name) + '</b><small>' + esc(domainOf(w.url)) + '</small></span>');
				});
			}
			if (catHits.length) {
				html += '<div class="ss-head"><span>Categories</span></div>';
				catHits.forEach(function (c) {
					html += row("query", c, '<span class="ss-ico">' + iconTag() + '</span><span class="ss-text">' + esc(c) + '</span>');
				});
			}
			html += row("query", input.value.trim(),
				'<span class="ss-ico">' + iconSearch() + '</span><span class="ss-text">Search for <b>&ldquo;' + esc(input.value.trim()) + '&rdquo;</b></span>');
		}

		if (!html) { close(); return; }

		panel.innerHTML = html;
		items = Array.prototype.slice.call(panel.querySelectorAll("[data-value]"));
		active = -1;
	}

	function place() {
		var r = box.getBoundingClientRect();
		panel.style.left = (r.left + window.pageXOffset) + "px";
		panel.style.top = (r.bottom + window.pageYOffset + 8) + "px";
		panel.style.width = r.width + "px";
	}

	function open() {
		render();
		if (!panel.innerHTML) { return; }
		place();
		panel.hidden = false;
	}

	function close() {
		panel.hidden = true;
		active = -1;
	}

	function go(value) {
		value = String(value || "").trim();
		if (!value) { return; }
		saveRecent(value);
		window.location.href = "search.html?q=" + encodeURIComponent(value);
	}

	function setActive(i) {
		items.forEach(function (el) { el.classList.remove("on"); });
		if (i < 0 || i >= items.length) { active = -1; return; }
		active = i;
		items[i].classList.add("on");
		items[i].scrollIntoView({ block: "nearest" });
	}

	function init() {
		input = document.getElementById("searchInput");
		box = input && input.closest(".search-box");
		if (!input || !box) { return; }

		panel = document.createElement("div");
		panel.className = "ss-panel";
		panel.setAttribute("role", "listbox");
		panel.hidden = true;
		document.body.appendChild(panel);

		input.setAttribute("autocomplete", "off");

		/* website list for the suggestions */
		fetch("/review-platform/websites").then(function (r) { return r.ok ? r.json() : []; })
			.then(function (list) {
				sites = Array.isArray(list) ? list : [];
				if (!panel.hidden) { render(); }
			}).catch(function () { sites = []; });

		input.addEventListener("focus", open);
		input.addEventListener("click", open);
		input.addEventListener("input", open);

		input.addEventListener("keydown", function (e) {
			if (panel.hidden && (e.key === "ArrowDown" || e.key === "ArrowUp")) { open(); }
			if (panel.hidden) { return; }

			if (e.key === "ArrowDown") { e.preventDefault(); setActive(active + 1 >= items.length ? 0 : active + 1); }
			else if (e.key === "ArrowUp") { e.preventDefault(); setActive(active <= 0 ? items.length - 1 : active - 1); }
			else if (e.key === "Enter" && active >= 0) { e.preventDefault(); e.stopImmediatePropagation(); go(items[active].getAttribute("data-value")); }
			else if (e.key === "Escape") { close(); }
		}, true);

		/* the normal Search button / Enter also remembers the search */
		var btn = document.getElementById("searchButton");
		if (btn) { btn.addEventListener("click", function () { saveRecent(input.value); }, true); }

		panel.addEventListener("mousedown", function (e) { e.preventDefault(); });

		panel.addEventListener("click", function (e) {
			if (e.target.closest("#ssClear")) { clearRecent(); render(); return; }
			var el = e.target.closest("[data-value]");
			if (el) { go(el.getAttribute("data-value")); }
		});

		document.addEventListener("click", function (e) {
			if (!panel.hidden && !box.contains(e.target) && !panel.contains(e.target)) { close(); }
		});

		window.addEventListener("resize", function () { if (!panel.hidden) { place(); } });
		window.addEventListener("scroll", function () { if (!panel.hidden) { place(); } }, { passive: true });
	}

	/* logo fallback for the small favicons */
	document.addEventListener("error", function (event) {
		var img = event.target;
		if (!img || img.tagName !== "IMG") { return; }
		var b = img.parentElement;
		if (b && b.classList.contains("ss-logo")) {
			b.textContent = b.getAttribute("data-letter") || "W";
			b.classList.remove("has-logo");
		}
	}, true);

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();