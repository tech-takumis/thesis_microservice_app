// Unified routes with role-based access
export const ROUTES = [
  // Dashboard (all roles redirect here)
  {
    path: "/dashboard",
    name: "dashboard",
    component: () => import("@/pages/underwriter/UnderwriterDashboard.vue"),
    meta: {
      title: "Dashboard",
      guard: "auth",
      roles: ["ADMIN", "UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },

  // ==================== ADMIN ROUTES ====================
  // Staff Management
  {
    path: "/staff/register",
    name: "staff-register",
    component: () => import("@/pages/admin/staff/RegisterStaff.vue"),
    meta: {
      title: "Register New Staff",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/staff/manage",
    name: "staff-manage",
    component: () => import("@/pages/admin/staff/ManageStaff.vue"),
    meta: {
      title: "Manage Staff Accounts",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/roles",
    name: "roles-permissions",
    component: () => import("@/pages/admin/roles/RolesPermissions.vue"),
    meta: {
      title: "Roles & Permissions",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  // Insurance Management
  {
    path: "/applications/new",
    name: "applications-new",
    component: () => import("@/pages/admin/applications/NewApplication.vue"),
    meta: {
      title: "Create New Application Type",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/admin/applications/all",
    name: "admin-applications-all",
    component: () => import("@/pages/admin/applications/ViewApplications.vue"),
    meta: {
      title: "All Application Types",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/applications/manage",
    name: "applications-manage",
    component: () => import("@/pages/admin/applications/ManageApplications.vue"),
    meta: {
      title: "Manage Application Types",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  // System Settings
  {
    path: "/settings/general",
    name: "settings-general",
    component: () => import("@/pages/admin/settings/GeneralSettings.vue"),
    meta: {
      title: "General Settings",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/settings/audit-logs",
    name: "settings-audit-logs",
    component: () => import("@/pages/admin/settings/AuditLogs.vue"),
    meta: {
      title: "Audit Logs",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/settings/database",
    name: "settings-database",
    component: () => import("@/pages/admin/settings/DatabaseManagement.vue"),
    meta: {
      title: "Database Management",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  // Reports & Analytics
  {
    path: "/reports/overall",
    name: "reports-overall",
    component: () => import("@/pages/admin/reports/OverallPerformance.vue"),
    meta: {
      title: "Overall Performance",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },
  {
    path: "/reports/user-activity",
    name: "reports-user-activity",
    component: () => import("@/pages/admin/reports/UserActivity.vue"),
    meta: {
      title: "User Activity",
      guard: "auth",
      roles: ["ADMIN"],
    },
  },


  // ==================== UNDERWRITER & CLAIM ADJUSTER ROUTES ====================
  // Applications (shared)
  {
    path: "/applications/all",
    name: "applications-all",
    component: () => import("@/pages/underwriter/applications/ViewApplications.vue"),
    meta: {
      title: "All Applications",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/applications/type/insurance/:insuranceId/:submissionId/detail",
    name: "applications-detail",
    component: () => import("@/pages/underwriter/applications/ApplicationDetail.vue"),
    meta: {
      title: "Application Detail",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/application/:insuranceId/inspection",
    name: "application-inspection",
    component: () => import("@/pages/underwriter/applications/ApplicationInspection.vue"),
    meta: {
      title: "Application Inspection",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/damage/:insuranceId/:submissionId/claim-review",
    name: "damage-claim-review",
    component: () => import("@/pages/underwriter/applications/DamageClaimReview.vue"),
    meta: {
      title: "Damage Claim Review",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/applications/insurance/:insuranceId/:submissionId/claim",
    name: "applications-claim",
    component: () => import("@/pages/underwriter/applications/ApplicationClaim.vue"),
    meta: {
      title: "Application Claim",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/application/:applicationId/map",
    name: "application-map",
    component: () => import("@/pages/underwriter/applications/ApplicationMap.vue"),
    meta: {
      title: "Application Location Map",
      guard: "auth",
      roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    },
  },

  // ==================== UNDERWRITER ONLY ROUTES ====================
  // Risk Assessment
  {
    path: "/risk-factors",
    name: "risk-factors",
    component: () => import("@/pages/underwriter/risk-assessment/RiskFactor.vue"),
    meta: {
      title: "Risk Factors",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },
  {
    path: "/risk-geo",
    name: "risk-geo",
    component: () => import("@/pages/underwriter/risk-assessment/GeographicAnalysis.vue"),
    meta: {
      title: "Geographic Analysis",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },
  // Guidelines & Tools
  {
    path: "/guidelines-eligibility",
    name: "guidelines-eligibility",
    component: () => import("@/pages/underwriter/tools/GuidelinesEligibility.vue"),
    meta: {
      title: "Guidelines & Eligibility",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },
  {
    path: "/guidelines-coverage",
    name: "guidelines-coverage",
    component: () => import("@/pages/underwriter/tools/GuidelinesCoverage.vue"),
    meta: {
      title: "Guidelines & Coverage",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },
  {
    path: "/guidelines-manual",
    name: "guidelines-manual",
    component: () => import("@/pages/underwriter/tools/GuidelinesManual.vue"),
    meta: {
      title: "Guidelines Manual",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },
  {
    path: "/reports",
    name: "underwriter-reports",
    component: () => import("@/pages/underwriter/Reports.vue"),
    meta: {
      title: "Underwriter Reports",
      guard: "auth",
      roles: ["UNDERWRITER"],
    },
  },

  // ==================== CLAIM ADJUSTER ONLY ROUTES ====================
  {
    path: "/claims/pending",
    name: "claims-pending",
    component: () => import("@/pages/claims-processor/PendingClaims.vue"),
    meta: {
      title: "Pending Claims",
      guard: "auth",
      roles: ["CLAIM_ADJUSTER"],
    },
  },
  {
    path: "/claims/processed",
    name: "claims-processed",
    component: () => import("@/pages/claims-processor/ProcessedClaims.vue"),
    meta: {
      title: "Processed Claims",
      guard: "auth",
      roles: ["CLAIM_ADJUSTER"],
    },
  },
]
