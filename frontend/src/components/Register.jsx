import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import './styles/Auth.css';

const Register = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    phone: '',
    displayName: '',
    businessName: '',
    businessType: '',
    payoutAccountRef: '',
    role: ''
  });
  
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Email validation
    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    // Display name validation
    if (!formData.displayName) {
      newErrors.displayName = 'Display name is required';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    const newErrors = validateForm();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    
    try {
      const payload = { ...formData, role: 'SUPPLIER' };
      await authService.register(payload);
      
      // Redirect to dashboard after successful registration
      navigate('/dashboard');
    } catch (error) {
      // Handle different error formats
      if (error.errors) {
        setErrors(error.errors);
      } else if (error.message) {
        setErrors({ submit: error.message });
      } else {
        setErrors({ submit: 'Registration failed. Please try again.' });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div>
            <h2>Supplier Registration</h2>
            <p className="auth-subtitle">Create your supplier account</p>
          </div>
          <button
            type="button"
            onClick={() => navigate('/login')}
            className="btn-back"
          >
            ← Back to Login
          </button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {/* Email */}
          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={errors.email ? 'error' : ''}
              placeholder="Enter your email"
              autoComplete="email"
            />
            {errors.email && <span className="error-message">{errors.email}</span>}
          </div>

          {/* Password */}
          <div className="form-group">
            <label htmlFor="password">Password *</label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? "text" : "password"}
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={errors.password ? 'error' : ''}
                placeholder="Minimum 8 characters"
                autoComplete="new-password"
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                )}
              </button>
            </div>
            {errors.password && <span className="error-message">{errors.password}</span>}
          </div>

          {/* Display Name */}
          <div className="form-group">
            <label htmlFor="displayName">Display Name *</label>
            <input
              type="text"
              id="displayName"
              name="displayName"
              value={formData.displayName}
              onChange={handleChange}
              className={errors.displayName ? 'error' : ''}
              placeholder="How you want to be known"
              autoComplete="name"
            />
            {errors.displayName && <span className="error-message">{errors.displayName}</span>}
          </div>

          {/* Phone */}
          <div className="form-group">
            <label htmlFor="phone">Phone Number</label>
            <input
              type="tel"
              id="phone"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="Optional"
              autoComplete="tel"
            />
          </div>

          {/* Business Name */}
          <div className="form-group">
            <label htmlFor="businessName">Business Name</label>
            <input
              type="text"
              id="businessName"
              name="businessName"
              value={formData.businessName}
              onChange={handleChange}
              placeholder="Your company name"
              autoComplete="organization"
            />
          </div>

          {/* Business Type */}
          <div className="form-group">
            <label htmlFor="businessType">Business Type</label>
            <select
              id="businessType"
              name="businessType"
              value={formData.businessType}
              onChange={handleChange}
            >
              <option value="">Select business type</option>
              <option value="Sole Proprietorship">Sole Proprietorship</option>
              <option value="Partnership">Partnership</option>
              <option value="LLC">LLC</option>
              <option value="Corporation">Corporation</option>
              <option value="Other">Other</option>
            </select>
          </div>

          {/* Payout Account */}
          <div className="form-group">
            <label htmlFor="payoutAccountRef">Payout Account Reference</label>
            <input
              type="text"
              id="payoutAccountRef"
              name="payoutAccountRef"
              value={formData.payoutAccountRef}
              onChange={handleChange}
              placeholder="Bank account or payment reference"
            />
          </div>

          {/* Submit Error */}
          {errors.submit && (
            <div className="error-message submit-error">
              {errors.submit}
            </div>
          )}

          {/* Submit Button */}
          <button 
            type="submit" 
            className="btn-primary" 
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner"></span>
                Registering...
              </>
            ) : (
              'Register'
            )}
          </button>
        </form>

        <p className="auth-link">
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;