import { FileBarChart2, FileCheck, LayoutDashboard, MessageCircle, Sprout, Users, ClipboardList, CheckSquare, BarChart, BookOpen, Stethoscope, ShoppingCart, Leaf, PawPrint } from 'lucide-vue-next'

/**
 * Unified navigation for all users
 * Access is controlled by roles and permissions
 */
export const UNIFIED_NAVIGATION = [
    {
        title: 'Dashboard',
        icon: LayoutDashboard,
        to: { name: 'agriculturist-dashboard' },
        exact: true
    },
    {
        title: 'User Management',
        icon: Users,
        roles: ['ADMIN'],
        children: [
            {
                title: 'All Users',
                to: { name: 'admin-users' },
                permissions: ['CAN_VIEW_USER']
            },
            {
                title: 'Roles & Permissions',
                to: { name: 'admin-roles-permissions' },
                permissions: ['CAN_VIEW_USER']
            }
        ]
    },
    {
        title: 'Applications',
        icon: ClipboardList,
        roles: ['ADMIN'],
        children: [
            {
                title: 'View Applications',
                to: { name: 'admin-applications' }
            },
            {
                title: 'New Application',
                to: { name: 'admin-new-application' }
            }
        ]
    },
    {
        title: 'Approvals',
        icon: CheckSquare,
        to: { name: 'admin-approvals' },
        roles: ['ADMIN']
    }, 
    {
        title: 'Claims',
        icon: FileCheck,
        children: [
            {
                title: 'Process Claims',
                to: { name: 'agriculturist-process-claims' },
                permissions: ['CAN_PROCESS_CLAIM']
            }
        ]
    },
    {
        title: 'Extension Services',
        icon: BookOpen,
        roles: ['AGRICULTURAL EXTENSION WORKER'],
        children: [
            {
                title: 'Conduct Training',
                to: { name: 'extension-worker-conduct-training' }
            }
        ]
    },
    {
        title: 'Diagnostic Services',
        icon: Stethoscope,
        to: { name: 'extension-worker-diagnostic-services' },
        roles: ['AGRICULTURAL EXTENSION WORKER']
    },
    {
        title: 'Market Access',
        icon: ShoppingCart,
        to: { name: 'extension-worker-market-access' },
        roles: ['AGRICULTURAL EXTENSION WORKER']
    },
    {
        title: 'Sustainability',
        icon: Leaf,
        to: { name: 'extension-worker-promote-sustainability' },
        roles: ['AGRICULTURAL EXTENSION WORKER']
    },
    {
        title: 'Veterinary Support',
        icon: PawPrint,
        to: { name: 'extension-worker-support-veterinary' },
        roles: ['AGRICULTURAL EXTENSION WORKER']
    },
    {
        title: 'Messages',
        icon: MessageCircle,
        to: { name: 'agriculturist-message' }
    }
];

// Legacy exports for backwards compatibility - will be removed
export const ADMIN_NAVIGATION = UNIFIED_NAVIGATION;
export const MUNICIPAL_AGRICULTURIST_NAVIGATION = UNIFIED_NAVIGATION;
export const AGRICULTURAL_EXTENSION_WORKER_NAVIGATION = UNIFIED_NAVIGATION;
