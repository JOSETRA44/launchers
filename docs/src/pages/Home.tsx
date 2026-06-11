import { Terminal, Shield, FolderGit2, HelpCircle, Database, Cpu, Lock } from 'lucide-react';
import { motion } from 'framer-motion';
import { TerminalSimulator } from '../components/terminal/TerminalSimulator';
import { useLanguage } from '../components/LanguageContext';
import { useState, useEffect, useRef } from 'react';

const DecryptText = ({ text, className }: { text: string, className?: string }) => {
  const [display, setDisplay] = useState(text.replace(/[a-zA-Z]/g, '_'));
  const [inView, setInView] = useState(false);
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) setInView(true);
    });
    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!inView) return;
    let iteration = 0;
    const interval = setInterval(() => {
      setDisplay(text.split('').map((char, i) => {
        if (i < iteration || char === ' ') return char;
        return String.fromCharCode(33 + Math.floor(Math.random() * 93));
      }).join(''));
      
      if (iteration >= text.length) clearInterval(interval);
      iteration += 1 / 3;
    }, 30);
    return () => clearInterval(interval);
  }, [inView, text]);

  return <span ref={ref} className={className}>{display}</span>;
};

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

      {/* Hacker Architecture & Info Matrix */}
      <section className="flex flex-col gap-12 mt-12 relative">
        <div className="absolute left-0 top-0 bottom-0 w-px bg-gradient-to-b from-transparent via-primary to-transparent opacity-30"></div>
        
        <div className="pl-8 relative">
          <div className="absolute left-[-4px] top-2 w-2 h-2 bg-primary animate-ping rounded-full"></div>
          <h2 className="text-3xl font-bold mb-4 font-mono uppercase text-white tracking-widest"><DecryptText text={t('engineered')} /></h2>
          <p className="text-on-surface max-w-3xl border-l-2 border-primary/50 pl-4 py-2 font-mono text-sm bg-primary/5">
            {t('engineered_desc')}
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 pl-8">
          
          {/* Core Modules List */}
          <div className="flex flex-col gap-6">
            <motion.div 
              initial={{ opacity: 0, x: -20 }} whileInView={{ opacity: 1, x: 0 }} viewport={{ once: true }}
              className="border-l-4 border-primary bg-gradient-to-r from-primary/10 to-transparent p-6 group"
            >
              <div className="flex items-center gap-3 text-primary mb-2">
                <Terminal size={20} /> <h3 className="text-lg font-bold font-mono tracking-wider">CLI ENGINE_</h3>
              </div>
              <p className="text-on-background text-sm font-mono leading-relaxed">
                Fuzzy-matching architecture capable of executing system directives, launching deep-linked apps, and manipulating state instantly. 
                Zero-lag parsing achieved through coroutines.
              </p>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }} whileInView={{ opacity: 1, x: 0 }} viewport={{ once: true }} transition={{ delay: 0.1 }}
              className="border-l-4 border-cursor bg-gradient-to-r from-cursor/10 to-transparent p-6 group"
            >
              <div className="flex items-center gap-3 text-cursor mb-2">
                <Shield size={20} /> <h3 className="text-lg font-bold font-mono tracking-wider">SECURITY.SYS_</h3>
              </div>
              <p className="text-on-background text-sm font-mono leading-relaxed">
                Passive environment auditing. Detects active ADB debug states, root binaries (`su`), and evaluates device lock integrity. 
                Includes a built-in cryptographic token generator.
              </p>
            </motion.div>
            
            <motion.div 
              initial={{ opacity: 0, x: -20 }} whileInView={{ opacity: 1, x: 0 }} viewport={{ once: true }} transition={{ delay: 0.2 }}
              className="border-l-4 border-prompt bg-gradient-to-r from-prompt/10 to-transparent p-6 group"
            >
              <div className="flex items-center gap-3 text-prompt mb-2">
                <Database size={20} /> <h3 className="text-lg font-bold font-mono tracking-wider">SMART CACHE_</h3>
              </div>
              <p className="text-on-background text-sm font-mono leading-relaxed">
                A localized neural-like decay algorithm ranks your apps. Apps you use drop in priority if ignored over time. Stored efficiently in DataStore.
              </p>
            </motion.div>
          </div>

          {/* DDD Architecture Tree */}
          <div className="flex flex-col gap-4">
            <div className="flex items-center gap-2 text-on-surface font-mono text-sm border-b border-border pb-2">
              <Cpu size={16} /> SYSTEM ARCHITECTURE [DDD + CLEAN]
            </div>
            <motion.div 
              initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}
              className="font-mono text-xs sm:text-sm border border-border bg-[#050505] p-5 rounded text-primary-dim shadow-inner overflow-x-auto relative"
            >
              <div className="absolute top-0 right-0 p-2 opacity-20"><Lock size={40} /></div>
              <div className="text-primary font-bold mb-3 drop-shadow-[0_0_5px_var(--primary)]">[/] tensor/app/src/main/java</div>
              <div className="pl-2 sm:pl-4 border-l border-primary/30 flex flex-col gap-1">
                <div className="text-on-bg hover:text-white transition-colors cursor-crosshair">├── domain/ <span className="opacity-50 text-xs">// 0 Android Dependencies</span></div>
                <div className="pl-6 opacity-80">│   ├── model/       <span className="opacity-50"># Core entities</span></div>
                <div className="pl-6 opacity-80">│   ├── repository/  <span className="opacity-50"># Ports (Interfaces)</span></div>
                <div className="pl-6 opacity-80">│   └── usecase/     <span className="opacity-50"># Business logic</span></div>
                <div className="text-on-bg hover:text-white transition-colors mt-2 cursor-crosshair">├── data/ <span className="opacity-50 text-xs">// Adapters</span></div>
                <div className="pl-6 opacity-80">│   ├── repository/  <span className="opacity-50"># Implementations</span></div>
                <div className="pl-6 opacity-80">│   └── local/       <span className="opacity-50"># Room / DataStore</span></div>
                <div className="text-on-bg hover:text-white transition-colors mt-2 cursor-crosshair">└── presentation/ <span className="opacity-50 text-xs">// UI Layer</span></div>
                <div className="pl-6 opacity-80">    ├── ui/          <span className="opacity-50"># Jetpack Compose UI</span></div>
                <div className="pl-6 opacity-80">    └── viewmodel/   <span className="opacity-50"># State holders</span></div>
              </div>
            </motion.div>
            
            <div className="mt-4 border border-border bg-surface p-4 text-xs font-mono text-on-surface">
              <span className="text-primary animate-pulse inline-block mr-2">●</span> 
              Live Memory Diagnostics: Optimal. Garbage Collector cycles reduced by 40% due to strictly scoped ViewModels.
            </div>
          </div>

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
