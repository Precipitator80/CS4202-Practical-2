/**
 * A predictor that uses a bit predictor for each possible outcome of previous branches.
 */
public class GlobalPredictor implements BranchPredictor {
    BitPredictor[] predictors; // The predictors for each possible outcome.
    boolean lastTaken; // Whether the last branch was taken or not.

    // Store parameters for logging.
    final int NUMBER_OF_BITS; // The number of bits used for the bit predictors.
    final int TABLE_SIZE; // The table size for the bit predictors.

    /**
     * Initialises the array of bit predictors.
     * @param NUMBER_OF_BITS The number of bits used for the bit predictors.
     * @param TABLE_SIZE The table size for the bit predictors.
     */
    public GlobalPredictor(int NUMBER_OF_BITS, int TABLE_SIZE) {
        this.NUMBER_OF_BITS = NUMBER_OF_BITS;
        this.TABLE_SIZE = TABLE_SIZE;
        predictors = new BitPredictor[2];
        for (int i = 0; i < predictors.length; i++) {
            switch (NUMBER_OF_BITS) {
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
        int index = lastTaken ? 1 : 0; // Select which predictor to use based on the previous branch result(s).
        boolean prediction = predictors[index].predict(address, taken); // Predict using the chosen predictor.
        lastTaken = taken; // Update the tracker for the previous branch result(s).
        return prediction;
    }
}
