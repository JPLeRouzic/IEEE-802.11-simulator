package IEEE11af.layer2;

/**
 * Represents a packet.
 *
 * @author Dimitris El. Vassis
 */
public class afMSDU {
    /**
     * The time that the packet was generated
     */
    public long generationTime;
    /**
     * The packet length in bits
     */
    public int length;
    /**
     * The ID of the packet. -1 if it is a dummy packet
     */
    public int id;
}
