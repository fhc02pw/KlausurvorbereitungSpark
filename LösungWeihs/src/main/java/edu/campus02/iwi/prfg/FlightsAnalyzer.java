package edu.campus02.iwi.prfg;

import static org.apache.spark.sql.functions.col;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.graphframes.GraphFrame;

import spark.exercise.env.WinConfig;

public class FlightsAnalyzer {

	public static void main(String[] args) {

		WinConfig.setupEnv();

		SparkConf cnf = new SparkConf().setMaster("local[2]")
				.set("spark.executor.memory", "2g")
				.set("spark.driver.memory", "2g")
				.setAppName(FlightsAnalyzer.class.getName());

		SparkSession spark = SparkSession.builder()
				.config(cnf)
				.getOrCreate();

		//a) airports DF
		Dataset<Row> airports = spark.read()
				.option("header",true)
				.option("delimiter",",")
				.option("nullValue", "\\N")
				.option("inferSchema",true)
				.csv("data/input/airports.dat")
				.cache();
		airports.printSchema();

		//b) airlines DF
		//TODO 1a: read in airlines DF
		Dataset<Row> airlines = spark.read()
				.option("header",true)
				.option("delimiter",",")
				.option("nullValue", "\\N")
				.option("inferSchema",true)
				.csv("data/input/airlines.dat")
				.cache();
		airlines.printSchema();
		
		//c) routes DF
		//TODO 1b: read in routes DF
		Dataset<Row> routes = spark.read()
				.option("header",true)
				.option("delimiter",",")
				.option("nullValue", "\\N")
				.option("inferSchema",true)
				.csv("data/input/routes.dat")
				.cache();
		routes.printSchema();
		
		
		//TODO 2: create GraphFrame based on airports and routes
		GraphFrame gf = new GraphFrame(airports.selectExpr("AirportID as id", "City", "Country"), 
				routes.selectExpr("SourceAirportID as src", "DestinationAirportID as dst", "AirlineID")); 
		
		//TODO 3:
		//all airports in "United Kingdom" or "Australia"
		//with at most 100 outgoing flight connections
		Dataset<Row> outDegrees = gf.outDegrees().filter("outDegree <= 100"); 
		Dataset<Row> airportsWithOutDegrees = outDegrees.join(airports, col("id").equalTo(col("AirportID"))); 
		Dataset<Row> filteredByCountry = airportsWithOutDegrees.filter("Country IN('United Kingdom', 'Australia')"); 
		Dataset<Row> selectedColumns = filteredByCountry.select(col("Name"), col("City"), col("outDegree").alias("Anzahl")); 
		selectedColumns.orderBy(col("Anzahl").desc()).show(); 
		
		//TODO 4:
		//all direct flights starting from Norway to Italy
		Dataset<Row> result = gf.bfs().fromExpr("Country='Norway'").toExpr("Country='Italy'").maxPathLength(1).run();
		Dataset<Row> result2 = result.join(airlines, col("e0.AirlineID").equalTo(col("AirlineID"))); 
		result2.printSchema();
		result2.select(col("from.City").alias("From"), col("to.City").alias("To"), col("Name").alias("Airline")).show(false); 
		 
		
	}

}
