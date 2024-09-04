# Intersection Similarity Join Over Moving Objects (IS-Join)
This implementation follows the content of the article "Intersection Similarity Join Over Moving Objects",
which implements the parts of BF-Alg, MJ-Alg and BJ-Alg, and evaluates their performances for different settings.

## Environment
Ubuntu 11

Java SE 1.8.0_91

## balltree
The package 'balltree' implements the indexing structure of our Hball-tree with/without re-partition techniques.
Specifiaclly, the BallNode.java and the BallTree.java are the implementation of Hball-tree without re-partition techniques.
The TernaryBallNode.java and the TernaryBallTree.java are the implementation of Hball-tree with re-partition techniques.

## mtree
The package 'mtree' implements the indexing construction of M*-tree, which serves as a baseline method.

## poi
QuadTree.java is a simple implementation for retrieving POI locations for a given rectangle range, which can be alternative to
other indexes.

## utils
The package 'utils' implements the classes of Location, Ellipse, ContactPair, etc.

Location: (object id, longititude, latitude, x, y, timestamp) with some distance calculation methods

NN: A pair of objects satisfying thresohold constraints

Point: A tuple (x,y) represents the POI location

TimePointMR: time-point motion range implementation

TimePointMR: time-interval motion range implementation

Trajectory: All moving objects are represented by their trajectories.

## evaluation
### Setting
The default parameter settings are listed in Settings.java.
### data loading
All data source is loaded by Loader.java, the data source is detailed below in open data.
### test
The evaluation of BF-Alg, MJ-Alg, BJ#-Alg and BJ-Alg are implemented by BFAlg.java, MJALG.java, BJAlgNoRepartition.java and BJAlg.java, respectively. 
## main entrance & quick start
Test.java is the main entrance of this project for overall evaluation with varied parameters.

Important Parameters:

- `data`: (String) The dataset name, including 'SIFTSMALL','SIFT','GIST','MNIST','Fashion-MNIST'.
- `expNB`: (int) The number of individual experiments.
- `objNB`: (int) The number of moving objects.
- `poiNB`: (int) The totoal number of POI locations.
- `isShuffle`: (boolean) If true, shuffle the dataset
- `simThreshold`: (double) The similarity threshold.
- `I`: (int) The length of the given time interval.
- `intervalNum`: (int) The number of probed time points within I.

# Open Data
The Geolife dataset is available at https://www.kaggle.com/datasets/arashnic/microsoft-geolife-gps-trajectory-dataset.

The Porto dataset is available at https://www.kaggle.com/c/pkdd-15-predict-taxi-service-trajectory-i/data.
