# CS4202-Practical-2 - Branch Predictor
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