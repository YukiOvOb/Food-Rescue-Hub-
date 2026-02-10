import React, { useEffect, useState } from 'react';
import authService from '../services/authService';

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
  const [user, setUser] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [foodCategories, setFoodCategories] = useState([]);
  const [successMessage, setSuccessMessage] = useState('');
  const [formData, setFormData] = useState({
    storeId: '',
    title: '',
    description: '',
    originalPrice: '',
    rescuePrice: '',
    pickupStart: '',
    pickupEnd: '',
    expiryAt: '',
    categoryIds: [],
    categoryWeightById: {}
  });
  const [photoFile, setPhotoFile] = useState(null);
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

  const toTimeInput = (dt) => {
    if (!dt) return '';
    const d = new Date(dt);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  };

  const toDateTimeLocalInput = (dt) => {
    if (!dt) return '';
    const d = new Date(dt);
    const offsetMs = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - offsetMs).toISOString().slice(0, 16);
  };

  const formatTime = (dt) => {
    if (!dt) return 'N/A';
    const date = new Date(dt);
    if (Number.isNaN(date.getTime())) return 'N/A';
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const formatDateTime = (dt) => {
    if (!dt) return 'N/A';
    const date = new Date(dt);
    if (Number.isNaN(date.getTime())) return 'N/A';
    return date.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const resolvePhotoUrl = (url) => {
    if (!url) return null;
    if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
      return url;
    }
    const normalizedPath = url.startsWith('/') ? url : `/${url}`;
    return `${window.location.origin}${normalizedPath}`;
  };

  const getPhotoUrls = (listing) => {
    if (Array.isArray(listing.photoUrls)) {
      return listing.photoUrls.map(resolvePhotoUrl).filter(Boolean);
    }
    if (Array.isArray(listing.photos)) {
      return listing.photos
        .map((p) => resolvePhotoUrl(p?.photoUrl || p?.url))
        .filter(Boolean);
    }
    if (listing.photoUrl) {
      const resolved = resolvePhotoUrl(listing.photoUrl);
      return resolved ? [resolved] : [];
    }
    return [];
  };

  const resetForm = () => {
    setFormData({
      storeId: '',
      title: '',
      description: '',
      originalPrice: '',
      rescuePrice: '',
      pickupStart: '',
      pickupEnd: '',
      expiryAt: '',
      categoryIds: [],
      categoryWeightById: {}
    });
    setPhotoFile(null);
    setErrors([]);
    setEditingId(null);
    setShowCreateForm(false);
  };

  const apiBase = '/api';
  const supplierBase = `${apiBase}/supplier`;

  useEffect(() => {
    const loadUser = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        if (currentUser) {
          setUser(currentUser);
        }
      } catch (error) {
        console.error('Error loading user:', error);
      }
    };
    loadUser();
  }, []);

  useEffect(() => {
    fetch(`${apiBase}/food-categories`)
      .then((r) => {
        if (!r.ok) throw new Error('network');
        return r.json();
      })
      .then((data) => setFoodCategories(Array.isArray(data) ? data : []))
      .catch((err) => {
        console.error('Error fetching food categories:', err);
        setFoodCategories([]);
      });
  }, []);

  useEffect(() => {
    if (user?.userId || user?.supplierId) {
      fetchListings();
    }
  }, [user?.userId, user?.supplierId]);

  const fetchListings = () => {
    const supplierId = user?.supplierId ?? user?.userId;
    if (!supplierId) return;
    
    fetch(`${supplierBase}/listings/supplier/${supplierId}`)
      .then((r) => {
        if (!r.ok) throw new Error('network');
        return r.json();
      })
      .then((data) => {
        console.log('Listings fetched:', data);
        setListings(data);
      })
      .catch((err) => {
        console.error('Error fetching listings:', err);
        setListings([]);
      });
  };

  const uploadPhoto = async (listingId) => {
    if (!photoFile || !listingId) return;

    const fd = new FormData();
    fd.append('file', photoFile);

    await fetch(`${supplierBase}/listings/${listingId}/photos`, {
      method: 'POST',
      body: fd,
      credentials: 'include'
    });
  };

  const handleDelete = (listingId) => {
    if (!listingId) return;
    const confirmed = window.confirm('Delete this listing?');
    if (!confirmed) return;
    setErrors([]);
    setSuccessMessage('');

    fetch(`${supplierBase}/listings/${listingId}`, {
      method: 'DELETE',
      credentials: 'include'
    })
      .then((r) => {
        if (!r.ok) throw new Error('Failed to delete');
        return r.text();
      })
      .then((message) => {
        setSuccessMessage(message || 'Listing deleted successfully.');
        fetchListings();
      })
      .catch((err) => {
        console.error(err);
        setErrors(['Failed to delete listing']);
      });
  };

  const startEdit = (listing) => {
    const listingId = listing.listingId || listing.id;
    const categoryWeights = Array.isArray(listing.categoryWeights) ? listing.categoryWeights : [];
    const categoryIdsFromWeights = categoryWeights
      .map((item) => item?.categoryId)
      .filter((id) => Number.isFinite(id));
    const fallbackCategoryIds = Array.isArray(listing.categoryIds) ? listing.categoryIds : [];
    const categoryIds = categoryIdsFromWeights.length > 0 ? categoryIdsFromWeights : fallbackCategoryIds;
    const categoryWeightById = categoryWeights.reduce((acc, item) => {
      if (!Number.isFinite(item?.categoryId) || item.weightKg == null) return acc;
      const grams = Number(item.weightKg) * 1000;
      acc[item.categoryId] = Number.isFinite(grams) ? String(Math.round(grams)) : '';
      return acc;
    }, {});

    setEditingId(listingId);
    setShowCreateForm(true);
    setPhotoFile(null);
    setErrors([]);
    setSuccessMessage('');

    setFormData({
      storeId: listing.storeId || listing.store?.storeId || '',
      title: listing.title || '',
      description: listing.description || '',
      originalPrice: listing.originalPrice ?? '',
      rescuePrice: listing.rescuePrice ?? '',
      pickupStart: toTimeInput(listing.pickupStart),
      pickupEnd: toTimeInput(listing.pickupEnd),
      expiryAt: toDateTimeLocalInput(listing.expiryAt),
      categoryIds,
      categoryWeightById
    });
  };

  const getPrimaryPhoto = (listing) => {
    const urls = getPhotoUrls(listing);
    return urls && urls.length > 0 ? urls[0] : null;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors([]);
    setSuccessMessage('');
  };

  const toggleCategory = (categoryId) => {
    setFormData((prev) => {
      const exists = prev.categoryIds.includes(categoryId);
      const nextIds = exists
        ? prev.categoryIds.filter((id) => id !== categoryId)
        : [...prev.categoryIds, categoryId];
      const nextWeights = { ...prev.categoryWeightById };
      if (exists) {
        delete nextWeights[categoryId];
      }
      return { ...prev, categoryIds: nextIds, categoryWeightById: nextWeights };
    });
    setErrors([]);
    setSuccessMessage('');
  };

  const handleCategoryWeightChange = (categoryId, value) => {
    setFormData((prev) => ({
      ...prev,
      categoryWeightById: { ...prev.categoryWeightById, [categoryId]: value }
    }));
    setErrors([]);
    setSuccessMessage('');
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

  const getCategoryErrors = () => {
    const errs = [];
    if (!formData.categoryIds || formData.categoryIds.length === 0) {
      errs.push('Select at least one food category');
    }
    if (formData.categoryIds && formData.categoryIds.length > 0) {
      const invalidWeights = formData.categoryIds.filter((id) => {
        const grams = parseFloat(formData.categoryWeightById[id]);
        return Number.isNaN(grams) || grams <= 0;
      });
      if (invalidWeights.length > 0) {
        errs.push('Each selected category must have a weight (grams) greater than 0');
      }
    }
    return errs;
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
    setSuccessMessage('');

    const priceErrors = validatePrices();
    const timeErrors = getTimeErrors();
    const categoryErrors = getCategoryErrors();
    const combinedErrors = [...priceErrors, ...timeErrors, ...categoryErrors];
    if (combinedErrors.length > 0) {
      setErrors(combinedErrors);
      return;
    }

    if (!editingId && !formData.storeId) {
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
      expiryAt: expiryAtStr,
      categoryWeights: formData.categoryIds.map((id) => ({
        categoryId: id,
        weightKg: parseFloat(formData.categoryWeightById[id]) / 1000
      }))
    };

    // Backend expects storeId as query param, not inside the JSON body.
    const url = editingId
      ? `${supplierBase}/listings/${editingId}`
      : `${supplierBase}/listings?storeId=${encodeURIComponent(formData.storeId)}`;

    const method = editingId ? 'PUT' : 'POST';
    const wasEditing = !!editingId;

    fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
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
      .then(async (saved) => {
        if (photoFile && saved) {
          const listingId = saved.listingId || saved.id;
          await uploadPhoto(listingId);
        }
        resetForm();
        setSuccessMessage(wasEditing ? 'Listing updated successfully.' : 'Listing created successfully.');
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

      {successMessage && (
        <div
          style={{
            marginBottom: 16,
            backgroundColor: '#e8f7eb',
            border: '1px solid #c6e9cc',
            color: '#14532d',
            borderRadius: 10,
            padding: '12px 14px'
          }}
        >
          {successMessage}
        </div>
      )}

      {showCreateForm && (
        <div style={{ ...cardStyle, marginBottom: 20 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h2 style={{ margin: 0 }}>{editingId ? 'Edit Listing' : 'Create New Listing'}</h2>
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
                required={!editingId}
                disabled={!!editingId}
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Photo</label>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setPhotoFile(e.target.files && e.target.files[0] ? e.target.files[0] : null)}
              />
              {photoFile && <div style={{ color: '#6b7280', marginTop: 6 }}>Selected: {photoFile.name}</div>}
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

            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                Food Categories
              </label>
              {foodCategories.length === 0 ? (
                <div style={{ color: '#6b7280', fontSize: 13 }}>No food categories found.</div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '10px' }}>
                  {foodCategories.map((cat) => (
                    <label
                      key={cat.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '8px 10px',
                        borderRadius: '10px',
                        border: getCategoryErrors().length > 0 ? '2px solid red' : '1px solid #e5e7eb',
                        background: '#fff'
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={formData.categoryIds.includes(cat.id)}
                        onChange={() => toggleCategory(cat.id)}
                      />
                      <span style={{ fontSize: 14, color: '#111827' }}>{cat.name}</span>
                    </label>
                  ))}
                </div>
              )}
            </div>

            {formData.categoryIds.length > 0 && (
              <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                  Category Weights (grams)
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '10px' }}>
                  {formData.categoryIds.map((id) => {
                    const cat = foodCategories.find((c) => c.id === id);
                    return (
                      <div
                        key={id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                          padding: '8px 10px',
                          borderRadius: '10px',
                          border: getCategoryErrors().length > 0 ? '2px solid red' : '1px solid #e5e7eb',
                          background: '#fff'
                        }}
                      >
                        <span style={{ flex: 1, fontSize: 14, color: '#111827' }}>
                          {cat ? cat.name : `Category ${id}`}
                        </span>
                        <input
                          type="number"
                          value={formData.categoryWeightById[id] ?? ''}
                          onChange={(e) => handleCategoryWeightChange(id, e.target.value)}
                          step="1"
                          min="1"
                          placeholder="grams"
                          style={{
                            width: 90,
                            padding: '6px 8px',
                            borderRadius: '8px',
                            border: '1px solid #d1d5db',
                            boxSizing: 'border-box'
                          }}
                        />
                      </div>
                    );
                  })}
                </div>
                <div style={{ marginTop: 8, color: '#6b7280', fontSize: 12 }}>
                  {(() => {
                    const totalGrams = formData.categoryIds.reduce((sum, id) => {
                      const v = parseFloat(formData.categoryWeightById[id]);
                      return Number.isNaN(v) ? sum : sum + v;
                    }, 0);
                    const totalKg = totalGrams / 1000;
                    return `Total weight: ${totalGrams.toFixed(0)} g (${totalKg.toFixed(3)} kg)`;
                  })()}
                </div>
              </div>
            )}

            <div style={{ display: 'flex', gap: '10px' }}>
              <button type="submit" style={pillPrimary}>
                {editingId ? 'Update Listing' : 'Create'}
              </button>
              <button type="button" onClick={resetForm} style={pillSubtle}>
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
            const primaryPhoto = getPrimaryPhoto(listing);
            return (
              <div key={listing.listingId || listing.id} style={{ ...cardStyle, padding: 18 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 10, marginBottom: 8 }}>
                  <h3 style={{ margin: 0, flex: 1 }}>{listing.title}</h3>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                    <span style={{ background: '#ecfdf3', color: '#166534', padding: '4px 10px', borderRadius: '999px', fontSize: 12 }}>
                      {listing.status || 'ACTIVE'}
                    </span>
                    <button
                      style={{ ...pillSubtle, padding: '6px 10px', fontSize: 12 }}
                      onClick={() => startEdit(listing)}
                    >
                      Edit
                    </button>
                    <button
                      style={{ ...pillSubtle, padding: '6px 10px', fontSize: 12, backgroundColor: '#fee2e2', color: '#b91c1c' }}
                      onClick={() => handleDelete(listing.listingId || listing.id)}
                    >
                      Delete
                    </button>
                  </div>
                </div>
                <div style={{ marginTop: 12, borderRadius: 12, overflow: 'hidden', border: '1px solid #e5e7eb', height: 180, backgroundColor: '#f8fafc' }}>
                  {primaryPhoto ? (
                    <img
                      src={primaryPhoto}
                      alt={listing.title}
                      style={{ width: '100%', height: 180, objectFit: 'cover', display: 'block' }}
                      loading="lazy"
                    />
                  ) : (
                    <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9ca3af', fontSize: 13 }}>
                      No photo uploaded
                    </div>
                  )}
                </div>

                {listing.description && <p style={{ color: '#4b5563', marginTop: 10, marginBottom: 0 }}>{listing.description}</p>}
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
                  <div>Pickup: {formatTime(listing.pickupStart)} - {formatTime(listing.pickupEnd)}</div>
                  <div>Expires: {formatDateTime(listing.expiryAt)}</div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
