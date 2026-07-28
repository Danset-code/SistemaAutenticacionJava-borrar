const form = document.getElementById("loginForm");
const mensaje = document.getElementById("mensaje");

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const datos = {
        correo: document.getElementById("correo").value,
        password: document.getElementById("password").value
    };

    mensaje.textContent = "Validando...";
    mensaje.className = "";

    try {
        const respuesta = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });

        const resultado = await respuesta.json();

        if (!respuesta.ok) {
            mensaje.textContent = resultado.message || "Credenciales incorrectas";
            mensaje.className = "error";
            return;
        }

        localStorage.setItem("usuario", JSON.stringify(resultado));
        mensaje.textContent = resultado.message;
        mensaje.className = "exito";

        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 700);
    } catch (error) {
        mensaje.textContent = "No se pudo conectar con el backend";
        mensaje.className = "error";
    }
});
