import React from "react";
import { useAuth } from "../contexts/AuthContext";
import AdminDashboard from "../components/dashboards/AdminDashboard";
import ModeratorDashboard from "../components/dashboards/ModeratorDashboard";
import SupportDashboard from "../components/dashboards/SupportDashboard";
import UserDashboard from "../components/dashboards/UserDashboard";
import { isAdmin, isModerator, isSupport, isUser } from "../utils/auth";

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  // Render role-specific dashboard
  if (isAdmin(user)) {
    return <AdminDashboard />;
  }

  if (isModerator(user)) {
    return <ModeratorDashboard />;
  }

  if (isSupport(user)) {
    return <SupportDashboard />;
  }

  if (isUser(user)) {
    return <UserDashboard />;
  }

  // Fallback to user dashboard for unknown roles
  return <UserDashboard />;
};

export default Dashboard;
