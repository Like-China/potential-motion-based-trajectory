package evaluation;

import java.util.*;

import poi.QuadTree;
import utils.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Loader {
    // all loaded trajs
    public ArrayList<Trajectory> trjs = new ArrayList<>();
    // query (A), database set (B)
    public ArrayList<Trajectory> A = new ArrayList<>();
    public ArrayList<Trajectory> B = new ArrayList<>();
    // POIs
    HashSet<Point> points = new HashSet<>();
    // lon, lat range
    public double minLon = 180;
    public double minLat = 180;
    public double maxLon = -180;
    public double maxLat = -180;
    // x, y range
    public double minX = Double.MAX_VALUE;
    public double minY = Double.MAX_VALUE;
    public double maxX = -Double.MAX_VALUE;
    public double maxY = -Double.MAX_VALUE;
    // POI index
    public QuadTree qStatic = new QuadTree();

    public Loader(int dataNB, int POINB) {
        long t1 = System.currentTimeMillis();
        getTrajectoryData(dataNB);
        getQueryDB(dataNB);
        for (Trajectory trj : trjs) {
            for (Location l : trj.locationSeq) {
                minLon = minLon > l.longititude ? l.longititude : minLon;
                minLat = minLat > l.latitude ? l.latitude : minLat;
                maxLon = maxLon < l.longititude ? l.longititude : maxLon;
                maxLat = maxLat < l.latitude ? l.latitude : maxLat;

                minX = minX > l.x ? l.x : minX;
                minY = minY > l.y ? l.y : minY;
                maxX = maxX < l.x ? l.x : maxX;
                maxY = maxY < l.y ? l.y : maxY;
            }
        }
        System.out.println("Lon range: " + minLon + " -> " + maxLon);
        System.out.println("Lat range: " + minLat + " -> " + maxLat);
        System.out.println("X   range: " + minX + " -> " + maxX + ": " + (maxX - minX));
        System.out.println("Y   range: " + minY + " -> " + maxY + ": " + (maxY - minY));

        // generate a set of random POI locations within (minX, maxX, minY, maxY)
        points = generateRandomPoints(minX, minY, maxX, maxY, POINB);
        // construct quadtree for quickly retrieve POIs within a given motion range
        // QuadTree<Double> qDynamic = new QuadTree<>();
        // qDynamic.DYNAMIC_MAX_OBJECTS = true;
        // qDynamic.MAX_OBJ_TARGET_EXPONENT = 0.5;
        qStatic.LEAF_MAX_OBJECTS = 100;
        for (Point p : points) {
            qStatic.place(p.x, p.y);
        }

        long t2 = System.currentTimeMillis();
        System.out.println("Generated POIs: " + points.size());
        System.out.println("Load data time cost (ms): " + (t2 - t1));
        System.out.println();

    }

    public HashSet<Point> generateRandomPoints(double minX, double minY, double maxX, double maxY, int n) {
        Random random = new Random(0);
        HashSet<Point> points = new HashSet<>();
        while (points.size() < n) {
            double x = minX + (maxX - minX) * random.nextDouble();
            double y = minY + (maxY - minY) * random.nextDouble();
            points.add(new Point(x, y));
        }

        return points;
    }

    /**
     * get all trajectory files in the given dierctory, for geo
     * *
     * 
     * @param fileInput   the directory that stores trajectory files
     * @param allFileList store all trajectory files of the input fileInput in a
     *                    list
     */
    public void getAllFile(File fileInput, List<File> allFileList) {
        File[] fileList = fileInput.listFiles();
        assert fileList != null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                getAllFile(file, allFileList);
            } else {
                if (!Character.isLetter(file.getName().charAt(0))) {
                    allFileList.add(file);
                }
            }
        }
    }

    /**
     * store all data in the form of TimeIntervalMR (Data) in ArrayList<Data>
     * allData
     * 
     * @param readObjNum the maximum number of loaded trajectories/moving objects
     * @param maxSpeed   the maximum speed of a moving object to its averaged speed
     */
    public void getTrajectoryData(int readObjNum) {
        if (Settings.data == "Porto") {
            getPortoTrajectory(readObjNum);
        } else if (Settings.data == "Geolife") {
            getGeolifeTrajectory(readObjNum);
        } else {
            System.out.println("No such dataset!!");
            return;
        }
        System.out.printf("Dataset name: %s Trajectory Size: %d \n", Settings.data, trjs.size());
    }

    public void getGeolifeTrajectory(int readObjNum) {
        File dir = new File(Settings.geolifePath);
        List<File> allFileList = new ArrayList<>();
        if (!dir.exists()) {
            return;
        }
        getAllFile(dir, allFileList);
        Collections.sort(allFileList);
        if (Settings.isShuffle) {
            Collections.shuffle(allFileList);
        }
        // obtain all locations
        BufferedReader reader;
        int id = 0;
        int lastTS = 0;
        for (File f : allFileList) {
            try {
                reader = new BufferedReader(new FileReader(f));
                String lineString = null;
                // omit the first 6 lines
                for (int i = 0; i < 6; i++) {
                    lineString = reader.readLine();
                }
                // load one trajectory per line
                ArrayList<Location> traj = new ArrayList<>();
                while ((lineString = reader.readLine()) != null) {
                    String[] line = lineString.split(",");
                    // omit short trajectories with length less than 5
                    if (line.length > 5) {
                        double real_lat = Double.parseDouble(line[0]);
                        double real_lon = Double.parseDouble(line[1]);
                        String[] hms = line[line.length - 1].split(":");
                        int ts = Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60
                                + Integer.parseInt(hms[2]);
                        if (ts != lastTS) {
                            traj.add(new Location(id, real_lon, real_lat, ts));
                        }
                        lastTS = ts;
                        if (traj.size() >= Settings.tsNB) {
                            break;
                        }
                    }
                }
                Trajectory newTrj = new Trajectory(id, traj);
                if (!newTrj.isDelete()) {
                    trjs.add(newTrj);
                    id++;
                }
                if (id >= readObjNum)
                    break;
                reader.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

    public void getPortoTrajectory(int readObjNum) {
        BufferedReader reader;
        int id = 0;
        try {
            reader = new BufferedReader(new FileReader(Settings.portoPath));
            String lineString = reader.readLine();
            while ((lineString = reader.readLine()) != null) {
                String[] line = lineString.split("\\[\\[");
                if (line.length < 2)
                    continue;
                if (id >= readObjNum)
                    break;
                line = line[1].split("],");
                // load one trajectory per line
                ArrayList<Location> traj = new ArrayList<>();
                int ts = 0;
                for (String l : line) {
                    l = l.replace("[", "");
                    l = l.replace("]", "");
                    l = l.replace("\"", "");
                    String[] lonlat = l.split(",");
                    double real_lon = Double.parseDouble(lonlat[0]);
                    double real_lat = Double.parseDouble(lonlat[1]);
                    traj.add(new Location(id, real_lon, real_lat, ts));
                    if (traj.size() >= Settings.tsNB) {
                        break;
                    }
                    ts += 15;
                }
                Trajectory newTrj = new Trajectory(id, traj);

                if (!newTrj.isDelete()) {
                    trjs.add(newTrj);
                    id += 1;
                }
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * first run getAllData() to fill ArrayList<Data> allData, then run getBatch()
     * to get a batch of data
     * 
     * @param dataNB the query size, the remaining is stored in the database
     */
    public void getQueryDB(int dataNB) {
        A = new ArrayList<>();
        B = new ArrayList<>();
        int size = trjs.size();
        assert size >= dataNB : "Lack of data!";
        for (int i = 0; i < dataNB; i++) {
            Trajectory trj = trjs.get(i);
            A.add(trj);
            B.add(trj);
        }
    }

}
