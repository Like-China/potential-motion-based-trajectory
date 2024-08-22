package evaluation;

public class Settings {
        // public static String data = "Geolife";
        public static String data = "Porto";
        // the default threshold of similarity
        public static double simThreshold = 0.1;
        // shuffle data
        public static boolean isShuffle = false;
        // kNN k
        public static int k = 5;
        // use order strategy in ball-tree nn join
        public static boolean useOrder = false;
        // the number of processed moving objects
        public static int dataNB = 200;
        // the number of partitions of a time-interval
        public static int intervalNum = 10;
        // the default sampling POI number in a time-point motion range
        public static int sampleNum = 5;
        public static int tsNB = 10;
        // the node size in the balltree
        public static int minLeafNB = 10;

        public static String geolifePath = "/home/like/data/trajectory/Geolife/Data/";
        public static String portoPath = "/home/like/data/trajectory/porto.csv";
        public static double[] interRatios = { 0.1, 0.2, 0.3, 0.4, 0.5 };
        // the default and varying maximum speed of each moving objects
        // the default and varying number of moving objects
        public static int cardinality = (data == "Geolife") ? 100000 : 20000;
        public static int[] cardinalities = (data == "Geolife") ? new int[] { 100000, 200000, 300000, 400000, 500000 }
                        : new int[] { 20000, 40000, 60000, 80000, 100000 };
        // the default and varying re-partition threshold
        public static double repartitionRatio = 0.3;
        public static double[] repartitionRatios = { 0.1, 0.2, 0.3, 0.4, 0.5 };
}