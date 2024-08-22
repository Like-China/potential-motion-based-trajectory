package utils;

public class Location {
    public int objectID;
    public double longititude;
    public double latitude;
    public double x;
    public double y;
    public int timestamp;

    public Location(int objectID, double longititude, double latitude, int timestamp) {
        this.objectID = objectID;
        this.longititude = longititude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        double[] xy = this.convertToCartesian(latitude, longititude);
        this.x = xy[0];
        this.y = xy[1];
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return objectID + "@" + timestamp + "  " + longititude + "  " + latitude + "  " + x + "  " + y;
    }

    public double distTo(Location that) {
        // Distance in meters
        return Math.sqrt(Math.pow(that.x - this.x, 2) + Math.pow(that.y - this.y, 2));
    }

    /**
     * Calculates the distance between two points on the Earth's surface specified
     * by latitude and longitude.
     * 
     * @param lat1 Latitude of the first point in degrees
     * @param lon1 Longitude of the first point in degrees
     * @param lat2 Latitude of the second point in degrees
     * @param lon2 Longitude of the second point in degrees
     * @return Distance in kilometers
     */
    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double EARTH_RADIUS_KM = 6371.0;
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differences in coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        return EARTH_RADIUS_KM * c;
    }

    /*
     * 
     * Converts latitude and longitude to Cartesian coordinates (x, y).
     *
     * @param latitude Latitude in degrees
     * 
     * @param longitude Longitude in degrees
     * 
     * @return An array where index 0 is x and index 1 is y
     */
    public double[] convertToCartesian(double latitude, double longitude) {
        double EARTH_RADIUS_KM = 6371.0;
        // Convert latitude and longitude from degrees to radians
        double latRad = Math.toRadians(latitude);
        double lonRad = Math.toRadians(longitude);
        // Calculate Cartesian coordinates
        double x = EARTH_RADIUS_KM * Math.cos(latRad) * Math.cos(lonRad);
        double y = EARTH_RADIUS_KM * Math.cos(latRad) * Math.sin(lonRad);
        return new double[] { x, y };
    }
}