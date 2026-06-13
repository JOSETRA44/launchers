import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Terminal, Star, Clock, X, Menu, Globe, ChevronRight, Download } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useLanguage } from '../LanguageContext';

const Navbar = () => {
  const location = useLocation();
  const { t, lang, setLang } = useLanguage();
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => { setMenuOpen(false); }, [location.pathname]);

  useEffect(() => {
    document.body.style.overflow = menuOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [menuOpen]);

  const navItems = [
    { name: t('nav_home'),      path: '/',        icon: <Terminal size={15} />, isExternal: false },
    { name: t('nav_reviews'),   path: '/resenas', icon: <Star size={15} />,    isExternal: false },
    { name: t('nav_changelog'), path: 'https://github.com/JOSETRA44/launchers/releases/', icon: <Clock size={15} />, isExternal: true },
  ];

  const isActive = (path: string, isExternal: boolean) =>
    !isExternal && location.pathname === path;

  return (
    <nav className="sticky top-0 z-50 bg-background/90 backdrop-blur-md border-b border-border" role="navigation" aria-label="Main navigation">
      <div className="container mx-auto px-4 max-w-7xl flex items-center justify-between h-14">

        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 text-primary font-bold font-mono text-lg" aria-label="Tensor — Home">
          <div className="w-7 h-7 bg-primary-dim flex items-center justify-center rounded border border-primary/30 shrink-0" aria-hidden="true">
            <span className="leading-none text-xs text-primary">&gt;_</span>
          </div>
          <span className="text-glow">TENSOR</span>
          <span className="hidden sm:inline text-[10px] text-on-surface font-mono font-normal border border-border px-1.5 py-0.5 rounded">
            v1.0.4
          </span>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-5 font-mono text-sm">
          {navItems.map((item) => {
            const active = isActive(item.path, item.isExternal);
            const cls = `flex items-center gap-2 transition-all duration-200 min-h-[44px] px-1 ${
              active ? 'text-primary text-glow' : 'text-on-background hover:text-primary'
            }`;
            if (item.isExternal) {
              return (
                <a key={item.path} href={item.path} target="_blank" rel="noopener noreferrer" className={cls}>
                  {item.icon} {item.name}
                </a>
              );
            }
            return (
              <Link key={item.path} to={item.path} className={cls} aria-current={active ? 'page' : undefined}>
                {item.icon} {item.name}
                {active && <span className="w-1 h-1 rounded-full bg-primary animate-pulse" aria-hidden="true" />}
              </Link>
            );
          })}

          <div className="h-4 w-px bg-border" aria-hidden="true" />

          <button
            onClick={() => setLang(lang === 'en' ? 'es' : 'en')}
            className="flex items-center gap-1.5 text-on-surface hover:text-primary transition-colors border border-border hover:border-primary/40 px-2.5 py-1.5 rounded text-xs min-h-[44px]"
            aria-label={`Switch language — currently ${lang.toUpperCase()}`}
          >
            <Globe size={12} aria-hidden="true" /> {lang.toUpperCase()}
          </button>

          <a
            href="https://github.com/JOSETRA44/launchers/releases/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 bg-primary text-black font-bold text-xs px-3 py-2 rounded hover:bg-cursor transition-colors min-h-[44px]"
          >
            <Download size={12} aria-hidden="true" /> APK
          </a>
        </div>

        {/* Mobile Controls */}
        <div className="md:hidden flex items-center gap-2">
          <button
            onClick={() => setLang(lang === 'en' ? 'es' : 'en')}
            className="text-on-surface border border-border px-2 py-1.5 rounded text-xs font-mono min-h-[44px] min-w-[44px]"
            aria-label={`Toggle language — ${lang.toUpperCase()}`}
          >
            {lang.toUpperCase()}
          </button>
          <button
            onClick={() => setMenuOpen(v => !v)}
            className="text-primary p-2 rounded border border-border/50 hover:border-primary/50 transition-colors min-h-[44px] min-w-[44px] flex items-center justify-center"
            aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            aria-expanded={menuOpen}
            aria-controls="mobile-drawer"
          >
            {menuOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </div>

      {/* ── Mobile Drawer ────────────────────────────────────── */}
      <AnimatePresence>
        {menuOpen && (
          <>
            {/* Backdrop */}
            <motion.div
              key="backdrop"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2 }}
              className="fixed inset-0 top-14 bg-black/65 backdrop-blur-sm z-40 md:hidden"
              onClick={() => setMenuOpen(false)}
              aria-hidden="true"
            />

            {/* Panel */}
            <motion.div
              key="panel"
              id="mobile-drawer"
              role="dialog"
              aria-modal="true"
              aria-label="Navigation menu"
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              transition={{ type: 'spring', stiffness: 320, damping: 32 }}
              className="fixed top-14 right-0 bottom-0 w-72 bg-surface border-l border-border z-50 md:hidden flex flex-col"
            >
              <div className="p-6 flex flex-col gap-2 flex-grow overflow-y-auto">
                <div className="text-on-surface text-[10px] font-mono mb-4 flex items-center gap-2 tracking-widest uppercase">
                  <span className="text-primary animate-pulse" aria-hidden="true">●</span> Navigation
                </div>

                {navItems.map((item) => {
                  const active = isActive(item.path, item.isExternal);
                  const inner = (
                    <div className={`flex items-center justify-between p-3.5 rounded border transition-all duration-200 font-mono text-sm min-h-[52px] ${
                      active
                        ? 'border-primary/50 bg-primary/10 text-primary'
                        : 'border-border text-on-background hover:border-primary/30 hover:text-primary hover:bg-primary/5'
                    }`}>
                      <span className="flex items-center gap-3">{item.icon} {item.name}</span>
                      <ChevronRight size={14} className="opacity-40" aria-hidden="true" />
                    </div>
                  );

                  if (item.isExternal) {
                    return <a key={item.path} href={item.path} target="_blank" rel="noopener noreferrer">{inner}</a>;
                  }
                  return (
                    <Link key={item.path} to={item.path} aria-current={active ? 'page' : undefined}>
                      {inner}
                    </Link>
                  );
                })}
              </div>

              <div className="p-6 border-t border-border shrink-0">
                <a
                  href="https://github.com/JOSETRA44/launchers/releases/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-center gap-2 w-full bg-primary text-black font-mono font-bold text-sm py-3.5 rounded hover:bg-cursor transition-colors"
                >
                  <Download size={16} aria-hidden="true" /> Download APK
                </a>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </nav>
  );
};

export default Navbar;
