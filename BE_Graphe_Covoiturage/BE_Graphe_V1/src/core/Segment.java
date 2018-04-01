package core;

public class Segment {

	private float dLongitude;
	private float dLatitude;
	
	public Segment(float dLongitude, float dLatitude) {
		this.dLongitude = dLongitude;
		this.dLatitude = dLatitude;
	}

	public float getdLongitude() {
		return dLongitude;
	}

	public void setdLongitude(float dLongitude) {
		this.dLongitude = dLongitude;
	}

	public float getdLatitude() {
		return dLatitude;
	}

	public void setdLatitude(float dLatitude) {
		this.dLatitude = dLatitude;
	}
}
