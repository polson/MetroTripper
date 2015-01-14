package com.philsoft.metrotripper.constants;

public enum Direction {
    UNKNOWN(0),
    SOUTHBOUND(1),
    EASTBOUND(2),
    WESTBOUND(3),
    NORTHBOUND(4);

    private final int directionNum;

    Direction(int directionNum) {
        this.directionNum = directionNum;
    }

    public static Direction fromValue(int value) {
        switch (value) {
            case 1:
                return SOUTHBOUND;
            case 2:
                return EASTBOUND;
            case 3:
                return WESTBOUND;
            case 4:
                return NORTHBOUND;
            default:
                return UNKNOWN;
        }
    }

    public int getDirectionNum() {
        return directionNum;
    }
}