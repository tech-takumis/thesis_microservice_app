// Unified dashboard for all users
export const DASHBOARD_ROUTE = {
    path: '/agriculturist/dashboard',
    name: 'agriculturist-dashboard',
    component: () => import('@/pages/agriculturist/AgriculturistDashboard.vue'),
    meta: {
        title: 'Dashboard',
        guard: 'auth'
    }
};

export const ADMIN_ROUTES = [
    {
        path: '/admin/users',
        name: 'admin-users',
        component: () => import('@/pages/admin/users/AllUsers.vue'),
        meta: {
            title: 'All Users',
            guard: 'auth',
            permissions: ['CAN_VIEW_USER']
        }
    },
    {
        path: '/admin/users/:id',
        name: 'admin-user-view',
        component: () => import('@/pages/admin/users/ViewUser.vue'),
        meta: {
            title: 'View User',
            guard: 'auth',
            permissions: ['CAN_VIEW_USER']
        }
    },
    {
        path: '/admin/users/:id/edit',
        name: 'admin-user-edit',
        component: () => import('@/pages/admin/users/EditUser.vue'),
        meta: {
            title: 'Edit User',
            guard: 'auth',
            permissions: ['CAN_VIEW_USER']
        }
    },
    {
        path: '/admin/roles',
        name: 'admin-roles-permissions',
        component: () => import('@/pages/admin/roles/RolesPermissions.vue'),
        meta: {
            title: 'Roles & Permissions',
            guard: 'auth',
            permissions: ['CAN_VIEW_USER']
        }
    },
    {
        path: '/admin/applications',
        name: 'admin-applications',
        component: () => import('@/pages/admin/applications/ViewApplications.vue'),
        meta: {
            title: 'View Applications',
            guard: 'auth',
            role: 'ADMIN'
        }
    },
    {
        path: '/admin/applications/new',
        name: 'admin-new-application',
        component: () => import('@/pages/admin/applications/NewApplication.vue'),
        meta: {
            title: 'New Application',
            guard: 'auth',
            role: 'ADMIN'
        }
    },
    {
        path: '/admin/approvals',
        name: 'admin-approvals',
        component: () => import('@/pages/admin/approvals/Approvals.vue'),
        meta: {
            title: 'Approvals',
            guard: 'auth',
            role: 'ADMIN'
        }
    }
];

