package it.polimi.ingsw.psp1.santorini.network.packets.server;

import it.polimi.ingsw.psp1.santorini.model.map.Map;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumTurnState;
import it.polimi.ingsw.psp1.santorini.network.ServerHandler;
import it.polimi.ingsw.psp1.santorini.network.packets.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerGameData implements Packet<ServerHandler> {

    private final Map gameMap;
    private final HashMap<PlayerData, EnumTurnState> players;

    public ServerGameData(Map gameMap, HashMap<PlayerData, EnumTurnState> players) {
        this.gameMap = gameMap;
        this.players = players;
    }

    @Override
    public void processPacket(ServerHandler netHandler) {
        netHandler.handleSendGameData(this);
    }

    public Map getGameMap() {
        return gameMap;
    }

    public HashMap<PlayerData, EnumTurnState> getPlayerData() {
        return players;
    }

    public List<PlayerData> getPlayers() {
        return new ArrayList<>(players.keySet());
    }

}


