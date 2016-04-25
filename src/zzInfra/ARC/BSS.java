package zzInfra.ARC;

import javax.swing.*;

// import java.io.File;
// import java.net.URL;

/* the class BSS manages a group of STA_Traffic for a simulation
its role is not so clear... It should only involve adding, removing, modifying node allocation for a given simulation 
*/
public class BSS extends JFrame {

//    public Vector<STA_Traffic> nodesList;

    // public
    public int nmbrOfNodes;    //The number of nodes (read from the xml file).

    // private


    public BSS() {
        nmbrOfNodes = 0;
    }

    /**
     * Finds a node whose Id is 'nodeId', or null if the node with the specified ID does not exist.
     *
     * @param nodeId The ID of the node.
     * @return The node whose ID is 'nodeId'
     * @throws ElementDoesNotExistException
     */
    
    public void getNode()  {
//dummy
    }


    /**
     * Adds a new node to the system.
     * Pay attention: The user is responsible for assigning correct node IDs. If a duplicate ID exists in the system
     * the simulator is not responsible of identifying it, and unexpected results will occur.
     *
     */
    public void addNode( )
             {
    // dummy
        nmbrOfNodes++;
    }

    /**
     * Removes a node with a specific ID
     *
     * @param nodeId The ID of the node to be removed.
     * @throws ElementDoesNotExistException
     */
    public void removeNode(int nodeId)  {
    // dummy
        nmbrOfNodes--;

    }

    /**
     * Removes all nodes from the system
     */
    public void removeAllNodes() {
    // dummy
        nmbrOfNodes = 0;
    }

}
