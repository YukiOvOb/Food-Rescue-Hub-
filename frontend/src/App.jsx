import React, { useEffect, useState } from 'react'

export default function App() {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [listings, setListings] = useState([]);
  const [formData, setFormData] = useState({
    storeId: '',
    title: '',
    description: '',
    originalPrice: '',
    rescuePrice: '',
    pickupStart: '',
    pickupEnd: '',
    expiryAt: ''
  });
  const [errors, setErrors] = useState([]);

  // ----- helpers for datetime formatting -----
  const todayStr = () => new Date().toISOString().split('T')[0]; // "yyyy-MM-dd"

  // "HH:MM" -> "HH:MM:00"
  const withSeconds = (timeStr) =>
    timeStr && timeStr.length === 5 ? `${timeStr}:00` : timeStr;

  // "yyyy-MM-ddTHH:MM" -> "yyyy-MM-ddTHH:MM:00"
  const normalizeDateTimeLocal = (dt) =>
    dt && dt.length === 16 ? `${dt}:00` : dt;

  // ------------------------------------------

  useEffect(() => {
    fetchListings();
  }, []);

  const fetchListings = () => {
    fetch('/api/listings')
      .then(r => {
        if (!r.ok) throw new Error('network');
        return r.json();
      })
      .then(data => setListings(data))
      .catch(() => setListings([]));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setErrors([]);
  };

  const validatePrices = () => {
    const priceErrors = [];
    const originalPrice = parseFloat(formData.originalPrice);
    const rescuePrice = parseFloat(formData.rescuePrice);

    if (formData.originalPrice && originalPrice <= 0) {
      priceErrors.push('Original price must be greater than 0');
    }
    if (formData.rescuePrice && rescuePrice < 0) {
      priceErrors.push('Rescue price cannot be negative');
    }
    if (formData.originalPrice && formData.rescuePrice && rescuePrice >= originalPrice) {
      priceErrors.push('Rescue price must be lower than original price');
    }
    return priceErrors;
  };

  const getPriceErrors = () => {
    const priceErrors = [];
    const originalPrice = parseFloat(formData.originalPrice);
    const rescuePrice = parseFloat(formData.rescuePrice);

    if (formData.originalPrice && originalPrice <= 0) {
      priceErrors.push('Original price must be greater than 0');
    }
    if (formData.rescuePrice && rescuePrice < 0) {
      priceErrors.push('Rescue price cannot be negative');
    }
    if (formData.originalPrice && formData.rescuePrice && rescuePrice >= originalPrice) {
      priceErrors.push('Rescue price must be lower than original price');
    }
    return priceErrors;
  };

  const getTimeErrors = () => {
    const errs = [];
    const today = todayStr();

    if (formData.pickupStart) {
      const start = new Date(`${today}T${withSeconds(formData.pickupStart)}`);
      const now = new Date();
      if (start <= now) {
        errs.push('Pickup start time must be later than the current time');
      }
      if (formData.pickupEnd) {
        const end = new Date(`${today}T${withSeconds(formData.pickupEnd)}`);
        if (end <= start) {
          errs.push('Pickup end time must be after pickup start time');
        }
      }
    }
    return errs;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const priceErrors = validatePrices();
    const timeErrors = getTimeErrors();
    const combinedErrors = [...priceErrors, ...timeErrors];
    if (combinedErrors.length > 0) {
      setErrors(combinedErrors);
      return;
    }

    if (!formData.storeId) {
      setErrors(['Store ID is required']);
      return;
    }

    // Build LocalDateTime strings that Spring can parse
    const today = todayStr(); // "yyyy-MM-dd"

    const pickupStartStr = `${today}T${withSeconds(formData.pickupStart)}`; // yyyy-MM-ddTHH:MM:SS
    const pickupEndStr   = `${today}T${withSeconds(formData.pickupEnd)}`;
    const expiryAtStr    = normalizeDateTimeLocal(formData.expiryAt);       // yyyy-MM-ddTHH:MM:SS

    const payload = {
      title: formData.title,
      description: formData.description,
      originalPrice: parseFloat(formData.originalPrice),
      rescuePrice: parseFloat(formData.rescuePrice),
      pickupStart: pickupStartStr,
      pickupEnd: pickupEndStr,
      expiryAt: expiryAtStr
    };

    // Backend expects storeId as query param, not inside the JSON body.
    const url = `/api/listings?storeId=${encodeURIComponent(formData.storeId)}`;

    fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(async r => {
        if (!r.ok) {
          try {
            const jsonErr = await r.json();
            return Promise.reject(jsonErr);
          } catch (_) {
            const txt = await r.text();
            return Promise.reject(txt || `Failed to create listing (status ${r.status})`);
          }
        }
        return r.json();
      })
      .then(() => {
        setFormData({
          storeId: '',
          title: '',
          description: '',
          originalPrice: '',
          rescuePrice: '',
          pickupStart: '',
          pickupEnd: '',
          expiryAt: ''
        });
        setErrors([]);
        setShowCreateForm(false);
        fetchListings();
      })
      .catch(err => {
        console.error('Create listing error:', err);
        if (Array.isArray(err)) {
          setErrors(err);
        } else if (typeof err === 'string') {
          setErrors([err]);
        } else {
          setErrors(['Failed to create listing']);
        }
      });
  };

  const priceErrors = getPriceErrors();
  const timeErrors = getTimeErrors();

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: 24, maxWidth: 1200, margin: '0 auto' }}>
      <h1>Food Rescue Hub - Listings</h1>

      {!showCreateForm ? (
        <button
          onClick={() => setShowCreateForm(true)}
          style={{
            padding: '10px 20px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '16px',
            marginBottom: '20px'
          }}
        >
          Create Listing
        </button>
      ) : (
        <div style={{
          backgroundColor: '#f5f5f5',
          padding: '20px',
          borderRadius: '4px',
          marginBottom: '20px'
        }}>
          <h2>Create New Listing</h2>
          <form onSubmit={handleSubmit}>
            {errors.length > 0 && (
              <div style={{
                backgroundColor: '#ffebee',
                color: '#c62828',
                padding: '10px',
                borderRadius: '4px',
                marginBottom: '15px'
              }}>
                {errors.map((err, i) => <div key={i}>{err}</div>)}
              </div>
            )}

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Store ID *
              </label>
              <input
                type="number"
                name="storeId"
                value={formData.storeId}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '4px',
                  border: '1px solid #ccc',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Title *
              </label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '4px',
                  border: '1px solid #ccc',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Description
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '4px',
                  border: '1px solid #ccc',
                  boxSizing: 'border-box',
                  minHeight: '80px'
                }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Original Price *
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="number"
                    name="originalPrice"
                    value={formData.originalPrice}
                    onChange={handleChange}
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    required
                    style={{
                      width: '100%',
                      padding: '8px 8px 8px 25px',
                      borderRadius: '4px',
                      border: priceErrors.length > 0 ? '2px solid red' : '1px solid #ccc',
                      boxSizing: 'border-box'
                    }}
                  />
                  <span style={{ position: 'absolute', left: '8px', top: '8px' }}>$</span>
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Rescue Price *
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="number"
                    name="rescuePrice"
                    value={formData.rescuePrice}
                    onChange={handleChange}
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    required
                    style={{
                      width: '100%',
                      padding: '8px 8px 8px 25px',
                      borderRadius: '4px',
                      border: priceErrors.length > 0 ? '2px solid red' : '1px solid #ccc',
                      boxSizing: 'border-box'
                    }}
                  />
                  <span style={{ position: 'absolute', left: '8px', top: '8px' }}>$</span>
                </div>
              </div>
            </div>

            {priceErrors.length > 0 && (
              <div style={{
                backgroundColor: '#fff3cd',
                color: '#856404',
                padding: '10px 15px',
                borderRadius: '4px',
                marginBottom: '15px',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                border: '1px solid #ffeaa7'
              }}>
                <span style={{ fontSize: '18px' }}>⚠</span>
                <div>
                  {priceErrors.map((err, i) => <div key={i}>{err}</div>)}
                </div>
              </div>
            )}

            {timeErrors.length > 0 && (
              <div style={{
                backgroundColor: '#fff3cd',
                color: '#856404',
                padding: '10px 15px',
                borderRadius: '4px',
                marginBottom: '15px',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                border: '1px solid #ffeaa7'
              }}>
                <span style={{ fontSize: '18px' }}>[time]</span>
                <div>
                  {timeErrors.map((err, i) => <div key={i}>{err}</div>)}
                </div>
              </div>
            )}

            {/* Savings banner is more for consumer view; keeping logic here if you want it later */}
            {/* {savings && (
              <div style={{
                backgroundColor: '#e8f5e9',
                padding: '10px',
                borderRadius: '4px',
                marginBottom: '15px',
                color: '#2e7d32',
                fontWeight: 'bold'
              }}>
                Save ${savings.amount} ({savings.percent}%)
              </div>
            )} */}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '15px', marginBottom: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Pickup Start (Time) *
                </label>
                <input
                  type="time"
                  name="pickupStart"
                  value={formData.pickupStart}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '8px',
                    borderRadius: '4px',
                    border: '1px solid #ccc',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Pickup End (Time) *
                </label>
                <input
                  type="time"
                  name="pickupEnd"
                  value={formData.pickupEnd}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '8px',
                    borderRadius: '4px',
                    border: '1px solid #ccc',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Expiry *
                </label>
                <input
                  type="datetime-local"
                  name="expiryAt"
                  value={formData.expiryAt}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '8px',
                    borderRadius: '4px',
                    border: '1px solid #ccc',
                    boxSizing: 'border-box'
                  }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                type="submit"
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '16px'
                }}
              >
                Create
              </button>
              <button
                type="button"
                onClick={() => setShowCreateForm(false)}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#f44336',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '16px'
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <h2>All Listings</h2>
      {listings.length === 0 ? (
        <p>No listings found.</p>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
          {listings.map(listing => (
            <div
              key={listing.listingId || listing.id}
              style={{
                border: '1px solid #ddd',
                borderRadius: '4px',
                padding: '15px',
                backgroundColor: '#fff'
              }}
            >
              <h3>{listing.title}</h3>
              {listing.description && <p>{listing.description}</p>}
              <div style={{ marginTop: '10px' }}>
                <p><strong>Original Price:</strong> ${listing.originalPrice}</p>
                <p><strong>Rescue Price:</strong> ${listing.rescuePrice}</p>
              </div>
              <div style={{ marginTop: '15px', borderTop: '1px solid #eee', paddingTop: '10px' }}>
                <p style={{ fontSize: '13px', marginBottom: '8px' }}>
                  <strong>Pickup Window:</strong>
                </p>
                <p style={{ fontSize: '12px', color: '#555', marginBottom: '4px' }}>
                  📅 {new Date(listing.pickupStart).toLocaleDateString()}
                </p>
                <p style={{ fontSize: '12px', color: '#555', marginBottom: '8px' }}>
                  🕐 {new Date(listing.pickupStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  {' - '}
                  {new Date(listing.pickupEnd).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </p>
                <p style={{ fontSize: '12px', color: '#d32f2f', marginBottom: '0' }}>
                  ⏰ Expires: {new Date(listing.expiryAt).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}



