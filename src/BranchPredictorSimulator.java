import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
// import java.io.RandomAccessFile;
// import java.nio.MappedByteBuffer;
// import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * Class to simulate branch prediction when running a specific program.
 */
public class BranchPredictorSimulator {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(
                    "Invalid arguments. Arguments must be in the form:\n ./BranchPredictorSimulator <path to trace file> [path to data output folder]");
            System.exit(1);
        }

        String programTraceFileName = args[0];
        String outputDirectory = "output-data/";
        if (args.length > 1) {
            outputDirectory = args[1];
        }

        clearCsvFiles(outputDirectory);

        new BranchPredictorSimulator(new AlwaysTakenPredictor(), outputDirectory).simulate(programTraceFileName);
        for (int i = 0; i < 10; i++) {
            int TABLE_SIZE = 32 * (int) Math.pow(2, i);
            System.out.println("Table size: " + TABLE_SIZE);
            System.out.println("-----");
            new BranchPredictorSimulator(new OneBitPredictor(TABLE_SIZE),
                    outputDirectory).simulate(programTraceFileName);
            new BranchPredictorSimulator(new TwoBitPredictor(TABLE_SIZE),
                    outputDirectory).simulate(programTraceFileName);
            new BranchPredictorSimulator(new GlobalPredictor(1, TABLE_SIZE),
                    outputDirectory).simulate(programTraceFileName);
            new BranchPredictorSimulator(new GlobalPredictor(2, TABLE_SIZE),
                    outputDirectory).simulate(programTraceFileName);
            new BranchPredictorSimulator(new GSharePredictor(TABLE_SIZE),
                    outputDirectory).simulate(programTraceFileName);
        }
    }

    final int LINE_SIZE = 42; // The fixed size of each line.
    final int ADDRESS_LENGTH = 16; // The length of address strings.
    BranchPredictor predictor; // The predictor to use.
    String outputDirectory; // The output directory to use for data.
    int correct; // The number of correct guesses.
    int incorrect; // The number of incorrect guesses.

    /**
     * Initialises the simulator with a predictor.
     * @param predictor The predictor to use.
     * @param outputDirectory The output directory to use for data.
     */
    public BranchPredictorSimulator(BranchPredictor predictor, String outputDirectory) {
        this.predictor = predictor;
        this.outputDirectory = outputDirectory;
    }

    /**
     * Simulates branch prediction when running a specific program.
     * @param programTraceFileName The file name of the program trace.
     */
    void simulate(String programTraceFileName) {
        // Read in lines and simulate each branch prediction.
        // Using a buffered reader seems to be faster than the memory map code commented out below.
        try (BufferedReader reader = new BufferedReader(new FileReader(programTraceFileName), LINE_SIZE)) {
            reader.lines().forEach(line -> {
                // BinaryAddress memoryAddress = new BinaryAddress(line.substring(17, 33));
                // int size = Integer.parseInt(line.substring(36, 39));
                // simulateMemoryOp(memoryAddress, size, 0);

                //boolean direct = buffer.get(36) == '1';
                boolean conditional = line.charAt(38) == '1'; // Whether the branch is conditional or not.
                // Simulate branch prediction if the branch is conditional.
                if (conditional) {
                    // Get the address.
                    BinaryAddress address = new BinaryAddress(line.substring(0, ADDRESS_LENGTH));
                    boolean taken = line.charAt(40) == '1'; // Whether the branch was taken or not.

                    // Make a prediction.
                    boolean prediction = predictor.predict(address, taken);

                    // Check whether the predictor was correct to update metrics.
                    if (taken == prediction) {
                        correct++;
                    } else {
                        incorrect++;
                    }
                }

                // No need to explicitly close the MappedByteBuffer.
                // The resources will be released when the FileChannel is closed.
            });
        }
        /*
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
                boolean conditional = buffer.get(38) == '1'; // Whether the branch is conditional or not.
                // Simulate branch prediction if the branch is conditional.
                if (conditional) {
                    // Get the address.
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int j = 0; j < ADDRESS_LENGTH; j++) {
                        lineBuilder.append((char) buffer.get());
                    }
                    BinaryAddress address = new BinaryAddress(lineBuilder.toString());
                    boolean taken = buffer.get(40) == '1'; // Whether the branch was taken or not.
        
                    // Make a prediction.
                    boolean prediction = predictor.predict(address, taken);
        
                    // Check whether the predictor was correct to update metrics.
                    if (taken == prediction) {
                        correct++;
                    } else {
                        incorrect++;
                    }
                }
        
                // No need to explicitly close the MappedByteBuffer.
                // The resources will be released when the FileChannel is closed.
            }
        }*/ catch (IOException e) {
            System.err.println("Could not read trace file:\n" + e.getMessage());
        }

        // Print the results of the simulator. Calculate the accuracy to 2 decimal places.
        StringBuilder stringBuilder = new StringBuilder("Predictor Type: ");
        stringBuilder.append(predictor.getClass().getName());
        stringBuilder.append('\n');
        stringBuilder.append("Number of correct guesses: ");
        stringBuilder.append(correct);
        stringBuilder.append('\n');
        stringBuilder.append("Number of incorrect guesses: ");
        stringBuilder.append(incorrect);
        stringBuilder.append('\n');
        stringBuilder.append("Total number of guesses: ");
        stringBuilder.append(correct + incorrect);
        stringBuilder.append('\n');
        DecimalFormat df = new DecimalFormat("#.##");
        String mispredictionRate = df.format(100 * ((float) incorrect / (correct + incorrect)));
        stringBuilder.append("Misprediction Rate: ");
        stringBuilder.append(mispredictionRate);
        stringBuilder.append('%');
        stringBuilder.append("\n---------------");
        System.out.println(stringBuilder.toString());
        exportData();
    }

    private void exportData() {
        try {
            if (predictor instanceof BitPredictor) {
                int TABLE_SIZE = ((BitPredictor) predictor).TABLE_SIZE;
                String predictorName = predictor.getClass().getName();
                Path filePath = Paths.get(outputDirectory + predictorName + ".csv");
                if (Files.notExists(filePath)) {
                    filePath.toFile().createNewFile();
                }

                DecimalFormat df = new DecimalFormat("#.##");
                String mispredictionRateString = df.format(100 * ((float) incorrect / (correct + incorrect)));
                //float mispredictionRate = df.parse(mispredictionRateString).floatValue();
                double mispredictionRate = Double.parseDouble(mispredictionRateString);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(TABLE_SIZE);
                stringBuilder.append(',');
                stringBuilder.append(mispredictionRate);
                stringBuilder.append(System.lineSeparator());

                Files.write(filePath, stringBuilder.toString().getBytes(), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            System.out.println("Failed to export simulator data:\n" + e.toString());
        }
    }

    public static void clearCsvFiles(String outputDirectory) {
        File directory = new File(outputDirectory);

        // Check if the specified path is a directory
        if (!directory.isDirectory()) {
            System.out.println("Specified path is not a directory.");
            return;
        }

        // List all files in the directory
        File[] files = directory.listFiles();

        // If the directory is empty, return
        if (files == null || files.length == 0) {
            System.out.println("Directory is empty.");
            return;
        }

        // Iterate over each file in the directory
        for (File file : files) {
            // Check if the file is a CSV file
            if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                try {
                    // Delete the file
                    if (file.delete()) {
                        System.out.println("Deleted file: " + file.getName());
                    } else {
                        System.out.println("Failed to delete file: " + file.getName());
                    }
                } catch (SecurityException e) {
                    System.out.println("Permission denied to delete file: " + file.getName());
                }
            }
        }
    }
}
