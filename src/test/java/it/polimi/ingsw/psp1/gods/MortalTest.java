package it.polimi.ingsw.psp1.gods;

import it.polimi.ingsw.psp1.santorini.model.Game;
import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Mortal;
import it.polimi.ingsw.psp1.santorini.model.powers.Power;
import it.polimi.ingsw.psp1.santorini.model.turn.Build;
import it.polimi.ingsw.psp1.santorini.model.turn.Move;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class MortalTest {

    private Game game;
    private Player player1, player2;

    @Before
    public void setup() {
        this.game = new Game("1", 2);
        this.player1 = new Player("p1");
        this.player2 = new Player("p2");

        game.addPlayer(player1);
        game.addPlayer(player2);

        player1.setPower(new Mortal());
        player2.setPower(new Mortal());
    }

    @Test
    public void onBeginTurn_normalBehaviour_shouldGoToMove() {
        Worker w = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(4, 4));
        Worker w3 = new Worker(new Point(2, 2));
        Worker w4 = new Worker(new Point(3, 2));

        player1.addWorker(w);
        player1.addWorker(w2);
        player2.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);

        assertTrue(game.getTurnState() instanceof Move);
    }

    @Test
    public void onBeginTurn_normalBehaviour_shouldLose() {
        Worker w = new Worker(new Point(0, 0));
        Worker w2 = new Worker(new Point(4, 4));
        Worker w3 = new Worker(new Point(2, 2));
        Worker w4 = new Worker(new Point(3, 2));
        
        game.getMap().buildBlock(new Point (0, 1), true);
        game.getMap().buildBlock(new Point (1, 0), true);
        game.getMap().buildBlock(new Point (1, 1), true);
        game.getMap().buildBlock(new Point (4, 3), true);
        game.getMap().buildBlock(new Point (3, 3), true);
        game.getMap().buildBlock(new Point (3, 4), true);
        
        player1.addWorker(w);
        player1.addWorker(w2);
        player2.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);

        List<Point> validMoves = game.getTurnState().getValidMoves(game, player1, w);

        assertTrue(validMoves.isEmpty());
        assertTrue(player1.hasLost());
    }
    

    @Test
    public void onYourBuild_normalBehaviour_shouldBuild() {
        Worker w = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(4, 4));
        Worker w3 = new Worker(new Point(2, 3));
        Worker w4 = new Worker(new Point(3, 2));

        Point newPosition = new Point(2, 2);

        int oldLevel = game.getMap().getLevel(newPosition);

        player1.addWorker(w);
        player1.addWorker(w2);
        player2.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);
        game.getTurnState().selectSquare(game, player1, new Point(1, 2));
        game.getTurnState().selectSquare(game, player1, newPosition);

        int newLevel = game.getMap().getLevel(newPosition);

        assertEquals(newLevel, oldLevel + 1);
    }

    @Test
    public void onYourMove_normalBehaviour_shouldMove() {
        Point oldPosition = new Point(1, 1);
        Point newPosition = new Point(2, 2);
        Worker w = new Worker(oldPosition);
        Worker w2 = new Worker(new Point(4, 4));
        Worker w3 = new Worker(new Point(2, 3));
        Worker w4 = new Worker(new Point(3, 2));

        player1.addWorker(w);
        player1.addWorker(w2);
        player2.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);
        game.getTurnState().selectSquare(game, player1, newPosition);

        assertEquals(w.getPosition(), newPosition);
        assertTrue(game.getTurnState() instanceof Build);
    }

    @Test
    public void onYourMove_normalBehaviour_shouldWin() {
        Point oldPosition = new Point(1, 1);
        Point newPosition = new Point(2, 2);
        Worker w = new Worker(oldPosition);
        Worker w2 = new Worker(new Point(4, 4));
        Worker w3 = new Worker(new Point(2, 3));
        Worker w4 = new Worker(new Point(3, 2));

        IntStream.range(0, 2).forEach(i -> game.getMap().buildBlock(oldPosition, false));
        IntStream.range(0, 3).forEach(i -> game.getMap().buildBlock(newPosition, false));

        player1.addWorker(w);
        player1.addWorker(w2);
        player2.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);
        game.getTurnState().selectSquare(game, player1, newPosition);

        assertTrue(player1.hasWon());
    }

    @Test
    public void getValidMoves_normalBehaviourMove_shouldNotContainOtherWorkerPosition() {
        Worker w1 = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(2, 2));
        Worker w3 = new Worker(new Point(1, 2));
        Worker w4 = new Worker(new Point(4, 4));

        player1.addWorker(w1);
        player2.addWorker(w2);
        player1.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w1);

        assertTrue(game.getTurnState() instanceof Move);
        assertFalse(game.getTurnState().getValidMoves(game, player1, w1).contains(w2.getPosition()));
        assertFalse(game.getTurnState().getValidMoves(game, player1, w1).contains(w3.getPosition()));
    }

    @Test
    public void getValidMoves_normalBehaviourMove_shouldNotContainDome() {
        Worker w = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(2, 2));
        Worker w3 = new Worker(new Point(1, 2));
        Worker w4 = new Worker(new Point(4, 4));

        Point position = new Point(2, 2);
        game.getMap().buildBlock(position, true);

        player1.addWorker(w);
        player2.addWorker(w2);
        player1.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);

        assertTrue(game.getTurnState() instanceof Move);
        assertFalse(game.getTurnState().getValidMoves(game, player1, w).contains(position));
    }

    @Test
    public void getValidMoves_normalBehaviourMove_shouldNotContainTooHighPlace() {
        Point oldPosition = new Point(2, 2);
        Point newPosition = new Point(2, 2);

        Worker w = new Worker(new Point(oldPosition));
        Worker w2 = new Worker(new Point(3, 3));
        Worker w3 = new Worker(new Point(1, 2));
        Worker w4 = new Worker(new Point(4, 4));

        IntStream.range(0, 0).forEach(i -> game.getMap().buildBlock(oldPosition, false));
        IntStream.range(0, 2).forEach(i -> game.getMap().buildBlock(newPosition, false));

        player1.addWorker(w);
        player2.addWorker(w2);
        player1.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);

        assertTrue(game.getTurnState() instanceof Move);
        assertFalse(game.getTurnState().getValidMoves(game, player1, w).contains(newPosition));
    }

    @Test
    public void getValidMoves_normalBehaviourBuild_shouldNotContainDome() {
        Worker w = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(3, 3));
        Worker w3 = new Worker(new Point(3, 2));
        Worker w4 = new Worker(new Point(4, 4));

        Point position = new Point(2, 2);
        game.getMap().buildBlock(position, true);

        player1.addWorker(w);
        player2.addWorker(w2);
        player1.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w);
        game.getTurnState().selectSquare(game, player1, new Point(1, 2));

        assertTrue(game.getTurnState() instanceof Build);
        assertFalse(game.getTurnState().getValidMoves(game, player1, w).contains(position));
    }

    @Test
    public void getValidMoves_normalBehaviourBuild_shouldNotContainOtherWorkerPosition() {
        Worker w1 = new Worker(new Point(1, 1));
        Worker w2 = new Worker(new Point(2, 2));
        Worker w3 = new Worker(new Point(1, 2));
        Worker w4 = new Worker(new Point(4, 4));

        player1.addWorker(w1);
        player2.addWorker(w2);
        player1.addWorker(w3);
        player2.addWorker(w4);

        game.startTurn();

        game.getTurnState().selectWorker(game, player1, w1);
        game.getTurnState().selectSquare(game, player1, new Point(2, 1));

        assertTrue(game.getTurnState() instanceof Build);
        assertFalse(game.getTurnState().getValidMoves(game, player1, w1).contains(w2.getPosition()));
        assertFalse(game.getTurnState().getValidMoves(game, player1, w1).contains(w3.getPosition()));
    }
}