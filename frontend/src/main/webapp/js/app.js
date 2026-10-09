const isLocal =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1";

const BACKEND_HOST = isLocal
    ? "localhost:8080"
    : "backend-sistemaautenticacionjava-borrar.onrender.com";

const API = isLocal
    ? `http://${BACKEND_HOST}/api`
    : `https://${BACKEND_HOST}/api`;

const WS_URL = isLocal
    ? `ws://${BACKEND_HOST}/ws/sensores`
    : `wss://${BACKEND_HOST}/ws/sensores`;

const SENSOR_CATALOG = [
    // Ambiente
    { id: "temperatura-aire", nombre: "Temperatura", unidad: "°C" },
    { id: "humedad-aire", nombre: "Humedad relativa", unidad: "%RH" },
    { id: "co2", nombre: "CO₂", unidad: "ppm" },
    { id: "presion-atmosferica", nombre: "Presión atmosférica", unidad: "hPa" },

    // Luz y radiación
    { id: "par", nombre: "Radiación PAR", unidad: "µmol/m²/s" },
    { id: "radiacion-solar", nombre: "Radiación solar", unidad: "W/m²" },
    { id: "uv", nombre: "Índice UV", unidad: "UV Index" },

    // Planta / superficie foliar
    { id: "temperatura-hoja", nombre: "Temperatura de hoja", unidad: "°C" },
    { id: "humedad-foliar", nombre: "Humedad foliar", unidad: "%" },

    // Sustrato / suelo
    { id: "humedad-sustrato", nombre: "Humedad del sustrato", unidad: "%VWC" },
    { id: "temperatura-sustrato", nombre: "Temperatura del sustrato", unidad: "°C" },
    { id: "ph-sustrato", nombre: "pH del sustrato", unidad: "pH" },
    { id: "ec-sustrato", nombre: "Conductividad eléctrica del sustrato", unidad: "mS/cm" },

    // Agua / fertirriego
    { id: "temperatura-agua", nombre: "Temperatura del agua", unidad: "°C" },
    { id: "ph-agua", nombre: "pH del agua", unidad: "pH" },
    { id: "ec-agua", nombre: "Conductividad eléctrica del agua", unidad: "mS/cm" },
    { id: "oxigeno-disuelto", nombre: "Oxígeno disuelto", unidad: "mg/L" },
    { id: "orp", nombre: "Potencial ORP", unidad: "mV" },
    { id: "salinidad", nombre: "Salinidad", unidad: "ppt" },
    { id: "nivel-agua", nombre: "Nivel de agua", unidad: "%" },
    { id: "caudal", nombre: "Caudal de riego", unidad: "L/min" },
    { id: "presion-riego", nombre: "Presión de riego", unidad: "bar" },
    { id: "nitratos", nombre: "Nitratos (NO₃⁻)", unidad: "mg/L" },
];

let accessToken = null;

const state = {
    page: "dashboard",
    user: null,
    devices: [],
    dashboardDeviceId: "",
    pairingCode: null,
    sensors: [],
    charts: [],
    readings: [],
    reportData: [],
    ws: null,
    wsConnected: false,
    chartHistory: {}
};

const app = document.getElementById("app");

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
    }[ch]));
}

async function api(path, options = {}) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (accessToken && !headers.Authorization) headers.Authorization = `Bearer ${accessToken}`;
    const response = await fetch(API + path, { ...options, headers });
    const text = await response.text();
    let data = {};
    try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }
    if (response.status === 401 && accessToken) {
        accessToken = null;
        state.user = null;
        state.devices = [];
        state.dashboardDeviceId = "";
        state.sensors = [];
        state.charts = [];
        state.readings = [];
        state.reportData = [];
        state.chartHistory = {};
        document.getElementById("user-profile-modal")?.remove();
        if (state.ws) state.ws.close();
        state.ws = null;
        state.wsConnected = false;
        render();
    }
    if (!response.ok) throw new Error(data.message || "Error de comunicación con la API");
    return data;
}

function render() {
    if (!state.user) {
        renderLogin();
        return;
    }

    app.innerHTML = `
        <div class="layout">
            <aside class="sidebar">
                <div class="brand">Monitoreo</div>
                <nav class="nav">
                    <button class="${state.page === "dashboard" ? "active" : ""}" onclick="navigate('dashboard')">Dashboard</button>
                    <button class="${state.page === "sensores" ? "active" : ""}" onclick="navigate('sensores')">Sensores</button>
                    <button class="${state.page === "dispositivos" ? "active" : ""}" onclick="navigate('dispositivos')">Dispositivos</button>
                    <button class="${state.page === "reportes" ? "active" : ""}" onclick="navigate('reportes')">Reportes</button>
                </nav>
                <div style="padding:20px;font-size:14px;">
                    <span class="ws-dot ${state.wsConnected ? "ok" : ""}"></span>
                    ${state.wsConnected ? "Tiempo real conectado" : "Conectando..."}
                    <br><br>${escapeHtml(state.user.nombre)}
                    <br><button class="btn btn-secondary" style="margin-top:12px" onclick="openUserProfile()">Mi perfil</button>
                    <button class="btn btn-secondary" style="margin-top:8px" onclick="logout()">Cerrar sesión</button>
                </div>
            </aside>
            <main class="main" id="main-content"></main>
        </div>
    `;

    if (state.page === "dashboard") renderDashboard();
    if (state.page === "sensores") renderSensors();
    if (state.page === "dispositivos") renderDevices();
    if (state.page === "reportes") renderReports();
}

function renderLogin() {
    app.innerHTML = `
    <div class="login-page">
      <div class="login-box">
        <h1>Monitoreo de Cultivo</h1>
        <p class="muted">Sistema de visualización de datos ambientales</p>
        <div class="login-tabs">
          <button id="tabLogin" class="active" onclick="showLoginTab('login')">Iniciar sesión</button>
          <button id="tabRegister" onclick="showLoginTab('register')">Registrarse</button>
        </div>
        <div id="auth-form"></div>
      </div>
    </div>`;
    showLoginTab("login");
}

function showForgotPassword() {
    const form = document.getElementById("auth-form");

    form.innerHTML = `
        <div id="auth-message"></div>

        <form onsubmit="forgotPassword(event)">
            <div class="form-row">
                <label>Correo</label>
                <input
                    id="forgotCorreo"
                    class="form-control"
                    type="email"
                    required
                >
            </div>

            <button class="btn btn-primary full">
                Recuperar contraseña
            </button>
        </form>

        <p style="margin-top:15px">
            <a href="#" onclick="showLoginTab('login'); return false;">
                Volver al inicio de sesión
            </a>
        </p>
    `;
}

