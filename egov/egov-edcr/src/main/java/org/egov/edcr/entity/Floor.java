package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;


import org.egov.edcr.entity.measurement.Measurement;

public class Floor extends Measurement {

    private List<Occupancy> occupancies=new ArrayList<>();
    private List<FloorUnit> units=new ArrayList<>();
    private List<Room> habitableRooms = new ArrayList<>();
    private Measurement exterior;
    private List<Measurement> openSpaces = new ArrayList<>();
    private String name;

    public List<Room> getHabitableRooms() {
        return habitableRooms;
    }

    public void setHabitableRooms(List<Room> habitableRooms) {
        this.habitableRooms = habitableRooms;
    }

    public Measurement getExterior() {
        return exterior;
    }

    public void setExterior(Measurement exterior) {
        this.exterior = exterior;
    }

    public List<Measurement> getOpenSpaces() {
        return openSpaces;
    }

    public void setOpenSpaces(List<Measurement> openSpaces) {
        this.openSpaces = openSpaces;
    }

    @Override
    public String toString() {

        return "Floor [habitableRooms Count" + habitableRooms.size() + "\n exterior=" + exterior + "\n openSpaces Count=" + openSpaces.size() + "]";

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
