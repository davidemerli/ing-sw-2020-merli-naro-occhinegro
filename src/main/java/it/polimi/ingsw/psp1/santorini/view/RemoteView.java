package it.polimi.ingsw.psp1.santorini.view;

import it.polimi.ingsw.psp1.santorini.model.EnumActionType;
import it.polimi.ingsw.psp1.santorini.model.Game;
import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Power;
import it.polimi.ingsw.psp1.santorini.network.ClientConnectionHandler;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumRequestType;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumTurnState;
import it.polimi.ingsw.psp1.santorini.network.packets.server.*;
import it.polimi.ingsw.psp1.santorini.observer.ConnectionObserver;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Turns client input into commands for the controller to apply changes to the model
 */
public class RemoteView extends View {

    private final ClientConnectionHandler connection;

    /**
     * {@inheritDoc}
     * <p>
     * The RemoteView will convert every received packet into commands for the controller
     * to apply changes to the model, and will convert any receiving model information into packets
     * to send to the client.
     *
     * @param player     associated with the current view
     * @param connection channel where packets are transparently sent and received from
     */
    public RemoteView(Player player, ClientConnectionHandler connection) {
        super(player);
        this.connection = connection;
        this.connection.addObserver(new PacketReceiver());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet a String notification error
     */
    @Override
    public void notifyError(String error) {
        connection.sendPacket(new ServerInvalidPacket(error));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with action data
     */
    @Override
    public void playerPlaceWorker(Player player, Worker worker) {
        ServerPlayerMove.PlayerPlaceWorker move = new ServerPlayerMove.PlayerPlaceWorker(worker);

        ServerPlayerMove packet = new ServerPlayerMove(toData(player), move, EnumActionType.PLACE_WORKER);
        connection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with action data
     */
    @Override
    public void playerMove(Player player, Worker worker, Point from, Point where) {
        ServerPlayerMove.PlayerMove move = new ServerPlayerMove.PlayerMove(worker, from, where);

        ServerPlayerMove packet = new ServerPlayerMove(toData(player), move, EnumActionType.MOVE);
        connection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with action data
     */
    @Override
    public void playerBuild(Player player, Worker worker, Point where, boolean forceDome) {
        ServerPlayerMove.PlayerBuild move = new ServerPlayerMove.PlayerBuild(worker, where, forceDome);

        ServerPlayerMove packet = new ServerPlayerMove(toData(player), move, EnumActionType.BUILD);
        connection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with updated player status
     */
    @Override
    public void playerUpdate(Game game, Player player) {
        EnumTurnState turnState = EnumTurnState.fromTurnState(game.getTurnState());
        boolean interaction = game.getTurnState().shouldShowInteraction(game, player);

        PlayerData playerData = toData(player);

        ServerSendPlayerUpdate packet = new ServerSendPlayerUpdate(playerData, turnState, interaction);
        connection.sendPacket(packet);

        if (player.hasLost()) {
            connection.sendPacket(new ServerSendPlayerUpdate(playerData, EnumTurnState.LOSE, interaction));
        } else if (player.hasWon()) {
            connection.sendPacket(new ServerSendPlayerUpdate(playerData, EnumTurnState.WIN, interaction));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with updated interaction status and EndTurn state
     */
    @Override
    public void playerEndTurn(Game game, Player player) {
        PlayerData playerData = toData(player);

        ServerSendPlayerUpdate packet = new ServerSendPlayerUpdate(playerData, EnumTurnState.END_TURN, false);
        connection.sendPacket(packet);

        ServerMovePossibilities movesPacket = new ServerMovePossibilities(List.of(), Map.of());
        connection.sendPacket(movesPacket);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with the game current status
     */
    @Override
    public void gameUpdate(Game game, boolean forced) {
        List<PlayerData> players = game.getPlayerList().stream()
                .map(this::toData).collect(Collectors.toList());
        EnumTurnState turnState = EnumTurnState.fromTurnState(game.getTurnState());

        ServerGameData packet = new ServerGameData(game.getMap().copy(), players, turnState, forced);
        connection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with the available moves
     */
    @Override
    public void availableMovesUpdate(Player player, List<Point> validMoves, Map<Power, List<Point>> blockedMoves) {
        //used to sort positions in clock wise order given a selected point
        BiFunction<Point, Point, Double> angle = (p, c) -> 180.0 / Math.PI * Math.atan2(p.y - c.y, p.x - c.x);

        if (getPlayer().equals(player)) {//sends moves only to the current player
            Optional<Worker> optWorker = player.getSelectedWorker();

            if (optWorker.isPresent()) {
                Point center = optWorker.get().getPosition();
                //sorts with angle function
                validMoves = validMoves.stream()
                        .sorted(Comparator.comparingDouble(p -> angle.apply(p, center)))
                        .collect(Collectors.toUnmodifiableList());
            }

            ServerMovePossibilities packet = new ServerMovePossibilities(validMoves, blockedMoves);
            connection.sendPacket(packet);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with the request
     */
    @Override
    public void requestToPlayer(Player player, EnumRequestType requestType) {
        Optional<Player> optPlayer = connection.getPlayer();

        if (optPlayer.isPresent() && optPlayer.get().equals(player)) {
            connection.sendPacket(new ServerAskRequest(requestType));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a packet with the god powers list
     */
    @Override
    public void sendPowerList(List<Power> availablePowers, int toSelect) {
        List<Power> powers = availablePowers.stream()
                .map(Power::copy).collect(Collectors.toList());
        connection.sendPacket(new ServerPowerList(powers, toSelect));
    }

    /**
     * Gets the corresponding PlayerData
     *
     * @param player to get the data from
     * @return a PlayerData object
     */
    private PlayerData toData(Player player) {
        List<Worker> workers = player.getWorkers().stream()
                .map(w -> new Worker(w.getPosition())).collect(Collectors.toList());

        return new PlayerData(player.getName(), player.getPower(), workers);
    }

    private class PacketReceiver implements ConnectionObserver {

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processPowerList(List<Power> powerList) {
            notifyObservers(o -> o.selectPowers(RemoteView.this, getPlayer(), powerList));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processSquareSelection(Point square) {
            notifyObservers(o -> o.selectSquare(RemoteView.this, getPlayer(), square));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processToggleInteraction() {
            notifyObservers(o -> o.toggleInteraction(RemoteView.this, getPlayer()));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void handlePlayerForfeit() {
            notifyObservers(o -> o.playerSurrender(RemoteView.this, getPlayer()));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processRequestGameData() {
            //TODO: request game data,  maybe process in connection?
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processWorkerSelection(Point workerPosition) {
            notifyObservers(o -> o.selectWorker(RemoteView.this, getPlayer(), workerPosition));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void processStartingPlayerSelection(String name) {
            notifyObservers(o -> o.selectStartingPlayer(RemoteView.this, getPlayer(), name));
        }

        /**
         * {@inheritDoc}
         * <p>
         * Translates connection data into a notification for ViewObservers (i.e. controllers)
         */
        @Override
        public void handleCloseConnection() {
            notifyObservers(o -> o.leaveGame(RemoteView.this, getPlayer()));
            removeAllObservers();
        }

        /**
         * {@inheritDoc}
         * <p>
         * Notifies all clients
         */
        @Override
        public void processUndo() {
            notifyObservers(o -> o.undo(RemoteView.this, getPlayer()));
        }
    }
}