async function forgotPassword(event) {
    event.preventDefault();

    const correo = document.getElementById("forgotCorreo").value;

    try {
        const result = await api("/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({ correo })
        });

        document.getElementById("auth-message").innerHTML =
            `<div class="success">${escapeHtml(result.message)}</div>`;

    } catch (e) {
        document.getElementById("auth-message").innerHTML =
            `<div class="error">${escapeHtml(e.message)}</div>`;
    }
}

function showLoginTab(mode) {
    document.getElementById("tabLogin").classList.toggle("active", mode === "login");
    document.getElementById("tabRegister").classList.toggle("active", mode === "register");
    const form = document.getElementById("auth-form");

    if (mode === "login") {
        form.innerHTML = `
            <div id="auth-message"></div>

            <form onsubmit="login(event)">
                <div class="form-row">
                    <label>Correo</label>
                    <input
                        id="loginCorreo"
                        class="form-control"
                        type="email"
                        required
                    >
                </div>

                <div class="form-row">
                    <label>Contraseña</label>
                    <input
                        id="loginPassword"
                        class="form-control"
                        type="password"
                        required
                    >
                </div>

                <button class="btn btn-primary full">
                    Iniciar sesión
                </button>
            </form>

            <p style="margin-top:15px">
                <a href="#" onclick="showForgotPassword(); return false;">
                    ¿Olvidaste tu contraseña?
                </a>
            </p>

            <p class="muted" style="margin-top:18px">
                Prueba: admin@monitoreo.com / Admin123
            </p>
        `;
    } else {
        form.innerHTML = `
        <div id="auth-message"></div>
        <form onsubmit="register(event)">
          <div class="form-row"><label>Nombre</label><input id="regNombre" class="form-control" required></div>
          <div class="form-row"><label>Correo</label><input id="regCorreo" class="form-control" type="email" required></div>
          <div class="form-row"><label>Contraseña</label><input id="regPassword" class="form-control" type="password" minlength="6" required></div>
          <button class="btn btn-primary full">Registrar usuario</button>
        </form>`;
    }
}

async function login(event) {
    event.preventDefault();
    const box = document.getElementById("auth-message");
    try {
        const data = await api("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                correo: document.getElementById("loginCorreo").value,
                password: document.getElementById("loginPassword").value
            })
        });
        accessToken = data.accessToken;
        if (!accessToken) throw new Error("El backend no devolvió el token de sesión.");
        state.user = data;
        state.dashboardDeviceId = "";
        state.page = "dashboard";
        await loadData();
        await loadDevices();
        connectWebSocket();
        render();
    } catch (e) {
        box.innerHTML = `<div class="alert error">${escapeHtml(e.message)}</div>`;
    }
}

async function register(event) {
    event.preventDefault();
    const box = document.getElementById("auth-message");
    try {
        await api("/auth/register", {
            method: "POST",
            body: JSON.stringify({
                nombre: document.getElementById("regNombre").value,
                correo: document.getElementById("regCorreo").value,
                password: document.getElementById("regPassword").value
            })
        });
        box.innerHTML = `<div class="alert success">Usuario registrado. Ahora inicia sesión.</div>`;
        setTimeout(() => showLoginTab("login"), 700);
    } catch (e) {
        box.innerHTML = `<div class="alert error">${escapeHtml(e.message)}</div>`;
    }
}

async function openUserProfile() {
    if (!state.user?.usuarioId) {
        alert("No hay un usuario activo.");
        return;
    }

    try {
        const usuario = await api(`/auth/usuarios/${state.user.usuarioId}`);
        state.user = usuario;
        
        const esAdmin = usuario.correo?.toLowerCase() === "admin@monitoreo.com";

        const modal = document.createElement("div");
        modal.id = "user-profile-modal";
        modal.className = "modal-backdrop";
        modal.innerHTML = `
            <div class="modal profile-modal">
                <h2>Mi perfil</h2>
                <p class="muted" style="margin-top:-8px;margin-bottom:18px;">
                    Actualiza los datos de tu cuenta. Deja la contraseña vacía para conservar la actual.
                </p>
                <div id="profile-message"></div>
                <form onsubmit="updateUser(event)">
                    <div class="form-row">
                        <label>Nombre</label>
                        <input id="profileNombre" class="form-control" maxlength="100" required value="${escapeHtml(usuario.nombre)}">
                    </div>
                    <div class="form-row">
                        <label>Correo</label>
                        <input id="profileCorreo" class="form-control" type="email" maxlength="150" required value="${escapeHtml(usuario.correo)}">
                    </div>
                        ${!esAdmin ? `
                            <div class="form-row">
                                <label>Nueva contraseña <span class="muted">(opcional)</span></label>
                                <input id="profilePassword"
                                       class="form-control"
                                       type="password"
                                       minlength="6"
                                       maxlength="100"
                                       placeholder="Dejar vacía para no cambiarla">
                            </div>
                            ` : `
                            <div class="alert" style="margin-top:10px;">
                                La contraseña del administrador no puede modificarse.
                            </div>
                       `}
                    <div style="display:flex;gap:10px;justify-content:flex-end;flex-wrap:wrap;margin-top:18px;">
                        ${!esAdmin ? `
                        <button type="button"
                                class="btn btn-danger"
                                onclick="deleteUserAccount()">
                            Eliminar cuenta
                        </button>
                        ` : ""}
                        <button type="button" class="btn btn-secondary" onclick="closeUserProfile()">Cancelar</button>
                        <button class="btn btn-primary">Guardar cambios</button>
                    </div>
                </form>
            </div>
        `;
        document.body.appendChild(modal);
    } catch (e) {
        alert(e.message || "No se pudo cargar el perfil");
    }
}

async function updateUser(event) {
    const esAdmin = state.user?.correo?.toLowerCase() === "admin@monitoreo.com";
    event.preventDefault();

    if (!state.user?.usuarioId) {
        alert("No hay un usuario activo.");
        return;
    }

    const message = document.getElementById("profile-message");
    try {
        const passwordInput = document.getElementById("profilePassword");
        const password = passwordInput ? passwordInput.value : "";

        if (esAdmin && password) {
            alert("La contraseña del administrador no puede modificarse.");
            return;
        }
        const data = await api(`/auth/usuarios/${state.user.usuarioId}`, {
            method: "PUT",
            body: JSON.stringify({
                nombre: document.getElementById("profileNombre").value,
                correo: document.getElementById("profileCorreo").value,
                password: password || null
            })
        });

        state.user = data;

        if (message) {
            message.innerHTML = `<div class="alert success">${escapeHtml(data.message || "Usuario actualizado correctamente")}</div>`;
        }

        setTimeout(() => {
            closeUserProfile();
            render();
        }, 500);
    } catch (e) {
        if (message) {
            message.innerHTML = `<div class="alert error">${escapeHtml(e.message || "No se pudo actualizar el usuario")}</div>`;
        }
    }
}

