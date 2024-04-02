import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A predictor that builds a profile of a program trace to form predictions.
 */
public class ProfiledPredictor implements BranchPredictor {
    Map<Long, Integer> decisionMap; // Maps addresses to a taken ration from a profile.

    /**
     * Initialises the decision map by profiling the program trace of a simulator.
     * @param simulator The simulator this predictor is for.
     */
    public ProfiledPredictor(BranchPredictorSimulator simulator) {
        try (BufferedReader reader = new BufferedReader(new FileReader(simulator.programTraceFileName),
                simulator.LINE_SIZE)) {
            decisionMap = new HashMap<>();
            reader.lines().forEach(line -> {
                boolean conditional = line.charAt(38) == '1'; // Whether the branch is conditional or not.
                // Simulate branch prediction if the branch is conditional.
                if (conditional) {
                    // Get the address.
                    BinaryAddress address = new BinaryAddress(line.substring(0, simulator.ADDRESS_LENGTH));
                    int taken = line.charAt(40) == '1' ? 1 : -1; // Whether the branch was taken or not.

                    // Increment the value in the map if taken and decrement otherwise.
                    if (!decisionMap.containsKey(address.address)) {
                        decisionMap.put(address.address, taken);
                    } else {
                        // Most efficient way to increment a Map value in Java - LE GALL Benoît - https://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java - Accessed 02.04.2024
                        decisionMap.merge(address.address, taken, Integer::sum);
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Could not read trace file:\n" + e.getMessage());
        }
    }

    @Override
    public boolean predict(BinaryAddress address, boolean taken) {
        return decisionMap.get(address.address) >= 0; // Predict taken / not taken based on which was chosen the most in the profile.
    }
}
