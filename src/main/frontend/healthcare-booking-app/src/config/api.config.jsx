const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'https://entirely-dynamic-penguin.ngrok-free.app',
  ENDPOINTS: {
      LOGIN: '/api/v1/auth/login',
      REGISTER: '/api/v1/auth/register',
      DOCTORS: '/api/v1/doctor',
      APPOINTMENTS: '/api/v1/appointments',
    },
  };
  
  export default API_CONFIG;
  