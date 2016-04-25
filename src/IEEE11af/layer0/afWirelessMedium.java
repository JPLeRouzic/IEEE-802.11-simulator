package IEEE11af.layer0;

import zzInfra.layer0.WirelessChannel;
import java.util.List;

import zzInfra.kernel.JEEvent;
import zzInfra.layer1.JE802Phy;

public interface afWirelessMedium {

	/*
	 * (non-Javadoc)
	 * 
	 * @see kernel.JEEventHandler#event_handler(kernel.JEEvent)
	 */
	public void event_handler(JEEvent anEvent);

	public double getReuseDistance();

	public List<WirelessChannel> getAvailableChannels();

	public Integer getHandlerId();

	public double getRxPowerLevel_mW(JE802Phy JE802Phy);

	public double getBusyPowerLevel_mW();

	public double getSnirAtRx(int da, JE802Phy je802Phy);

}