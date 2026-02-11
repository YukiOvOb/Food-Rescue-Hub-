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
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-left">
          <h1>Supplier Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-greeting">Welcome, {user.displayName}!</span>
          <button onClick={() => setConfirmDialog(true)} className="btn-logout">
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
            <p><strong>Supplier ID:</strong> {user.userId ?? user.supplierId}</p>
            <p><strong>Role:</strong> {user.role}</p>
            {user.businessName && <p><strong>Business:</strong> {user.businessName}</p>}
            {user.phone && <p><strong>Phone:</strong> {user.phone}</p>}
          </div>
        </div>

        {/* Dashboard Cards */}
        <div className="dashboard-cards">
          <div className="card">
            <div className="card-icon">🏪</div>
            <h3>Store Management</h3>
            <p>View and manage your store details</p>
            <Link to="/stores" className="btn-card" style={{ textDecoration: 'none', textAlign: 'center' }}>
              Go to Stores
            </Link>
          </div>
          <div className="card">
            <div className="card-icon">📊</div>
            <h3>Orders</h3>
            <p>Track and manage orders</p>
            <button className="btn-card" onClick={goToOrders}>View Orders</button>
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
            <button className="btn-card" onClick={goToAnalytics}>View Analytics</button>
          </div>
          <div className="card">
            <div className="card-icon">🧺</div>
            <h3>Listings</h3>
            <p>Create and manage rescue listings</p>
            <button className="btn-card" onClick={goToListings}>View Listing</button>
          </div>
          <div className="card">
            <div className="card-icon">📱</div>
            <h3>QR Code Detector</h3>
            <p>Scan and decode pickup tokens</p>
            <button className="btn-card" onClick={goToQRDecoder}>Scan QR Code</button>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="quick-stats">
          <div className="stat-card">
            <h4>Total Listings</h4>
            <p className="stat-value">{statsLoading ? '...' : totalProducts}</p>
          </div>
          <div className="stat-card">
            <h4>Pending Orders</h4>
            <p className="stat-value">{statsLoading ? '...' : pendingOrders}</p>
          </div>
          <div className="stat-card">
            <h4>Total Revenue</h4>
            <p className="stat-value">
              {statsLoading ? '...' : `$${totalRevenue.toFixed(2)}`}
            </p>
          </div>
          <div className="stat-card">
            <h4>CO2 Saved (30d)</h4>
            <p className="stat-value">
              {statsLoading ? '...' : `${co2SavedKg.toFixed(2)} kg`}
            </p>
          </div>
          <div className="stat-card">
            <h4>Active Customers</h4>
            <p className="stat-value">{statsLoading ? '...' : activeCustomers}</p>
          </div>
        </div>
      </div>

      <ConfirmDialog
        isOpen={confirmDialog}
        onClose={() => setConfirmDialog(false)}
        onConfirm={handleLogout}
        title="Confirm Logout"
        message="Are you sure you want to logout?"
        confirmText="Logout"
        cancelText="Cancel"
        type="warning"
      />
    </div>
  );
};

export default Dashboard;
