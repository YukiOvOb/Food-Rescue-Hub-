import React, { useState } from 'react';
import { GoogleMap, useLoadScript, Autocomplete, Marker } from '@react-google-maps/api';

const libraries = ['places']; // Load the "Places" library for Autocomplete
const mapContainerStyle = { width: '100%', height: '300px', marginTop: '10px' };
const center = { lat: 1.3521, lng: 103.8198 }; // Default: Singapore

export default function AddStore() {
    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
        libraries,
    });

    // Form State
    // first is variable name, second is setter
    // need to useState so that view gets refreshed everytime new variable is set
    const [storeName, setStoreName] = useState("");
    const [address, setAddress] = useState("");
    const [coordinates, setCoordinates] = useState(center);
    const [autocomplete, setAutocomplete] = useState(null);

    // 1. Handle Address Selection
    // prop name onLoad is fixed by google
    // google would call the onLoad prop and would place their autocomplete logic inside it
    const onLoad = (autoC) => setAutocomplete(autoC);

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

                // this sets the data for coordinates and address defined previously in the use state lines
                // coordinates would center the map since it uses useState(center) which is replaced with selected coordinates
                setCoordinates({ lat, lng });
                setAddress(formattedAddress);
                console.log("Selected:", lat, lng, formattedAddress);
            }
        }
    };

    // 2. Handle Submit to Backend
    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            supplierId: 1, // Hardcoded for now (replace with User ID later)
            storeName: storeName,
            addressLine: address,
            lat: coordinates.lat,
            lng: coordinates.lng,
            openingHours: "09:00 - 21:00", // Default or add input field
            description: "Fresh bakery goods"
        };

        try {
            const response = await fetch('http://localhost:8081/api/stores/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                alert("Store Created Successfully!");
            } else {
                alert("Failed to create store.");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Server Error");
        }
    };

    if (loadError) return "Error loading maps";
    if (!isLoaded) return "Loading Maps...";

    return (
        <div style={{ padding: "20px", maxWidth: "600px", margin: "0 auto" }}>
            <h2>Add New Store</h2>
            <form onSubmit={handleSubmit}>
                {/* Store Name */}
                <div style={{ marginBottom: "15px" }}>
                    <label>Store Name:</label>
                    <input
                        type="text"
                        value={storeName}
                        onChange={(e) => setStoreName(e.target.value)}
                        required
                        style={{ width: "100%", padding: "8px" }}
                    />
                </div>

                {/* Google Autocomplete Address */}
                <div style={{ marginBottom: "15px" }}>
                    <label>Search Address:</label>
                    <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                        <input
                            type="text"
                            placeholder="Type location"
                            style={{ width: "100%", padding: "8px" }}
                        />
                    </Autocomplete>
                </div>

                {/* The Map Visual */}
                <div style={{ marginBottom: "15px", border: "1px solid #ccc" }}>
                    <GoogleMap
                        mapContainerStyle={mapContainerStyle}
                        zoom={15}
                        center={coordinates}
                    >
                        <Marker position={coordinates} />
                    </GoogleMap>
                </div>

                {/* Read-Only Coordinates (Optional Debugging) */}
                <p style={{ fontSize: "12px", color: "#666" }}>
                    Selected: {coordinates.lat.toFixed(6)}, {coordinates.lng.toFixed(6)}
                </p>

                <button type="submit" style={{ padding: "10px 20px", background: "green", color: "white", border: "none" }}>
                    Save Store
                </button>
            </form>
        </div>
    );
}