import { useEffect, useRef } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Navbar from './Navbar';
import Footer from './Footer';
import { useTheme } from '../ThemeProvider';

const MatrixBackground = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const { theme } = useTheme();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = canvas.width = window.innerWidth;
    let height = canvas.height = window.innerHeight;
    const columns = Math.floor(width / 20) + 1;
    const yPositions = Array(columns).fill(0);

    // FIX: Match color synchronously without DOM query
    const getPrimaryColor = () => {
      switch (theme) {
        case 'hacker-cyan': return '#00e5ff';
        case 'matrix-green': return '#20c20e';
        case 'hacker-dark':
        default: return '#00ff41';
      }
    };
    const primaryColor = getPrimaryColor();

    const draw = () => {
      // FIX: Increase matrix trail length by reducing alpha clear
      ctx.fillStyle = 'rgba(0, 0, 0, 0.03)';
      ctx.fillRect(0, 0, width, height);
      
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
  }, [theme]); // Re-run effect exclusively when theme updates

  return <canvas ref={canvasRef} className="fixed top-0 left-0 w-full h-full -z-10 opacity-20" />;
};

const Layout = () => {
  const location = useLocation();
  const { theme } = useTheme();

  return (
    <div className="min-h-screen flex flex-col relative">
      <MatrixBackground />
      {/* Pro Max CRT Flash effect on theme change */}
      <AnimatePresence mode="wait">
        <motion.div
          key={theme}
          initial={{ opacity: 0.15, scale: 1.02 }}
          animate={{ opacity: 0, scale: 1 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
          className="fixed inset-0 bg-primary z-50 pointer-events-none mix-blend-overlay"
        />
      </AnimatePresence>
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
      <Footer />
    </div>
  );
};

export default Layout;
