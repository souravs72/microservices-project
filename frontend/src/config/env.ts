// Environment configuration
export const config = {
  // API Configuration
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || "",

  // Development Configuration
  NODE_ENV: import.meta.env.VITE_NODE_ENV || "development",

  // Feature Flags
  ENABLE_DEBUG: import.meta.env.VITE_ENABLE_DEBUG === "true",
  ENABLE_ANALYTICS: import.meta.env.VITE_ENABLE_ANALYTICS === "true",

  // External Services
  GOOGLE_ANALYTICS_ID: import.meta.env.VITE_GOOGLE_ANALYTICS_ID || "",
  SENTRY_DSN: import.meta.env.VITE_SENTRY_DSN || "",
};

export default config;
