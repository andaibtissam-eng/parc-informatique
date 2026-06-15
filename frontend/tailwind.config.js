/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#172033",
        muted: "#667085",
        line: "#e7eaf0",
        canvas: "#eef4fb",
        skywash: "#e8f2ff",
        lilacwash: "#f2ecff",
        mintwash: "#eaf8f1",
        peachwash: "#fff2e8",
        brand: {
          50: "#eef4ff",
          100: "#dce8ff",
          500: "#2f6fed",
          600: "#2458d6",
          700: "#1f47ad"
        },
        violet: {
          50: "#f5f1ff",
          100: "#ebe4ff",
          500: "#7c5cff",
          600: "#6845e8"
        },
        success: "#16a34a",
        warning: "#d97706",
        danger: "#dc2626"
      },
      fontFamily: {
        sans: ["Inter", "Manrope", "sans-serif"],
        display: ["Manrope", "sans-serif"]
      },
      boxShadow: {
        soft: "0 12px 30px rgba(23, 32, 51, 0.08)",
        pop: "0 18px 45px rgba(47, 111, 237, 0.16)"
      },
      borderRadius: {
        xl: "1rem",
        "2xl": "1.25rem"
      }
    }
  },
  plugins: []
};
