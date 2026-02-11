import React, { useState } from 'react';
import PropTypes from 'prop-types';
import './styles/CancelOrderDialog.css';

const CancelOrderDialog = ({ isOpen, onClose, onConfirm, orderId }) => {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (!reason.trim()) {
      setError('Please enter a cancellation reason');
      return;
    }
    onConfirm(reason);
    setReason('');
    setError('');
  };

  const handleClose = () => {
    setReason('');
    setError('');
    onClose();
  };

  return (
    <div className="cancel-dialog-overlay" onClick={handleClose}>
      <div className="cancel-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="cancel-dialog-header">
          <h3>Cancel Order #{orderId}</h3>
          <button className="close-btn" onClick={handleClose}>×</button>
        </div>

        <div className="cancel-dialog-body">
          <p className="cancel-dialog-message">
            Please provide a reason for cancelling this order. This will be sent to the customer.
          </p>

          <textarea
            className="cancel-reason-input"
            placeholder="Enter cancellation reason..."
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              setError('');
            }}
            rows={4}
            autoFocus
          />

          {error && <p className="error-text">{error}</p>}
        </div>

        <div className="cancel-dialog-actions">
          <button onClick={handleClose} className="btn-secondary">
            Keep Order
          </button>
          <button onClick={handleConfirm} className="btn-danger">
            Cancel Order
          </button>
        </div>
      </div>
    </div>
  );
};

CancelOrderDialog.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  orderId: PropTypes.number,
};

export default CancelOrderDialog;
