import React, { createContext, useContext, useState, useEffect } from 'react';

type Theme = 'hacker-dark' | 'hacker-cyan' | 'matrix-green';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
  const [theme, setThemeState] = useState<Theme>(() => {
    return (localStorage.getItem('tensor-theme') as Theme) || 'hacker-dark';
  });

  const setTheme = (newTheme: Theme) => {
    setThemeState(newTheme);
    localStorage.setItem('tensor-theme', newTheme);
  };

  useEffect(() => {
    document.body.classList.remove('theme-hacker-dark', 'theme-hacker-cyan', 'theme-matrix-green');
    if (theme !== 'hacker-dark') {
      document.body.classList.add(`theme-${theme}`);
    }
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) throw new Error('useTheme must be used within ThemeProvider');
  return context;
};
