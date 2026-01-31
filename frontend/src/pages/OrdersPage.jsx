import React, { useEffect, useState } from 'react';
import axios from '../services/axiosConfig';
import './OrdersPage.css';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await axios.get('/orders');
      setOrders(response.data || []);
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  return (
    <div className="orders-page">
      <div className="orders-header">
        <h2>Orders</h2>
        <button className="orders-refresh" onClick={fetchOrders} disabled={loading}>
          {loading ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      {error && <div className="orders-error">❌ {error}</div>}

      <div className="orders-table">
        <div className="orders-row orders-head">
          <div>Order ID</div>
          <div>Status</div>
          <div>Total</div>
          <div>Currency</div>
          <div>Store</div>
          <div>Consumer</div>
          <div>Pickup Slot</div>
          <div>Created</div>
        </div>

        {!loading && orders.length === 0 && (
          <div className="orders-empty">No orders found.</div>
        )}

        {orders.map((order) => (
          <div className="orders-row" key={order.orderId}>
            <div>{order.orderId}</div>
            <div>{order.status}</div>
            <div>{order.totalAmount}</div>
            <div>{order.currency}</div>
            <div>{order.store?.storeId ?? '-'}</div>
            <div>{order.consumer?.consumerId ?? '-'}</div>
            <div>
              {order.pickupSlotStart ? `${order.pickupSlotStart} - ${order.pickupSlotEnd || ''}` : '-'}
            </div>
            <div>{order.createdAt || '-'}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OrdersPage;
