/**
 * 
 */

package balltree;

import java.util.*;

public class BallNode {
    public int id;
    public int idxStart;
    public int idxEnd;
    public double[] pivot;
    public double radius;
    public BallNode leftNode = null;
    public BallNode rightNode = null;

    /**
     * 
     * @param id
     * @param idxStart
     * @param idxEnd
     * @param pivot
     * @param
     */
    public BallNode(int id, int idxStart, int idxEnd, double[] pivot, double radius) {
        this.id = id;
        this.idxStart = idxStart;
        this.idxEnd = idxEnd;
        this.pivot = pivot;
        this.radius = radius;
    }

    public HashMap<Integer, double[]> preOrder(BallNode root, HashMap<Integer, double[]> searchMap) {
        if (root != null) {
            searchMap.put(root.id, root.pivot);
            if (root.leftNode != null) {
                preOrder(root.leftNode, searchMap);
            }
            if (root.rightNode != null) {
                preOrder(root.rightNode, searchMap);
            }
        }
        return searchMap;
    }

    public ArrayList<BallNode> levelOrder(BallNode root) {
        if (root == null) {
            return null;
        }
        ArrayList<BallNode> allNodes = new ArrayList<>();
        Queue<BallNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            BallNode head = queue.poll();
            if (head.leftNode != null) {
                queue.offer(head.leftNode);
            }

            if (head.rightNode != null) {
                queue.offer(head.rightNode);
            }
            allNodes.add(head);
        }
        // print balltree internal information
        double radiusSum = 0;
        for (BallNode node : allNodes) {
            radiusSum += node.radius;
        }
        System.out.println(
                allNodes.size() + " / " +
                        root.getDepth(root.leftNode) + "/"
                        + root.getDepth(root.rightNode) + " / " + radiusSum);
        return allNodes;
    }

    public int getDepth(BallNode root) {
        if (root == null)
            return 0;
        int left = getDepth(root.leftNode);
        int right = getDepth(root.rightNode);
        return (left > right ? left : right) + 1;
    }

}
