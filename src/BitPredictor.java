/**
 * An abstract class for bit predictors using a prediction table.
 */
public abstract class BitPredictor implements BranchPredictor {
    protected final int TABLE_SIZE;
    protected int[] predictionTable;

    /**
     * Initialises the predictor with a prediction table.
     * @param TABLE_SIZE The size of the prediction table.
     */
    public BitPredictor(int TABLE_SIZE) {
        this.TABLE_SIZE = TABLE_SIZE;
        this.predictionTable = new int[TABLE_SIZE];
    }

    /**
     * Gets the index of an address in the prediction table.
     * @param address The address to check.
     * @return The index that the address maps to in the prediction table.
     */
    protected int indexInTable(long address) {
        return (int) (address % TABLE_SIZE);
    }
}
