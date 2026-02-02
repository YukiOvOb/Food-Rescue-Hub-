import React, { useEffect, useState } from 'react';

const cardStyle = {
  backgroundColor: '#fff',
  borderRadius: '14px',
  boxShadow: '0 10px 30px rgba(31, 41, 55, 0.08)',
  padding: '20px',
  border: '1px solid #e5e7eb'
};

const pillPrimary = {
  padding: '10px 16px',
  backgroundColor: '#6366f1',
  color: '#fff',
  border: 'none',
  borderRadius: '10px',
  cursor: 'pointer',
  fontWeight: 600,
  fontSize: '14px'
};

const pillSubtle = {
  ...pillPrimary,
  backgroundColor: '#e5e7eb',
  color: '#111827'
};

export default function ListingsPage() {
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
  const [photoFiles, setPhotoFiles] = useState([]);
  const [errors, setErrors] = useState([]);

  // ----- helpers for datetime formatting -----
  // Local date string (avoids UTC shift from toISOString)
  const todayStr = () => {
    const d = new Date();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`; // yyyy-MM-dd in local time
  };

  const withSeconds = (timeStr) =>
    timeStr && timeStr.length === 5 ? `${timeStr}:00` : timeStr; // "HH:MM" -> "HH:MM:00"

  const normalizeDateTimeLocal = (dt) =>
    dt && dt.length === 16 ? `${dt}:00` : dt; // "yyyy-MM-ddTHH:MM" -> "yyyy-MM-ddTHH:MM:00"

  const apiBase = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';
  const supplierBase = `${apiBase}/supplier`;

  useEffect(() => {
    fetchListings();
  }, []);

  const fetchListings = () => {
    fetch(`${supplierBase}/listings`)
      .then((r) => {
        if (!r.ok) throw new Error('network');
        return r.json();
      })
      .then((data) => setListings(data))
      .catch(() => setListings([]));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors([]);
  };

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files || []).slice(0, 1); // limit to 1 file
    const newErrors = [];
    const acceptedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    const maxSize = 5 * 1024 * 1024; // 5 MB

    files.forEach((file) => {
      if (!acceptedTypes.includes(file.type)) {
        newErrors.push(`Unsupported file type: ${file.name}`);
      }
      if (file.size > maxSize) {
        newErrors.push(`File too large (max 5MB): ${file.name}`);
      }
    });

    if (newErrors.length > 0) {
      setErrors(newErrors);
      return;
    }

    setPhotoFiles(files);
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

  const uploadListingPhotos = async (listingId) => {
    if (!photoFiles.length) return;

    const file = photoFiles[0];
    const formDataUpload = new FormData();
    formDataUpload.append('file', file);
    formDataUpload.append('sortOrder', 1);

    const res = await fetch(`${supplierBase}/listings/${listingId}/photos`, {
      method: 'POST',
      body: formDataUpload
    });

    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || 'Photo upload failed');
    }
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
    const pickupEndStr = `${today}T${withSeconds(formData.pickupEnd)}`;
    const expiryAtStr = normalizeDateTimeLocal(formData.expiryAt); // yyyy-MM-ddTHH:MM:SS

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
    const url = `${supplierBase}/listings?storeId=${encodeURIComponent(formData.storeId)}`;

    fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(async (r) => {
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
      .then(async (created) => {
        const newListingId = created.listingId || created.id;
        let uploadErrorMessage = null;

        try {
          await uploadListingPhotos(newListingId);
        } catch (uploadErr) {
          console.error('Upload error:', uploadErr);
          uploadErrorMessage = `Listing saved but photo upload failed: ${uploadErr.message}`;
          // Listing already saved; keep going so user can see it.
        }

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
        setPhotoFiles([]);
        setErrors(uploadErrorMessage ? [uploadErrorMessage] : []);
        setShowCreateForm(false);
        fetchListings();
      })
      .catch((err) => {
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

  const resolveUrl = (url) => {
    if (!url) return null;
    if (url.startsWith('http')) return url;
    const backendOrigin = apiBase.replace(/\/api$/, '');
    return `${backendOrigin}${url}`;
  };

  const getPrimaryPhoto = (listing) => {
    if (listing.photoUrls && listing.photoUrls.length > 0) {
      return resolveUrl(listing.photoUrls[0]);
    }
    if (listing.photos && listing.photos.length > 0) {
      const sorted = [...listing.photos].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
      return resolveUrl(sorted[0]?.photoUrl);
    }
    return null;
  };

  return (
    <div style={{ fontFamily: 'Inter, "Segoe UI", sans-serif', padding: 24, maxWidth: 1200, margin: '0 auto', background: '#f5f7fb', minHeight: '100vh' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <p style={{ margin: 0, color: '#6b7280', fontSize: 12 }}>Supplier</p>
          <h1 style={{ margin: 0, color: '#0f172a' }}>Listings</h1>
        </div>
        {!showCreateForm && (
          <button style={pillPrimary} onClick={() => setShowCreateForm(true)}>
            + Create Listing
          </button>
        )}
      </div>

      {showCreateForm && (
        <div style={{ ...cardStyle, marginBottom: 20 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h2 style={{ margin: 0 }}>Create New Listing</h2>
            <button type="button" style={pillSubtle} onClick={() => setShowCreateForm(false)}>Cancel</button>
          </div>
          <form onSubmit={handleSubmit}>
            {errors.length > 0 && (
              <div
                style={{
                  backgroundColor: '#ffebee',
                  color: '#c62828',
                  padding: '10px',
                  borderRadius: '4px',
                  marginBottom: '15px'
                }}
              >
                {errors.map((err, i) => (
                  <div key={i}>{err}</div>
                ))}
              </div>
            )}

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Store ID *</label>
              <input
                type="number"
                name="storeId"
                value={formData.storeId}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '10px',
                  border: '1px solid #d1d5db',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Title *</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '10px',
                  border: '1px solid #d1d5db',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '10px',
                  border: '1px solid #d1d5db',
                  boxSizing: 'border-box',
                  minHeight: '80px'
                }}
              />
            </div>

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Photos (JPG/PNG/WEBP, max 5MB each)
              </label>
              <input
                type="file"
                accept=".jpg,.jpeg,.png,.webp"
                onChange={handleFileChange}
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '10px',
                  border: '1px solid #d1d5db',
                  boxSizing: 'border-box'
                }}
              />
              {photoFiles.length > 0 && (
                <div style={{ marginTop: 6, color: '#6b7280', fontSize: 13 }}>
                  {photoFiles.length} file(s) selected
                </div>
              )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Original Price *</label>
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
                      padding: '10px 10px 10px 26px',
                      borderRadius: '10px',
                      border: priceErrors.length > 0 ? '2px solid red' : '1px solid #d1d5db',
                      boxSizing: 'border-box'
                    }}
                  />
                  <span style={{ position: 'absolute', left: '10px', top: '10px', color: '#9ca3af' }}>$</span>
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Rescue Price *</label>
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
                      padding: '10px 10px 10px 26px',
                      borderRadius: '10px',
                      border: priceErrors.length > 0 ? '2px solid red' : '1px solid #d1d5db',
                      boxSizing: 'border-box'
                    }}
                  />
                  <span style={{ position: 'absolute', left: '10px', top: '10px', color: '#9ca3af' }}>$</span>
                </div>
              </div>
            </div>

            {priceErrors.length > 0 && (
              <div
                style={{
                  backgroundColor: '#fff3cd',
                  color: '#856404',
                  padding: '10px 15px',
                  borderRadius: '4px',
                  marginBottom: '15px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  border: '1px solid #ffeaa7'
                }}
              >
                <span style={{ fontSize: '18px' }}>⚠</span>
                <div>
                  {priceErrors.map((err, i) => (
                    <div key={i}>{err}</div>
                  ))}
                </div>
              </div>
            )}

            {timeErrors.length > 0 && (
              <div
                style={{
                  backgroundColor: '#fff3cd',
                  color: '#856404',
                  padding: '10px 15px',
                  borderRadius: '4px',
                  marginBottom: '15px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  border: '1px solid #ffeaa7'
                }}
              >
                <span style={{ fontSize: '18px' }}>🕒</span>
                <div>
                  {timeErrors.map((err, i) => (
                    <div key={i}>{err}</div>
                  ))}
                </div>
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '15px', marginBottom: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Pickup Start (Time) *</label>
                <input
                  type="time"
                  name="pickupStart"
                  value={formData.pickupStart}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '10px',
                    borderRadius: '10px',
                    border: '1px solid #d1d5db',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Pickup End (Time) *</label>
                <input
                  type="time"
                  name="pickupEnd"
                  value={formData.pickupEnd}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '10px',
                    borderRadius: '10px',
                    border: '1px solid #d1d5db',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Expiry *</label>
                <input
                  type="datetime-local"
                  name="expiryAt"
                  value={formData.expiryAt}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '10px',
                    borderRadius: '10px',
                    border: '1px solid #d1d5db',
                    boxSizing: 'border-box'
                  }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button type="submit" style={pillPrimary}>
                Create
              </button>
              <button type="button" onClick={() => setShowCreateForm(false)} style={pillSubtle}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <h2 style={{ marginTop: 10, marginBottom: 10, color: '#0f172a' }}>All Listings</h2>
      {listings.length === 0 ? (
        <div style={{ ...cardStyle, textAlign: 'center', color: '#6b7280' }}>
          No listings found.
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
          {listings.map((listing) => {
            const photoUrl = getPrimaryPhoto(listing);
            return (
              <div key={listing.listingId || listing.id} style={{ ...cardStyle, padding: 18 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ margin: 0 }}>{listing.title}</h3>
                  <span style={{ background: '#ecfdf3', color: '#166534', padding: '4px 10px', borderRadius: '999px', fontSize: 12 }}>
                    {listing.status || 'ACTIVE'}
                  </span>
                </div>

                {photoUrl && (
                  <div style={{ marginTop: 12, overflow: 'hidden', borderRadius: 12, border: '1px solid #e5e7eb' }}>
                    <img
                      src={photoUrl}
                      alt={`${listing.title} photo`}
                      style={{ width: '100%', height: 180, objectFit: 'cover', display: 'block' }}
                    />
                  </div>
                )}

                {listing.description && <p style={{ color: '#4b5563' }}>{listing.description}</p>}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 10 }}>
                  <div>
                    <div style={{ color: '#6b7280', fontSize: 12 }}>Original</div>
                    <div style={{ fontWeight: 700 }}>${listing.originalPrice}</div>
                  </div>
                  <div>
                    <div style={{ color: '#6b7280', fontSize: 12 }}>Rescue</div>
                    <div style={{ fontWeight: 700, color: '#16a34a' }}>${listing.rescuePrice}</div>
                  </div>
                </div>
                <div style={{ marginTop: 12, borderTop: '1px solid #e5e7eb', paddingTop: 10, color: '#4b5563', fontSize: 13 }}>
                  <div>Pickup: {new Date(listing.pickupStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {new Date(listing.pickupEnd).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                  <div>Expires: {new Date(listing.expiryAt).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
