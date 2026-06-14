import { Terminal, Shield, FolderGit2, HelpCircle, Database, Cpu, Lock, Download, Zap, Code2, ChevronDown, Keyboard, Globe } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { TerminalSimulator } from '../components/terminal/TerminalSimulator';
import { useLanguage } from '../components/LanguageContext';
import { useState, useEffect, useRef, type ReactNode } from 'react';

/* ── Decrypt text on scroll ──────────────────────────────────── */
const DecryptText = ({ text, className }: { text: string; className?: string }) => {
  const [display, setDisplay] = useState(text.replace(/[a-zA-Z]/g, '_'));
  const [inView, setInView] = useState(false);
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const obs = new IntersectionObserver(([e]) => { if (e.isIntersecting) setInView(true); });
    if (ref.current) obs.observe(ref.current);
    return () => obs.disconnect();
  }, []);

  useEffect(() => {
    if (!inView) return;
    let it = 0;
    const id = setInterval(() => {
      setDisplay(text.split('').map((c, i) => {
        if (i < it || c === ' ') return c;
        return String.fromCharCode(33 + Math.floor(Math.random() * 93));
      }).join(''));
      if (it >= text.length) clearInterval(id);
      it += 1 / 3;
    }, 30);
    return () => clearInterval(id);
  }, [inView, text]);

  return <span ref={ref} className={className}>{display}</span>;
};

/* ── Animated counter ────────────────────────────────────────── */
const AnimatedCounter = ({ target, suffix = '' }: { target: number; suffix?: string }) => {
  const [n, setN] = useState(0);
  const [started, setStarted] = useState(false);
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const obs = new IntersectionObserver(([e]) => { if (e.isIntersecting) setStarted(true); }, { threshold: 0.6 });
    if (ref.current) obs.observe(ref.current);
    return () => obs.disconnect();
  }, []);

  useEffect(() => {
    if (!started) return;
    const steps = 36;
    let step = 0;
    const id = setInterval(() => {
      step++;
      setN(Math.round((target * step) / steps));
      if (step >= steps) clearInterval(id);
    }, 1100 / steps);
    return () => clearInterval(id);
  }, [started, target]);

  return <span ref={ref} className="animate-count-in">{n}{suffix}</span>;
};

/* ── Feature card with 3D hover tilt ────────────────────────── */
interface CardProps {
  icon: ReactNode; title: string; desc: string;
  borderCls: string; textCls: string; gradCls: string; index: number;
}
const FeatureCard = ({ icon, title, desc, borderCls, textCls, gradCls, index }: CardProps) => (
  <motion.div
    initial={{ opacity: 0, y: 18 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true }}
    transition={{ delay: index * 0.08, duration: 0.4, ease: [0.25, 0.1, 0.25, 1] }}
  >
    <motion.div
      whileHover={{ rotateX: -4, rotateY: 6, scale: 1.025 }}
      transition={{ type: 'spring', stiffness: 380, damping: 26 }}
      style={{ transformPerspective: 900, willChange: 'transform' }}
      className={`border-l-4 ${borderCls} bg-gradient-to-br ${gradCls} to-transparent p-6 h-full cursor-default border border-border/40 hover:border-primary/20 transition-colors`}
    >
      <div className={`flex items-center gap-3 ${textCls} mb-3`}>
        <span aria-hidden="true">{icon}</span>
        <h3 className="text-sm font-bold font-mono tracking-wider">{title}</h3>
      </div>
      <p className="text-on-background text-sm font-mono leading-relaxed">{desc}</p>
    </motion.div>
  </motion.div>
);

