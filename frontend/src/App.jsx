import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';
import StoreList from './components/StoreList';
import AddStore from './components/AddStore';
import EditStore from './components/EditStore';

import './App.css';

function App() {
    return (
        <Router>
            <div className="App">
                <Routes>
                    {/* Public Routes */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Protected Routes */}
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <Dashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* 2. Store Management Routes */}
                    <Route
                        path="/stores"
                        element={
                            <ProtectedRoute>
                                <StoreList />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/add-store"
                        element={
                            <ProtectedRoute>
                                <AddStore />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/edit-store/:storeId"
                        element={
                            <ProtectedRoute>
                                <EditStore />
                            </ProtectedRoute>
                        }
                    />

                    {/* Default Route - Redirect to login */}
                    <Route path="/" element={<Navigate to="/login" replace />} />

                    {/* Catch all - Redirect to login */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;