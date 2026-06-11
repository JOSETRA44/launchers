import { Terminal, Shield, Zap, Activity, FolderGit2, HelpCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import { TerminalSimulator } from '../components/terminal/TerminalSimulator';
import { useLanguage } from '../components/LanguageContext';

const Home = () => {
  const { t } = useLanguage();

  return (
    <div className="py-12 flex flex-col gap-24">
      {/* Hero Section */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
        <div className="flex flex-col gap-6">
          <div className="font-mono text-primary text-sm tracking-widest flex items-center gap-2 uppercase">
            <span className="w-2 h-2 bg-primary animate-pulse rounded-full"></span>
            {t('hero_tag')}
          </div>
          <h1 className="text-5xl md:text-6xl font-bold leading-tight">
            {t('hero_title').split('Tensor')[0]}
            <span className="text-primary drop-shadow-[0_0_10px_rgba(0,255,65,0.4)]">Tensor</span>
            {t('hero_title').split('Tensor')[1]}
          </h1>
          <p className="text-on-background text-lg max-w-lg">
            {t('hero_desc')}
          </p>
          <div className="flex flex-wrap gap-4 mt-4">
            <button className="bg-primary text-black font-mono px-6 py-3 rounded font-semibold hover:bg-cursor transition-colors shadow-[0_4px_14px_rgba(0,255,65,0.15)] hover:shadow-[0_6px_20px_rgba(0,255,65,0.35)] flex items-center gap-2">
              <Terminal size={18} /> {t('btn_terminal')}
            </button>
            <button className="border border-border text-primary font-mono px-6 py-3 rounded hover:bg-primary/10 transition-colors flex items-center gap-2">
              <FolderGit2 size={18} /> {t('btn_github')}
            </button>
          </div>
        </div>

        {/* Interactive Hero Terminal Simulator */}
        <TerminalSimulator />
      </section>

      {/* Bento Grid Features */}
      <section className="flex flex-col gap-8">
        <div className="text-center max-w-2xl mx-auto mb-8">
          <h2 className="text-3xl font-bold mb-4">{t('engineered')}</h2>
          <p className="text-on-surface">{t('engineered_desc')}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <motion.div whileHover={{ y: -5 }} className="col-span-1 md:col-span-2 bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors group">
            <div className="w-10 h-10 bg-primary-dim rounded flex items-center justify-center text-primary mb-4 group-hover:scale-110 transition-transform">
              <Terminal size={24} />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">CLI Command Engine</h3>
            <p className="text-on-background">Launch applications, pin items to dock containers, switch theme schemes, create folder directories, and query web information in real-time. Features a command parsing architecture with fuzzy auto-matching.</p>
          </motion.div>

          <motion.div whileHover={{ y: -5 }} className="col-span-1 bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors group">
            <div className="w-10 h-10 bg-primary-dim rounded flex items-center justify-center text-primary mb-4 group-hover:scale-110 transition-transform">
              <Zap size={24} />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Charging Overlay</h3>
            <p className="text-on-background">Fully immersive screen charging overlay displaying ASCII levels and rendering a digital canvas rain backdrop.</p>
          </motion.div>

          <motion.div whileHover={{ y: -5 }} className="col-span-1 bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors group">
            <div className="w-10 h-10 bg-primary-dim rounded flex items-center justify-center text-primary mb-4 group-hover:scale-110 transition-transform">
              <Activity size={24} />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Smart Ranking</h3>
            <p className="text-on-background">Apps automatically rank in the app drawer list based on recent use frequencies decay-weighted over time.</p>
          </motion.div>

          <motion.div whileHover={{ y: -5 }} className="col-span-1 md:col-span-2 bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors group">
            <div className="w-10 h-10 bg-primary-dim rounded flex items-center justify-center text-primary mb-4 group-hover:scale-110 transition-transform">
              <Shield size={24} />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Security Toolkit</h3>
            <p className="text-on-background">Check security flags such as active screen locks, adb configurations, dev mode state, and su root binaries. Includes a secure passphrase generator with custom character classes avoiding ambiguous glyphs.</p>
          </motion.div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="flex flex-col gap-8">
        <div className="flex items-center gap-3 mb-4 border-b border-border pb-4">
          <HelpCircle className="text-primary" size={28} />
          <h2 className="text-3xl font-bold">{t('faq_title')}</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors">
            <h3 className="text-xl font-bold text-white mb-2">{t('faq_1_q')}</h3>
            <p className="text-on-background leading-relaxed">{t('faq_1_a')}</p>
          </div>
          <div className="bg-surface border border-border p-6 rounded-lg shadow-lg hover:border-primary transition-colors">
            <h3 className="text-xl font-bold text-white mb-2">{t('faq_2_q')}</h3>
            <p className="text-on-background leading-relaxed">{t('faq_2_a')}</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;