/* ── Accordion FAQ item ──────────────────────────────────────── */
const FaqItem = ({ q, a, borderCls, textCls }: { q: string; a: string; borderCls: string; textCls: string }) => {
  const [open, setOpen] = useState(false);
  return (
    <article className={`border-l-2 ${borderCls} bg-black/50 overflow-hidden hover:bg-black/70 transition-colors`}>
      <button
        onClick={() => setOpen(v => !v)}
        className="w-full text-left p-5 flex items-start justify-between gap-4 min-h-[52px]"
        aria-expanded={open}
      >
        <span className={`font-bold ${textCls} font-mono text-sm flex items-start gap-2 leading-snug`}>
          <span className="text-on-surface opacity-50 shrink-0 mt-0.5 text-xs">Q:/&gt;</span> {q}
        </span>
        <motion.span
          animate={{ rotate: open ? 180 : 0 }}
          transition={{ duration: 0.2 }}
          className={`${textCls} shrink-0 mt-0.5`}
          aria-hidden="true"
        >
          <ChevronDown size={16} />
        </motion.span>
      </button>
      <AnimatePresence initial={false}>
        {open && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.22, ease: 'easeInOut' }}
            className="overflow-hidden"
          >
            <p className="text-on-background font-mono text-sm px-5 pb-5 pl-12 border-t border-border/20 pt-4 leading-relaxed">
              <span className="text-primary opacity-70 mr-2 text-xs">[SYS]</span>{a}
            </p>
          </motion.div>
        )}
      </AnimatePresence>
    </article>
  );
};

