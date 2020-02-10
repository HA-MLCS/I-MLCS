# I-MLCS
## Introduction
We propose a novel *MLCS* mining tool ***I-MLCS*** (<u>**I**</u>ntegrated  <u>**M**</u>ultiple <u>**L**</u>ongest <u>**C**</u>ommon <u>**S**</u>ubsequence algorithm) for mining big sequence data. The algorithm includes a series of novel *MLCS* mining strategies, and can integrate both exact and approximate *MLCS* algorithms to effectively meet those challenges. Extensive experiments on both synthetic and real-world biological sequence datasets demonstrate *I-MLCS*’s outstanding performance in terms of both running time efficiency and result quality compared to the state-of-the-art existing exact and approximate *MLCS* algorithms. 

In summary, the main contributions of this algorithm are: 
 - It reveals and verifies some serious weaknesses of current popular dominant-based point *MLCS* algorithms through both theoretical analysis and experiments for the first time.
 - It proposes a novel problem-solving graph model, called Non-redundant Common Subsequence Graph (*NCSG*), and introduces a series of novel *MLCS* mining techniques for *NCSG* to harness the big sequence data efficiently and effectively.
 - Based on several shared functions, it designs an novel integrated MLCS mining tool *I-MLCS* that integrates both exact and approximate algorithms with the ability to customize the mining precision and visualizing the mined results.

## Install & Run

 1. Install JDK-1.8;
 2. Put the data files to be processed into the 'file' folder of this project;
 3. Run the following command in a shell command window.
```
java -jar -Dmlcs.max-thread=2 -Djava.util.Arrays.useLegacyMergeSort=true -Dmlcs.p=1 I-MLCS.jar [fileName]
```
 - [fileName]: program parameter, files should be put in 'file' Folder to be read;
 - [-Dmlcs.max-thread]: VM parameter, the number of threads you need to start;
 - [-Djava.util.Arrays.useLegacyMergeSort]: VM parameter, should be set true;
 - [-Dmlcs.p]: VM parameter, the range of this value is between 0 and 1, the bigger the number, the higher the accuracy;
