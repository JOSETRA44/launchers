/**
 * Domain model for release entries.
 * This is the data layer — presentation components import from here,
 * keeping the data contract separate from rendering concerns (DDD).
 */

export type ChangeType = 'add' | 'fix' | 'improve' | 'remove' | 'security' | 'perf';
export type ReleaseType = 'feature' | 'fix' | 'release' | 'hotfix';

export interface VersionChange {
  readonly id: number;
  readonly type: ChangeType;
  readonly text: string;
}

export interface ReleaseEntry {
  readonly version: string;
  readonly date: string;
  readonly releaseType: ReleaseType;
  readonly title: string;
  readonly description: string;
  readonly changes: readonly VersionChange[];
  readonly downloadUrl: string;
  readonly isLatest: boolean;
  readonly commitHash?: string;
}

export const releases: readonly ReleaseEntry[] = [
  {
    version: 'v1.0.4',
    date: '2026-06-11',
    releaseType: 'feature',
    isLatest: true,
    title: 'Security Toolkit Integration',
    description: 'Full passive security auditing suite integrated into the CLI engine. Device environment inspection without root or privileged APIs.',
    downloadUrl: 'https://github.com/JOSETRA44/launchers/releases/tag/v1.0.4',
    commitHash: '90ab12f',
    changes: [
      { id: 1, type: 'add',      text: '/sec command — audits ADB debug state, root binaries, and screen lock integrity.' },
      { id: 2, type: 'add',      text: '/genpass <length> — cryptographic token generator via SecureRandom, avoids ambiguous characters.' },
      { id: 3, type: 'improve',  text: 'Matrix Rain rendering optimized with deterministic Canvas draw cycle, reduced GC pressure.' },
      { id: 4, type: 'fix',      text: 'Wallpaper persistence across reboots via new dedicated WallpaperRepository.' },
      { id: 5, type: 'improve',  text: 'Margin handling extended to accept signed values for advanced layout control.' },
    ],
  },
  {
    version: 'v1.0.3',
    date: '2026-05-20',
    releaseType: 'feature',
    isLatest: false,
    title: 'Smart Ranking & Insights',
    description: 'Neural-like time-decay algorithm for app ranking. Frequently-launched apps float to the top; idle apps sink naturally over time.',
    downloadUrl: 'https://github.com/JOSETRA44/launchers/releases/tag/v1.0.3',
    commitHash: '3102101',
    changes: [
      { id: 1, type: 'add',      text: 'Decay-weighted frequency scoring for installed apps, persisted in DataStore.' },
      { id: 2, type: 'add',      text: '/stats command — pulls screen-time insight metrics from the ranking engine.' },
      { id: 3, type: 'add',      text: 'Step counter baseline storage for daily fitness tracking integration.' },
      { id: 4, type: 'improve',  text: 'AppListViewModel refactored — coroutine scope strictly bound to screen lifecycle.' },
      { id: 5, type: 'fix',      text: 'String resources fully extracted for Arsenal, Insights, and Security screens.' },
    ],
  },
  {
    version: 'v1.0.2',
    date: '2026-05-05',
    releaseType: 'fix',
    isLatest: false,
    title: 'Navigation & Architecture Hardening',
    description: 'Saveable back-stack navigation replaces NavController. Improved job lifecycle management and immersive mode fixes across screens.',
    downloadUrl: 'https://github.com/JOSETRA44/launchers/releases/tag/v1.0.2',
    commitHash: 'e3f9abc',
    changes: [
      { id: 1, type: 'improve',  text: 'Navigation refactored to AppDestination enum + saveable back stack in MainActivity.' },
      { id: 2, type: 'fix',      text: 'BackHandler enabled flag prevents ghost back presses in overlay screens.' },
      { id: 3, type: 'improve',  text: 'Job lifecycle improved in Arsenal — coroutines cancel correctly on screen exit.' },
      { id: 4, type: 'fix',      text: 'AppListScreen updated with stickyHeader experimental API opt-in.' },
    ],
  },
  {
    version: 'v1.0.0',
    date: '2026-04-10',
    releaseType: 'release',
    isLatest: false,
    title: 'Initial Open Source Release',
    description: 'First public release. Full DDD Clean Architecture baseline, three visual themes, and the core CLI engine built from scratch.',
    downloadUrl: 'https://github.com/JOSETRA44/launchers/releases/tag/v1.0.0',
    commitHash: '2779438',
    changes: [
      { id: 1, type: 'add',      text: 'Core CLI engine with fuzzy find matching for fast app launch.' },
      { id: 2, type: 'add',      text: 'Hacker Dark, Hacker Cyan, and Matrix Green theme palettes.' },
      { id: 3, type: 'add',      text: 'DDD Clean Architecture baseline — domain, data, and presentation layers.' },
      { id: 4, type: 'add',      text: 'Immersive Mode and Dynamic Status Bar integration.' },
      { id: 5, type: 'add',      text: 'Matrix digital rain Canvas animation with per-column independent timers.' },
      { id: 6, type: 'add',      text: 'Manual DI via AppModule object — zero Hilt dependency.' },
    ],
  },
] as const;

/** Aggregate stats derived from the domain data */
export const releaseStats = {
  total: releases.length,
  totalChanges: releases.reduce((acc, r) => acc + r.changes.length, 0),
  latest: releases.find(r => r.isLatest)!,
  byType: (type: ReleaseType) => releases.filter(r => r.releaseType === type),
} as const;
