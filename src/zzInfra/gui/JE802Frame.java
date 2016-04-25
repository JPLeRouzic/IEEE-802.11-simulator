package zzInfra.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import zzInfra.kernel.JETime;

public class JE802Frame extends JE802Drawable {

	private JETime dur;
	private int channel;
	private String label;
	private String secLabel;
	private String destLabel;

	public JE802Frame(JETime aStart, JETime aDur, int aChannel, int aSta, String aLabel, String a2ndLabel, String destLabel,
			int channel) {
		super(aStart, aSta, 0, channel);
		this.dur = aDur;
		this.channel = aChannel;
		this.label = aLabel;
		this.secLabel = a2ndLabel;
		this.destLabel = destLabel;
	}

	@Override
	public void draw(Graphics g, JE802GuiContext context) {
		if (context.frames_are_wanted | context.frame_lines_are_wanted) {
			Rectangle rect = new Rectangle();
			JETime aStart = this.aTime;
			double X = aStart.getTime();
			X = X * context.thePixel_per_ms - context.thePanel_min_ms * context.thePixel_per_ms;
			rect.x = (int) Math.round(X);
			rect.y = 2 + aSta * new Double(context.thePixel_per_Station + context.thePixel_between_Stations).intValue();
			rect.height = new Double(context.thePixel_per_Station).intValue() - 4;

			double W = dur.getTime();

			W = W * context.thePixel_per_ms;
			if (W < 1.0) {
				W = 1.0;
			}
			rect.width = (int) Math.round(W);
			if (context.frames_are_wanted) {
				g.setColor(JE802GuiTimePanel.channel2color(channel));
				if (context.only_fill_frames) {
					g.fillRect(rect.x, rect.y, rect.width, rect.height);
				} else {
					g.fill3DRect(rect.x, rect.y, rect.width, rect.height, true);
				}
			}
			if (context.frame_lines_are_wanted || !context.frames_are_wanted) {
				float[] hsb = Color.RGBtoHSB(130, 130, 130, null);
				g.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
				// g.drawRect(rect.x, rect.y, rect.width, rect.height);
				if (!context.only_fill_frames) {
					g.draw3DRect(rect.x, rect.y, rect.width, rect.height, true);
				}
			}
			if (context.thePixel_per_ms > context.thePixel_per_ms_LABELS) {
				g.setColor(Color.black);
				g.setFont(new Font("Arial", 1, g.getFont().getSize() - 4));
				g.drawString(label, rect.x + 2, rect.y - 2 + rect.height - 1);
				g.drawString(secLabel, rect.x + 2, rect.y - 2 + rect.height - 28);
				g.drawString(destLabel, rect.x + 2, rect.y - 2 + rect.height - 14);
				g.drawString(aStart.toString(), rect.x + 2, rect.y - 2 + rect.height - 42);
				g.drawString(aStart.plus(dur).toString(), rect.x + 4 + rect.width, rect.y - 2 + rect.height - 42);
				g.setFont(new Font("Verdana", 1, g.getFont().getSize() + 4));
			}
		}
	}

	public void setDur(JETime dur) {
		this.dur = dur;
	}

}
