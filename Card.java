import java.awt.*;

// each cell on grid with information on its coordinates, broken/intact walls, and number of times visited
public class Card {
    public boolean[] ID = {true,true,true,true};    // bottom, top, right, left
    int manhattanDistance = 0;
    // can be ignored since the distance between each cell is the same
    int heur_func = 0;
    // x, y coordinates of cell
    int x = 0;
    int y = 0;
    // indicates how many times a cell is visited
    int visited = 0;
    // for linked list path traversal
    Point parent = new Point(-1,-1);
    Point child = new Point(-1, -1);
}