export const MUNICIPALITY_ROUTES = [
    {
        path: '/agriculturist/submit-crop-data',
        name: 'agriculturist-submit-crop-data',
        component: () => import('@/pages/agriculturist/applications/ApplicationType.vue'),
        meta: {
            title: 'Manage Applications',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/submit-crop-data/application-type/:id',
        name: 'agriculturist-application-type',
        component: () => import('@/pages/agriculturist/applications/ApplicationList.vue'),
        meta: {
            title: 'Application Submission',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/submit-crop-data/application-type/submission-detail/:id/:applicationTypeId',
        name: 'agriculturist-submit-crop-data-detail',
        component: () => import('@/pages/agriculturist/applications/ApplicationDetail.vue'),
        meta: {
            title: 'Application Submission Detail',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/submit-crop-data/application/:applicationId/map',
        name: 'agriculturist-submit-crop-data-map',
        component: () => import('@/pages/agriculturist/applications/ApplicationMap.vue'),
        meta: {
            title: 'Application Location Map',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/ai/damage-result/:applicationId/:applicationTypeId',
        name: 'agriculturist-damage-report',
        component: () => import('@/pages/agriculturist/applications/DamageClaimReview.vue'),
        meta: {
            title: 'AI Damage Analysis',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/application/verification/:applicationId/:applicationTypeId',
        name: 'agriculturist-application-verification',
        component: () => import('@/pages/agriculturist/applications/ApplicationVerificationPage.vue'),
        meta: {
            title: 'Application Verification',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/voucher/all',
        name: 'agriculturist-voucher-all',
        component: () => import('@/pages/agriculturist/operations/VoucherList.vue'),
        meta: {
            title: 'All Vouchers',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/voucher/generate',
        name: 'agriculturist-voucher-generate',
        component: () => import('@/pages/agriculturist/operations/AgriculturistVoucher.vue'),
        meta: {
            title: 'Voucher Generation',
            guard: 'auth',
            permissions: ['CAN_SUBMIT_CROP_DATA']
        }
    },
    {
        path: '/agriculturist/voucher/scanner',
        name: 'agriculturist-voucher-scanner',
        component: () => import('@/pages/agriculturist/operations/VoucherScanner.vue'),
        meta: {
            title: 'Voucher Scanner',
            guard: 'auth'
        }
    },
    {
        path: '/agriculturist/voucher/:id/detail',
        name: 'agriculturist-voucher-detail',
        component: () => import('@/pages/agriculturist/operations/VoucherDetails.vue'),
        meta: {
            title: 'Voucher Detail',
            guard: 'auth'
        }
    },
    {
        path: '/agriculturist/transactions',
        name: 'agriculturist-transaction',
        component: () => import('@/pages/agriculturist/operations/AgriculturistTransaction.vue'),
        meta: {
            title: 'Transactions',
            guard: 'auth'
        }
    },
    {
        path: '/agriculturist/transactions/:id/detail',
        name: 'agriculturist-transaction-detail',
        component: () => import('@/pages/agriculturist/operations/TransactionDetail.vue'),
        meta: {
            title: 'Transaction Detail',
            guard: 'auth'
        }
    },
    {
        path: '/agriculturist/monitor-programs',
        name: 'agriculturist-monitor-programs',
        component: () => import('@/pages/agriculturist/operations/AgriculturistMonitorPrograms.vue'),
        meta: {
            title: 'Monitor Programs',
            guard: 'auth',
            permissions: ['CAN_MONITOR_PROGRAMS']
        }
    },
    {
        path: '/agriculturist/monitor-programs/:id/detail',
        name: 'agriculturist-monitor-programs-detail',
        component: () => import('@/pages/agriculturist/operations/ProgramDetail.vue'),
        meta: {
            title: 'Program Detail',
            guard: 'auth',
            permissions: ['CAN_MONITOR_PROGRAMS']
        }
    },
    {
        path: '/agriculturist/process-claims',
        name: 'agriculturist-process-claims',
        component: () => import('@/pages/agriculturist/claims/AgriculturistProcessClaims.vue'),
        meta: {
            title: 'Process Claims',
            guard: 'auth',
            permissions: ['CAN_PROCESS_CLAIM']
        }
    },
    {
        path: '/agriculturist/process-claims/:id/detail',
        name: 'agriculturist-claim-detail',
        component: () => import('@/pages/agriculturist/claims/ClaimDetails.vue'),
        meta: {
            title: 'Claim Detail',
            guard: 'auth',
            permissions: ['CAN_PROCESS_CLAIM']
        }
    },
    {
        path: '/agriculturist/message',
        name: 'agriculturist-message',
        component: () => import('@/pages/agriculturist/message/AgriculturistMessage.vue'),
        meta: {
            title: 'Message',
            guard: 'auth'
        }
    }
];

export const AGRICULTURAL_EXTENSION_WORKER_ROUTES = [
    // {
    //     path: '/extension-worker/submit-crop-data',
    //     name: 'extension-worker-submit-crop-data',
    //     component: () => import('@/pages/extension-worker/ExtensionWorkerSubmitCropData.vue'),
    //     meta: {
    //         title: 'Submit Crop Data',
    //         guard: 'auth',
    //         permissions: ['CAN_SUBMIT_CROP_DATA']
    //     }
    // }
    // {
    //     path: '/extension-worker/conduct-training',
    //     name: 'extension-worker-conduct-training',
    //     component: () => import('@/pages/extension-worker/ExtensionWorkerConductTraining.vue'),
    //     meta: {
    //         title: 'Conduct Training',
    //         guard: 'auth',
    //         role: 'AGRICULTURAL EXTENSION WORKER'
    //     }
    // }
];
