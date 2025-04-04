import React from "react";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";

const MapContainer: React.FC = () => {
    const storeLocation = { lat: 21.028511, lng: 105.804817 };

    const mapContainerStyle = {
        width: "100%",
        height: "400px",
    };

    const mapOptions = {
        zoom: 15,
        center: storeLocation,
    };

    return (
        <LoadScript googleMapsApiKey={process.env.REACT_APP_GOOGLE_MAP_API_KEY ?? ""}>
            <GoogleMap
                mapContainerStyle={mapContainerStyle}
                center={mapOptions.center}
                zoom={mapOptions.zoom}
            >
                <Marker position={storeLocation} />
            </GoogleMap>
        </LoadScript>
    );
};

export default MapContainer;
