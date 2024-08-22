package quadtree.point_quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tester {

    public static void test() {

        // generate test data
        int queryNB = 500;
        int dbNB = 100000;
        int[][] inputPoint = new int[dbNB][2];
        int[][] queryRectangle = new int[dbNB][4];
        Random r = new Random();
        for (int i = 0; i < dbNB; i++) {
            inputPoint[i] = new int[] { r.nextInt(1000), r.nextInt(1000) };
        }
        for (int i = 0; i < queryNB; i++) {
            int x1 = r.nextInt(500);
            int y1 = r.nextInt(500);
            queryRectangle[i] = new int[] { x1, y1, x1 + r.nextInt(500), y1 + r.nextInt(500) };
        }
        List<QuadTree<Integer>.CoordHolder> res = new ArrayList<>();
        long t1, t2;
        /*
         * Create a tree with a dynamic leaf size that reflects the square root of the
         * size of the growing tree.
         */
        t1 = System.currentTimeMillis();
        QuadTree<Integer> qDynamic = new QuadTree<>();
        qDynamic.DYNAMIC_MAX_OBJECTS = true;
        qDynamic.MAX_OBJ_TARGET_EXPONENT = 0.5;
        for (int i = 0; i < dbNB; i++) {
            qDynamic.place(inputPoint[i][0], inputPoint[i][1], i);
        }
        for (int i = 0; i < queryNB; i++) {
            res = qDynamic.findAll(queryRectangle[i][0], queryRectangle[i][1], queryRectangle[i][2],
                    queryRectangle[i][3]);
        }
        t2 = System.currentTimeMillis();
        System.out.println("Dynamic Index Time consume:" + (t2 - t1));

        /*
         * Create a tree with static leaf size.
         */
        t1 = System.currentTimeMillis();
        QuadTree<Integer> qStatic = new QuadTree<>();
        qStatic.LEAF_MAX_OBJECTS = 100;
        for (int i = 0; i < dbNB; i++) {
            qStatic.place(inputPoint[i][0], inputPoint[i][1], i);
        }
        for (int i = 0; i < queryNB; i++) {
            res = qStatic.findAll(queryRectangle[i][0], queryRectangle[i][1], queryRectangle[i][2],
                    queryRectangle[i][3]);
        }
        t2 = System.currentTimeMillis();
        System.out.println("Static Indexed Time consume:" + (t2 - t1));

        // bruteful
        int count = 0;
        for (int i = 0; i < queryNB; i++) {
            int[] query = queryRectangle[i];
            count = 0;
            for (int j = 0; j < dbNB; j++) {
                int[] db = inputPoint[j];
                if (db[0] >= query[0] && db[0] <= query[2] && db[1] >= query[1] && db[1] <= query[3]) {
                    count++;
                }
            }
        }
        System.out.println(res.size() + "/" + count);
        t2 = System.currentTimeMillis();
        System.out.println("Bruteful Time consume:" + (t2 - t1));
    }

    public static void main(String[] args) {
        test();
    }
}
