import javax.swing.*;
import java.awt.*;

// JPanel that is drawn on
class Search extends JPanel {
    //DrawMaze d = new DrawMaze();
    public int height = 20;
    public int width = 20;
    Card[][] maze = new Card[width][height];   // Card matrix to be used in making game interface

    Search() {
        setBackground(Color.black);
        MakeGrid();
    }

    // initialize grid of cells, all with borders
    public void MakeGrid() {
        for (int i = 0; i < width; i++)                      // fill grid with cards
            for (int j = 0; j < height; j++) {
                Card card = new Card();
                maze[i][j] = card;
                card.x = i;
                card.y = j;
            }
        maze[0][0].ID[3] = false;                   // start and end points
        maze[width - 1][height - 1].ID[2] = false;
    } // end MakeGrid()

    // reset visited cards
     public void resetVisited(){
         for (int i = 0; i < width; i++)
             for (int j = 0; j < height; j++) {
                 maze[i][j].visited = 0;
             }

     }

}


