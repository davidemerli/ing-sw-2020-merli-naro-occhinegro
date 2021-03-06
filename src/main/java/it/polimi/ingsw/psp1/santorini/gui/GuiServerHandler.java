package it.polimi.ingsw.psp1.santorini.gui;

import it.polimi.ingsw.psp1.santorini.gui.controllers.*;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Power;
import it.polimi.ingsw.psp1.santorini.network.Client;
import it.polimi.ingsw.psp1.santorini.network.ServerHandler;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumTurnState;
import it.polimi.ingsw.psp1.santorini.network.packets.server.*;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Manages server behaviour in GUI
 */
public class GuiServerHandler extends ServerHandler {

    private final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    /**
     * Generic constructor using client
     *
     * @param client current client
     */
    public GuiServerHandler(Client client) {
        super(client);

        client.enableDebug();

        GuiObserver guiObserver = new GuiObserver(client, this);

        ChooseGameSceneController.getInstance().addObserver(guiObserver);
        NameSelectionController.getInstance().addObserver(guiObserver);
        IpSelectionController.getInstance().addObserver(guiObserver);
        GameSceneController.getInstance().addObserver(guiObserver);
        ChoosePowersController.getInstance().addObserver(guiObserver);
        WaitGodSelectionController.getInstance().addObserver(guiObserver);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages game data
     *
     * @param packet to send
     */
    @Override
    public void handleGameData(ServerGameData packet) {
        String first = playerDataList.get(0).getName();

        super.handleGameData(packet);

        if(!packet.getPlayerData().get(0).getName().equals(first)) {
            GameSceneController.getInstance().highlightCurrentPlayer(packet.getPlayerData().get(0).getName());
        }

        if (packet.isForced()) { //resets the map with the data from the packet
            pool.execute(() -> {
                GameSceneController.getInstance().resetMap();

                for (Point p : packet.getGameMap().getAllSquares()) {
                    int level = packet.getGameMap().getLevel(p);
                    boolean hasDome = packet.getGameMap().hasDome(p);

                    IntStream.range(0, hasDome ? level - 1 : level)
                            .forEach(i -> GameSceneController.getInstance().addBlockAt(p.x, p.y, false, false));

                    if (hasDome) {
                        GameSceneController.getInstance().addBlockAt(p.x, p.y, true, false);
                    }
                }

                for (PlayerData player : packet.getPlayerData()) {
                    boolean isOwn = player.getName().equals(playerName);
                    Color color = playerColorMap.get(player.getName()).getColor();

                    player.getWorkers().stream()
                            .map(Worker::getPosition)
                            .forEach(p -> GameSceneController.getInstance().addWorker(p.x, p.y, color, isOwn, false));
                }

                List<Point> valid = isYourTurn() ? validMoves : List.of();
                List<Point> blocked = isYourTurn() ?
                        blockedMoves.values().stream().flatMap(Collection::stream).collect(Collectors.toList()) :
                        List.of();
                GameSceneController.getInstance().showValidMoves(valid, blocked, lastTurnState);
            });
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages all request types
     * @param packet to send
     */
    @Override
    public void handleRequest(ServerAskRequest packet) {
        super.handleRequest(packet);

        String request = null;

        switch (packet.getRequestType()) {
            case RESELECT_NAME:
                pool.schedule(() -> NameSelectionController.getInstance().showError(), 1, TimeUnit.SECONDS);
                break;
            case CHOOSE_GAME:
                pool.schedule(() -> Gui.getInstance().changeSceneSync(EnumScene.CREATE_JOIN), 2, TimeUnit.SECONDS);
                break;
            case CHOOSE_POWERS:
                pool.schedule(() -> Gui.getInstance().changeSceneSync(EnumScene.CHOOSE_POWERS), 2, TimeUnit.SECONDS);
                break;
            case SELECT_POWER:
                pool.schedule(() -> Gui.getInstance().changeSceneSync(EnumScene.CHOOSE_POWERS), 1, TimeUnit.SECONDS);
                break;
            case SELECT_STARTING_PLAYER:
                pool.schedule(() -> {
                    WaitGodSelectionController.getInstance().setStateMessage("Click to select the starting player!");

                    for (PlayerData playerData : getPlayerDataList()) {
                        StartingPlayerController.getInstance().addPlayer(playerData.getName(), playerData.getPower());
                        WaitGodSelectionController.getInstance().setPlayerPower(playerData.getName(), playerData.getPower());
                    }

                    WaitGodSelectionController.getInstance().setupForStartingPlayerChoice();

                    Gui.getInstance().changeSceneSync(EnumScene.WAIT_GOD_SELECTION);
                }, 1, TimeUnit.SECONDS);
                break;

            case PLACE_WORKER:
                request = "Place a Worker!";
                break;
            case SELECT_WORKER:
                request = "Select a Worker!";
                break;
            case SELECT_SQUARE:
                request = lastTurnState == EnumTurnState.MOVE ? "Move a Worker!" : "Build a block!";
                break;
            case DISCONNECT:
                client.disconnect();
                break;
        }

        if (request != null) {
            GameSceneController.getInstance().showRequest(request);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages player update
     *
     * @param packet to send
     */
    @Override
    public void handlePlayerUpdate(ServerSendPlayerUpdate packet) {
        super.handlePlayerUpdate(packet);

        boolean yourUpdate = packet.getPlayerData().getName().equals(playerName);

        if (packet.getPlayerData().getPower() != null) {
            WaitGodSelectionController.getInstance().setPlayerPower(packet.getPlayerData().getName(),
                    packet.getPlayerData().getPower());

            Color color = getPlayerColorMap().get(packet.getPlayerData().getName()).getColor();

            GameSceneController.getInstance().addPlayer(packet.getPlayerData().getName(), color,
                    packet.getPlayerData().getPower(), yourUpdate);
        }

        if (packet.getPlayerState() == EnumTurnState.WORKER_PLACING) {
            Gui.getInstance().changeSceneSync(EnumScene.GAME);
        }

        if (!yourUpdate && packet.getPlayerState() == EnumTurnState.SELECT_POWERS) {
            String state = packet.getPlayerData().getName() + " is choosing powers for this game...";

            pool.schedule(() -> WaitGodSelectionController.getInstance().setStateMessage(state), 2, TimeUnit.SECONDS);
        }

        if (!yourUpdate && packet.getPlayerState() == EnumTurnState.CHOOSE_OWN_POWER
                && packet.getPlayerData().getPower() == null) {
            String state = packet.getPlayerData().getName() + " is choosing his powers...";

            pool.schedule(() -> WaitGodSelectionController.getInstance().setStateMessage(state), 1, TimeUnit.SECONDS);
        }

        if (!yourUpdate && packet.getPlayerState() == EnumTurnState.SELECT_STARTING_PLAYER) {
            String state = packet.getPlayerData().getName() + " is choosing the starting player...";

            WaitGodSelectionController.getInstance().setStateMessage(state);
        }

        if (yourUpdate && shouldShowInteraction) {
            GameSceneController.getInstance().setInteractButtonTexture(packet.getPlayerData().getPower());
            GameSceneController.getInstance().showInteract(packet.getPlayerData().getPower(), true);
        } else {
            GameSceneController.getInstance().showInteract(packet.getPlayerData().getPower(), false);
        }

        if(yourUpdate && packet.getPlayerState() == EnumTurnState.WIN) {
            GameSceneController.getInstance().showEndGame(playerName, true);
        }

        if(yourUpdate && packet.getPlayerState() == EnumTurnState.LOSE) {
            GameSceneController.getInstance().showEndGame(playerName, false);
        }

        if(yourUpdate && packet.getPlayerState() == EnumTurnState.END_TURN) {
            GameSceneController.getInstance().setupUndoTimer();
        }

        if (!yourUpdate) {
            GameSceneController.getInstance().hideRequest();
            GameSceneController.getInstance().showValidMoves(List.of(), List.of(), EnumTurnState.END_TURN);
        }

        GameSceneController.getInstance().showUndo(isYourTurn());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages received moves
     *
     * @param packet to send
     */
    @Override
    public void handleReceivedMoves(ServerMovePossibilities packet) {
        super.handleReceivedMoves(packet);

        List<Point> blockedMoves = getBlockedMoves().values().stream()
                .flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());

        if (isYourTurn()) {
            pool.schedule(() -> GameSceneController.getInstance().showValidMoves(getValidMoves(), blockedMoves, lastTurnState),
                    300, TimeUnit.MILLISECONDS);
        }

        Gui.getInstance().changeSceneSync(EnumScene.GAME);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages errors
     *
     * @param packet to send
     */
    @Override
    public void handleError(ServerInvalidPacket packet) {
        //No need to handle errors since user input is limited in GUI
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages player moves
     *
     * @param packet to send
     */
    @Override
    public void handlePlayerMove(ServerPlayerMove packet) {
        boolean ownData = packet.getPlayerData().getName().equals(playerName);

        switch (packet.getMoveType()) {
            case MOVE:
                ServerPlayerMove.PlayerMove move = (ServerPlayerMove.PlayerMove) packet.getMove();

                GameSceneController.getInstance().moveWorker(move.getSrc(), move.getDest(), ownData);
                break;
            case BUILD:
                ServerPlayerMove.PlayerBuild build = (ServerPlayerMove.PlayerBuild) packet.getMove();

                GameSceneController.getInstance().addBlockAt(build.getDest().x, build.getDest().y, build.forceDome(), true);

                break;
            case PLACE_WORKER:
                ServerPlayerMove.PlayerPlaceWorker worker = (ServerPlayerMove.PlayerPlaceWorker) packet.getMove();

                Color color = getPlayerColorMap().get(packet.getPlayerData().getName()).getColor().darker();

                GameSceneController.getInstance().addWorker(worker.getDest().x, worker.getDest().y,
                        packet.getPlayerData().getName().equals("PRIDE") ? null : color,
                        isYourTurn(), true);
                break;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages power list
     *
     * @param packet to send
     */
    @Override
    public void handlePowerList(ServerPowerList packet) {
        super.handlePowerList(packet);

        if (isYourTurn()) {
            ChoosePowersController.getInstance().addGods(packet.getAvailablePowers(), packet.getToSelect());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages player connection
     *
     * @param packet to send
     */
    @Override
    public void handlePlayerConnected(ServerConnectedToGame packet) {
        super.handlePlayerConnected(packet);

        pool.schedule(() -> {
            String message = getPlayerName().equals(packet.getUsername()) ? "You joined a game!" :
                    packet.getUsername() + " joined the game!";

            if (packet.getPlayerNumber() == WaitGodSelectionController.getInstance().getConnectedPlayersCount()) {
                message += " Game is starting...";
            }

            WaitGodSelectionController.getInstance().setStateMessage(message);
            WaitGodSelectionController.getInstance().showRoomID(packet.getGameID());
        }, 2000, TimeUnit.MILLISECONDS);

        pool.schedule(() -> WaitGodSelectionController.getInstance().addPlayer(packet.getUsername()),
                2500, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages payer disconnection
     */
    @Override
    public void onDisconnect() {
        pool.schedule(() -> {
            if(!GameSceneController.getInstance().hasGameEnded()) {
                Gui.getInstance().changeSceneSync(EnumScene.IP_SELECT);
                GameSceneController.getInstance().reset();
            }

            reset();
        }, 1, TimeUnit.SECONDS);

        System.out.println("Connection lost, please reconnect.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages connection fail
     */
    @Override
    public void onConnectionFail() {
        IpSelectionController.getInstance().stopConnectionAnimation();
        IpSelectionController.getInstance().showConnectionError();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Manages reset
     */
    @Override
    public void reset() {
        super.reset();

        ChooseGameSceneController.getInstance().reset();
        ChoosePowersController.getInstance().reset();
        IpSelectionController.getInstance().reset();
        NameSelectionController.getInstance().reset();
        StartingPlayerController.getInstance().reset();
        WaitGodSelectionController.getInstance().reset();
    }
}