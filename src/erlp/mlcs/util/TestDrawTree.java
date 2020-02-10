package erlp.mlcs.util;

import javax.swing.*;

import erlp.mlcs.Location;
import erlp.mlcs.Mlcs;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestDrawTree extends JFrame {

    public TestDrawTree() {
        super("MLCS");
    }

    public static void visualize(List<List<Location>> data, boolean isDraw, int maxWidth,Mlcs mlcs) {
        if (!isDraw)
            return;

        TestDrawTree testDrawTree = new TestDrawTree();
        testDrawTree.initComponents2(testDrawTree, data, maxWidth,mlcs);
    }





    
    public void initComponents2(TestDrawTree testDrawTree, List<List<Location>> data, int maxWidth,Mlcs mlcs) {
        /*
         * initial the data of tree
         */

        DrawNode drawNode = new DrawNode(0, "-", mlcs.start);
        drawNode.setLayer(0);
        DrawNode preNode = drawNode;

        int i = 0;
        int size = data.size();
        for(List<Location> l:data){
        	if(i > 100000)
        		break;
        	int j = 0;
        	for(Location location:l){
        		
                if (preNode.haschild(String.valueOf(location.getName()))) {
                    preNode = preNode.getChild(String.valueOf(location.getName()));
                } else {
                    DrawNode newNode = new DrawNode(j + i * size + 1, location.getName() + "" , location);
                    newNode.setLayer(j+1);
                    preNode.addchild(newNode);
                    preNode = newNode;
                }
                j++;
        	}
        	preNode = drawNode;
//            System.out.println(i);
            i++;
        }
         Queue<DrawNode> drawTreeQueue = new LinkedBlockingQueue<>();
        DrawNode firstNode = null;
        boolean isUsed = false;
        drawTreeQueue.add(drawNode);

        HashMap<Integer, HashMap<String, DrawNode>> levelNode = new HashMap<>();

        while (!drawTreeQueue.isEmpty()) {

            DrawNode newdrawNode = drawTreeQueue.poll();


            if (!isUsed) {
                firstNode = newdrawNode;
                isUsed = true;
            }

            for (String c : newdrawNode.getAllChild().keySet()) {

                DrawNode temp = newdrawNode.getChild(c);
                if (levelNode.get(temp.getLayer()) == null) {
                    levelNode.put(temp.getLayer(), new HashMap<String, DrawNode>());
                    
                }

                if (levelNode.get(temp.getLayer()).get(temp.getName() + temp.getLocation()) == null) {
                	levelNode.get(temp.getLayer()).put(temp.getName() + temp.getLocation(), temp);
                    drawTreeQueue.add(temp);
                } 
            }
        }


        /*
         * create panel 
         * 
         */

        testDrawTree.setLayout(null);
        testDrawTree.setBounds(0, 0, 1400, 800);


        TreePanel panel1 = new TreePanel(TreePanel.CHILD_ALIGN_RELATIVE);
        panel1.setTree(firstNode, levelNode);    //set data into tree

        panel1.setPreferredSize(new Dimension(maxWidth, 800));//Mainly this code, set the preferred size of panel, and ensure that the width is greater than the width of JScrollPane, so that the following JScrollPane will appear scrollbars

        JScrollPane scrollPane = new JScrollPane(panel1);   //window sliding
        scrollPane.setBounds(0, 0, 1400, 800);
        this.getContentPane().add(scrollPane);
        this.setTitle("MLCS");
        this.setVisible(true);
 //       this.setTitle("ML");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        
        testDrawTree.setVisible(true);
    }

}