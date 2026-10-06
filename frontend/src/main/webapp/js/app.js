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
    { id: "temperatura-aire", nombre: "Temperatura del aire", unidad: "°C" },
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

const state = {
    page: "dashboard",
    user: JSON.parse(localStorage.getItem("monitoreo_user") || "null"),
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
    const response = await fetch(API + path, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    const text = await response.text();
    let data = {};
    try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }
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
                    <button class="${state.page === "reportes" ? "active" : ""}" onclick="navigate('reportes')">Reportes</button>
                </nav>
                <div style="padding:20px;font-size:14px;">
                    <span class="ws-dot ${state.wsConnected ? "ok" : ""}"></span>
                    ${state.wsConnected ? "Tiempo real conectado" : "Conectando..."}
                    <br><br>${escapeHtml(state.user.nombre)}
                    <br><button class="btn btn-secondary" style="margin-top:12px" onclick="logout()">Cerrar sesión</button>
                </div>
            </aside>
            <main class="main" id="main-content"></main>
        </div>
    `;

    if (state.page === "dashboard") renderDashboard();
    if (state.page === "sensores") renderSensors();
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
        state.user = data;
        localStorage.setItem("monitoreo_user", JSON.stringify(data));
        state.page = "dashboard";
        await loadData();
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

function logout() {
    if (state.ws) state.ws.close();
    state.user = null;
    localStorage.removeItem("monitoreo_user");
    render();
}

function navigate(page) {
    state.page = page;
    render();
    if (page === "dashboard") renderCharts();
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

function renderDashboard() {
    const main = document.getElementById("main-content");

    main.innerHTML = `
      <div class="topbar">
        <h1>Sistema de Monitoreo de Cultivo Medicinal</h1>
        <button class="btn btn-primary" onclick="openChartModal()">Crear gráfico</button>
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
    if (!state.charts.length) {
        container.innerHTML = `<div class="card empty">No hay gráficos activos. Usa "Crear gráfico".</div>`;
        return;
    }
    container.innerHTML = state.charts.map(g => `
        <div class="card chart-card">
          <div class="chart-head">
            <div class="chart-title">${escapeHtml(g.nombre)}</div>
            <button class="btn btn-danger" onclick="deleteChart(${g.id})">Eliminar</button>
          </div>
          <div class="muted" style="margin-bottom:8px">${escapeHtml(g.sensor.tipo)} · ${escapeHtml(g.tipo)}</div>
          <div class="chart-wrap"><canvas id="chart-${g.id}"></canvas></div>
        </div>`).join("");

    state.charts.forEach(g => drawChart(g));
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

    // Primero mostramos los sensores que ya existen en el sistema.
    state.sensors.forEach(s => {
        options.push(
            `<option value="sensor:${s.id}">${escapeHtml(s.nombre)} — ${escapeHtml(s.tipo)} (${escapeHtml(s.unidad)})</option>`
        );
    });

    // Después mostramos TODO el catálogo base para poder crear sensores nuevos
    // sin depender de los sensores que ya existan en la base de datos.
    SENSOR_CATALOG.forEach(t => {
        options.push(
            `<option value="tipo:${escapeHtml(t.id)}">Crear nuevo: ${escapeHtml(t.nombre)} — ${escapeHtml(t.unidad)}</option>`
        );
    });

    return options.join("");
}

function openChartModal() {
    const modal = document.getElementById("modal-container");
    if (!modal) return;

    const options = buildChartSensorOptions();
    modal.innerHTML = `
      <div class="modal-backdrop">
        <div class="modal">
          <h2>Crear gráfico</h2>
          <form onsubmit="createChart(event)">
            <div class="form-row"><label>Nombre</label><input id="chartNombre" class="form-control" placeholder="Ej. Temperatura ambiente" required></div>
            <div class="form-row"><label>Sensor</label><select id="chartSensor" class="form-control" required>
                ${options || `<option value="">No hay tipos de sensores disponibles</option>`}
            </select></div>
            <div class="form-row"><label>Tipo</label><select id="chartTipo" class="form-control"><option>LINEAL</option><option>AREA</option><option>BARRAS</option></select></div>
            <div style="display:flex;gap:10px;justify-content:flex-end"><button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button><button class="btn btn-primary">Crear</button></div>
          </form>
        </div>
      </div>`;
}

function closeModal() {
    const m = document.querySelector(".modal-backdrop");
    if (m) m.remove();
}

