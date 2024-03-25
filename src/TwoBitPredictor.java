/**
 * Performs branch prediction using two bits.
 * Uses the value of the last conditional within an FSM to do a prediction.
 */
public class TwoBitPredictor extends BitPredictor {
    /**
    * Initialises the predictor with a prediction table.
    * @param TABLE_SIZE The size of the prediction table.
    */
    public TwoBitPredictor(int TABLE_SIZE) {
        super(TABLE_SIZE);
    }

    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        // Find the previous state for the address and use it to make a prediction.
        int indexInTable = indexInTable(address);
        int value = predictionTable[indexInTable];
        boolean prediction = (value & 2) != 0;

        // Update the table by switching the state using the actual value.
        switch (value) {
            case 0:
                if (taken) { // Strongly not taken.
                    predictionTable[indexInTable] = 1; // Move to weakly not taken.
                }
                break;
            case 1: // Weakly not taken.
                if (taken) {
                    predictionTable[indexInTable] = 3; // Move to strongly taken.
                } else {
                    predictionTable[indexInTable] = 0; // Move to strongly not taken.
                }
                break;
            case 2: // Weakly taken.
                if (taken) {
                    predictionTable[indexInTable] = 3; // Move to strongly taken.
                } else {
                    predictionTable[indexInTable] = 0; // Move to strongly not taken.
                }
                break;
            case 3: // Strongly taken.
                if (!taken) {
                    predictionTable[indexInTable] = 2; // Move to weakly taken.
                }
                break;
        }

        // Return the prediction.
        return prediction;
    }
}