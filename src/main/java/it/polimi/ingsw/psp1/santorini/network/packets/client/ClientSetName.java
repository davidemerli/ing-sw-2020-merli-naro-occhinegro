package it.polimi.ingsw.psp1.santorini.network.packets.client;

import it.polimi.ingsw.psp1.santorini.network.ClientHandler;
import it.polimi.ingsw.psp1.santorini.network.packets.Packet;

/**
 * Client packet containing a name
 */
public class ClientSetName implements Packet<ClientHandler> {

    private final String name;

    /**
     * Generic constructor using the name of the player
     * @param name of the player
     */
    public ClientSetName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPacket(ClientHandler netHandler) {
        netHandler.handlePlayerSetName(this);
    }

    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(name);
    }
}
