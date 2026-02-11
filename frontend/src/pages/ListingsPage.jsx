import { useEffect, useState } from 'react';
import authService from '../services/authService';
import { useNavigate } from 'react-router-dom';
import ConfirmDialog from '../components/ConfirmDialog';
import './ListingsPage.css';

export default function ListingsPage() {
  const navigate = useNavigate();
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [listings, setListings] = useState([]);
  const [user, setUser] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [foodCategories, setFoodCategories] = useState([]);
  const [successMessage, setSuccessMessage] = useState('');
  const [confirmDeleteDialog, setConfirmDeleteDialog] = useState({ isOpen: false, listingId: null });
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
        // ignore
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
      .catch(() => {
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
        setListings(data);
      })
      .catch(() => {
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
    setConfirmDeleteDialog({ isOpen: true, listingId });
  };

  const confirmDelete = () => {
    const listingId = confirmDeleteDialog.listingId;
    if (!listingId) return;

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
        setConfirmDeleteDialog({ isOpen: false, listingId: null });
      })
      .catch(() => {
        setErrors(['Failed to delete listing']);
        setConfirmDeleteDialog({ isOpen: false, listingId: null });
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
    <div className="listings-page">
      {/* Header Banner */}
      <div className="listings-header-banner">
        <div className="banner-content">
          <div className="banner-left">
            <span className="banner-tag">SUPPLIER</span>
            <h1 className="banner-title">Listings</h1>
          </div>
          <div className="banner-right">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="btn-back-dashboard"
            >
              ← Back to Dashboard
            </button>
            {!showCreateForm && (
              <button className="btn-create-listing" onClick={() => setShowCreateForm(true)}>
                + Create Listing
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="listings-content">

        {successMessage && (
          <div className="listings-success">
            {successMessage}
          </div>
        )}

      {showCreateForm && (
        <div className="listings-form-card">
          <div className="listings-form-header">
            <h2>{editingId ? 'Edit Listing' : 'Create New Listing'}</h2>
            <button
              type="button"
              onClick={resetForm}
              className="listings-btn-back"
            >
              ← Back to Listings
            </button>
          </div>
          <form onSubmit={handleSubmit}>
            {errors.length > 0 && (
              <div className="listings-errors">
                {errors.map((err, i) => (
                  <div key={i}>{err}</div>
                ))}
              </div>
            )}

            <div className="listings-form-field">
              <label>Store ID *</label>
              <input
                type="number"
                name="storeId"
                value={formData.storeId}
                onChange={handleChange}
                required={!editingId}
                disabled={!!editingId}
              />
            </div>

            <div className="listings-form-field">
              <label>Title *</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
              />
            </div>

            <div className="listings-form-field">
              <label>Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
              />
            </div>

            <div className="listings-form-field">
              <label>Photo</label>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setPhotoFile(e.target.files && e.target.files[0] ? e.target.files[0] : null)}
              />
              {photoFile && <div style={{ color: '#6b7280', marginTop: 6 }}>Selected: {photoFile.name}</div>}
            </div>

            <div className="listings-grid-2">
              <div className="listings-form-field">
                <label>Original Price *</label>
                <div className="listings-price-input-wrapper">
                  <input
                    type="number"
                    name="originalPrice"
                    value={formData.originalPrice}
                    onChange={handleChange}
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    required
                    className={priceErrors.length > 0 ? 'error-border' : ''}
                  />
                  <span className="listings-price-dollar">$</span>
                </div>
              </div>

              <div className="listings-form-field">
                <label>Rescue Price *</label>
                <div className="listings-price-input-wrapper">
                  <input
                    type="number"
                    name="rescuePrice"
                    value={formData.rescuePrice}
                    onChange={handleChange}
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    required
                    className={priceErrors.length > 0 ? 'error-border' : ''}
                  />
                  <span className="listings-price-dollar">$</span>
                </div>
              </div>
            </div>

            {priceErrors.length > 0 && (
              <div className="listings-warning">
                <span className="listings-warning-icon">⚠</span>
                <div>
                  {priceErrors.map((err, i) => (
                    <div key={i}>{err}</div>
                  ))}
                </div>
              </div>
            )}

            {timeErrors.length > 0 && (
              <div className="listings-warning">
                <span className="listings-warning-icon">🕒</span>
                <div>
                  {timeErrors.map((err, i) => (
                    <div key={i}>{err}</div>
                  ))}
                </div>
              </div>
            )}

            <div className="listings-grid-3">
              <div className="listings-form-field">
                <label>Pickup Start (Time) *</label>
                <input
                  type="time"
                  name="pickupStart"
                  value={formData.pickupStart}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="listings-form-field">
                <label>Pickup End (Time) *</label>
                <input
                  type="time"
                  name="pickupEnd"
                  value={formData.pickupEnd}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="listings-form-field">
                <label>Expiry *</label>
                <input
                  type="datetime-local"
                  name="expiryAt"
                  value={formData.expiryAt}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="listings-form-field">
              <label>Food Categories</label>
              {foodCategories.length === 0 ? (
                <div style={{ color: '#6b7280', fontSize: 13 }}>No food categories found.</div>
              ) : (
                <div className="listings-categories-grid">
                  {foodCategories.map((cat) => (
                    <label
                      key={cat.id}
                      className={`listings-category-checkbox ${getCategoryErrors().length > 0 ? 'error-border' : ''}`}
                    >
                      <input
                        type="checkbox"
                        checked={formData.categoryIds.includes(cat.id)}
                        onChange={() => toggleCategory(cat.id)}
                      />
                      <span>{cat.name}</span>
                    </label>
                  ))}
                </div>
              )}
            </div>

            {formData.categoryIds.length > 0 && (
              <div className="listings-form-field">
                <label>Category Weights (grams)</label>
                <div className="listings-weights-grid">
                  {formData.categoryIds.map((id) => {
                    const cat = foodCategories.find((c) => c.id === id);
                    return (
                      <div
                        key={id}
                        className={`listings-weight-item ${getCategoryErrors().length > 0 ? 'error-border' : ''}`}
                      >
                        <span className="listings-weight-name">
                          {cat ? cat.name : `Category ${id}`}
                        </span>
                        <input
                          type="number"
                          className="listings-weight-input"
                          value={formData.categoryWeightById[id] ?? ''}
                          onChange={(e) => handleCategoryWeightChange(id, e.target.value)}
                          step="1"
                          min="1"
                          placeholder="grams"
                        />
                      </div>
                    );
                  })}
                </div>
                <div className="listings-weight-total">
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

            <div className="listings-form-actions">
              <button type="submit" className="listings-btn-primary">
                {editingId ? 'Update Listing' : 'Create Listing'}
              </button>
              <button type="button" onClick={resetForm} className="listings-btn-secondary">
                Cancel & Back to Listings
              </button>
            </div>
          </form>
        </div>
      )}

      <h2 className="listings-section-title">All Listings</h2>
      {listings.length === 0 ? (
        <div className="listings-empty">
          No listings found.
        </div>
      ) : (
        <div className="listings-grid">
            {listings.map((listing) => {
              const primaryPhoto = getPrimaryPhoto(listing);
              return (
                <div key={listing.listingId || listing.id} className="listings-card">
                  <div className="listings-card-header">
                    <h3 className="listings-card-title">{listing.title}</h3>
                    <div className="listings-card-actions">
                      <span className="listings-status-badge">
                        {listing.status || 'ACTIVE'}
                      </span>
                      <button
                        className="listings-btn-edit"
                        onClick={() => startEdit(listing)}
                      >
                        Edit
                      </button>
                      <button
                        className="listings-btn-delete"
                        onClick={() => handleDelete(listing.listingId || listing.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                  <div className="listings-card-image">
                    {primaryPhoto ? (
                      <img
                        src={primaryPhoto}
                        alt={listing.title}
                        loading="lazy"
                      />
                    ) : (
                      <div className="listings-card-image-placeholder">
                        No photo uploaded
                      </div>
                    )}
                  </div>

                  {listing.description && <p className="listings-card-description">{listing.description}</p>}
                  <div className="listings-card-prices">
                    <div className="listings-price-box">
                      <div className="listings-price-label">Original</div>
                      <div className="listings-price-value">${listing.originalPrice}</div>
                    </div>
                    <div className="listings-price-box">
                      <div className="listings-price-label">Rescue</div>
                      <div className="listings-price-value rescue">${listing.rescuePrice}</div>
                    </div>
                  </div>
                  <div className="listings-card-meta">
                    <div>Pickup: {formatTime(listing.pickupStart)} - {formatTime(listing.pickupEnd)}</div>
                    <div>Expires: {formatDateTime(listing.expiryAt)}</div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <ConfirmDialog
          isOpen={confirmDeleteDialog.isOpen}
          onClose={() => setConfirmDeleteDialog({ isOpen: false, listingId: null })}
          onConfirm={confirmDelete}
          title="Delete Listing"
          message="Are you sure you want to delete this listing? This action cannot be undone."
          confirmText="Delete"
          cancelText="Cancel"
          type="danger"
        />
      </div>
    </div>
  );
}
