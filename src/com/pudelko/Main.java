package com.pudelko;

import com.pudelko.model.Datasource;
import com.pudelko.model.Measurement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        Datasource datasource = new Datasource();
        if (!datasource.open(scanner)) {
            System.out.println("Can't open datasource");
            return;
        }

        List<Measurement> measurements = datasource.queryMeasurements();
        if (measurements == null) {
            System.out.println("No measurements");
            return;
        }

        process(measurements);
        scanner.close();
        datasource.close();
    }

    // Method to process the Measurements as specified in the directions
    public static void process(List<Measurement> measurements) {
        Collections.sort(measurements);
        try {
            FileWriter csvWriter = new FileWriter("summary.csv");
            String[] header = {"Test_UID", "Min Height", "Min Height Location",
                    "Max Height", "Max Height Location", "Mean Height","Height Range",
                    "Average Roughness", "Root Mean Square Roughness (Standard Deviation)",
                    "Measurements Inside Filter", "Measurements Outside Filter"};
            csvWrite(csvWriter, header);

            System.out.println("How many standard deviations to use as a filter? (Default is 3)");
            String input = scanner.next();
            int numFilter = 3;
            try {
                numFilter = Integer.parseInt(input);
            } catch (NumberFormatException e){
                System.out.println("Error: Integer not inputted.");
            } finally {
                System.out.println("Filtering to " + numFilter+ " standard deviations.");
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

                String[] data = calculateTest(list, i, numFilter);
                csvWrite(csvWriter, data);
                csvWriter.write("\n");
            }
            csvWriter.close();

        } catch (IOException e) {
            System.out.println("Writing error: " + e.getMessage());
        }
    }

    // Method to write the array of Strings to the .csv file
    public static void csvWrite(FileWriter csvWriter, String[] data) {
        try {
            for(int j = 0; j < data.length; j++) {
                if(j < data.length -1) {
                    csvWriter.write(data[j] + ", ");
                } else {
                    csvWriter.write(data[j] + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Writing error: " + e.getMessage());
        }
    }

    // Method where all the calculations actually occur
    public static String[] calculateTest(List<Measurement> measurements, int test_uid, int numFilter) {
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

        int countInsideFilter = 0;
        int countOutsideFilter = 0;
        double bottomFilter = meanHeight - (numFilter*rootMeanSquareRoughness);
        double topFilter = meanHeight + (numFilter*rootMeanSquareRoughness);
        for(Measurement measurement: measurements) {
            double currentHeight = measurement.getHeight();
            if((currentHeight < meanHeight && currentHeight < bottomFilter) ||
                    (currentHeight > meanHeight && currentHeight > topFilter)) {
                countOutsideFilter++;
            } else {
                countInsideFilter++;
            }
        }

        String[] data = {test_uid+"", minHeight+"", minHeightLocation+"", maxHeight+"",
                maxHeightLocation+"", meanHeight+"", heightRange+"",
                averageRoughness+"", rootMeanSquareRoughness+"", countInsideFilter+"", countOutsideFilter+""};
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
