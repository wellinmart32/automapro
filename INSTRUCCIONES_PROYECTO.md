# INSTRUCCIONES DEL PROYECTO AUTOMAPRO

## ENTORNO
- Windows 11
- Python 3.13.3, comando: `py` (nunca `python` ni `pip` directo)
- PyInstaller 6.x
- Inno Setup 6.7
- Backend: Spring Boot + PostgreSQL (Render + Supabase)
- Frontend: Angular (Vercel)
- Ubicación Ecuador, banco Banco del Pacífico

## PASO A PASO
- Máximo 1 acción por mensaje
- Esperar confirmación (salida de consola) antes de continuar
- Nunca asumir que algo funcionó sin confirmación del usuario
- Si un paso usa tecnologías distintas (ej. SQL + CMD), darlas en mensajes separados
- Después de un commit, verificar en GitHub que el commit está en el repo correcto y contiene los archivos esperados

## COMANDOS
- Usar siempre CMD (símbolo del sistema de Windows)
- Usar siempre `py` en lugar de `python`
- No usar `type nul >` ni `echo.` para crear archivos
- Crear archivo: powershell -Command "New-Item -Path 'archivo.py' -ItemType File -Force"
- Crear carpeta: powershell -Command "New-Item -Path 'carpeta' -ItemType Directory -Force"

## VALORES PARA COPIAR
- Todo lo que haya que escribir o llenar (comandos, URLs, valores, textos) va en bloques con opción a copiar
- Cada valor en su propio bloque; nunca mezclar varios valores en un bloque
- Las URLs siempre en bloque propio, nunca dentro del texto
- Al subir archivos con el explorador, dar la ruta de la CARPETA, no la del archivo

## FORMATO DE ENTREGA DE ARCHIVOS
Siempre en bloques separados con opción a copiar:
1. Nombre del archivo
2. Ruta completa
3. Contenido (o los cambios)

- Archivo nuevo: primero el comando para crearlo; en el siguiente mensaje el contenido completo
  (excepción: el script de cambios puede crear archivos nuevos)
- Un archivo por mensaje (salvo cuando se usa el script de cambios)

## MÉTODO DE CAMBIOS EN ARCHIVOS EXISTENTES
### Por defecto: script de cambios
Cuando hay muchos cambios o más de 3 archivos:
1. Entregar un script de Python (ej. `aplicar_X_Y_Z.py`) como archivo descargable
2. El usuario lo guarda en Descargas (en Edge: "Conservar" si lo bloquea)
3. Lo ejecuta desde la carpeta del repositorio:
   `py "%USERPROFILE%\Downloads\aplicar_X_Y_Z.py"`
4. Verifica con `py -m py_compile` los archivos tocados
5. Revisa los cambios en GitHub Desktop antes del commit

El script debe:
- Aplicar Buscar → Reemplazar exactos (cada texto buscado debe aparecer 1 sola vez)
- Respetar los saltos de línea de Windows (CRLF)
- No modificar un archivo si alguno de sus cambios no coincide
- Avisar si un cambio ya estaba aplicado (se puede ejecutar dos veces sin dañar nada)
- Probarse antes de entregarlo sobre una copia con CRLF

### Excepción: Buscar → Reemplazar manual
Solo cuando son 1 a 3 archivos con poquísimos cambios:
- Bloques "Buscar" → "Reemplazar por" con el texto exacto
- Nunca instrucciones tipo "selecciona desde la línea X hasta antes de Y"

## CREAR ARCHIVOS — REGLAS GENERALES
Antes de crear cualquier archivo:
1. Revisar la estructura del repositorio
2. Identificar el patrón de nombrado existente
3. Ser 100% simétrico con ese patrón, sin inventar variantes
4. Nunca mezclar idiomas: si el proyecto usa español en variables/métodos, todo lo nuevo también

## CREAR ARCHIVOS — POR TECNOLOGÍA
- Python: crear manualmente (comando New-Item) o con el script de cambios
- Spring Boot: crear archivos .java manualmente; respetar controller / service / repository / entity / dto
- Angular u otros con generador (ng, nx): SIEMPRE usar el comando del framework primero
- React u otros sin generador: crear manualmente

## ARCHIVOS DE TESTING
- Si el equivalente existente tiene test → crear también el test del nuevo
- Si no tiene → NO crear test
- Nunca tests para modelos, entidades, DTOs, constantes
- Respetar exactamente el patrón de los tests existentes

## CONVENCIONES DE CÓDIGO
- Respetar el idioma del proyecto (español en las apps de escritorio)
- Comentarios solo si aportan contexto real
- Simetría total entre MensajesBiblicos, PublicadorRedes, LocalizadorAmigos y Marketplace: mismos nombres,
  estructura de módulos, colores ANSI (V, R, A, C, N, X), selectores Selenium en cascada y patrones de GUI
- Nombres o patrones nuevos solo si la funcionalidad es realmente distinta

## PATRONES OBLIGATORIOS (apps de escritorio)
- Toda ventana se centra antes de mostrarse:
  withdraw() → update_idletasks() → calcular x,y → geometry(f'{w}x{h}+{x}+{y}') → deiconify()
- Subventanas abiertas desde el Panel: grab_set() en la hija y grab_release() al cerrar
- Las actualizaciones nunca deben dañar lo que el cliente ya hizo (mensajes creados, editados o borrados,
  tareas, configuración, destinatarios)
- Datos personales nunca van a GitHub ni dentro de los .exe: config_global.txt es personal (gitignore);
  el instalador usa config_global.ejemplo.txt
- Contraseñas en el Administrador de credenciales de Windows, nunca en archivos de texto
- Cada acción nueva debe documentarse en el Centro de Ayuda del Panel

## VERIFICACIONES AL FINAL DE CADA FASE
- Prints de consola relevantes
- Contenido de configuraciones con `type archivo.txt`
- Screenshots si hay GUI
- Queries SQL si hay base de datos
- Antes de instalar: cerrar Panel, Configurador y Gestor de Tareas; después comparar con `dir` las horas
  del .exe instalado y el de dist\

## DECISIONES Y COMMITS
- Decisiones de arquitectura: opciones explicadas con una recomendación clara y el porqué
- Decisiones obvias se infieren de los proyectos existentes sin preguntar
- Commits por hitos lógicos, desde GitHub Desktop: dar título y descripción
- Indicar siempre en qué repositorio va el commit y recordar revisar "Current repository" en GitHub Desktop

## PUBLICAR UNA VERSIÓN (MensajesBiblicos)
1. Subir versión en version.txt e instalador_mensajes.iss
2. compilar.bat → ISCC instalador_mensajes.iss → instalar y probar
3. Commit + push en GitHub Desktop (repo automatizacion-mensajes-biblicos)
4. GitHub Release en wellinmart32/automapro-releases con tag vX.Y.Z y el instalador
5. Supabase: UPDATE aplicaciones SET version, ruta_archivo
6. Verificar el endpoint público de versión

## CAMBIO DE MODELO
Usar el modelo adecuado según el tipo de trabajo:
- **Opus**: diseñar (arquitectura, nuevas funcionalidades, decisiones) y depurar errores difíciles
- **Sonnet**: código mecánico (aplicar un diseño ya decidido, scripts de cambios, ajustes repetitivos)
- **Haiku**: cambios mínimos (un texto, un valor, un comando suelto)

Cuando el siguiente paso conviene hacerlo con otro modelo, avisarlo en el MISMO mensaje del paso,
al inicio y bien visible, así:

> ## 🔁 CAMBIO DE MODELO → Sonnet
> Motivo: (una línea)

No mandar el aviso en un mensaje aparte.