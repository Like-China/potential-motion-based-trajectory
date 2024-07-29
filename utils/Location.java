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
        // double[] xy = this.MillierConvertion(latitude, longititude);
        // this.x = xy[0];
        // this.y = xy[1];
        this.x = longititude;
        this.y = latitude;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return objectID + "@" + timestamp + "  " + longititude + "  " + latitude + "  " + x + "  " + y;
    }

    public double distTo(Location that) {
        return Math.sqrt(Math.pow(that.x - this.x, 2) + Math.pow(that.y - this.y, 2));
    }

    // distance with (longtitude, latitude) locations
    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double deg2rad_lat1 = deg2rad(lat1);
        double deg2rad_lat2 = deg2rad(lat2);
        double dist = Math.sin(deg2rad_lat1) * Math.sin(deg2rad_lat2)
                + Math.cos(deg2rad_lat1) * Math.cos(deg2rad_lat2)
                        * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        double miles = dist * 60 * 1.1515;
        return miles * 1000;
    }

    public double deg2rad(double degree) {
        return degree / 180 * Math.PI;
    }

    public double rad2deg(double radian) {
        return radian * 180 / Math.PI;
    }

    // map coordinate
    public double[] MillierConvertion(double lat, double lon) {
        double L = 6381372 * Math.PI * 2;
        double W = L;
        double H = L / 2;
        double mill = 2.3;
        double x = lon * Math.PI / 180;
        double y = lat * Math.PI / 180;
        y = 1.25 * Math.log(Math.tan(0.25 * Math.PI + 0.4 * y));
        x = (W / 2) + (W / (2 * Math.PI)) * x;
        y = (H / 2) - (H / (2 * mill)) * y;
        double[] result = new double[2];
        result[0] = x;
        result[1] = y;
        return result;
    }

}