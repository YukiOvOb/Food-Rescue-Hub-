import React, { useState } from 'react';
import authService from '../services/authService';

const DiagnosticsPage = () => {
  const [output, setOutput] = useState('');
  const [loading, setLoading] = useState(false);

  const testLogin = async () => {
    setLoading(true);
    setOutput('Testing login flow...\n');
    
    try {
      
      localStorage.clear();
      setOutput(prev => prev + '✓ Cleared localStorage\n');

      
      console.log('Sending login request...');
      const response = await authService.login({
        email: 'bakery@breadtalk.sg',
        password: 'password123'
      });
      
      setOutput(prev => prev + `✓ Login response received:\n${JSON.stringify(response, null, 2)}\n\n`);

      
      const user = localStorage.getItem('user');
      const isLoggedIn = localStorage.getItem('isLoggedIn');
      setOutput(prev => prev + `✓ localStorage.user: ${user}\n`);
      setOutput(prev => prev + `✓ localStorage.isLoggedIn: ${isLoggedIn}\n\n`);

     
      console.log('Testing /auth/me endpoint...');
      const meResponse = await authService.getCurrentUser();
      setOutput(prev => prev + `✓ /auth/me response:\n${JSON.stringify(meResponse, null, 2)}\n\n`);

      
      const isAuth = await authService.isAuthenticated();
      setOutput(prev => prev + `✓ isAuthenticated: ${isAuth}\n`);

    } catch (error) {
      console.error('Test failed:', error);
      setOutput(prev => prev + `✗ Error: ${error.message}\n`);
      if (error.response) {
        setOutput(prev => prev + `Status: ${error.response.status}\n`);
        setOutput(prev => prev + `Data: ${JSON.stringify(error.response.data)}\n`);
      }
    } finally {
      setLoading(false);
    }
  };

  const checkLocalStorage = () => {
    const user = localStorage.getItem('user');
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    setOutput(`Current localStorage:\nuser: ${user}\nisLoggedIn: ${isLoggedIn}`);
  };

  const clearStorage = () => {
    localStorage.clear();
    setOutput('Cleared all localStorage');
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'monospace' }}>
      <h1>Login Diagnostics</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <button onClick={testLogin} disabled={loading} style={{ marginRight: '10px', padding: '10px 20px' }}>
          {loading ? 'Testing...' : 'Test Full Login Flow'}
        </button>
        <button onClick={checkLocalStorage} style={{ marginRight: '10px', padding: '10px 20px' }}>
          Check localStorage
        </button>
        <button onClick={clearStorage} style={{ padding: '10px 20px' }}>
          Clear Storage
        </button>
      </div>

      <pre style={{
        backgroundColor: '#f0f0f0',
        padding: '15px',
        borderRadius: '5px',
        maxHeight: '500px',
        overflow: 'auto',
        whiteSpace: 'pre-wrap',
        wordWrap: 'break-word'
      }}>
        {output}
      </pre>
    </div>
  );
};

export default DiagnosticsPage;
