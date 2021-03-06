/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.analysis.toolbox;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Locations;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;



/**
 * A plotter to plot vehicle-routing-solution and routes respectively.
 * 
 * @author stefan schroeder
 *
 */

@Deprecated
public class SolutionPlotter {
	
	private static class NoLocationFoundException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	private static Logger log = Logger.getLogger(SolutionPlotter.class);
	

	/**
	 * Plots the {@link VehicleRoutingProblem} to png-file.

	 * @param vrp
	 * @param pngFile target path with filename.
	 * @see VehicleRoutingProblem, VehicleRoutingProblemSolution
	 * @deprecated use Plotter.java instead (this plotter is not maintained anymore and might plot incorrectly)
	 */
	@Deprecated
	public static void plotVrpAsPNG(VehicleRoutingProblem vrp, String pngFile, String title){
		String filename = pngFile;
		if(!pngFile.endsWith(".png")) filename += ".png";
		log.info("plot routes to " + filename);
		XYSeriesCollection problem;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,filename);
	}

	/**
	 * Retrieves the problem from routes, and plots it along with the routes to pngFile.
	 * 
	 * @param routes
	 * @param locations indicating the locations for the tour-activities.
	 * @param pngFile target path with filename.
	 * @param plotTitle 
	 * @see VehicleRoute
	 * @deprecated use Plotter.java instead (this plotter is not maintained anymore and might plot incorrectly)
	 */
	@Deprecated
	public static void plotRoutesAsPNG(Collection<VehicleRoute> routes, Locations locations, String pngFile, String title) {
		String filename = pngFile;
		if(!pngFile.endsWith(".png")) filename += ".png";
		log.info("plot routes to " + filename);
		XYSeriesCollection problem;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(routes, labels);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYSeriesCollection solutionColl = makeSolutionSeries(routes,locations);
		XYPlot plot = createPlot(problem, solutionColl, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,filename);
	}
	
	/**
	 * Plots problem and solution to pngFile.
	 * 
	 * <p>This can only plot if vehicles and jobs have locationIds and coordinates (@see Coordinate). Otherwise a warning message is logged
	 * and method returns but does not plot.
	 * 
	 * @param vrp
	 * @param solution
	 * @param pngFile target path with filename.
	 * @see VehicleRoutingProblem, VehicleRoutingProblemSolution
	 * @deprecated use Plotter.java instead (this plotter is not maintained anymore and might plot incorrectly)
	 */
	@Deprecated
	public static void plotSolutionAsPNG(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, String pngFile, String title){
		String filename = pngFile;
		if(!pngFile.endsWith(".png")) filename += ".png";
		log.info("plot solution to " + filename);
		XYSeriesCollection problem;
		XYSeriesCollection solutionColl;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
			solutionColl = makeSolutionSeries(vrp, solution);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem, solutionColl, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,filename);
		
	}
	
	

	private static XYPlot createPlot(final XYSeriesCollection problem, final Map<XYDataItem, String> labels) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
//		problemRenderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {
//			
//			@Override
//			public String generateLabel(XYDataset arg0, int arg1, int arg2) {
//				XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
//				return labels.get(item);
//			}
//		});
		problemRenderer.setBaseItemLabelsVisible(true);
		problemRenderer.setBaseItemLabelPaint(Color.BLACK);
		
		NumberAxis xAxis = new NumberAxis();		
		xAxis.setRangeWithMargins(problem.getDomainBounds(true));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(problem.getRangeBounds(false));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		return plot;
	}

	private static XYPlot createPlot(final XYSeriesCollection problem, XYSeriesCollection solutionColl, final Map<XYDataItem, String> labels) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
//		problemRenderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {
//			
//			@Override
//			public String generateLabel(XYDataset arg0, int arg1, int arg2) {
//				XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
//				return labels.get(item);
//			}
//		});
		problemRenderer.setBaseItemLabelsVisible(true);
		problemRenderer.setBaseItemLabelPaint(Color.BLACK);

		
		NumberAxis xAxis = new NumberAxis();		
		xAxis.setRangeWithMargins(problem.getDomainBounds(true));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(problem.getRangeBounds(true));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		
		XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
