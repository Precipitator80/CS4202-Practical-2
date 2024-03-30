public class GSharePredictor extends TwoBitPredictor {
    // Tracks the history of whether branches were taken or not by setting bits 1 (taken) or 0 (not taken).
    long globalHistoryRegister;

    public GSharePredictor(int TABLE_SIZE) {
        super(TABLE_SIZE);
    }

    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        boolean result = super.predict(address, taken);
        globalHistoryRegister <<= 1; // Shift the global history register to the left.
        globalHistoryRegister |= taken ? 1 : 0; // Update the lowest bit to track results over time.
        globalHistoryRegister &= Long.MAX_VALUE; // Set the highest bit to 0 to avoid XOR getting negative value.
        return result;
    }

    @Override
    protected int indexInTable(long address) {
        return super.indexInTable(globalHistoryRegister ^ address); // XOR the global history register with the address to index the table.
    }
}
