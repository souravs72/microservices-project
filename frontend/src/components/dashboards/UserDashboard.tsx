import React, { useState, useEffect } from "react";
import {
  Clock,
  Calendar,
  Bell,
  User,
  Activity,
  Package,
  ShoppingCart,
} from "lucide-react";
import { notificationsAPI, ordersAPI } from "../../services/api";

interface DashboardStats {
  totalOrders: number;
  pendingOrders: number;
  totalNotifications: number;
  accountStatus: string;
}

interface RecentActivity {
  id: string;
  type: "order" | "notification" | "account";
  message: string;
  timestamp: string;
  status: "success" | "warning" | "error";
}

const UserDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalOrders: 0,
    pendingOrders: 0,
    totalNotifications: 0,
    accountStatus: "active",
  });
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

        const [notificationsResponse, ordersResponse] =
          await Promise.allSettled([
            notificationsAPI.getAll({ page: 0, size: 1 }),
            ordersAPI.getAll({ page: 0, size: 1 }),
          ]);

        const totalNotifications =
          notificationsResponse.status === "fulfilled"
            ? Array.isArray(notificationsResponse.value.data)
              ? notificationsResponse.value.data.length
              : notificationsResponse.value.data.totalElements || 0
            : 0;

        const totalOrders =
          ordersResponse.status === "fulfilled"
            ? Array.isArray(ordersResponse.value.data)
              ? ordersResponse.value.data.length
              : ordersResponse.value.data.totalElements || 0
            : 0;

        setStats({
          totalOrders,
          pendingOrders: Math.floor(totalOrders * 0.3), // Assume 30% are pending
          totalNotifications,
          accountStatus: "active",
        });

        // Generate recent activity
        const recentActivity: RecentActivity[] = [
          {
            id: "1",
            type: "order",
            message: "Order #12345 has been shipped",
            timestamp: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
            status: "success",
          },
          {
            id: "2",
            type: "notification",
            message: "New product available in your wishlist",
            timestamp: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
            status: "warning",
          },
          {
            id: "3",
            type: "account",
            message: "Profile information updated successfully",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
            status: "success",
          },
          {
            id: "4",
            type: "order",
            message: "Order #12344 is being processed",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4).toISOString(),
            status: "success",
          },
        ];

        setRecentActivity(recentActivity);
      } catch (error) {
        console.error("Failed to fetch dashboard data:", error);
        setStats({
          totalOrders: 0,
          pendingOrders: 0,
          totalNotifications: 0,
          accountStatus: "active",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "success":
        return <Activity className="h-4 w-4 text-green-500" />;
      case "warning":
        return <Bell className="h-4 w-4 text-yellow-500" />;
      case "error":
        return <Activity className="h-4 w-4 text-red-500" />;
      default:
        return <Activity className="h-4 w-4 text-gray-500" />;
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case "order":
        return <ShoppingCart className="h-4 w-4" />;
      case "notification":
        return <Bell className="h-4 w-4" />;
      case "account":
        return <User className="h-4 w-4" />;
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
              Welcome to Your Dashboard
            </h1>
            <p className="text-gray-600 mt-1">
              Track your orders, notifications, and account activity.
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
              <ShoppingCart className="h-8 w-8 text-blue-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Orders
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.totalOrders}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Clock className="h-8 w-8 text-yellow-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Pending Orders
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.pendingOrders}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Bell className="h-8 w-8 text-purple-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Notifications
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.totalNotifications}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <User className="h-8 w-8 text-green-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Account Status
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.accountStatus}
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          Quick Actions
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="flex items-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
            <ShoppingCart className="h-6 w-6 text-blue-600 mr-3" />
            <div className="text-left">
              <div className="text-sm font-medium text-gray-900">
                Place Order
              </div>
              <div className="text-sm text-gray-500">Start shopping</div>
            </div>
          </button>
          <button className="flex items-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
            <Package className="h-6 w-6 text-purple-600 mr-3" />
            <div className="text-left">
              <div className="text-sm font-medium text-gray-900">
                Track Orders
              </div>
              <div className="text-sm text-gray-500">View order status</div>
            </div>
          </button>
          <button className="flex items-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
            <Bell className="h-6 w-6 text-yellow-600 mr-3" />
            <div className="text-left">
              <div className="text-sm font-medium text-gray-900">
                Notifications
              </div>
              <div className="text-sm text-gray-500">
                View all notifications
              </div>
            </div>
          </button>
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

export default UserDashboard;
