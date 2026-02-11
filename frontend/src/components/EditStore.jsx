import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { GoogleMap, useLoadScript, Autocomplete, Marker } from '@react-google-maps/api';
import axiosInstance from "../services/axiosConfig";

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
            alert("Closing time must be later than opening time.");
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
                alert("Store Updated Successfully!");
                navigate('/stores');
            }
        } catch (error) {
            alert("Update failed.");
        }
    };

    if (loadError) return "Error loading maps";
    if (!isLoaded) return "Loading Maps...";

    return (
        <div style={{ padding: "20px", maxWidth: "600px", margin: "0 auto", fontFamily: "Arial" }}>
            <h2>Edit Store: {storeName}</h2>
            <form onSubmit={handleSubmit}>
                <div style={inputGroupStyle}>
                    <label>Store Name*:</label>
                    <input type="text" value={storeName} onChange={(e) => setStoreName(e.target.value)} required style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Address (Search to update location):</label>
                    <Autocomplete onLoad={setAutocomplete} onPlaceChanged={onPlaceChanged}>
                        <input type="text" placeholder={address} style={inputStyle} />
                    </Autocomplete>
                    <small>Current: {address}</small>
                </div>

                <div style={inputGroupStyle}>
                    <label>Postal Code*:</label>
                    <input type="text" value={postalCode} onChange={(e) => setPostalCode(e.target.value)} maxLength="6" required style={inputStyle} />
                </div>

                <div style={inputGroupStyle}>
                    <label>Opening Hours:</label>
                    <div style={{ display: "flex", gap: "10px" }}>
                        <input
                            type="time"
                            value={openingTime}
                            onChange={(e) => setOpeningTime(e.target.value)}
                            style={inputStyle}
                        />
                        <input
                            type="time"
                            value={closingTime}
                            onChange={(e) => setClosingTime(e.target.value)}
                            style={inputStyle}
                            min={openingTime || undefined}
                        />
                    </div>
                    <small>Closing time must be later than opening time.</small>
                </div>

                <div style={inputGroupStyle}>
                    <label>Pickup Instructions:</label>
                    <textarea value={pickupInstructions} onChange={(e) => setPickupInstructions(e.target.value)} rows="2" style={inputStyle} />
                </div>

                <div style={{ marginBottom: "15px", border: "1px solid #ccc" }}>
                    <GoogleMap mapContainerStyle={mapContainerStyle} zoom={15} center={coordinates}>
                        <Marker position={coordinates} />
                    </GoogleMap>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                    <button type="submit" style={{ ...submitButtonStyle, background: "#ffc107", color: "black" }}>Update Store</button>
                    <button type="button" onClick={() => navigate('/stores')} style={{ ...submitButtonStyle, background: "#ccc", color: "black" }}>Cancel</button>
                </div>
            </form>
        </div>
    );
}

const inputGroupStyle = { marginBottom: "15px" };
const inputStyle = { width: "100%", padding: "8px", boxSizing: "border-box" };
const submitButtonStyle = { flex: 1, padding: "10px", border: "none", cursor: "pointer", fontWeight: "bold" };
