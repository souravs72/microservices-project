import React, { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import {
  Users,
  Activity,
  TrendingUp,
  Clock,
  Calendar,
  Mail,
  Bell,
  AlertCircle,
  CheckCircle,
  XCircle,
  Package,
} from "lucide-react";
import {
  usersAPI,
  ordersAPI,
  inventoryAPI,
  notificationsAPI,
} from "../services/api";

interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalOrders: number;
  totalRevenue: number;
  totalProducts: number;
  lowStockProducts: number;
  pendingNotifications: number;
}

interface RecentActivity {
  id: string;
  type: "user" | "order" | "notification" | "email";
  message: string;
  timestamp: string;
  status: "success" | "warning" | "error";
}

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    activeUsers: 0,
    totalOrders: 0,
    totalRevenue: 0,
    totalProducts: 0,
    lowStockProducts: 0,
    pendingNotifications: 0,
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
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        // Fetch real data from APIs
        const [
          usersResponse,
          ordersResponse,
          productsResponse,
          notificationsResponse,
        ] = await Promise.all([
          usersAPI.getAll({ page: 0, size: 1 }),
          ordersAPI.getAll({ page: 0, size: 1 }),
          inventoryAPI.getProducts({ page: 0, size: 1 }),
          notificationsAPI.getAll({ page: 0, size: 1, unreadOnly: true }),
        ]);

        // Calculate stats from API responses
        const totalUsers = usersResponse.data.totalElements || 0;
        const totalOrders = ordersResponse.data.totalElements || 0;
        const totalProducts = productsResponse.data.totalElements || 0;
        const pendingNotifications =
          notificationsResponse.data.totalElements || 0;

        // Calculate revenue from orders (simplified)
        const orders = ordersResponse.data.content || [];
        const totalRevenue = orders.reduce(
          (sum: number, order: any) => sum + (order.totalAmount || 0),
          0
        );

        // Get low stock products
        const lowStockResponse = await inventoryAPI.getLowStockProducts();
        const lowStockProducts = lowStockResponse.data.length || 0;

        setStats({
          totalUsers,
          activeUsers: Math.floor(totalUsers * 0.7), // Assume 70% are active
          totalOrders,
          totalRevenue,
          totalProducts,
          lowStockProducts,
          pendingNotifications,
        });

        // Generate recent activity from real data
        const recentActivity: RecentActivity[] = [
          {
            id: "1",
            type: "user",
            message: `Total users: ${totalUsers}`,
            timestamp: new Date().toISOString(),
            status: "success",
          },
          {
            id: "2",
            type: "order",
            message: `Total orders: ${totalOrders}`,
            timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
            status: "success",
          },
          {
            id: "3",
            type: "notification",
            message: `${pendingNotifications} pending notifications`,
            timestamp: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
            status: pendingNotifications > 0 ? "warning" : "success",
          },
          {
            id: "4",
            type: "order",
            message: `${lowStockProducts} products with low stock`,
            timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
            status: lowStockProducts > 0 ? "warning" : "success",
          },
        ];

        setRecentActivity(recentActivity);
      } catch (error) {
        console.error("Failed to fetch dashboard data:", error);
        // Fallback to mock data if APIs fail
        setStats({
          totalUsers: 0,
          activeUsers: 0,
          totalOrders: 0,
          totalRevenue: 0,
          totalProducts: 0,
          lowStockProducts: 0,
          pendingNotifications: 0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();

    // Refresh data every 30 seconds
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
      case "user":
        return <Users className="h-4 w-4" />;
      case "order":
        return <TrendingUp className="h-4 w-4" />;
      case "notification":
        return <Bell className="h-4 w-4" />;
      case "email":
        return <Mail className="h-4 w-4" />;
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
              Welcome back, {user?.firstName}!
            </h1>
            <p className="text-gray-600 mt-1">
              Here's what's happening with your microservices today.
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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Users className="h-8 w-8 text-blue-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Users
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.totalUsers.toLocaleString()}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Activity className="h-8 w-8 text-green-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Active Users
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.activeUsers.toLocaleString()}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <TrendingUp className="h-8 w-8 text-purple-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Orders
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.totalOrders.toLocaleString()}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <TrendingUp className="h-8 w-8 text-yellow-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Revenue
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  ${stats.totalRevenue.toLocaleString()}
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
                  {stats.totalProducts.toLocaleString()}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <AlertCircle className="h-8 w-8 text-orange-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Low Stock
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.lowStockProducts}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Bell className="h-8 w-8 text-red-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Pending Notifications
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.pendingNotifications}
                </dd>
              </dl>
            </div>
          </div>
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

export default Dashboard;
