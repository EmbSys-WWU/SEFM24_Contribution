# SEFM24_Contribution

This reproduction package contains the prototypical implementation of our approach to leverage symbolic execution for precise information flow analysis of timed concurrent systems.

All examples are contained in the examples directory. Executing the provided JAR file (requires Java 21 or newer) runs the analysis on all these examples, printing some results into the console while providing a more detailed report for each example in a Report.txt file in the respective directory. Pregenerated reports are already present.

Please note that due to differences in how compilers handle generics, the system can currently only be compiled with the eclipse compiler for java (ECJ).
