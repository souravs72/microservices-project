import React, { useState, useEffect } from "react";
import {
  User,
  Mail,
  Calendar,
  Edit,
  Save,
  X,
  Camera,
  Shield,
  Upload,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { usersAPI } from "../services/api";

const Profile: React.FC = () => {
  const { user, updateUser } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    username: "",
    phone: "",
    address: "",
    bio: "",
    profilePictureUrl: "",
  });

  const [profilePicture, setProfilePicture] = useState<File | null>(null);
  const [profilePicturePreview, setProfilePicturePreview] = useState<
    string | null
  >(null);

  // Fetch user details
  const fetchUserDetails = async () => {
    console.log("fetchUserDetails called, user:", user);

    // If no user ID, try to get it from the username
    let userId = user?.id;
    if (!userId && user?.username) {
      console.log(
        "No user ID found, trying to get user by username:",
        user.username
      );
      try {
        // Get user by username to find the ID
        const response = await usersAPI.getByUsername(user.username);
        const foundUser = response.data;
        if (foundUser) {
          userId = foundUser.id?.toString();
          console.log("Found user ID:", userId);
        }
      } catch (err) {
        console.error("Error finding user by username:", err);
      }
    }

    if (!userId) {
      console.log("No user ID available, returning early");
      return;
    }

    try {
      console.log("Fetching user details for ID:", userId);
      setLoading(true);
      const response = await usersAPI.getById(Number(userId));
      const userData = response.data;
      console.log("Fetched user data:", userData);

      setFormData((prev) => ({
        ...prev,
        firstName: userData.firstName || "",
        lastName: userData.lastName || "",
        email: userData.email || "",
        username: userData.username || "",
        phone: userData.phone || "",
        address: userData.address || "",
        bio: userData.bio || "",
        profilePictureUrl: userData.profilePictureUrl || "",
      }));

      if (userData.profilePictureUrl) {
        setProfilePicturePreview(userData.profilePictureUrl);
      }
      console.log("Form data updated with fetched data");
    } catch (err: any) {
      console.error("Error fetching user details:", err);
      setError(err.response?.data?.message || "Failed to fetch user details");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserDetails();
  }, [user?.id]);

  // Update form data when user data changes
  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        email: user.email || "",
        username: user.username || "",
        phone: user.phone || "",
        address: user.address || "",
        bio: user.bio || "",
        profilePictureUrl: user.profilePictureUrl || "",
      });

      if (user.profilePictureUrl) {
        setProfilePicturePreview(user.profilePictureUrl);
      }
    }
  }, [user]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfilePicture(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setProfilePicturePreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = async () => {
    console.log("handleSave called");
    console.log("user:", user);
    console.log("formData:", formData);

    // If no user ID, try to get it from the username
    let userId = user?.id;
    if (!userId && user?.username) {
      console.log(
        "No user ID found, trying to get user by username:",
        user.username
      );
      try {
        // Get user by username to find the ID
        const response = await usersAPI.getByUsername(user.username);
        const foundUser = response.data;
        if (foundUser) {
          userId = foundUser.id?.toString();
          console.log("Found user ID:", userId);
        }
      } catch (err) {
        console.error("Error finding user by username:", err);
      }
    }

    if (!userId) {
      console.log("No user ID found");
      setError("Unable to identify user. Please try logging in again.");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setSuccess(null);

      // Upload profile picture if selected
      if (profilePicture) {
        console.log("Uploading profile picture...");
        try {
          const uploadResponse = await usersAPI.uploadProfilePicture(
            Number(userId),
            profilePicture
          );
          formData.profilePictureUrl = uploadResponse.data.profilePictureUrl;
          console.log(
            "Profile picture uploaded:",
            uploadResponse.data.profilePictureUrl
          );
        } catch (uploadErr: any) {
          console.warn("Profile picture upload failed:", uploadErr);
          setError(
            "Failed to upload profile picture: " +
              (uploadErr.response?.data?.message || uploadErr.message)
          );
          return; // Stop execution if picture upload fails
        }
      }

      // Prepare update data - only include fields supported by the API
      const updateData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        phone: formData.phone || null,
        address: formData.address || null,
        bio: formData.bio || null,
        profilePictureUrl: formData.profilePictureUrl || null,
      };

      console.log("Sending update data:", updateData);
      const response = await usersAPI.update(Number(userId), updateData);
      console.log("Update response:", response.data);

      // Update local user data
      updateUser({
        ...user,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        email: response.data.email,
      });

      setSuccess("Profile updated successfully");
      setIsEditing(false);
      setProfilePicture(null);
    } catch (err: any) {
      console.error("Profile update error:", err);
      setError(err.response?.data?.message || "Failed to update profile");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setError(null);
    setSuccess(null);
    setProfilePicture(null);
    setProfilePicturePreview(user?.profilePictureUrl || null);
    // Reset form data to current user data
    setFormData({
      firstName: user?.firstName || "",
      lastName: user?.lastName || "",
      email: user?.email || "",
      username: user?.username || "",
      phone: user?.phone || "",
      address: user?.address || "",
      bio: user?.bio || "",
      profilePictureUrl: user?.profilePictureUrl || "",
    });
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  if (loading && !isEditing) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center">
            <User className="h-6 w-6 mr-2" />
            My Profile
          </h1>
          <p className="text-gray-600 mt-1">
            Manage your account information and preferences
          </p>
        </div>
        <div className="flex space-x-3">
          {!isEditing ? (
            <button
              onClick={() => setIsEditing(true)}
              className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700"
            >
              <Edit className="h-4 w-4 mr-2" />
              Edit Profile
            </button>
          ) : (
            <div className="flex space-x-2">
              <button
                onClick={handleCancel}
                className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              >
                <X className="h-4 w-4 mr-2" />
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={loading}
                className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50"
              >
                <Save className="h-4 w-4 mr-2" />
                {loading ? "Saving..." : "Save Changes"}
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Error/Success Messages */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <X className="h-5 w-5 text-red-400" />
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error</h3>
              <p className="text-sm text-red-700 mt-1">{error}</p>
            </div>
          </div>
        </div>
      )}

      {success && (
        <div className="bg-green-50 border border-green-200 rounded-md p-4">
          <div className="flex">
            <Save className="h-5 w-5 text-green-400" />
            <div className="ml-3">
              <h3 className="text-sm font-medium text-green-800">Success</h3>
              <p className="text-sm text-green-700 mt-1">{success}</p>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Profile Overview */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="text-center">
              <div className="relative inline-block">
                {profilePicturePreview ? (
                  <img
                    src={
                      profilePicturePreview.startsWith("http")
                        ? profilePicturePreview
                        : `${window.location.origin}${profilePicturePreview}`
                    }
                    alt="Profile"
                    className="h-24 w-24 rounded-full object-cover mx-auto"
                    onError={(e) => {
                      console.log(
                        "Image failed to load:",
                        profilePicturePreview
                      );
                      e.currentTarget.style.display = "none";
                    }}
                  />
                ) : user?.profilePictureUrl ? (
                  <img
                    src={
                      user.profilePictureUrl.startsWith("http")
                        ? user.profilePictureUrl
                        : `${window.location.origin}${user.profilePictureUrl}`
                    }
                    alt="Profile"
                    className="h-24 w-24 rounded-full object-cover mx-auto"
                    onError={(e) => {
                      console.log(
                        "Image failed to load:",
                        user.profilePictureUrl
                      );
                      e.currentTarget.style.display = "none";
                    }}
                  />
                ) : (
                  <div className="h-24 w-24 rounded-full bg-indigo-600 flex items-center justify-center mx-auto">
                    <span className="text-2xl font-bold text-white">
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </span>
                  </div>
                )}
                {isEditing && (
                  <label className="absolute bottom-0 right-0 h-8 w-8 rounded-full bg-white border-2 border-gray-300 flex items-center justify-center hover:bg-gray-50 cursor-pointer">
                    <Camera className="h-4 w-4 text-gray-600" />
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleFileChange}
                      className="hidden"
                    />
                  </label>
                )}
              </div>
              <h2 className="mt-4 text-xl font-semibold text-gray-900">
                {user?.firstName} {user?.lastName}
              </h2>
              <p className="text-gray-600">@{user?.username}</p>
              <div className="mt-4 flex items-center justify-center text-sm text-gray-500">
                <Shield className="h-4 w-4 mr-1" />
                {user?.roles?.[0] || "User"}
              </div>
            </div>

            <div className="mt-6 space-y-4">
              <div className="flex items-center text-sm text-gray-600">
                <Calendar className="h-4 w-4 mr-2" />
                <span>
                  Member since{" "}
                  {user?.createdAt ? formatDate(user.createdAt) : "N/A"}
                </span>
              </div>
              <div className="flex items-center text-sm text-gray-600">
                <Mail className="h-4 w-4 mr-2" />
                <span>{user?.email}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Profile Details */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-sm">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">
                Profile Information
              </h3>
            </div>

            <div className="p-6 space-y-6">
              {/* Basic Information */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-4">
                  Basic Information
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      First Name
                    </label>
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Last Name
                    </label>
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Username
                    </label>
                    <input
                      type="text"
                      name="username"
                      value={formData.username}
                      disabled
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-gray-50 text-gray-500"
                    />
                    <p className="mt-1 text-xs text-gray-500">
                      Username cannot be changed
                    </p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                    />
                  </div>
                </div>
              </div>

              {/* Contact Information */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-4">
                  Contact Information
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Phone
                    </label>
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      placeholder="Enter phone number"
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Address
                    </label>
                    <input
                      type="text"
                      name="address"
                      value={formData.address}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      placeholder="Enter your address"
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                    />
                  </div>
                </div>
              </div>

              {/* Bio */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Bio
                </label>
                <textarea
                  name="bio"
                  rows={4}
                  value={formData.bio}
                  onChange={handleInputChange}
                  disabled={!isEditing}
                  placeholder="Tell us about yourself..."
                  className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
                />
              </div>

              {/* Profile Picture Upload */}
              {isEditing && (
                <div>
                  <h4 className="text-md font-medium text-gray-900 mb-4">
                    Profile Picture
                  </h4>
                  <div className="space-y-4">
                    <div className="flex items-center space-x-4">
                      <div className="flex-shrink-0">
                        {profilePicturePreview ? (
                          <img
                            src={
                              profilePicturePreview.startsWith("http")
                                ? profilePicturePreview
                                : `${window.location.origin}${profilePicturePreview}`
                            }
                            alt="Profile preview"
                            className="h-16 w-16 rounded-full object-cover"
                            onError={(e) => {
                              console.log(
                                "Preview image failed to load:",
                                profilePicturePreview
                              );
                              e.currentTarget.style.display = "none";
                            }}
                          />
                        ) : (
                          <div className="h-16 w-16 rounded-full bg-gray-200 flex items-center justify-center">
                            <User className="h-8 w-8 text-gray-400" />
                          </div>
                        )}
                      </div>
                      <div className="flex-1">
                        <label className="cursor-pointer">
                          <div className="flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
                            <Upload className="h-4 w-4 mr-2" />
                            Choose Profile Picture
                          </div>
                          <input
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                            className="hidden"
                          />
                        </label>
                        <p className="mt-1 text-xs text-gray-500">
                          JPG, PNG or GIF. Max size 2MB.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Note about password changes */}
              {isEditing && (
                <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
                  <div className="flex">
                    <Shield className="h-5 w-5 text-blue-400" />
                    <div className="ml-3">
                      <h3 className="text-sm font-medium text-blue-800">
                        Password Changes
                      </h3>
                      <p className="text-sm text-blue-700 mt-1">
                        To change your password, please contact your
                        administrator or use the password reset feature on the
                        login page.
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
