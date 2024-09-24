# CS4202-Practical-2 - Branch Predictor

The coursework specification detailed the implementation and analysis of several branch prediction strategies. These included always taken, two-bit, GShare and profiled predictors. The final implementation additionally includes one-bit, global one-bit and global two-bit predictors. Bit predictors, including the GShare predictor, are tested with a table size range from 32 to 16,384 entries. Tests are done on a range of program traces provided with the specification, both using entire files and smaller portions. In summary, the GShare predictor performed best with a large table size for full trace files and two-bit predictors outperformed one-bit predictors.

[See the project report here.](190018469-CS4202-Practical-2.pdf)

To compile, run the following in the src folder:
```bash
make all
```

If make is not supported, run the compilation command manually:
```
javac *.java
```

After this, the simulator can be run with the following command:
```
java BranchPredictorSimulator [programTrace.out] [outputFolder]
```

programTrace.out: Path to trace file.  
outputFolder: Path to data output folder.  
Passing no arguments runs all the trace files in the "trace-files" folder. By default, results are passed to the "output-data" folder in the same directory from which the program is run.  
