/**
 * Performs branch prediction using a single bit.
 * Uses the value of the last conditional directly to do a prediction.
 */
public class OneBitPredictor extends BitPredictor {
    /**
    * Initialises the predictor with a prediction table.
    * @param TABLE_SIZE The size of the prediction table.
    */
    public OneBitPredictor(int TABLE_SIZE) {
        super(TABLE_SIZE);
    }

    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        // Find the previous taken value for the address and use it to make a prediction.
        int indexInTable = indexInTable(address);
        boolean prediction = predictionTable[indexInTable] != 0;

        // Update the table with the actual value and return the prediction.
        predictionTable[indexInTable] = taken ? 1 : 0;
        return prediction;
    }
}