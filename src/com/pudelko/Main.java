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
            String[] header = {"Test_UID", "Min Height", "Min Height Location",
                    "Max Height", "Max Height Location", "Mean Height","Height Range",
                    "Average Roughness", "Root Mean Square Roughness"};
            for(int i = 0; i < header.length; i++) {
                if(i < header.length-1) {
                    csvWriter.write(header[i] + ", ");
                } else {
                    csvWriter.write(header[i] + "\n");
                }
            }
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

                String[] data = calculateTest(list, i);
                for(int j = 0; j < data.length; j++) {
                    if(j < data.length -1) {
                        csvWriter.write(data[j] + ", ");
                    } else {
                        csvWriter.write(data[j] + "\n");
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
            String[] summary = {test_uid+"", null, null, null ,null ,null, null, null, null};
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


        String[] data = {test_uid+"", minHeight+"", minHeightLocation+"", maxHeight+"",
                maxHeightLocation+"", meanHeight+"", heightRange+"",
                averageRoughness+"", rootMeanSquareRoughness+""};
        return data;
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