async function deleteUserAccount() {
    if (!state.user?.usuarioId) {
        alert("No hay un usuario activo.");
        return;
    }

    const confirmar = confirm(
        "¿Seguro que deseas eliminar tu cuenta? Esta acción elimina el usuario y sus datos de recuperación de contraseña y no se puede deshacer."
    );
    if (!confirmar) return;

    try {
        await api(`/auth/usuarios/${state.user.usuarioId}`, { method: "DELETE" });

        if (state.ws) state.ws.close();
        accessToken = null;
        state.user = null;
        state.devices = [];
        state.sensors = [];
        state.charts = [];
        state.readings = [];
        state.reportData = [];
        state.chartHistory = {};

        closeUserProfile();
        render();
    } catch (e) {
        alert(e.message || "No se pudo eliminar el usuario");
    }
}

function closeUserProfile() {
    document.getElementById("user-profile-modal")?.remove();
}

async function logout() {
    const token = accessToken;
    if (state.ws) state.ws.close();
    state.ws = null;
    state.wsConnected = false;
    try {
        if (token) await api("/auth/logout", { method: "POST" });
    } catch (e) {
        console.warn("La sesión local se cerró aunque el servidor no confirmó el cierre:", e.message);
    }
    accessToken = null;
    state.user = null;
    state.devices = [];
    state.dashboardDeviceId = "";
    state.sensors = [];
    state.charts = [];
    state.readings = [];
    state.reportData = [];
    state.chartHistory = {};
    render();
}

function navigate(page) {
    state.page = page;
    render();
    if (page === "dashboard") renderCharts();
    if (page === "dispositivos" && state.user) loadDevices().then(renderDevices);
}

async function loadData() {
    try {
        state.sensors = await api("/sensores");
        state.charts = await api("/graficos?activos=true");
        state.readings = await api("/lecturas");
        seedHistory();
    } catch (e) {
        console.error(e);
    }
}

async function refreshSensorStatuses() {
    try {
        const sensors = await api("/sensores");

        sensors.forEach(updated => {
            const sensor = state.sensors.find(s => s.id === updated.id);

            if (sensor) {
                sensor.estado = updated.estado;
                sensor.ultimaLectura = updated.ultimaLectura;
                sensor.valorActual = updated.valorActual;
            }
        });

        if (state.page === "sensores") {
            renderSensors();
        }
    } catch (e) {
        console.error("Error actualizando estados de sensores:", e);
    }
}

async function loadDevices() {
    if (!state.user) return;
    try { state.devices = await api("/dispositivos"); }
    catch (e) { console.error("No se pudieron cargar los dispositivos:", e); state.devices = []; }
}

function renderDevices() {
    const main = document.getElementById("main-content");
    const paired = state.devices.filter(d => d.hardwareId);
    main.innerHTML = `
      <div class="topbar"><h1>Dispositivos físicos</h1><button class="btn btn-primary" onclick="createPairingCode()">Vincular dispositivo</button></div>
      <p class="muted" style="margin-bottom:18px;">El nombre device1, device2… se asigna automáticamente. No escribas un nombre de placa ni un ID manualmente: el microcontrolador envía su identificador de hardware.</p>
      <div class="card table-card"><table><thead><tr><th>Nombre asignado</th><th>Identificador detectado</th><th>Vinculación</th></tr></thead><tbody>
      ${state.devices.map(d => `<tr><td><strong>${escapeHtml(d.alias)}</strong></td><td>${escapeHtml(d.hardwareId || "Se asignará al conectar el equipo")}</td><td>${escapeHtml(d.estadoVinculacion)}</td></tr>`).join("") || `<tr><td colspan="3" class="empty">Todavía no has vinculado dispositivos.</td></tr>`}
      </tbody></table></div>
      ${state.pairingCode ? `<div class="card" style="margin-top:16px;"><h3>Código para ${escapeHtml(state.pairingCode.alias)}</h3><p>Introduce este código de un solo uso en la configuración inicial del firmware. La placa enviará su identificador real automáticamente y recibirá una clave de dispositivo.</p><div style="font-size:18px;font-weight:700;word-break:break-all;margin:12px 0;">${escapeHtml(state.pairingCode.pairingCode)}</div><div class="toolbar"><button class="btn btn-secondary" onclick="copyPairingCode()">Copiar código</button><button class="btn btn-secondary" onclick="statePairingDismiss()">Ocultar código</button></div><p class="muted">${escapeHtml(state.pairingCode.instrucciones || "Código de un solo uso. No lo compartas.")}</p></div>` : ""}
      <div class="card" style="margin-top:16px;"><strong>¿Cómo funciona?</strong><p>1. Genera un código desde esta pantalla. 2. Configúralo en el dispositivo. 3. El dispositivo detecta/envía su hardwareId y canjea el código en <code>/api/dispositivos/vincular</code>. 4. Guarda de forma privada el <code>deviceKey</code> devuelto y úsalo en cada lectura mediante el encabezado <code>X-Device-Key</code>.</p></div>`;
}

async function createPairingCode() {
    try {
        state.pairingCode = await api("/dispositivos/codigo-vinculacion", { method: "POST" });
        await loadDevices();
        renderDevices();
    } catch (e) { alert(e.message || "No se pudo generar el código de vinculación."); }
}

async function copyPairingCode() {
    if (!state.pairingCode?.pairingCode) return;
    try { await navigator.clipboard.writeText(state.pairingCode.pairingCode); alert("Código copiado."); }
    catch { alert("Copia manualmente este código: " + state.pairingCode.pairingCode); }
}
function statePairingDismiss() { state.pairingCode = null; renderDevices(); }

function seedHistory() {
    state.sensors.forEach(s => {
        const values = state.readings.filter(r => r.sensor.id === s.id).slice(0, 18).reverse();
        state.chartHistory[s.id] = values.map(r => ({
            value: r.valor,
            date: r.fecha
        }));
    });
}

function normalizeSensorType(type) {
    return String(type || "").trim().toLowerCase().replaceAll("co₂", "co2");
}

function metricFor(type) {
    const sensor = state.sensors.find(s => normalizeSensorType(s.tipo) === normalizeSensorType(type));
    return sensor || { valorActual: 0, unidad: "" };
}

function getDashboardDevices() {
    // Usa los dispositivos vinculados y, como respaldo, los IDs presentes en los sensores.
    // El filtro se hace con hardwareId/deviceId, no con el alias visible (device1, device2…).
    const devicesById = new Map();

    state.devices.forEach(device => {
        if (!device?.hardwareId) return;
        const hardwareId = String(device.hardwareId);
        devicesById.set(hardwareId, {
            hardwareId,
            alias: device.alias || hardwareId
        });
    });

    state.sensors.forEach(sensor => {
        if (!sensor?.deviceId) return;
        const hardwareId = String(sensor.deviceId);
        const existing = devicesById.get(hardwareId);
        if (existing) {
            if ((!existing.alias || existing.alias === hardwareId) && sensor.deviceAlias) {
                existing.alias = sensor.deviceAlias;
            }
        } else {
            devicesById.set(hardwareId, {
                hardwareId,
                alias: sensor.deviceAlias || hardwareId
            });
        }
    });

    return Array.from(devicesById.values()).sort((a, b) =>
        String(a.alias).localeCompare(String(b.alias), "es", { numeric: true })
    );
}

