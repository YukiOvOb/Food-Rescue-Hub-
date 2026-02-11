import React, { useEffect } from 'react';
import PropTypes from 'prop-types';

const Toast = ({ message, type = 'info', duration = 3000, onClose }) => {
  useEffect(() => {
    if (duration && onClose) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  if (!message) return null;

  return (
    <div className={`toast toast-${type}`}>
      <div className="toast-content">
        <span className="toast-message">{message}</span>
        {onClose && (
          <button onClick={onClose} className="toast-close">
            ×
          </button>
        )}
      </div>
    </div>
  );
};

Toast.propTypes = {
  message: PropTypes.string,
  type: PropTypes.oneOf(['info', 'success', 'warning', 'error']),
  duration: PropTypes.number,
  onClose: PropTypes.func,
};

export default Toast;
