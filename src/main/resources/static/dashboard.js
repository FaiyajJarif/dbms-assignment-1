document.addEventListener("DOMContentLoaded", async () => {

    const token = localStorage.getItem("auth_token");
    if (!token) {
        console.warn("No token found");
        return;
    }

    const container = document.getElementById("category-list");

    try {
        const res = await fetch("/api/dashboard/categories", {
            headers: { Authorization: "Bearer " + token }
        });

        const data = await res.json();
        container.innerHTML = "";

        Object.keys(data).forEach(parent => {
            const section = document.createElement("div");
            section.innerHTML = `<h3>${parent}</h3>`;

            data[parent].forEach(child => {
                const div = document.createElement("div");
                div.textContent = child.name;
                section.appendChild(div);
            });

            container.appendChild(section);
        });

    } catch (err) {
        console.error("Dashboard JS error:", err);
    }
});