function filterDashboardByDevice(deviceId) {
    state.dashboardDeviceId = deviceId ? String(deviceId) : "";
    renderCharts();
}

function renderDashboard() {
    const main = document.getElementById("main-content");
    const devices = getDashboardDevices();

    // Si el dispositivo seleccionado ya no está asociado a esta cuenta, regresar a "Todos".
    if (state.dashboardDeviceId && !devices.some(d => d.hardwareId === state.dashboardDeviceId)) {
        state.dashboardDeviceId = "";
    }

    main.innerHTML = `
      <div class="topbar">
        <h1>Sistema de Monitoreo de Cultivo Medicinal</h1>
        <button class="btn btn-primary" onclick="openChartModal()">Crear gráfico</button>
      </div>
      <div class="toolbar" style="align-items:center;margin-top:4px;">
        <label for="dashboardDeviceFilter" style="font-weight:600;align-self:center;">Filtrar por dispositivo:</label>
        <select class="input" id="dashboardDeviceFilter" onchange="filterDashboardByDevice(this.value)" aria-label="Filtrar gráficos por dispositivo">
          <option value="" ${state.dashboardDeviceId === "" ? "selected" : ""}>Todos los dispositivos</option>
          ${devices.map(d => `
            <option value="${escapeHtml(d.hardwareId)}" ${state.dashboardDeviceId === d.hardwareId ? "selected" : ""}>
              ${escapeHtml(d.alias)}
            </option>
          `).join("")}
        </select>
        <span class="muted">Se muestran los gráficos de los sensores asociados al dispositivo elegido.</span>
      </div>
      <div class="grid-charts" id="charts-container"></div>
      <div id="modal-container"></div>
    `;
    renderCharts();
}

function formatValue(s) {
    const value = Number(s?.valorActual || 0);
    const type = findSensorDefinition(s?.tipo);
    const decimals = type && ["CO₂", "Nitratos (NO₃⁻)"].includes(type.nombre) ? 0 : 1;
    return value.toFixed(decimals);
}

function renderCharts() {
    const container = document.getElementById("charts-container");
    if (!container) return;

    const visibleCharts = state.dashboardDeviceId
        ? state.charts.filter(g => String(g.sensor?.deviceId || "") === state.dashboardDeviceId)
        : state.charts;

    if (!visibleCharts.length) {
        if (!state.charts.length) {
            container.innerHTML = `<div class="card empty">No hay gráficos activos. Usa "Crear gráfico" para agregar uno.</div>`;
        } else if (state.dashboardDeviceId) {
            const device = getDashboardDevices().find(d => d.hardwareId === state.dashboardDeviceId);
            container.innerHTML = `<div class="card empty">No hay gráficos activos para ${escapeHtml(device?.alias || "este dispositivo")}. Selecciona otro dispositivo o crea un gráfico para uno de sus sensores.</div>`;
        } else {
            container.innerHTML = `<div class="card empty">No hay gráficos para mostrar.</div>`;
        }
        return;
    }

    container.innerHTML = visibleCharts.map(g => `
        <div class="card chart-card">
          <div class="chart-head">
            <div class="chart-title">${escapeHtml(g.nombre)}</div>
            <button class="btn btn-danger" onclick="deleteChart(${g.id})">Eliminar</button>
          </div>
          <div class="muted" style="margin-bottom:8px">
            ${escapeHtml(g.sensor?.deviceAlias || g.sensor?.deviceId || "Sin dispositivo")} ·
            ${escapeHtml(g.sensor?.tipo || "Sensor")} · ${escapeHtml(g.tipo)}
          </div>
          <div class="chart-wrap"><canvas id="chart-${g.id}"></canvas></div>
        </div>`).join("");

    visibleCharts.forEach(g => drawChart(g));
}

function drawChart(g) {
    const canvas = document.getElementById(`chart-${g.id}`);
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    const rect = canvas.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);
    const w = rect.width, h = rect.height;
    ctx.clearRect(0,0,w,h);

    const history = state.chartHistory[g.sensor.id] || [];
    const values = history.length ? history.map(x => Number(x.value)) : [Number(g.sensor.valorActual)];
    const min = Math.min(...values), max = Math.max(...values);
    const pad = (max - min || 1) * .15;
    const lo = min - pad, hi = max + pad;

    ctx.strokeStyle = "#d5d9df";
    ctx.lineWidth = 1;
    for (let i=0;i<5;i++) {
        const y = 20 + i*(h-45)/4;
        ctx.beginPath(); ctx.moveTo(45,y); ctx.lineTo(w-15,y); ctx.stroke();
    }

    ctx.fillStyle = "#4b5563";
    ctx.font = "12px Arial";
    ctx.fillText(hi.toFixed(1), 5, 23);
    ctx.fillText(lo.toFixed(1), 5, h-25);

    if (g.tipo === "BARRAS") {
        const bw = Math.max(4, (w-70)/values.length - 5);
        values.forEach((v,i) => {
            const x = 50 + i*(bw+5);
            const y = h-30 - ((v-lo)/(hi-lo))*(h-55);
            ctx.fillStyle = "#08b737";
            ctx.fillRect(x,y,bw,h-30-y);
        });
    } else {
        ctx.strokeStyle = "#08b737";
        ctx.lineWidth = 3;
        ctx.beginPath();
        values.forEach((v,i) => {
            const x = 50 + i*((w-70)/Math.max(1,values.length-1));
            const y = h-30 - ((v-lo)/(hi-lo))*(h-55);
            if (i===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
        });
        ctx.stroke();
        if (g.tipo === "AREA") {
            ctx.lineTo(w-20,h-30);
            ctx.lineTo(50,h-30);
            ctx.closePath();
            ctx.fillStyle = "rgba(8,183,55,.15)";
            ctx.fill();
        }
    }

    ctx.fillStyle = "#111";
    ctx.font = "13px Arial";
    ctx.fillText(`${g.sensor.tipo}: ${Number(g.sensor.valorActual).toFixed(1)} ${g.sensor.unidad}`, 55, h-7);
}

function findSensorDefinition(typeOrName) {
    const normalized = normalizeSensorType(typeOrName);
    return SENSOR_CATALOG.find(item =>
        normalizeSensorType(item.nombre) === normalized || item.id === normalized
    ) || null;
}

