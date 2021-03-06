package it.polimi.ingsw.psp1.santorini.cli;

import it.polimi.ingsw.psp1.santorini.model.map.GameMap;
import it.polimi.ingsw.psp1.santorini.model.map.Point;
import it.polimi.ingsw.psp1.santorini.model.map.Worker;
import it.polimi.ingsw.psp1.santorini.model.powers.Power;
import it.polimi.ingsw.psp1.santorini.network.packets.EnumTurnState;
import it.polimi.ingsw.psp1.santorini.network.packets.server.PlayerData;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.polimi.ingsw.psp1.santorini.cli.Color.*;

/**
 * Support class for the CLI
 * The functions allow console printing with colors and more ease of use
 */
public class PrintUtils {

    private static final int MAX_LENGTH = 150;
    private static final int SIZE = 3;
    private static final int SPACING = 1;

    private static final int MAP_X = 1;
    private static final int MAP_Y = 5;

    /**
     * Makes the class not instantiable
     */
    private PrintUtils() {
    }

    /**
     * Prints map's background in console
     */
    public static void printMapBackground() {
        StringBuilder bgLine = new StringBuilder();
        IntStream.range(0, GameMap.SIDE_LENGTH * ((SIZE * 2) + (SPACING * 2 - 1)))
                .forEach(i -> bgLine.append(" "));

        for (int i = 0; i < GameMap.SIDE_LENGTH * (SIZE + SPACING) + 1; i++) {
            print(BG_GRASS2 + bgLine.toString(), MAP_X, MAP_Y + i, false);
        }
    }

    /**
     * Prints workers in the correct position on map in console
     *
     * @param players  list of players from where to get workers for each player
     * @param colorMap worker's color
     */
    public static void printWorkers(List<PlayerData> players, Map<String, Color> colorMap) {
        for (PlayerData player : players) {
            for (int i = 0; i < player.getWorkers().size(); i++) {
                Worker w = player.getWorkers().get(i);

                int x = MAP_X + 2 + w.getPosition().x * (SIZE * 2 + SPACING * 2 - 1);
                int y = MAP_Y + 2 + w.getPosition().y * (SIZE + SPACING);

                String string = colorMap.get(player.getName()) + "W" + (i + 1);

                print(string, x, y, false);
            }
        }
    }

    /**
     * When a worker moves into another place, prints an arrow to highlight his movement
     *
     * @param arrow type
     * @param where to print arrow
     */
    public static void printArrow(EnumArrow arrow, Point where) {
        int x = MAP_X + where.x * (SIZE * 2 + SPACING * 2 - 1);
        int y = MAP_Y + where.y * (SIZE + SPACING);

        //TODO: fix swap on UP (and/or other directions)

        //TODO: Redo entirely

        if (arrow.getVector().x == 1) {
            x += SIZE * 2;
        } else if (arrow.getVector().x == 0) {
            x += SIZE;
        }

        if (arrow.getVector().y == 1) {
            y += SIZE + 1;
        } else if (arrow.getVector().y == 0) {
            y += Math.ceil(SIZE / 2D);
        }

        if(arrow == EnumArrow.UP) {
            x -= 1;
        }

        print(arrow.toUnicode(), x, y, false);
    }

    /**
     * Prints the current map in console whenever it changes
     *
     * @param map that must be printed in console
     */
    public static void printMap(GameMap map) {
        if (map == null) {
            return;
        }

        String s = String.format("%" + (SIZE * 2 - 1) + "s", "");

        for (int i = 0; i < GameMap.SIDE_LENGTH; i++) {
            for (int j = 0; j < GameMap.SIDE_LENGTH; j++) {
                Point point = new Point(i, j);
                int level = map.getLevel(point) - (map.hasDome(point) ? 1 : 0);

                int x = MAP_X + i * (SIZE * 2) + i + SPACING;
                int y = MAP_Y + j * SIZE + j * SPACING + SPACING;

                for (int k = 0; k < SIZE; k++) {
                    print(getColorFromLevel(level) + s, x, y + k, false);
                }

                print(BG_GRASS2 + BLACK.toString() + "L" + level, x + SIZE * 2 - 3, y + SIZE - 1, false);

                if (map.hasDome(point)) {
                    for (int k = 1; k < SIZE - 1; k++) {
                        String string = BG_BRIGHT_BLUE + s.substring(4);

                        print(string, x + 2, y + k, false);
                    }
                }
            }
        }
    }

