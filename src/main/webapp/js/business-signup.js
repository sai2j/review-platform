
const WEBSITE_API = "/review-platform/websites";

const BUSINESS_API = "/review-platform/businesses";

const BUSINESS_CLAIM_API = "/review-platform/business-claims";


/* =========================================================

GET LOGGED-IN USER

========================================================= */

function getLoggedInUser() {

	try {

		return JSON.parse(

			localStorage.getItem("loggedInUser")

		);

	} catch (error) {

		console.error(

			"Unable to read logged-in user:",

			error

		);

		return null;

	}

}


/* =========================================================

SHOW MESSAGE

========================================================= */

function showMessage(message, type) {

	const messageElement =

		document.getElementById(

			"businessSignupMessage"

		);

	if (!messageElement) {

		return;

	}

	messageElement.className =

		"business-signup-message " +

		(type || "");

	messageElement.textContent =

		message;

}


/* =========================================================

CREATE WEBSITE

========================================================= */

function createWebsite(

	name,

	url,

	description,

	category,

	country

) {

	const website = {

		name: name,

		url: url,

		description: description,

		category: category,

		country: country

	};


	return fetch(

		WEBSITE_API,

		{

			method: "POST",

			credentials: "include",

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

			return response.text()

				.then(function(errorText) {

					throw new Error(

						errorText ||

						"Website could not be created."

					);

				});

		}

		return response.json();

	});

}


/* =========================================================

CHECK EXISTING BUSINESS

========================================================= */

function getBusinessForWebsite(

	websiteId

) {

	return fetch(

		BUSINESS_API +

		"/website/" +

		websiteId,

		{

			method: "GET",

			credentials: "include"

		}

	)

	.then(function(response) {

		if (response.status === 404) {

			return null;

		}


		if (!response.ok) {

			return null;

		}


		return response.json();

	})

	.catch(function(error) {

		console.error(

			"Unable to check existing business:",

			error

		);

		return null;

	});

}


/* =========================================================

CREATE BUSINESS

========================================================= */

function createBusiness(

	name,

	url,

	email,

	description

) {

	const business = {

		name: name,

		description:

			description || "",

		officialUrl: url,

		status: "PENDING",

		businessEmail:

			email || ""

	};


	return fetch(

		BUSINESS_API,

		{

			method: "POST",

			credentials: "include",

			headers: {

				"Content-Type":

					"application/json"

			},

			body:

				JSON.stringify(business)

		}

	)

	.then(function(response) {

		if (!response.ok) {

			return response.text()

				.then(function(errorText) {

					throw new Error(

						errorText ||

						"Business could not be created."

					);

				});

		}


		return response.json();

	});

}


/* =========================================================

CREATE CLAIM

========================================================= */

function createClaim(

	businessId

) {

	const claim = {

		businessId:

			Number(businessId),

		status:

			"PENDING"

	};


	return fetch(

		BUSINESS_CLAIM_API,

		{

			method: "POST",

			credentials: "include",

			headers: {

				"Content-Type":

					"application/json"

			},

			body:

				JSON.stringify(claim)

		}

	)

	.then(function(response) {

		if (!response.ok) {

			return response.text()

				.then(function(errorText) {

					throw new Error(

						errorText ||

						"Business claim could not be submitted."

					);

				});

		}


		return response.json();

	});

}


/* =========================================================

BUSINESS SIGNUP FORM

========================================================= */

