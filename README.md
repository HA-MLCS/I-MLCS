

#IV_MLCS
###Introduction
**IV-MLCS**(**I**ntegrated and **V**isualized **M**ultiple **L**ongest **C**ommon **S**ubsequence algorithm) can deal with big
 sequences effectively and efficiently in terms of algorithms's running time and results'precision and can visualize the structrue of all
 the MLCSs. In summary, the main contributions of this algorithm are:
* We propose a novel problem-solving model, namely Non-redundant Common Subsequence Graph, NCSG, and its continually dynamic optimizing strategy.
* with extracting functional commonalities both exact and approximate MLCS algorithms and introducing series of well-designed strategies.
* By adopting visualization technology, IV-MLCS can visualize mined all of MLCSs’ structure for further insight and mining by user.

###Install & Run
1. Install JDK-1.8;
2. in the eclipse ,you can run with parameter [fileName][-Dmlcs.p][-Djava.util.Arrays.useLegacyMergeSort]


    "fileName":program parameter,files should be put in "file" Folder to be read
     [-Dmlcs.p]: VM parameter,the range of this value is between 0 and 1,the bigger the number, the higher the accuracy
     [-Djava.util.Arrays.useLegacyMergeSort]:VM parameter,should be set true
### Project structure
```
│  .classpath
│  .project
│  README.md
│
├─.settings
│      org.eclipse.jdt.core.prefs
│
├─file                        //Put the files that need to be processed here.
│      100_3_4_1.txt
│
├─lib
└─src
    ├─arlp                     //Approximate Real Linear Parallel MLCS algorithm
    │  └─mlcs
    │      │  Crawler.java      //the entrance of approximate real Linear parallel MLCS algorithm
    │      │  Graph.java
    │      │  Location.java
    │      │  Mlcs.java
    │      │  MyBestOrderSort.java
    │      │  RadixSorting.java
    │      │  RadixSortingbeta2.java
    │      │  Sequence.java
    │      │  SingleDeminsionSortedResult.java
    │      │
    │      └─util               //Toolkit for approximate real Linear parallel MLCS algorithm
    │              DrawNode.java
    │              GrowableQueue.java
    │              Logger.java
    │              LogWriter.java
    │              Queues.java
    │              Stopwatch.java
    │              TestDrawTree.java
    │              TreePanel.java
    │
    ├─erlp                     //Exact Real Linear Parallel MLCS algorithm
    │  └─mlcs
    │      │  Border.java
    │      │  BoundedPaths.java
    │      │  Graph.java
    │      │  InNode.java
    │      │  Location.java
    │      │  Mlcs.java
    │      │  Sequence.java
    │      │  SubGraph.java
    │      │
    │      ├─stage2
    │      │      ERLP_MLCS.java //the entrance of exact real linear parallel MLCS algorithm
    │      │      IndegreeGraph.java
    │      │
    │      └─util                //Toolkit for exact real linear parallel MLCS algorithm
    │              DrawNode.java
    │              Logger.java
    │              LogWriter.java
    │              Queues.java
    │              Seilize.java
    │              StepGenerator.java
    │              Stopwatch.java
    │              TestDrawTree.java
    │              TreePanel.java
    │
    └─mlcs
        └─iv
                Main.java         //Main class


```