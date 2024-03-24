import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * Class to simulate branch prediction when running a specific program.
 */
public class BranchPredictorSimulator {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(
                    "Invalid arguments. Arguments must be in the form:\n ./BranchPredictorSimulator <path to trace file>");
            System.exit(1);
        }

        new BranchPredictorSimulator(new OneBitPredictor()).simulate(args[0]);
    }

    final int LINE_SIZE = 42; // The fixed size of each line.
    BranchPredictor predictor; // The predictor to use.
    int correct; // The number of correct guesses.
    int incorrect; // The number of incorrect guesses.

    /**
     * Initialises the simulator with a predictor.
     * @param predictor The predictor to use.
     */
    public BranchPredictorSimulator(BranchPredictor predictor) {
        this.predictor = predictor;
    }

    /**
     * Simulates branch prediction when running a specific program.
     * @param programTraceFileName The file name of the program trace.
     */
    void simulate(String programTraceFileName) {
        // Read in lines and simulate each branch prediction.
        // Map the file directly to memory and read each line using the known line size.
        try (RandomAccessFile file = new RandomAccessFile(programTraceFileName, "r");
                FileChannel fileChannel = file.getChannel()) {

            long fileSize = fileChannel.size();
            long numLines = fileSize / LINE_SIZE;

            for (long i = 0; i < numLines; i++) {
                // Map the line to a byte buffer.
                long position = i * LINE_SIZE;
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, LINE_SIZE);

                // Read from the buffer.
                //boolean direct = buffer.get(36) == '1';
                boolean conditional = buffer.get(38) == '1';
                boolean taken = buffer.get(40) == '1';

                // Simulate branch prediction.
                boolean prediction = predictor.predict(conditional);
                if (taken == prediction) {
                    correct++;
                } else {
                    incorrect++;
                }

                // No need to explicitly close the MappedByteBuffer.
                // The resources will be released when the FileChannel is closed.
            }
        } catch (IOException e) {
            System.err.println("Could not read trace file:\n" + e.getMessage());
        }

        // Print the results of the simulator. Calculate the accuracy to 2 decimal places.
        System.out.println("Number of correct guesses: " + correct);
        System.out.println("Number of incorrect guesses: " + incorrect);
        System.out.println("Total number of guesses: " + (correct + incorrect));
        DecimalFormat df = new DecimalFormat("#.##");
        String accuracy = df.format(100 * ((float) correct / (correct + incorrect)));
        System.out.println("Accuracy: " + accuracy + "%");
    }
}
