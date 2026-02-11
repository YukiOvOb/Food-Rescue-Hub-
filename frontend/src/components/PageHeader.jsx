import React from 'react';
import PropTypes from 'prop-types';

const PageHeader = ({ title, subtitle, actions, children }) => {
  return (
    <div className="page-header">
      <div className="page-header-content">
        <div className="page-header-text">
          {title && <h1 className="page-title">{title}</h1>}
          {subtitle && <p className="page-subtitle">{subtitle}</p>}
        </div>
        {actions && <div className="page-header-actions">{actions}</div>}
      </div>
      {children}
    </div>
  );
};

PageHeader.propTypes = {
  title: PropTypes.string,
  subtitle: PropTypes.string,
  actions: PropTypes.node,
  children: PropTypes.node,
};

export default PageHeader;