function buildChartSensorOptions() {
    const options = [];

    state.sensors.forEach(s => {
        const visibleDevice = s.deviceAlias || s.deviceId;
        const link = visibleDevice ? ` · ${visibleDevice}${s.canal ? ` / canal ${s.canal}` : ""}` : "";
        options.push(`<option value="sensor:${escapeHtml(s.id)}">${escapeHtml(s.nombre)} · ${escapeHtml(s.tipo)}${escapeHtml(link)}</option>`);
    });

    SENSOR_CATALOG.forEach(t => {
        options.push(`<option value="tipo:${escapeHtml(t.id)}">Nuevo ${escapeHtml(t.nombre)}</option>`);
    });

    return options.join("");
}

function openChartModal() {

    const modal = document.getElementById("modal-container");

    if (!modal) return;

    /*
     * Solo mostramos sensores que:
     *
     * 1. Tienen deviceId
     * 2. Tienen canal
     * 3. Todavía NO tienen un gráfico activo
     *
     * El sensor puede existir perfectamente sin gráfico.
     */
    const availableSensors = state.sensors
        .filter(s =>
            s.deviceId &&
            s.canal !== undefined &&
            s.canal !== null &&
            String(s.canal).trim() !== "" &&
            !state.charts.some(g =>
                g.sensor &&
                String(g.sensor.id) === String(s.id)
            )
        )
        .sort((a, b) => {

            const deviceCompare =
                String(a.deviceId).localeCompare(
                    String(b.deviceId)
                );

            if (deviceCompare !== 0) {
                return deviceCompare;
            }

            return String(a.canal).localeCompare(
                String(b.canal),
                undefined,
                { numeric: true }
            );
        });

    const options = availableSensors
        .map(s => `
            <option value="${escapeHtml(s.id)}">
                ${escapeHtml(s.deviceAlias || s.deviceId)}
                · Canal ${escapeHtml(s.canal)}
                · ${escapeHtml(s.tipo)}
            </option>
        `)
        .join("");

    modal.innerHTML = `
        <div class="modal-backdrop">

            <div class="modal">

                <h2>Crear gráfico</h2>

                <form onsubmit="createChart(event)">

                    <div class="form-row">
                        <label>
                            Nombre del gráfico
                        </label>

                        <input
                            id="chartNombre"
                            class="form-control"
                            placeholder="Ej. Temperatura Invernadero 01"
                            maxlength="100"
                            required
                        >
                    </div>

                    <div class="form-row">

                        <label>
                            Canal del microcontrolador
                        </label>

                        <select
                            id="chartSensor"
                            class="form-control"
                            required
                        >

                            ${
                                options ||
                                `<option value="">
                                    No hay canales disponibles para graficar
                                </option>`
                            }

                        </select>

                    </div>

                    <div
                        id="chart-channel-info"
                        class="hint"
                        style="margin-top:-4px;margin-bottom:14px;"
                    ></div>

                    <div class="form-row">

                        <label>
                            Tipo de gráfico
                        </label>

                        <select
                            id="chartTipo"
                            class="form-control"
                            required
                        >

                            <option value="LINEAL">
                                Línea
                            </option>

                            <option value="AREA">
                                Área
                            </option>

                            <option value="BARRAS">
                                Barras
                            </option>

                        </select>

                    </div>

                    <div
                        style="
                            display:flex;
                            gap:10px;
                            justify-content:flex-end;
                            margin-top:18px;
                        "
                    >

                        <button
                            type="button"
                            class="btn btn-secondary"
                            onclick="closeModal()"
                        >
                            Cancelar
                        </button>

                        <button
                            class="btn btn-primary"
                            ${
                                availableSensors.length === 0
                                    ? "disabled"
                                    : ""
                            }
                        >
                            Crear gráfico
                        </button>

                    </div>

                </form>

            </div>

        </div>
    `;

    const select =
        document.getElementById("chartSensor");

    const info =
        document.getElementById("chart-channel-info");

    function updateChannelInfo() {

        if (!select || !info) return;

        const sensor =
            state.sensors.find(
                s =>
                    String(s.id) ===
                    String(select.value)
            );

        if (!sensor) {

            info.textContent =
                "No hay un canal seleccionado.";

            return;
        }

        info.innerHTML = `
            <b>Microcontrolador:</b>
            ${escapeHtml(sensor.deviceAlias || sensor.deviceId)}

            ·

            <b>Canal:</b>
            ${escapeHtml(sensor.canal)}</br>

            <b>Sensor:</b>
            ${escapeHtml(sensor.tipo)}

            ·

            <b>Unidad:</b>
            ${escapeHtml(sensor.unidad)}
        `;
    }

    if (select) {

        select.addEventListener(
            "change",
            updateChannelInfo
        );

        updateChannelInfo();
    }
}

function toggleChartLinkFields() {
    const select = document.getElementById("chartSensor");
    const fields = document.getElementById("chart-link-fields");
    if (!select || !fields) return;
    fields.style.display = select.value.startsWith("tipo:") ? "block" : "none";
}

function closeModal() {
    const m = document.querySelector(".modal-backdrop");
    if (m) m.remove();
}

async function createChart(event) { 
    event.preventDefault(); 
    try {  
        const sensorId = Number( document.getElementById( "chartSensor" ).value ); 
        const chartNombre = document.getElementById( "chartNombre" ).value.trim(); 
        const chartTipo = document.getElementById( "chartTipo" ).value; 
        
        if (!chartNombre) { 
            throw new Error( "Debes ingresar un nombre para el gráfico." ); 
        } 
        if (!sensorId) { 
            throw new Error( "Debes seleccionar un canal disponible." ); 
        } 
        const sensor = state.sensors.find( s => Number(s.id) === sensorId ); 
        if (!sensor) { 
            throw new Error( "El canal seleccionado ya no existe." ); 
        } 
        const nuevoGrafico = await api( "/graficos", { 
            method:"POST", 
            body: JSON.stringify({ 
                nombre: chartNombre, 
                tipo: chartTipo, 
                sensorId: sensorId, 
                activo: true }) } );
        const readings = await api( `/lecturas/sensor/${sensorId}` ); 
        const history = Array.isArray(readings) ? readings 
                .slice(0,100) 
                .reverse() 
                .map(r => ({ 
            value: Number(r.valor), 
            date: r.fecha })) : []; 
        state.chartHistory[sensorId] = history; 
        state.charts = await api( "/graficos?activos=true" );  
        closeModal();  
        renderDashboard();
        
        await api(`/sensores/${sensorId}`, {
            method: "PUT",
            body: JSON.stringify({
                nombre: chartNombre,
                tipo: sensor.tipo,
                unidad: sensor.unidad,
                valorActual: sensor.valorActual ?? 0,
                estado: sensor.estado,
                deviceId: sensor.deviceId,
                canal: sensor.canal
            })
        });
    } 
    catch (e) { console.error( "Error creando gráfico:", e ); 
        alert( e.message || "No se pudo crear el gráfico." ); 
    } 
}

