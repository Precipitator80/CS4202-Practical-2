// An interface for branch predictors.
public interface BranchPredictor {
    /**
     * Method to predict whether a branch is taken or not.
     * @param bit The bit indicating the current conditional.
     * @return whether the branch is predicted to be taken or not.
     */
    abstract boolean predict(boolean bit);
}