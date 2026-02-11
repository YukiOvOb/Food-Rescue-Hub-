import React from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';

const BackButton = ({ to, label = '返回' }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    if (to) {
      navigate(to);
    } else {
      navigate(-1);
    }
  };

  return (
    <button onClick={handleClick} className="btn-back">
      ← {label}
    </button>
  );
};

BackButton.propTypes = {
  to: PropTypes.string,
  label: PropTypes.string,
};

export default BackButton;
