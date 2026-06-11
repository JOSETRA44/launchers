import { Link, useLocation } from 'react-router-dom';
import { Terminal, Star, Clock, Menu } from 'lucide-react';

const Navbar = () => {
  const location = useLocation();

  const navItems = [
    { name: 'Home', path: '/', icon: <Terminal size={18} /> },
    { name: 'Reviews', path: '/resenas', icon: <Star size={18} /> },
    { name: 'Changelog', path: '/version', icon: <Clock size={18} /> },
  ];

  return (
    <nav className="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-border">
      <div className="container mx-auto px-4 max-w-7xl flex items-center justify-between h-14">
        
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 text-primary font-bold font-mono text-lg">
          <div className="w-6 h-6 bg-primary-dim flex items-center justify-center rounded text-primary">
            <span className="leading-none text-sm">&gt;_</span>
          </div>
          TENSOR
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-6 font-mono text-sm">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-2 transition-colors ${
                  isActive ? 'text-primary' : 'text-on-background hover:text-primary'
                }`}
              >
                {item.icon}
                {item.name}
              </Link>
            );
          })}
        </div>

        {/* Mobile Nav Toggle */}
        <div className="md:hidden text-primary">
          <Menu />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