    /**
     * Prints the winning result at the end of the game
     */
    public static void printWin() {
        String c = BG_BRIGHT_BLUE.toString() + BLUE.toString();

        print(c + "                                                           ", 6, 9, false);
        print(c + "  ██╗   ██╗██╗ ██████╗████████╗ ██████╗ ██████╗ ██╗   ██╗  ", 6, 10, false);
        print(c + "  ██║   ██║██║██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗╚██╗ ██╔╝  ", 6, 11, false);
        print(c + "  ██║   ██║██║██║        ██║   ██║   ██║██████╔╝ ╚████╔╝   ", 6, 12, false);
        print(c + "  ╚██╗ ██╔╝██║██║        ██║   ██║   ██║██╔══██╗  ╚██╔╝    ", 6, 13, false);
        print(c + "   ╚████╔╝ ██║╚██████╗   ██║   ╚██████╔╝██║  ██║   ██║     ", 6, 14, false);
        print(c + "    ╚═══╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝   ╚═╝     ", 6, 15, false);
        print(c + "                                                           ", 6, 16, false);
    }

    /**
     * Prints the losing result at the end of the game
     */
    public static void printLoser() {
        String c = BG_DARK_RED.toString() + RED.toString();

        print(c + "                                                          ", 6, 9, false);
        print(c + "     ██████╗ ███████╗███████╗███████╗ █████╗ ████████╗    ", 6, 10, false);
        print(c + "     ██╔══██╗██╔════╝██╔════╝██╔════╝██╔══██╗╚══██╔══╝    ", 6, 11, false);
        print(c + "     ██║  ██║█████╗  █████╗  █████╗  ███████║   ██║       ", 6, 12, false);
        print(c + "     ██║  ██║██╔══╝  ██╔══╝  ██╔══╝  ██╔══██║   ██║       ", 6, 13, false);
        print(c + "     ██████╔╝███████╗██║     ███████╗██║  ██║   ██║       ", 6, 14, false);
        print(c + "     ╚═════╝ ╚══════╝╚═╝     ╚══════╝╚═╝  ╚═╝   ╚═╝       ", 6, 15, false);
        print(c + "                                                          ", 6, 16, false);
    }

    /**
     * Prints an object (implicit toString call)
     *
     * @param o generic object
     */
    public static synchronized void print(Object o) {
        System.out.print(o);
        System.out.flush();
    }

    /**
     * Used to clear console before using it
     */
    public static void firstClear() {
        print("\033[H\033[3J\033[2J");
    }

    /**
     * Used to clear console while game is running
     */
    public static void clear() {
        for (int i = 0; i < 150; i++) {
            clearRow(0, i);
        }
    }

    /**
     * Used to set cursor in a generic position in console
     *
     * @param x Column number
     * @param y Row number
     */
    public static void setCursor(int x, int y) {
        print(String.format("\033[%d;%dH", y, x));
    }

    /**
     * Used to set cursor in a standard position in console
     */
    public static void resetCursor() {
        Point point = getCommandCoords();
        setCursor(point.x + 3, point.y);
    }

    /**
     * Used to clear a row in console
     *
     * @param x column number
     * @param y row number
     */
    public static void clearRow(int x, int y) {
        setCursor(x, y);

        print("\033[K");
    }

    /**
     * Used to print a string in console
     *
     * @param string  string that must be printed in console
     * @param x       column number
     * @param y       row number
     * @param toClean true if the row must be deleted before printing
     */
    public synchronized static void print(String string, int x, int y, boolean toClean) {
        if (toClean) {
            clearRow(x, y);
        }

        print(RESET);

        setCursor(x, y);

        print(string + RESET);

        resetCursor();
    }

    /**
     * Prints a String given an offset from the line where the user types commands
     *
     * @param string  string to print
     * @param xOff    x offset
     * @param yOff    y offset
     * @param toClear true if the console needs to be cleaned
     */
    public static void printFromCommand(String string, int xOff, int yOff, boolean toClear) {
        Point point = getCommandCoords();
        print(string, point.x + xOff, point.y + yOff, toClear);
    }

    /**
     * Used to print command zone in console
     */
    public static void printCommand() {
        Point point = getCommandCoords();
        print("> ", point.x, point.y, true);
    }

    /**
     * @return command's coordinates
     */
    public static Point getCommandCoords() {
        return new Point(0, 4 + MAP_Y + GameMap.SIDE_LENGTH * (SIZE + SPACING) - SPACING);
    }

