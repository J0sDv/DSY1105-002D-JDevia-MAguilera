# Modo Guardián · MVP móvil

**DSY1105 · Desarrollo de Aplicaciones Móviles** · **Modo Guardián** 

**Autores:** Josias Devia y Maximiliano Aguilera

---

## Usuarios de prueba

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | usuariomodoguardian+admin@gmail.com | modoguardian123 |
| Supervisor | usuariomodoguardian+sup@gmail.com | modoguardian123 |
| Operador | usuariomodoguardian+ope@gmail.com | modoguardian123 |

> Son cuentas creadas solo para esta evaluación. Los tres correos llegan a una misma bandeja (`usuariomodoguardian@gmail.com`).

### Sobre los roles

- **Todo usuario que se registra queda con el rol Operador.**
- El **Administrador** puede cambiar el rol de cualquier cuenta desde la app (Perfil → Gestionar usuarios y roles). La persona debe volver a iniciar sesión para ver el cambio. El primer Administrador se asigna directamente en la consola de Firebase.
- Las reglas de seguridad de Firestore impiden que una persona cambie su propio rol y que un rol sin permiso lea usuarios o cargue datos.

| Función | Operador | Supervisor | Administrador |
| --- | --- | --- | --- |
| Ver cámaras, eventos y alertas | Sí | Sí | Sí |
| Foto, grabación y linterna en CAM-01 | Sí | Sí | Sí |
| Reportar una incidencia | Sí | Sí | Sí |
| Atender alertas (confirmar, falsa alarma, resolver, foto, llamada) | Sí | Sí | No |
| Seguimiento con indicadores en Inicio | No | Sí | Sí |
| Simular alerta crítica | No | Sí | Sí |
| Cargar datos de prueba | No | No | Sí |
| Gestionar usuarios y roles | No | No | Sí |

---

## Cómo funciona la app

### Pantallas

Después de iniciar sesión, la app muestra cinco pestañas (barra inferior en celular y barra lateral en pantallas anchas):

1. **Inicio:** estado general (cámaras totales, en línea, en atención y capacidad operativa), cámaras asignadas, eventos en vivo, seguimiento de atención (solo Supervisor y Administrador) y el botón "Reportar incidencia".
2. **Cámaras:** grilla adaptable con filtros (todas, en línea, sin conexión) y detalle de cada cámara con fecha y hora, foto, grabación, linterna y estado del dispositivo.
3. **Eventos:** historial agrupado por hora, con contadores del día, filtros por tipo, búsqueda por texto y filtros de críticas y pendientes.
4. **Alertas:** lista de alertas con las pendientes primero. El detalle muestra la confianza de la IA, la cámara asociada, la cuenta regresiva de 2 minutos, las acciones disponibles según el rol y el historial de acciones.
5. **Perfil:** datos de la cuenta, cambio de contraseña por correo, notificaciones, modo oscuro, herramientas de Administrador y Supervisor, y cierre de sesión.

Además están las pantallas de **inicio de sesión** y **registro**, y la de **gestión de usuarios** (solo Administrador).

Todas las pantallas se adaptan al tamaño: en celular se muestran en una columna, en tablet pequeña dentro de una tarjeta centrada y en tablet grande en dos paneles.

### Cámaras

- **CAM-01 (Entrada Principal)** usa la **cámara real del teléfono** con CameraX: tomar foto, grabar video y encender la linterna funcionan de verdad. Las fotos y los videos se guardan en la Galería, dentro de la carpeta ModoGuardian.
- **Las demás cámaras son simuladas:** reproducen un video de prueba en bucle (o una señal animada si no se agregó el video) y muestran fecha y hora superpuestas.
- Si una cámara está sin conexión o en mantención, se muestra "Sin señal".
- Los botones HABLAR y FULL son simulados en este MVP.

### Eventos, alertas y trazabilidad

