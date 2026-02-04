import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../services/axiosConfig";

export default function StoreList() {
    const [stores, setStores] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const [supplierId, setSupplierId] = useState(null);

    useEffect(() => {
        const getSessionUser = async () => {
            try {
                // Fetch the logged-in user's details
                const response = await axiosInstance.get('/auth/me');
                const id = response.data.supplierId;
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

    if (loading) return <p style={{ textAlign: 'center' }}>Loading your stores...</p>;

    const handleDelete = async (storeId) => {
        const confirmed = window.confirm("Are you sure you want to delete this store? This action cannot be undone.");

        if (confirmed) {
            try {
                // axiosInstance already has the baseURL (http://localhost:8080/api)
                // and includes withCredentials: true
                const response = await axiosInstance.delete(`/stores/delete/${storeId}`);

                // Axios treats non-2xx status codes as errors, so if we reach here, it's a 200 OK
                alert("Store deleted successfully.");
                setStores(stores.filter(store => store.storeId !== storeId));
            } catch (error) {
                console.error("Error deleting store:", error);
                const message = error.response?.data?.message || "Failed to delete the store.";
                alert(message);
            }
        }
    };

    return (
        <div style={{ padding: "20px", maxWidth: "1000px", margin: "0 auto", fontFamily: "Arial" }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                    <button onClick={() => navigate('/dashboard')} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '20px' }}>
                        ⬅️
                    </button>
                </div>
                <h2>My Stores</h2>
                <button
                    onClick={() => navigate('/add-store')}
                    style={{ padding: "10px", background: "#007bff", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
                >
                    + Add New Store
                </button>
            </div>

            {stores.length === 0 ? (
                <p>No stores found. Start by adding one!</p>
            ) : (
                <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "20px" }}>
                    <thead>
                    <tr style={{ backgroundColor: "#f4f4f4", textAlign: "left" }}>
                        <th style={tableHeaderStyle}>Name</th>
                        <th style={tableHeaderStyle}>Address</th>
                        <th style={tableHeaderStyle}>Postal Code</th>
                        <th style={tableHeaderStyle}>Pickup Instructions</th>
                        <th style={tableHeaderStyle}>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {stores.map((store) => (
                        <tr key={store.storeId} style={{ borderBottom: "1px solid #ddd" }}>
                            <td style={tableCellStyle}><strong>{store.storeName}</strong></td>
                            <td style={tableCellStyle}>{store.addressLine}</td>
                            <td style={tableCellStyle}>{store.postalCode}</td>
                            <td style={tableCellStyle}>
                                <small style={{ color: "#555" }}>{store.pickupInstructions || "No instructions provided"}</small>
                            </td>
                            {/* Combine all actions into this single cell */}
                            <td style={tableCellStyle}>
                                <div style={{ display: 'flex', gap: '8px' }}>
                                    <button
                                        onClick={() => navigate(`/edit-store/${store.storeId}`)}
                                        style={editButtonStyle}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        onClick={() => handleDelete(store.storeId)}
                                        style={deleteButtonStyle}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

// Minimal Styles
const tableHeaderStyle = { padding: "12px", borderBottom: "2px solid #ddd" };
const tableCellStyle = { padding: "12px" };
const editButtonStyle = { background: "#ffc107", border: "none", padding: "5px 10px", cursor: "pointer", borderRadius: "4px" };
const deleteButtonStyle = { background: "#dc3545", color: "white", border: "none", padding: "5px 10px", cursor: "pointer", borderRadius: "4px" };