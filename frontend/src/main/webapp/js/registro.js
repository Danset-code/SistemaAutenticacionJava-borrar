const form = document.getElementById("registroForm");
const mensaje = document.getElementById("mensaje");

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const datos = {
        nombre: document.getElementById("nombre").value,
        correo: document.getElementById("correo").value,
        password: document.getElementById("password").value
    };

    mensaje.textContent = "Registrando usuario...";
    mensaje.className = "";

    try {
        const respuesta = await fetch("http://localhost:8080/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });

        const resultado = await respuesta.json();

        if (!respuesta.ok) {
            mensaje.textContent = resultado.message || "No fue posible registrar el usuario";
            mensaje.className = "error";
            return;
        }

        mensaje.textContent = resultado.message;
        mensaje.className = "exito";
        form.reset();
    } catch (error) {
        mensaje.textContent = "No se pudo conectar con el backend";
        mensaje.className = "error";
    }
});
