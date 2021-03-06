package it.polimi.ingsw.psp1.santorini.model.powers;

import it.polimi.ingsw.psp1.santorini.model.Game;
import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.turn.Move;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Power: Apollo can move into an opponent worker's space by forcing their worker to the space your just vacated
 */
public class Apollo extends Mortal {

    /**
     * {@inheritDoc}
     * <p>
     * Blocks occupied by enemy worker are included in valid moves
     */
    @Override
    public List<Point> getValidMoves(Worker worker, Game game) {
        if (game.getTurnState() instanceof Move && worker != null) {
            Point wPos = worker.getPosition();
            List<Point> neighbors = game.getMap().getNeighbors(wPos);

            return neighbors.stream()
                    .filter(getStandardDomeCheck(game))
                    .filter(getStandardMoveCheck(worker, game))
                    .filter(getStandardWorkerCheck(game).or(enemyWorkerCheck(game)))
                    .collect(Collectors.toList());
        } else {
            return super.getValidMoves(worker, game);
        }
    }

    /**
     * {@inheritDoc}<br>
     * <p>
     * If worker moves on enemy worker, they are swapped.
     */
    @Override
    public void onMove(Player player, Worker worker, Point where, Game game) {
        if (player.equals(this.player)) {
            Optional<Worker> optWorker = game.getWorkerOn(where);

            if (optWorker.isPresent()) {
                Point oldPos = worker.getPosition();

                Optional<Player> opponent = game.getPlayerOf(optWorker.get());

                if(opponent.isPresent()) {
                    game.moveWorker(opponent.get(), optWorker.get(), oldPos);
                } else {
                    throw new IllegalStateException("Player of opponent worker not found");
                }
            }
        }

        super.onMove(player, worker, where, game);//normal moving behaviour
    }

    /**
     * {@inheritDoc}
     *
     * Checks if the only moves remaining are moves that swap workers, and the player then cannot build anywhere
     */
    @Override
    public boolean canCompleteValidTurn(Worker worker, Game game) {
        List<Point> performableMoves = getPerformableMoves(game, worker);

        if(performableMoves.size() == 0) {
            return false;
        }

        if(performableMoves.size() > 1) {
            return true;
        }

        Optional<Worker> enemyWorker = game.getWorkerOn(performableMoves.get(0));

        if(enemyWorker.isPresent()) {
            Point enemyPos = enemyWorker.get().getPosition();

            long performableBuilds = game.getMap().getNeighbors(enemyPos).stream()
                    .filter(getStandardDomeCheck(game))
                    .filter(getStandardWorkerCheck(game))
                    .count();

            return performableBuilds > 0;
        } else {
            return true;
        }
    }

    /**
     * Predicate that returns true if given position refers to a worker that's owned by an opponent
     *
     * @param game current game instance
     * @return enemyWorker predicate
     */
    private Predicate<Point> enemyWorkerCheck(Game game) {
        return p -> {
            Optional<Worker> optWorker = game.getWorkerOn(p);
            Optional<Player> optPlayer = optWorker.isPresent() ? game.getPlayerOf(optWorker.get()) : Optional.empty();

            return optPlayer.isPresent() && !optPlayer.get().equals(player);
        };
    }
}
