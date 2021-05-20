package bg.server.dominos.model;

public enum GameType {
	TwovTwo(4), OnevOne(2), ThreePlayers(3);

	int players;

	GameType(int players) {
		this.players = players;
	}

}
