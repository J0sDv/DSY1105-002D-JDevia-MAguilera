# Modo Guardián · MVP móvil (Login)

**DSY1105 · Desarrollo de Aplicaciones Móviles** · Duoc UC. 

Autores: [Josias Devia] y [Maximiliano Aguilera]

## Tecnologías

- Kotlin
- Jetpack Compose y Material Design 3
- Arquitectura MVVM (StateFlow)
- Navigation Compose
- Window Size Classes (diseño adaptable)
- SharedPreferences (persistencia local limitada)

## Usuarios de prueba

Todos los datos son ficticios. No se usan credenciales ni datos reales.

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@guardian.test | 123456 |
| Supervisor | supervisor@guardian.test | 123456 |
| Operador | operador@guardian.test | 123456 |

## Funcionalidades

- Inicio de sesión con validación de campos (correo con @ y contraseña de al menos 6 caracteres).
- Indicador de carga y mensaje de error ante credenciales incorrectas o falta de conexión.
- Pantalla Home distinta según el rol del usuario.
- Diseño adaptable a pantallas Compact, Medium y Expanded.
- Opción "Recordar mi correo": el correo se conserva solo si se cierra la app sin cerrar sesión. Al cerrar sesión se borra.

## Estructura del proyecto

- `model/`: estado de la pantalla, errores, usuario y rol.
- `viewmodel/`: LoginViewModel.
- `repository/`: usuarios de prueba, conectividad y preferencias.
- `navigation/`: rutas, eventos de navegación y MainViewModel.
- `ui/screens/`: pantallas de login (tres tamaños) y Home.
- `ui/utils/`: cálculo del tamaño de pantalla.

## Cómo ejecutarlo

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Seleccionar un emulador (Android API 33 o superior) y presionar Run.
4. Iniciar sesión con alguno de los usuarios de la tabla.
