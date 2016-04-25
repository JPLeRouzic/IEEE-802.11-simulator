/*
It is possible to dynamically change the size of the channels. 
Improvements in coexistence mechanisms exist to allow greater channel width in contexts mixing 802.11 a/n and 802.11ac
802.11ac essentially changes the 802.11n MAC layer management to address coexistence issues induced by the use of wider channels. In addition minor changes are made in the  802.11n mechanism of aggregation to improve efficiency.
With 802.11n many channels at 20 and 40 MHz in the 5 GHz band, the risk of channels overlap between two BSS, can be easily avoided by choosing a different channel for each BSS. In the worst case, if an overlap is inevitable on the 40 MHz channel within two neighboring BSS, the same 20 MHz primary channel is chosen  to allow a maximum of coexistence. With a much larger channel size in 802.11ac, it becomes much more difficult to avoid overlap between neighboring BSS. It also becomes more difficult to choose a primary channel common between overlapping BSS. 
To resolve this issue, 802.11ac brings three improvements: 
·	better detection of occupation of the secondary channel with "Clear Channel Assessment (CCA), 
·	improving the management of dynamic width of the channel, 
·	a new frame of notification of the operation mode.
*/
/*
 * 
 */
package IEEE11ax.layer1;

/**
 * @author jean-Pierre Le Rouzic
 */
public class axChan_adapt {

}
 
