/*
 * 
 * This is jemula.
 *
 *    Copyright (c) 2006-2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package zzInfra.plot;


import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A window containing multiple real-time plots each of which containing a single
 * data series.
 * 
 * This class is a helper class which allows the convenient application
 * of the JFreeChart library. However it is not part of the JFreeChart
 * library.
 * 
 * @author Laurent Zimmerli - laurentz@student.ethz.ch
 *
 */
@SuppressWarnings("serial")
public class JESynchroPlotter extends ApplicationFrame {

    /** The number of subplots. */
    private int SUBPLOT_COUNT;
    
    /** Current number of added sub plots. */
    private int plotCount = 0;
    
    /** The plot container. */
    private CombinedDomainXYPlot plot;
    
    /** The data sets containing the values of the series. */
    private XYSeriesCollection[] datasets;
    
    /** The title of the plot. */
    private String plotTitle;
    
    /** X-axis labels (time axis). */
    private String xAxisLabel;
    
    /** Duration of the plot areas. */
    private double duration;

    /**
     * Create a window which will be able to hold multiple plots in it each displaying a single data series.
     * 
     * @param plotTitle    Title of the plot window.
     * @param subPlotCount Maximum number of plots in the window.
     * @param xAxisLabel   Label of the time axis.
     * @param duration     Range of the time axis.
     */
    public JESynchroPlotter(String plotTitle, int subPlotCount, String xAxisLabel, double duration) {
        super(plotTitle);
        
        // Set the client's parameters:
        this.plotTitle = plotTitle;
        this.SUBPLOT_COUNT = subPlotCount;
        this.xAxisLabel = xAxisLabel;
        this.duration = duration;

        // Create the chart:
        JFreeChart chart = createChart();
        
        // Create the GUI:
        JPanel content = new JPanel(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(640, 480));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        setContentPane(content);
    }
    
    /**
     * Create the chart.
     * 
     * @return The chart.
     */
    private JFreeChart createChart() {
    	plot = new CombinedDomainXYPlot(new DateAxis(xAxisLabel));
        datasets = new XYSeriesCollection[SUBPLOT_COUNT];
        
        JFreeChart chart = new JFreeChart(plotTitle, plot);
        
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(duration);
        
        return chart;
    }
    
    /**
     * Add a new plot to the window with dynamic y-axis range.
     * 
     * @param seriesTitle Title of the data series of this plot.
     * @param yAxisLabel  Label of the y-axis.
     */
    public void addSubPlot(String seriesTitle, String yAxisLabel) {
    	if (plotCount < SUBPLOT_COUNT) {
	    	XYSeries series = new XYSeries(seriesTitle);
		    datasets[plotCount] = new XYSeriesCollection(series);
		    NumberAxis rangeAxis = new NumberAxis(yAxisLabel);
		    rangeAxis.setAutoRangeIncludesZero(false);
		    
		    XYPlot subplot = new XYPlot(datasets[plotCount], null, rangeAxis, new StandardXYItemRenderer());
		    plot.add(subplot);
		    
		    plotCount++;
    	}
    }
    
    /**
     * Add a new plot to the window with fixed y-axis range.
     * 
     * @param seriesTitle Title of the data series of this plot.
     * @param yAxisLabel  Label of the y-axis.
     * @param yMin        Minimum y value.
     * @param yMax        Maximum y value.
     */
    public void addSubPlot(String seriesTitle, String yAxisLabel, double yMin, double yMax) {
    	if (plotCount < SUBPLOT_COUNT) {
	    	XYSeries series = new XYSeries(seriesTitle);
		    datasets[plotCount] = new XYSeriesCollection(series);
		    NumberAxis rangeAxis = new NumberAxis(yAxisLabel);
		    rangeAxis.setAutoRangeIncludesZero(false);
		    rangeAxis.setRange(yMin, yMax);
		    
		    XYPlot subplot = new XYPlot(datasets[plotCount], null, rangeAxis, new StandardXYItemRenderer());
		    plot.add(subplot);
		    
		    plotCount++;
    	}
    }
    
    /**
     * Show the window.
     */
    public void display() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
    
    /**
     * Plot a given value at a given time in a specified plot.
     * 
     * @param time      Time at which the value should be plotted.
     * @param value     The value to be plotted.
     * @param plotIndex The plot to which the value should be plotted.
     */
    public void plot(double time, double value, int plotIndex) {
    	if (plotIndex < plotCount)
    		datasets[plotIndex].getSeries(0).add(time, value);
    }

}