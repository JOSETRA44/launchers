import React, { createContext, useContext, useState } from 'react';

type Language = 'en' | 'es';

interface LanguageContextType {
  lang: Language;
  setLang: (lang: Language) => void;
  t: (key: string) => string;
}

const translations = {
  en: {
    hero_tag: 'CLI Home-Screen Shell',
    hero_title: 'Control Android Through Tensor',
    hero_desc: 'Tensor is a terminal-driven home screen engineered for developers and power users. Built from the ground up using Jetpack Compose, Material3, and strict DDD Clean Architecture.',
    btn_terminal: 'OPEN TERMINAL',
    btn_github: 'GITHUB',
    engineered: 'Engineered For Power Users',
    engineered_desc: 'Designed with deep system integrations, efficient fuzzy matching engines, and defensive tools to audit your Android environment directly.',
    nav_home: 'Home',
    nav_reviews: 'Reviews',
    nav_changelog: 'Changelog',
    faq_title: 'Frequently Asked Questions',
    faq_1_q: 'Does it drain battery?',
    faq_1_a: 'No. Tensor uses pure Compose and does not poll the system. The Matrix rain only renders when the terminal is open, automatically pausing when the app goes into the background.',
    faq_2_q: 'Can I install standard widgets?',
    faq_2_a: 'Tensor is purely CLI-based by design. Traditional widgets are not supported to maintain the defensive, distraction-free environment.',
  },
  es: {
    hero_tag: 'Shell CLI para Pantalla de Inicio',
    hero_title: 'Controla Android a través de Tensor',
    hero_desc: 'Tensor es un launcher basado en terminal diseñado para desarrolladores y usuarios avanzados. Construido desde cero usando Jetpack Compose, Material3 y estricta Arquitectura Limpia DDD.',
    btn_terminal: 'ABRIR TERMINAL',
    btn_github: 'VER CÓDIGO',
    engineered: 'Diseñado Para Usuarios Avanzados',
    engineered_desc: 'Con integraciones profundas en el sistema, motores de coincidencia difusa y herramientas defensivas para auditar tu entorno Android al instante.',
    nav_home: 'Inicio',
    nav_reviews: 'Reseñas',
    nav_changelog: 'Versiones',
    faq_title: 'Preguntas Frecuentes (FAQ)',
    faq_1_q: '¿Consume mucha batería?',
    faq_1_a: 'No. Tensor no hace "polling" al sistema. Las animaciones pesadas como la lluvia digital Matrix se pausan completamente cuando la app está en segundo plano.',
    faq_2_q: '¿Puedo instalar widgets normales?',
    faq_2_a: 'Por diseño, Tensor es puramente CLI. Los widgets tradicionales no están soportados para mantener un entorno libre de distracciones y enfocado en la velocidad.',
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
    return (translations[lang] as any)[key] || key;
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
