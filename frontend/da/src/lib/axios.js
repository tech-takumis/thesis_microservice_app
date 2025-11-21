import Axios from 'axios';

const axios = Axios.create({
    baseURL: import.meta.env.VITE_PUBLIC_BACKEND_URL || 'http://localhost:9001',
    headers: {
        'Accept': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
    },
    withCredentials: true // Ensure credentials (cookies) are sent with every request
});

// Request interceptor
axios.interceptors.request.use(
    config => {
        // You can add auth token here if needed
        // const token = localStorage.getItem('token');
        // if (token) {
        //     config.headers.Authorization = `Bearer ${token}`;
        // }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// Response interceptor to handle authentication and service errors
axios.interceptors.response.use(
    response => response,
    error => {
        // Handle authentication errors (401 Unauthorized)
        if (error.response?.status === 401) {
            // Only redirect if this is NOT a login request
            // Login requests should handle their own 401 errors
            const isLoginRequest = error.config?.url?.includes('/auth/login');
            if (!isLoginRequest) {
                console.log('[Axios] 401 detected, redirecting to login');
                window.location.href = '/';
            } else {
                console.log('[Axios] 401 from login request, letting login handler deal with it');
            }
        }
        // Handle 403 Forbidden errors
        if (error.response?.status === 403) {
            console.error('Access Denied:', error.response?.data?.message || 'You do not have permission to access this resource');
        }
        // Handle 503 Service Unavailable errors
        if (error.response?.status === 503) {
            console.error('[Axios] 503 Service Unavailable - Backend service is not running');

            // Show toast notification
            showServiceUnavailableToast();

            // Redirect to dashboard after a brief delay
            setTimeout(() => {
                // Get the current path to determine the appropriate dashboard
                const currentPath = window.location.pathname;
                if (currentPath.includes('/admin')) {
                    window.location.href = '/admin/dashboard';
                } else if (currentPath.includes('/agriculturist')) {
                    window.location.href = '/agriculturist/dashboard';
                } else if (currentPath.includes('/worker')) {
                    window.location.href = '/worker/dashboard';
                } else {
                    window.location.href = '/';
                }
            }, 2000);
        }
        return Promise.reject(error);
    }
);

// Helper function to show service unavailable toast
function showServiceUnavailableToast() {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = 'fixed top-4 right-4 bg-red-500 text-white px-6 py-4 rounded-lg shadow-lg z-[9999] flex items-center gap-3 animate-slide-in';
    toast.innerHTML = `
        <svg class="w-6 h-6 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
        </svg>
        <div>
            <p class="font-semibold">Service Unavailable</p>
            <p class="text-sm">The backend service is not running. Redirecting to dashboard...</p>
        </div>
    `;

    // Add animation styles
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slide-in {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        .animate-slide-in {
            animation: slide-in 0.3s ease-out;
        }
    `;

    // Append to document
    if (!document.querySelector('style[data-toast-styles]')) {
        style.setAttribute('data-toast-styles', 'true');
        document.head.appendChild(style);
    }
    document.body.appendChild(toast);

    // Remove toast after 5 seconds
    setTimeout(() => {
        toast.style.transition = 'opacity 0.3s ease-out';
        toast.style.opacity = '0';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 5000);
}

export default axios;
