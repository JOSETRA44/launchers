import { Terminal, FolderGit2, MessageSquare, Mail } from 'lucide-react';
import { useLanguage } from '../LanguageContext';
import { Link } from 'react-router-dom';

const Footer = () => {
  const { t } = useLanguage();
  return (
    <footer className="border-t border-border/50 bg-background/80 backdrop-blur-sm mt-24 py-12 relative overflow-hidden">
      <div className="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary/50 to-transparent"></div>
      <div className="container mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-8 font-mono text-sm">
        <div className="col-span-1 md:col-span-2 flex flex-col gap-4">
          <div className="flex items-center gap-2 text-primary text-xl font-bold">
            <Terminal size={24} /> Tensor_OS
          </div>
          <p className="text-on-background opacity-70 max-w-sm leading-relaxed">
            {t('hero_desc')}
          </p>
        </div>
        
        <div className="flex flex-col gap-3">
          <h4 className="text-white font-bold mb-2">SYSTEM_LINKS</h4>
          <Link to="/" className="text-primary-dim hover:text-primary transition-colors">[/] Root (Home)</Link>
          <Link to="/resenas" className="text-primary-dim hover:text-primary transition-colors">[/] Reviews</Link>
          <a href="https://github.com/JOSETRA44/launchers/releases/" target="_blank" rel="noopener noreferrer" className="text-primary-dim hover:text-primary transition-colors">[/] Changelog (Releases)</a>
        </div>

        <div className="flex flex-col gap-3">
          <h4 className="text-white font-bold mb-2">NETWORK</h4>
          <a href="https://github.com/JOSETRA44/launchers" target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 text-primary-dim hover:text-primary transition-colors"><FolderGit2 size={16}/> GitHub Repo</a>
          <a href="https://github.com/JOSETRA44/launchers/releases/" target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 text-primary-dim hover:text-primary transition-colors"><MessageSquare size={16}/> Releases & APKs</a>
        </div>
      </div>
      
      <div className="container mx-auto px-6 mt-12 pt-6 border-t border-border/30 flex flex-col md:flex-row justify-between items-center text-xs text-on-surface opacity-50">
        <p>© 2026 Tensor Open Source Project. All rights reversed.</p>
        <p className="mt-2 md:mt-0">Built with React, Vite & Tailwind [Clean Architecture]</p>
      </div>
    </footer>
  );
};

export default Footer;
