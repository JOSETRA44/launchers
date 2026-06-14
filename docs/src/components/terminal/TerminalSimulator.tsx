import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '../ThemeProvider';

interface TermLine {
  id: string;
  text: string;
  isHtml?: boolean;
  className?: string;
}

export const TerminalSimulator = () => {
  const [lines, setLines] = useState<TermLine[]>([
    { id: '1', text: 'Tensor CLI Core [Version 1.0.4]' },
    { id: '2', text: 'System online. Ready to execute directives.' },
    { id: '3', text: 'Type /help or click below for available options.' },
  ]);
  const [input, setInput] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const terminalBodyRef = useRef<HTMLDivElement>(null);
  const { setTheme } = useTheme();

  useEffect(() => {
    if (terminalBodyRef.current) {
      // FIX: Scroll only the terminal's internal container, not the whole window
      terminalBodyRef.current.scrollTop = terminalBodyRef.current.scrollHeight;
    }
  }, [lines]);

  const addLine = (text: string, isHtml = false, className = '') => {
    setLines(prev => [...prev, { id: Math.random().toString(), text, isHtml, className }]);
  };

  const handleCommand = async (cmd: string) => {
    const trimmed = cmd.trim();
    if (!trimmed) return;

    addLine(`<span class="text-prompt">guest@tensor:~$</span> ${trimmed}`, true);
    
    // FIX: Handle multiple spaces seamlessly and auto-prefix missing slashes
    const parts = trimmed.split(/\s+/);
    let rawCmd = parts[0].toLowerCase();
    
    if (!rawCmd.startsWith('/') && ['theme', 'sec', 'stats', 'genpass', 'help', 'clean', 'cls', '?', 'scan', 'portscan', 'wifi', 'ssl', 'kb'].includes(rawCmd)) {
      rawCmd = '/' + rawCmd;
    }
    
    const mainCmd = rawCmd;
    const args = parts.slice(1);

    switch (mainCmd) {
      case 'help':
      case '/help':
      case '?':
        addLine('Available Terminal Directives:');
        addLine('  /theme <dark|cyan|matrix>  : Live style theme toggle');
        addLine('  /sec                       : Audits browser environment status');
        addLine('  /scan                      : TCP port scan simulation');
        addLine('  /wifi                      : WiFi recon simulation');
        addLine('  /ssl                       : TLS chain audit simulation');
        addLine('  /stats                     : Pulls screen time insight metrics');
        addLine('  /genpass <length>          : Builds random character passwords');
        addLine('  /kb                        : Terminal keyboard info');
        addLine('  /clean, cls, clear         : Wipes terminal log rows');
        break;
      case '/theme':
        if (args[0] && ['dark', 'cyan', 'matrix'].includes(args[0].toLowerCase())) {
          const mapped = args[0] === 'dark' ? 'hacker-dark' : args[0] === 'cyan' ? 'hacker-cyan' : 'matrix-green';
          setTheme(mapped as any);
          addLine(`Theme shifted successfully to [${args[0].toUpperCase()}]`, false, 'text-cursor font-bold drop-shadow-[0_0_8px_var(--cursor)]');
        } else {
          addLine('Error: Invalid theme choice. Use: /theme <dark | cyan | matrix>', false, 'text-error');
        }
        break;
      case '/sec':
        addLine('Auditing Environment Defenses...', false, 'text-primary font-bold');
        setTimeout(() => {
          addLine(`  SECURE CONTEXT   : ${window.isSecureContext ? 'PASS' : 'WARN'}`);
          addLine(`  SCREEN RESOLUTION: ${window.screen.width}x${window.screen.height}`);
          addLine('Device audit verification complete.', false, 'text-primary font-bold');
        }, 300);
        break;
      case '/stats':
        addLine('Gathering Screen Time Insights (mock):', false, 'text-primary font-bold');
        setTimeout(() => {
          addLine('  WhatsApp   [████████░░] 6.2h');
          addLine('  GitHub     [██████░░░░] 4.5h');
          addLine('  Termux     [████░░░░░░] 3.1h');
        }, 200);
        break;
      case '/genpass':
        let len = parseInt(args[0]) || 16;
        if (len < 8 || len > 64) len = 16;
        const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*()_+';
        const pass = Array.from({ length: len }, () => chars.charAt(Math.floor(Math.random() * chars.length))).join('');
        addLine('Generated Secure Token:');
        addLine(pass, false, 'text-cursor font-bold tracking-wider break-all');
        break;
      case '/scan':
      case '/portscan': {
        addLine('Initializing PORT SCAN module...', false, 'text-cursor font-bold');
        const ports = [
          { port: 22, svc: 'SSH',    open: false },
          { port: 80, svc: 'HTTP',   open: true  },
          { port: 443, svc: 'HTTPS', open: true  },
          { port: 3306, svc: 'MYSQL', open: false },
          { port: 8080, svc: 'HTTP-X', open: false },
        ];
        let delay = 200;
        ports.forEach(({ port, svc, open }) => {
          setTimeout(() => {
            addLine(
              `  ${port}/${svc.padEnd(8)} ${open ? '<span class="text-yellow-400">OPEN</span>' : '<span class="text-primary/50">CLOSED</span>'}`,
              true
            );
          }, delay);
          delay += 120;
        });
        setTimeout(() => {
          addLine('LOCALHOST · 2 OPEN · 3 CLOSED', false, 'text-cursor font-bold');
        }, delay + 100);
        break;
      }
      case '/wifi': {
        addLine('Initializing WIFI RECON module...', false, 'text-cursor font-bold');
        setTimeout(() => {
          addLine('  SIGNAL       ████░  GOOD · -62 dBm');
          addLine('  LINK SPEED   240 Mbps');
          addLine('  BAND         5 GHz');
          addLine('  NETWORKS     12 visible · 0 OPEN · 0 WEP · 3 WPA3', false, 'text-primary');
        }, 300);
        break;
      }
      case '/ssl': {
        addLine('Initializing SSL INSPECTOR module...', false, 'text-cursor font-bold');
        const domains = [
          { host: 'google.com',    days: 72,  ok: true  },
          { host: 'cloudflare.com', days: 120, ok: true  },
          { host: 'github.com',    days: 45,  ok: true  },
        ];
        let sslDelay = 300;
        domains.forEach(({ host, days, ok }) => {
          setTimeout(() => {
            addLine(
              `  ${host.padEnd(16)} <span class="${ok ? 'text-primary' : 'text-error'}">${ok ? `valid ${days}d · TLSv1.3` : 'EXPIRED'}</span>`,
              true
            );
          }, sslDelay);
          sslDelay += 180;
        });
        setTimeout(() => {
          addLine('3 DOMAINS · 0 WARNINGS', false, 'text-cursor font-bold');
        }, sslDelay + 100);
        break;
      }
      case '/kb':
        addLine('TERMINAL KEYBOARD — TensorKeyboard v1.0', false, 'text-cursor font-bold');
        addLine('  Rows: command chips · digits · QWERTY · alpha · symbols');
        addLine('  Features: shift toggle · quick /commands · dismiss [×]');
        addLine('  Trigger: tap [⌨] in nav bar · back gesture dismisses');
        addLine('  Style: adapts to active theme (Dark / Cyan / Matrix)');
        break;
      case '/clean':
      case 'cls':
        setLines([]);
        break;
      default:
        addLine(`Command not found: ${mainCmd}. Type /help for assistance.`, false, 'text-error');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleCommand(input);
      setInput('');
    }
  };

  const chips = ['/help', '/scan', '/wifi', '/ssl', '/sec', '/stats', '/theme cyan', '/theme dark', '/theme matrix', '/genpass 24', '/kb'];

  return (
    <div className="flex flex-col gap-4 w-full">
      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
        className="bg-surface border border-border rounded-lg shadow-[0_8px_32px_rgba(0,0,0,0.8)] overflow-hidden font-mono text-sm h-[350px] flex flex-col relative cursor-text group"
        onClick={() => inputRef.current?.focus()}
      >
        {/* Terminal Header */}
        <div className="bg-surface-variant px-4 py-2 flex items-center justify-between border-b border-border">
          <div className="flex gap-2">
            <div className="w-3 h-3 rounded-full bg-red-500 shadow-sm"></div>
            <div className="w-3 h-3 rounded-full bg-yellow-500 shadow-sm"></div>
            <div className="w-3 h-3 rounded-full bg-green-500 shadow-sm"></div>
          </div>
          <div className="text-on-surface text-xs font-bold">guest@tensor:~</div>
          <div className="w-12"></div>
        </div>

        {/* Terminal Body */}
        <div ref={terminalBodyRef} className="p-4 flex flex-col gap-1 flex-grow overflow-y-auto scroll-smooth">
          <AnimatePresence>
            {lines.map((line) => {
              const motionProps = {
                initial: { opacity: 0, x: -10, filter: 'blur(4px)' },
                animate: { opacity: 1, x: 0, filter: 'blur(0px)' },
                transition: { duration: 0.3, ease: 'easeOut' as const },
                className: `break-words ${line.className || 'text-primary'}`,
              };
              
              if (line.isHtml) {
                return <motion.div key={line.id} {...motionProps} dangerouslySetInnerHTML={{ __html: line.text }} />;
              }
              return <motion.div key={line.id} {...motionProps}>{line.text}</motion.div>;
            })}
          </AnimatePresence>
          
          <div className="flex items-center gap-2 mt-2">
            <span className="text-prompt drop-shadow-[0_0_5px_var(--prompt)]">guest@tensor:~$</span>
            <div className="relative flex-grow flex items-center">
              <input
                ref={inputRef}
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                className="bg-transparent border-none outline-none text-primary w-full flex-grow caret-transparent absolute inset-0 z-10"
                spellCheck="false"
                autoComplete="off"
              />
              <span className="text-primary invisible whitespace-pre">{input}</span>
              <motion.span
                animate={{ opacity: [1, 0, 1] }}
                transition={{ repeat: Infinity, duration: 0.8 }}
                className="w-2 h-[15px] bg-cursor inline-block ml-1 shadow-[0_0_8px_var(--cursor)]"
              ></motion.span>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Quick Chips */}
      <div className="flex flex-wrap gap-2">
        {chips.map(cmd => (
          <motion.button
            whileHover={{ scale: 1.05, y: -2 }}
            whileTap={{ scale: 0.95 }}
            key={cmd}
            onClick={() => handleCommand(cmd)}
            className="bg-surface border border-border text-on-background px-3 py-1 rounded-full text-xs font-mono hover:border-primary hover:text-primary hover:bg-primary/10 transition-colors shadow-sm"
          >
            {cmd}
          </motion.button>
        ))}
      </div>
    </div>
  );
};
