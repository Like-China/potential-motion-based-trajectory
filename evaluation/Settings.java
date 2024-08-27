package evaluation;

public class Settings {
        public static int expNB = 10;
        // the number of processed moving objects
        public static int objNB = 6000;
        public static int poiNB = 400000;
        // the default threshold of similarity
        public static double simThreshold = 0.5;
        public static int start = 0;
        public static int end = 1;
        // kNN k
        public static int k = 300;
        public static String data = "Geolife";
        // public static String data = "Porto";
        // shuffle data
        public static boolean isShuffle = false;
        // the number of partitions of a time-interval
        public static int intervalNum = 10;
        // the default sampling POI number
        public static int tsNB = 20;
        // the node size in the balltree
        public static int minLeafNB = 10;
        public static String geolifePath = "/home/like/data/trajectory/Geolife/Data/";
        public static String portoPath = "/home/like/data/trajectory/porto.csv";
        public static double repartitionRatio = 0.5;
}