//		for(int i=0;i<solutionColl.getSeriesCount();i++){
//			XYSeries s = solutionColl.getSeries(i);
//			XYDataItem firstCustomer = s.getDataItem(1);
//			solutionRenderer.addAnnotation(new XYShapeAnnotation( new Ellipse2D.Double(firstCustomer.getXValue()-0.7, firstCustomer.getYValue()-0.7, 1.5, 1.5), new BasicStroke(1.0f), Color.RED));
//		}
		plot.setDataset(1, solutionColl);
		plot.setRenderer(1, solutionRenderer);
		plot.setDomainAxis(1, xAxis);
		plot.setRangeAxis(1, yAxis);
		
		return plot;
	}

	private static void save(JFreeChart chart, String pngFile) {
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
		} catch (IOException e) {
			log.error("cannot plot");
			log.error(e);
			e.printStackTrace();	
		}
	}

	private static XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) throws NoLocationFoundException{
		Locations locations = retrieveLocations(vrp);
		XYSeriesCollection coll = new XYSeriesCollection();
		int counter = 1;
		for(VehicleRoute route : solution.getRoutes()){
			if(route.isEmpty()) continue;
			XYSeries series = new XYSeries(counter, false, true);
			
			Coordinate startCoord = locations.getCoord(route.getStart().getLocationId());
			series.add(startCoord.getX(), startCoord.getY());
			
			for(TourActivity act : route.getTourActivities().getActivities()){
				Coordinate coord = locations.getCoord(act.getLocationId());
				series.add(coord.getX(), coord.getY());
			}
			
			Coordinate endCoord = locations.getCoord(route.getEnd().getLocationId());
			series.add(endCoord.getX(), endCoord.getY());
			
			coll.addSeries(series);
			counter++;
		}
		return coll;
	}
	
	private static XYSeriesCollection makeSolutionSeries(Collection<VehicleRoute> routes, Locations locations){
		XYSeriesCollection coll = new XYSeriesCollection();
		int counter = 1;
		for(VehicleRoute route : routes){
			if(route.isEmpty()) continue;
			XYSeries series = new XYSeries(counter, false, true);
			
			Coordinate startCoord = locations.getCoord(route.getStart().getLocationId());
			series.add(startCoord.getX(), startCoord.getY());
			
			for(TourActivity act : route.getTourActivities().getActivities()){
				Coordinate coord = locations.getCoord(act.getLocationId());
				series.add(coord.getX(), coord.getY());
			}
			
			Coordinate endCoord = locations.getCoord(route.getEnd().getLocationId());
			series.add(endCoord.getX(), endCoord.getY());
			
			coll.addSeries(series);
			counter++;
		}
		return coll;
	}
	
	private static XYSeriesCollection makeVrpSeries(Collection<Vehicle> vehicles, Collection<Job> services, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		XYSeriesCollection coll = new XYSeriesCollection();
		XYSeries vehicleSeries = new XYSeries("depot", false, true);
		for(Vehicle v : vehicles){
			Coordinate coord = v.getStartLocationCoordinate();
			if(coord == null) throw new NoLocationFoundException();
			vehicleSeries.add(coord.getX(),coord.getY());	
		}
		coll.addSeries(vehicleSeries);
		
		XYSeries serviceSeries = new XYSeries("service", false, true);
		XYSeries pickupSeries = new XYSeries("pickup", false, true);
		XYSeries deliverySeries = new XYSeries("delivery", false, true);
		for(Job job : services){
			if(job instanceof Pickup){
				Pickup service = (Pickup)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				pickupSeries.add(dataItem);
				labels.put(dataItem, String.valueOf(service.getCapacityDemand()));
			}
			else if(job instanceof Delivery){
				Delivery service = (Delivery)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				deliverySeries.add(dataItem);
				labels.put(dataItem, String.valueOf(service.getCapacityDemand()));
			}
			else if(job instanceof Service){
				Service service = (Service)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				serviceSeries.add(dataItem);
				labels.put(dataItem, String.valueOf(service.getCapacityDemand()));
			}
			else{
				throw new IllegalStateException("job instanceof " + job.getClass().toString() + ". this is not supported.");
			}
			
		}
		if(!serviceSeries.isEmpty()) coll.addSeries(serviceSeries);
		if(!pickupSeries.isEmpty()) coll.addSeries(pickupSeries);
		if(!deliverySeries.isEmpty()) coll.addSeries(deliverySeries);
		return coll;
	}
	
	private static XYSeriesCollection makeVrpSeries(Collection<VehicleRoute> routes, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		Set<Vehicle> vehicles = new HashSet<Vehicle>();
		Set<Job> jobs = new HashSet<Job>();
		for(VehicleRoute route : routes){
			vehicles.add(route.getVehicle());
			jobs.addAll(route.getTourActivities().getJobs());
		}
		return makeVrpSeries(vehicles, jobs, labels);
	}
	
	private static XYSeriesCollection makeVrpSeries(VehicleRoutingProblem vrp, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		return makeVrpSeries(vrp.getVehicles(), vrp.getJobs().values(), labels);
	}
	
	private static Locations retrieveLocations(VehicleRoutingProblem vrp) throws NoLocationFoundException {
		final Map<String, Coordinate> locs = new HashMap<String, Coordinate>();
		for(Vehicle v : vrp.getVehicles()){
			String startLocationId = v.getStartLocationId();
			if(startLocationId == null) throw new NoLocationFoundException();
			Coordinate coord = v.getStartLocationCoordinate();
			if(coord == null) throw new NoLocationFoundException();
			locs.put(startLocationId, coord);
		}
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Service){
				String locationId = ((Service) j).getLocationId();
				if(locationId == null) throw new NoLocationFoundException();
				Coordinate coord = ((Service) j).getCoord();
				if(coord == null) throw new NoLocationFoundException();
				locs.put(locationId, coord);
			}
			else{
				throw new IllegalStateException("job is not a service. this is not supported yet.");
			}
		}
		return new Locations() {
			
			@Override
			public Coordinate getCoord(String id) {
				return locs.get(id);
			}
		};
	}
	
}
