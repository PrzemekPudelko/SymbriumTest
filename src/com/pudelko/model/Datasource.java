package com.pudelko.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Datasource {

    public static final String CONNECTION_STRING = "jdbc:sqlite:";
    public static final String TABLE_MEASUREMENTS = "Measurements";
    public static final String COLUMN_MEASUREMENT_UID = "measurement_uid";
    public static final String COLUMN_TEST_UID = "test_uid";
    public static final String COLUMN_X = "x";
    public static final String COLUMN_Y = "y";
    public static final String COLUMN_HEIGHT = "height";


    private Connection conn;

    public boolean open() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter path to the database: ");
            String path = scanner.next();
            conn = DriverManager.getConnection(CONNECTION_STRING + path);
            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if(conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    public List<Measurement> queryMeasurements() {
        Statement statement = null;
        ResultSet results = null;

        try {
            statement = conn.createStatement();
            results = statement.executeQuery("SELECT * FROM " + TABLE_MEASUREMENTS);

            List<Measurement> measurements = new ArrayList<>();
            while (results.next()) {
                Measurement measurement = new Measurement();
                measurement.setMeasurement_uid(results.getInt(COLUMN_MEASUREMENT_UID));
                measurement.setTest_uid(results.getInt(COLUMN_TEST_UID));
                measurement.setX(results.getDouble(COLUMN_X));
                measurement.setY(results.getDouble(COLUMN_Y));
                measurement.setHeight(results.getDouble(COLUMN_HEIGHT));
                measurements.add(measurement);
            }

            return measurements;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        } finally {
            try {
                if(results != null) {
                    results.close();
                }
            } catch (SQLException e) {
                System.out.println("Error Closing ResultSet" + e.getMessage());
            }
            try {
                if(statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing Statement" + e.getMessage());
            }
        }
    }
}
