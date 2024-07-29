package evaluation;

public class Settings {
        // public static String data = "Geolife";
        public static String data = "Porto";
        // shuffle data
        public static boolean isShuffle = true;
        // the number of processed moving objects
        public static int objectNB = (data == "Geolife") ? 2000 : 10000;
        // the number of partitions of a time-interval
        public static int intervalNum = 5;
        // the default sampling number for approxiamte refinement
        public static int sampleNum = 20;
        // the node size in the balltree
        public static int minLeafNB = 20;
        // public static String geolifePath = "D:/VLDB/Geolife/Data/";
        public static String geolifePath = "/home/like/data/Geolife/Data/";
        public static String portoPath = "/home/like/data/porto.csv";
        // the default and varying threshold of intersection ratios
        public static double interRatio = 0.3;
        public static double[] interRatios = { 0.1, 0.2, 0.3, 0.4, 0.5 };
        // the default and varying maximum speed of each moving objects
        public static double maxSpeed = 2.5;
        public static double[] maxSpeeds = { 1.5, 2, 2.5, 3, 3.5 };
        // the default and varying number of moving objects
        public static int cardinality = (data == "Geolife") ? 100000 : 20000;
        public static int[] cardinalities = (data == "Geolife") ? new int[] {100000,200000,300000,400000,500000 }
                        : new int[] { 20000, 40000, 60000, 80000, 100000 };
        // the default and varying re-partition threshold
        public static double repartitionRatio = 0.3;
        public static double[] repartitionRatios = { 0.1, 0.2, 0.3, 0.4, 0.5 };
}