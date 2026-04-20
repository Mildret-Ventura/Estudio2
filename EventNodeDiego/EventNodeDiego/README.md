<h1>🎫 EventNode</h1>

<h2>📋 Requisitos</h2>
<ul>
  <li><strong>Git</strong></li>
  <li><strong>Docker Desktop</strong> (<a href="https://www.docker.com/products/docker-desktop">descargar</a>)</li>
  <li><strong>MySQL Workbench</strong> (opcional)</li>
</ul>

<h2>🚀 Primeros pasos (5 min)</h2>

<pre>
<code>
#0. NECESARIO PARA TENER LOS NUEVOS CAMBIOS DE LA BD
docker compose down -v && docker compose up -d

# 1. Clonar repo
git clone https://github.com/tu-repo/EventNode.git
cd EventNode

# 2. Ir a rama develop
git checkout develop

# 3. Levantar base de datos
cd docker
docker-compose up -d
</code>
</pre>

<h2>🔌 Conexiones</h2>

<h3>Base de datos</h3>
<pre>
<code>
Host: localhost
Puerto: 3307
Usuario: vixo_dev
Contraseña: eventnode01
Base de datos: event_node
</code>
</pre>

<h2>📦 Comandos diarios (ejecutar desde /docker)</h2>

<table>
  <tr>
    <th>Acción</th>
    <th>Comando</th>
  </tr>
  <tr>
    <td>Iniciar BD</td>
    <td><code>docker-compose start</code></td>
  </tr>
  <tr>
    <td>Detener BD</td>
    <td><code>docker-compose stop</code></td>
  </tr>
  <tr>
    <td>Ver logs</td>
    <td><code>docker-compose logs -f</code></td>
  </tr>
  <tr>
    <td>Ver estado</td>
    <td><code>docker-compose ps</code></td>
  </tr>
  <tr>
    <td>Reiniciar</td>
    <td><code>docker-compose restart</code></td>
  </tr>
</table>

<h2>⚠️ Nota</h2>
<ul>
  <li>Cada quien tiene su propia BD local</li>
  <li>Si cambia la estructura, actualizar <code>docker/mysql/init.sql</code> y subir cambios</li>
</ul>
