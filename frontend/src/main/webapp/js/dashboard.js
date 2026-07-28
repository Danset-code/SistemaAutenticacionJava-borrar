const usuarioGuardado = localStorage.getItem("usuario");

if (!usuarioGuardado) {
    window.location.href = "index.html";
} else {
    const usuario = JSON.parse(usuarioGuardado);
    document.getElementById("usuarioInfo").textContent =
        `Sesión iniciada para: ${usuario.nombre} (${usuario.correo})`;
}

document.getElementById("cerrarSesion").addEventListener("click", () => {
    localStorage.removeItem("usuario");
    window.location.href = "index.html";
});