    private static int getOffset() {
        return GameMap.SIDE_LENGTH * ((SIZE + SPACING) * 2 - 1) + 10;
    }

    /**
     * Used to print the list of gods in console
     *
     * @param list list that must be printed in console
     */
    public static void printPowerList(List<Power> list) {
        int row = 5;

        print(!list.isEmpty() ? "Power list:" : "", getOffset(), 4, true);

        for (int i = MAP_Y; i < getCommandCoords().y - 3; i++) {
            clearRow(getOffset(), i);
        }

        for (int i = 0; i < list.size(); i++) {
            if (i % 3 == 0) {
                row++;
            }

            print((i + 1) + ") " + BLUE + list.get(i).getClass().getSimpleName(),
                    (i % 3) * 20 + getOffset(), row, false);
        }

        printCommand();
        resetCursor();
    }

    /**
     * Used to print players' info while gaming is running
     *
     * @param clientPlayer player name
     * @param list         players list
     * @param state        current state
     * @param colorMap     map color
     * @param interact     true if player can use the power of his god
     */
    public static void printPlayerInfo(String clientPlayer, List<PlayerData> list, EnumTurnState state,
                                       Map<String, Color> colorMap, boolean interact) {

        UnaryOperator<String> name = s -> colorMap.get(s) + s +
                (s.equals(clientPlayer) ? "(YOU)" : "") + RESET;

        clearRow(2, 1);
        clearRow(2, 2);
        clearRow(2, 3);

        PrintUtils.print("\uD83E\uDC7E Current player", 1, 1, true);

        for (int i = 0; i < list.size(); i++) {
            PlayerData data = list.get(i);

            print(name.apply(data.getName()), i * 20 + 2, 2, false);
            print(data.getPower() != null ? data.getPower().getClass().getSimpleName().toUpperCase() : "N/A",
                    i * 20 + 2, 3, false);
        }

        printCommand();
    }

    /**
     * Used to print the valid moves on the map
     *
     * @param valid   valid moves
     * @param blocked blocked moves
     */
    public static void printValidMoves(List<Point> valid, Map<Power, List<Point>> blocked) {
        int counter = 1;

        for (Point point : valid) {
            boolean isBlocked = blocked.values().stream()
                    .flatMap(Collection::stream).anyMatch(p -> p.equals(point));

            String s = String.valueOf(counter);
            print((isBlocked ? BG_DARK_RED : BG_BRIGHT_YELLOW) + "" + RED + s,
                    MAP_X + point.x * (SIZE * 2 - 1 + SPACING * 2) + 1,
                    MAP_Y + point.y * (SIZE + SPACING) + 1,
                    false);
            counter++;
        }
    }

    /**
     * Used to know which color must be used, because every level has different color
     *
     * @param level block's level
     * @return the color of the level
     */
    private static Color getColorFromLevel(int level) {
        switch (level) {
            case 1:
                return BG_GRAY1;
            case 2:
                return BG_GRAY2;
            case 3:
                return BG_GRAY3;
            default:
                return BG_GRASS;
        }
    }

    /**
     * Used to print gods description
     *
     * @param power power to print
     */
    public static void printPowerInfo(Power power) {
        List<String> desc = reduceInLines(power.getDescription(), 50);

        int yOffset = 14;

        for (int i = 0; i < 10; i++) {
            clearRow(getOffset(), yOffset + i);
        }

        print(RED + power.getName(), getOffset(), yOffset, true);
        print(BLUE + power.getAlias(), getOffset(), yOffset + 1, true);

        for (int i = 0; i < desc.size(); i++) {
            print(desc.get(i), getOffset(), i + yOffset + 2, true);
        }
    }

    /**
     * Given a String and a maximum width, splits the String in a list of Strings
     * with every one of them within the given length
     *
     * @param longString string to split if needed
     * @param width      maximum length of every string
     * @return the list with the string in multiple lines
     */
    private static List<String> reduceInLines(String longString, int width) {
        Deque<StringBuilder> list = new ArrayDeque<>();

        for (String s : longString.split(" ")) {
            if (list.isEmpty() || list.peek().length() + s.length() + 1 > width) {
                list.push(new StringBuilder());
            }

            if (list.peek() != null) {
                list.peek().append(s).append(" ");
            }
        }

        List<String> toReturn = list.stream().map(StringBuilder::toString).collect(Collectors.toList());
        Collections.reverse(toReturn);

        return toReturn;
    }
}
