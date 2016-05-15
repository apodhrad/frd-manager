package org.apodhrad.frd.manager;

public class FrdData {

	private String name;
	private String state;
	private int size;
	private int downloaded;
	private double speed;
	private double averageSpeed;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(int downloaded) {
		this.downloaded = downloaded;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	@Override
	public String toString() {
		return "FrdData [name=" + name + ", state=" + state + "]";
	}

}
