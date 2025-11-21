import {
  LayoutDashboard,
  FileText,
  Shield,
  Calculator,
  Users,
  Settings,
  ClipboardList,
  BarChart3,
  FileCheck,
} from "lucide-vue-next"

// Unified navigation with role-based access
export const NAVIGATION = [
  {
    title: "Dashboard",
    icon: LayoutDashboard,
    to: { name: "dashboard" },
    exact: true,
    roles: ["ADMIN", "UNDERWRITER", "CLAIM_ADJUSTER"],
  },
  // Staff Management (Admin only)
  {
    title: "Staff Management",
    icon: Users,
    roles: ["ADMIN"],
    children: [
      { title: "Register Staff", to: { name: "staff-register" } },
      { title: "Manage Staff Accounts", to: { name: "staff-manage" } },
      { title: "Roles & Permissions", to: { name: "roles-permissions" } },
    ],
  },
  // Insurance Management (Admin only)
  {
    title: "Insurance Management",
    icon: ClipboardList,
    roles: ["ADMIN"],
    children: [
      { title: "Create Application Type", to: { name: "applications-new" } },
      { title: "View All Applications", to: { name: "admin-applications-all" } },
    ],
  },
  // System Settings (Admin only)
  {
    title: "System Settings",
    icon: Settings,
    roles: ["ADMIN"],
    children: [
      { title: "General Settings", to: { name: "settings-general" } },
      { title: "Audit Logs", to: { name: "settings-audit-logs" } },
      { title: "Database Management", to: { name: "settings-database" } },
    ],
  },
  // Reports & Analytics (Admin only)
  {
    title: "Reports & Analytics",
    icon: BarChart3,
    roles: ["ADMIN"],
    children: [
      { title: "Overall Performance", to: { name: "reports-overall" } },
      { title: "User Activity", to: { name: "reports-user-activity" } },
    ],
  },
  // Applications (Underwriter & Claim Adjuster)
  {
    title: "Applications",
    icon: FileText,
    roles: ["UNDERWRITER", "CLAIM_ADJUSTER"],
    children: [
      { title: "All Applications", to: { name: "applications-all" } },
    ],
  },
  // Risk Assessment (Underwriter only)
  {
    title: "Risk Assessment",
    icon: Shield,
    roles: ["UNDERWRITER"],
    children: [
      { title: "Risk Factors", to: { name: "risk-factors" } },
      { title: "Geographic Analysis", to: { name: "risk-geo" } },
    ],
  },
  // Guidelines & Tools (Underwriter only)
  {
    title: "Guidelines & Tools",
    icon: Calculator,
    roles: ["UNDERWRITER"],
    children: [
      { title: "Eligibility Criteria", to: { name: "guidelines-eligibility" } },
      { title: "Coverage & Premium", to: { name: "guidelines-coverage" } },
      { title: "Underwriting Manual", to: { name: "guidelines-manual" } },
    ],
  },
  // Claims Processing (Claim Adjuster only)
  {
    title: "Claims Processing",
    icon: FileCheck,
    roles: ["CLAIM_ADJUSTER"],
    children: [
      { title: "Pending Claims", to: { name: "claims-pending" } },
      { title: "Processed Claims", to: { name: "claims-processed" } },
    ],
  }, 
]

// Data types for application fields
export const DATA_TYPES = ["TEXT", "NUMBER", "DATE", "BOOLEAN", "FILE", "ENUM", "GEOLOCATION"]
