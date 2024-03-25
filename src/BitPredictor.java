/**
 * An abstract class for bit predictors using a prediction table.
 */
public abstract class BitPredictor implements BranchPredictor {
    private final int TABLE_SIZE;
    protected boolean[] predictionTable;

    /**
     * Initialises the predictor with a prediction table.
     * @param TABLE_SIZE The size of the prediction table.
     */
    public BitPredictor(int TABLE_SIZE) {
        this.TABLE_SIZE = TABLE_SIZE;
        this.predictionTable = new boolean[TABLE_SIZE];
    }

    /**
     * Gets the index of an address in the prediction table.
     * @param address The address to check.
     * @return The index that the address maps to in the prediction table.
     */
    protected int indexInTable(BinaryAddress address) {
        return (int) (address.address % TABLE_SIZE);
    }
}