/* ── Home page ───────────────────────────────────────────────── */
const Home = () => {
  const { t } = useLanguage();

  const stats = [
    { label: t('stat_downloads'), value: 500, suffix: '+', raw: '',       icon: <Download size={14} /> },
    { label: t('stat_version'),   value: 0,   suffix: '',  raw: 'v1.0.4', icon: <Terminal size={14} /> },
    { label: t('stat_themes'),    value: 3,   suffix: '',  raw: '',       icon: <Zap size={14} /> },
    { label: t('stat_arsenal'),   value: 9,   suffix: '',  raw: '',       icon: <Shield size={14} /> },
  ];

  const featureCards: Omit<CardProps, 'index'>[] = [
    { icon: <Terminal size={18} />,   title: t('feat_cli_title'),     desc: t('feat_cli_desc'),     borderCls: 'border-primary', textCls: 'text-primary', gradCls: 'from-primary/8' },
    { icon: <Shield size={18} />,     title: t('feat_sec_title'),     desc: t('feat_sec_desc'),     borderCls: 'border-cursor',  textCls: 'text-cursor',  gradCls: 'from-cursor/8'  },
    { icon: <Database size={18} />,   title: t('feat_cache_title'),   desc: t('feat_cache_desc'),   borderCls: 'border-prompt',  textCls: 'text-prompt',  gradCls: 'from-prompt/8'  },
    { icon: <Code2 size={18} />,      title: t('feat_matrix_title'),  desc: t('feat_matrix_desc'),  borderCls: 'border-primary', textCls: 'text-primary', gradCls: 'from-primary/5' },
    { icon: <Zap size={18} />,        title: t('feat_themes_title'),  desc: t('feat_themes_desc'),  borderCls: 'border-cursor',  textCls: 'text-cursor',  gradCls: 'from-cursor/5'  },
    { icon: <FolderGit2 size={18} />, title: t('feat_oss_title'),     desc: t('feat_oss_desc'),     borderCls: 'border-prompt',  textCls: 'text-prompt',  gradCls: 'from-prompt/5'  },
    { icon: <Globe size={18} />,      title: t('feat_arsenal_title'), desc: t('feat_arsenal_desc'), borderCls: 'border-primary', textCls: 'text-primary', gradCls: 'from-primary/8' },
    { icon: <Keyboard size={18} />,   title: t('feat_kb_title'),      desc: t('feat_kb_desc'),      borderCls: 'border-cursor',  textCls: 'text-cursor',  gradCls: 'from-cursor/8'  },
  ];

  const faqItems = [
    { q: t('faq_1_q'), a: t('faq_1_a'), borderCls: 'border-primary', textCls: 'text-primary' },
    { q: t('faq_2_q'), a: t('faq_2_a'), borderCls: 'border-cursor',  textCls: 'text-cursor'  },
    { q: t('faq_3_q'), a: t('faq_3_a'), borderCls: 'border-prompt',  textCls: 'text-prompt'  },
    { q: t('faq_4_q'), a: t('faq_4_a'), borderCls: 'border-primary', textCls: 'text-primary' },
    { q: t('faq_5_q'), a: t('faq_5_a'), borderCls: 'border-cursor',  textCls: 'text-cursor'  },
    { q: t('faq_6_q'), a: t('faq_6_a'), borderCls: 'border-prompt',  textCls: 'text-prompt'  },
  ];

  const arsenalModules = [
    { id: 'device',   name: 'DEVICE INTEGRITY', tagline: 'Root · ADB · bootloader · emulator',         severity: 'HIGH',   color: 'text-cursor',  isNew: false },
    { id: 'access',   name: 'ACCESS CONTROL',   tagline: 'Screen lock · biometrics · lock strength',   severity: 'MEDIUM', color: 'text-prompt',  isNew: false },
    { id: 'apprisk',  name: 'APP RISK',          tagline: 'Dangerous perms · sideloads · debug flags',  severity: 'MEDIUM', color: 'text-primary', isNew: false },
    { id: 'trust',    name: 'TRUST STORE',       tagline: 'User CAs · MITM proxy detection',            severity: 'HIGH',   color: 'text-cursor',  isNew: false },
    { id: 'network',  name: 'NETWORK INTEL',     tagline: 'IP · DNS · VPN · metered detection',         severity: 'LOW',    color: 'text-primary', isNew: false },
    { id: 'runtime',  name: 'RUNTIME TELEMETRY', tagline: 'Memory · CPU threads · process lifetime',    severity: 'INFO',   color: 'text-prompt',  isNew: false },
    { id: 'portscan', name: 'PORT SCAN',         tagline: 'TCP probe — 18 ports · localhost & gateway', severity: 'MEDIUM', color: 'text-cursor',  isNew: true  },
    { id: 'wifi',     name: 'WIFI RECON',        tagline: 'Signal · band · WEP/OPEN detection',         severity: 'LOW',    color: 'text-primary', isNew: true  },
    { id: 'ssl',      name: 'SSL INSPECTOR',     tagline: 'TLS audit — expiry · protocol · issuer',     severity: 'INFO',   color: 'text-prompt',  isNew: true  },
  ];

  return (
    <div className="py-10 flex flex-col gap-20">

      {/* ── Hero ─────────────────────────────────────────────── */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center" aria-label="Hero">
        <div className="flex flex-col gap-6">
          <div className="font-mono text-primary text-xs tracking-[0.3em] flex items-center gap-2 uppercase" aria-hidden="true">
            <span className="w-2 h-2 bg-primary animate-pulse rounded-full" />
            {t('hero_tag')}
          </div>
          <h1 className="text-5xl md:text-6xl font-bold leading-tight">
            {t('hero_title').split('Tensor')[0]}
            <span className="text-primary drop-shadow-[0_0_16px_rgba(0,255,65,0.5)]">Tensor</span>
            {t('hero_title').split('Tensor')[1]}
          </h1>
          <p className="text-on-background text-base max-w-lg leading-relaxed">
            {t('hero_desc')}
          </p>
          <div className="flex flex-wrap gap-3 mt-2">
            <a
              href="https://github.com/JOSETRA44/launchers/releases/"
              target="_blank" rel="noopener noreferrer"
              className="bg-primary text-black font-mono px-6 py-3 rounded font-bold hover:bg-cursor transition-colors shadow-[0_4px_20px_rgba(0,255,65,0.2)] hover:shadow-[0_6px_30px_rgba(0,255,65,0.4)] flex items-center gap-2 text-sm min-h-[48px]"
            >
              <Download size={16} aria-hidden="true" /> {t('btn_terminal')}
            </a>
            <a
              href="https://github.com/JOSETRA44/launchers"
              target="_blank" rel="noopener noreferrer"
              className="border border-border text-primary font-mono px-6 py-3 rounded hover:bg-primary/10 hover:border-primary/50 transition-colors flex items-center gap-2 text-sm min-h-[48px]"
            >
              <FolderGit2 size={16} aria-hidden="true" /> {t('btn_github')}
            </a>
          </div>
        </div>
        <TerminalSimulator />
      </section>

      {/* ── Stats Bar ────────────────────────────────────────── */}
      <section aria-label="Project statistics">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {stats.map((s, i) => (
            <motion.div
              key={s.label}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.09 }}
              className="border border-border bg-surface p-5 text-center relative overflow-hidden group hover:border-primary/40 transition-colors"
            >
              <div className="absolute inset-0 bg-gradient-to-b from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" aria-hidden="true" />
              <div className="text-on-surface text-[11px] font-mono mb-2 flex items-center justify-center gap-1.5">
                <span aria-hidden="true">{s.icon}</span> {s.label}
              </div>
              <div className="text-3xl font-bold text-primary font-mono">
                {s.raw ? s.raw : <AnimatedCounter target={s.value} suffix={s.suffix} />}
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* ── Features + Architecture ───────────────────────────── */}
      <section className="flex flex-col gap-10" aria-label="Features and architecture">
        <div className="flex flex-col gap-2 relative pl-6">
          <div className="absolute left-0 top-0 bottom-0 w-px bg-gradient-to-b from-transparent via-primary to-transparent opacity-30" aria-hidden="true" />
          <div className="absolute left-[-3px] top-2 w-2 h-2 bg-primary animate-ping rounded-full" aria-hidden="true" />
          <div className="font-mono text-[10px] text-on-surface tracking-[0.3em] uppercase">
            [MODULE_STATUS: ONLINE]
          </div>
          <h2 className="text-3xl font-bold font-mono uppercase tracking-wider text-white">
            <DecryptText text={t('engineered')} />
          </h2>
          <p className="text-on-background max-w-2xl text-sm font-mono leading-relaxed border-l-2 border-primary/40 pl-4 py-1">
            {t('engineered_desc')}
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
          {/* 6-card feature grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {featureCards.map((card, i) => (
              <FeatureCard key={card.title} {...card} index={i} />
            ))}
          </div>

          {/* DDD Architecture tree */}
          <div className="flex flex-col gap-4">
            <div className="flex items-center gap-2 text-on-surface font-mono text-xs border-b border-border pb-2">
              <Cpu size={14} aria-hidden="true" /> SYSTEM ARCHITECTURE [DDD + CLEAN]
            </div>
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              className="font-mono text-xs border border-border bg-[#050505] p-5 rounded text-primary shadow-inner overflow-x-auto relative"
            >
              <div className="absolute top-3 right-3 opacity-10" aria-hidden="true"><Lock size={36} /></div>
              <div className="text-primary font-bold mb-3 drop-shadow-[0_0_5px_var(--primary)]">[/] tensor/app/src/main/java</div>
              <div className="pl-4 border-l border-primary/30 flex flex-col gap-1 text-primary/80">
                <div className="hover:text-white transition-colors cursor-crosshair">├── domain/ <span className="opacity-40 text-[10px]">// 0 Android imports</span></div>
                <div className="pl-5 opacity-70">│   ├── model/       <span className="opacity-50"># Core entities</span></div>
                <div className="pl-5 opacity-70">│   ├── port/        <span className="opacity-50"># Interfaces</span></div>
                <div className="pl-5 opacity-70">│   └── usecase/     <span className="opacity-50"># Business logic</span></div>
                <div className="hover:text-white transition-colors mt-2 cursor-crosshair">├── data/ <span className="opacity-40 text-[10px]">// Adapters</span></div>
                <div className="pl-5 opacity-70">│   ├── repository/  <span className="opacity-50"># Implementations</span></div>
                <div className="pl-5 opacity-70">│   ├── source/      <span className="opacity-50"># DataStore / PM</span></div>
                <div className="pl-5 text-cursor/80">│   └── arsenal/     <span className="opacity-50"># 9 security modules</span></div>
                <div className="hover:text-white transition-colors mt-2 cursor-crosshair">└── presentation/ <span className="opacity-40 text-[10px]">// UI layer</span></div>
                <div className="pl-5 opacity-70">    ├── ui/          <span className="opacity-50"># Jetpack Compose</span></div>
                <div className="pl-5 opacity-70">    └── viewmodel/   <span className="opacity-50"># StateFlow holders</span></div>
              </div>
            </motion.div>
            <div className="border border-border bg-surface p-4 text-xs font-mono text-on-surface">
              <span className="text-primary animate-pulse inline-block mr-2" aria-hidden="true">●</span>
              Live Memory: Optimal. GC cycles reduced ~40% via strictly scoped ViewModels.
            </div>
          </div>
        </div>
      </section>

      {/* ── Arsenal Modules Grid ─────────────────────────────── */}
      <section className="flex flex-col gap-8" aria-label="Arsenal security modules">
        <div className="flex flex-col gap-2 relative pl-6">
          <div className="absolute left-0 top-0 bottom-0 w-px bg-gradient-to-b from-transparent via-cursor to-transparent opacity-30" aria-hidden="true" />
          <div className="absolute left-[-3px] top-2 w-2 h-2 bg-cursor animate-ping rounded-full" aria-hidden="true" />
          <div className="font-mono text-[10px] text-on-surface tracking-[0.3em] uppercase">
            [ARSENAL_REGISTRY: 9/9 MODULES LOADED]
          </div>
          <h2 className="text-3xl font-bold font-mono uppercase tracking-wider text-white">
            <DecryptText text={t('arsenal_section_title')} />
          </h2>
          <p className="text-on-background max-w-2xl text-sm font-mono leading-relaxed border-l-2 border-cursor/40 pl-4 py-1">
            {t('arsenal_section_desc')}
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {arsenalModules.map((mod, i) => {
            const severityColor: Record<string, string> = {
              CRITICAL: 'text-red-400 border-red-400/40 bg-red-400/5',
              HIGH:     'text-orange-400 border-orange-400/40 bg-orange-400/5',
              MEDIUM:   'text-yellow-400 border-yellow-400/40 bg-yellow-400/5',
              LOW:      'text-blue-400 border-blue-400/40 bg-blue-400/5',
              INFO:     'text-green-400 border-green-400/40 bg-green-400/5',
            };
            return (
              <motion.div
                key={mod.id}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.06, duration: 0.35 }}
                className="border border-border bg-black/60 p-4 relative group hover:border-primary/30 transition-colors overflow-hidden"
              >
                {mod.isNew && (
                  <span className="absolute top-3 right-3 text-[9px] font-mono font-bold text-cursor border border-cursor/50 px-1.5 py-0.5 bg-cursor/10">NEW</span>
                )}
                <div className={`font-mono text-xs font-bold mb-1 ${mod.color}`}>{mod.name}</div>
                <p className="text-on-surface font-mono text-[11px] leading-relaxed mb-3">{mod.tagline}</p>
                <span className={`text-[9px] font-mono font-bold px-2 py-0.5 border ${severityColor[mod.severity] ?? ''}`}>
                  ▲ {mod.severity}
                </span>
                <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-primary/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" aria-hidden="true" />
              </motion.div>
            );
          })}
        </div>
      </section>

      {/* ── Install Steps ────────────────────────────────────── */}
      <section className="flex flex-col gap-6" aria-label="Installation steps">
        <h2 className="text-2xl font-bold font-mono tracking-wider text-white flex items-center gap-3">
          <Download size={22} className="text-primary" aria-hidden="true" />
          {t('install_title')}
        </h2>
        <ol className="grid grid-cols-1 sm:grid-cols-3 gap-4" role="list">
          {[
            { n: '01', title: t('install_step1'), desc: t('install_step1_desc') },
            { n: '02', title: t('install_step2'), desc: t('install_step2_desc') },
            { n: '03', title: t('install_step3'), desc: t('install_step3_desc') },
          ].map((step, i) => (
            <motion.li
              key={step.n}
              initial={{ opacity: 0, y: 14 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.12 }}
              className="border border-border bg-surface p-6 relative overflow-hidden group hover:border-primary/40 transition-colors"
            >
              <div className="absolute top-4 right-4 font-mono text-4xl font-bold text-primary/10 select-none" aria-hidden="true">{step.n}</div>
              <div className="text-primary font-mono font-bold text-xl mb-2">{step.n}</div>
              <h3 className="text-white font-bold font-mono text-sm mb-1">{step.title}</h3>
              <p className="text-on-surface text-xs font-mono leading-relaxed">{step.desc}</p>
            </motion.li>
          ))}
        </ol>
      </section>

      {/* ── FAQ ──────────────────────────────────────────────── */}
      <section className="flex flex-col gap-6" aria-labelledby="faq-heading">
        <div className="flex items-center gap-3 border-b border-primary/20 pb-4">
          <HelpCircle className="text-primary" size={24} aria-hidden="true" />
          <h2 id="faq-heading" className="text-2xl font-bold font-mono tracking-wider text-white">
            {t('faq_title')}
          </h2>
        </div>
        <div className="flex flex-col gap-2">
          {faqItems.map((item) => (
            <FaqItem key={item.q} {...item} />
          ))}
        </div>
      </section>

    </div>
  );
};

export default Home;
