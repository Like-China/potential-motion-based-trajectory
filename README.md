# potential-motion-based-trajectory
 
1. parallel 
2. generic
3. upper bound
4. road network support
5. baseline
6. unalighed support
7. time complexity analysis
8. uncertain road network/trajectories

The peroblem of existing work on traj sim:
1. potential motion range
2. time-aligned
3. threshold is not easily determined.

If a is NN to b, then record b as a's NN to update.

2024/7/30 update the computation of a,b in TimeIntervalMR, set the 1.05x
check if a,b==0
check if maxspeed == nan

change "double minDist = 10000" in TernaryBallTree to "double minDist = Double.MAX_VALUE;"

* strategy
1. use tree index
2. use order for nn
3. construct upper bound / lower bound