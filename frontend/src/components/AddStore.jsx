import { useState, useEffect } from 'react';
import { GoogleMap, useLoadScript, Autocomplete, Marker } from '@react-google-maps/api';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../services/axiosConfig";
import BackButton from './BackButton';
import Toast from './Toast';
import './styles/StoreForm.css';

const libraries = ['places']; // Load the "Places" library for Autocomplete
const mapContainerStyle = { width: '100%', height: '300px', marginTop: '10px' };
const center = { lat: 1.3521, lng: 103.8198 }; // Default: Singapore

export default function AddStore() {
    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
        libraries,
    });
    const navigate = useNavigate();
    const [supplierId, setSupplierId] = useState(null);

    // Form State
    // first is variable name, second is setter
    // need to useState so that view gets refreshed everytime new variable is set
    const [storeName, setStoreName] = useState("");
    const [address, setAddress] = useState("");
    const [postalCode, setPostalCode] = useState("");
    const [openingTime, setOpeningTime] = useState("");
    const [closingTime, setClosingTime] = useState("");
    const [description, setDescription] = useState("");
    const [pickupInstructions, setPickupInstructions] = useState("");
    const [coordinates, setCoordinates] = useState(center);
    const [autocomplete, setAutocomplete] = useState(null);

    // Toast state
    const [toast, setToast] = useState({ isOpen: false, message: '', type: 'info' });

    // 1. Handle Address Selection
    // prop name onLoad is fixed by google
    // google would call the onLoad prop and would place their autocomplete logic inside it
    const onLoad = (autoC) => setAutocomplete(autoC);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await axiosInstance.get('/auth/me');
                setSupplierId(response.data.userId ?? response.data.supplierId);
            } catch (error) {
                navigate('/login');
            }
        };
        fetchUser();
    }, [navigate]);

    const onPlaceChanged = () => {
        // this ensures that autocomplete is loaded
        // prevents crashing when user tries to type something before the map script is fully downloaded
        if (autocomplete !== null) {
            // this returns a place object based on what the user selects
            const place = autocomplete.getPlace();
            // checks if selected place has a physical location on a map
            if (place.geometry) {
                // .lat() and .lng() are functions that converts the map point into the decimal numbers
                const lat = place.geometry.location.lat();
                const lng = place.geometry.location.lng();
                const formattedAddress = place.formatted_address;

                // Automatically extract Postal Code from Google Data
                const addressComponents = place.address_components;
                const postcodeObj = addressComponents.find(c => c.types.includes("postal_code"));
                if (postcodeObj) {
                    setPostalCode(postcodeObj.long_name);
                }

                // this sets the data for coordinates and address defined previously in the use state lines
                // coordinates would center the map since it uses useState(center) which is replaced with selected coordinates
                setCoordinates({ lat, lng });
                setAddress(formattedAddress);
            }
        }
    };

    // 2. Handle Submit to Backend
    // ... inside AddStore component
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!supplierId) {
            setToast({ isOpen: true, message: 'Session expired. Please log in again.', type: 'error' });
            setTimeout(() => navigate('/login'), 2000);
            return;
        }

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
            const response = await axiosInstance.post('/stores/create', payload);

            if (response.status === 201 || response.status === 200) {
                setToast({ isOpen: true, message: 'Store created successfully!', type: 'success' });
                setTimeout(() => navigate('/stores'), 1500);
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || "Failed to create store.";
            setToast({ isOpen: true, message: errorMessage, type: 'error' });
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
                    <h2>Add New Store</h2>
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
                        <label>Search Address<span className="required-star">*</span></label>
                        <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                            <input type="text" placeholder="Type location" required />
                        </Autocomplete>
                    </div>

                    <div className="form-field">
                        <label>Postal Code (6 digits)<span className="required-star">*</span></label>
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
                        <label>Description</label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows="3"
                        />
                    </div>

                    <div className="form-field">
                        <label>Pickup Instructions</label>
                        <textarea
                            placeholder="e.g. Please collect from the side door."
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

                    <button type="submit" className="submit-btn">
                        Save Store
                    </button>
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
