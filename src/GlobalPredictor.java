public class GlobalPredictor implements BranchPredictor {
    BitPredictor[] predictors;
    boolean lastTaken;

    public GlobalPredictor(int numberOfBits, int TABLE_SIZE) {
        predictors = new BitPredictor[2];
        for (int i = 0; i < predictors.length; i++) {
            switch (numberOfBits) {
                case 1:
                    predictors[i] = new OneBitPredictor(TABLE_SIZE);
                    break;
                case 2:
                    predictors[i] = new TwoBitPredictor(TABLE_SIZE);
                    break;
            }
        }
    }

    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        int index = lastTaken ? 1 : 0;
        boolean prediction = predictors[index].predict(address, taken);
        lastTaken = taken;
        return prediction;
    }
}
