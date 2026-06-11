import { GitCommit, GitPullRequest, GitBranch } from 'lucide-react';
import { motion } from 'framer-motion';

const changelog = [
  {
    version: "v1.0.4",
    date: "2026-06-11",
    type: "feature",
    icon: <GitPullRequest size={20} />,
    title: "Security Toolkit Integration",
    changes: [
      "Added /sec command to audit device state (Screen Lock, Root, ADB).",
      "Integrated SecureRandom for /genpass avoiding ambiguous characters.",
      "Optimized Matrix Rain deterministic rendering inside Canvas."
    ]
  },
  {
    version: "v1.0.3",
    date: "2026-05-20",
    type: "feature",
    icon: <GitBranch size={20} />,
    title: "Smart Ranking & Insights",
    changes: [
      "Implemented decay-weighted frequency scoring for apps.",
      "Added /stats command to pull screen time metrics.",
      "Added step counter baseline storage in DataStore."
    ]
  },
  {
    version: "v1.0.0",
    date: "2026-04-10",
    type: "release",
    icon: <GitCommit size={20} />,
    title: "Initial Open Source Release",
    changes: [
      "Core CLI engine utilizing fuzzy find matching.",
      "Hacker Dark, Cyan and Matrix Green themes.",
      "DDD Clean Architecture baseline established.",
      "Immersive Mode and Dynamic Status Bar integration."
    ]
  }
];

const Changelog = () => {
  return (
    <div className="py-12 max-w-3xl mx-auto">
      <div className="mb-12 border-b border-border pb-6">
        <h1 className="text-4xl font-bold font-mono text-white mb-2">Changelog</h1>
        <p className="text-on-surface">Track the evolution of Tensor Launcher.</p>
      </div>

      <div className="flex flex-col gap-10 relative">
        {/* Timeline line */}
        <div className="absolute left-[19px] top-4 bottom-0 w-[2px] bg-border"></div>

        {changelog.map((log, i) => (
          <motion.div 
            key={log.version}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.15 }}
            className="flex gap-6 relative"
          >
            {/* Timeline icon node */}
            <div className="w-10 h-10 rounded-full bg-surface border-2 border-primary flex items-center justify-center text-primary z-10 shrink-0 shadow-[0_0_10px_rgba(0,255,65,0.2)]">
              {log.icon}
            </div>

            {/* Content */}
            <div className="bg-surface border border-border p-6 rounded-lg flex-grow">
              <div className="flex items-center justify-between mb-4 border-b border-border pb-4">
                <div className="flex items-center gap-3">
                  <h2 className="text-xl font-bold text-white font-mono">{log.version}</h2>
                  <span className="text-xs font-mono bg-primary-dim text-primary px-2 py-1 rounded">{log.type.toUpperCase()}</span>
                </div>
                <div className="font-mono text-on-surface text-sm">{log.date}</div>
              </div>
              <h3 className="text-lg text-primary mb-3">{log.title}</h3>
              <ul className="flex flex-col gap-2">
                {log.changes.map((change, j) => (
                  <li key={j} className="flex gap-2 text-on-background">
                    <span className="text-primary opacity-50">&gt;</span> {change}
                  </li>
                ))}
              </ul>
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
};

export default Changelog;
