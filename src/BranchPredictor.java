// An interface for branch predictors.
public interface BranchPredictor {
    /**
     * Method to predict whether a branch is taken or not.
     * @param address The program counder address of the branch.
     * @param taken Whether the branch was actually taken or not. Used for updating predictor.
     * @return whether the branch is predicted to be taken or not.
     */
    abstract boolean predict(BinaryAddress address, boolean taken);
}