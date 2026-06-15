import { motion } from 'framer-motion';
import { Terminal, Layers, Cpu, Database, Shield, ExternalLink, ArrowRight, ChevronRight } from 'lucide-react';
import { useLanguage } from '../components/LanguageContext';

/* ── Domain types ───────────────────────────────────────────── */

interface Command { readonly cmd: string; readonly alias: string | null; readonly args: string; readonly desc: string; readonly example: string }
interface ArchLayer { readonly id: string; readonly label: string; readonly dir: string; readonly rule: string; readonly contents: readonly string[]; readonly color: string; readonly border: string; readonly bg: string }
interface TechSpec { readonly label: string; readonly value: string; readonly note: string }
interface RankStep { readonly step: string; readonly title: string; readonly desc: string }

/* ── Static data ────────────────────────────────────────────── */

const commands: readonly Command[] = [
  { cmd: '/help',    alias: '?',         args: '—',                  desc: 'Lists all directives with their syntax and usage description.',        example: '> ? → prints command table'   },
  { cmd: '/theme',   alias: null,        args: '<dark|cyan|matrix>',  desc: 'Switches the global color palette live — no restart required.',        example: '> /theme cyan → Hacker Cyan'  },
  { cmd: '/sec',     alias: null,        args: '—',                  desc: 'Passive security audit: ADB state, root binaries, screen lock check.',  example: '> /sec → environment report'  },
  { cmd: '/stats',   alias: null,        args: '—',                  desc: 'Screen-time insight metrics for the current device session.',           example: '> /stats → uptime + launches' },
  { cmd: '/genpass', alias: null,        args: '[8–64]',             desc: 'Generates a cryptographically secure random token. Default length 16.', example: '> /genpass 32 → Xp9Q...Zr4'  },
  { cmd: '/clean',   alias: 'cls,clear', args: '—',                  desc: 'Wipes all rows from the terminal output buffer instantly.',             example: '> cls → buffer cleared'       },
  { cmd: '<app>',    alias: null,        args: '—',                  desc: 'Fuzzy-matches any installed app by name and launches it immediately.',  example: '> chro → launches Chrome'     },
] as const;

const archLayers: readonly ArchLayer[] = [
  {
    id: 'domain', label: 'DOMAIN', dir: 'domain/', rule: 'Zero Android imports',
    contents: ['model/ — AppInfo, ThemeId, ThemeConfig', 'port/ — Repository & Launcher interfaces', 'usecase/ — invoke() pattern, one class per op'],
    color: 'text-primary', border: 'border-primary/40', bg: 'bg-primary/5',
  },
  {
    id: 'data', label: 'DATA', dir: 'data/', rule: 'Implements domain ports',
    contents: ['AppDataSource — PackageManager adapter', 'AppRepositoryImpl — callbackFlow + BroadcastReceiver', 'WallpaperRepositoryImpl — Bitmap pipeline', 'UiPrefsDataSource — DataStore Preferences'],
    color: 'text-cursor', border: 'border-cursor/40', bg: 'bg-cursor/5',
  },
  {
    id: 'presentation', label: 'PRESENTATION', dir: 'presentation/', rule: 'Depends on domain only',
    contents: ['LauncherViewModel — CLI parsing + smart ranking', 'AppListViewModel — search & filter flow', 'SettingsViewModel — live theme switching', 'Compose screens — Launcher, AppList, Settings'],
    color: 'text-prompt', border: 'border-prompt/40', bg: 'bg-prompt/5',
  },
] as const;

const rankSteps: readonly RankStep[] = [
  { step: '01', title: 'Launch recorded',      desc: 'Each open increments the app\'s frequency counter, persisted in DataStore Preferences.' },
  { step: '02', title: 'Time decay applied',   desc: 'Score decreases exponentially as idle time grows — parameterized by a configurable λ factor.' },
  { step: '03', title: 'Ranked & surfaced',    desc: 'Apps sorted by final score. Freshly launched apps rise; long-idle apps sink without being deleted.' },
] as const;

