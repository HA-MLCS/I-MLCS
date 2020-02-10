package erlp.mlcs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubGraph implements Externalizable {
    public short minLevel;
    public short maxLevel;
    public int index;
    public Map<Location, InNode> location2Nodes;
    public Set<Location> preLocations;

    public SubGraph() {
    }

    public SubGraph(int index, short minLevel, short maxLevel, Map<Location, InNode> location2Nodes) {
        super();
        this.index = index;
        this.minLevel = (minLevel == 0) ? 1 : minLevel;
        this.maxLevel = maxLevel;
        this.location2Nodes = location2Nodes;
    }


    public SubGraph(int index, short minLevel, short maxLevel, Map<Location, InNode> location2Nodes, Set<Location> preLocations) {
        super();
        this.index = index;
        this.minLevel = (minLevel == 0) ? 1 : minLevel;
        this.maxLevel = maxLevel;
        this.location2Nodes = location2Nodes;
        this.preLocations = preLocations;
    }

    /**
     * Serialize subgraphs into files
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        int i = 0;
        Map<Location, Integer> tempLtoN = new HashMap<Location, Integer>();
        Iterator<Location> it = location2Nodes.keySet().iterator();
        short locLen = (short) it.next().index.length;
        for (Map.Entry<Location, InNode> l : location2Nodes.entrySet())
            tempLtoN.put(l.getKey(), i++);

        for (Location l : preLocations) {
            tempLtoN.put(l, i++);
        }

        out.writeShort(minLevel); //
        out.writeShort(maxLevel);//
        out.writeShort(index); //
        out.writeInt(location2Nodes.size());
        out.writeInt(preLocations.size());
        out.writeShort(locLen);
        for (Map.Entry<Location, InNode> l : location2Nodes.entrySet()) {
            out.writeInt(tempLtoN.get(l.getKey()));
            for (Short l1 : l.getKey().index)
                out.writeShort(l1);
        }
        for (Location l : preLocations) {
            out.writeInt(tempLtoN.get(l));
            for (Short l1 : l.index)
                out.writeShort(l1);
        }

        int j = 0;
        for (Map.Entry<Location, InNode> l2n : location2Nodes.entrySet()) {

//			System.out.println(j++);
            InNode in = l2n.getValue();
            out.writeInt(tempLtoN.get(in.loc));
            out.writeShort(in.indegree);
            out.writeShort(in.level);
            out.writeShort(in.predecessors.size());
            for (Location pre : in.predecessors)
                out.writeInt(tempLtoN.get(pre));

        }
    }

    /**
     * deserialize subgraphs from files
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<Location, InNode> loc2Node = new HashMap<Location, InNode>();
        Map<Integer, Location> tempLtoN = new HashMap<Integer, Location>();
        this.preLocations = new HashSet<>();

        this.minLevel = in.readShort();
        this.maxLevel = in.readShort();
        this.index = in.readShort();
        int size = in.readInt(); // locationToNode的size
        int size2 = in.readInt(); //preLocations的size
        short locLen = in.readShort();
        for (int i = 0; i < size; i++) {
            int k = in.readInt();
            short[] index = new short[locLen];
            for (int j = 0; j < locLen; j++)
                index[j] = in.readShort();
            Location loc = new Location(index);
            tempLtoN.put(k, loc);
        }
        for (int i = 0; i < size2; i++) {
            int k = in.readInt();
            short[] index = new short[locLen];
            for (int j = 0; j < locLen; j++)
                index[j] = in.readShort();
            Location loc = new Location(index);
            tempLtoN.put(k, loc);

            this.preLocations.add(loc);
        }


        for (int i = 0; i < size; i++) {
            InNode inNode = new InNode();
            List<Location> pres = new ArrayList<Location>();
            int locIndex = in.readInt();
            inNode.loc = tempLtoN.get(locIndex);
            inNode.indegree = in.readShort();
            inNode.level = in.readShort();
//			System.out.println(inNode.loc + ":" + inNode.level);
            short preSize = in.readShort();
            for (int j = 0; j < preSize; j++) {
//				if(tempLtoN.get(in.readInt()) == null){
//					System.out.println("ssss");
//				}
//				in.readInt();


                pres.add(tempLtoN.get(in.readInt()));
            }

            inNode.predecessors = pres;
            loc2Node.put(inNode.loc, inNode);
        }
        this.location2Nodes = loc2Node;
    }

    public String toString() {
        return "subgraph" + index + "[level " + minLevel + "~" + maxLevel + "] nodes:" + location2Nodes.size();
    }
}
