package bg.server.dominos.model;

public class Domino {
	// LOGIC
	private final int leftValue;
	private final int rightValue;
	private Position position;

	public Domino(int left, int right, Position position) {
		this.leftValue = left;
		this.rightValue = right;
		this.position = position;
	}

	@Override
	public String toString() {
		return "[" + getLeftValue() + " | " + getRightValue() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;
		Domino other = (Domino) obj;
		return (other.leftValue == leftValue && other.rightValue == rightValue)
				|| (other.leftValue == rightValue && other.rightValue == leftValue);
	}

	// GETTERS SETTERS

	public int getRightValue() {
		return rightValue;
	}

	public int getLeftValue() {
		return leftValue;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
