const CLAIM_API = "/review-platform/business-claims";
const urlParams = new URLSearchParams(window.location.search);
const businessId = urlParams.get("businessId");
if (businessId) {
    document.getElementById("businessId").value = businessId;
}
const claimForm = document.getElementById("claimForm");
claimForm.addEventListener("submit", function(event) {
    event.preventDefault();
    const businessId = document.getElementById("businessId").value;
    const userId = document.getElementById("userId").value;
    const message = document.getElementById("message");
    const claim = {
        businessId: Number(businessId),
        userId: Number(userId),
        status: "PENDING"
    };
    fetch(CLAIM_API, {
        method: "POST",
        headers: {"Content-Type": "application/json"
        },
        body: JSON.stringify(claim)
    })
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Claim could not be submitted");
            }
            return response.json();
        })
        .then(function(data) {
            console.log("Business Claim:", data);
            message.innerHTML ='<span class="success-message">Business claim submitted successfully!</span>';
            claimForm.reset();
        })
        .catch(function(error) {
            console.error(error);
            message.innerHTML ='<span class="error-message">Business claim could not be submitted.</span>';
        });
});