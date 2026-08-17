# YuliApp 🎈

App de juego sensorial para bebés de 9 a 12 meses, con colores altamente contrastados, sonidos táctiles y una interfaz pensada para que los más pequeños la usen por sí solos, mientras los padres personalizan el contenido desde un modo protegido.

## 🎮 Juegos incluidos

- **🎨 Colores** — Burbujas de colores que suenan al tocarlas, con un modo juego (quiz) de formas y colores.
- **🐮 La Granja de Sonidos** — Animales con sonidos reales (vía streaming con fallback sintetizado) y opción de grabar la voz de mamá/papá.
- **🎹 Piano** — Piano con teclas blancas y negras, multitáctil, dos instrumentos y canciones infantiles guiadas.
- **👩‍❤️‍👨 Mi Familia** — Tarjetas tipo polaroid con fotos, voz personalizada y avatares.

## 👨‍👩‍👧 Modo Padres

- Acceso manteniendo presionado el candado **2.5 segundos** en la pantalla de inicio.
- Permite grabar o subir voces, fotos, añadir/eliminar animales y familiares, y personalizar el fondo con un video en bucle.

## 🛠️ Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** para persistencia local (animales, familia)
- **AudioTrack** para síntesis de sonidos y **MediaRecorder/MediaPlayer** para grabación y reproducción
- **Coil** para imágenes
- **Robolectric + Roborazzi** para tests unitarios y screenshots
- Gradle KTS con version catalog

## ▶️ Cómo ejecutar

1. Abre el proyecto en **Android Studio**.
2. Crea un archivo `.env` en la raíz del proyecto con tu claves (ver `.env.example`).
3. Ejecuta la app en un emulador o dispositivo físico.

## 🧪 Tests

```bash
./gradlew testDebugUnitTest
```

> **Nota para Windows con usuario sin espacios:** si tu `user.home` contiene un espacio, Robolectric deja de encontrar sus librerías nativas; el `build.gradle.kts` ya lo resuelve apuntando temporalmente el `user.home` de los tests al directorio temporal del sistema.