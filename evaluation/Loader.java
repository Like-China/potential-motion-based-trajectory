package evaluation;

import java.util.*;
import utils.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Loader {
    // store all trajs
    public ArrayList<Trajectory> trjs = new ArrayList<>();
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Trajectory> queries = new ArrayList<>();
    public ArrayList<Trajectory> db = new ArrayList<>();

    /**
     * get all trajectory files in the given dierctory
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
            return;
        }
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
        for (File f : allFileList) {
            try {
                reader = new BufferedReader(new FileReader(f));
                String lineString = null;
                for (int i = 0; i < 6; i++) {
                    lineString = reader.readLine();
                }
                // load one trajectory per line
                ArrayList<Location> traj = new ArrayList<>();
                while ((lineString = reader.readLine()) != null) {
                    String[] line = lineString.split(",");
                    if (line.length > 5) {
                        double real_lat = Double.parseDouble(line[0]);
                        double real_lon = Double.parseDouble(line[1]);
                        String[] hms = line[line.length - 1].split(":");
                        int ts = Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60
                                + Integer.parseInt(hms[2]);
                        traj.add(new Location(id, real_lon, real_lat, ts));
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
        System.out.printf("Trajectory Size: %d \n", id, trjs.size());
    }

    /**
     * store all data in the form of TimeIntervalMR (Data) in ArrayList<Data>
     * allData for
     * Porto dataset
     * 
     * @param readObjNum
     * @param maxSpeed
     */
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
                    id++;
                }
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.printf("Trajectory Size: %d \n", trjs.size());
    }

    /**
     * first run getAllData() to fill ArrayList<Data> allData, then run getBatch()
     * to get a batch of data
     * 
     * @param querySize the query size, the remaining is stored in the database
     */
    public void getQueryDB(int querySize) {
        queries = new ArrayList<>();
        db = new ArrayList<>();
        int size = trjs.size();
        assert size >= 2 * querySize : "Lack of data!";
        // if not shuffle, sequencely read, else shuffle data
        if (Settings.isShuffle)
            Collections.shuffle(trjs);
        for (int i = 0; i < 2 * querySize; i++) {
            Trajectory trj = trjs.get(i);
            if (i < querySize) {
                queries.add(trj);
            } else {
                db.add(trj);
            }
        }
    }

    // public static void main(String[] args) {
    // Loader l = new Loader();
    // l.getPortoTrajectory(100);
    // for (Trajectory t : l.trjs) {
    // System.out.println(t);
    // }
    // }

}
