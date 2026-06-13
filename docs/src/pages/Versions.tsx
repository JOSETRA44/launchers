import { useState, useMemo, type ReactNode } from 'react';
import { Download, GitCommit, Plus, Wrench, TrendingUp, Minus, Shield, Zap, Filter, ExternalLink } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { releases, releaseStats, type ReleaseEntry, type ReleaseType, type ChangeType } from '../data/releases';

/* ── Domain → presentation mappings ────────────────────────── */

const releaseTypeMeta: Record<ReleaseType, { label: string; borderCls: string; textCls: string; bgCls: string; nodeCls: string }> = {
  feature: { label: 'FEATURE',  borderCls: 'border-primary', textCls: 'text-primary', bgCls: 'bg-primary/15',   nodeCls: 'bg-primary'        },
  fix:     { label: 'FIX',      borderCls: 'border-cursor',  textCls: 'text-cursor',  bgCls: 'bg-cursor/15',    nodeCls: 'bg-cursor'         },
  release: { label: 'RELEASE',  borderCls: 'border-border',  textCls: 'text-white',   bgCls: 'bg-surface',      nodeCls: 'bg-on-surface'     },
  hotfix:  { label: 'HOTFIX',   borderCls: 'border-error',   textCls: 'text-error',   bgCls: 'bg-error/15',     nodeCls: 'bg-error'          },
};

const changeMeta: Record<ChangeType, { icon: ReactNode; textCls: string; prefix: string }> = {
  add:      { icon: <Plus size={12} />,        textCls: 'text-primary', prefix: '+'  },
  fix:      { icon: <Wrench size={12} />,      textCls: 'text-cursor',  prefix: '~'  },
  improve:  { icon: <TrendingUp size={12} />,  textCls: 'text-prompt',  prefix: '↑'  },
  remove:   { icon: <Minus size={12} />,       textCls: 'text-error',   prefix: '−'  },
  security: { icon: <Shield size={12} />,      textCls: 'text-primary', prefix: '⚑'  },
  perf:     { icon: <Zap size={12} />,         textCls: 'text-cursor',  prefix: '⚡' },
};

/* ── Version card ───────────────────────────────────────────── */