const specs: readonly TechSpec[] = [
  { label: 'minSdk',     value: '24',              note: 'Android 7.0 Nougat' },
  { label: 'targetSdk',  value: '36',              note: 'Android 15'         },
  { label: 'Language',   value: 'Kotlin',          note: '2.x + coroutines'   },
  { label: 'UI Kit',     value: 'Jetpack Compose', note: 'Material3'          },
  { label: 'DI',         value: 'Manual',          note: 'Zero Hilt / Dagger' },
  { label: 'Storage',    value: 'DataStore',       note: 'Preferences API'    },
  { label: 'Net Perms',  value: 'None',            note: 'No network needed'  },
  { label: 'Permission', value: 'BOOT_RECEIVED',   note: 'Optional only'      },
  { label: 'APK Size',   value: '< 5 MB',          note: 'Release build'      },
] as const;

/* ── Shared motion helper ───────────────────────────────────── */

const EASE: [number, number, number, number] = [0.25, 0.1, 0.25, 1];

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 28 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: '-60px' },
  transition: { duration: 0.45, delay, ease: EASE },
});

/* ── Features page ─────────────────────────────────────────── */

const Features = () => {
  const { t } = useLanguage();

  return (
    <div className="py-12 flex flex-col gap-24">

      {/* ── Page header ──────────────────────────────────────── */}
      <motion.header {...fadeUp()} className="flex flex-col gap-5 max-w-2xl">
        <div className="font-mono text-[10px] text-on-surface tracking-[0.3em] uppercase flex items-center gap-2">
          <span className="text-primary animate-pulse" aria-hidden="true">●</span>
          {t('feat_page_tag')}
        </div>
        <h1 className="text-4xl sm:text-5xl font-bold font-mono text-white leading-tight">
          {t('feat_page_title')}<span className="text-primary">_</span>
        </h1>
        <p className="text-on-background font-mono text-sm leading-relaxed">{t('feat_page_desc')}</p>
      </motion.header>

      {/* ── CLI Command Reference ─────────────────────────────── */}
      <section aria-labelledby="cmd-title">
        <motion.div {...fadeUp(0.05)} className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <Terminal size={18} className="text-primary" aria-hidden="true" />
            <h2 id="cmd-title" className="text-xl font-bold font-mono text-white">{t('feat_cmd_title')}</h2>
          </div>
          <p className="text-on-background font-mono text-sm">{t('feat_cmd_desc')}</p>
        </motion.div>

        {/* Terminal window */}
        <motion.div {...fadeUp(0.1)} className="bg-surface border border-border overflow-hidden">
          <div className="flex items-center gap-2 px-4 py-3 border-b border-border bg-background/50" aria-hidden="true">
            <span className="w-3 h-3 rounded-full bg-error/60" />
            <span className="w-3 h-3 rounded-full bg-cursor/60" />
            <span className="w-3 h-3 rounded-full bg-primary/60" />
            <span className="font-mono text-[11px] text-on-surface ml-3 tracking-widest">TENSOR // COMMAND REFERENCE v1.0.4</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full font-mono text-xs" role="table">
              <thead>
                <tr className="border-b border-border/60 bg-background/30">
                  <th scope="col" className="text-left px-4 py-3 text-on-surface text-[10px] tracking-widest uppercase">COMMAND</th>
                  <th scope="col" className="text-left px-4 py-3 text-on-surface text-[10px] tracking-widest uppercase hidden sm:table-cell">ALIAS</th>
                  <th scope="col" className="text-left px-4 py-3 text-on-surface text-[10px] tracking-widest uppercase hidden md:table-cell">ARGS</th>
                  <th scope="col" className="text-left px-4 py-3 text-on-surface text-[10px] tracking-widest uppercase">DESCRIPTION</th>
                  <th scope="col" className="text-left px-4 py-3 text-on-surface text-[10px] tracking-widest uppercase hidden lg:table-cell">EXAMPLE</th>
                </tr>
              </thead>
              <tbody>
                {commands.map((c, i) => (
                  <motion.tr
                    key={c.cmd}
                    initial={{ opacity: 0, x: -12 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.12 + i * 0.05, duration: 0.3 }}
                    className="border-b border-border/30 hover:bg-primary/5 transition-colors"
                  >
                    <td className="px-4 py-3.5 text-primary font-bold whitespace-nowrap">{c.cmd}</td>
                    <td className="px-4 py-3.5 text-on-surface hidden sm:table-cell whitespace-nowrap">{c.alias ?? '—'}</td>
                    <td className="px-4 py-3.5 text-cursor hidden md:table-cell whitespace-nowrap">{c.args}</td>
                    <td className="px-4 py-3.5 text-on-background leading-relaxed">{c.desc}</td>
                    <td className="px-4 py-3.5 text-on-surface/60 hidden lg:table-cell italic whitespace-nowrap">{c.example}</td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        </motion.div>
      </section>

      {/* ── DDD Architecture ─────────────────────────────────── */}
      <section aria-labelledby="arch-title">
        <motion.div {...fadeUp(0.05)} className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <Layers size={18} className="text-primary" aria-hidden="true" />
            <h2 id="arch-title" className="text-xl font-bold font-mono text-white">{t('feat_arch_title')}</h2>
          </div>
          <p className="text-on-background font-mono text-sm max-w-xl">{t('feat_arch_desc')}</p>
        </motion.div>

        {/* Dependency rule banner */}
        <motion.div
          {...fadeUp(0.1)}
          className="font-mono text-xs text-center py-4 border border-border bg-background/40 mb-6 flex items-center justify-center gap-3 flex-wrap"
          aria-label="Dependency rule: presentation depends on domain, data depends on domain"
        >
          <span className="text-prompt font-bold tracking-widest">PRESENTATION</span>
          <span className="text-border text-lg" aria-hidden="true">──→</span>
          <span className="text-primary font-bold text-glow tracking-widest">DOMAIN</span>
          <span className="text-border text-lg" aria-hidden="true">←──</span>
          <span className="text-cursor font-bold tracking-widest">DATA</span>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {archLayers.map((layer, i) => (
            <motion.div
              key={layer.id}
              {...fadeUp(0.1 + i * 0.08)}
              whileHover={{ rotateX: -3, rotateY: 4, scale: 1.015, transition: { type: 'spring', stiffness: 380, damping: 28 } }}
              style={{ transformPerspective: 900, willChange: 'transform' }}
              className={`${layer.bg} border ${layer.border} p-5 flex flex-col gap-3`}
            >
              <div className="flex items-center justify-between">
                <span className={`${layer.color} font-mono font-bold text-xs tracking-widest`}>{layer.label}</span>
                <code className="font-mono text-[10px] text-on-surface border border-border px-2 py-0.5 rounded bg-background/40">{layer.dir}</code>
              </div>
              <div className={`font-mono text-[10px] ${layer.color} opacity-70 tracking-wider`}>{layer.rule}</div>
              <ul className="flex flex-col gap-1.5 mt-1" role="list">
                {layer.contents.map(item => (
                  <li key={item} className="font-mono text-[11px] text-on-background flex items-start gap-1.5">
                    <span className={`${layer.color} shrink-0 mt-0.5`} aria-hidden="true">›</span>
                    {item}
                  </li>
                ))}
              </ul>
            </motion.div>
          ))}
        </div>
      </section>

      {/* ── Smart Ranking ────────────────────────────────────── */}
      <section aria-labelledby="rank-title">
        <motion.div {...fadeUp()} className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <Cpu size={18} className="text-primary" aria-hidden="true" />
            <h2 id="rank-title" className="text-xl font-bold font-mono text-white">{t('feat_rank_title')}</h2>
          </div>
          <p className="text-on-background font-mono text-sm max-w-xl">{t('feat_rank_desc')}</p>
        </motion.div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
          {rankSteps.map((rs, i) => (
            <motion.div
              key={rs.step}
              {...fadeUp(0.07 * i)}
              className="border border-border bg-surface p-5 flex flex-col gap-3 relative overflow-hidden"
            >
              <span className="absolute top-3 right-4 font-mono text-5xl font-bold text-primary/5 select-none pointer-events-none leading-none" aria-hidden="true">
                {rs.step}
              </span>
              <div className="font-mono text-[10px] text-on-surface tracking-widest">STEP {rs.step}</div>
              <div className="font-mono font-bold text-sm text-white">{rs.title}</div>
              <p className="font-mono text-xs text-on-background leading-relaxed">{rs.desc}</p>
              {i < rankSteps.length - 1 && (
                <ChevronRight
                  size={14}
                  className="hidden sm:block absolute -right-2 top-1/2 -translate-y-1/2 text-border z-10"
                  aria-hidden="true"
                />
              )}
            </motion.div>
          ))}
        </div>

        {/* Decay formula */}
        <motion.div
          {...fadeUp(0.15)}
          className="border border-border/60 bg-background/50 p-5 font-mono text-center"
          aria-label="Time-decay scoring formula"
        >
          <div className="text-[10px] text-on-surface tracking-widest mb-2 uppercase">Scoring Formula</div>
          <div className="text-primary text-sm font-bold tracking-wider">
            score = frequency × e<sup className="text-cursor">(-λ × days_idle)</sup>
          </div>
          <div className="text-[10px] text-on-surface mt-2 opacity-70">λ configurable · persisted in DataStore · updated on each launch event</div>
        </motion.div>
      </section>

      {/* ── Tech Specs ───────────────────────────────────────── */}
      <section aria-labelledby="specs-title">
        <motion.div {...fadeUp()} className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <Database size={18} className="text-primary" aria-hidden="true" />
            <h2 id="specs-title" className="text-xl font-bold font-mono text-white">{t('feat_specs_title')}</h2>
          </div>
        </motion.div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3">
          {specs.map((s, i) => (
            <motion.div
              key={s.label}
              {...fadeUp(0.04 * i)}
              className="border border-border bg-surface p-4 flex flex-col gap-1 hover:border-primary/30 transition-colors"
            >
              <div className="font-mono text-[10px] text-on-surface tracking-widest uppercase">{s.label}</div>
              <div className="font-mono font-bold text-sm text-primary">{s.value}</div>
              {s.note && <div className="font-mono text-[10px] text-on-surface/70">{s.note}</div>}
            </motion.div>
          ))}
        </div>
      </section>

      {/* ── CTA ──────────────────────────────────────────────── */}
      <motion.div
        {...fadeUp()}
        className="border border-border bg-surface p-8 flex flex-col sm:flex-row items-center justify-between gap-6"
      >
        <div className="flex flex-col gap-2">
          <div className="font-mono text-[10px] text-on-surface tracking-widest uppercase flex items-center gap-2">
            <Shield size={11} className="text-primary" aria-hidden="true" /> Open Source · MIT License · No tracking
          </div>
          <div className="font-mono font-bold text-white text-lg">Ready to try Tensor?</div>
          <p className="font-mono text-xs text-on-background">Latest signed APK distributed via GitHub Releases — no Play Store required.</p>
        </div>
        <div className="flex items-center gap-3 shrink-0 flex-wrap justify-center sm:justify-start">
          <a
            href="https://github.com/JOSETRA44/launchers"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 border border-border text-on-background hover:text-primary hover:border-primary/40 font-mono text-xs px-4 py-3 transition-colors min-h-[44px]"
          >
            <ExternalLink size={13} aria-hidden="true" /> GitHub
          </a>
          <a
            href="https://github.com/JOSETRA44/launchers/releases/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 bg-primary text-black font-mono font-bold text-xs px-5 py-3 hover:bg-cursor transition-colors min-h-[44px]"
          >
            <ArrowRight size={13} aria-hidden="true" /> Download APK
          </a>
        </div>
      </motion.div>

    </div>
  );
};

export default Features;