async function deleteChart(id) {
    if (!confirm("¿Eliminar este gráfico? El sensor asociado también desaparecerá de la sección Sensores si no tiene otro gráfico.")) return;
    try {
        await api(`/graficos/${id}`, { method:"DELETE" });
        await loadData();
        renderDashboard();
    } catch(e) { alert(e.message); }
}

function openSensorModal() {
    const modal = document.getElementById("modal-container");
    if (!modal) return;
    const devices = state.devices.filter(d => d.hardwareId);
    if (!devices.length) {
        alert("Primero vincula un dispositivo en la sección Dispositivos. El ID físico será detectado automáticamente.");
        navigate("dispositivos");
        return;
    }
    modal.innerHTML = `<div class="modal-backdrop"><div class="modal"><h2>Agregar canal de sensor</h2><form onsubmit="createSensor(event)">
      <div class="form-row"><label>Nombre del sensor</label><input id="sensorNombre" class="form-control" maxlength="100" required placeholder="Ej. Temperatura del invernadero"></div>
      <div class="form-row"><label>Tipo de sensor</label><select id="sensorTipoCatalogo" class="form-control" required>${SENSOR_CATALOG.map(t => `<option value="${escapeHtml(t.id)}">${escapeHtml(t.nombre)} (${escapeHtml(t.unidad)})</option>`).join("")}</select></div>
      <div class="form-row"><label>Dispositivo vinculado</label><select id="sensorDeviceSelect" class="form-control" required>${devices.map(d => `<option value="${escapeHtml(d.hardwareId)}">${escapeHtml(d.alias)}</option>`).join("")}</select><p class="muted">El identificador físico no se introduce ni se nombra manualmente.</p></div>
      <div class="form-row"><label>Canal</label><input id="sensorCanal" class="form-control" required maxlength="50" placeholder="Ej. 1"></div>
      <div class="form-row"><label>Valor inicial</label><input id="sensorValor" class="form-control" type="number" step="any" value="0" required></div>
      <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:18px"><button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button><button class="btn btn-primary">Guardar canal</button></div>
    </form></div></div>`;
}

async function createSensor(event) {
    event.preventDefault();
    try {
        const tipoId=document.getElementById("sensorTipoCatalogo").value;
        const tipo=SENSOR_CATALOG.find(t => t.id === tipoId);
        if (!tipo) throw new Error("Selecciona un tipo de sensor válido.");
        const deviceId=document.getElementById("sensorDeviceSelect").value;
        const canal=document.getElementById("sensorCanal").value.trim();
        if (!deviceId || !canal) throw new Error("Selecciona el dispositivo y especifica el canal.");
        await api("/sensores", { method:"POST", body:JSON.stringify({
            nombre:document.getElementById("sensorNombre").value.trim(), tipo:tipo.nombre, unidad:tipo.unidad,
            valorActual:Number(document.getElementById("sensorValor").value), deviceId, canal
        })});
        await loadData();
        closeModal();
        renderSensors();
    } catch (e) { alert(e.message || "No se pudo crear el sensor"); }
}

async function deleteSensor(id) {
    const sensor = state.sensors.find(s => Number(s.id) === Number(id));
    const nombre = sensor?.nombre || `#${id}`;

    const confirmar = confirm(
        `¿Eliminar el sensor "${nombre}"?\n\nSe eliminarán también sus gráficos y todas las lecturas almacenadas en la base de datos.`
    );
    if (!confirmar) return;

    try {
        await api(`/sensores/${id}`, { method: "DELETE" });

        await loadData();
        renderSensors();
    } catch (e) {
        alert(e.message || "No se pudo eliminar el sensor");
    }
}

function renderSensors() {
    const main = document.getElementById("main-content");

    main.innerHTML = `
      <div class="topbar">
        <h1>Sensores configurados</h1>
        <button class="btn btn-primary" onclick="openSensorModal()">Agregar canal</button>
      </div>

      <p class="muted" style="margin-bottom:20px;">
        Puedes crear tantos sensores como necesites. El sistema dispone de un catálogo base amplio de variables de cultivo y cada tipo tiene su unidad de medida correspondiente.
      </p>

      <div class="cards">
        ${state.sensors.map(s => `
            <div class="card">
                <div class="metric-label">${escapeHtml(s.tipo)}</div>
                <div class="metric-value" id="sensor-metric-${s.id}">
                    ${formatValue(s)} ${escapeHtml(s.unidad)}
                </div>
            </div>
        `).join("") || `<div class="card empty">No hay sensores configurados.</div>`}
      </div>

      <div class="card table-card">
        <table>
          <thead>
            <tr>
                <th>Sensor</th>
                <th>Tipo</th>
                <th>Dispositivo</th>
                <th>Canal</th>
                <th>Estado</th>
                <th>Última lectura</th>
                <th>Valor actual</th>
                <th>Acciones</th>
            </tr>
          </thead>
          <tbody id="sensor-table">
            ${state.sensors.map(s => `
                <tr>
                    <td>${escapeHtml(s.nombre)}</td>
                    <td>${escapeHtml(s.tipo)}</td>
                    <td>${escapeHtml(s.deviceAlias || s.deviceId || "-")}</td>
                    <td>${escapeHtml(s.canal || "-")}</td>
                    <td class="status ${s.estado === "ACTIVO" ? "active" : "inactive"}">${escapeHtml(s.estado)}</td>
                    <td>${formatDate(s.ultimaLectura)}</td>
                    <td id="sensor-row-${s.id}">${formatValue(s)} ${escapeHtml(s.unidad)}</td>
                    <td>
                        <button class="btn btn-danger" onclick="deleteSensor(${s.id})">Eliminar</button>
                    </td>
                </tr>
            `).join("") || `<tr><td colspan="8" class="empty">No hay sensores configurados.</td></tr>`}
          </tbody>
        </table>
      </div>
      <div id="modal-container"></div>
    `;
}


