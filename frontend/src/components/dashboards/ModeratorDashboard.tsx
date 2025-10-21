import React, { useState, useEffect } from "react";
import {
  Package,
  CheckCircle,
  XCircle,
  Clock,
  Calendar,
  AlertCircle,
  Bell,
  Activity,
  Eye,
  ThumbsUp,
  ThumbsDown,
} from "lucide-react";
import { inventoryAPI, notificationsAPI } from "../../services/api";

interface DashboardStats {
  pendingApprovals: number;
  approvedToday: number;
  rejectedToday: number;
  totalProducts: number;
  pendingNotifications: number;
  recentApprovals: number;
}

interface PendingItem {
  id: string;
  type: "product" | "user" | "content";
  title: string;
  description: string;
  submittedBy: string;
  submittedAt: string;
  priority: "low" | "medium" | "high";
}

interface RecentActivity {
  id: string;
  type: "approval" | "rejection" | "notification";
  message: string;
  timestamp: string;
  status: "success" | "warning" | "error";
}

const ModeratorDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    pendingApprovals: 0,
    approvedToday: 0,
    rejectedToday: 0,
    totalProducts: 0,
    pendingNotifications: 0,
    recentApprovals: 0,
  });
  const [pendingItems, setPendingItems] = useState<PendingItem[]>([]);
  const [recentActivity, setRecentActivity] = useState<RecentActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentTime, setCurrentTime] = useState(new Date());

  // Update time every second
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // Fetch dashboard data
  useEffect(() => {
    if (!localStorage.getItem("token")) {
      setLoading(false);
      return;
    }
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        const [productsResponse, notificationsResponse] =
          await Promise.allSettled([
            inventoryAPI.getProducts({ page: 0, size: 1 }),
            notificationsAPI.getAll({ page: 0, size: 1, unreadOnly: true }),
          ]);

        const totalProducts =
          productsResponse.status === "fulfilled"
            ? Array.isArray(productsResponse.value.data)
              ? productsResponse.value.data.length
              : productsResponse.value.data.totalElements || 0
            : 0;

        const pendingNotifications =
          notificationsResponse.status === "fulfilled"
            ? Array.isArray(notificationsResponse.value.data)
              ? notificationsResponse.value.data.length
              : notificationsResponse.value.data.totalElements || 0
            : 0;

        setStats({
          pendingApprovals: 12, // Mock data
          approvedToday: 8,
          rejectedToday: 2,
          totalProducts,
          pendingNotifications,
          recentApprovals: 15,
        });

        // Mock pending items for demonstration
        setPendingItems([
          {
            id: "1",
            type: "product",
            title: "New Product: Wireless Headphones",
            description:
              "High-quality wireless headphones with noise cancellation",
            submittedBy: "john.doe",
            submittedAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
            priority: "high",
          },
          {
            id: "2",
            type: "product",
            title: "Product Update: Smart Watch Series 3",
            description: "Updated specifications and pricing for smart watch",
            submittedBy: "jane.smith",
            submittedAt: new Date(
              Date.now() - 1000 * 60 * 60 * 2
            ).toISOString(),
            priority: "medium",
          },
          {
            id: "3",
            type: "user",
            title: "User Account Verification",
            description:
              "New user requesting verification for premium features",
            submittedBy: "mike.wilson",
            submittedAt: new Date(
              Date.now() - 1000 * 60 * 60 * 4
            ).toISOString(),
            priority: "low",
          },
        ]);

        // Generate recent activity
        const recentActivity: RecentActivity[] = [
          {
            id: "1",
            type: "approval",
            message: "Approved product: Bluetooth Speaker",
            timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
            status: "success",
          },
          {
            id: "2",
            type: "rejection",
            message: "Rejected product: Unverified Smartphone",
            timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
            status: "warning",
          },
          {
            id: "3",
            type: "approval",
            message: "Approved user verification for premium access",
            timestamp: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
            status: "success",
          },
          {
            id: "4",
            type: "notification",
            message: "New product submission requires review",
            timestamp: new Date(Date.now() - 1000 * 60 * 90).toISOString(),
            status: "warning",
          },
        ];

        setRecentActivity(recentActivity);
      } catch (error) {
        console.error("Failed to fetch dashboard data:", error);
        setStats({
          pendingApprovals: 0,
          approvedToday: 0,
          rejectedToday: 0,
          totalProducts: 0,
          pendingNotifications: 0,
          recentApprovals: 0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleApprove = (itemId: string) => {
    setPendingItems((prev) => prev.filter((item) => item.id !== itemId));
    setStats((prev) => ({
      ...prev,
      pendingApprovals: prev.pendingApprovals - 1,
      approvedToday: prev.approvedToday + 1,
    }));
  };

  const handleReject = (itemId: string) => {
    setPendingItems((prev) => prev.filter((item) => item.id !== itemId));
    setStats((prev) => ({
      ...prev,
      pendingApprovals: prev.pendingApprovals - 1,
      rejectedToday: prev.rejectedToday + 1,
    }));
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString("en-US", {
      hour12: true,
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "high":
        return "text-red-600 bg-red-100";
      case "medium":
        return "text-yellow-600 bg-yellow-100";
      case "low":
        return "text-green-600 bg-green-100";
      default:
        return "text-gray-600 bg-gray-100";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "success":
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case "warning":
        return <AlertCircle className="h-4 w-4 text-yellow-500" />;
      case "error":
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <Activity className="h-4 w-4 text-gray-500" />;
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case "approval":
        return <ThumbsUp className="h-4 w-4" />;
      case "rejection":
        return <ThumbsDown className="h-4 w-4" />;
      case "notification":
        return <Bell className="h-4 w-4" />;
      default:
        return <Activity className="h-4 w-4" />;
    }
  };

  const formatRelativeTime = (timestamp: string) => {
    const now = new Date();
    const time = new Date(timestamp);
    const diffInSeconds = Math.floor((now.getTime() - time.getTime()) / 1000);

    if (diffInSeconds < 60) return "Just now";
    if (diffInSeconds < 3600)
      return `${Math.floor(diffInSeconds / 60)} minutes ago`;
    if (diffInSeconds < 86400)
      return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    return `${Math.floor(diffInSeconds / 86400)} days ago`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Moderator Dashboard
            </h1>
            <p className="text-gray-600 mt-1">
              Manage product approvals, user verifications, and content
              moderation.
            </p>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500 flex items-center">
              <Clock className="h-4 w-4 mr-1" />
              {formatTime(currentTime)}
            </div>
            <div className="text-sm text-gray-500 flex items-center mt-1">
              <Calendar className="h-4 w-4 mr-1" />
              {formatDate(currentTime)}
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <AlertCircle className="h-8 w-8 text-orange-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Pending Approvals
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.pendingApprovals}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <CheckCircle className="h-8 w-8 text-green-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Approved Today
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.approvedToday}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <XCircle className="h-8 w-8 text-red-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Rejected Today
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.rejectedToday}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Package className="h-8 w-8 text-purple-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Products
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.totalProducts}
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      {/* Pending Items */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">
            Pending Approvals
          </h3>
        </div>
        <div className="divide-y divide-gray-200">
          {pendingItems.map((item) => (
            <div key={item.id} className="px-6 py-4">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-3">
                    <h4 className="text-sm font-medium text-gray-900">
                      {item.title}
                    </h4>
                    <span
                      className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPriorityColor(
                        item.priority
                      )}`}
                    >
                      {item.priority.toUpperCase()}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    {item.description}
                  </p>
                  <div className="flex items-center space-x-4 mt-2">
                    <span className="text-xs text-gray-400">
                      Submitted by: {item.submittedBy}
                    </span>
                    <span className="text-xs text-gray-400">
                      {formatRelativeTime(item.submittedAt)}
                    </span>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleApprove(item.id)}
                    className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                  >
                    <CheckCircle className="h-3 w-3 mr-1" />
                    Approve
                  </button>
                  <button
                    onClick={() => handleReject(item.id)}
                    className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-red-600 hover:bg-red-700"
                  >
                    <XCircle className="h-3 w-3 mr-1" />
                    Reject
                  </button>
                  <button className="inline-flex items-center px-3 py-1.5 border border-gray-300 text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                    <Eye className="h-3 w-3 mr-1" />
                    View
                  </button>
                </div>
              </div>
            </div>
          ))}
          {pendingItems.length === 0 && (
            <div className="px-6 py-8 text-center">
              <CheckCircle className="mx-auto h-12 w-12 text-green-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                No pending approvals
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                All items have been reviewed and processed.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">Recent Activity</h3>
        </div>
        <div className="divide-y divide-gray-200">
          {recentActivity.map((activity) => (
            <div
              key={activity.id}
              className="px-6 py-4 flex items-center space-x-4"
            >
              <div className="flex-shrink-0">
                {getStatusIcon(activity.status)}
              </div>
              <div className="flex-shrink-0">{getTypeIcon(activity.type)}</div>
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-900">{activity.message}</p>
                <p className="text-sm text-gray-500">
                  {formatRelativeTime(activity.timestamp)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ModeratorDashboard;
