import { Terminal, FolderGit2, Download, Star, Shield } from 'lucide-react';
import { useLanguage } from '../LanguageContext';
import { Link } from 'react-router-dom';

const Footer = () => {
  const { t } = useLanguage();

  return (
    <footer className="border-t border-border/40 bg-background/90 backdrop-blur-sm mt-24 py-14 relative overflow-hidden" role="contentinfo">
      {/* Top accent line */}
      <div className="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary/40 to-transparent" aria-hidden="true" />

      {/* Dim background glyph */}
      <div className="absolute right-0 bottom-0 opacity-[0.03] select-none pointer-events-none" aria-hidden="true">
        <Terminal size={240} />
      </div>

      <div className="container mx-auto px-6 max-w-7xl grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10 font-mono text-sm relative">

        {/* Brand column */}
        <div className="sm:col-span-2 flex flex-col gap-4">
          <div className="flex items-center gap-2 text-primary text-lg font-bold" aria-label="Tensor Launcher">
            <div className="w-8 h-8 bg-primary-dim flex items-center justify-center rounded border border-primary/30" aria-hidden="true">
              <span className="text-xs leading-none">&gt;_</span>
            </div>
            Tensor_OS
          </div>
          <p className="text-on-background opacity-70 max-w-sm leading-relaxed text-xs">
            {t('hero_desc')}
          </p>
          <div className="flex items-center gap-2 text-xs text-on-surface">
            <Shield size={12} className="text-primary" aria-hidden="true" />
            MIT License · Android 7.0+ · DDD Clean Architecture
          </div>
        </div>

        {/* Links */}
        <nav aria-label="Site navigation links">
          <h4 className="text-white font-bold mb-4 text-xs tracking-widest uppercase">SYSTEM_LINKS</h4>
          <ul className="flex flex-col gap-2.5">
            <li>
              <Link to="/" className="text-primary-dim hover:text-primary transition-colors text-xs">
                [/] {t('nav_home')}
              </Link>
            </li>
            <li>
              <Link to="/resenas" className="text-primary-dim hover:text-primary transition-colors text-xs">
                [/] {t('nav_reviews')}
              </Link>
            </li>
            <li>
              <a href="https://github.com/JOSETRA44/launchers/releases/" target="_blank" rel="noopener noreferrer" className="text-primary-dim hover:text-primary transition-colors text-xs">
                [/] {t('nav_changelog')}
              </a>
            </li>
          </ul>
        </nav>

        {/* Network */}
        <nav aria-label="External network links">
          <h4 className="text-white font-bold mb-4 text-xs tracking-widest uppercase">NETWORK</h4>
          <ul className="flex flex-col gap-2.5">
            <li>
              <a
                href="https://github.com/JOSETRA44/launchers"
                target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 text-primary-dim hover:text-primary transition-colors text-xs"
              >
                <FolderGit2 size={13} aria-hidden="true" /> GitHub Repository
              </a>
            </li>
            <li>
              <a
                href="https://github.com/JOSETRA44/launchers/releases/"
                target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 text-primary-dim hover:text-primary transition-colors text-xs"
              >
                <Download size={13} aria-hidden="true" /> Releases &amp; APKs
              </a>
            </li>
            <li>
              <a
                href="https://github.com/JOSETRA44/launchers"
                target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 text-primary-dim hover:text-primary transition-colors text-xs"
              >
                <Star size={13} aria-hidden="true" /> Star on GitHub
              </a>
            </li>
          </ul>
        </nav>
      </div>

      {/* Bottom bar */}
      <div className="container mx-auto px-6 max-w-7xl mt-10 pt-6 border-t border-border/20 flex flex-col sm:flex-row justify-between items-center gap-2 text-[11px] text-on-surface opacity-50 font-mono">
        <p>© 2026 Tensor Open Source Project. All rights reversed.</p>
        <p>Built with React + Vite + Tailwind · Clean Architecture</p>
      </div>
    </footer>
  );
};

export default Footer;