async function createChart(event) {
    event.preventDefault();
    try {
        const selected = document.getElementById("chartSensor").value;
        let sensorId;

        if (selected.startsWith("sensor:")) {
            sensorId = Number(selected.split(":")[1]);
        } else if (selected.startsWith("tipo:")) {
            const tipoId = selected.substring("tipo:".length);
            const tipo = SENSOR_CATALOG.find(t => t.id === tipoId);
            if (!tipo) throw new Error("El tipo de sensor seleccionado no existe en el catálogo base.");

            const nuevoSensor = await api("/sensores", {
                method: "POST",
                body: JSON.stringify({
                    nombre: `Sensor ${tipo.nombre}`,
                    tipo: tipo.nombre,
                    unidad: tipo.unidad,
                    estado: "ACTIVO",
                    valorActual: 0
                })
            });
            sensorId = nuevoSensor.id;
        } else {
            throw new Error("Debes seleccionar un sensor.");
        }

        await api("/graficos", {
            method: "POST",
            body: JSON.stringify({
                nombre: document.getElementById("chartNombre").value,
                tipo: document.getElementById("chartTipo").value,
                sensorId,
                activo: true
            })
        });

        await loadData();
        closeModal();
        renderDashboard();
    } catch (e) {
        alert(e.message);
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
    const options = SENSOR_CATALOG.map(t =>
        `<option value="${escapeHtml(t.id)}">${escapeHtml(t.nombre)} (${escapeHtml(t.unidad)})</option>`
    ).join("");

    modal.innerHTML = `
      <div class="modal-backdrop">
        <div class="modal">
          <h2>Crear sensor</h2>
          <form onsubmit="createSensor(event)">
            <div class="form-row"><label>Tipo de sensor</label><select id="sensorTipoCatalogo" class="form-control" required>${options}</select></div>
            <div class="form-row"><label>Nombre</label><input id="sensorNombre" class="form-control" placeholder="Ej. Sensor CO₂ 01" required></div>
            <div class="form-row"><label>Estado</label><select id="sensorEstado" class="form-control"><option>ACTIVO</option><option>INACTIVO</option></select></div>
            <div class="form-row"><label>Valor inicial</label><input id="sensorValor" class="form-control" type="number" step="0.1" value="0" required></div>
            <div style="display:flex;gap:10px;justify-content:flex-end"><button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button><button class="btn btn-primary">Guardar sensor</button></div>
          </form>
        </div>
      </div>`;
}

async function createSensor(event) {
    event.preventDefault();
    try {
        const tipoId = document.getElementById("sensorTipoCatalogo").value;
        const tipo = SENSOR_CATALOG.find(t => t.id === tipoId);
        if (!tipo) throw new Error("Selecciona un tipo de sensor válido.");

        await api("/sensores", {
            method: "POST",
            body: JSON.stringify({
                nombre: document.getElementById("sensorNombre").value,
                tipo: tipo.nombre,
                unidad: tipo.unidad,
                estado: document.getElementById("sensorEstado").value,
                valorActual: Number(document.getElementById("sensorValor").value)
            })
        });

        await loadData();
        closeModal();
        renderSensors();
    } catch (e) {
        alert(e.message || "No se pudo crear el sensor");
    }
}

function renderSensors() {
    const main = document.getElementById("main-content");

    main.innerHTML = `
      <div class="topbar">
        <h1>Sensores configurados</h1>
        <button class="btn btn-primary" onclick="openSensorModal()">Agregar sensor</button>
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
                <th>Estado</th>
                <th>Última lectura</th>
                <th>Valor actual</th>
            </tr>
          </thead>
          <tbody id="sensor-table">
            ${state.sensors.map(s => `
                <tr>
                    <td>${escapeHtml(s.nombre)}</td>
                    <td>${escapeHtml(s.tipo)}</td>
                    <td class="status ${s.estado === "ACTIVO" ? "active" : "inactive"}">${escapeHtml(s.estado)}</td>
                    <td>${formatDate(s.ultimaLectura)}</td>
                    <td id="sensor-row-${s.id}">${formatValue(s)} ${escapeHtml(s.unidad)}</td>
                </tr>
            `).join("") || `<tr><td colspan="5" class="empty">No hay sensores configurados.</td></tr>`}
          </tbody>
        </table>
      </div>
      <div id="modal-container"></div>
    `;
}

function renderReports() {
    const main = document.getElementById("main-content");
    main.innerHTML = `
      <div class="topbar"><h1>Reportes del sistema</h1><button class="btn btn-primary" onclick="exportReport()">Exportar Excel</button></div>
      <div class="toolbar">
        <input class="input" id="reportSearch" placeholder="Buscar reporte..." oninput="filterReports()">
        <select class="input" id="reportSensor" onchange="loadReports()"><option value="">Todos los sensores</option>${state.sensors.map(s => `<option value="${s.id}">${escapeHtml(s.tipo)}</option>`).join("")}</select>
        <input class="input" id="reportFrom" type="date" onchange="loadReports()">
        <input class="input" id="reportTo" type="date" onchange="loadReports()">
      </div>
      <div class="card table-card">
        <div id="report-info" class="muted" style="margin-bottom:12px;">
            Mostrando los 50 registros más recientes.
        </div>
        <table>
            <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Tipo Sensor</th>
                    <th>Valor</th>
                </tr>
            </thead>
            <tbody id="reports-table"></tbody>
        </table>
    </div>`;
    loadReports();
}

async function loadReports() {
    const sensor = document.getElementById("reportSensor")?.value || "";
    const from = document.getElementById("reportFrom")?.value || "";
    const to = document.getElementById("reportTo")?.value || "";
    const params = new URLSearchParams();
    if (sensor) params.set("sensorId", sensor);
    if (from) params.set("desde", from);
    if (to) params.set("hasta", to);
    try {
        state.reportData = await api(`/reportes?${params.toString()}`);
        filterReports();
    } catch(e) {
        console.error(e);
    }
}

function filterReports() {
    const q = (
        document.getElementById("reportSearch")?.value || ""
    ).toLowerCase();

    const rows = (state.reportData || []).filter(r =>
        `${r.sensor.tipo} ${r.valor} ${r.fecha}`
            .toLowerCase()
            .includes(q)
    );
    const tbody = document.getElementById("reports-table");
    const info = document.getElementById("report-info");
    if (!tbody) return;
    if (info) {
        info.textContent =
            `Mostrando ${rows.length} registro${rows.length === 1 ? "" : "s"} de un máximo de 50.`;
    }
    tbody.innerHTML = rows.map(r => `
        <tr>
            <td>${formatDate(r.fecha)}</td>
            <td>${escapeHtml(r.sensor.tipo)}</td>
            <td>
                ${Number(r.valor).toFixed(1)}
                ${escapeHtml(r.sensor.unidad)}
            </td>
        </tr>
    `).join("") || `
        <tr>
            <td colspan="3" class="empty">
                No hay registros.
            </td>
        </tr>
    `;
}

async function exportReport() {
    const sensor = document.getElementById("reportSensor")?.value || "";
    const from = document.getElementById("reportFrom")?.value || "";
    const to = document.getElementById("reportTo")?.value || "";

    const params = new URLSearchParams();

    if (sensor) {
        params.set("sensorId", sensor);
    }

    if (from) {
        params.set("desde", from);
    }

    if (to) {
        params.set("hasta", to);
    }

    try {
        const response = await fetch(
            `${API}/reportes/exportar?${params.toString()}`
        );

        if (!response.ok) {
            const text = await response.text();
            throw new Error(
                text || "No se pudo exportar el reporte"
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
        alert(
            e.message ||
            "No se pudo exportar el reporte"
        );
    }
}

function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString("es-CO");
}

function connectWebSocket() {
    if (state.ws && state.ws.readyState <= 1) return;

    try {
        state.ws = new WebSocket(WS_URL);
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
    const sensor = state.sensors.find(s => s.id === msg.sensorId);
    if (!sensor) return;

    sensor.valorActual = Number(msg.valor);
    sensor.ultimaLectura = msg.fecha;

    if (!state.chartHistory[sensor.id]) state.chartHistory[sensor.id] = [];
    state.chartHistory[sensor.id].push({value: sensor.valorActual, date: msg.fecha});
    if (state.chartHistory[sensor.id].length > 24) state.chartHistory[sensor.id].shift();

    const metricId = sensor.tipo.toLowerCase() === "temperatura" ? "metric-temp" :
                     sensor.tipo.toLowerCase() === "humedad" ? "metric-hum" :
                     sensor.tipo.toLowerCase() === "co₂" || sensor.tipo.toLowerCase() === "co2" ? "metric-co2" : null;
    if (metricId) {
        const el = document.getElementById(metricId);
        if (el) el.textContent = `${formatValue(sensor)} ${sensor.unidad}`;
    }

    const metric = document.getElementById(`sensor-metric-${sensor.id}`);
    const row = document.getElementById(`sensor-row-${sensor.id}`);
    if (metric) metric.textContent = `${formatValue(sensor)} ${sensor.unidad}`;
    if (row) row.textContent = `${formatValue(sensor)} ${sensor.unidad}`;

    if (state.page === "dashboard") state.charts.forEach(drawChart);
}

window.navigate = navigate;
window.login = login;
window.register = register;
window.showLoginTab = showLoginTab;
window.showForgotPassword = showForgotPassword;
window.forgotPassword = forgotPassword;
window.logout = logout;
window.openChartModal = openChartModal;
window.openSensorModal = openSensorModal;
window.closeModal = closeModal;
window.createChart = createChart;
window.createSensor = createSensor;
window.deleteChart = deleteChart;
window.loadReports = loadReports;
window.filterReports = filterReports;
window.exportReport = exportReport;

(async function start() {
    if (state.user) {
        await loadData();
        connectWebSocket();
    }
    render();
})();
