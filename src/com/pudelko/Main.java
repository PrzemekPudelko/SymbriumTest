package com.pudelko;

import com.pudelko.model.Datasource;
import com.pudelko.model.Measurement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Datasource datasource = new Datasource();
        if (!datasource.open()) {
            System.out.println("Can't open datasource");
            return;
        }

        List<Measurement> measurements = datasource.queryMeasurements();
        if (measurements == null) {
            System.out.println("No measurements");
            return;
        }

        Collections.sort(measurements);
        try {
            FileWriter csvWriter = new FileWriter("summary.csv");
            int count = 0;
            // measurements list is sorted by test_uid
            // so we can set the for loop limit to the test_uid of the last element
            // in the measurements list
            for (int i = 1; i <= measurements.get(measurements.size()-1).getTest_uid(); i++) {
                List<Measurement> list = new ArrayList<>();

                while (count != measurements.size() && measurements.get(count).getTest_uid() == i) {
                    list.add(measurements.get(count));
                    count++;
                }

                String[] summary = calculateTest(list, i);
                for(int j = 0; j < summary.length; j++) {
                    csvWriter.write(summary[j]);
                    j++;
                    if(j < summary.length) {
                        csvWriter.write(summary[j] + "\n");
                    }
                }
                csvWriter.write("\n");

            }
            csvWriter.close();

        } catch (IOException e) {
            System.out.println("Writing error: " + e.getMessage());
        }

        datasource.close();
    }

    public static String[] calculateTest(List<Measurement> measurements, int test_uid) {
        if(measurements == null || measurements.size()==0) {
            System.out.println("No measurements for Test " + test_uid);
            String[] summary = {"No Measurements for Test "+ test_uid+ "\n"};
            return summary;
        }
        double minHeight = Double.MAX_EXPONENT;
        int minHeightLocation = 0;
        double maxHeight = Double.MIN_EXPONENT;
        int maxHeightLocation = 0;
        double meanHeight = 0.0;
        double heightRange = 0.0;

        List<Double> heightList = new ArrayList<>();

        for(Measurement measurement: measurements) {
            double currentHeight = measurement.getHeight();
            heightList.add(currentHeight);
            meanHeight += currentHeight;

            if(currentHeight > maxHeight) {
                maxHeight = currentHeight;
                maxHeightLocation = measurement.getMeasurement_uid();
            }
            if(currentHeight < minHeight) {
                minHeight = currentHeight;
                minHeightLocation = measurement.getMeasurement_uid();
            }
        }

        meanHeight /= measurements.size();
        heightRange = maxHeight - minHeight;
        double averageRoughness = calculateAverageRoughness(heightList, meanHeight);
        double rootMeanSquareRoughness = calculateRootMeanSquareRoughness(heightList, meanHeight);


        String[] summary = {"Test: ", test_uid+"", "Min Height: ", minHeight+"", "Min Height Location: ",
                minHeightLocation+"", "Max Height: ", maxHeight+"", "Max Height Location: ", maxHeightLocation+"",
                "Mean Height: ", meanHeight+"","Height Range: ", heightRange+"", "Average Roughness: ",
                averageRoughness+"", "Root Mean Square Roughness: ", rootMeanSquareRoughness+""};
        return summary;
    }

    public static double calculateAverageRoughness(List<Double> list, double meanElevation) {
        if(list == null) {
            System.out.println("No measurements");
            return 0.0;
        }
        double sum = 0.0;
        for(Double d: list) {
            sum += Math.abs(d - meanElevation);
        }
        return sum/list.size();

    }

    public static double calculateRootMeanSquareRoughness(List<Double> list, double meanElevation) {
        if (list == null) {
            System.out.println("No measurements");
            return 0.0;
        }
        double sum = 0.0;
        for(Double d: list) {
             double result = d - meanElevation;
             sum += (result*result);
        }
        return Math.sqrt(sum/list.size());
    }
}
