import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import axiosInstance from '../services/axiosConfig';
import './AnalyticsPage.css';

const AnalyticsPage = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const loadTopProducts = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        if (!currentUser?.supplierId) {
          navigate('/login');
          return;
        }

        const response = await axiosInstance.get(
          `/analytics/supplier/${currentUser.supplierId}/top-products?limit=3`
        );

        if (!cancelled) {
          setItems(response.data || []);
        }
      } catch (err) {
        if (!cancelled) {
          setError('Unable to load analytics data');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadTopProducts();
    return () => {
      cancelled = true;
    };
  }, [navigate]);

  return (
    <div className="analytics-page">
      <div className="analytics-header">
        <div>
          <span className="analytics-tag">Supplier</span>
          <h1>Analytics</h1>
          <p>Top Selling Products</p>
        </div>
        <button className="analytics-btn" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>

      {loading ? (
        <div className="analytics-empty">Loading...</div>
      ) : error ? (
        <div className="analytics-empty">{error}</div>
      ) : items.length === 0 ? (
        <div className="analytics-empty">No sales data available</div>
      ) : (
        <div className="analytics-grid">
          {items.map((item, index) => (
            <div className="analytics-card" key={item.listingId || index}>
              <div className="analytics-rank">Top {index + 1}</div>
              <h3>{item.title}</h3>
              <div className="analytics-metric">
                <span>Quantity Sold</span>
                <strong>{item.totalQuantity}</strong>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AnalyticsPage;
