package zzInfra.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import zzInfra.kernel.JETime;

public class JE802Backoff extends JE802Drawable {

	private JETime dur;
	private String window;
	private int cw;
	private JETime cwTime;
	private Color blue;

	public JE802Backoff(JETime aStart, JETime aDur, Integer aSta, Integer anAC, String aWindow, Integer aCW, JETime aCWTime,
			int channel) {
		super(aStart, aSta, anAC, channel);
		this.dur = aDur;
		this.window = aWindow;
		this.cw = aCW;
		this.cwTime = aCWTime;
		this.blue = new Color(114, 159, 206);
	}

	@Override
	public void draw(Graphics g, JE802GuiContext context) {
		if (context.backoffs_are_wanted) {
			JETime aStart = this.aTime;
			Rectangle rect = new Rectangle();
			double X = aStart.getTimeMs();
			X = X * context.thePixel_per_ms - context.thePanel_min_ms * context.thePixel_per_ms;
			rect.x = (int) Math.round(X);

			double height = Math.max(12, context.thePixel_per_Station / 4.0);

			int y_top = new Double(0.8 * height + (anAC - 1) * height + aSta
					* (context.thePixel_per_Station + context.thePixel_between_Stations)).intValue();
			int y_bottom = new Double(0.2 * height + (anAC - 1) * height + aSta
					* (context.thePixel_per_Station + context.thePixel_between_Stations)).intValue();

			rect.height = y_top - y_bottom;
			rect.y = y_bottom;

			double W = dur.getTimeMs();
			double CW = cwTime.getTimeMs();

			W = W * context.thePixel_per_ms;
			if (W < 1.0) {
				W = 1.0;
			}

			CW = CW * context.thePixel_per_ms;
			if (CW < 1.0) {
				CW = 1.0;
			}
			rect.width = (int) Math.round(CW);
			g.setColor(blue);
			if (rect.width > 10) {
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
			}

			rect.width = (int) Math.round(W);
			g.setColor(Color.white);
			if (rect.width > 10) {
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
			}

			if (context.thePixel_per_ms > context.thePixel_per_ms_LABELS) {
				int aXPos = new Double(Math.round(X)).intValue() - 6;
				int aYPos = rect.y - 3;
				g.setColor(Color.black);
				g.setFont(new Font("Arial", 1, g.getFont().getSize() - 4));
				g.drawString(window + " (" + cw + ") " + aStart.toString(), aXPos, aYPos);
				g.setFont(new Font("Verdana", 1, g.getFont().getSize() + 4));
			}
		}

	}
        
}
