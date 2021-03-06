package it.polimi.ingsw.psp1.santorini.view;

import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.observer.ModelObserver;
import it.polimi.ingsw.psp1.santorini.observer.Observable;
import it.polimi.ingsw.psp1.santorini.observer.ViewObserver;

/**
 * Shows changing of the game
 * Communicates with controller
 */
public abstract class View extends Observable<ViewObserver> implements ModelObserver {

    private final Player player;

    /**
     * Creates a new view object with the player associated with the view
     *
     * @param player associated with the view
     */
    public View(Player player) {
        this.player = player;
    }

    /**
     * Called when an error has to be notified
     *
     * @param error String with the error
     */
    public abstract void notifyError(String error);

    public Player getPlayer() {
        return player;
    }
}
