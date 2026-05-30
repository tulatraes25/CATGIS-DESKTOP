/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin.generate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class BoundaryMatchDataEngine {
    private static final Logger LOGGER = Logger.getLogger(BoundaryMatchDataEngine.class);
    private Coordinate southwestCornerOfLeftLayer = new Coordinate(0.0, 0.0);
    private int layerHeightInCells = 4;
    private int layerWidthInCells = 1;
    private double cellSideLength = 100.0;
    private int verticesPerCellSide = 4;
    private double boundaryAmplitude = 20.0;
    private double boundaryPeriod = 150.0;
    private int verticesPerBoundarySide = 6;
    private double maxBoundaryPerturbation = 1.0;
    private double perturbationProbability = 0.5;
    private GeometryFactory factory = new GeometryFactory();

    public void setSouthwestCornerOfLeftLayer(Coordinate newSouthwestCornerOfLeftLayer) {
        this.southwestCornerOfLeftLayer = newSouthwestCornerOfLeftLayer;
    }

    public void setLayerHeightInCells(int newLayerHeightInCells) {
        this.layerHeightInCells = newLayerHeightInCells;
    }

    public void setLayerWidthInCells(int newLayerWidthInCells) {
        this.layerWidthInCells = newLayerWidthInCells;
    }

    public void setCellSideLength(double newCellSideLength) {
        this.cellSideLength = newCellSideLength;
    }

    public void setVerticesPerCellSide(int newVerticesPerCellSide) {
        this.verticesPerCellSide = newVerticesPerCellSide;
    }

    public void setBoundaryAmplitude(double newBoundaryAmplitude) {
        this.boundaryAmplitude = newBoundaryAmplitude;
    }

    public void setBoundaryPeriod(double newBoundaryPeriod) {
        this.boundaryPeriod = newBoundaryPeriod;
    }

    public void setVerticesPerBoundarySide(int newVerticesPerBoundarySide) {
        this.verticesPerBoundarySide = newVerticesPerBoundarySide;
    }

    public void setMaxBoundaryPerturbation(double newMaxBoundaryPerturbation) {
        this.maxBoundaryPerturbation = newMaxBoundaryPerturbation;
    }

    public void setPerturbationProbability(double newPerturbationProbability) {
        this.perturbationProbability = newPerturbationProbability;
    }

    public Coordinate getSouthwestCornerOfLeftLayer() {
        return this.southwestCornerOfLeftLayer;
    }

    public int getLayerHeightInCells() {
        return this.layerHeightInCells;
    }

    public int getLayerWidthInCells() {
        return this.layerWidthInCells;
    }

    public double getCellSideLength() {
        return this.cellSideLength;
    }

    public int getVerticesPerCellSide() {
        return this.verticesPerCellSide;
    }

    public double getBoundaryAmplitude() {
        return this.boundaryAmplitude;
    }

    public double getBoundaryPeriod() {
        return this.boundaryPeriod;
    }

    public int getVerticesPerBoundarySide() {
        return this.verticesPerBoundarySide;
    }

    public double getMaxBoundaryPerturbation() {
        return this.maxBoundaryPerturbation;
    }

    public double getPerturbationProbability() {
        return this.perturbationProbability;
    }

    public void execute(PlugInContext context) throws Exception {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        FeatureDataset leftFeatureCollection = new FeatureDataset(featureSchema);
        FeatureDataset rightFeatureCollection = new FeatureDataset(featureSchema);
        this.addLeftSquareCells(leftFeatureCollection);
        this.addRightSquareCells(rightFeatureCollection);
        this.addBoundaryCells(leftFeatureCollection, rightFeatureCollection);
        context.addLayer(StandardCategoryNames.WORKING, I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataEngine.left"), leftFeatureCollection);
        context.addLayer(StandardCategoryNames.WORKING, I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataEngine.right"), rightFeatureCollection);
    }

    private double segmentLength() {
        return this.cellSideLength / (double)(this.verticesPerCellSide - 1);
    }

    private void addBoundaryCells(FeatureCollection leftFeatureCollection, FeatureCollection rightFeatureCollection) throws Exception {
        Coordinate southwestCornerOfBoundary = new Coordinate(this.southwestCornerOfLeftLayer.x + (double)this.layerWidthInCells * this.cellSideLength, this.southwestCornerOfLeftLayer.y);
        Coordinate topLeftBoundaryCoordinate = null;
        Coordinate topRightBoundaryCoordinate = null;
        double boundaryX = southwestCornerOfBoundary.x + this.cellSideLength / 2.0;
        int j = 0;
        while (j < this.layerHeightInCells) {
            topLeftBoundaryCoordinate = this.addBoundaryCell(leftFeatureCollection, boundaryX, southwestCornerOfBoundary.x, southwestCornerOfBoundary.y + (double)j * this.cellSideLength, topLeftBoundaryCoordinate);
            topRightBoundaryCoordinate = this.addBoundaryCell(rightFeatureCollection, boundaryX, southwestCornerOfBoundary.x + this.cellSideLength, southwestCornerOfBoundary.y + (double)j * this.cellSideLength, topRightBoundaryCoordinate);
            ++j;
        }
    }

    private Coordinate addBoundaryCell(FeatureCollection featureCollection, double boundaryX, double flatX, double south, Coordinate prevCellsTopBoundaryCoordinate) throws Exception {
        List boundaryCoordinates = this.boundaryCoordinates(boundaryX, south, prevCellsTopBoundaryCoordinate);
        this.add(this.boundaryCell(flatX, south, boundaryCoordinates), featureCollection);
        return (Coordinate)boundaryCoordinates.get(0);
    }

    private Polygon boundaryCell(double x, double south, List boundaryCoordinates) {
        ArrayList<Object> coordinates = new ArrayList<Object>();
        int i = 0;
        while (i < this.verticesPerCellSide) {
            coordinates.add(this.round(new Coordinate(x, south + (double)i * this.segmentLength())));
            ++i;
        }
        coordinates.addAll(boundaryCoordinates);
        coordinates.add(coordinates.get(0));
        return this.polygon(coordinates);
    }

    private List boundaryCoordinates(double boundaryX, double south, Coordinate prevCellsTopBoundaryCoordinate) {
        ArrayList<Coordinate> boundaryCoordinates = new ArrayList<Coordinate>();
        double segmentLength = this.cellSideLength / (double)(this.verticesPerBoundarySide - 1);
        int i = this.verticesPerBoundarySide - 1;
        while (i >= 0) {
            if (i == 0 && prevCellsTopBoundaryCoordinate != null) {
                boundaryCoordinates.add(prevCellsTopBoundaryCoordinate);
            } else {
                double y = south + (double)i * segmentLength;
                double x = boundaryX + this.boundaryAmplitude * Math.sin(Math.PI * 2 * y / this.boundaryPeriod);
                if (Math.random() < this.perturbationProbability) {
                    x += 2.0 * Math.random() * this.maxBoundaryPerturbation - this.maxBoundaryPerturbation;
                    y += 2.0 * Math.random() * this.maxBoundaryPerturbation - this.maxBoundaryPerturbation;
                }
                boundaryCoordinates.add(this.round(new Coordinate(x, y)));
            }
            --i;
        }
        return boundaryCoordinates;
    }

    private void addLeftSquareCells(FeatureCollection leftFeatureCollection) throws Exception {
        this.addSquareCells(leftFeatureCollection, this.southwestCornerOfLeftLayer);
    }

    private void addRightSquareCells(FeatureCollection rightFeatureCollection) throws Exception {
        Coordinate southwestCornerOfRightLayer = new Coordinate(this.southwestCornerOfLeftLayer.x + (double)(this.layerWidthInCells + 1) * this.cellSideLength, this.southwestCornerOfLeftLayer.y);
        this.addSquareCells(rightFeatureCollection, southwestCornerOfRightLayer);
    }

    private void addSquareCells(FeatureCollection featureCollection, Coordinate southwestCornerOfLayer) throws Exception {
        int i = 0;
        while (i < this.layerWidthInCells) {
            int j = 0;
            while (j < this.layerHeightInCells) {
                this.add(this.squareCell(i, j, southwestCornerOfLayer), featureCollection);
                ++j;
            }
            ++i;
        }
    }

    private void add(Polygon polygon, FeatureCollection featureCollection) throws Exception {
        Feature feature = FeatureUtil.toFeature((Geometry)polygon, featureCollection.getFeatureSchema());
        featureCollection.add(feature);
    }

    private Polygon squareCell(int i, int j, Coordinate southwestCornerOfLayer) {
        return this.squareCell(southwestCornerOfLayer.x + (double)i * this.cellSideLength, southwestCornerOfLayer.y + (double)j * this.cellSideLength);
    }

    private Polygon squareCell(double west, double south) {
        ArrayList<Object> coordinates = new ArrayList<Object>();
        int i = 0;
        while (i < this.verticesPerCellSide - 1) {
            coordinates.add(this.round(new Coordinate(west, south + (double)i * this.segmentLength())));
            ++i;
        }
        i = 0;
        while (i < this.verticesPerCellSide - 1) {
            coordinates.add(this.round(new Coordinate(west + (double)i * this.segmentLength(), south + this.cellSideLength)));
            ++i;
        }
        i = this.verticesPerCellSide - 1;
        while (i > 0) {
            coordinates.add(this.round(new Coordinate(west + this.cellSideLength, south + (double)i * this.segmentLength())));
            --i;
        }
        i = this.verticesPerCellSide - 1;
        while (i > 0) {
            coordinates.add(this.round(new Coordinate(west + (double)i * this.segmentLength(), south)));
            --i;
        }
        coordinates.add(coordinates.get(0));
        return this.polygon(coordinates);
    }

    private Polygon polygon(List coordinates) {
        Coordinate[] coordinateArray = coordinates.toArray(new Coordinate[0]);
        return this.factory.createPolygon(this.factory.createLinearRing(coordinateArray), null);
    }

    private Coordinate round(Coordinate coord) {
        coord.x = Math.floor(coord.x);
        coord.y = Math.floor(coord.y);
        return coord;
    }
}

