import React, { createContext, useContext, useState } from 'react';

type Language = 'en' | 'es';

interface LanguageContextType {
  lang: Language;
  setLang: (lang: Language) => void;
  t: (key: string) => string;
}

const translations = {
  en: {
    // Hero
    hero_tag: 'CLI Home-Screen Shell',
    hero_title: 'Control Android Through Tensor',
    hero_desc: 'Tensor is a terminal-driven home screen engineered for developers and power users. Built from the ground up using Jetpack Compose, Material3, and strict DDD Clean Architecture.',
    btn_terminal: 'DOWNLOAD APK',
    btn_github: 'GITHUB',
    // Nav
    nav_home: 'Home',
    nav_reviews: 'Reviews',
    nav_changelog: 'Changelog',
    // Stats
    stat_downloads: 'Downloads',
    stat_version: 'Latest',
    stat_themes: 'Themes',
    stat_perms: 'Extra Perms',
    // Architecture section
    engineered: 'Engineered For Power Users',
    engineered_desc: 'Deep system integrations, efficient fuzzy matching engines, and defensive tools to audit your Android environment directly.',
    // Feature cards
    feat_cli_title: 'CLI ENGINE_',
    feat_cli_desc: 'Fuzzy-matching architecture executing system directives, launching deep-linked apps, and manipulating state instantly. Zero-lag parsing via coroutines.',
    feat_sec_title: 'SECURITY.SYS_',
    feat_sec_desc: 'Passive environment auditing. Detects active ADB debug states, root binaries, and evaluates device lock integrity. Built-in cryptographic token generator.',
    feat_cache_title: 'SMART CACHE_',
    feat_cache_desc: 'A localized neural-like decay algorithm ranks your apps. Apps you ignore drop in priority over time. Stored efficiently in DataStore.',
    feat_matrix_title: 'MATRIX RAIN_',
    feat_matrix_desc: 'Real-time Matrix digital rain rendered on Canvas. Three synchronized color palettes: green, cyan, and matrix. Auto-pauses in background to save battery.',
    feat_themes_title: 'MULTI THEME_',
    feat_themes_desc: 'Hacker Dark, Hacker Cyan, and Matrix Green palettes. All colors are semantic CSS tokens. Switch themes live from the terminal using /theme.',
    feat_oss_title: 'OPEN SOURCE_',
    feat_oss_desc: 'MIT licensed. Full DDD Clean Architecture — domain, data, and presentation layers. Zero hardcoded colors. 600-line limit per file enforced.',
    // Install
    install_title: 'Install in 3 Steps',
    install_step1: 'Download the APK',
    install_step1_desc: 'Get the latest release from GitHub. No Play Store required.',
    install_step2: 'Allow installation',
    install_step2_desc: 'Enable "Install from unknown sources" for your file manager.',
    install_step3: 'Set as launcher',
    install_step3_desc: 'Select Tensor as your default home app in system settings.',
    // FAQ
    faq_title: 'Frequently Asked Questions',
    faq_1_q: 'Does Tensor drain battery?',
    faq_1_a: 'No. Tensor uses pure Compose and does not poll the system. The Matrix rain only renders when the screen is on, automatically pausing in the background.',
    faq_2_q: 'Can I install standard widgets?',
    faq_2_a: 'Tensor is purely CLI-based by design. Traditional widgets are not supported to maintain the defensive, distraction-free environment.',
    faq_3_q: 'What Android version is required?',
    faq_3_a: 'Tensor requires Android 7.0 (API 24) or higher. It is compiled against API 36 with Material3 adaptive components and Jetpack Compose runtime.',
    faq_4_q: 'Is root access needed?',
    faq_4_a: 'No root required. The /sec command performs passive auditing using standard Android APIs — it reads environment flags without executing privileged shell commands.',
    faq_5_q: 'How does smart app ranking work?',
    faq_5_a: 'Tensor uses a time-decay frequency score. Each launch adds weight; weight decays exponentially over time. Apps you stop using gradually sink below fresh ones.',
  },
  es: {
    // Hero
    hero_tag: 'Shell CLI para Pantalla de Inicio',
    hero_title: 'Controla Android a través de Tensor',
    hero_desc: 'Tensor es un launcher basado en terminal diseñado para desarrolladores y usuarios avanzados. Construido desde cero usando Jetpack Compose, Material3 y estricta Arquitectura Limpia DDD.',
    btn_terminal: 'DESCARGAR APK',
    btn_github: 'VER CÓDIGO',
    // Nav
    nav_home: 'Inicio',
    nav_reviews: 'Reseñas',
    nav_changelog: 'Versiones',
    // Stats
    stat_downloads: 'Descargas',
    stat_version: 'Versión',
    stat_themes: 'Temas',
    stat_perms: 'Permisos Extra',
    // Architecture section
    engineered: 'Diseñado Para Usuarios Avanzados',
    engineered_desc: 'Con integraciones profundas en el sistema, motores de coincidencia difusa y herramientas defensivas para auditar tu entorno Android al instante.',
    // Feature cards
    feat_cli_title: 'MOTOR CLI_',
    feat_cli_desc: 'Arquitectura con búsqueda difusa que ejecuta directivas del sistema, lanza apps con deep links y manipula el estado al instante. Parsing sin latencia con coroutines.',
    feat_sec_title: 'SEGURIDAD.SYS_',
    feat_sec_desc: 'Auditoría pasiva del entorno. Detecta estados ADB activos, binarios root, e integridad del bloqueo de pantalla. Incluye generador criptográfico de tokens.',
    feat_cache_title: 'CACHÉ INTELIGENTE_',
    feat_cache_desc: 'Un algoritmo de decaimiento ordena tus apps. Las que no usas bajan de prioridad con el tiempo. Almacenado eficientemente en DataStore.',
    feat_matrix_title: 'LLUVIA MATRIX_',
    feat_matrix_desc: 'Lluvia digital Matrix en tiempo real renderizada en Canvas. Tres paletas sincronizadas: verde, cian y matrix. Se pausa automáticamente en segundo plano.',
    feat_themes_title: 'MULTI TEMA_',
    feat_themes_desc: 'Paletas Hacker Dark, Hacker Cyan y Matrix Green. Todos los colores son tokens CSS semánticos. Cambia el tema en vivo desde la terminal con /theme.',
    feat_oss_title: 'CÓDIGO ABIERTO_',
    feat_oss_desc: 'Licencia MIT. Arquitectura DDD completa — capas dominio, datos y presentación. Sin colores hardcodeados. Límite de 600 líneas por archivo.',
    // Install
    install_title: 'Instala en 3 Pasos',
    install_step1: 'Descarga el APK',
    install_step1_desc: 'Obtén la última versión desde GitHub. No requiere Play Store.',
    install_step2: 'Permite la instalación',
    install_step2_desc: 'Activa "Instalar fuentes desconocidas" en tu gestor de archivos.',
    install_step3: 'Establece como launcher',
    install_step3_desc: 'Selecciona Tensor como app de inicio predeterminada en ajustes del sistema.',
    // FAQ
    faq_title: 'Preguntas Frecuentes (FAQ)',
    faq_1_q: '¿Consume mucha batería?',
    faq_1_a: 'No. Tensor no hace "polling" al sistema. Las animaciones pesadas como la lluvia Matrix se pausan completamente cuando la app está en segundo plano.',
    faq_2_q: '¿Puedo instalar widgets normales?',
    faq_2_a: 'Por diseño, Tensor es puramente CLI. Los widgets tradicionales no están soportados para mantener un entorno libre de distracciones y enfocado en la velocidad.',
    faq_3_q: '¿Qué versión de Android necesito?',
    faq_3_a: 'Tensor requiere Android 7.0 (API 24) o superior. Compilado contra API 36 con componentes adaptativos de Material3 y Jetpack Compose runtime.',
    faq_4_q: '¿Se necesita acceso root?',
    faq_4_a: 'No se necesita root. El comando /sec realiza auditoría pasiva usando APIs estándar de Android — lee flags del entorno sin ejecutar comandos de shell privilegiados.',
    faq_5_q: '¿Cómo funciona el ranking inteligente de apps?',
    faq_5_a: 'Tensor usa una puntuación de frecuencia con decaimiento temporal. Cada lanzamiento añade peso; el peso decae exponencialmente. Las apps que dejas de usar bajan gradualmente.',
  }
};

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider = ({ children }: { children: React.ReactNode }) => {
  const [lang, setLang] = useState<Language>(() => {
    return (localStorage.getItem('tensor-lang') as Language) || 'es';
  });

  const changeLang = (l: Language) => {
    setLang(l);
    localStorage.setItem('tensor-lang', l);
  };

  const t = (key: string) => {
    return (translations[lang] as Record<string, string>)[key] || key;
  };

  return (
    <LanguageContext.Provider value={{ lang, setLang: changeLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) throw new Error('useLanguage must be used within LanguageProvider');
  return context;
};
