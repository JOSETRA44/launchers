import React, { createContext, useContext, useState } from 'react';

type Language = 'en' | 'es';
interface LanguageContextType { lang: Language; setLang: (l: Language) => void; t: (k: string) => string; }

const translations = {
  en: {
    // ── Hero ────────────────────────────────────────
    hero_tag:   'CLI Home-Screen Shell',
    hero_title: 'Control Android Through Tensor',
    hero_desc:  'Tensor is a terminal-driven home screen engineered for developers and power users. Built from the ground up using Jetpack Compose, Material3, and strict DDD Clean Architecture.',
    btn_terminal: 'DOWNLOAD APK',
    btn_github:   'GITHUB',
    // ── Navigation ──────────────────────────────────
    nav_home:      'Home',
    nav_features:  'Features',
    nav_reviews:   'Reviews',
    nav_changelog: 'Changelog',
    // ── Stats bar ───────────────────────────────────
    stat_downloads: 'Downloads',
    stat_version:   'Latest',
    stat_themes:    'Themes',
    stat_arsenal:   'Arsenal Modules',
    // ── Architecture section ─────────────────────────
    engineered:      'Engineered For Power Users',
    engineered_desc: 'Deep system integrations, efficient fuzzy matching engines, and defensive tools to audit your Android environment directly.',
    // ── Feature cards (8) ───────────────────────────
    feat_cli_title:     'CLI ENGINE_',
    feat_cli_desc:      'Fuzzy-matching architecture executing system directives, launching deep-linked apps, and manipulating state instantly. Zero-lag parsing via coroutines.',
    feat_sec_title:     'SECURITY.SYS_',
    feat_sec_desc:      'Passive environment auditing. Detects active ADB debug states, root binaries, and evaluates device lock integrity. Built-in cryptographic token generator.',
    feat_cache_title:   'SMART CACHE_',
    feat_cache_desc:    'A localized decay algorithm ranks your apps. Apps you ignore drop in priority over time. Frequency × time-decay scores stored in DataStore.',
    feat_matrix_title:  'MATRIX RAIN_',
    feat_matrix_desc:   'Real-time digital rain rendered on Canvas. Three synchronized color palettes: green, cyan, matrix. Auto-pauses when backgrounded to save battery.',
    feat_themes_title:  'MULTI THEME_',
    feat_themes_desc:   'Hacker Dark, Hacker Cyan, and Matrix Green palettes. All colors are semantic CSS tokens. Switch themes live from the terminal using /theme.',
    feat_oss_title:     'OPEN SOURCE_',
    feat_oss_desc:      'MIT licensed. Full DDD Clean Architecture — domain, data, and presentation layers. Zero hardcoded colors. 600-line limit per file enforced.',
    feat_arsenal_title: 'ARSENAL SEC_',
    feat_arsenal_desc:  '9 passive security modules covering device integrity, network intelligence, port scanning, SSL inspection, and MITM detection. No root required.',
    feat_kb_title:      'FAST LAUNCH_',
    feat_kb_desc:       'Type any app name and fuzzy-find instantly launches it. Smart ranking surfaces your most-used apps. Full keyboard-driven workflow from the home screen.',
    // ── Arsenal section ─────────────────────────────
    arsenal_section_title: 'Arsenal Security Toolkit',
    arsenal_section_desc:  'Nine passive modules that audit your Android environment without root access or privileged APIs. All checks run on-device — nothing is transmitted externally.',
    // ── Install steps ───────────────────────────────
    install_title:      'Install in 3 Steps',
    install_step1:      'Download the APK',
    install_step1_desc: 'Get the latest release from GitHub. No Play Store required.',
    install_step2:      'Allow installation',
    install_step2_desc: 'Enable "Install from unknown sources" for your file manager.',
    install_step3:      'Set as launcher',
    install_step3_desc: 'Select Tensor as your default home app in system settings.',
    // ── FAQ (6) ─────────────────────────────────────
    faq_title: 'Frequently Asked Questions',
    faq_1_q: 'Does Tensor drain battery?',
    faq_1_a: 'No. Tensor uses pure Compose and does not poll the system. The Matrix rain auto-pauses when the screen is off or the app goes to the background.',
    faq_2_q: 'Can I install standard widgets?',
    faq_2_a: 'Tensor is purely CLI-based by design. Traditional widgets are not supported to maintain the defensive, distraction-free environment.',
    faq_3_q: 'What Android version is required?',
    faq_3_a: 'Tensor requires Android 7.0 (API 24) or higher. It is compiled against API 36 with Material3 adaptive components and Jetpack Compose runtime.',
    faq_4_q: 'Is root access needed?',
    faq_4_a: 'No root required. The /sec command and Arsenal modules perform passive auditing using standard Android APIs — no privileged shell commands are executed.',
    faq_5_q: 'How does smart app ranking work?',
    faq_5_a: 'Tensor uses a time-decay frequency score. Each launch adds weight; weight decays exponentially over time. Apps you stop using gradually sink below fresh ones.',
    faq_6_q: 'Can Arsenal detect if my device is compromised?',
    faq_6_a: 'Arsenal audits environment signals (ADB state, root binaries, user CAs, open ports) and reports what it finds. It flags anomalies but cannot guarantee detection of sophisticated rootkits.',
    // ── Features page ────────────────────────────────
    feat_page_tag:   'App Deep Dive',
    feat_page_title: 'Everything Tensor Can Do',
    feat_page_desc:  'A full reference of CLI commands, architecture layers, security modules, and technical specifications.',
    feat_cmd_title:  'CLI Command Reference',
    feat_cmd_desc:   'All directives available in the Tensor terminal. Commands can be typed directly or launched via quick-access chips.',
    feat_arch_title: 'Clean Architecture (DDD)',
    feat_arch_desc:  'Three strict layers with a one-way dependency rule. The domain layer has zero Android imports — it is pure Kotlin business logic that can be tested without a device.',
    feat_rank_title: 'Smart Ranking Algorithm',
    feat_rank_desc:  'The ranking engine scores each app using frequency and a time-decay factor. The longer an app goes unused, the more its score decreases — surfacing apps that match your current habits.',
    feat_specs_title: 'Technical Specifications',
  },
  es: {
    // ── Hero ────────────────────────────────────────
    hero_tag:   'Shell CLI para Pantalla de Inicio',
    hero_title: 'Controla Android a través de Tensor',
    hero_desc:  'Tensor es un launcher basado en terminal diseñado para desarrolladores y usuarios avanzados. Construido desde cero usando Jetpack Compose, Material3 y estricta Arquitectura Limpia DDD.',
    btn_terminal: 'DESCARGAR APK',
    btn_github:   'VER CÓDIGO',
    // ── Navigation ──────────────────────────────────
    nav_home:      'Inicio',
    nav_features:  'Funciones',
    nav_reviews:   'Reseñas',
    nav_changelog: 'Versiones',
    // ── Stats bar ───────────────────────────────────
    stat_downloads: 'Descargas',
    stat_version:   'Versión',
    stat_themes:    'Temas',
    stat_arsenal:   'Módulos Arsenal',
    // ── Architecture section ─────────────────────────
    engineered:      'Diseñado Para Usuarios Avanzados',
    engineered_desc: 'Con integraciones profundas en el sistema, motores de coincidencia difusa y herramientas defensivas para auditar tu entorno Android al instante.',
    // ── Feature cards (8) ───────────────────────────
    feat_cli_title:     'MOTOR CLI_',
    feat_cli_desc:      'Arquitectura con búsqueda difusa que ejecuta directivas del sistema, lanza apps con deep links y manipula el estado al instante. Sin latencia gracias a las coroutines.',
    feat_sec_title:     'SEGURIDAD.SYS_',
    feat_sec_desc:      'Auditoría pasiva del entorno. Detecta estados ADB activos, binarios root e integridad del bloqueo de pantalla. Incluye generador criptográfico de tokens.',
    feat_cache_title:   'CACHÉ INTELIGENTE_',
    feat_cache_desc:    'Un algoritmo de decaimiento temporal ordena tus apps. Las que no usas bajan de prioridad con el tiempo. Las puntuaciones se guardan en DataStore.',
    feat_matrix_title:  'LLUVIA MATRIX_',
    feat_matrix_desc:   'Lluvia digital Matrix en tiempo real en Canvas. Tres paletas de color sincronizadas. Se pausa automáticamente en segundo plano para ahorrar batería.',
    feat_themes_title:  'MULTI TEMA_',
    feat_themes_desc:   'Paletas Hacker Dark, Hacker Cyan y Matrix Green. Todos los colores son tokens CSS semánticos. Cambia el tema en vivo desde la terminal con /theme.',
    feat_oss_title:     'CÓDIGO ABIERTO_',
    feat_oss_desc:      'Licencia MIT. Arquitectura DDD completa — capas dominio, datos y presentación. Sin colores hardcodeados. Límite de 600 líneas por archivo.',
    feat_arsenal_title: 'ARSENAL SEC_',
    feat_arsenal_desc:  '9 módulos de seguridad pasivos: integridad del dispositivo, inteligencia de red, escaneo de puertos, inspección SSL y detección de MITM. Sin root.',
    feat_kb_title:      'LANZAMIENTO RÁPIDO_',
    feat_kb_desc:       'Escribe el nombre de cualquier app y el motor difuso la lanza al instante. El ranking inteligente muestra tus apps más usadas primero. Todo desde el teclado.',
    // ── Arsenal section ─────────────────────────────
    arsenal_section_title: 'Arsenal de Seguridad',
    arsenal_section_desc:  'Nueve módulos pasivos que auditan tu entorno Android sin acceso root ni APIs privilegiadas. Todos los checks corren en el dispositivo — nada se transmite externamente.',
    // ── Install steps ───────────────────────────────
    install_title:      'Instala en 3 Pasos',
    install_step1:      'Descarga el APK',
    install_step1_desc: 'Obtén la última versión desde GitHub. No requiere Play Store.',
    install_step2:      'Permite la instalación',
    install_step2_desc: 'Activa "Instalar fuentes desconocidas" en tu gestor de archivos.',
    install_step3:      'Establece como launcher',
    install_step3_desc: 'Selecciona Tensor como app de inicio predeterminada en ajustes del sistema.',
    // ── FAQ (6) ─────────────────────────────────────
    faq_title: 'Preguntas Frecuentes (FAQ)',
    faq_1_q: '¿Consume mucha batería?',
    faq_1_a: 'No. Tensor no hace polling al sistema. La lluvia Matrix se pausa cuando la pantalla se apaga o la app va a segundo plano.',
    faq_2_q: '¿Puedo instalar widgets normales?',
    faq_2_a: 'Por diseño, Tensor es puramente CLI. Los widgets tradicionales no están soportados para mantener un entorno libre de distracciones y enfocado en la velocidad.',
    faq_3_q: '¿Qué versión de Android necesito?',
    faq_3_a: 'Tensor requiere Android 7.0 (API 24) o superior. Compilado contra API 36 con componentes adaptativos de Material3 y Jetpack Compose runtime.',
    faq_4_q: '¿Se necesita acceso root?',
    faq_4_a: 'No se necesita root. El comando /sec y los módulos Arsenal realizan auditoría pasiva usando APIs estándar de Android — sin ejecutar comandos de shell privilegiados.',
    faq_5_q: '¿Cómo funciona el ranking inteligente de apps?',
    faq_5_a: 'Tensor usa una puntuación de frecuencia con decaimiento temporal. Cada lanzamiento añade peso; el peso decae exponencialmente. Las apps sin usar bajan gradualmente.',
    faq_6_q: '¿Puede Arsenal detectar si mi dispositivo está comprometido?',
    faq_6_a: 'Arsenal audita señales del entorno (estado ADB, binarios root, CAs de usuario, puertos abiertos) y reporta lo que encuentra. Marca anomalías pero no garantiza detectar rootkits sofisticados.',
    // ── Features page ────────────────────────────────
    feat_page_tag:   'Exploración Detallada',
    feat_page_title: 'Todo lo que Tensor Puede Hacer',
    feat_page_desc:  'Referencia completa de comandos CLI, capas de arquitectura, módulos de seguridad y especificaciones técnicas.',
    feat_cmd_title:  'Referencia de Comandos CLI',
    feat_cmd_desc:   'Todos los comandos disponibles en el terminal Tensor. Pueden escribirse directamente o ejecutarse desde los chips de acceso rápido.',
    feat_arch_title: 'Arquitectura Limpia (DDD)',
    feat_arch_desc:  'Tres capas estrictas con regla de dependencia unidireccional. La capa de dominio tiene cero imports de Android — es Kotlin puro testeable sin dispositivo.',
    feat_rank_title: 'Algoritmo de Ranking Inteligente',
    feat_rank_desc:  'El motor de ranking puntúa cada app usando frecuencia y un factor de decaimiento temporal. Cuanto más tiempo pasa sin usar una app, más baja su puntuación.',
    feat_specs_title: 'Especificaciones Técnicas',
  },
} as const;

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider = ({ children }: { children: React.ReactNode }) => {
  const [lang, setLang] = useState<Language>(() => (localStorage.getItem('tensor-lang') as Language) || 'es');

  const changeLang = (l: Language) => { setLang(l); localStorage.setItem('tensor-lang', l); };
  const t = (key: string) => (translations[lang] as Record<string, string>)[key] || key;

  return (
    <LanguageContext.Provider value={{ lang, setLang: changeLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const ctx = useContext(LanguageContext);
  if (!ctx) throw new Error('useLanguage must be used within LanguageProvider');
  return ctx;
};
