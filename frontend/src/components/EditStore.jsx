import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { GoogleMap, useLoadScript, Autocomplete, Marker } from '@react-google-maps/api';
import axiosInstance from "../services/axiosConfig";
import BackButton from './BackButton';
import Toast from './Toast';
import './styles/StoreForm.css';

const libraries = ['places'];
const mapContainerStyle = { width: '100%', height: '300px', marginTop: '10px' };

export default function EditStore() {
    const { storeId } = useParams(); // Extracts the ID from the URL
    const navigate = useNavigate();

    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
        libraries,
    });

    // State matching your StoreRequest DTO
    const [storeName, setStoreName] = useState("");
    const [address, setAddress] = useState("");
    const [postalCode, setPostalCode] = useState("");
    const [openingTime, setOpeningTime] = useState("");
    const [closingTime, setClosingTime] = useState("");
    const [description, setDescription] = useState("");
    const [pickupInstructions, setPickupInstructions] = useState("");
    const [coordinates, setCoordinates] = useState({ lat: 1.3521, lng: 103.8198 });
    const [autocomplete, setAutocomplete] = useState(null);
    const [supplierId, setSupplierId] = useState(null);

    // Toast state
    const [toast, setToast] = useState({ isOpen: false, message: '', type: 'info' });

    // 1. Fetch existing data on page load
    useEffect(() => {
        const initializeData = async () => {
            try {
                // 2. First, get the current logged-in user
                const userResponse = await axiosInstance.get('/auth/me');
                setSupplierId(userResponse.data.userId ?? userResponse.data.supplierId);

                // 3. Then, fetch the existing store data
                const storeResponse = await axiosInstance.get(`/stores/${storeId}`);
                const data = storeResponse.data;

                setStoreName(data.storeName);
                setAddress(data.addressLine);
                setPostalCode(data.postalCode);
                if (data.openingHours && data.openingHours.includes("-")) {
                    const parts = data.openingHours.split("-").map(s => s.trim());
                    setOpeningTime(parts[0] ?? "");
                    setClosingTime(parts[1] ?? "");
                }
                setDescription(data.description);
                setPickupInstructions(data.pickupInstructions);
                setCoordinates({ lat: data.lat, lng: data.lng });
            } catch (error) {
                console.error("Initialization error:", error);
                // Redirect if unauthorized or store not found
                navigate('/login');
            }
        };

        if (storeId) initializeData();
    }, [storeId, navigate]);

    const onPlaceChanged = () => {
        if (autocomplete !== null) {
            const place = autocomplete.getPlace();
            if (place.geometry) {
                const lat = place.geometry.location.lat();
                const lng = place.geometry.location.lng();
                const addressComponents = place.address_components;
                const postcodeObj = addressComponents.find(c => c.types.includes("postal_code"));

                setCoordinates({ lat, lng });
                setAddress(place.formatted_address);
                if (postcodeObj) setPostalCode(postcodeObj.long_name);
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (openingTime && closingTime && closingTime <= openingTime) {
            setToast({ isOpen: true, message: 'Closing time must be later than opening time.', type: 'warning' });
            return;
        }

        const openingHours = openingTime && closingTime
            ? `${openingTime} - ${closingTime}`
            : "";

        const payload = {
            supplierId: supplierId,
            storeName: storeName,
            addressLine: address,
            postalCode: postalCode,
            lat: coordinates.lat,
            lng: coordinates.lng,
            openingHours: openingHours,
            description: description,
            pickupInstructions: pickupInstructions
        };

        try {
            const response = await axiosInstance.put(`/stores/update/${storeId}`, payload);

            if (response.status === 200) {
                setToast({ isOpen: true, message: 'Store updated successfully!', type: 'success' });
                setTimeout(() => navigate('/stores'), 1500);
            }
        } catch (error) {
            console.error("Error updating store:", error);
            setToast({ isOpen: true, message: 'Failed to update store.', type: 'error' });
        }
    };

    if (loadError) {
        return (
            <div className="error-maps">
                Error loading maps
            </div>
        );
    }

    if (!isLoaded) {
        return (
            <div className="loading-maps">
                <div className="loading-spinner"></div>
                <p>Loading Maps...</p>
            </div>
        );
    }

    return (
        <div className="store-form-container">
            <div className="store-form-content">
                <div className="store-form-header">
                    <BackButton to="/stores" variant="primary" />
                    <h2>Edit Store: {storeName}</h2>
                </div>
                <form onSubmit={handleSubmit} className="store-form">
                    <div className="form-field">
                        <label>Store Name<span className="required-star">*</span></label>
                        <input
                            type="text"
                            value={storeName}
                            onChange={(e) => setStoreName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-field">
                        <label>Address (Search to update location)</label>
                        <Autocomplete onLoad={setAutocomplete} onPlaceChanged={onPlaceChanged}>
                            <input type="text" placeholder={address} />
                        </Autocomplete>
                        <span className="current-address">Current: {address}</span>
                    </div>

                    <div className="form-field">
                        <label>Postal Code<span className="required-star">*</span></label>
                        <input
                            type="text"
                            value={postalCode}
                            onChange={(e) => setPostalCode(e.target.value)}
                            maxLength="6"
                            required
                        />
                    </div>

                    <div className="form-field">
                        <label>Opening Hours</label>
                        <div className="time-inputs">
                            <input
                                type="time"
                                value={openingTime}
                                onChange={(e) => setOpeningTime(e.target.value)}
                            />
                            <input
                                type="time"
                                value={closingTime}
                                onChange={(e) => setClosingTime(e.target.value)}
                                min={openingTime || undefined}
                            />
                        </div>
                        <span className="form-hint">Closing time must be later than opening time.</span>
                    </div>

                    <div className="form-field">
                        <label>Pickup Instructions</label>
                        <textarea
                            value={pickupInstructions}
                            onChange={(e) => setPickupInstructions(e.target.value)}
                            rows="2"
                        />
                    </div>

                    <div className="map-container">
                        <GoogleMap mapContainerStyle={mapContainerStyle} zoom={15} center={coordinates}>
                            <Marker position={coordinates} />
                        </GoogleMap>
                    </div>

                    <div className="button-group">
                        <button type="submit" className="submit-btn">
                            Update Store
                        </button>
                        <button
                            type="button"
                            onClick={() => navigate('/stores')}
                            className="cancel-btn"
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>

            <Toast
                isOpen={toast.isOpen}
                onClose={() => setToast({ ...toast, isOpen: false })}
                message={toast.message}
                type={toast.type}
            />
        </div>
    );
}
