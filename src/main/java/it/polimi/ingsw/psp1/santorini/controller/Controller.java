package it.polimi.ingsw.psp1.santorini.controller;

import it.polimi.ingsw.psp1.santorini.model.Game;
import it.polimi.ingsw.psp1.santorini.model.Player;
import it.polimi.ingsw.psp1.santorini.model.game.Play;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Power;
import it.polimi.ingsw.psp1.santorini.observer.ViewObserver;
import it.polimi.ingsw.psp1.santorini.view.View;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

public class Controller implements ViewObserver {

    private Game model;

    private Controller(Game model) {
        this.model = model;
    }

    @Override
    public void selectSquare(View view, Player player, Point location) {
        try {
            if (!(player.getGameState() instanceof Play)) {
                view.notifyError("Unsupported operation in this state");
                return;
            }

            //TODO: decouple player and turnstate ?
            player.getTurnState().selectSquare(location);
        } catch (UnsupportedOperationException | ArrayIndexOutOfBoundsException |
                IllegalArgumentException ex) {
            view.notifyError(ex.getMessage());
        }
    }

    @Override
    public void selectWorker(View view, Player player, Worker worker) {
        try {
            if (!(player.getGameState() instanceof Play)) {
                view.notifyError("Unsupported operation in this state");
                return;
            }

            player.getTurnState().selectWorker(worker);
        } catch (UnsupportedOperationException | ArrayIndexOutOfBoundsException |
                IllegalArgumentException | NoSuchElementException ex) {
            view.notifyError(ex.getMessage());
        }
    }

    @Override
    public void toggleInteraction(View view, Player player) {
        try {
            if (!(player.getGameState() instanceof Play)) {
                view.notifyError("Unsupported operation in this state");
                return;
            }

            player.getTurnState().toggleInteraction();
        } catch (UnsupportedOperationException | ArrayIndexOutOfBoundsException |
                IllegalArgumentException ex) {
            view.notifyError(ex.getMessage());
        }
    }

    @Override
    public void selectPowers(View view, Player player, List<Power> powerList) {
        try {
            if (player.getGameState() instanceof Play) {
                view.notifyError("Unsupported operation in this state");
                return;
            }

            powerList.forEach(p -> player.getGameState().selectGod(p));
        } catch (UnsupportedOperationException ex) {
            view.notifyError(ex.getMessage());
        }
    }

    @Override
    public void undo(View view, Player player) {
        try {
            player.getGameState().undo();
        } catch (UnsupportedOperationException ex) {
            view.notifyError(ex.getMessage());
        }
    }
}
