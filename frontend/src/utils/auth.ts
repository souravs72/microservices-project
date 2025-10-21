// Auth utility functions for role-based access control

export interface User {
  id?: string;
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: string;
  bio?: string;
  roles?: string[];
  createdAt?: string;
  isActive?: boolean;
  profilePictureUrl?: string;
}

export const hasRole = (user: User | null, role: string): boolean => {
  if (!user || !user.roles) {
    return false;
  }
  return user.roles.includes(role);
};

export const hasAnyRole = (user: User | null, roles: string[]): boolean => {
  if (!user || !user.roles) {
    return false;
  }
  return roles.some((role) => user.roles?.includes(role));
};

export const isAdmin = (user: User | null): boolean => {
  return hasRole(user, "ADMIN");
};

export const isModerator = (user: User | null): boolean => {
  return hasRole(user, "MODERATOR");
};

export const isUser = (user: User | null): boolean => {
  return hasRole(user, "USER");
};

export const isSupport = (user: User | null): boolean => {
  return hasRole(user, "SUPPORT");
};

// Role hierarchy - higher roles have access to lower role features
export const hasMinimumRole = (
  user: User | null,
  minimumRole: string
): boolean => {
  if (!user || !user.roles) {
    return false;
  }

  const roleHierarchy = {
    USER: 1,
    SUPPORT: 2,
    MODERATOR: 3,
    ADMIN: 4,
  };

  const userRole = user.roles[0]; // Assuming single role for now
  const userLevel = roleHierarchy[userRole as keyof typeof roleHierarchy] || 0;
  const requiredLevel =
    roleHierarchy[minimumRole as keyof typeof roleHierarchy] || 0;

  return userLevel >= requiredLevel;
};
