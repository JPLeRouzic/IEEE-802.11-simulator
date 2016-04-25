package zzInfra.gui;

import java.awt.Color;
import java.awt.Graphics;
import zzInfra.kernel.JETime;

public class JE802Nav extends JE802Drawable {

	private final JETime dur;
	private final Color chameleon = new Color(78, 154, 6);

	public JE802Nav(JETime aStart, JETime aDur, Integer aSta, Integer anAC, int channel) {
		super(aStart, aSta, anAC, channel);
		this.dur = aDur;
	}

	@Override
	public void draw(Graphics g, JE802GuiContext context) {
		if (context.navs_are_wanted) {
			JETime aStart = aTime;
			double X = aStart.getTimeMs();
			X = X * context.thePixel_per_ms - context.thePanel_min_ms * context.thePixel_per_ms;
			double W = dur.getTimeMs();
			W = W * context.thePixel_per_ms;
			if (W < 1.0) {
				W = 1.0;
			}
			W = W + X;
			int xx = (int) X;
			int ww = (int) W;

			double height = Math.max(12, context.thePixel_per_Station / 4.0);

			int y_top = new Double(0.8 * height + (anAC - 1) * height + aSta
					* (context.thePixel_per_Station + context.thePixel_between_Stations)).intValue();
			int y_bottom = new Double(0.2 * height + (anAC - 1) * height + aSta
					* (context.thePixel_per_Station + context.thePixel_between_Stations)).intValue();

			g.setColor(this.chameleon);
			g.drawLine(xx, y_top, xx, y_bottom);
			g.drawLine(xx, y_top, ww, y_top + (y_bottom - y_top) / 2);
			g.drawLine(xx, y_bottom, ww, y_top + (y_bottom - y_top) / 2);
		}

	}
}
