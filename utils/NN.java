package utils;

public class NN {

    public TimeIntervalMR mra;
    public TimeIntervalMR mrb;

    public double sim = 0;
    public double simUpper1 = 0;
    public double simUpper2 = 0;

    public NN(TimeIntervalMR mra, TimeIntervalMR mrb) {
        this.mra = mra;
        this.mrb = mrb;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return String.format("%6d@a=%f,b=%f\n%6d@a=%f,b=%f Sim: %.8f\n SimUB: %.8f", mra.objectID, mra.a, mra.b, mrb.objectID,
                mrb.a,
                mrb.b, sim, simUpper1);
    }
}
