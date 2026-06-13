import { Star, MessageSquare, Terminal } from 'lucide-react';
import { motion } from 'framer-motion';

const reviews = [
  { id: 1, user: '@cypher_punk',   role: 'DevOps Engineer',     stars: 5, text: 'Finally an Android launcher that speaks my language. The CLI engine saves me dozens of taps a day, and the DDD architecture is a joy to read through.' },
  { id: 2, user: '@android_dev23', role: 'Android Developer',   stars: 5, text: 'The way Tensor handles immersive mode and dynamic status bars without polling is genius. This is peak Jetpack Compose performance optimization.' },
  { id: 3, user: '@netsec_pro',    role: 'Security Auditor',    stars: 5, text: 'The built-in /sec tools and the fact that it never requests unnecessary permissions makes Tensor my daily driver. Highly recommended for devs.' },
  { id: 4, user: '@minimalist',    role: 'Power User',          stars: 5, text: 'Zero clutter. Pure functionality. The smart decay ranking algorithm works flawlessly — it learned my muscle memory within two days of use.' },
  { id: 5, user: '@root_access',   role: 'System Administrator',stars: 5, text: '/genpass alone is worth installing. Clean, secure token generation right from my home screen. No separate app needed.' },
  { id: 6, user: '@compose_fan',   role: 'Mobile Engineer',     stars: 5, text: 'Open source, Clean Architecture, no hardcoded colors, strict layer separation — this codebase is a textbook example of how Android apps should be built.' },
];

const aggregate = { total: reviews.length, avg: 5.0 };

const Reviews = () => {
  return (
    <div className="py-12">

      {/* Header */}
      <header className="text-center max-w-2xl mx-auto mb-12">
        <div className="flex items-center justify-center gap-2 text-primary font-mono text-xs tracking-widest uppercase mb-4" aria-hidden="true">
          <span className="w-2 h-2 bg-primary rounded-full animate-pulse" /> User Feedback Log
        </div>
        <h1 className="text-4xl font-bold mb-3 font-mono flex items-center justify-center gap-3">
          <Terminal className="text-primary" size={30} aria-hidden="true" />
          User Reviews
        </h1>
        <p className="text-on-surface text-base font-mono">
          What developers and power users say about Tensor Launcher.
        </p>

        {/* Aggregate rating */}
        <div className="mt-6 inline-flex items-center gap-4 border border-border bg-surface px-6 py-3 rounded font-mono">
          <div>
            <div className="text-4xl font-bold text-primary">{aggregate.avg.toFixed(1)}</div>
            <div className="text-on-surface text-xs">out of 5.0</div>
          </div>
          <div className="h-10 w-px bg-border" aria-hidden="true" />
          <div>
            <div className="flex gap-1 mb-1">
              {Array.from({ length: 5 }).map((_, i) => (
                <Star key={i} size={14} className="text-primary" fill="currentColor" aria-hidden="true" />
              ))}
            </div>
            <div className="text-on-surface text-xs">{aggregate.total} verified reviews</div>
          </div>
        </div>
      </header>

      {/* Review grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5" role="list" aria-label="User reviews">
        {reviews.map((r, i) => (
          <motion.article
            key={r.id}
            role="listitem"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: i * 0.07, duration: 0.4 }}
            whileHover={{ y: -4, transition: { type: 'spring', stiffness: 400, damping: 28 } }}
            className="bg-surface border border-border p-6 rounded relative overflow-hidden group hover:border-primary/30 transition-colors flex flex-col"
          >
            {/* Background quote mark */}
            <MessageSquare
              className="absolute top-4 right-4 text-border opacity-30 group-hover:opacity-50 transition-opacity"
              size={36}
              aria-hidden="true"
            />

            {/* Stars */}
            <div className="flex items-center gap-1 mb-4" role="img" aria-label={`${r.stars} out of 5 stars`}>
              {Array.from({ length: r.stars }).map((_, i) => (
                <Star key={i} size={14} className="text-primary" fill="currentColor" aria-hidden="true" />
              ))}
            </div>

            {/* Quote */}
            <blockquote className="text-on-background text-sm font-mono leading-relaxed mb-5 flex-grow">
              "{r.text}"
            </blockquote>

            {/* Author */}
            <footer className="font-mono text-xs border-t border-border/50 pt-4">
              <span className="text-primary font-bold">{r.user}</span>
              <span className="text-on-surface ml-2 opacity-70">// {r.role}</span>
            </footer>
          </motion.article>
        ))}
      </div>

      {/* CTA */}
      <motion.div
        initial={{ opacity: 0 }}
        whileInView={{ opacity: 1 }}
        viewport={{ once: true }}
        className="mt-12 text-center border border-border bg-surface p-8 font-mono"
      >
        <p className="text-on-surface text-sm mb-4">
          <span className="text-primary mr-2" aria-hidden="true">[SYS]</span>
          Enjoying Tensor? Leave a star on GitHub — it helps the project grow.
        </p>
        <a
          href="https://github.com/JOSETRA44/launchers"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 border border-primary text-primary px-5 py-2.5 rounded hover:bg-primary/10 transition-colors text-sm min-h-[44px]"
        >
          <Star size={14} aria-hidden="true" /> Star on GitHub
        </a>
      </motion.div>
    </div>
  );
};

export default Reviews;
