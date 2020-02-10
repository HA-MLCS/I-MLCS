package arlp.mlcs.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arlp.mlcs.Location;


public class DrawNode implements Serializable {
    private Location location;
    private int layer = 0;	//level of node

    private int id;

    private String name;

    private String nextName;

    private HashMap<String, DrawNode> mapChilds;   //save children of this node

    private List<DrawNode> childs;	

    @Override
    public boolean equals(Object obj) {
    	DrawNode node = (DrawNode)obj;
    	return this.location.equals(node.location);
    }
    
    
    public DrawNode(int id, String name, Location location){
        this.name = name;
        this.id = id;
        this.location = location;
        this.mapChilds = new HashMap<>();
    }

    /**
     * add a child
     */
    public void add(DrawNode n){
        if(childs == null)
            childs = new ArrayList<DrawNode>();
        n.setLayer(layer+1);
        setChildLayout(n);
        childs.add(n);
    }

    /**
     * set level of child
     * 
     */
    private void setChildLayout(DrawNode n){
        if(n.hasChild()){
            List<DrawNode> c = n.getChilds();
            for(DrawNode drawNode : c){
                drawNode.setLayer(drawNode.getLayer()+1);
                setChildLayout(drawNode);
            }
        }
    }

    /**
     * get name of node
     */
    public String getName() {
        return name;
    }

    public String getLocationName(){
    	return location.toString();
    }
    /**
     * set name of node
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getNextName() {
        return nextName;
    }

    public void setNextName(String nextName) {
        this.nextName = nextName;
    }

    

    public int getLayer() {
        return layer;
    }


    public void setLayer(int layer) {
        this.layer = layer;
    }


    public List<DrawNode> getChilds() {
    	List<DrawNode> children = new ArrayList<>();
    	for(Map.Entry<String, DrawNode> entry:mapChilds.entrySet()){
    		children.add(entry.getValue());
    	}
        return children;
    }


//    public boolean hasChild(){
//        return childs == null ? false : true;
//    }
    public boolean hasChild(){
    	return !mapChilds.isEmpty();
    }

    /**
     * 
     * print all node
     */
    public void printAllNode(DrawNode n){
        System.out.println(n);
        if(n.hasChild()){
            List<DrawNode> c = n.getChilds();
            for(DrawNode drawNode : c){
                printAllNode(drawNode);
            }
        }
    }

    public String getAllNodeName(DrawNode n){
        String s = n.toString()+"/n";
        if(n.hasChild()){
            List<DrawNode> c = n.getChilds();
            for(DrawNode drawNode : c){
                s+=getAllNodeName(drawNode)+"/n";
            }
        }
        return s;
    }

    public boolean haschild(String name) {
        return mapChilds.get(name) != null;
    }

    public DrawNode getChild(String name) {
        return mapChilds.get(name);
    }

    public HashMap<String, DrawNode> getAllChild() {
        return mapChilds;
    }

    public DrawNode addchild(DrawNode drawNode) {
        return mapChilds.put(drawNode.getName(), drawNode);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String toString(){
        return name + "\n\n" + location;
    }
}






