import { Link, useLocation } from 'react-router-dom';
import { Terminal, Star, Clock, Menu, Globe } from 'lucide-react';
import { useLanguage } from '../LanguageContext';

const Navbar = () => {
  const location = useLocation();
  const { t, lang, setLang } = useLanguage();

  const navItems = [
    { name: t('nav_home'), path: '/', icon: <Terminal size={18} />, isExternal: false },
    { name: t('nav_reviews'), path: '/resenas', icon: <Star size={18} />, isExternal: false },
    { name: t('nav_changelog'), path: 'https://github.com/JOSETRA44/launchers/releases/', icon: <Clock size={18} />, isExternal: true },
  ];

  return (
    <nav className="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-border">
      <div className="container mx-auto px-4 max-w-7xl flex items-center justify-between h-14">
        
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 text-primary font-bold font-mono text-lg">
          <div className="w-6 h-6 bg-primary-dim flex items-center justify-center rounded text-primary">
            <span className="leading-none text-sm">&gt;_</span>
          </div>
          TENSOR
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-6 font-mono text-sm">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            const classes = `flex items-center gap-2 transition-colors ${
              isActive ? 'text-primary' : 'text-on-background hover:text-primary'
            }`;

            if (item.isExternal) {
              return (
                <a key={item.path} href={item.path} target="_blank" rel="noopener noreferrer" className={classes}>
                  {item.icon}
                  {item.name}
                </a>
              );
            }

            return (
              <Link key={item.path} to={item.path} className={classes}>
                {item.icon}
                {item.name}
              </Link>
            );
          })}

          {/* I18n Switcher */}
          <button 
            onClick={() => setLang(lang === 'en' ? 'es' : 'en')}
            className="flex items-center gap-2 text-on-surface hover:text-primary transition-colors border border-border px-3 py-1 rounded"
          >
            <Globe size={14} /> {lang.toUpperCase()}
          </button>
        </div>

        {/* Mobile Nav Toggle */}
        <div className="md:hidden text-primary">
          <Menu />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
