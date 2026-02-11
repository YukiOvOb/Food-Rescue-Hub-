import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../services/axiosConfig";
import PageHeader from './PageHeader';
import ConfirmDialog from './ConfirmDialog';
import Toast from './Toast';
import './styles/StoreList.css';

export default function StoreList() {
    const [stores, setStores] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const [supplierId, setSupplierId] = useState(null);

    // Dialog states
    const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, storeId: null });
    const [toast, setToast] = useState({ isOpen: false, message: '', type: 'info' });

    useEffect(() => {
        const getSessionUser = async () => {
            try {
                // Fetch the logged-in user's details
                const response = await axiosInstance.get('/auth/me');
                const id = response.data.userId ?? response.data.supplierId;
                setSupplierId(id);
                fetchStores(id); // Fetch stores for THIS specific supplier
            } catch (error) {
                navigate('/login');
            }
        };
        getSessionUser();
    }, [navigate]);

    const fetchStores = async (id) => {
        try {
            // Use axiosInstance - it already knows about localhost:8080/api
            const response = await axiosInstance.get(`/stores/supplier/${id}`);
            setStores(response.data);
        } catch (error) {
            console.error("Error fetching stores:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="loading-message">
                <div className="loading-spinner"></div>
                <p>Loading your stores...</p>
            </div>
        );
    }

    const handleDelete = async (storeId) => {
        setConfirmDialog({ isOpen: true, storeId });
    };

    const confirmDelete = async () => {
        const storeId = confirmDialog.storeId;
        try {
            await axiosInstance.delete(`/stores/delete/${storeId}`);
            setStores(stores.filter(store => store.storeId !== storeId));
            setToast({ isOpen: true, message: 'Store deleted successfully!', type: 'success' });
        } catch (error) {
            console.error("Error deleting store:", error);
            const message = error.response?.data?.message || "Failed to delete the store.";
            setToast({ isOpen: true, message, type: 'error' });
        }
    };

    return (
        <div className="store-list-container">
            {/* Header Banner */}
            <div className="store-header-banner">
                <div className="banner-content">
                    <div className="banner-left">
                        <span className="banner-tag">SUPPLIER</span>
                        <h1 className="banner-title">My Stores</h1>
                    </div>
                    <div className="banner-right">
                        <button
                            onClick={() => navigate('/dashboard')}
                            className="btn-back-dashboard"
                        >
                            ← Back to Dashboard
                        </button>
                        <button
                            onClick={() => navigate('/add-store')}
                            className="btn-add-store"
                        >
                            + Add New Store
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="store-list-content">
                {stores.length === 0 ? (
                    <div className="no-stores-message">
                        <p>No stores found. Start by adding one!</p>
                    </div>
                ) : (
                    <div className="stores-table-wrapper">
                        <table className="stores-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Address</th>
                                <th>Postal Code</th>
                                <th>Pickup Instructions</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {stores.map((store) => (
                                <tr key={store.storeId}>
                                    <td><span className="store-name">{store.storeName}</span></td>
                                    <td>{store.addressLine}</td>
                                    <td>{store.postalCode}</td>
                                    <td>
                                        <span className="pickup-instructions">
                                            {store.pickupInstructions || "No instructions provided"}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="store-actions">
                                            <button
                                                onClick={() => navigate(`/edit-store/${store.storeId}`)}
                                                className="btn-edit"
                                            >
                                                Edit
                                            </button>
                                            <button
                                                onClick={() => handleDelete(store.storeId)}
                                                className="btn-delete"
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            <ConfirmDialog
                isOpen={confirmDialog.isOpen}
                onClose={() => setConfirmDialog({ isOpen: false, storeId: null })}
                onConfirm={confirmDelete}
                title="Delete Store"
                message="Are you sure you want to delete this store? This action cannot be undone."
            />

            {toast.isOpen && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast({ ...toast, isOpen: false })}
                />
            )}
        </div>
    );
}
