# Plan de Mejora: "Mundo Mágico" - Fase 2 (Refinamiento y Dibujo)

Este plan completa la visión del "Mundo Mágico" refinando los componentes existentes, corrigiendo errores menores de la fase anterior y añadiendo la funcionalidad de "Dibujo Libre" inspirada en el diseño HTML.

## User Review Required

> [!IMPORTANT]
> - Se añadirá una nueva sección de **Dibujo Libre** para completar las opciones del menú principal.
> - Se actualizarán los nombres de las categorías para que sean más descriptivos y juguetones ("Animales Musicales", "Juegos de Formas", etc.).
> - Se integrará un botón de control de volumen global en la pantalla de inicio.

## Proposed Changes

### 🎨 Refinamiento Visual y Correcciones
Ajustaremos detalles técnicos y visuales para una mejor experiencia.

#### [MODIFY] [MainActivity.kt](file:///D:/baku/aplicaciones%20android/yuliapp/app/src/main/java/com/example/MainActivity.kt)
- **Corrección `ConfettiCelebration`**: Corregir el acceso a `maxWidth` y `maxHeight` dentro del scope de `BoxWithConstraints`.
- **Mejora en `AnimalCard` / `FamilyCard`**: Centralizar la lógica de color para evitar errores de parseo y seguir recomendaciones de lint.
- **Botón de Volumen**: Añadir un botón flotante en la esquina superior derecha de la `HomeScreen` (estilo Speaker del HTML).

### 🖍️ Nueva Pantalla: Dibujo Libre
Un espacio sensorial donde el bebé puede pintar con los dedos.

#### [MODIFY] [MainActivity.kt](file:///D:/baku/aplicaciones%20android/yuliapp/app/src/main/java/com/example/MainActivity.kt)
- **`DrawingScreen`**: Implementar un lienzo (`Canvas`) que detecte gestos táctiles y dibuje líneas de colores vibrantes que desaparecen lentamente o se pueden borrar (efecto mágico).
- **Actualizar `HomeScreen`**: Añadir el botón "Dibujo Libre" con un icono de crayón o pincel.

### 🎹 Piano Mágico
- **Mejora Visual**: Asegurar que las "estrellas" y guías de canciones resalten aún más sobre el fondo de "Mundo Mágico".

## Verification Plan

### Automated Tests
- `gradlew :app:compileDebugKotlin` para verificar la integridad del código.

### Manual Verification
1.  **Dibujo**: Probar que se puede pintar con varios dedos y que los colores son llamativos.
2.  **Navegación**: Verificar que el botón de "Dibujo" en la pantalla de inicio funciona correctamente.
3.  **Visualización**: Confirmar que los nuevos títulos ("Animales Musicales", etc.) aparecen correctamente.
