package evaluation;

import java.util.ArrayList;
import java.util.Collections;

import utils.Data;
import utils.Ellipse;
import utils.Location;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Loader {
    // store all data (location, nextlocation) in a ArryLiat<Data> (real values)
    ArrayList<Data> allData = new ArrayList<>();
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Data> queries = new ArrayList<>();
    public ArrayList<Data> db = new ArrayList<>();

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
     * store all data in the form of Ellipse (Data) in ArrayList<Data> allData
     * 
     * @param readObjNum the maximum number of loaded trajectories/moving objects
     * @param maxSpeed   the maximum speed of a moving object to its averaged speed
     */
    public void getAllData(int readObjNum, double maxSpeed) {
        if (Settings.data == "Porto") {
            getPortoData(readObjNum, maxSpeed);
            return;
        }
        allData = new ArrayList<>();
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
        List<Location> locations = new ArrayList<>();
        int id = 0;
        for (File f : allFileList) {
            try {
                reader = new BufferedReader(new FileReader(f));
                String lineString = null;
                for (int i = 0; i < 6; i++) {
                    lineString = reader.readLine();
                }
                while ((lineString = reader.readLine()) != null) {
                    String[] line = lineString.split(",");
                    if (line.length > 5) {
                        double real_lat = Double.parseDouble(line[0]);
                        double real_lon = Double.parseDouble(line[1]);
                        String[] hms = line[line.length - 1].split(":");
                        int ts = Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60
                                + Integer.parseInt(hms[2]);
                        locations.add(new Location(id, real_lon, real_lat, ts));
                    }
                }
                reader.close();
                id++;
                if (id >= readObjNum)
                    break;
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        // use locations to form ellpises
        for (int i = 0; i < locations.size() - 1; i++) {
            Location cur = locations.get(i);
            Location next = locations.get(i + 1);
            if (cur.objectID == next.objectID && next.timestamp - cur.timestamp > 0) {
                Data data = new Data(new Ellipse(cur, next, maxSpeed));
                // remove static or abnormal objects
                Ellipse bead = data.bead;
                // a == 0 the timestampe does not change
                if (bead.meanSpeed <= 0 || bead.meanSpeed >= 5 || bead.a == 0) {
                    continue;
                }
                allData.add(data);
            }
        }
        System.out.printf("objects: %d, locations: %d, ellipses: %d\n", id, locations.size(), allData.size());
    }

    /**
     * store all data in the form of Ellipse (Data) in ArrayList<Data> allData for
     * Porto dataset
     * 
     * @param readObjNum
     * @param maxSpeed
     */
    public void getPortoData(int readObjNum, double maxSpeed) {
        allData = new ArrayList<>();
        // obtain all locations
        BufferedReader reader;
        List<Location> locations = new ArrayList<>();
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
                int ts = 0;
                for (String l : line) {
                    l = l.replace("[", "");
                    l = l.replace("]", "");
                    l = l.replace("\"", "");
                    String[] lonlat = l.split(",");
                    double real_lon = Double.parseDouble(lonlat[0]);
                    double real_lat = Double.parseDouble(lonlat[1]);
                    locations.add(new Location(id, real_lon, real_lat, ts));
                    ts += 15;
                }
                id++;
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        // use locations to form ellpises
        for (int i = 0; i < locations.size() - 1; i++) {
            Location cur = locations.get(i);
            Location next = locations.get(i + 1);
            if (cur.objectID == next.objectID && next.timestamp - cur.timestamp > 0) {
                Data data = new Data(new Ellipse(cur, next, maxSpeed));
                // remove static or abnormal objects
                Ellipse bead = data.bead;
                // a == 0 the timestampe does not change
                if (bead.meanSpeed <= 0 || bead.meanSpeed >= 5 || bead.a == 0) {
                    continue;
                }
                allData.add(data);
            }
        }
        System.out.printf("objects: %d, locations: %d, ellipses: %d\n", id, locations.size(), allData.size());
    }

    //
    //

    /**
     * first run getAllData() to fill ArrayList<Data> allData, then run getBatch()
     * to get a batch of data
     * 
     * @param cardinality the query size, the remaining is stored in the database
     */
    public void getBatch(int cardinality) {
        queries = new ArrayList<>();
        db = new ArrayList<>();
        int size = allData.size();
        assert size >= 2 * cardinality : "Lack of data!";
        // if not shuffle, sequencely read, else shuffle data
        if (Settings.isShuffle)
            Collections.shuffle(allData);
        for (int i = 0; i < 2 * cardinality; i++) {
            Data data = allData.get(i);
            if (i < cardinality) {
                queries.add(data);
            } else {
                db.add(data);
            }
        }
    }

}
