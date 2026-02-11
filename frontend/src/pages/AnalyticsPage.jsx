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
      <div className="analytics-header">
        <div>
          <span className="analytics-tag">Supplier</span>
          <h1>Analytics</h1>
        </div>
        <button className="analytics-btn" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>

      {loading ? (
        <div className="analytics-empty">Loading...</div>
      ) : error ? (
        <div className="analytics-empty">{error}</div>
      ) : (
        <>
          {co2Summary && (
            <>
              <div className="analytics-grid">
                <div className="analytics-card">
                  <div className="analytics-rank">Last 30 Days</div>
                  <h3>CO2 Savings</h3>
                  <div className="analytics-metric">
                    <span>Total CO2 Saved (kg)</span>
                    <strong>{Number(co2Summary.totalCo2Kg ?? 0).toFixed(2)}</strong>
                  </div>
                  <div className="analytics-metric">
                    <span>Total Weight (kg)</span>
                    <strong>{Number(co2Summary.totalWeightKg ?? 0).toFixed(2)}</strong>
                  </div>
                  <button className="analytics-btn" onClick={handleDownloadPdf}>
                    Download CO2 PDF
                  </button>
                </div>
              </div>

              <div className="analytics-card analytics-table-card">
                <h3>CO2 Breakdown by Category</h3>
                {Array.isArray(co2Summary.categories) && co2Summary.categories.length > 0 ? (
                  <div className="analytics-table-wrap">
                    <table className="analytics-table">
                      <thead>
                        <tr>
                          <th>Category</th>
                          <th>Total Weight (kg)</th>
                          <th>Total CO2 (kg)</th>
                        </tr>
                      </thead>
                      <tbody>
                        {co2Summary.categories.map((row) => (
                          <tr key={row.categoryId}>
                            <td>{row.categoryName}</td>
                            <td>{Number(row.totalWeightKg ?? 0).toFixed(3)}</td>
                            <td>{Number(row.totalCo2Kg ?? 0).toFixed(3)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="analytics-empty">No CO2 data available</div>
                )}
              </div>
            </>
          )}

          <h2>Top Selling Products</h2>
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
        </>
      )}
    </div>
  );
};

export default AnalyticsPage;
