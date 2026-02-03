import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../services/authService';

const ProtectedRoute = ({ children }) => {
  const [loading, setLoading] = useState(true);
  const [isAuth, setIsAuth] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      try {
       
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        if (isLoggedIn) {
          console.log('User authenticated via localStorage');
          setIsAuth(true);
        } else {
         
          const auth = await authService.isAuthenticated();
          console.log('Authentication status from server:', auth);
          setIsAuth(auth);
        }
      } catch (error) {
        console.error('Auth check failed:', error);
        setIsAuth(false);
      } finally {
        setLoading(false);
      }
    };
    
    checkAuth();
  }, []);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner-large"></div>
        <p>Checking authentication...</p>
      </div>
    );
  }

  if (!isAuth) {
    console.log('Not authenticated, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;