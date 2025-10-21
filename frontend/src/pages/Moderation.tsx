import React from "react";
import { CheckCircle, XCircle, Clock, AlertCircle } from "lucide-react";

const Moderation: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h1 className="text-2xl font-bold text-gray-900">Content Moderation</h1>
        <p className="text-gray-600 mt-2">
          Manage product approvals, user verifications, and content reviews.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Clock className="h-8 w-8 text-orange-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Pending Reviews
                </dt>
                <dd className="text-lg font-medium text-gray-900">12</dd>
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
                <dd className="text-lg font-medium text-gray-900">8</dd>
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
                <dd className="text-lg font-medium text-gray-900">2</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <AlertCircle className="h-8 w-8 text-yellow-600" />
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  Flagged Content
                </dt>
                <dd className="text-lg font-medium text-gray-900">3</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-lg font-medium text-gray-900 mb-4">
          Recent Moderation Actions
        </h2>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <h3 className="text-sm font-medium text-gray-900">
                Product Approval
              </h3>
              <p className="text-sm text-gray-500">
                Wireless Headphones - Approved
              </p>
            </div>
            <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-green-800 bg-green-100">
              APPROVED
            </span>
          </div>
          <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <h3 className="text-sm font-medium text-gray-900">
                User Verification
              </h3>
              <p className="text-sm text-gray-500">
                Premium account verification
              </p>
            </div>
            <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-green-800 bg-green-100">
              VERIFIED
            </span>
          </div>
          <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <h3 className="text-sm font-medium text-gray-900">
                Content Review
              </h3>
              <p className="text-sm text-gray-500">
                Product description flagged
              </p>
            </div>
            <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full text-red-800 bg-red-100">
              REJECTED
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Moderation;
