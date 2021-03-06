package it.polimi.ingsw.psp1.santorini.network;

import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumRequestType;
import it.polimi.ingsw.psp1.santorini.network.packets.Packet;
import it.polimi.ingsw.psp1.santorini.network.packets.client.*;
import it.polimi.ingsw.psp1.santorini.network.packets.server.ServerAskRequest;
import it.polimi.ingsw.psp1.santorini.network.packets.server.ServerInvalidPacket;
import it.polimi.ingsw.psp1.santorini.network.packets.server.ServerKeepAlive;
import it.polimi.ingsw.psp1.santorini.observer.ConnectionObserver;
import it.polimi.ingsw.psp1.santorini.observer.Observable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages the client behaviour during the communication between client and server
 */
public class ClientConnectionHandler extends Observable<ConnectionObserver> implements Runnable, ClientHandler {

    private final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    private final Server server;

    private final Socket clientSocket;

    private final ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private Player player;
    private boolean closed;

    ClientConnectionHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;

        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        sendPacket(new ServerAskRequest(EnumRequestType.SELECT_NAME));
    }

    /**
     * Sends a packet without delay
     *
     * @param packet to send
     */
    public void sendPacket(Packet<ServerHandler> packet) {
        sendPacket(packet, 0);
    }

    /**
     * Sends a packet with delay
     *
     * @param packet to send
     * @param delay  in milliseconds
     */
    public void sendPacket(Packet<ServerHandler> packet, int delay) {
        if (closed) {
            return;
        }

        pool.schedule(() -> {
            try {
                objectOutputStream.writeObject(packet);

                if (!(packet instanceof ServerKeepAlive)) {
                    System.out.println("sent to '" + getPlayer().orElse(new Player("N/A")).getName() + "' :" + packet.toString());
                }
            } catch (IOException e) {
                closeConnection();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Available at the reception of the packet from server
     * Prints received packet from server
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (!closed) {
                Object object = objectInputStream.readObject();

                if (!(object instanceof ClientKeepAlive)) {
                    System.out.println("received " + object.toString());
                }

                ((Packet<ClientHandler>) object).processPacket(this);
            }
        } catch (Exception ex) {
            closeConnection();
        }
        closeConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePlayerSetName(ClientSetName packet) {
        if(getPlayer().isPresent()) {
            sendPacket(new ServerInvalidPacket("Username already set"));
            return;
        }

        if (server.isUsernameValid(packet.getName()) && server.isUsernameUnique(packet.getName())) {
            player = new Player(packet.getName());

            sendPacket(new ServerAskRequest(EnumRequestType.CHOOSE_GAME));
        } else {
            sendPacket(new ServerAskRequest(EnumRequestType.RESELECT_NAME));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCreateGame(ClientCreateGame packet) {
        try {
            server.createGame(this, packet.getPlayerNumber());
        } catch (Exception e) {
            sendPacket(new ServerInvalidPacket(e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Joins game with name's room or joins the queue
     */
    @Override
    public void handleJoinGame(ClientJoinGame packet) {
        try {
            if (packet.getGameRoom() != null) {
                server.joinGame(this, packet.getGameRoom());
            } else {
                server.joinQueue(this, packet.getPlayerNumber());
            }
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
            sendPacket(new ServerInvalidPacket(ex.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handlePowerChoosing(ClientChoosePower packet) {
        notifyObservers(o -> o.processPowerList(packet.getPowers()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleSquareSelect(ClientSelectSquare packet) {
        notifyObservers(o -> o.processSquareSelection(packet.getSquare()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleInteractionToggle() {
        notifyObservers(ConnectionObserver::processToggleInteraction);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handlePlayerForfeit() {
        notifyObservers(ConnectionObserver::handlePlayerForfeit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleKeepAlive() {
        sendPacket(new ServerKeepAlive(), 1000);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleUndo() {
        notifyObservers(ConnectionObserver::processUndo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleRequestGameData() {
        notifyObservers(ConnectionObserver::processRequestGameData);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleWorkerSelection(ClientSelectWorker packet) {
        notifyObservers(o -> o.processWorkerSelection(packet.getWorkerPosition()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifies all players
     */
    @Override
    public void handleSelectStartingPlayer(ClientSelectStartingPlayer packet) {
        notifyObservers(o -> o.processStartingPlayerSelection(packet.getName()));
    }

    /**
     * Closes connection between client and server
     */
    public void closeConnection() {
        if (closed) {
            return;
        }

        System.out.println("Client associated with '" + getPlayer().orElse(new Player("N/A")).getName() +
                "' disconnected.");

        closed = true;
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.disconnectClient(this);

        notifyObservers(ConnectionObserver::handleCloseConnection);
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    public boolean isClosed() {
        return closed;
    }

}
