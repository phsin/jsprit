package jsprit.analysis.toolbox;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 
 * @author schroeder
 *
 */
public class XYLineChartBuilder {
	
	/**
	 * Helper that just saves the chart as specified png-file. The width of the image is 1000 and height 600.
	 * 
	 * @param chart
	 * @param pngFilename
	 */
	public static void saveChartAsPNG(JFreeChart chart, String pngFilename){
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFilename), chart, 1000, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a new instance of the builder.
	 * 
	 * @param chartTitle appears on top of the XYLineChart
	 * @param xDomainName appears below the xAxis
	 * @param yDomainName appears beside the yAxis
	 * 
	 * @return the builder
	 */
	public static XYLineChartBuilder newInstance(String chartTitle, String xDomainName, String yDomainName){
		return new XYLineChartBuilder(chartTitle, xDomainName, yDomainName);
	}
	
	private ConcurrentHashMap<String,XYSeries> seriesMap = new ConcurrentHashMap<String, XYSeries>();
	
	private final String xDomain;
	
	private final String yDomain;
	
	private final String chartName;
	
	private XYLineChartBuilder(String chartName, String xDomainName, String yDomainName) {
		this.xDomain=xDomainName;
		this.yDomain=yDomainName;
		this.chartName=chartName;
	}
	
	/**
	 * Adds data to the according series (i.e. XYLine).
	 * 
	 * @param seriesName
	 * @param xVal
	 * @param yVal
	 */
	public void addData(String seriesName, double xVal, double yVal){
		if(!seriesMap.containsKey(seriesName)){
			seriesMap.put(seriesName, new XYSeries(seriesName,true,true));
		}
		seriesMap.get(seriesName).add(xVal, yVal);
	}
	
	/**
	 * Builds and returns JFreeChart.
	 * @return
	 */
	public JFreeChart build(){
		XYSeriesCollection collection = new XYSeriesCollection();
		for(XYSeries s : seriesMap.values()){
			collection.addSeries(s);
		}
		JFreeChart chart = ChartFactory.createXYLineChart(chartName, xDomain, yDomain, collection, PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		Range rangeBounds = collection.getRangeBounds(true);
		double upper = Math.min(rangeBounds.getUpperBound(), rangeBounds.getLowerBound()*5);
		if(upper == 0.0){ upper = 10000; }
		if(rangeBounds.getLowerBound() == upper){
			yAxis.setRangeWithMargins(rangeBounds.getLowerBound()-rangeBounds.getLowerBound()*.1,upper+upper*.1);
		}
		else{
			yAxis.setRangeWithMargins(rangeBounds.getLowerBound(),upper);
		}
		return chart;
	}

}
