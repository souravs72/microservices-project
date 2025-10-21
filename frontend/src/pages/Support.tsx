import React from "react";
import {
  HelpCircle,
  MessageCircle,
  Phone,
  Clock,
  CheckCircle,
  AlertCircle,
} from "lucide-react";

const Support: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h1 className="text-2xl font-bold text-gray-900">Customer Support</h1>
        <p className="text-gray-600 mt-2">
          Manage customer support tickets, communications, and help requests.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <HelpCircle className="h-8 w-8 text-orange-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Open Tickets
                </dt>
                <dd className="text-lg font-medium text-gray-900">15</dd>
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
                <dd className="text-lg font-medium text-gray-900">12</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Clock className="h-8 w-8 text-blue-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Avg Response Time
                </dt>
                <dd className="text-lg font-medium text-gray-900">2.5h</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <AlertCircle className="h-8 w-8 text-red-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Urgent Tickets
                </dt>
                <dd className="text-lg font-medium text-gray-900">3</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            Recent Support Tickets
          </h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div className="flex items-center space-x-3">
                <MessageCircle className="h-5 w-5 text-blue-600" />
                <div>
                  <h3 className="text-sm font-medium text-gray-900">
                    Login Issues
                  </h3>
                  <p className="text-sm text-gray-500">john.doe@example.com</p>
                </div>
              </div>
              <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-red-800 bg-red-100">
                HIGH
              </span>
            </div>
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div className="flex items-center space-x-3">
                <Phone className="h-5 w-5 text-green-600" />
                <div>
                  <h3 className="text-sm font-medium text-gray-900">
                    Password Reset
                  </h3>
                  <p className="text-sm text-gray-500">
                    jane.smith@example.com
                  </p>
                </div>
              </div>
              <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-yellow-800 bg-yellow-100">
                MEDIUM
              </span>
            </div>
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div className="flex items-center space-x-3">
                <HelpCircle className="h-5 w-5 text-purple-600" />
                <div>
                  <h3 className="text-sm font-medium text-gray-900">
                    Feature Request
                  </h3>
                  <p className="text-sm text-gray-500">
                    mike.wilson@example.com
                  </p>
                </div>
              </div>
              <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-green-800 bg-green-100">
                LOW
              </span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            Support Tools
          </h2>
          <div className="space-y-4">
            <button className="w-full flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <MessageCircle className="h-5 w-5 text-blue-600 mr-3" />
              <span className="text-sm font-medium text-gray-900">
                Live Chat
              </span>
            </button>
            <button className="w-full flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <Phone className="h-5 w-5 text-green-600 mr-3" />
              <span className="text-sm font-medium text-gray-900">
                Phone Support
              </span>
            </button>
            <button className="w-full flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <HelpCircle className="h-5 w-5 text-purple-600 mr-3" />
              <span className="text-sm font-medium text-gray-900">
                Knowledge Base
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Support;
