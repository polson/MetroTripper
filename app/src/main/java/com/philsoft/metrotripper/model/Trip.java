package com.philsoft.metrotripper.model;

/**
 * Created by polson on 1/14/15.
 */
public class Trip {

	public final Vehicle vehicle;
	public final boolean actual;
	public final int blockNumber;
	public final String departureText;
	public final long departureTime;
	public final String description;
	public final String gate;
	public final String routeDirection;

	public Trip(Vehicle vehicle, boolean actual, int blockNumber, String departureText, long departureTime, String description, String gate, String routeDirection) {
		this.vehicle = vehicle;
		this.actual = actual;
		this.blockNumber = blockNumber;
		this.departureText = departureText;
		this.departureTime = departureTime;
		this.description = description;
		this.gate = gate;
		this.routeDirection = routeDirection;
	}

	@Override
	public String toString() {
		return "Trip{" +
				"vehicle=" + vehicle +
				", actual=" + actual +
				", blockNumber=" + blockNumber +
				", departureText='" + departureText + '\'' +
				", departureTime=" + departureTime +
				", description='" + description + '\'' +
				", gate='" + gate + '\'' +
				", routeDirection='" + routeDirection + '\'' +
				'}';
	}
}
