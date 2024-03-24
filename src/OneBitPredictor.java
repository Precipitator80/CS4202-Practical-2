/**
 * Performs branch prediction using a single bit.
 * Uses the value of the last bit / conditional directly to do a prediction.
 */
public class OneBitPredictor implements BranchPredictor {
    boolean lastBit; // The previously read bit / conditional.

    @Override
    public boolean predict(boolean bit) {
        boolean prediction = lastBit;
        lastBit = bit;
        return prediction;
    }
}