- Cada evento tiene tipo, prioridad (crítica, alerta o evento), estado de atención, cámara, zona, fecha y hora.
- Cada acción sobre una alerta (confirmar, falsa alarma, resolver, foto de incidencia, llamada a Central) se guarda en la colección `acciones` con el usuario, el rol, la fecha y la hora, y se muestra en el historial del detalle.
- Los **indicadores** (eventos del día, críticas, pendientes, atendidas y tiempo promedio de respuesta) se calculan en vivo desde los datos.

### Datos, conexión y modo sin conexión

- Las cámaras, eventos y acciones viven en **Cloud Firestore** y se leen en tiempo real.
- Una copia local en **Room** es la fuente de datos de las pantallas, así que sin conexión se ve lo último sincronizado.
- Si una acción se hace sin conexión, la app avisa que se enviará al reconectar y Firestore la sube sola.
- Todos los datos son **ficticios**. El Administrador puede cargarlos con un botón (Perfil → Cargar datos de prueba) y volver a cargarlos para restablecer el estado.

### Notificaciones

Las notificaciones son **locales y de prueba**: cuando llega una alerta crítica pendiente nueva, la app muestra una notificación. Se pueden apagar en Perfil, y el Administrador o Supervisor pueden generar una con "Simular alerta crítica". Android 13 o superior pide el permiso la primera vez.

### Crear una cuenta con correo

1. La persona completa el formulario. La app valida cada campo: el correo debe tener `@`, la contraseña debe tener al menos 8 caracteres con una minúscula y un número, las dos contraseñas deben coincidir y el nombre de usuario debe tener entre 3 y 20 caracteres (letras, números y guiones simples).
2. Firebase Authentication crea la cuenta. La app revisa que el nombre de usuario no esté repetido y guarda el perfil (usuario, región y rol) en Firestore.
3. Firebase envía un **correo con un enlace de verificación** y la app cierra la sesión.
4. La persona abre el enlace desde su correo (se abre una página de Firebase que confirma la verificación), vuelve a la app e inicia sesión.

### Iniciar sesión con correo

1. Firebase comprueba el correo y la contraseña.
2. Si el correo **no está verificado**, la app reenvía el enlace de verificación y no deja entrar.
3. Si está verificado, la app lee el perfil en Firestore y abre la pantalla principal con el rol correspondiente.

### Continuar con Google

La persona elige una cuenta de Google del dispositivo. Firebase valida la cuenta y, si es la primera vez, la app crea un perfil con rol Operador y un nombre de usuario generado. Google ya verifica el correo, por eso este camino no pide el enlace de verificación.

### Cerrar sesión y datos guardados

- Al cerrar sesión se detiene la sincronización y se limpian el formulario, la contraseña y el correo guardado.
- "Recordar mi correo" guarda solo el correo en el dispositivo (nunca la contraseña).
- La app **nunca almacena contraseñas**: las guarda Firebase Authentication.
- Se guardan en el dispositivo, además, el modo oscuro y el interruptor de notificaciones.

### Manejo de errores

Un error de datos no detiene ni reinicia la app. Cada operación muestra un estado de carga y traduce los errores a mensajes claros: credenciales incorrectas, correo ya registrado, nombre de usuario repetido, sin conexión, correo sin verificar, falta de permisos o demasiados intentos. Los datos dañados en la nube se ignoran sin cerrar la app.

---

## Inicio de sesión con Google y la huella SHA-1

### El problema

Para dejar entrar con Google, Firebase necesita reconocer la app. La reconoce por la **huella SHA-1** de la clave con que se firma (se empaqueta) la aplicación. Android Studio crea automáticamente una clave de depuración **distinta en cada computador**. Por lo tanto, la huella que se registró en Firebase pertenecía solo al computador de quien programó: en cualquier otro equipo, como el del docente, el botón de Google habría fallado con un error de configuración, aunque el resto de la app funcionara bien.

### La solución aplicada

1. Se creó una clave de firma propia del proyecto: `app/modoguardian-debug.keystore`, y se subió al repositorio.
2. En `app/build.gradle.kts` se configuró la firma de depuración (`signingConfigs`) para que use siempre esa clave, en lugar de la que genera cada computador.
3. Se obtuvo la huella
