import { Terminal, Shield, Zap, Activity, FolderGit2 } from 'lucide-react';
import { motion } from 'framer-motion';

const Home = () => {
  return (
    <div className="py-12 flex flex-col gap-24">
      {/* Hero Section */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
        <div className="flex flex-col gap-6">
          <div className="font-mono text-primary text-sm tracking-widest flex items-center gap-2 uppercase">
            <span className="w-2 h-2 bg-primary animate-pulse rounded-full"></span>
            CLI Home-Screen Shell
          </div>
          <h1 className="text-5xl md:text-6xl font-bold leading-tight">
            Control Android <br /> Through <span className="text-primary drop-shadow-[0_0_10px_rgba(0,255,65,0.4)]">Tensor</span>
          </h1>
          <p className="text-on-background text-lg max-w-lg">
            Tensor is a terminal-driven home screen engineered for developers and power users. Built from the ground up using Jetpack Compose, Material3, and strict DDD Clean Architecture.
          </p>
          <div className="flex flex-wrap gap-4 mt-4">
            <button className="bg-primary text-black font-mono px-6 py-3 rounded font-semibold hover:bg-cursor transition-colors shadow-[0_4px_14px_rgba(0,255,65,0.15)] hover:shadow-[0_6px_20px_rgba(0,255,65,0.35)] flex items-center gap-2">
              <Terminal size={18} /> OPEN TERMINAL
            </button>
            <button className="border border-border text-primary font-mono px-6 py-3 rounded hover:bg-primary/10 transition-colors flex items-center gap-2">
              <FolderGit2 size={18} /> GITHUB
            </button>
          </div>
        </div>

        {/* Hero Terminal Simulator Mock */}
        <div className="bg-surface border border-border rounded-lg shadow-[0_8px_32px_rgba(0,0,0,0.8)] overflow-hidden font-mono text-sm h-[350px] flex flex-col relative">
          <div className="bg-surface-variant px-4 py-2 flex items-center justify-between border-b border-border">
            <div className="flex gap-2">
              <div className="w-3 h-3 rounded-full bg-red-500"></div>
              <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
              <div className="w-3 h-3 rounded-full bg-green-500"></div>
            </div>
            <div className="text-on-surface text-xs">guest@tensor:~</div>
            <div className="w-12"></div>
          </div>
          <div className="p-4 flex flex-col gap-2 text-primary flex-grow">
            <div>Tensor CLI Core [Version 1.0.4]</div>
            <div>System online. Ready to execute directives.</div>
            <div className="flex items-center gap-2 mt-2">
              <span className="text-prompt">guest@tensor:~$</span>
              <span className="animate-pulse">_</span>
            </div>
          </div>
        </div>
      </section>

      {/* Bento Grid Features */}
      <section className="flex flex-col gap-8">
        <div className="text-center max-w-2xl mx-auto mb-8">
          <h2 className="text-3xl font-bold mb-4">Engineered For Power Users</h2>
          <p className="text-on-surface">Designed with deep system integrations, efficient fuzzy matching engines, and defensive tools to audit your Android environment directly.</p>
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
    </div>
  );
};

export default Home;
