import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './styles/Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        
        if (!currentUser) {
          console.log('No user found, redirecting to login');
          navigate('/login');
          return;
        }
        
        console.log('User loaded:', currentUser);
        setUser(currentUser);
      } catch (error) {
        console.error('Error fetching user:', error);
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  const handleLogout = async () => {
    const confirmLogout = window.confirm('Are you sure you want to logout?');
    if (confirmLogout) {
      await authService.logout();
      navigate('/login');
    }
  };

  const goToListings = () => navigate('/listings');

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner-large"></div>
        <p>Loading dashboard...</p>
      </div>
    );
  }

  if (!user) {
    return null; // safety check
  }

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-left">
          <h1>Supplier Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-greeting">Welcome, {user.displayName}!</span>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </header>

      {/* Main Content */}
      <div className="dashboard-content">
        {/* Welcome Section */}
        <div className="welcome-section">
          <h2>Welcome back, {user.displayName}!</h2>
          <div className="user-info">
            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Supplier ID:</strong> {user.supplierId}</p>
            <p><strong>Role:</strong> {user.role}</p>
            {user.businessName && <p><strong>Business:</strong> {user.businessName}</p>}
            {user.phone && <p><strong>Phone:</strong> {user.phone}</p>}
          </div>
        </div>

        {/* Dashboard Cards */}
        <div className="dashboard-cards">
          <div className="card">
            <div className="card-icon">📦</div>
            <h3>Products</h3>
            <p>Manage your product catalog</p>
            <button className="btn-card">View Products</button>
          </div>
          <div className="card">
            <div className="card-icon">📊</div>
            <h3>Orders</h3>
            <p>Track and manage orders</p>
            <button className="btn-card">View Orders</button>
          </div>
          <div className="card">
            <div className="card-icon">💰</div>
            <h3>Revenue</h3>
            <p>View your earnings</p>
            <button className="btn-card">View Revenue</button>
          </div>
          <div className="card">
            <div className="card-icon">⚙️</div>
            <h3>Settings</h3>
            <p>Manage your account</p>
            <button className="btn-card">View Settings</button>
          </div>
          <div className="card">
            <div className="card-icon">📈</div>
            <h3>Analytics</h3>
            <p>View performance metrics</p>
            <button className="btn-card">View Analytics</button>
          </div>
          <div className="card">
            <div className="card-icon">👥</div>
            <h3>Customers</h3>
            <p>Manage customer relationships</p>
            <button className="btn-card">View Customers</button>
          </div>
          <div className="card">
            <div className="card-icon">🧺</div>
            <h3>Listings</h3>
            <p>Create and manage rescue listings</p>
            <button className="btn-card" onClick={goToListings}>Create Listing</button>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="quick-stats">
          <div className="stat-card">
            <h4>Total Products</h4>
            <p className="stat-value">0</p>
          </div>
          <div className="stat-card">
            <h4>Pending Orders</h4>
            <p className="stat-value">0</p>
          </div>
          <div className="stat-card">
            <h4>Total Revenue</h4>
            <p className="stat-value">$0.00</p>
          </div>
          <div className="stat-card">
            <h4>Active Customers</h4>
            <p className="stat-value">0</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
