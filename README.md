# Modo Guardián · MVP móvil (Login y registro)

**DSY1105 · Desarrollo de Aplicaciones Móviles** · **Modo Guardián:** **Inicio de sesión y registro de usuarios**.

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
- De momento, **el rol solo se puede cambiar desde Firebase**, no desde la app.
- Las reglas de seguridad de Firestore impiden que una persona cambie su propio rol desde la app.

---

## Cómo funciona la app

### Pantallas

1. **Inicio de sesión:** correo y contraseña, o botón "Continuar con Google". Incluye la opción "Recordar mi correo" y un enlace para crear una cuenta.
2. **Registro:** correo, contraseña, confirmar contraseña, nombre de usuario y país o región. También permite continuar con Google.
3. **Home:** muestra el nombre de usuario, el correo, el rol y las funciones disponibles para ese rol (Administrador, Supervisor u Operador).

Todas las pantallas de acceso se adaptan al tamaño de pantalla: en celular se muestran en una columna, en tablet pequeña dentro de una tarjeta centrada y en tablet grande en dos paneles.

### Crear una cuenta con correo

1. La persona completa el formulario. La app valida cada campo: el correo debe tener `@`, la contraseña debe tener al menos 8 caracteres con una minúscula y un número, las dos contraseñas deben coincidir y el nombre de usuario debe tener entre 3 y 20 caracteres (letras, números y guiones simples).
2. Firebase Authentication crea la cuenta. La app revisa que el nombre de usuario no esté repetido y guarda el perfil (usuario, región y rol) en Firestore.
3. Firebase envía un **correo con un enlace de verificación** y la app cierra la sesión.
4. La persona abre el enlace desde su correo (se abre una página de Firebase que confirma la verificación), vuelve a la app e inicia sesión.

### Iniciar sesión con correo

1. Firebase comprueba el correo y la contraseña.
2. Si el correo **no está verificado**, la app reenvía el enlace de verificación y no deja entrar al Home.
3. Si está verificado, la app lee el perfil en Firestore y abre el Home con el rol correspondiente.

### Continuar con Google

La persona elige una cuenta de Google del dispositivo. Firebase valida la cuenta y, si es la primera vez, la app crea un perfil con rol Operador y un nombre de usuario generado. Google ya verifica el correo, por eso este camino no pide el enlace de verificación.

### Cerrar sesión y datos guardados

- Al cerrar sesión se limpian el formulario, la contraseña y el correo guardado.
- "Recordar mi correo" guarda solo el correo en el dispositivo (nunca la contraseña) y solo se conserva si se cierra la app sin cerrar sesión.
- La app **nunca almacena contraseñas**: las guarda Firebase Authentication.

### Manejo de errores

Un error de datos no detiene ni reinicia la app. Cada operación muestra un estado de carga y traduce los errores a mensajes claros: credenciales incorrectas, correo ya registrado, nombre de usuario repetido, sin conexión, correo sin verificar o demasiados intentos.

---

## Inicio de sesión con Google y la huella SHA-1

### El problema

Para dejar entrar con Google, Firebase necesita reconocer la app. La reconoce por la **huella SHA-1** de la clave con que se firma (se empaqueta) la aplicación. Android Studio crea automáticamente una clave de depuración **distinta en cada computador**. Por lo tanto, la huella que se registró en Firebase pertenecía solo al computador de quien programó: en cualquier otro equipo, como el del docente, el botón de Google habría fallado con un error de configuración, aunque el resto de la app funcionara bien.

### La solución aplicada

1. Se creó una clave de firma propia del proyecto: `app/modoguardian-debug.keystore`, y se subió al repositorio.
2. En `app/build.gradle.kts` se configuró la firma de depuración (`signingConfigs`) para que use siempre esa clave, en lugar de la que genera cada computador.
3. Se obtuvo la huella SHA-1 de esa clave y se registró en la consola de Firebase.
4. Se descargó de nuevo el archivo `app/google-services.json`, que ya incluye los datos de Google para esa huella.

Así, cualquier computador que abra el proyecto firma la app con la misma clave y la misma huella, y el botón de Google funciona sin configurar nada.

### Advertencia: no es una práctica recomendada

Esta solución es **solo para que el proyecto de la evaluación funcione en cualquier computador**. En una aplicación real no se debe hacer así:

- La clave de firma y su contraseña **nunca se suben al repositorio**. Aquí la contraseña es pública (`android`) y cualquiera con acceso al repositorio puede firmar una app idéntica.
- Una app real usa una clave de publicación privada, guardada fuera del código y protegida (por ejemplo, con Play App Signing), y registra en Firebase la huella de cada entorno (desarrollo, pruebas y producción).
- Cada desarrollador usaría su propia clave de depuración y registraría su huella en Firebase, en lugar de compartir una clave.

---

## Tecnologías

- Kotlin y Jetpack Compose con Material Design 3
- Arquitectura MVVM (StateFlow)
- Navigation Compose
- Window Size Classes (diseño adaptable)
- Firebase Authentication (correo y Google) y Cloud Firestore
- Credential Manager (selector de cuentas de Google)
- SharedPreferences (persistencia local limitada)

## Estructura del proyecto

```
com.example.modoguardian
├── MainActivity.kt
├── model        → estado de las pantallas, errores, resultados, usuario y rol
├── navigation   → rutas, eventos de navegación y MainViewModel
├── repository   → conexión con Firebase y preferencias locales
├── viewmodel    → LoginViewModel y RegistroViewModel
└── ui
    ├── screens  → login, registro y home (tres diseños por tamaño)
    ├── theme    → tema de la app
    └── utils    → tamaño de pantalla y botón de Google
```

## Cómo ejecutarlo

1. Abrir la carpeta del proyecto en Android Studio y esperar a que termine la sincronización de Gradle.
2. Comprobar que existen `app/google-services.json` y `app/modoguardian-debug.keystore`.
3. Usar un emulador con internet. Para probar "Continuar con Google", el emulador debe tener **Google Play** y una cuenta de Google agregada.
4. Presionar **Run** e ingresar con alguno de los usuarios de prueba o crear una cuenta nueva.

## Alcance de esta entrega

- El ingreso con Apple no está incluido, porque requiere una cuenta de pago de Apple Developer.
- La verificación del correo se hace con el enlace que envía Firebase, no con un código de 6 dígitos.
- El rol de una cuenta solo se cambia desde la consola de Firebase.
