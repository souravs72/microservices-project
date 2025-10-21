import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { hasRole, hasAnyRole, hasMinimumRole } from "../utils/auth";
import LoadingSpinner from "./LoadingSpinner";

interface RoleGuardProps {
  children: React.ReactNode;
  requiredRole?: string;
  requiredRoles?: string[];
  minimumRole?: string;
  fallback?: React.ReactNode;
  redirectTo?: string;
}

const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  requiredRole,
  requiredRoles,
  minimumRole,
  fallback,
  redirectTo = "/dashboard",
}) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Check specific role requirement
  if (requiredRole && !hasRole(user, requiredRole)) {
    return fallback ? <>{fallback}</> : <Navigate to={redirectTo} replace />;
  }

  // Check any of the required roles
  if (requiredRoles && !hasAnyRole(user, requiredRoles)) {
    return fallback ? <>{fallback}</> : <Navigate to={redirectTo} replace />;
  }

  // Check minimum role requirement
  if (minimumRole && !hasMinimumRole(user, minimumRole)) {
    return fallback ? <>{fallback}</> : <Navigate to={redirectTo} replace />;
  }

  return <>{children}</>;
};

export default RoleGuard;
