import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

// class to draw grid and path
public class DrawMaze extends Search {
    // new jpanel to write on
    public Search m = new Search();
    public Search w = new Search();
    // original cell size
    public int superSize = 15;
    boolean Astar = false;

    int offset = 0;
    // indicates if grid is drawn
    boolean drawn = false;
    // indicates if solver is at the end
    boolean end = false;
    // path to be saved and redrawn (coordinates are based on screen coords for drawing)
    ArrayList<Point> path = new ArrayList<>();
    // path converted to 2d matrix coordinates
    ArrayList<Point> concurrent_path = new ArrayList<>();

    private void draw(Graphics g){
        Graphics2D s = (Graphics2D)g;
        // draw red line out of maze if it is at the end
        if(end){
            s.setStroke(new BasicStroke(4));
            s.setColor(Color.red);
            s.drawLine(10 + m.maze[m.width - 1][m.height - 1].x * superSize + (superSize / 2),
                       10 + m.maze[m.width - 1][m.height - 1].y * superSize + (superSize / 2),
                       10 + m.maze[m.width - 1][m.height - 1].x * superSize + (superSize / 2) + 22,
                       10 + m.maze[m.width - 1][m.height - 1].y * superSize + (superSize / 2));
        }
        makeGrid(s);
    }

    private void makeGrid(Graphics2D s) {
        // iterate through grid
        for (int i = 0; i < m.width; i++) {
            int currx = i * superSize + 10 + offset;   // current x coordinate (based on point on screen)
            for (int j = 0; j < m.height; j++) {
                int curry = j * superSize + 10;  // current y coordinate (based on point on screen)
                s.setColor(Color.lightGray);
                s.setStroke(new BasicStroke(1));
                if (m.maze[i][j].ID[0])
                    s.drawLine(currx, curry + superSize, currx + superSize, curry + superSize); // bottom grid line
                if (m.maze[i][j].ID[1])
                    s.drawLine(currx, curry, currx + superSize, curry);   // top
                if (m.maze[i][j].ID[2])
                    s.drawLine(currx + superSize, curry, currx + superSize, curry + superSize);     // right
                if (m.maze[i][j].ID[3])
                    s.drawLine(currx, curry, currx, curry + superSize);       // left
                if(Astar)
                    if (m.maze[i][j].parent != null && m.maze[i][j].child != null)
                        if (m.maze[i][j].parent.x != -1 && m.maze[j][j].child.x == -1) {
                            s.setColor(Color.CYAN);
                            s.setStroke(new BasicStroke(3));
                            s.drawLine(10 + i * superSize + (superSize / 2)
                                    , 10 + j * superSize + (superSize / 2), 10 + (i) * superSize + (superSize / 2)
                                    , 10 + (j) * superSize + (superSize / 2));     // right
                        }
            }
        }
        // if a path exists, draw it
        if(!path.isEmpty()){
            // larger stroke and different color than grid lines
            s.setStroke(new BasicStroke(4));
            s.setColor(Color.green);
            // draw from middle of cell
            s.drawLine((superSize / 2) - 8 , 10 + (superSize / 2) , 10 + (superSize / 2), (10 + superSize / 2));
            // begin drawing path
            for(int i = 0; i < path.size(); i++){
                s.setColor(Color.cyan);
                s.setStroke(new BasicStroke(3) );
                // backtracking, draw in new color
                if(i + 1 < path.size() && i + 1 < concurrent_path.size()) {
                    if(m.maze[concurrent_path.get(i).x][concurrent_path.get(i).y].visited > 1
                    && m.maze[concurrent_path.get(i + 1).x][concurrent_path.get(i + 1).y].visited >= 1) {
                        s.setColor(Color.magenta);
                        s.drawLine(path.get(i).x, path.get(i).y, path.get(i + 1).x, path.get(i + 1).y);
                    }
                    // else, draw in normal color
                    else{
                        s.setColor(Color.cyan);
                        s.drawLine(path.get(i).x, path.get(i).y, path.get(i + 1).x, path.get(i + 1).y);
                    }
                }
                // covers case of only a point needing to be drawn (ie the size of the path is odd)
                else if(i < path.size() && i < concurrent_path.size()) {
                    if (m.maze[concurrent_path.get(i).x][concurrent_path.get(i).y].visited > 1) {
                        s.setColor(Color.MAGENTA);
                        s.drawLine(path.get(i).x, path.get(i).y, path.get(i).x, path.get(i).y);
                    } else {
                        s.setColor(Color.cyan);
                        s.drawLine(path.get(i).x, path.get(i).y, path.get(i).x, path.get(i).y);
                    }
                }

            }
        }
    }

    // helper function to reset grid, path if new dimensions are chosen
    public void resetDraw(){
        // reset state variables and paths
        drawn = false;
        end = false;
        path.clear(); concurrent_path.clear();
        m.maze = new Card[m.width][m.height];   // Card matrix to be used in making game interface
        m.MakeGrid();

        w.maze = new Card[m.width][m.height];   // Card matrix to be used in making game interface
        w.MakeGrid();


    }

    // called on repaint()
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    // gets the percentage of visited cells and returns it in "###.##" format
    String getPerc() {
        double count = 0;
        for (int i = 0; i < m.width; i++) {
            for (int j = 0; j < m.height; j++) {
                if(m.maze[i][j].visited > 0)
                    count++;
            }
        }

        DecimalFormat format = new DecimalFormat("###.##");
        return format.format((count / ((double)m.width * (double)m.height)) * 100);
    }
}




