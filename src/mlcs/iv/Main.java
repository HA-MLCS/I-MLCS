package mlcs.iv;

import arlp.mlcs.Crawler;
import erlp.mlcs.stage2.ERLP_MLCS;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class Main {
    public static double P = 0.8;

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println(
                    "Usage:HorizontalCrawler /path/to/your/data/file -Dmlcs.max-thread=2 -Djava.util.Arrays.useLegacyMergeSort=true -Dmlcs.p=1");
            return;
        }
        if (System.getProperty("mlcs.p") == null) {
            System.out.println("should set -Dmlcs.p between 0  and 1");
            return;
        }
        P = Double.parseDouble(System.getProperty("mlcs.p"));
        if (P == 1.0) {
            ERLP_MLCS.main(args);
        } else {
            Crawler.main(args, P);
        }
    }

    public static void writeFile(String fileName, List<String> stringList) {
        try {
            FileWriter writer = new FileWriter(fileName);
            for (String str : stringList) {
                writer.write(str + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
