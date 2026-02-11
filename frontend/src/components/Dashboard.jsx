import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import axiosInstance from '../services/axiosConfig';
import ConfirmDialog from './ConfirmDialog';
import './styles/Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [pendingOrders, setPendingOrders] = useState(0);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalProducts, setTotalProducts] = useState(0);
  const [co2SavedKg, setCo2SavedKg] = useState(0);
  const [activeCustomers, setActiveCustomers] = useState(0);
  const [statsLoading, setStatsLoading] = useState(false);

  // Logout confirmation dialog
  const [confirmDialog, setConfirmDialog] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const currentUser = await authService.getCurrentUser();

        if (!currentUser) {
          navigate('/login');
          return;
        }

        setUser(currentUser);
      } catch (error) {
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  useEffect(() => {
    const supplierId = user?.userId ?? user?.supplierId;
    if (!supplierId) {
      return;
    }

    let cancelled = false;

    const loadStats = async () => {
      setStatsLoading(true);
      try {
        const [storesResult, listingsResult, co2Result] = await Promise.allSettled([
          axiosInstance.get(`/stores/supplier/${supplierId}`),
          axiosInstance.get(`/supplier/listings/supplier/${supplierId}`),
          axiosInstance.get(`/analytics/supplier/${supplierId}/co2?days=30`)
        ]);

        const stores = storesResult.status === 'fulfilled' && Array.isArray(storesResult.value?.data)
          ? storesResult.value.data
          : [];
        const allListings = listingsResult.status === 'fulfilled' && Array.isArray(listingsResult.value?.data)
          ? listingsResult.value.data
          : [];
        const co2Summary = co2Result.status === 'fulfilled' && !Array.isArray(co2Result.value?.data)
          ? (co2Result.value?.data || {})
          : {};
        const co2Total = Number(co2Summary?.totalCo2Kg ?? 0);

        const storeIds = stores
          .map((store) => store?.storeId)
          .filter((storeId) => Number.isFinite(Number(storeId)));

        const [pendingResults, completedResults] = await Promise.all([
          Promise.allSettled(
            storeIds.map((storeId) => axiosInstance.get(`/supplier/orders/${storeId}?status=PENDING`))
          ),
          Promise.allSettled(
            storeIds.map((storeId) => axiosInstance.get(`/supplier/orders/${storeId}?status=COMPLETED`))
          )
        ]);

        const pendingOrdersList = pendingResults.flatMap((result) => {
          if (result.status === 'fulfilled' && Array.isArray(result.value?.data)) {
            return result.value.data;
          }
          return [];
        });

        const completedOrdersList = completedResults.flatMap((result) => {
          if (result.status === 'fulfilled' && Array.isArray(result.value?.data)) {
            return result.value.data;
          }
          return [];
        });

        const pendingCount = pendingOrdersList.length;

        const revenue = completedOrdersList.reduce((sum, order) => {
          const amount = Number(order?.totalAmount ?? 0);
          return sum + (Number.isFinite(amount) ? amount : 0);
        }, 0);

        const listingCount = allListings.length;
        const activeCustomerCount = new Set(
          [...pendingOrdersList, ...completedOrdersList]
            .map((order) => order?.consumerId ?? order?.consumer?.consumerId)
            .filter((id) => id !== undefined && id !== null)
        ).size;

        if (!cancelled) {
          setPendingOrders(pendingCount);
          setTotalRevenue(revenue);
          setTotalProducts(listingCount);
          setCo2SavedKg(Number.isFinite(co2Total) ? co2Total : 0);
          setActiveCustomers(activeCustomerCount);
        }
      } catch (error) {
        if (!cancelled) {
          setPendingOrders(0);
          setTotalRevenue(0);
          setTotalProducts(0);
          setCo2SavedKg(0);
          setActiveCustomers(0);
        }
      } finally {
        if (!cancelled) {
          setStatsLoading(false);
        }
      }
    };

    loadStats();
    return () => {
      cancelled = true;
    };
  }, [user?.userId, user?.supplierId]);

  const handleLogout = async () => {
    await authService.logout();
    navigate('/login');
  };

  const goToListings = () => navigate('/listings');
  const goToOrders = () => navigate('/orders');
  const goToAnalytics = () => navigate('/analytics');
  const goToQRDecoder = () => navigate('/qr-decoder');

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
      {/* Top Navigation Bar */}
      <nav className="top-nav">
        <div className="nav-left">
          <h1 className="nav-title">Supplier Dashboard</h1>
        </div>
        <div className="nav-right">
          <span className="nav-welcome">
            <span className="welcome-icon">👤</span>
            Welcome, {user.displayName}!
          </span>
          <button onClick={() => setConfirmDialog(true)} className="btn-logout">
            Logout
          </button>
        </div>
      </nav>

      {/* Main Content */}
      <div className="dashboard-content">
        {/* Welcome Card */}
        <div className="welcome-card">
          <h2 className="welcome-title">Welcome back, {user.displayName}!</h2>
          <div className="welcome-info">
            <div className="info-item">
              <span className="info-label">Email:</span>
              <span className="info-value">{user.email}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Supplier ID:</span>
              <span className="info-value">{user.userId ?? user.supplierId}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Role:</span>
              <span className="info-value">{user.role}</span>
            </div>
          </div>
        </div>

        {/* Feature Cards Grid */}
        <div className="feature-cards-grid">
          <div className="feature-card">
            <div className="feature-icon">🏪</div>
            <h3 className="feature-title">Store Management</h3>
            <p className="feature-description">View and manage your store details</p>
            <Link to="/stores" className="btn-feature">
              Go to Stores
            </Link>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📦</div>
            <h3 className="feature-title">Orders</h3>
            <p className="feature-description">Track and manage orders</p>
            <button className="btn-feature" onClick={goToOrders}>View Orders</button>
          </div>

          <div className="feature-card">
            <div className="feature-icon">⚙️</div>
            <h3 className="feature-title">Settings</h3>
            <p className="feature-description">Manage your account</p>
            <button className="btn-feature">View Settings</button>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📊</div>
            <h3 className="feature-title">Analytics</h3>
            <p className="feature-description">View performance metrics</p>
            <button className="btn-feature" onClick={goToAnalytics}>View Analytics</button>
          </div>

          <div className="feature-card">
            <div className="feature-icon">🛒</div>
            <h3 className="feature-title">Listings</h3>
            <p className="feature-description">Create and manage rescue listings</p>
            <button className="btn-feature" onClick={goToListings}>View Listing</button>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📱</div>
            <h3 className="feature-title">QR Code Detector</h3>
            <p className="feature-description">Scan and decode pickup tokens</p>
            <button className="btn-feature" onClick={goToQRDecoder}>Scan QR Code</button>
          </div>
        </div>

        {/* Statistics Bar */}
        <div className="stats-bar">
          <div className="stat-item">
            <div className="stat-label">TOTAL LISTINGS</div>
            <div className="stat-value">{statsLoading ? '...' : totalProducts}</div>
          </div>

          <div className="stat-item">
            <div className="stat-label">PENDING ORDERS</div>
            <div className="stat-value">{statsLoading ? '...' : pendingOrders}</div>
          </div>

          <div className="stat-item">
            <div className="stat-label">TOTAL REVENUE</div>
            <div className="stat-value">{statsLoading ? '...' : `$${totalRevenue.toFixed(2)}`}</div>
          </div>

          <div className="stat-item">
            <div className="stat-label">CO2 SAVED (KG)</div>
            <div className="stat-value">{statsLoading ? '...' : `${co2SavedKg.toFixed(2)} kg`}</div>
          </div>

          <div className="stat-item">
            <div className="stat-label">ACTIVE CUSTOMERS</div>
            <div className="stat-value">{statsLoading ? '...' : activeCustomers}</div>
          </div>
        </div>
      </div>

      <ConfirmDialog
        isOpen={confirmDialog}
        onClose={() => setConfirmDialog(false)}
        onConfirm={handleLogout}
        title="确认登出"
        message="您确定要登出吗？"
      />
    </div>
  );
};

export default Dashboard;
