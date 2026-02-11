import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import axiosInstance from '../services/axiosConfig';
import './SettingsPage.css';

const SettingsPage = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Profile form state
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

  // Password form state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        if (!currentUser) {
          navigate('/login');
          return;
        }
        setUser(currentUser);
        setDisplayName(currentUser.displayName || '');
        setEmail(currentUser.email || '');
        setPhoneNumber(currentUser.phoneNumber || '');
      } catch (err) {
        setError('Failed to load user information');
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setSaving(true);

    try {
      const supplierId = user?.userId ?? user?.supplierId;
      if (!supplierId) {
        throw new Error('User ID not found');
      }

      await axiosInstance.put(`/suppliers/${supplierId}`, {
        displayName,
        email,
        phoneNumber
      });

      setSuccessMessage('Profile updated successfully!');

      // Update local user state
      const updatedUser = { ...user, displayName, email, phoneNumber };
      setUser(updatedUser);
      authService.setUser(updatedUser);

      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    // Validate passwords
    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError('All password fields are required');
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError('New passwords do not match');
      return;
    }

    if (newPassword.length < 8) {
      setPasswordError('New password must be at least 8 characters');
      return;
    }

    setSaving(true);

    try {
      const supplierId = user?.userId ?? user?.supplierId;
      if (!supplierId) {
        throw new Error('User ID not found');
      }

      await axiosInstance.put(`/suppliers/${supplierId}/password`, {
        currentPassword,
        newPassword
      });

      setPasswordSuccess('Password changed successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');

      setTimeout(() => setPasswordSuccess(''), 3000);
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Failed to change password');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="settings-page">
        <div className="settings-loading">Loading settings...</div>
      </div>
    );
  }

  return (
    <div className="settings-page">
      {/* Header Banner */}
      <div className="settings-header-banner">
        <div className="banner-content">
          <div className="banner-left">
            <span className="banner-tag">SUPPLIER</span>
            <h1 className="banner-title">Settings</h1>
          </div>
          <div className="banner-right">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="btn-back-dashboard"
            >
              ← Back to Dashboard
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="settings-content">
        {/* Account Information Card */}
        <div className="settings-card">
          <div className="settings-card-header">
            <h2>Account Information</h2>
          </div>
          <div className="settings-card-body">
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">Supplier ID</span>
                <span className="info-value">{user?.userId ?? user?.supplierId}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Role</span>
                <span className="info-value">{user?.role}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Account Status</span>
                <span className="status-badge status-active">Active</span>
              </div>
            </div>
          </div>
        </div>

        {/* Profile Settings Card */}
        <div className="settings-card">
          <div className="settings-card-header">
            <h2>Profile Settings</h2>
            <p className="card-subtitle">Update your personal information</p>
          </div>
          <div className="settings-card-body">
            {error && <div className="error-message">❌ {error}</div>}
            {successMessage && <div className="success-message">✅ {successMessage}</div>}

            <form onSubmit={handleProfileUpdate} className="settings-form">
              <div className="form-group">
                <label htmlFor="displayName">Display Name</label>
                <input
                  type="text"
                  id="displayName"
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Email Address</label>
                <input
                  type="email"
                  id="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="phoneNumber">Phone Number</label>
                <input
                  type="tel"
                  id="phoneNumber"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="Optional"
                />
              </div>

              <div className="form-actions">
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={saving}
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Change Password Card */}
        <div className="settings-card">
          <div className="settings-card-header">
            <h2>Change Password</h2>
            <p className="card-subtitle">Update your account password</p>
          </div>
          <div className="settings-card-body">
            {passwordError && <div className="error-message">❌ {passwordError}</div>}
            {passwordSuccess && <div className="success-message">✅ {passwordSuccess}</div>}

            <form onSubmit={handlePasswordChange} className="settings-form">
              <div className="form-group">
                <label htmlFor="currentPassword">Current Password</label>
                <input
                  type="password"
                  id="currentPassword"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="newPassword">New Password</label>
                <input
                  type="password"
                  id="newPassword"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  minLength={8}
                />
                <small className="form-hint">Minimum 8 characters</small>
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm New Password</label>
                <input
                  type="password"
                  id="confirmPassword"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  minLength={8}
                />
              </div>

              <div className="form-actions">
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={saving}
                >
                  {saving ? 'Changing...' : 'Change Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;