function renderReports() {
    const main = document.getElementById("main-content");
    const devices = getDashboardDevices();

    main.innerHTML = `
      <div class="topbar">
        <h1>Reportes del sistema</h1>
        <button class="btn btn-primary" onclick="exportReport()">
            Exportar Excel
        </button>
      </div>

      <div class="toolbar">
        <input
            class="input"
            id="reportSearch"
            placeholder="Buscar reporte..."
            oninput="filterReports()"
        >

        <select
            class="input"
            id="reportDevice"
            onchange="loadReports()"
        >
            <option value="">Todos los dispositivos</option>
            ${devices.map(d => `
                <option value="${escapeHtml(d.hardwareId)}">
                    ${escapeHtml(d.alias)} (${escapeHtml(d.hardwareId)})
                </option>
            `).join("")}
        </select>

        <select
            class="input"
            id="reportSensor"
            onchange="loadReports()"
        >
            <option value="">Todos los sensores</option>
            ${state.sensors.map(s => `
                <option value="${s.id}">
                    ${escapeHtml(s.nombre || s.tipo)}
                </option>
            `).join("")}
        </select>

        <input
            class="input"
            id="reportFrom"
            type="date"
            title="Fecha inicial"
            onchange="loadReports()"
        >

        <input
            class="input"
            id="reportTo"
            type="date"
            title="Fecha final"
            onchange="loadReports()"
        >
      </div>

      <div class="card table-card">
        <div
            id="report-info"
            class="muted"
            style="margin-bottom:12px;"
        >
            Cargando reportes...
        </div>

        <table>
            <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Dispositivo</th>
                    <th>ID</th>
                    <th>Canal</th>
                    <th>Nombre Sensor</th>
                    <th>Tipo de sensor</th>
                    <th>Valor</th>
                </tr>
            </thead>

            <tbody id="reports-table"></tbody>
        </table>
      </div>
    `;

    loadReports();
}


async function loadReports() {
    const sensorId =
        document.getElementById("reportSensor")?.value || "";

    const deviceId =
        document.getElementById("reportDevice")?.value || "";

    const desde =
        document.getElementById("reportFrom")?.value || "";

    const hasta =
        document.getElementById("reportTo")?.value || "";

    const info = document.getElementById("report-info");
    const tbody = document.getElementById("reports-table");

    if (desde && hasta && desde > hasta) {
        if (info) {
            info.textContent =
                "La fecha inicial no puede ser posterior a la fecha final.";
        }

        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty">
                        Corrige el rango de fechas.
                    </td>
                </tr>
            `;
        }

        return;
    }

    const params = new URLSearchParams();

    if (sensorId) params.set("sensorId", sensorId);
    if (deviceId) params.set("deviceId", deviceId);
    if (desde) params.set("desde", desde);
    if (hasta) params.set("hasta", hasta);

    if (info) info.textContent = "Consultando base de datos...";

    try {
        const resultado = await api(
            `/reportes?${params.toString()}`
        );

        if (!Array.isArray(resultado)) {
            throw new Error(
                "El backend no devolvió una lista de reportes."
            );
        }

        state.reportData = resultado;
        filterReports();

    } catch (e) {
        console.error("Error consultando reportes:", e);

        if (info) {
            info.textContent = `No se pudieron cargar los reportes: ${e.message}`;
        }

        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty">
                        Error al consultar los reportes.
                    </td>
                </tr>
            `;
        }
    }
}



function filterReports() {
    const q = (
        document.getElementById("reportSearch")?.value || ""
    ).trim().toLowerCase();

    const selectedDeviceId = (
        document.getElementById("reportDevice")?.value || ""
    ).trim();

    const rows = (state.reportData || []).filter(r => {
        const sensor = r.sensor || {};

        // El ID físico puede venir dentro del sensor o directamente
        // en el objeto del reporte.
        const rowDeviceId = String(
            sensor.deviceId ?? r.deviceId ?? ""
        ).trim();

        // Aplicar el filtro del dispositivo seleccionado.
        const coincideDispositivo =
            !selectedDeviceId ||
            rowDeviceId === selectedDeviceId;

        // Mantener el buscador de texto existente.
        const texto = [
            r.fecha,
            sensor.deviceAlias,
            sensor.deviceId,
            sensor.canal,
            sensor.nombre,
            sensor.tipo,
            sensor.unidad,
            r.valor
        ]
        .map(v => String(v ?? ""))
        .join(" ")
        .toLowerCase();

        return coincideDispositivo && texto.includes(q);
    });

    const tbody = document.getElementById("reports-table");
    const info = document.getElementById("report-info");

    if (!tbody) return;

    if (info) {
        info.textContent =
            `Mostrando ${rows.length} registro(s) que coinciden con los filtros.`;
    }

    tbody.innerHTML = rows.map(r => {
        const sensor = r.sensor || {};

        const valor = Number(r.valor);
        const valorTexto = Number.isFinite(valor)
            ? `${valor.toFixed(1)} ${sensor.unidad || ""}`
            : "-";

        return `
            <tr>
                <td>${escapeHtml(formatDate(r.fecha))}</td>
                <td>${escapeHtml(sensor.deviceAlias || "-")}</td>
                <td>${escapeHtml(sensor.deviceId || r.deviceId || "-")}</td>
                <td>${escapeHtml(sensor.canal || "-")}</td>
                <td>${escapeHtml(sensor.nombre || "-")}</td>
                <td>${escapeHtml(sensor.tipo || "-")}</td>
                <td>${escapeHtml(valorTexto)}</td>
            </tr>
        `;
    }).join("") || `
        <tr>
            <td colspan="7" class="empty">
                No hay registros para el dispositivo seleccionado.
            </td>
        </tr>
    `;
}


async function exportReport() {
    const sensorId =
        document.getElementById("reportSensor")?.value || "";

    const deviceId =
        document.getElementById("reportDevice")?.value || "";

    const desde =
        document.getElementById("reportFrom")?.value || "";

    const hasta =
        document.getElementById("reportTo")?.value || "";

    if (desde && hasta && desde > hasta) {
        alert("La fecha inicial no puede ser posterior a la fecha final.");
        return;
    }

    const params = new URLSearchParams();

    if (sensorId) params.set("sensorId", sensorId);
    if (deviceId) params.set("deviceId", deviceId);
    if (desde) params.set("desde", desde);
    if (hasta) params.set("hasta", hasta);

    try {
        const response = await fetch(
            `${API}/reportes/exportar?${params.toString()}`,
            {
                headers: accessToken
                    ? { Authorization: `Bearer ${accessToken}` }
                    : {}
            }
        );

        if (!response.ok) {
            const mensaje = await response.text();

            throw new Error(
                mensaje || `Error al exportar: HTTP ${response.status}`
            );
        }

        const blob = await response.blob();

        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");

        a.href = url;
        a.download = "reporte_sensores.xlsx";

        document.body.appendChild(a);
        a.click();
        a.remove();

        URL.revokeObjectURL(url);

    } catch (e) {
        console.error("Error exportando Excel:", e);
        alert(e.message || "No se pudo exportar el reporte.");
    }
}

function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString("es-CO");
}

function connectWebSocket() {
    if (!accessToken || !state.user) return;
    if (state.ws && state.ws.readyState <= 1) return;

    try {
        state.ws = new WebSocket(`${WS_URL}?token=${encodeURIComponent(accessToken)}`);
        state.ws.onopen = () => { state.wsConnected = true; render(); };
        state.ws.onclose = () => {
            state.wsConnected = false;
            if (state.user) setTimeout(connectWebSocket, 2500);
            render();
        };
        state.ws.onerror = () => { state.wsConnected = false; };
        state.ws.onmessage = event => {
            try {
                const msg = JSON.parse(event.data);
                updateSensorFromWebSocket(msg);
            } catch (e) { console.error(e); }
        };
    } catch (e) {
        console.error(e);
    }
}

