package balltree;

import java.util.*;

public class TernaryBallNode extends BallNode {
    public TernaryBallNode leftNode = null;
    public TernaryBallNode rightNode = null;
    public TernaryBallNode extraNode = null;

    public TernaryBallNode(int id, int idxStart, int idxEnd, double[] pivot, double radius) {
        super(id, idxStart, idxEnd, pivot, radius);
    }

    public HashMap<Integer, double[]> preOrder(TernaryBallNode root, HashMap<Integer, double[]> searchMap) {
        if (root != null) {
            searchMap.put(root.id, root.pivot);
            if (root.leftNode != null) {
                preOrder(root.leftNode, searchMap);
            }
            if (root.extraNode != null) {
                preOrder(root.extraNode, searchMap);
            }
            if (root.rightNode != null) {
                preOrder(root.rightNode, searchMap);
            }
        }
        return searchMap;
    }

    public List<TernaryBallNode> levelOrder(TernaryBallNode root) {
        if (root == null) {
            return null;
        }
        List<TernaryBallNode> allNodes = new ArrayList<>();

        Queue<TernaryBallNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TernaryBallNode head = queue.poll();
            if (head.leftNode != null) {
                queue.offer(head.leftNode);
            }
            if (head.extraNode != null) {
                queue.offer(head.extraNode);
            }
            if (head.rightNode != null) {
                queue.offer(head.rightNode);
            }
            allNodes.add(head);
        }
        // print balltree internal information
        double radiusSum = 0;
        for (TernaryBallNode node : allNodes) {
            radiusSum += node.radius;
        }
        System.out.println(allNodes.size() + " / " + radiusSum);
        return allNodes;
    }

}
