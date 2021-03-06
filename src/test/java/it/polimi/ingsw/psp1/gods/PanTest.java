package it.polimi.ingsw.psp1.gods;

import it.polimi.ingsw.psp1.santorini.model.Game;
import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Pan;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PanTest {

    private Game game;
    private Player player;

    @Before
    public void setup() {
        this.game = new Game("1", 2);
        this.player = new Player("p1");

        game.addPlayer(player);

        player.setPower(new Pan());
    }

    @After
    public void teardown() {
        for (int i = player.getWorkers().size() - 1; i >= 0; i--) {
            player.removeWorker(player.getWorkers().get(i));
        }
    }

    @Test
    public void onYourMove_normalBehaviour_shouldWinIfConditionsArePrompted() {
        Point oldPosition = new Point(1, 1);
        Point newPosition = new Point(1, 2);

        game.getMap().buildBlock(oldPosition, false);
        game.getMap().buildBlock(oldPosition, false);

        Worker w = new Worker(oldPosition);
        Worker w2 = new Worker(new Point(4, 4));

        player.addWorker(w);
        player.addWorker(w2);


        game.startTurn();

        game.getTurnState().selectWorker(game, player, w);
        game.getTurnState().selectSquare(game, player, newPosition);

        assertTrue(player.hasWon());
    }
}