import {
  BarChart3,
  Bell,
  Boxes,
  CalendarDays,
  ClipboardCheck,
  FileText,
  FolderKanban,
  History,
  LayoutDashboard,
  LifeBuoy,
  Repeat2,
  Settings,
  Tag,
  UserCircle,
  Users2,
  Wrench
} from "lucide-react";

export const navigation = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard, group: "Overview", permissions: ["dashboard.read"] },
  { to: "/analytics", label: "Analytics", icon: BarChart3, group: "Overview", permissions: ["analytics.read"] },
  { to: "/equipments", label: "Equipements", icon: Boxes, group: "Inventory", permissions: ["equipments.read", "equipments.manage"] },
  { to: "/categories", label: "Categories", icon: Tag, group: "Inventory", permissions: ["equipments.manage"] },
  { to: "/users", label: "Utilisateurs", icon: Users2, group: "People", permissions: ["users.manage"] },
  { to: "/assignments", label: "Affectations", employeeLabel: "Mes materiels", icon: ClipboardCheck, group: "Operations", permissions: ["assignments.manage", "assignments.read.own"] },
  { to: "/returns", label: "Restitutions", icon: Repeat2, group: "Operations", permissions: ["assignments.manage", "maintenances.manage"] },
  { to: "/maintenance", label: "Maintenance", icon: Wrench, group: "Operations", permissions: ["maintenances.read", "maintenances.manage", "maintenances.request"] },
  { to: "/tickets", label: "Tickets", icon: LifeBuoy, group: "Operations", permissions: ["maintenances.read", "maintenances.manage"] },
  { to: "/calendar", label: "Calendrier", icon: CalendarDays, group: "Operations", permissions: ["maintenances.read", "maintenances.manage"] },
  { to: "/notifications", label: "Notifications", icon: Bell, group: "System", permissions: ["notifications.read.own"] },
  { to: "/reports", label: "Rapports", icon: FileText, group: "System", permissions: ["reports.read"] },
  { to: "/history", label: "Historique", icon: History, group: "System", permissions: ["audit.read"] },
  { to: "/profile", label: "Profil", icon: UserCircle, group: "Workspace", permissions: ["profile.manage.own"] },
  { to: "/settings", label: "Parametres", icon: Settings, group: "Workspace", permissions: ["system.manage"] },
  { to: "/board", label: "Kanban", icon: FolderKanban, group: "Operations", permissions: ["maintenances.read", "maintenances.manage"] }
];