function updateSensorFromWebSocket(msg) {

    /*
     * Primero intentamos localizar el sensor por ID.
     */
    let sensor = state.sensors.find(
        s =>
            String(s.id) ===
            String(msg.sensorId)
    );

    /*
     * Si no está por ID, intentamos localizarlo
     * mediante deviceId + canal.
     */
    if (!sensor && msg.deviceId && msg.canal) {

        sensor = state.sensors.find(
            s =>
                String(s.deviceId) ===
                    String(msg.deviceId)
                &&
                String(s.canal) ===
                    String(msg.canal)
        );
    }

    /*
     * SENSOR NUEVO
     *
     * Si el backend acaba de registrar un sensor
     * porque el microcontrolador comenzó a transmitir,
     * lo incorporamos inmediatamente al frontend.
     */
    if (!sensor && msg.sensorId) {

        sensor = {

            id: msg.sensorId,

            nombre:
                `Sensor ${msg.tipo || msg.sensorId}`,

            tipo:
                msg.tipo || "Sensor",

            unidad:
                msg.unidad || "",

            deviceId:
                msg.deviceId || null,

            canal:
                msg.canal || null,

            estado:
                msg.estado || "ACTIVO",

            valorActual:
                Number(msg.valor || 0),

            ultimaLectura:
                msg.fecha || null
        };

        state.sensors.push(sensor);

        /*
         * Este sensor comienza a tener historial
         * aunque todavía no tenga gráfico.
         */
        state.chartHistory[sensor.id] = [];
    }

    if (!sensor) {
        return;
    }

    /*
     * Actualizamos información del sensor.
     */
    if (msg.estado) {
        sensor.estado = msg.estado;
    }

    if (
        msg.valor !== undefined &&
        msg.valor !== null
    ) {
        sensor.valorActual =
            Number(msg.valor);
    }

    if (msg.fecha) {
        sensor.ultimaLectura =
            msg.fecha;
    }

    if (msg.deviceId) {
        sensor.deviceId = msg.deviceId;
    }
    if (msg.deviceAlias) sensor.deviceAlias = msg.deviceAlias;

    if (msg.canal !== undefined) {
        sensor.canal =
            msg.canal;
    }

    /*
     * Actualizar cualquier gráfico que esté
     * asociado a este sensor.
     *
     * IMPORTANTE:
     * aquí NO se crea ningún gráfico.
     */
    state.charts.forEach(g => {

        if (
            g.sensor &&
            String(g.sensor.id) ===
            String(sensor.id)
        ) {
            g.sensor = sensor;
        }
    });

    /*
     * Guardamos el historial de lecturas.
     *
     * Esto también ocurre cuando el sensor
     * todavía no tiene gráfico.
     */
    if (
        msg.valor !== undefined &&
        msg.valor !== null
    ) {

        if (!state.chartHistory[sensor.id]) {

            state.chartHistory[sensor.id] = [];
        }

        const history =
            state.chartHistory[sensor.id];

        const last =
            history[history.length - 1];

        if (
            !last ||
            last.date !== msg.fecha ||
            Number(last.value) !==
                Number(msg.valor)
        ) {

            history.push({

                value:
                    Number(msg.valor),

                date:
                    msg.fecha
            });

            if (history.length > 100) {
                history.shift();
            }
        }
    }

    /*
     * Tarjetas principales del dashboard.
     */
    const normalizedType =
        normalizeSensorType(
            sensor.tipo
        );

    const metricId =
        normalizedType.includes("temperatura")
            ? "metric-temp"

        : (
            normalizedType === "humedad" ||
            normalizedType.includes(
                "humedad relativa"
            )
        )
            ? "metric-hum"

        : normalizedType === "co2"
            ? "metric-co2"

        : null;

    if (metricId) {

        const el =
            document.getElementById(
                metricId
            );

        if (el) {

            el.textContent =
                `${formatValue(sensor)} ${sensor.unidad}`;
        }
    }

    /*
     * Actualización de la sección Sensores.
     */
    const metric =
        document.getElementById(
            `sensor-metric-${sensor.id}`
        );

    const row =
        document.getElementById(
            `sensor-row-${sensor.id}`
        );

    if (metric) {

        metric.textContent =
            `${formatValue(sensor)} ${sensor.unidad}`;
    }

    if (row) {

        row.textContent =
            `${formatValue(sensor)} ${sensor.unidad}`;
    }

    /*
     * Si estamos viendo Sensores, renderizamos
     * para que aparezca inmediatamente un sensor
     * recién conectado.
     */
    if (state.page === "sensores") {

        renderSensors();
    }

    /*
     * Si estamos en Dashboard, solamente
     * actualizamos los gráficos que YA EXISTEN.
     */
    else if (state.page === "dashboard") {

        state.charts.forEach(
            g => drawChart(g)
        );
    }
}

window.navigate = navigate;
window.filterDashboardByDevice = filterDashboardByDevice;
window.login = login;
window.register = register;
window.showLoginTab = showLoginTab;
window.showForgotPassword = showForgotPassword;
window.forgotPassword = forgotPassword;
window.openUserProfile = openUserProfile;
window.updateUser = updateUser;
window.deleteUserAccount = deleteUserAccount;
window.closeUserProfile = closeUserProfile;
window.logout = logout;
window.openChartModal = openChartModal;
window.closeModal = closeModal;
window.createChart = createChart;
window.createSensor = createSensor;
window.openSensorModal = openSensorModal;
window.loadDevices = loadDevices;
window.createPairingCode = createPairingCode;
window.copyPairingCode = copyPairingCode;
window.statePairingDismiss = statePairingDismiss;
window.renderDevices = renderDevices;
window.deleteSensor = deleteSensor;
window.deleteChart = deleteChart;
window.loadReports = loadReports;
window.filterReports = filterReports;
window.exportReport = exportReport;

// Si una placa termina de vincularse mientras el usuario está viendo Dispositivos,
// refresca el listado sin recargar toda la página ni borrar el código que aún se muestra.
setInterval(async () => {
    if (!accessToken || !state.user || !["dispositivos", "dashboard"].includes(state.page)) return;
    const anterior = JSON.stringify(state.devices);
    await loadDevices();
    if (!accessToken || !state.user) return;
    if (JSON.stringify(state.devices) !== anterior) {
        if (state.page === "dispositivos") renderDevices();
        else if (state.page === "dashboard") renderDashboard();
    }
}, 5000);

(async function start() {
    // No se restaura usuario/token del almacenamiento: cada carga de la URL empieza en el login.
    localStorage.removeItem("monitoreo_user");
    sessionStorage.removeItem("monitoreo_user");
    render();
})();
