import React from 'react';
import PropTypes from 'prop-types';

const ConfirmDialog = ({ isOpen, onClose, onConfirm, title, message }) => {
  if (!isOpen) return null;

  return (
    <div className="confirm-dialog-overlay" onClick={onClose}>
      <div className="confirm-dialog" onClick={(e) => e.stopPropagation()}>
        <h3>{title || '确认操作'}</h3>
        <p>{message || '您确定要执行此操作吗？'}</p>
        <div className="confirm-dialog-actions">
          <button onClick={onClose} className="btn-cancel">
            取消
          </button>
          <button onClick={onConfirm} className="btn-confirm">
            确认
          </button>
        </div>
      </div>
    </div>
  );
};

ConfirmDialog.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func,
  title: PropTypes.string,
  message: PropTypes.string,
};

export default ConfirmDialog;
