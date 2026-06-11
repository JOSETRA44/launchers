import { useEffect, useRef } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Navbar from './Navbar';

const MatrixBackground = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = canvas.width = window.innerWidth;
    let height = canvas.height = window.innerHeight;
    const columns = Math.floor(width / 20) + 1;
    const yPositions = Array(columns).fill(0);

    const draw = () => {
      ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
      ctx.fillRect(0, 0, width, height);
      
      const primaryColor = getComputedStyle(document.body).getPropertyValue('--primary').trim() || '#00ff41';
      ctx.fillStyle = primaryColor;
      ctx.font = '15px monospace';

      yPositions.forEach((y, index) => {
        const text = String.fromCharCode(33 + Math.random() * 93);
        const x = index * 20;
        ctx.fillText(text, x, y);

        if (y > 100 + Math.random() * 10000) {
          yPositions[index] = 0;
        } else {
          yPositions[index] = y + 20;
        }
      });
    };

    const interval = setInterval(draw, 50);

    const handleResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', handleResize);

    return () => {
      clearInterval(interval);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return <canvas ref={canvasRef} className="fixed top-0 left-0 w-full h-full -z-10 opacity-5" />;
};

const Layout = () => {
  const location = useLocation();

  return (
    <div className="min-h-screen flex flex-col relative">
      <MatrixBackground />
      <Navbar />
      <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8 max-w-7xl">
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.3 }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>
      <footer className="py-8 border-t border-border mt-16 text-center text-on-surface text-sm font-mono">
        <p>Tensor Launcher is open-source. Licensed under Apache 2.0.</p>
        <p className="opacity-50 text-[10px] mt-2">SYSTEM DIRECTIVE: guest@tensor:~$ /shutdown</p>
      </footer>
    </div>
  );
};

export default Layout;
