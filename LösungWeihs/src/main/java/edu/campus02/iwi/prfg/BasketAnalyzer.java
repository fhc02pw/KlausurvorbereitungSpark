package edu.campus02.iwi.prfg;

import static org.apache.spark.sql.functions.*;

import org.apache.spark.SparkConf;
import org.apache.spark.network.protocol.Encoders;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import spark.exercise.env.WinConfig;

public class BasketAnalyzer {

	public static void main(String[] args) {

		WinConfig.setupEnv();
		
		//Step1: Create SparkConf and session
		SparkConf conf = new SparkConf().setMaster("local[1]").setAppName(BasketAnalyzer.class.getName()); 
		SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
		
		//Step2: Lesen Sie die Datei basketinfos.json in ein Dataset<Row> ein
		String filePath = "data/input/basketinfos.json"; 
		Dataset<Row> lines = spark.read().json(filePath).cache();
		
		//Step3: Registrieren Sie ein View mit dem namen baskets
		lines.createOrReplaceTempView("baskets");
		
		//Step4: Erzeugen Sie ein neues Dataframe. Filtern wenn NICHT mit MasterCard bezahlt wurde, Kategorien Toys oder Music und 150-300 Bestellwert
		//SQL und nicht DSL verwenden
		String queryStep4 = "SELECT * FROM baskets b WHERE b.paymentType != 'MasterCard' AND b.productCategory IN ('Toys','Music') AND b.orderTotal BETWEEN 150 AND 300"; 
		Dataset<Row> step4 = spark.sql(queryStep4);
		step4.show(); 
		
		
		//Step5: Filtern....Chicaco, Memphis oder Baltimore, Amex bezahlt und Wert < 200
		//SQL und nicht DSL verwenden
		String queryStep5 = "SELECT * FROM baskets b WHERE b.buyingLocation IN ('Chicaco', 'Memphis', 'Baltimore') AND b.paymentType = 'Amex' AND b.orderTotal < 200"; 
		Dataset<Row> step5 = spark.sql(queryStep5);
		step5.show();
		
		//Step6: Berechnen Anzahl an Baskets pro Bezahlungsmittel und Ergebnis aufsteigend sortieren. 
		//Kein Ergebnis weniger als 10000 Baskets (12500 gibts nicht)
		//DSL verwenden und nicht SQL
		RelationalGroupedDataset step6Group = lines.groupBy("paymentType"); 
		Dataset<Row> step6 = step6Group.count().filter("count > 10000").orderBy("count"); 
		step6.show(); 
		
		
		//Step7: Pro Kategorie soll min, max und durchschnittlicher Bestellwert berechnet werden. Sortierung absteigend nach durchschnitt
		//SQL oder DSL
		RelationalGroupedDataset step7Group = lines.groupBy("productCategory");
		Dataset<Row> step7 = step7Group.agg(min("orderTotal"), max("orderTotal"), avg("orderTotal")).orderBy(col("avg(orderTotal)").desc());
		step7.show(); 
	}
}