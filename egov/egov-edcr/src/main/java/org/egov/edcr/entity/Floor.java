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
    private String number;

    public void addOccupancy(Occupancy occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy))
            occupancies.get(occupancies.indexOf(occupancy)).getArea().add(occupancy.getArea());
        else
            occupancies.add(occupancy);

    }
    public void subtractOccupancyArea(Occupancy occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy))
            occupancies.get(occupancies.indexOf(occupancy)).getArea().subtract(occupancy.getArea());
        else
            occupancies.add(occupancy);

    }
    public List<Occupancy> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(List<Occupancy> occupancies) {
        this.occupancies = occupancies;
    }

    public List<FloorUnit> getUnits() {
        return units;
    }

    public void setUnits(List<FloorUnit> units) {
        this.units = units;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

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
