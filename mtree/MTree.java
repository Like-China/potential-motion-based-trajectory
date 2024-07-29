package mtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import mtree.SplitFunction.SplitResult;
import utils.ContactPair;
import utils.Data;

/**
 * The main class that implements the M-Tree.
 *
 * @param <Data> The type of Data that will be indexed by the M-Tree. Objects of
 *               this type are stored in HashMaps and HashSets, so their
 *               {@code hashCode()} and {@code equals()} methods must be
 *               consistent.
 */
public class MTree {
	// Exception classes
	private static class SplitNodeReplacement extends Exception {
		// A subclass of Throwable cannot be generic. :-(
		// So, we have newNodes declared as Object[] instead of Node[].
		private Object newNodes[];

		private SplitNodeReplacement(Object... newNodes) {
			this.newNodes = newNodes;
		}
	}

	private static class RootNodeReplacement extends Exception {
		// A subclass of Throwable cannot be generic. :-(
		// So, we have newRoot declared as Object instead of Node.
		private Object newRoot;

		private RootNodeReplacement(Object newRoot) {
			this.newRoot = newRoot;
		}
	}

	private static class NodeUnderCapacity extends Exception {
	}

	private static class DataNotFound extends Exception {
	}

	/**
	 * An {@link Iterable} class which can be iterated to fetch the results of a
	 * nearest-neighbors query.
	 * 
	 * <p>
	 * The neighbors are presented in non-decreasing order from the {@code
	 * queryData} argument to the {@link MTree#getNearest(Object, double, int)
	 * getNearest*()}
	 * call.
	 * 
	 * <p>
	 * The query on the M-Tree is executed during the iteration, as the
	 * results are fetched. It means that, by the time when the <i>n</i>-th
	 * result is fetched, the next result may still not be known, and the
	 * resources allocated were only the necessary to identify the <i>n</i>
	 * first results.
	 */
	public class Query {
		private Data Data;
		private double range;

		private class ItemWithDistances<U> implements Comparable<ItemWithDistances<U>> {
			private U item;
			private double distance;
			private double minDistance;

			public ItemWithDistances(U item, double distance, double minDistance) {
				this.item = item;
				this.distance = distance;
				this.minDistance = minDistance;
			}

			@Override
			public int compareTo(ItemWithDistances<U> that) {
				if (this.minDistance < that.minDistance) {
					return -1;
				} else if (this.minDistance > that.minDistance) {
					return +1;
				} else {
					return 0;
				}
			}
		}

		private PriorityQueue<ItemWithDistances<Node>> pendingQueue = new PriorityQueue<ItemWithDistances<Node>>();
		private ArrayList<ContactPair> rangeRes = new ArrayList<>();

		public ArrayList<ContactPair> rangeQuery() {
			if (MTree.this.root == null) {
				return rangeRes;
			}
			double distance = MTree.this.distanceFunction.calculate(Query.this.Data, MTree.this.root.Data);
			double minDistance = Math.max(distance - MTree.this.root.radius, 0.0);

			pendingQueue.add(new ItemWithDistances<Node>(MTree.this.root, distance, minDistance));

			while (!pendingQueue.isEmpty()) {
				ItemWithDistances<Node> pending = pendingQueue.poll();
				Node node = pending.item;
				for (IndexItem child : node.children.values()) {
					if (Math.abs(pending.distance - child.distanceToParent) <= Query.this.range + child.radius
							+ pending.distance) {
						double childDistance = MTree.this.distanceFunction.calculate(Query.this.Data, child.Data);
						double childMinDistance = Math.max(childDistance, 0.0);
						if (childMinDistance <= Query.this.range + child.radius) {
							if (child instanceof MTree.Entry) {
								Entry entry = (Entry) child;
								rangeRes.add(new ContactPair(Query.this.Data, entry.Data, childDistance));
							} else {
								Node childNode = (Node) child;
								pendingQueue
										.add(new ItemWithDistances<Node>(childNode, childDistance, childMinDistance));
							}
						}
					}
				}

			}
			return rangeRes;
		}

		private Query(Data Data, double range, int limit) {
			this.Data = Data;
			this.range = range;
			pendingQueue = new PriorityQueue<ItemWithDistances<Node>>();
			rangeRes = new ArrayList<>();
		}

	}

	/**
	 * The default minimum capacity of nodes in an M-Tree, when not specified in
	 * the constructor call.
	 */
	public static final int DEFAULT_MIN_NODE_CAPACITY = 50;

