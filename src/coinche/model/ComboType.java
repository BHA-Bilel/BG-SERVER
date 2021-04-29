package coinche.model;

public enum ComboType {
	JackCarré(200, 6), NineCarré(150, 5), Carré(100, 4), Suite(100, 3), Annonce(50, 2), Tierce(20, 1), Belote(20, 0);

	private final int order;
	private final int value;

	ComboType(int value, int order) {
		this.value = value;
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public int getValue() {
		return value;
	}

	public static ComboType get(int order) {
		for (ComboType name : ComboType.values()) {
			if (name.getOrder() == order) {
				return name;
			}
		}
		return null;
	}
}
