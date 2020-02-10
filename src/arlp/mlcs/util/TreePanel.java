package arlp.mlcs.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreePanel extends JPanel {

	private DrawNode tree; // save whole tree
	private int gridWidth = 30; // the width of each node
	private int gridHeight = 20; // the height of each node
	private int vGap = 30; // vertical distance of every two nodes
	private int hGap = 20; // horizontal distance of every two nodes

	private int startY = 100; // the Y of root node
	private int startX = 0; // the X of root node 

	private HashMap<Integer, HashMap<String, DrawNode>> treeMap; // 

	private int childAlign; // child alignment
	public static int CHILD_ALIGN_ABSOLUTE = 0; // 
	public static int CHILD_ALIGN_RELATIVE = 1; // 

	private Font font = new Font("微软雅黑", Font.BOLD, 8); // font of node

	private Color gridColor = Color.BLACK;  //background color
	private Color linkLineColor = Color.BLACK; // line color
	private Color stringColor = Color.WHITE; // the color of text

	/**
	 * Set the alignment policy when you want to draw
	 *
	 * @param childAlign
	 *            
	 */
	public TreePanel(int childAlign) {
		this(null, childAlign);
	}


	public TreePanel(DrawNode n, int childAlign) {
		super();
		setTree(n, null);
		this.childAlign = childAlign;
	}

	/**
	 * 
	 *
	 */
	public void setTree(DrawNode n, HashMap<Integer, HashMap<String, DrawNode>> treeMap) {
		tree = n;
		this.treeMap = treeMap;
	}

	// draw 
	public void paintComponent(Graphics g) {
		// startX = (getWidth() - gridWidth) / 2;
		 startY = (getHeight() - gridHeight) / 2;
		super.paintComponent(g);
		g.setFont(font);
		mydrawAllNode(tree, startY, g);
	}
/**
 * draw result
 */
	public void mydrawAllNode(DrawNode n, int y, Graphics g) {

		int preSize = 0;
		ArrayList<DrawNode> preDrawNode = new ArrayList<>();
		ArrayList<DrawNode> names = null;
		for (Integer layer : treeMap.keySet()) {
//			System.out.println("layer:" + layer);
			HashMap<String, DrawNode> stringDrawNodeHashMap = treeMap.get(layer);
			names = new ArrayList<>();
			names.addAll(stringDrawNodeHashMap.values());

			int temp;
			for (int i = 0; i < names.size(); i++) {
				int x = layer * (vGap + gridWidth);
				int oldx = (layer - 1) * (vGap + gridWidth);
				temp = -names.size() / 2 * 30 + y + i * 60;
				int fontY = temp + gridHeight - 5;
				g.setColor(Color.lightGray);
				g.fillOval(x, temp, 20, 20);
				g.setColor(Color.BLACK);
				g.drawString(names.get(i).getName(), x + 7, fontY);
				g.drawString(names.get(i).getLocation().toString(), x - 5, fontY + 15);
				

				
				if (!preDrawNode.isEmpty()) {
//					System.out.println(preDrawNode.size());
					for (int k = 0; k < preDrawNode.size(); k++) {
						for (DrawNode node : preDrawNode.get(k).getAllChild().values()) {
//							System.out.print(node.getLocation());

							if (node.getLocationName().equals(names.get(i).getLocationName())) {
//								System.out.println(node.getName());
//								System.out.println(names.get(i).getName());
								g.drawLine(oldx + 20, (-preSize / 2 * 30 + y + k * 60) + 10, x, temp + 10); // Draw lines connecting nodes
							}
						}
//						System.out.println();
					}
				}

			}

			preDrawNode.clear();
			preDrawNode.addAll(treeMap.get(layer).values());
			preSize = names.size();

		}

	}

}
