# MarryPlugin

Plugin simple de matrimonios para Paper/Spigot 1.21.x. Solo permite casarse
(y divorciarse), guarda un registro persistente en base de datos de quién
está casado con quién, y da a los administradores un comando para
intervenir en cualquier matrimonio.

## Requisitos

- Java 21 o superior
- Maven 3.6+
- Servidor Paper o Spigot 1.21.x

## Cómo compilar

Desde la carpeta del proyecto:

```
mvn clean package
```

Esto genera `target/MarryPlugin.jar`, ya con el driver de SQLite incluido
adentro (no hace falta instalar nada más en el servidor). Copiá ese archivo
a la carpeta `plugins/` y reiniciá.

> Nota: la primera compilación necesita descargar el `paper-api` y el
> driver `sqlite-jdbc` desde internet, así que la máquina donde compiles
> necesita acceso a internet. El servidor donde corre el plugin no
> necesita internet para nada de esto.

## Comandos de jugador

| Comando | Descripción |
|---|---|
| `/marry <jugador>` | Propone matrimonio a otro jugador |
| `/marry accept` | Acepta la propuesta pendiente |
| `/marry deny` | Rechaza la propuesta pendiente |
| `/marry cancel` | Cancela la propuesta que enviaste |
| `/divorce` | Pide confirmación para divorciarte |
| `/divorce confirm` | Confirma el divorcio |
| `/married [jugador]` | Consulta tu estado civil o el de otro jugador |

## Comando de administrador

| Comando | Descripción |
|---|---|
| `/marryadmin divorce <jugador>` | Separa a la fuerza a un jugador de su pareja |
| `/marryadmin force <j1> <j2>` | Casa a la fuerza a dos jugadores (rompe matrimonios previos si los tenían) |
| `/marryadmin list` | Lista todos los matrimonios actualmente registrados |
| `/marryadmin info <jugador>` | Muestra el matrimonio actual de un jugador específico |
| `/marryadmin history <jugador>` | Muestra el historial completo de un jugador (casamientos y divorcios pasados) |
| `/marryadmin reload` | Recarga `config.yml` **y** vuelve a leer todos los matrimonios desde la base de datos (`loadFromDatabase()`), sin reiniciar el servidor. Útil si la RAM y el archivo `.db` se desincronizan por algún motivo. |

## Seguridad RAM ↔ base de datos

`marry()` y `divorce()` solo actualizan la memoria (RAM) **después** de que la
operación en SQLite fue exitosa. Si la base de datos falla por cualquier
motivo (disco lleno, archivo bloqueado, etc.), el cambio no llega a reflejarse
en RAM y el jugador recibe un mensaje de error en vez de quedar "casado a
medias". Ambos métodos devuelven `true`/`false` según si la operación se
completó.

## Placeholders (PlaceholderAPI)

Si tenés [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
instalado en el servidor, MarryPlugin registra automáticamente estos
placeholders (no hace falta configurar nada más):

| Placeholder | Devuelve |
|---|---|
| `%marry_partner%` | Nombre de la pareja, o el texto de "soltero/a" configurado |
| `%marry_status%` | "Casado/a" o "Soltero/a" (textos configurables) |
| `%marry_since%` | Fecha de casamiento (o vacío si no está casado/a) |

Los textos y el formato de fecha se pueden cambiar en la sección
`placeholders:` de `config.yml`. Si PlaceholderAPI no está instalado, el
plugin funciona exactamente igual, simplemente esos placeholders no van a
estar disponibles (no es obligatorio tenerlo).

## Permisos

| Permiso | Default | Uso |
|---|---|---|
| `marry.use` | todos | `/marry`, `/divorce`, `/married` |
| `marry.admin` | solo OP | `/marryadmin` |

## Almacenamiento (base de datos SQLite)

Todo se guarda en `plugins/MarryPlugin/marriages.db`, un archivo de base
de datos SQLite real (no un YAML). Hay dos tablas:

- **`marriages`**: el estado actual. Una fila por matrimonio activo, con
  el UUID y nombre de ambos jugadores y la fecha en que se casaron. Cada
  casamiento o divorcio escribe inmediatamente en esta tabla, así que el
  estado sobrevive sin problemas a un reinicio o incluso a un crash del
  servidor: **nadie se divorcia solo al reiniciar**.
- **`marriage_history`**: un registro permanente de cada evento (cada
  casamiento Y cada divorcio que haya ocurrido alguna vez), aunque la
  pareja ya no esté junta. Se consulta con `/marryadmin history <jugador>`.

Las propuestas pendientes (`/marry <jugador>` sin respuesta todavía) y las
confirmaciones de `/divorce` siguen viviendo solo en memoria, a propósito:
no tiene sentido que una propuesta de hace una semana siga "viva" después
de un reinicio.

Para inspeccionar la base de datos manualmente (por ejemplo con
[DB Browser for SQLite](https://sqlitebrowser.org/)), simplemente abrí el
archivo `marriages.db` que está en la carpeta del plugin. El servidor debe
estar apagado mientras lo editás a mano, para evitar conflictos de
escritura.

## Personalización

Todos los mensajes y los tiempos de espera están en
`plugins/MarryPlugin/config.yml`, así que podés cambiar textos, colores
(con `&`) y tiempos sin tocar el código.
