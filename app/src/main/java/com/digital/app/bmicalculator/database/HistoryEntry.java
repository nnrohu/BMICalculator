package com.digital.app.bmicalculator.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "history")
public class HistoryEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double magnitude;

    private double weight;

    private double height;

    private String weightUnit;

    private String heightUnit;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @Ignore
    public HistoryEntry(double magnitude, double weight, double height, String weightUnit, String heightUnit, Date createdAt) {
        this.magnitude = magnitude;
        this.weight = weight;
        this.height = height;
        this.weightUnit = weightUnit;
        this.heightUnit = heightUnit;
        this.createdAt = createdAt;
    }

    public HistoryEntry(int id, double magnitude, double weight, double height, String weightUnit, String heightUnit, Date createdAt) {
        this.id = id;
        this.magnitude = magnitude;
        this.weight = weight;
        this.height = height;
        this.weightUnit = weightUnit;
        this.heightUnit = heightUnit;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(String heightUnit) {
        this.heightUnit = heightUnit;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
