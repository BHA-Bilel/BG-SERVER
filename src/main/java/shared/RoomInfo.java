package shared;

import java.io.Serializable;

public class RoomInfo implements Serializable {

	public final int room_id;
	public final String host_name;
	public final int room_players;

	public RoomInfo(int room_id, String host_name, int room_players) {
		this.room_id = room_id;
		this.host_name = host_name;
		this.room_players = room_players;
	}

}