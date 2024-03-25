/**
 * A predictor that always predicts a branch to be taken.
 */
public class AlwaysTakenPredictor implements BranchPredictor {
    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        return true;
    }
}