	protected int minNodeCapacity;
	protected int maxNodeCapacity;
	public DistanceFunction<? super Data> distanceFunction;
	protected SplitFunction<Data> splitFunction;
	public Node root;

	/**
	 * Constructs an M-Tree with the specified distance function.
	 * 
	 * @param distanceFunction The object used to calculate the distance between
	 *                         two Data objects.
	 */
	public MTree(DistanceFunction<? super Data> distanceFunction,
			SplitFunction<Data> splitFunction) {
		this(DEFAULT_MIN_NODE_CAPACITY, distanceFunction, splitFunction);
	}

	/**
	 * Constructs an M-Tree with the specified minimum node capacity and
	 * distance function.
	 * 
	 * @param minNodeCapacity  The minimum capacity for the nodes of the tree.
	 * @param distanceFunction The object used to calculate the distance between
	 *                         two Data objects.
	 * @param splitFunction    The object used to process the split of nodes if
	 *                         they are full when a new child must be added.
	 */
	public MTree(int minNodeCapacity,
			DistanceFunction<? super Data> distanceFunction,
			SplitFunction<Data> splitFunction) {
		this(minNodeCapacity, 2 * minNodeCapacity - 1, distanceFunction, splitFunction);
	}

	/**
	 * Constructs an M-Tree with the specified minimum and maximum node
	 * capacities and distance function.
	 * 
	 * @param minNodeCapacity  The minimum capacity for the nodes of the tree.
	 * @param maxNodeCapacity  The maximum capacity for the nodes of the tree.
	 * @param distanceFunction The object used to calculate the distance between
	 *                         two Data objects.
	 * @param splitFunction    The object used to process the split of nodes if
	 *                         they are full when a new child must be added.
	 */
	public MTree(int minNodeCapacity, int maxNodeCapacity,
			DistanceFunction<? super Data> distanceFunction,
			SplitFunction<Data> splitFunction) {
		if (minNodeCapacity < 2 || maxNodeCapacity <= minNodeCapacity ||
				distanceFunction == null) {
			throw new IllegalArgumentException();
		}

		if (splitFunction == null) {
			splitFunction = new ComposedSplitFunction<Data>(
					new PromotionFunctions.RandomPromotion<Data>(),
					new PartitionFunctions.BalancedPartition<Data>());
		}

		this.minNodeCapacity = minNodeCapacity;
		this.maxNodeCapacity = maxNodeCapacity;
		this.distanceFunction = distanceFunction;
		this.splitFunction = splitFunction;
		this.root = null;
	}

	/**
	 * Adds and indexes a Data object.
	 * 
	 * <p>
	 * An object that is already indexed should not be added. There is no
	 * validation regarding this, and the behavior is undefined if done.
	 * 
	 * @param Data The Data object to index.
	 */
	public void add(Data Data) {
		if (root == null) {
			root = new RootLeafNode(Data);
			try {
				root.addData(Data, 0);
			} catch (SplitNodeReplacement e) {
				throw new RuntimeException("Should never happen!");
			}
		} else {
			double distance = distanceFunction.calculate(Data, root.Data);
			try {
				root.addData(Data, distance);
			} catch (SplitNodeReplacement e) {
				Node newRoot = new RootNode(Data);
				root = newRoot;
				for (int i = 0; i < e.newNodes.length; i++) {
					@SuppressWarnings("unchecked")
					Node newNode = (Node) e.newNodes[i];
					distance = distanceFunction.calculate(root.Data, newNode.Data);
					root.addChild(newNode, distance);
				}
			}
		}
	}

	/**
	 * Removes a Data object from the M-Tree.
	 * 
	 * @param Data The Data object to be removed.
	 * @return {@code true} if and only if the object was found.
	 */
	public boolean remove(Data Data) {
		if (root == null) {
			return false;
		}

		double distanceToRoot = distanceFunction.calculate(Data, root.Data);
		try {
			root.removeData(Data, distanceToRoot);
		} catch (RootNodeReplacement e) {
			@SuppressWarnings("unchecked")
			Node newRoot = (Node) e.newRoot;
			root = newRoot;
		} catch (DataNotFound e) {
			return false;
		} catch (NodeUnderCapacity e) {
			throw new RuntimeException("Should have never happened", e);
		}
		return true;
	}

