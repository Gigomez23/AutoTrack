# AutoTrak - Gestión Inteligente de Vehículos 🚗💨

AutoTrak es una solución móvil integral diseñada para propietarios de vehículos y administradores de flotas que buscan optimizar el control, mantenimiento y rendimiento de sus unidades. La aplicación combina una interfaz moderna y profesional con capacidades robustas de sincronización offline para garantizar que los datos estén siempre accesibles.

## 🚀 Características Principales

- **Dashboard Inteligente**: Vista general del estado de la flota, alertas de mantenimiento, multas pendientes y rendimiento de combustible.
* **Control de Combustible**: Registro detallado de cargas, cálculo automático de eficiencia (km/L) y seguimiento de costos.
* **Gestión de Mantenimiento**: Historial de servicios preventivos y correctivos, con alertas de próximos mantenimientos.
* **Seguimiento de Problemas**: Registro de fallas mecánicas con niveles de severidad para una atención oportuna.
* **Bóveda de Documentos**: Digitalización de licencias, circulaciones y seguros con recordatorios de vencimiento.
* **Gestión de Multas**: Control centralizado de infracciones de tránsito y estados de pago.
* **Modo Offline**: Capacidad de trabajar sin conexión a internet; los datos se sincronizan automáticamente al recuperar la señal.
* **Interfaz Adaptativa**: Soporte completo para Modo Claro y Modo Oscuro con una paleta de colores profesional (Navy Blue).

## 🛠️ Stack Tecnológico

*   **Lenguaje**: Kotlin
*   **Interfaz de Usuario**: Jetpack Compose (Modern Declarative UI)
*   **Arquitectura**: MVVM (Model-View-ViewModel) con Repository Pattern.
*   **Base de Datos Local**: Room Database para persistencia offline.
*   **Red**: Retrofit 2 & GSON para comunicación con el API REST.
*   **Navegación**: Jetpack Compose Navigation.
*   **Gestión de Estado**: Kotlin Coroutines & Flow.
*   **Inyección de Dependencias**: Implementación manual optimizada para claridad y rendimiento.

## 🏗️ Estructura del Proyecto

```text
ni.edu.uam.autotrak
├── data
│   ├── local      # Entidades de Room, DAOs y Base de Datos.
│   ├── mapper     # Convertidores entre DTOs, Entidades y Modelos de Dominio.
│   ├── remote     # Clientes API, Retrofit y modelos de red.
│   ├── repository # Implementaciones de lógica de datos.
│   └── sync       # SyncManager para sincronización bidireccional.
├── ui
│   ├── components # Componentes visuales reutilizables.
│   ├── navigation # Configuración de rutas y grafos de navegación.
│   ├── screens    # Pantallas principales de la aplicación.
│   └── theme      # Definiciones de Color (NavyBlue), Tipografía y MaterialTheme.
└── viewmodel      # Lógica de presentación y gestión de estado de UI.
```

## ⚙️ Configuración e Instalación

1.  **Requisitos**: Android Studio Ladybug o superior, JDK 17+.
2.  **Clonación**:
    ```bash
    git clone https://github.com/tu-usuario/autotrak-android.git
    ```
3.  **Configuración del Servidor**:
    Asegúrate de configurar la URL base en `ni.edu.uam.autotrak.data.remote.RetrofitClient` para apuntar a tu instancia del backend de AutoTrak.
4.  **Ejecución**:
    Compila y ejecuta el módulo `:app` en un emulador o dispositivo físico con Android 8.0 (API 26) o superior.

## 🎨 Diseño Visual

La aplicación utiliza una identidad visual basada en el color **Navy Blue (#25254B)**, transmitiendo confianza y profesionalismo. Se han implementado esquemas de color específicos para garantizar la accesibilidad y el confort visual tanto en entornos de alta luminosidad como en modo nocturno.

---

**Desarrollado como parte del Proyecto de Programación Orientada a Objetos II - UAM.**
