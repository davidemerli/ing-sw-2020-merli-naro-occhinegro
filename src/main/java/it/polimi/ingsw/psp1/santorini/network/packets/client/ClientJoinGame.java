package it.polimi.ingsw.psp1.santorini.network.packets.client;

import it.polimi.ingsw.psp1.santorini.network.ClientHandler;
import it.polimi.ingsw.psp1.santorini.network.packets.Packet;

/**
 * Client packet containing the information to join the game
 */
public class ClientJoinGame implements Packet<ClientHandler> {

    private final String gameRoom;
    private final int playerNumber;

    /**
     * Generic constructor using the number of players and the name of the room
     * @param playerNumber number of players
     * @param gameRoom     name of the room
     */
    public ClientJoinGame(int playerNumber, String gameRoom) {
        this.playerNumber = playerNumber;
        this.gameRoom = gameRoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPacket(ClientHandler netHandler) {
        netHandler.handleJoinGame(this);
    }

    public int getPlayerNumber() {
        return this.playerNumber;
    }

    public String getGameRoom() {
        return this.gameRoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(gameRoom, playerNumber);
    }
}
