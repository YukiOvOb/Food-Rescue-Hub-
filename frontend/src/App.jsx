import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

// Component Imports
import StoreList from './components/StoreList';
import AddStore from './components/AddStore';
import EditStore from './components/EditStore';
import ListingsPage from './pages/ListingsPage';
import OrdersPage from './pages/OrdersPage';
import DiagnosticsPage from './pages/DiagnosticsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import QRCodeDecoder from './components/QRCodeDecoder';

import './App.css';

function App() {
    return (
        <Router>
            <div className="App">
                <Routes>
                    {/* Public Routes */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/diagnostics" element={<DiagnosticsPage />} />

                    {/* Protected Routes */}
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <Dashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* Listings Management */}
                    <Route
                        path="/listings"
                        element={
                            <ProtectedRoute>
                                <ListingsPage />
                            </ProtectedRoute>
                        }
                    />

                    {/* Orders Management */}
                    <Route
                        path="/orders"
                        element={
                            <ProtectedRoute>
                                <OrdersPage />
                            </ProtectedRoute>
                        }
                    />

                    {/* Analytics */}
                    <Route
                        path="/analytics"
                        element={
                            <ProtectedRoute>
                                <AnalyticsPage />
                            </ProtectedRoute>
                        }
                    />

                    {/* Store Management */}
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

                    <Route
                        path="/qr-decoder"
                        element={
                            <ProtectedRoute>
                                <QRCodeDecoder />
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