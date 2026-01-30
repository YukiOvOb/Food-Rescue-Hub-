import React, { useState , useEffect} from 'react';
import { GoogleMap, useLoadScript, Autocomplete, Marker } from '@react-google-maps/api';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../services/axiosConfig";

const libraries = ['places']; // Load the "Places" library for Autocomplete
const mapContainerStyle = { width: '100%', height: '300px', marginTop: '10px' };
const center = { lat: 1.3521, lng: 103.8198 }; // Default: Singapore

export default function AddStore() {
    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
        libraries,
    });
    console.log("Key Loaded:", import.meta.env.VITE_GOOGLE_MAPS_API_KEY ? "Yes" : "No");
    const navigate = useNavigate();
    const [supplierId, setSupplierId] = useState(null);

    // Form State
    // first is variable name, second is setter
    // need to useState so that view gets refreshed everytime new variable is set
    const [storeName, setStoreName] = useState("");
    const [address, setAddress] = useState("");
    const [postalCode, setPostalCode] = useState("");
    const [openingHours, setOpeningHours] = useState("");
    const [description, setDescription] = useState("");
    const [pickupInstructions, setPickupInstructions] = useState("");
    const [coordinates, setCoordinates] = useState(center);
    const [autocomplete, setAutocomplete] = useState(null);

    // 1. Handle Address Selection
    // prop name onLoad is fixed by google
    // google would call the onLoad prop and would place their autocomplete logic inside it
    const onLoad = (autoC) => setAutocomplete(autoC);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await axiosInstance.get('/auth/me');
                setSupplierId(response.data.supplierId);
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
                console.log("Selected:", lat, lng, formattedAddress);
            }
        }
    };

    // 2. Handle Submit to Backend
    // ... inside AddStore component
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!supplierId) {
            alert("Session expired. Please log in again.");
            navigate('/login');
            return;
        }

        const payload = {
            supplierId: supplierId, // Use the dynamic ID from session
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
            // Axios handles JSON stringification automatically
            const response = await axiosInstance.post('/stores/create', payload);

            if (response.status === 201 || response.status === 200) {
                alert("Store Created Successfully!");
                navigate('/stores');
            }
        } catch (error) {
            console.error("Error creating store:", error);
            const errorMessage = error.response?.data?.message || "Failed to create store.";
            alert("Error:\n" + errorMessage);
        }
    };

    if (loadError) return "Error loading maps";
    if (!isLoaded) return "Loading Maps...";

    return (

        <div style={{ padding: "20px", maxWidth: "600px", margin: "0 auto", fontFamily: "Arial" }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '20px' }}>
                <button
                    type="button"
                    onClick={() => navigate('/stores')}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '20px', padding: 0 }}
                >
                    ⬅️
                </button>
                <h2 style={{ margin: 0 }}>Add New Store</h2>
            </div>
            <form onSubmit={handleSubmit}>
                <div style={inputGroupStyle}>
                    <label>Store Name*:</label>
                    <input type="text" value={storeName} onChange={(e) => setStoreName(e.target.value)} required style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Search Address*:</label>
                    <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                        <input type="text" placeholder="Type location" style={inputStyle} required />
                    </Autocomplete>
                </div>

                {/* Postal Code - Usually auto-filled by Google, but editable just in case */}
                <div style={inputGroupStyle}>
                    <label>Postal Code (6 digits)*:</label>
                    <input type="text" value={postalCode} onChange={(e) => setPostalCode(e.target.value)} maxLength="6" required style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Opening Hours:</label>
                    <input type="text" value={openingHours} placeholder="e.g. 09:00 - 21:00" onChange={(e) => setOpeningHours(e.target.value)} style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Description:</label>
                    <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows="3" style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Pickup Instructions:</label>
                    <textarea
                        placeholder="e.g. Please collect from the side door."
                        value={pickupInstructions}
                        onChange={(e) => setPickupInstructions(e.target.value)}
                        rows="2"
                        style={inputStyle}
                    />
                </div>

                <div style={{ marginBottom: "15px", border: "1px solid #ccc" }}>
                    <GoogleMap mapContainerStyle={mapContainerStyle} zoom={15} center={coordinates}>
                        <Marker position={coordinates} />
                    </GoogleMap>
                </div>

                <button type="submit" style={submitButtonStyle}>
                    Save Store
                </button>
            </form>
        </div>
    );
}

// Simple Styles
const inputGroupStyle = { marginBottom: "15px" };
const inputStyle = { width: "100%", padding: "8px", boxSizing: "border-box" };
const submitButtonStyle = { width: "100%", padding: "10px", background: "#28a745", color: "white", border: "none", cursor: "pointer", fontWeight: "bold" };