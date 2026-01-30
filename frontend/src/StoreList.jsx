import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function StoreList() {
    const [stores, setStores] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // In a real app, this would come from your Auth context/Login session
    const supplierId = 1;

    useEffect(() => {
        fetchStores();
    }, []);

    const fetchStores = async () => {
        try {
            const response = await fetch(`http://localhost:8081/api/stores/supplier/${supplierId}`);
            if (response.ok) {
                const data = await response.json();
                setStores(data);
            } else {
                console.error("Failed to fetch stores");
            }
        } catch (error) {
            console.error("Error connecting to backend:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <p style={{ textAlign: 'center' }}>Loading your stores...</p>;

    return (
        <div style={{ padding: "20px", maxWidth: "1000px", margin: "0 auto", fontFamily: "Arial" }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
                            <td style={tableCellStyle}>
                                <button
                                    onClick={() => navigate(`/edit-store/${store.storeId}`)}
                                    style={editButtonStyle}
                                >
                                    Edit
                                </button>
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