	/**
	 * Performs a nearest-neighbors query on the M-Tree, constrained by distance.
	 * 
	 * @param queryData The query Data object.
	 * @param range     The maximum distance from {@code queryData} to fetched
	 *                  neighbors.
	 * @return A {@link Query} object used to iterate on the results.
	 */
	public Query getNearestByRange(Data queryData, double range) {
		return getNearest(queryData, range, Integer.MAX_VALUE);
	}

	/**
	 * Performs a nearest-neighbor query on the M-Tree, constrained by distance
	 * and/or the number of neighbors.
	 * 
	 * @param queryData The query Data object.
	 * @param range     The maximum distance from {@code queryData} to fetched
	 *                  neighbors.
	 * @param limit     The maximum number of neighbors to fetch.
	 * @return A {@link Query} object used to iterate on the results.
	 */
	public Query getNearest(Data queryData, double range, int limit) {
		return new Query(queryData, range, limit);
	}

	/**
	 * Performs a nearest-neighbor query on the M-Tree, without constraints.
	 * 
	 * @param queryData The query Data object.
	 * @return A {@link Query} object used to iterate on the results.
	 */
	public Query getNearest(Data queryData) {
		return new Query(queryData, Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
	}

	protected void _check() {
		if (root != null) {
			root._check();
		}
	}

	private class IndexItem {
		Data Data;
		protected double radius;
		double distanceToParent;

		private IndexItem(Data Data) {
			this.Data = Data;
			this.radius = Data.radius;
			this.distanceToParent = -1;
		}

		int _check() {
			_checkRadius();
			_checkDistanceToParent();
			return 1;
		}

		private void _checkRadius() {
			assert radius >= 0;
		}

		protected void _checkDistanceToParent() {
			assert !(this instanceof MTree.RootLeafNode);
			assert !(this instanceof MTree.RootNode);
			assert distanceToParent >= 0;
		}
	}

	public abstract class Node extends IndexItem {

		protected Map<Data, IndexItem> children = new HashMap<Data, IndexItem>();
		protected Rootness rootness;
		protected Leafness<Data> leafness;

		private <R extends NodeTrait & Rootness, L extends NodeTrait & Leafness<Data>> Node(Data Data, R rootness,
				L leafness) {
			super(Data);

			rootness.thisNode = this;
			this.rootness = rootness;

			leafness.thisNode = this;
			this.leafness = leafness;
		}

		private final void addData(Data Data, double distance) throws SplitNodeReplacement {
			doAddData(Data, distance);
			checkMaxCapacity();
		}

		int _check() {
			super._check();
			_checkMinCapacity();
			_checkMaxCapacity();

			int childHeight = -1;
			for (Map.Entry<Data, IndexItem> e : children.entrySet()) {
				Data Data = e.getKey();
				IndexItem child = e.getValue();
				assert child.Data.equals(Data);

				_checkChildClass(child);
				_checkChildMetrics(child);

				int height = child._check();
				if (childHeight < 0) {
					childHeight = height;
				} else {
					assert childHeight == height;
				}
			}
			return childHeight + 1;
		}

		protected void doAddData(Data Data, double distance) {
			leafness.doAddData(Data, distance);
		}

		protected void doRemoveData(Data Data, double distance) throws DataNotFound {
			leafness.doRemoveData(Data, distance);
		}

		private final void checkMaxCapacity() throws SplitNodeReplacement {
			if (children.size() > MTree.this.maxNodeCapacity) {
				DistanceFunction<? super Data> cachedDistanceFunction = DistanceFunctions
						.cached(MTree.this.distanceFunction);
				SplitResult<Data> splitResult = MTree.this.splitFunction.process(children.keySet(),
						cachedDistanceFunction);

				Node newNode0 = null;
				Node newNode1 = null;
				for (int i = 0; i < 2; ++i) {
					Data promotedData = splitResult.promoted.get(i);
					Set<Data> partition = splitResult.partitions.get(i);

					Node newNode = newSplitNodeReplacement(promotedData);
					for (Data Data : partition) {
						IndexItem child = children.get(Data);
						children.remove(Data);
						double distance = cachedDistanceFunction.calculate(promotedData, Data);
						newNode.addChild(child, distance);
					}

					if (i == 0) {
						newNode0 = newNode;
					} else {
						newNode1 = newNode;
					}
				}
				assert children.isEmpty();

				throw new SplitNodeReplacement(newNode0, newNode1);
			}

		}

		protected Node newSplitNodeReplacement(Data Data) {
			return leafness.newSplitNodeReplacement(Data);
		}

		protected void addChild(IndexItem child, double distance) {
			leafness.addChild(child, distance);
		}

		void removeData(Data Data, double distance) throws RootNodeReplacement, NodeUnderCapacity, DataNotFound {
			doRemoveData(Data, distance);
			if (children.size() < getMinCapacity()) {
				throw new NodeUnderCapacity();
			}
		}

		protected int getMinCapacity() {
			return rootness.getMinCapacity();
		}

		private void updateMetrics(IndexItem child, double distance) {
			child.distanceToParent = distance;
			updateRadius(child);
		}

		private void updateRadius(IndexItem child) {
			this.radius = Math.max(this.radius, child.distanceToParent + child.radius);
		}

		void _checkMinCapacity() {
			rootness._checkMinCapacity();
		}

		private void _checkMaxCapacity() {
			assert children.size() <= MTree.this.maxNodeCapacity;
		}

		private void _checkChildClass(IndexItem child) {
			leafness._checkChildClass(child);
		}

		private void _checkChildMetrics(IndexItem child) {
			double dist = MTree.this.distanceFunction.calculate(child.Data, this.Data);
			assert child.distanceToParent == dist;
			double sum = child.distanceToParent + child.radius;
			assert sum <= this.radius;
		}

		protected void _checkDistanceToParent() {
			rootness._checkDistanceToParent();
		}

		private MTree mtree() {
			return MTree.this;
		}
	}

	private abstract class NodeTrait {
		protected Node thisNode;
	}

	private interface Leafness<Data> {
		void doAddData(Data Data, double distance);

		void addChild(MTree.IndexItem child, double distance);

		void doRemoveData(Data Data, double distance) throws DataNotFound;

		MTree.Node newSplitNodeReplacement(Data Data);

		void _checkChildClass(MTree.IndexItem child);
	}

	private interface Rootness {
		int getMinCapacity();

		void _checkDistanceToParent();

		void _checkMinCapacity();
	}

	private class RootNodeTrait extends NodeTrait implements Rootness {

		@Override
		public int getMinCapacity() {
			throw new RuntimeException("Should not be called!");
		}

		@Override
		public void _checkDistanceToParent() {
			assert thisNode.distanceToParent == -1;
		}

		@Override
		public void _checkMinCapacity() {
			thisNode._checkMinCapacity();
		}

	};

	private class NonRootNodeTrait extends NodeTrait implements Rootness {

		@Override
		public int getMinCapacity() {
			return MTree.this.minNodeCapacity;
		}

		@Override
		public void _checkMinCapacity() {
			assert thisNode.children.size() >= thisNode.mtree().minNodeCapacity;
		}

		@Override
		public void _checkDistanceToParent() {
			assert thisNode.distanceToParent >= 0;
		}
	};

	private class LeafNodeTrait extends NodeTrait implements Leafness<Data> {

		public void doAddData(Data Data, double distance) {
			Entry entry = thisNode.mtree().new Entry(Data);
			assert !thisNode.children.containsKey(Data);
			thisNode.children.put(Data, entry);
			assert thisNode.children.containsKey(Data);
			thisNode.updateMetrics(entry, distance);
		}

		public void addChild(IndexItem child, double distance) {
			assert !thisNode.children.containsKey(child.Data);
			thisNode.children.put(child.Data, child);
			assert thisNode.children.containsKey(child.Data);
			thisNode.updateMetrics(child, distance);
		}

		public Node newSplitNodeReplacement(Data Data) {
			return thisNode.mtree().new LeafNode(Data);
		}

		@Override
		public void doRemoveData(Data Data, double distance) throws DataNotFound {
			if (thisNode.children.remove(Data) == null) {
				throw new DataNotFound();
			}
		}

		public void _checkChildClass(IndexItem child) {
			assert child instanceof MTree.Entry;
		}
	}

	class NonLeafNodeTrait extends NodeTrait implements Leafness<Data> {

		public void doAddData(Data Data, double distance) {
			class CandidateChild {
				Node node;
				double distance;
				double metric;

				private CandidateChild(Node node, double distance, double metric) {
					this.node = node;
					this.distance = distance;
					this.metric = metric;
				}
			}

			CandidateChild minRadiusIncreaseNeeded = new CandidateChild(null, -1.0, Double.POSITIVE_INFINITY);
			CandidateChild nearestDistance = new CandidateChild(null, -1.0, Double.POSITIVE_INFINITY);

			for (IndexItem item : thisNode.children.values()) {
				@SuppressWarnings("unchecked")
				Node child = (Node) item;
				double childDistance = thisNode.mtree().distanceFunction.calculate(child.Data, Data);
				if (childDistance > child.radius) {
					double radiusIncrease = childDistance - child.radius;
					if (radiusIncrease < minRadiusIncreaseNeeded.metric) {
						minRadiusIncreaseNeeded = new CandidateChild(child, childDistance, radiusIncrease);
					}
				} else {
					if (childDistance < nearestDistance.metric) {
						nearestDistance = new CandidateChild(child, childDistance, childDistance);
					}
				}
			}

			CandidateChild chosen = (nearestDistance.node != null)
					? nearestDistance
					: minRadiusIncreaseNeeded;

			Node child = chosen.node;
			try {
				child.addData(Data, chosen.distance);
				thisNode.updateRadius(child);
			} catch (SplitNodeReplacement e) {
				// Replace current child with new nodes
				IndexItem placeholder = thisNode.children.remove(child.Data);
				assert placeholder != null;

				for (int i = 0; i < e.newNodes.length; ++i) {
					@SuppressWarnings("unchecked")
					Node newChild = (Node) e.newNodes[i];
					distance = thisNode.mtree().distanceFunction.calculate(thisNode.Data, newChild.Data);
					thisNode.addChild(newChild, distance);
				}
			}
		}

		public void addChild(IndexItem newChild_, double distance) {
			@SuppressWarnings("unchecked")
			Node newChild = (Node) newChild_;

			class ChildWithDistance {
				Node child;
				double distance;

				private ChildWithDistance(Node child, double distance) {
					this.child = child;
					this.distance = distance;
				}
			}

			Deque<ChildWithDistance> newChildren = new ArrayDeque<ChildWithDistance>();
			newChildren.addFirst(new ChildWithDistance(newChild, distance));

			while (!newChildren.isEmpty()) {
				ChildWithDistance cwd = newChildren.removeFirst();

				newChild = cwd.child;
				distance = cwd.distance;
				if (thisNode.children.containsKey(newChild.Data)) {
					@SuppressWarnings("unchecked")
					Node existingChild = (Node) thisNode.children.get(newChild.Data);
					assert existingChild.Data.equals(newChild.Data);

					// Transfer the _children_ of the newChild to the existingChild
					for (IndexItem grandchild : newChild.children.values()) {
						existingChild.addChild(grandchild, grandchild.distanceToParent);
					}
					newChild.children.clear();

					try {
						existingChild.checkMaxCapacity();
					} catch (SplitNodeReplacement e) {
						IndexItem placeholder = thisNode.children.remove(existingChild.Data);
						assert placeholder != null;
						if (placeholder == null) {
							System.out.println(1);
						}
						for (int i = 0; i < e.newNodes.length; ++i) {
							@SuppressWarnings("unchecked")
							Node newNode = (Node) e.newNodes[i];
							distance = thisNode.mtree().distanceFunction.calculate(thisNode.Data, newNode.Data);
							newChildren.addFirst(new ChildWithDistance(newNode, distance));
						}
					}
				} else {
					thisNode.children.put(newChild.Data, newChild);
					thisNode.updateMetrics(newChild, distance);
				}
			}
		}

		public Node newSplitNodeReplacement(Data Data) {
			return new InternalNode(Data);
		}

		public void doRemoveData(Data Data, double distance) throws DataNotFound {
			for (IndexItem childItem : thisNode.children.values()) {
				@SuppressWarnings("unchecked")
				Node child = (Node) childItem;
				if (Math.abs(distance - child.distanceToParent) <= child.radius) {
					double distanceToChild = thisNode.mtree().distanceFunction.calculate(Data, child.Data);
					if (distanceToChild <= child.radius) {
						try {
							child.removeData(Data, distanceToChild);
							thisNode.updateRadius(child);
							return;
						} catch (DataNotFound e) {
							// If DataNotFound was thrown, then the Data was not found in the child
						} catch (NodeUnderCapacity e) {
							Node expandedChild = balanceChildren(child);
							thisNode.updateRadius(expandedChild);
							return;
						} catch (RootNodeReplacement e) {
							throw new RuntimeException("Should never happen!");
						}
					}
				}
			}

			throw new DataNotFound();
		}

		private Node balanceChildren(Node theChild) {
			// Tries to find anotherChild which can donate a grand-child to theChild.

			Node nearestDonor = null;
			double distanceNearestDonor = Double.POSITIVE_INFINITY;

			Node nearestMergeCandidate = null;
			double distanceNearestMergeCandidate = Double.POSITIVE_INFINITY;

			for (IndexItem child : thisNode.children.values()) {
				@SuppressWarnings("unchecked")
				Node anotherChild = (Node) child;
				if (anotherChild == theChild)
					continue;

				double distance = thisNode.mtree().distanceFunction.calculate(theChild.Data, anotherChild.Data);
				if (anotherChild.children.size() > anotherChild.getMinCapacity()) {
					if (distance < distanceNearestDonor) {
						distanceNearestDonor = distance;
						nearestDonor = anotherChild;
					}
				} else {
					if (distance < distanceNearestMergeCandidate) {
						distanceNearestMergeCandidate = distance;
						nearestMergeCandidate = anotherChild;
					}
				}
			}

			if (nearestDonor == null) {
				// Merge
				for (IndexItem grandchild : theChild.children.values()) {
					double distance = thisNode.mtree().distanceFunction.calculate(grandchild.Data,
							nearestMergeCandidate.Data);
					nearestMergeCandidate.addChild(grandchild, distance);
				}

				IndexItem removed = thisNode.children.remove(theChild.Data);
				assert removed != null;
				return nearestMergeCandidate;
			} else {
				// Donate
				// Look for the nearest grandchild
				IndexItem nearestGrandchild = null;
				double nearestGrandchildDistance = Double.POSITIVE_INFINITY;
				for (IndexItem grandchild : nearestDonor.children.values()) {
					double distance = thisNode.mtree().distanceFunction.calculate(grandchild.Data, theChild.Data);
					if (distance < nearestGrandchildDistance) {
						nearestGrandchildDistance = distance;
						nearestGrandchild = grandchild;
					}
				}

				IndexItem placeholder = nearestDonor.children.remove(nearestGrandchild.Data);
				assert placeholder != null;
				if (placeholder == null) {
					System.out.println(1);
				}
				theChild.addChild(nearestGrandchild, nearestGrandchildDistance);
				return theChild;
			}
		}

		public void _checkChildClass(IndexItem child) {
			assert child instanceof MTree.InternalNode
					|| child instanceof MTree.LeafNode;
		}
	}

	private class RootLeafNode extends Node {

		private RootLeafNode(Data Data) {
			super(Data, new RootNodeTrait(), new LeafNodeTrait());
		}

		void removeData(Data Data, double distance) throws RootNodeReplacement, DataNotFound {
			try {
				super.removeData(Data, distance);
			} catch (NodeUnderCapacity e) {
				assert children.isEmpty();
				throw new RootNodeReplacement(null);
			}
		}

		protected int getMinCapacity() {
			return 1;
		}

		void _checkMinCapacity() {
			assert children.size() >= 1;
		}
	}

	private class RootNode extends Node {

		private RootNode(Data Data) {
			super(Data, new RootNodeTrait(), new NonLeafNodeTrait());
		}

		void removeData(Data Data, double distance) throws RootNodeReplacement, NodeUnderCapacity, DataNotFound {
			try {
				super.removeData(Data, distance);
			} catch (NodeUnderCapacity e) {
				// Promote the only child to root
				@SuppressWarnings("unchecked")
				Node theChild = (Node) (children.values().iterator().next());
				Node newRoot;
				if (theChild instanceof MTree.InternalNode) {
					newRoot = new RootNode(theChild.Data);
				} else {
					assert theChild instanceof MTree.LeafNode;
					newRoot = new RootLeafNode(theChild.Data);
				}

				for (IndexItem grandchild : theChild.children.values()) {
					distance = MTree.this.distanceFunction.calculate(newRoot.Data, grandchild.Data);
					newRoot.addChild(grandchild, distance);
				}
				theChild.children.clear();

				throw new RootNodeReplacement(newRoot);
			}
		}

		@Override
		protected int getMinCapacity() {
			return 2;
		}

		@Override
		void _checkMinCapacity() {
			assert children.size() >= 2;
		}
	}

	private class InternalNode extends Node {
		private InternalNode(Data Data) {
			super(Data, new NonRootNodeTrait(), new NonLeafNodeTrait());
		}
	};

	private class LeafNode extends Node {

		public LeafNode(Data Data) {
			super(Data, new NonRootNodeTrait(), new LeafNodeTrait());
		}
	}

	private class Entry extends IndexItem {
		private Entry(Data Data) {
			super(Data);
		}
	}
}
