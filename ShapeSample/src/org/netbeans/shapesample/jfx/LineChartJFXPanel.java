/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.shapesample.jfx;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 *
 * @author lexa
 */
public class LineChartJFXPanel extends JFXPanel {

    private LineChart chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private ObservableList<XYChart.Series<Double, Double>> lineChartData;

    public LineChartJFXPanel() {
        super();
        // create JavaFX scene
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }

    private void createScene() {
        Group root = new Group();

        //xAxis = new NumberAxis("Values for X-Axis", 0, 3, 1);
        //yAxis = new NumberAxis("Values for Y-Axis", 0, 3, 1);
        xAxis = new NumberAxis();
        xAxis.setLabel("X-Axis");
        yAxis = new NumberAxis();
        yAxis.setLabel("Y-Axis");

        Integer theSize = 200;

        lineChartData = FXCollections.observableArrayList(
                new LineChart.Series<>("Series 1", FXCollections.observableArrayList(
                                new XYChart.Data<>(0.0, 0.0)
                        )));
        chart = new LineChart(xAxis, yAxis, lineChartData);
        root.getChildren().add(chart);

        Double[] xDoubleArray = new Double[theSize];
        Double[] yDoubleArray = new Double[theSize];

        for (int i = 0; i < theSize; i++) {
            Double x = new Double(java.lang.Math.log(i));
            if (x.isInfinite() || x.isNaN()) {
                x = 0.0;
            }
            xDoubleArray[i] = x;
            yDoubleArray[i] = new Double(java.lang.Math.random() * i);
        }
        updateXY("log(1:1000) / random(1:1000)", xDoubleArray, yDoubleArray);
        xAxis.setLabel("log(1:1000)");
        yAxis.setLabel("random(1:1000)");

        this.setScene(new Scene(root));
        this.validate();
    }

    public void updateXY(final String seriesTitle, final Double[] xArray, final Double[] yArray) {
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //convert double arrays to a typed list
                List<XYChart.Data> xyChartDataList = new ArrayList<>();
                for (int i = 0; i < xArray.length; i++) {
                    xyChartDataList.add(new XYChart.Data<>(xArray[i], yArray[i]));
                }
                //Wrap typed list in ObservableList wrapper collection
                ObservableList<XYChart.Data> observableList = FXCollections.observableList(xyChartDataList);
                //add new ObservableList collection to chart
                lineChartData.setAll(new LineChart.Series(seriesTitle, observableList));
            }
        });
    }
}