const VersionCard = ({ entry, index }: { entry: ReleaseEntry; index: number }) => {
  const meta = releaseTypeMeta[entry.releaseType];
  const [expanded, setExpanded] = useState(true);
  const visibleChanges = expanded ? entry.changes : entry.changes.slice(0, 3);
  const hasMore = entry.changes.length > 3;

  return (
    <motion.div
      layout
      initial={{ opacity: 0, x: -24 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 24, transition: { duration: 0.18 } }}
      transition={{ delay: index * 0.08, duration: 0.35, ease: [0.25, 0.1, 0.25, 1] }}
      className="flex gap-4 sm:gap-6 relative"
    >
      {/* Timeline node */}
      <div className="flex flex-col items-center shrink-0 pt-1" aria-hidden="true">
        <div className={`w-4 h-4 rounded-full ${meta.nodeCls} border-2 border-background shadow-[0_0_12px_currentColor] z-10 shrink-0`} />
        <div className="w-px flex-grow bg-gradient-to-b from-border to-transparent mt-2" />
      </div>

      {/* Card */}
      <motion.article
        whileHover={{ rotateX: -2, rotateY: 3, scale: 1.008, transition: { type: 'spring', stiffness: 380, damping: 28 } }}
        style={{ transformPerspective: 1000, willChange: 'transform' }}
        className={`flex-grow border-l-4 ${meta.borderCls} bg-surface border border-border/50 hover:border-primary/25 transition-colors mb-8 overflow-hidden`}
        aria-label={`Version ${entry.version}: ${entry.title}`}
      >
        {/* Card header */}
        <header className="p-5 flex flex-wrap items-start justify-between gap-3 border-b border-border/40">
          <div className="flex flex-wrap items-center gap-2 font-mono">
            {/* Version number */}
            <span className="text-2xl font-bold text-white tracking-tight">{entry.version}</span>

            {/* LATEST badge */}
            {entry.isLatest && (
              <span className="bg-primary text-black text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-widest animate-glow-pulse">
                LATEST
              </span>
            )}

            {/* Release type badge */}
            <span className={`${meta.bgCls} ${meta.textCls} ${meta.borderCls} border text-[10px] font-bold px-2 py-0.5 rounded font-mono uppercase tracking-wider`}>
              {meta.label}
            </span>

            {/* Commit hash */}
            {entry.commitHash && (
              <a
                href={`https://github.com/JOSETRA44/launchers/commit/${entry.commitHash}`}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-on-surface hover:text-primary transition-colors text-[11px]"
                aria-label={`View commit ${entry.commitHash}`}
              >
                <GitCommit size={11} aria-hidden="true" />
                {entry.commitHash}
              </a>
            )}
          </div>

          {/* Date + Download */}
          <div className="flex items-center gap-3">
            <time dateTime={entry.date} className="font-mono text-on-surface text-xs">{entry.date}</time>
            <a
              href={entry.downloadUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1.5 bg-primary/15 border border-primary/30 text-primary hover:bg-primary/25 transition-colors text-xs font-mono px-3 py-1.5 rounded min-h-[36px]"
              aria-label={`Download ${entry.version}`}
            >
              <Download size={12} aria-hidden="true" /> APK
            </a>
          </div>
        </header>

        {/* Title + description */}
        <div className="px-5 pt-4 pb-3">
          <h2 className="text-base font-bold text-white font-mono mb-1.5">{entry.title}</h2>
          <p className="text-on-background text-sm leading-relaxed max-w-2xl">{entry.description}</p>
        </div>

        {/* Change list */}
        <ul className="px-5 pb-4 flex flex-col gap-1.5" role="list" aria-label={`Changes in ${entry.version}`}>
          <AnimatePresence initial={false}>
            {visibleChanges.map((change) => {
              const cm = changeMeta[change.type];
              return (
                <motion.li
                  key={change.id}
                  layout
                  initial={{ opacity: 0, y: -4 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -4, transition: { duration: 0.12 } }}
                  transition={{ duration: 0.18 }}
                  className="flex items-start gap-2 font-mono text-xs"
                >
                  <span className={`${cm.textCls} shrink-0 mt-0.5 flex items-center gap-1`} aria-hidden="true">
                    {cm.icon} <span className="font-bold">{cm.prefix}</span>
                  </span>
                  <span className="text-on-background leading-relaxed">{change.text}</span>
                </motion.li>
              );
            })}
          </AnimatePresence>
        </ul>

        {/* Expand toggle */}
        {hasMore && (
          <button
            onClick={() => setExpanded(v => !v)}
            className="w-full px-5 pb-4 text-left font-mono text-[11px] text-on-surface hover:text-primary transition-colors flex items-center gap-1.5 min-h-[36px]"
            aria-expanded={expanded}
          >
            <span aria-hidden="true">{expanded ? '▲' : '▼'}</span>
            {expanded
              ? `Collapse changes`
              : `Show ${entry.changes.length - 3} more change${entry.changes.length - 3 > 1 ? 's' : ''}…`}
          </button>
        )}

        {/* GitHub full release link */}
        <div className="px-5 pb-5 border-t border-border/20 pt-3 flex justify-end">
          <a
            href={entry.downloadUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-on-surface hover:text-primary transition-colors font-mono text-[11px]"
          >
            <ExternalLink size={11} aria-hidden="true" /> Full release notes on GitHub
          </a>
        </div>
      </motion.article>
    </motion.div>
  );
};

/* ── Filter pill ────────────────────────────────────────────── */

const FilterPill = ({
  label, count, active, onClick,
}: { label: string; count: number; active: boolean; onClick: () => void }) => (
  <motion.button
    whileHover={{ scale: 1.04 }}
    whileTap={{ scale: 0.96 }}
    onClick={onClick}
    aria-pressed={active}
    className={`flex items-center gap-2 font-mono text-xs px-4 py-2 rounded border transition-all duration-200 min-h-[40px] ${
      active
        ? 'bg-primary text-black border-primary font-bold'
        : 'bg-surface border-border text-on-background hover:border-primary/40 hover:text-primary'
    }`}
  >
    {label}
    <span className={`text-[10px] px-1.5 py-0.5 rounded ${active ? 'bg-black/20 text-black' : 'bg-surface-variant text-on-surface'}`}>
      {count}
    </span>
  </motion.button>
);

/* ── Versions page ──────────────────────────────────────────── */

type Filter = 'all' | ReleaseType;

const Versions = () => {
  const [filter, setFilter] = useState<Filter>('all');

  const filtered = useMemo(
    () => filter === 'all' ? releases : releases.filter(r => r.releaseType === filter),
    [filter],
  );

  const filterOptions: Array<{ key: Filter; label: string }> = [
    { key: 'all',     label: 'ALL'     },
    { key: 'feature', label: 'FEATURE' },
    { key: 'fix',     label: 'FIX'     },
    { key: 'release', label: 'RELEASE' },
  ];

  return (
    <div className="py-12 flex flex-col gap-10">

      {/* ── Page header ──────────────────────────────────────── */}
      <header className="flex flex-col gap-4 max-w-2xl">
        <div className="font-mono text-[10px] text-on-surface tracking-[0.3em] uppercase flex items-center gap-2">
          <span className="text-primary animate-pulse" aria-hidden="true">●</span> Changelog / Releases
        </div>
        <h1 className="text-4xl font-bold font-mono text-white">
          Version<span className="text-primary">_</span>History
        </h1>
        <p className="text-on-background text-sm font-mono leading-relaxed">
          All releases are distributed as signed APKs via GitHub. Each entry includes a commit reference, change log, and direct download link.
        </p>

        {/* Aggregate stats */}
        <div className="flex flex-wrap gap-3 mt-1" role="list" aria-label="Release statistics">
          {[
            { label: 'Total Releases',  value: releaseStats.total,        suffix: '' },
            { label: 'Changes tracked', value: releaseStats.totalChanges, suffix: '' },
            { label: 'Latest version',  value: releaseStats.latest.version, isText: true },
          ].map(s => (
            <div
              key={s.label}
              role="listitem"
              className="border border-border bg-surface px-4 py-2.5 font-mono text-center"
            >
              <div className="text-[10px] text-on-surface mb-0.5">{s.label}</div>
              <div className="text-primary font-bold text-base">
                {s.isText ? s.value : `${s.value}${s.suffix}`}
              </div>
            </div>
          ))}
        </div>
      </header>

      {/* ── Filter pills ─────────────────────────────────────── */}
      <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Filter releases by type">
        <span className="text-on-surface font-mono text-xs flex items-center gap-1.5 mr-1">
          <Filter size={12} aria-hidden="true" /> FILTER:
        </span>
        {filterOptions.map(({ key, label }) => (
          <FilterPill
            key={key}
            label={label}
            count={key === 'all' ? releases.length : releases.filter(r => r.releaseType === key).length}
            active={filter === key}
            onClick={() => setFilter(key)}
          />
        ))}
      </div>

      {/* ── Timeline ─────────────────────────────────────────── */}
      <section aria-label="Release timeline" aria-live="polite">
        <AnimatePresence mode="popLayout">
          {filtered.length === 0 ? (
            <motion.p
              key="empty"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="font-mono text-on-surface text-sm border border-border bg-surface p-6"
            >
              <span className="text-primary mr-2" aria-hidden="true">[SYS]</span>
              No releases match the selected filter.
            </motion.p>
          ) : (
            filtered.map((entry, i) => (
              <VersionCard key={entry.version} entry={entry} index={i} />
            ))
          )}
        </AnimatePresence>
      </section>

      {/* ── CTA ──────────────────────────────────────────────── */}
      <div className="border border-border bg-surface p-6 font-mono flex flex-col sm:flex-row items-center justify-between gap-4">
        <div>
          <div className="text-white font-bold text-sm mb-1">Stay up to date</div>
          <p className="text-on-surface text-xs">Watch the GitHub repository to get notified on every new release.</p>
        </div>
        <a
          href="https://github.com/JOSETRA44/launchers"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-2 bg-primary text-black font-bold text-xs px-5 py-3 rounded hover:bg-cursor transition-colors shrink-0 min-h-[44px]"
        >
          <ExternalLink size={14} aria-hidden="true" /> Watch on GitHub
        </a>
      </div>

    </div>
  );
};

export default Versions;
