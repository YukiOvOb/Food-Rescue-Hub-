import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../services/axiosConfig';
import authService from '../services/authService';
import PageHeader from '../components/PageHeader';
import './OrdersPage.css';

const OrdersPage = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [stores, setStores] = useState([]);
  const [selectedStoreId, setSelectedStoreId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [storeLoading, setStoreLoading] = useState(true);

  const normalizeOrders = (payload) => {
    if (Array.isArray(payload)) return payload;
    if (Array.isArray(payload?.data)) return payload.data;
    if (Array.isArray(payload?.orders)) return payload.orders;
    return [];
  };

  const sortOrders = (items) => {
    return [...items].sort((a, b) => (b?.orderId || 0) - (a?.orderId || 0));
  };

  const dateTimeFormatter = new Intl.DateTimeFormat('en-US', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  });

  const timeFormatter = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  });

  const toDate = (value) => {
    if (!value) return null;
    if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
    if (typeof value !== 'string') return null;

    let normalized = value.trim();
    if (!normalized) return null;

    normalized = normalized.replace(' ', 'T');
    normalized = normalized.replace(/(\.\d{3})\d+/, '$1');

    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) {
      normalized = `${normalized}:00`;
    }

    let parsed = new Date(normalized);
    if (!Number.isNaN(parsed.getTime())) return parsed;

    const match = normalized.match(
      /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,3}))?)?$/
    );
    if (!match) return null;

    const [, y, m, d, hh, mm, ss = '0', ms = '0'] = match;
    parsed = new Date(
      Number(y),
      Number(m) - 1,
      Number(d),
      Number(hh),
      Number(mm),
      Number(ss),
      Number(ms.padEnd(3, '0'))
    );
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  };

  const formatDateTime = (value) => {
    const date = toDate(value);
    return date ? dateTimeFormatter.format(date) : '-';
  };

  const formatPickupSlot = (start, end) => {
    const startDate = toDate(start);
    const endDate = toDate(end);

    if (!startDate && !endDate) return '-';
    if (startDate && !endDate) return formatDateTime(startDate);
    if (!startDate && endDate) return formatDateTime(endDate);

    const sameDay =
      startDate.getFullYear() === endDate.getFullYear() &&
      startDate.getMonth() === endDate.getMonth() &&
      startDate.getDate() === endDate.getDate();

    if (sameDay) {
      return `${formatDateTime(startDate)} - ${timeFormatter.format(endDate)}`;
    }

    return `${formatDateTime(startDate)} - ${formatDateTime(endDate)}`;
  };

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      const response = await axios.patch(`/orders/${orderId}/status`, null, {
        params: { status: newStatus }
      });
      console.log(`Order ${orderId} updated to ${newStatus}`);
      fetchOrders(selectedStoreId);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update order status');
    }
  };

  const fetchOrders = async (storeId) => {
    setLoading(true);
    setError(null);

    try {
      if (!storeId) {
        setOrders([]);
        return;
      }
      const response = await axios.get(`/supplier/orders/${storeId}`);
      const normalizedOrders = normalizeOrders(response?.data);
      setOrders(sortOrders(normalizedOrders));
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const fetchStores = async () => {
    setStoreLoading(true);
    setError(null);
    try {
      const storedUser = authService.getStoredUser();
      const supplierIdFromStorage = storedUser?.userId ?? storedUser?.supplierId;
      let supplierId = supplierIdFromStorage;

      if (!supplierId) {
        const meResponse = await axios.get('/auth/me');
        supplierId = meResponse?.data?.userId ?? meResponse?.data?.supplierId;
      }
      if (!supplierId) {
        setStores([]);
        setSelectedStoreId(null);
        return;
      }
      const storesResponse = await axios.get(`/stores/supplier/${supplierId}`);
      const list = Array.isArray(storesResponse?.data) ? storesResponse.data : [];
      setStores(list);
      if (list.length === 1) {
        setSelectedStoreId(list[0].storeId);
        fetchOrders(list[0].storeId);
      } else {
        setSelectedStoreId(null);
        setOrders([]);
      }
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to load stores');
    } finally {
      setStoreLoading(false);
    }
  };

  useEffect(() => {
    fetchStores();
  }, []);

  return (
    <div className="orders-page">
      {/* Header Banner */}
      <div className="orders-header-banner">
        <div className="banner-content">
          <div className="banner-left">
            <span className="banner-tag">SUPPLIER</span>
            <h1 className="banner-title">Orders</h1>
          </div>
          <div className="banner-right">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="btn-back-dashboard"
            >
              ← Back to Dashboard
            </button>
            <button
              className="btn-refresh"
              onClick={() => fetchOrders(selectedStoreId)}
              disabled={loading || !selectedStoreId}
            >
              {loading ? 'Loading...' : 'Refresh'}
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="orders-content">
        {/* Store Selector */}
        {!storeLoading && stores.length > 1 && (
          <div className="store-selector">
            {stores.map((store) => (
              <button
                key={store.storeId}
                className={`store-chip ${selectedStoreId === store.storeId ? 'active' : ''}`}
                onClick={() => {
                  setSelectedStoreId(store.storeId);
                  fetchOrders(store.storeId);
                }}
              >
                {store.storeName}
              </button>
            ))}
          </div>
        )}

        {!storeLoading && stores.length === 1 && (
          <div className="single-store-info">
            <span className="store-label">Store:</span> {stores[0].storeName}
          </div>
        )}

        {!storeLoading && stores.length === 0 && (
          <div className="empty-state">No stores found for this supplier.</div>
        )}

        {error && <div className="error-message">❌ {error}</div>}

        {/* Orders Table */}
        {selectedStoreId && (
          <div className="orders-table-wrapper">
            <table className="orders-table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Status</th>
                  <th>Total</th>
                  <th>Currency</th>
                  <th>Store</th>
                  <th>Consumer</th>
                  <th>Pickup Token</th>
                  <th>Pickup Slot</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {!loading && (!Array.isArray(orders) || orders.length === 0) && (
                  <tr>
                    <td colSpan="9" className="empty-cell">
                      No orders found.
                    </td>
                  </tr>
                )}

                {Array.isArray(orders) && orders.map((order) => (
                  <tr key={order.orderId}>
                    <td><span className="order-id">#{order.orderId}</span></td>
                    <td>
                      <span className={`status-badge status-${order.status?.toLowerCase()}`}>
                        {order.status}
                      </span>
                    </td>
                    <td><strong>${order.totalAmount}</strong></td>
                    <td>{order.currency || 'SGD'}</td>
                    <td>{order.store?.storeName || '-'}</td>
                    <td>{order.consumer?.displayName || order.consumer?.username || '-'}</td>
                    <td className="pickup-token-cell">
                      {(order.pickupToken?.qrTokenHash || order.pickupTokenHash) ? (
                        <span
                          className="token-hash"
                          title={`Token: ${order.pickupToken?.qrTokenHash || order.pickupTokenHash}\nExpires: ${order.pickupToken?.expiresAt || order.pickupTokenExpiresAt || 'N/A'}`}
                        >
                          {(order.pickupToken?.qrTokenHash || order.pickupTokenHash).substring(0, 8)}...
                        </span>
                      ) : (
                        <span className="token-none">No token</span>
                      )}
                    </td>
                    <td className="pickup-slot-cell">
                      {formatPickupSlot(order.pickupSlotStart, order.pickupSlotEnd)}
                    </td>
                    <td>{formatDateTime(order.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;