function setupBusinessSignupForm() {

	const form =

		document.getElementById(

			"businessSignupForm"

		);


	if (!form) {

		return;

	}


	form.addEventListener(

		"submit",

		function(event) {

			event.preventDefault();


			const user =

				getLoggedInUser();


			if (!user || !user.id) {

				showMessage(

					"Please login before creating a business profile.",

					"error"

				);

				return;

			}


			const businessNameElement =

				document.getElementById(

					"businessName"

				);


			const officialUrlElement =

				document.getElementById(

					"officialUrl"

				);


			const businessEmailElement =

				document.getElementById(

					"businessEmail"

				);


			const descriptionElement =

				document.getElementById(

					"description"

				);


			/* NEW: CATEGORY FIELD */

			const categoryElement =

				document.getElementById(

					"category"

				);


			/* NEW: COUNTRY FIELD */

			const countryElement =

				document.getElementById(

					"country"

				);


			const createButton =

				document.getElementById(

					"createBusinessButton"

				);


			if (

				!businessNameElement ||

				!officialUrlElement ||

				!businessEmailElement ||

				!descriptionElement ||

				!categoryElement ||

				!countryElement ||

				!createButton

			) {

				console.error(

					"Business signup form elements not found."

				);

				return;

			}


			const name =

				businessNameElement.value.trim();


			const url =

				officialUrlElement.value.trim();


			const email =

				businessEmailElement.value.trim();


			const description =

				descriptionElement.value.trim();


			/* NEW: READ CATEGORY */

			const category =

				categoryElement.value.trim();


			/* NEW: READ COUNTRY */

			const country =

				countryElement.value.trim();


			if (!name) {

				showMessage(

					"Business name is required.",

					"error"

				);

				return;

			}


			if (!url) {

				showMessage(

					"Business website URL is required.",

					"error"

				);

				return;

			}


			/* NEW: CATEGORY VALIDATION */

			if (!category) {

				showMessage(

					"Please select a business category.",

					"error"

				);

				return;

			}


			/* NEW: COUNTRY VALIDATION */

			if (!country) {

				showMessage(

					"Please select a country.",

					"error"

				);

				return;

			}


			let normalizedUrl = url;


			if (

				!normalizedUrl.startsWith("http://") &&

				!normalizedUrl.startsWith("https://")

			) {

				normalizedUrl =

					"https://" +

					normalizedUrl;

			}


			try {

				new URL(normalizedUrl);

			} catch (error) {

				showMessage(

					"Please enter a valid business website URL.",

					"error"

				);

				return;

			}


			if (email) {

				const emailPattern =

					/^[^\s@]+@[^\s@]+\.[^\s@]+$/;


				if (

					!emailPattern.test(email)

				) {

					showMessage(

						"Please enter a valid business email.",

						"error"

					);

					return;

				}

			}


			createButton.disabled =

				true;


			createButton.textContent =

				"Creating business...";


			showMessage(

				"Creating your business profile...",

				"loading"

			);


			/* =========================

			   STEP 1

			   CREATE / FIND WEBSITE

			========================= */

			createWebsite(

				name,

				normalizedUrl,

				description,

				category,

				country

			)

			.then(function(website) {

				if (

					!website ||

					!website.id

				) {

					throw new Error(

						"Website could not be created."

					);

				}


				return getBusinessForWebsite(

					website.id

				)

				.then(function(existingBusiness) {

					return {

						website:

							website,

						existingBusiness:

							existingBusiness

					};

				});

			})


			/* =========================

			   STEP 2

			   CREATE BUSINESS

			========================= */

			.then(function(result) {

				if (

					result.existingBusiness &&

					result.existingBusiness.id

				) {

					showMessage(

						"This business already exists. Opening the business profile...",

						"success"

					);


					setTimeout(function() {

						window.location.href =

							"review.html?websiteId=" +

							encodeURIComponent(

								result.website.id

							);

					}, 800);


					return null;

				}


				return createBusiness(

					name,

					normalizedUrl,

					email,

					description

				)

				.then(function(business) {

					return {

						website:

							result.website,

						business:

							business

					};

				});

			})


			/* =========================

			   STEP 3

			   CREATE CLAIM

			========================= */

			.then(function(result) {

				if (!result) {

					return null;

				}


				if (

					!result.business ||

					!result.business.id

				) {

					throw new Error(

						"Business ID was not returned."

					);

				}


				showMessage(

					"Business profile created. Submitting your ownership claim...",

					"loading"

				);


				return createClaim(

					result.business.id

				)

				.then(function() {

					return result;

				});

			})


			/* =========================

			   STEP 4

			   OPEN BUSINESS PROFILE

			========================= */

			.then(function(result) {

				if (!result) {

					return;

				}


				showMessage(

					"Business created and claim submitted successfully.",

					"success"

				);


				form.reset();


				setTimeout(function() {

					window.location.href =

						"review.html?websiteId=" +

						encodeURIComponent(

							result.website.id

						);

				}, 1000);

			})


			.catch(function(error) {

				console.error(

					"Business signup error:",

					error

				);


				showMessage(

					error.message ||

					"Unable to create business profile.",

					"error"

				);

			})


			.finally(function() {

				createButton.disabled =

					false;


				createButton.textContent =

					"Create Business Profile";

			});

		}

	);

}


/* =========================================================

PAGE LOAD

========================================================= */

document.addEventListener(

	"DOMContentLoaded",

	function() {

		setupBusinessSignupForm();

	}

);
