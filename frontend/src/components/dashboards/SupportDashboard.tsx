import React, { useState, useEffect } from "react";
import {
  Bell,
  MessageCircle,
  Users,
  Clock,
  Calendar,
  CheckCircle,
  XCircle,
  AlertCircle,
  Activity,
  Phone,
  HelpCircle,
  TrendingUp,
} from "lucide-react";
import { notificationsAPI, usersAPI } from "../../services/api";

interface DashboardStats {
  pendingTickets: number;
  resolvedToday: number;
  totalNotifications: number;
  activeUsers: number;
  responseTime: string;
  customerSatisfaction: number;
}

interface SupportTicket {
  id: string;
  title: string;
  description: string;
  customer: string;
  priority: "low" | "medium" | "high" | "urgent";
  status: "open" | "in-progress" | "resolved" | "closed";
  createdAt: string;
  lastUpdated: string;
}

interface RecentActivity {
  id: string;
  type: "ticket" | "notification" | "message" | "call";
  message: string;
  timestamp: string;
  status: "success" | "warning" | "error";
}

const SupportDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    pendingTickets: 0,
    resolvedToday: 0,
    totalNotifications: 0,
    activeUsers: 0,
    responseTime: "2.5h",
    customerSatisfaction: 4.8,
  });
  const [supportTickets, setSupportTickets] = useState<SupportTicket[]>([]);
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

        const [notificationsResponse, usersResponse] = await Promise.allSettled(
          [
            notificationsAPI.getAll({ page: 0, size: 1 }),
            usersAPI.getAll({ page: 0, size: 1 }),
          ]
        );

        const totalNotifications =
          notificationsResponse.status === "fulfilled"
            ? Array.isArray(notificationsResponse.value.data)
              ? notificationsResponse.value.data.length
              : notificationsResponse.value.data.totalElements || 0
            : 0;

        const totalUsers =
          usersResponse.status === "fulfilled"
            ? Array.isArray(usersResponse.value.data)
              ? usersResponse.value.data.length
              : usersResponse.value.data.totalElements || 0
            : 0;

        setStats({
          pendingTickets: 15, // Mock data
          resolvedToday: 12,
          totalNotifications,
          activeUsers: Math.floor(totalUsers * 0.7),
          responseTime: "2.5h",
          customerSatisfaction: 4.8,
        });

        // Mock support tickets for demonstration
        setSupportTickets([
          {
            id: "1",
            title: "Login Issues",
            description: "User unable to login with correct credentials",
            customer: "john.doe@example.com",
            priority: "high",
            status: "open",
            createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
            lastUpdated: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
          },
          {
            id: "2",
            title: "Password Reset Request",
            description: "Customer forgot password and needs reset link",
            customer: "jane.smith@example.com",
            priority: "medium",
            status: "in-progress",
            createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
            lastUpdated: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
          },
          {
            id: "3",
            title: "Feature Request",
            description: "Customer requesting new feature for mobile app",
            customer: "mike.wilson@example.com",
            priority: "low",
            status: "open",
            createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
            lastUpdated: new Date(
              Date.now() - 1000 * 60 * 60 * 2
            ).toISOString(),
          },
        ]);

        // Generate recent activity
        const recentActivity: RecentActivity[] = [
          {
            id: "1",
            type: "ticket",
            message: "Resolved ticket: Payment processing issue",
            timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
            status: "success",
          },
          {
            id: "2",
            type: "notification",
            message: "New support ticket received",
            timestamp: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
            status: "warning",
          },
          {
            id: "3",
            type: "message",
            message: "Customer inquiry about product features",
            timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
            status: "success",
          },
          {
            id: "4",
            type: "call",
            message: "Phone support call completed",
            timestamp: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
            status: "success",
          },
        ];

        setRecentActivity(recentActivity);
      } catch (error) {
        console.error("Failed to fetch dashboard data:", error);
        setStats({
          pendingTickets: 0,
          resolvedToday: 0,
          totalNotifications: 0,
          activeUsers: 0,
          responseTime: "N/A",
          customerSatisfaction: 0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleTicketAction = (ticketId: string, action: string) => {
    setSupportTickets((prev) =>
      prev.map((ticket) =>
        ticket.id === ticketId
          ? {
              ...ticket,
              status: action as any,
              lastUpdated: new Date().toISOString(),
            }
          : ticket
      )
    );
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
      case "urgent":
        return "text-red-600 bg-red-100";
      case "high":
        return "text-orange-600 bg-orange-100";
      case "medium":
        return "text-yellow-600 bg-yellow-100";
      case "low":
        return "text-green-600 bg-green-100";
      default:
        return "text-gray-600 bg-gray-100";
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "open":
        return "text-red-600 bg-red-100";
      case "in-progress":
        return "text-yellow-600 bg-yellow-100";
      case "resolved":
        return "text-green-600 bg-green-100";
      case "closed":
        return "text-gray-600 bg-gray-100";
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
      case "ticket":
        return <HelpCircle className="h-4 w-4" />;
      case "notification":
        return <Bell className="h-4 w-4" />;
      case "message":
        return <MessageCircle className="h-4 w-4" />;
      case "call":
        return <Phone className="h-4 w-4" />;
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
              Support Dashboard
            </h1>
            <p className="text-gray-600 mt-1">
              Manage customer support tickets, notifications, and
              communications.
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
              <HelpCircle className="h-8 w-8 text-orange-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Pending Tickets
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.pendingTickets}
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
                  Resolved Today
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.resolvedToday}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Bell className="h-8 w-8 text-blue-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Total Notifications
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
              <Users className="h-8 w-8 text-purple-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Active Users
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.activeUsers}
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Clock className="h-8 w-8 text-indigo-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Avg Response Time
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.responseTime}
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
                  Customer Satisfaction
                </dt>
                <dd className="text-lg font-medium text-gray-900">
                  {stats.customerSatisfaction}/5.0
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      {/* Support Tickets */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">Support Tickets</h3>
        </div>
        <div className="divide-y divide-gray-200">
          {supportTickets.map((ticket) => (
            <div key={ticket.id} className="px-6 py-4">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-3">
                    <h4 className="text-sm font-medium text-gray-900">
                      {ticket.title}
                    </h4>
                    <span
                      className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPriorityColor(
                        ticket.priority
                      )}`}
                    >
                      {ticket.priority.toUpperCase()}
                    </span>
                    <span
                      className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(
                        ticket.status
                      )}`}
                    >
                      {ticket.status.toUpperCase()}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    {ticket.description}
                  </p>
                  <div className="flex items-center space-x-4 mt-2">
                    <span className="text-xs text-gray-400">
                      Customer: {ticket.customer}
                    </span>
                    <span className="text-xs text-gray-400">
                      Created: {formatRelativeTime(ticket.createdAt)}
                    </span>
                    <span className="text-xs text-gray-400">
                      Updated: {formatRelativeTime(ticket.lastUpdated)}
                    </span>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleTicketAction(ticket.id, "in-progress")}
                    className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                  >
                    Start
                  </button>
                  <button
                    onClick={() => handleTicketAction(ticket.id, "resolved")}
                    className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                  >
                    Resolve
                  </button>
                  <button
                    onClick={() => handleTicketAction(ticket.id, "closed")}
                    className="inline-flex items-center px-3 py-1.5 border border-gray-300 text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          ))}
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

export default SupportDashboard;
