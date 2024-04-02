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
    /**
     * Runs the full variety of predictors for a given trace file.
     * If no trace file is passed, then all available trace files are used.
     * @param args The command line arguments. [programTrace.out] [outputFolder]
     */
    public static void main(String[] args) {
        // If no arguments were passed, use all available trace files.
        // Else, use the specified trace file.
        if (args.length == 0) {
            File folder = new File("trace-files/");
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (fileName.endsWith(".out")) {
                            simulatePredictors("trace-files/" + fileName);
                        }
                    }
                }
            }
        } else {
            if (args.length > 1) {
                outputDirectory = args[1];
            }
            simulatePredictors(args[0]);
        }
    }

    private static String outputDirectory = "output-data/"; // The output directory to use for data.

    /**
     * Runs all the predictor varieties for a single trace file.
     * @param programTraceFileName The file name of the trace file.
     */
    private static void simulatePredictors(String programTraceFileName) {
        // Delete any previous data for the same program trace.
        String truncatedFileName = programTraceFileName.replaceAll(".*/(.*?)\\..*", "$1");
        deleteFilesMatchingPattern(outputDirectory, truncatedFileName + "-.*");

        // Run the simulator in various configurations for the program trace.
        new BranchPredictorSimulator(new AlwaysTakenPredictor(), programTraceFileName).simulate();
        for (int i = 0; i < 12; i++) {
            int TABLE_SIZE = 32 * (int) Math.pow(2, i);
            System.out.println("Table size: " + TABLE_SIZE);
            System.out.println("-----");
            new BranchPredictorSimulator(new OneBitPredictor(TABLE_SIZE), programTraceFileName).simulate();
            new BranchPredictorSimulator(new TwoBitPredictor(TABLE_SIZE), programTraceFileName).simulate();
            new BranchPredictorSimulator(new GlobalPredictor(1, TABLE_SIZE), programTraceFileName).simulate();
            new BranchPredictorSimulator(new GlobalPredictor(2, TABLE_SIZE), programTraceFileName).simulate();
            new BranchPredictorSimulator(new GSharePredictor(TABLE_SIZE), programTraceFileName).simulate();
        }
        BranchPredictorSimulator profiledSimulator = new BranchPredictorSimulator(null, programTraceFileName);
        profiledSimulator.predictor = new ProfiledPredictor(profiledSimulator);
        profiledSimulator.simulate();
    }

    final int LINE_SIZE = 42; // The fixed size of each line.
    final int ADDRESS_LENGTH = 16; // The length of address strings.
    BranchPredictor predictor; // The predictor to use.
    int correct; // The number of correct guesses.
    int incorrect; // The number of incorrect guesses.
    String programTraceFileName; // The file name of the trace file.

    /**
     * Initialises the simulator with a predictor.
     * @param predictor The predictor to use.
     * @param programTraceFileName The file name of the trace file.
     */
    public BranchPredictorSimulator(BranchPredictor predictor, String programTraceFileName) {
        this.predictor = predictor;
        this.programTraceFileName = programTraceFileName;
    }

    /**
     * Simulates branch prediction when running a specific program.
     */
    void simulate() {
        // Read in lines and simulate each branch prediction.
        // Using a buffered reader seems to be faster than the memory map code commented out below.
        try (BufferedReader reader = new BufferedReader(new FileReader(programTraceFileName), LINE_SIZE)) {
            reader.lines().forEach(line -> {
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
            });
            exportData();
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
    }

    /**
     * Prints the simulator results and writes them to a file.
     */
    private void exportData() {
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
        String mispredictionRateString = df.format(100 * ((float) incorrect / (correct + incorrect)));
        stringBuilder.append("Misprediction Rate: ");
        stringBuilder.append(mispredictionRateString);
        stringBuilder.append('%');
        stringBuilder.append("\n---------------");
        System.out.println(stringBuilder.toString());

        try {
            String predictorName = predictor.getClass().getName();
            int TABLE_SIZE = -1;
            if (predictor instanceof BitPredictor) {
                TABLE_SIZE = ((BitPredictor) predictor).TABLE_SIZE;
            } else if (predictor instanceof GlobalPredictor) {
                GlobalPredictor globalPredictor = (GlobalPredictor) predictor;
                TABLE_SIZE = globalPredictor.TABLE_SIZE;
                predictorName += String.valueOf(globalPredictor.NUMBER_OF_BITS);
            }
            String truncatedFileName = programTraceFileName.replaceAll(".*/(.*?)\\..*", "$1");
            Path filePath = Paths.get(outputDirectory + truncatedFileName + "-" + predictorName + ".csv");
            if (Files.notExists(filePath)) {
                filePath.toFile().createNewFile();
            }

            double mispredictionRate = Double.parseDouble(mispredictionRateString);

            stringBuilder.setLength(0);
            stringBuilder.append(TABLE_SIZE);
            stringBuilder.append(',');
            stringBuilder.append(mispredictionRate);
            stringBuilder.append(System.lineSeparator());

            Files.write(filePath, stringBuilder.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to export simulator data:\n" + e.toString());
        }
    }

    /**
     * Deletes all files matching a given pattern.
     * @param directoryPath The directory to look in.
     * @param pattern The pattern to use for matching files to delete.
     */
    public static void deleteFilesMatchingPattern(String directoryPath, String pattern) {
        // Open the directory.
        File directory = new File(directoryPath);

        // Check if the specified path is a directory.
        if (!directory.isDirectory()) {
            System.out.println("Specified path is not a directory.");
            return;
        }

        // List all files in the directory.
        File[] files = directory.listFiles();

        // If the directory is empty, return.
        if (files == null || files.length == 0) {
            System.out.println("Directory is empty.");
            return;
        }

        // Iterate over each file in the directory.
        for (File file : files) {
            // Check if the file matches the pattern.
            if (file.isFile() && file.getName().matches(pattern)) {
                try {
                    // Delete the file.
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
