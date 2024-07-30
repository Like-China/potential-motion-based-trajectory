package utils;

public class NN {

    public Trajectory trj;

    public double sim;

    public NN(Trajectory trj, double sim) {
        this.trj = trj;
        this.sim = sim;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.trj + "@" + sim;
    }
}
