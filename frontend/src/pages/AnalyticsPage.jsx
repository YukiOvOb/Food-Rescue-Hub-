import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import axiosInstance from '../services/axiosConfig';
import './AnalyticsPage.css';

const AnalyticsPage = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [co2Summary, setCo2Summary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const loadTopProducts = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        const supplierId = currentUser?.userId ?? currentUser?.supplierId;
        if (!supplierId) {
          navigate('/login');
          return;
        }

        const [topResponse, co2Response] = await Promise.all([
          axiosInstance.get(`/analytics/supplier/${supplierId}/top-products?limit=3`),
          axiosInstance.get(`/analytics/supplier/${supplierId}/co2?days=30`)
        ]);

        if (!cancelled) {
          setItems(topResponse.data || []);
          setCo2Summary(co2Response.data || null);
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

  const handleDownloadPdf = async () => {
    try {
      const currentUser = await authService.getCurrentUser();
      const supplierId = currentUser?.userId ?? currentUser?.supplierId;
      if (!supplierId) {
        navigate('/login');
        return;
      }

      const response = await axiosInstance.get(
        `/analytics/supplier/${supplierId}/co2/report?days=30`,
        { responseType: 'blob' }
      );

      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `co2-report-supplier-${supplierId}-last-30-days.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Unable to download CO2 report');
    }
  };

  return (
    <div className="analytics-page">
      {/* Header Banner */}
      <div className="analytics-header-banner">
        <div className="banner-content">
          <div className="banner-left">
            <span className="banner-tag">SUPPLIER</span>
            <h1 className="banner-title">Analytics</h1>
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
      <div className="analytics-content">
        {loading ? (
          <div className="analytics-empty">Loading...</div>
        ) : error ? (
          <div className="analytics-empty">{error}</div>
        ) : (
          <>
          {co2Summary && (
            <>
              {/* CO2 Savings Section */}
              <div className="analytics-section">
                <div className="analytics-section-header">
                  <h2 className="analytics-section-title">CO2 Impact Summary</h2>
                  <div className="analytics-rank">Last 30 Days</div>
                </div>
                <div className="analytics-grid">
                  <div className="analytics-card analytics-highlight">
                    <div className="analytics-icon">🌱</div>
                    <h3>CO2 Savings</h3>
                    <div className="analytics-metric">
                      <span>Total CO2 Saved</span>
                      <strong>{Number(co2Summary.totalCo2Kg ?? 0).toFixed(2)} kg</strong>
                    </div>
                    <div className="analytics-metric">
                      <span>Total Weight Rescued</span>
                      <strong>{Number(co2Summary.totalWeightKg ?? 0).toFixed(2)} kg</strong>
                    </div>
                    <button className="analytics-btn" onClick={handleDownloadPdf}>
                      📄 Download CO2 Report
                    </button>
                  </div>
                </div>
              </div>

              {/* CO2 Breakdown Section */}
              <div className="analytics-section">
                <div className="analytics-section-header">
                  <h2 className="analytics-section-title">CO2 Breakdown by Category</h2>
                </div>
                {Array.isArray(co2Summary.categories) && co2Summary.categories.length > 0 ? (
                  <div className="analytics-category-grid">
                    {co2Summary.categories.map((row) => (
                      <div className="analytics-category-card" key={row.categoryId}>
                        <div className="category-name">{row.categoryName}</div>
                        <div className="category-stats">
                          <div className="category-stat">
                            <span className="stat-label">Weight</span>
                            <span className="stat-value">{Number(row.totalWeightKg ?? 0).toFixed(2)} kg</span>
                          </div>
                          <div className="category-stat-divider"></div>
                          <div className="category-stat">
                            <span className="stat-label">CO2 Saved</span>
                            <span className="stat-value stat-highlight">{Number(row.totalCo2Kg ?? 0).toFixed(2)} kg</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="analytics-empty">No CO2 data available</div>
                )}
              </div>
            </>
          )}

          {/* Top Selling Products Section */}
          <div className="analytics-section">
            <div className="analytics-section-header">
              <h2 className="analytics-section-title">Top Selling Products</h2>
              <div className="analytics-tag">Best Performers</div>
            </div>
            {items.length === 0 ? (
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
        </>
      )}
      </div>
    </div>
  );
};

export default AnalyticsPage;
