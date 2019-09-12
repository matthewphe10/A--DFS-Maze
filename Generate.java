import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.IntStream;

// JFrame with two jpanels on it, one is a grid and the other is a control interface
// also handles logic for generating maze and solving it
public class Generate extends JFrame implements Runnable {
    private int sleepTime = 1000;
    private DrawMaze d = new DrawMaze();
    private DrawMaze A = new DrawMaze();
    private boolean isRunning = false;
    private boolean solveMaze = false;
    private boolean dogenerate = false;
    private boolean doSolve = false;
    private boolean start = false;
    private JLabel perc = new JLabel("Visited: " + 0 + "%");
    private JLabel perctwo = new JLabel("Visited: " + 0 + "%");
    private JLabel status = new JLabel("Status: ");
    private boolean Astarfini = false;
    private boolean DFSfini = false;

    public Generate() {
        // initialize JFrame settings
        init();
        // control panel
        JPanel pane = new JPanel();
        // make items vertically stacked, with gray background
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBackground(Color.gray);
        // labels for each slider
        JLabel speed = new JLabel("     Rendering Speed");
        // Allows user to see generation or not
        JCheckBox generate_check = new JCheckBox("Show Generation ");
        generate_check.setBackground(Color.gray);
        generate_check.addItemListener(e -> dogenerate = !dogenerate);
        // allows user to begin generation of maze
        pane.add(new JLabel(" "));
        JButton generator = new JButton("Generate");
        generator.addActionListener((ActionEvent e) -> {
            if(!isRunning) {
                (new Thread(this)).start();
                isRunning = !isRunning;
            }
        }); pane.add(generator, BorderLayout.EAST); pane.add(generate_check);

        //JLabel for control interface spacing
        pane.add(new JLabel(" "));

        // allows user to see solving process or not
        JCheckBox solve_check = new JCheckBox("Show Solving ");
        solve_check.setBackground(Color.gray);
        solve_check.addItemListener(e -> doSolve = !doSolve);
        // button that begins solving process
        JButton solve = new JButton("Solve");
        solve.addActionListener((ActionEvent e) -> {
            if(!solveMaze && !d.end && !A.end && !isRunning) {   // so button will not start multiple threads
                solveMaze = true;                      // indicate maze solving has started
                (new Thread(this)).start();
            }
        }); pane.add(solve, BorderLayout.EAST); pane.add(solve_check);
        pane.add(new JLabel(" "));
        // stops generation or solving
        JButton stop_start = new JButton("Start/Stop");
        stop_start.addActionListener((ActionEvent e) -> {
            if(isRunning || solveMaze) {
                start = !start;
                isRunning = true;
                d.drawn = false;
                A.drawn = false;
                solveMaze = true;
            }
        }); pane.add(stop_start, BorderLayout.EAST);
        // space for formatting
        pane.add(new JLabel(" "));
        add(pane, BorderLayout.EAST);
        // slider to adjust column number
        // button to apply changes made to resolution, reset state of interface
        JButton apply = new JButton("Apply Changes/Reset");
        apply.addActionListener((ActionEvent e) -> {
            resetMaze();
        }); pane.add(apply, BorderLayout.EAST);
        // JLabel for spacing
        pane.add(new JLabel(" "));
        JPanel speedpan = new JPanel();
        speedpan.setBackground(Color.gray);
        // allows user to adjust speed of generation or solving
        speedpan.add(speed);
        JSlider sleep = new JSlider(JSlider.VERTICAL, 700, 998, 700);
        sleep.setValue(0);
        sleep.setBackground(Color.gray);
        sleep.addChangeListener((ChangeEvent e) -> {
            JSlider source = (JSlider) e.getSource();
            if(source.getValueIsAdjusting()) {    // if value is being adjusted
                sleepTime = 1000 - source.getValue() ;    // as slider increases, wait time decreases
            }
            if (!source.getValueIsAdjusting()) {  // once adjustments have stopped
                sleepTime = 1000 - source.getValue() ;    // as slider increases, wait time decreases
            }
        });speedpan.add(sleep); this.add(speedpan);

        // percent visited and status indicators
        pane.add(new JLabel("DFS: "));
        pane.add(perc);
        pane.add(new JLabel(" "));
        pane.add(new JLabel("A*: "));
        pane.add(perctwo);
        pane.add(status);
    }
    // helper function for constructor
    private void init() {
        setTitle("Maze");
        this.setLayout(new GridLayout(2,2));
        setBackground(Color.black);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 700);
        add(d);
        A.offset = 1;
        add(A);
    }

    // logic for generating and solving
    public void run() {
            // generate if not already generated
            if (!d.drawn) {
                status.setText("Status: generating maze");
                // pick a random point in maze
                int ri = (int) (Math.random() * (d.m.width - 1));
                int rj = (int) (Math.random() * (d.m.height - 1));
                // use a stack
                Stack<Card> stack = new Stack<>();

                // add random point to maze and indicate it has been visited
                stack.push(d.m.maze[ri][rj]);
                d.m.maze[ri][rj].visited++;
                // generate maze
                while (!stack.empty()) {
                    status.setText("Status: generating maze...");
                    // if user hasn't indicated to stop generation
                    if(!start) {
                        // random directions to go
                        ArrayList<Integer> selection = rand();
                        // get previously added cell from stack
                        Card temp = stack.pop();
                        // save x and y coordinates
                        int ti = temp.x;
                        int tj = temp.y;
                        // iterate through random direction arraylist
                        IntStream.range(0, selection.size()).forEach(i -> {
                            switch (selection.get(i)) {
                                case 0: // right
                                    if (ti + 1 < d.m.width) {
                                        if (d.m.maze[ti + 1][tj].visited == 0) {
                                            // break right wall
                                            d.m.maze[ti][tj].ID[2] = false;
                                            // break left wall of cell to right so it doesn't write over right wall
                                            d.m.maze[ti + 1][tj].ID[3] = false;
                                            // add next cell to stack
                                            stack.push(d.m.maze[ti + 1][tj]);
                                            // indicate it has been visited
                                            d.m.maze[ti + 1][tj].visited++;
                                            // update
                                            //d.repaint();
                                        }
                                    }
                                    break;
                                case 1: // left
                                    if (ti - 1 >= 0) {
                                        if (d.m.maze[ti - 1][tj].visited == 0) {
                                            // break left wall
                                            d.m.maze[ti][tj].ID[3] = false;
                                            // break right wall of left cell so it doesn't cover up broken left
                                            d.m.maze[ti - 1][tj].ID[2] = false;
                                            // add to stack
                                            stack.push(d.m.maze[ti - 1][tj]);
                                            // inc visited
                                            d.m.maze[ti - 1][tj].visited++;
                                            //d.repaint();
                                        }
                                    }
                                    break;
                                case 2: // down
                                    if (tj + 1 < d.m.height) {
                                        if (d.m.maze[ti][tj + 1].visited == 0) {
                                            // break lower wall
                                            d.m.maze[ti][tj].ID[0] = false;
                                            // break upper wall of lower cell
                                            d.m.maze[ti][tj + 1].ID[1] = false;
                                            // push onto stack
                                            stack.push(d.m.maze[ti][tj + 1]);
                                            // inc visited
                                            d.m.maze[ti][tj + 1].visited++;
                                            //repaint();
                                        }
                                    }
                                    break;
                                case 3: // up
                                    if (tj - 1 >= 0) {
                                        if (d.m.maze[ti][tj - 1].visited == 0) {
                                            // break upper wall
                                            d.m.maze[ti][tj].ID[1] = false;
                                            // break lower wall of upper cell
                                            d.m.maze[ti][tj - 1].ID[0] = false;
                                            stack.push(d.m.maze[ti][tj - 1]);
                                            d.m.maze[ti][tj - 1].visited++;
                                            //repaint();
                                        }
                                    }
                                    break;
                            }
                            for (int w = 0; w < d.m.width; w++) {
                                for (int c = 0; c < d.m.height; c++) {
                                    A.m.maze[w][c].x = w;
                                    A.m.maze[w][c].y = c;
                                    for(int r = 0; r < A.m.maze[w][c].ID.length; r++)
                                        A.m.maze[w][c].ID[r] = d.m.maze[w][c].ID[r];
                                       // d.m.maze[w][c].ID[r] = false;
                                }
                            }
                            repaint();
                            // if user has selected to see generation, generate at indicated speed
                            if (dogenerate) {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                }
                // update state
                isRunning = false;
                d.drawn = true;
                status.setText("Status: maze generated");
            }

            if (solveMaze) {
                status.setText("Status: solving maze...");
                // arraylist of previous points, for backtracking
                ArrayList<Point> saved = new ArrayList<>();
                // stack for search algorithm
                Stack<Card> stack = new Stack<>();
                // make new priority queue
                Comparator<Card> comparator = new nodeCompare();
                PriorityQueue<Card> nodes = new PriorityQueue<>(10, comparator);
                // initialize and add starting cell
                A.resetVisited();
                A.m.maze[0][0].visited++;
                A.path.add(new Point(10 + (d.superSize / 2), 10 + (d.superSize / 2)));
                A.concurrent_path.add(new Point(0,0));
                A.m.maze[0][0].manhattanDistance = Manhattan_Distance(A.m.maze[0][0], A.m.maze[A.width - 1][A.height - 1]);
                A.m.maze[0][0].parent = null;
                nodes.add(A.m.maze[0][0]);
                // reset visited indicators from grid creation
                d.m.resetVisited();
                // add start point to path
                stack.push(d.m.maze[0][0]);
                // indicate start point has been visited
                d.m.maze[0][0].visited++;
                // add start point to drawn path
                d.path.add(new Point(10 + (d.superSize / 2), 10 + (d.superSize / 2)));
                // add start point to path represented in grid coordinates
                d.concurrent_path.add(new Point(0,0));
                // placeholder points for current point on grid
                int ti, tj;
                // draw while openings can be found in maze
                int tempi = 0;
                int tempj = 0;
                A.Astar = true;
                while (!stack.empty() || !nodes.isEmpty()) {
                    // kill thread
                    if(!solveMaze)
                        break;
                    // if user hasn't toggled start/stop button to on
                    if(!start) {
                        // container for dequeued card
                        Card t ;
                        if(!nodes.isEmpty() && !Astarfini) {
                            // take node with lowest heuristic value from priority queue
                            t = nodes.remove();
                            // save the coordinates of this node
                            tempi = t.x; tempj = t.y;
                            // update the drawn path
                            update_path(A.path, A.concurrent_path, tempi, tempj);
                            // check if at end
                            if (tempi == A.width - 1 && tempj == A.height - 1) {
                                A.end = true;
                                //solveMaze = false;
                                repaint();
                                Astarfini = true;
                                //break;
                            }
                            // check right
                            if (tempi + 1 < d.width && !d.m.maze[tempi][tempj].ID[2]) {
                                A.m.maze[tempi + 1][tempj].manhattanDistance = Manhattan_Distance(A.m.maze[tempi + 1][tempj], A.m.maze[A.width - 1][A.height - 1]);
                                ASolve(nodes, tempi, tempj, tempi + 1, tempj);
                            }
                            // check left
                            if (tempi - 1 >= 0 && !d.m.maze[tempi][tempj].ID[3]) {
                                A.m.maze[tempi - 1][tempj].manhattanDistance = Manhattan_Distance(A.m.maze[tempi - 1][tempj], A.m.maze[A.width - 1][A.height - 1]);
                                ASolve(nodes, tempi, tempj, tempi - 1, tempj);
                            }
                            // check down
                            if (tempj + 1 < A.height && !d.m.maze[tempi][tempj].ID[0]) {
                                A.m.maze[tempi][tempj + 1].manhattanDistance = Manhattan_Distance(A.m.maze[tempi][tempj + 1], A.m.maze[A.width - 1][A.height - 1]);
                                ASolve(nodes, tempi, tempj, tempi, tempj + 1);
                            }
                            // check up
                            if (tempj - 1 >= 0 && !d.m.maze[tempi][tempj].ID[1]) {
                                A.m.maze[tempi][tempj - 1].manhattanDistance = Manhattan_Distance(A.m.maze[tempi][tempj - 1], A.m.maze[A.width - 1][A.height - 1]);
                                ASolve(nodes, tempi, tempj, tempi, tempj - 1);
                            }
                        }

                        // random directions to check
                        ArrayList<Integer> selection = rand();
                        // get first point to test
                        Card temp = new Card();
                        if(!stack.empty())
                            temp = stack.pop();

                        // save point's values
                        ti = temp.x;
                        tj = temp.y;
                        // check if at end
                        if (ti == d.m.width - 1 && tj == d.m.height - 1) {
                            d.end = true;
                            //solveMaze = false;
                            repaint();
                            DFSfini = true;
                           // break;
                        }

                        if(!DFSfini) {
                            // check all directions until valid path is found, or backtrack if only option is backwards
                            for (int i = 0; i < selection.size(); i++) {

                                // right wall rule, ***uncomment for enhanced DFS
                                if (ti + 1 >= d.m.width && !d.m.maze[ti][tj].ID[0] && tj + 1 < d.m.height)
                                    if (mazeSolver(saved, stack, ti, tj, ti, tj + 1)) break;

                                // left wall rule
                                if (ti - 1 < 0 && !d.m.maze[ti][tj].ID[0] && tj + 1 < d.m.height)
                                    if (mazeSolver(saved, stack, ti, tj, ti, tj + 1)) break;

                                // prioritize right
                                if (ti + 1 < d.m.width && !d.m.maze[ti][tj].ID[2])
                                    if (mazeSolver(saved, stack, ti, tj, ti + 1, tj)) break;

                                // move right )
                                if (selection.get(i) == 0)
                                    if (ti + 1 < d.m.width && !d.m.maze[ti][tj].ID[2])
                                        if (mazeSolver(saved, stack, ti, tj, ti + 1, tj)) break;

                                // move left
                                if (selection.get(i) == 1)
                                    if (ti - 1 >= 0 && !d.m.maze[ti][tj].ID[3])
                                        if (mazeSolver(saved, stack, ti, tj, ti - 1, tj)) break;

                                // move down
                                if (selection.get(i) == 3)
                                    if (tj + 1 < d.m.height && !d.m.maze[ti][tj].ID[0])
                                        if (mazeSolver(saved, stack, ti, tj, ti, tj + 1)) break;

                                // move up
                                if (selection.get(i) == 2)
                                    if (tj - 1 >= 0 && !d.m.maze[ti][tj].ID[1])
                                        if (mazeSolver(saved, stack, ti, tj, ti, tj - 1)) break;

                                // backtrack
                                if (d.path.size() >= 2 && i == 3) {
                                    if (saved.size() >= 1) {
                                        // indicate visited again
                                        d.m.maze[ti][tj].visited++;
                                        // push previous point
                                        stack.push(d.m.maze[saved.get(saved.size() - 1).x][saved.get(saved.size() - 1).y]);
                                        // draw backwards, now in new color
                                        d.path.add(new Point(10 + saved.get(saved.size() - 1).x * d.superSize + (d.superSize / 2)
                                                , 10 + saved.get(saved.size() - 1).y * d.superSize + (d.superSize / 2)));
                                        d.concurrent_path.add(new Point(saved.get(saved.size() - 1).x, saved.get(saved.size() - 1).y));
                                        // remove last previous point so backtracking
                                        // can occur again if needed
                                        saved.remove(saved.size() - 1);
                                    }
                                }
                            }
                        }
                        repaint();
                        // speed at which maze is solved
                        if (doSolve) {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // update percentage
                    perc.setText("Visited: " + d.getPerc() + "%");
                    perctwo.setText("Visited: " + A.getPerc() + "%");
                }
                // indicate maze has been solved
                status.setText("Status: maze solved");
                solveMaze = false;
            }
    }

    // helper function so run() method isn't so long with similar code (this is for DFS)
    private boolean mazeSolver(ArrayList<Point> saved, Stack<Card> stack, int ti, int tj, int i, int j) {
        if (d.m.maze[i][j].visited == 0) {
            // add point to path
            d.path.add(new Point(10 + d.m.maze[i][j].x * d.superSize + (d.superSize / 2)
                    , 10 + d.m.maze[i][j].y * d.superSize + (d.superSize / 2)));
            // add x and y equivalent too
            d.concurrent_path.add(new Point(i, j));
            // indicate visited
            d.m.maze[i][j].visited++;
            // push point onto stack
            stack.push(d.m.maze[i][j]);
            // save point in case we need to backtrack
            saved.add(new Point(ti, tj));
            // update path
            d.repaint();
            return true;
        }
        return false;
    }


    // helper function so run() method isn't so long with similar code (this is for A* algorithm)
    private void ASolve(PriorityQueue<Card> n, int ti, int tj, int i, int j) {
        if (A.m.maze[i][j].visited == 0) {
            // indicate visited
            A.m.maze[i][j].visited++;
            A.m.maze[i][j].parent.x = ti;
            A.m.maze[i][j].parent.y = tj;

            A.m.maze[tj][ti].child.x = i;
            A.m.maze[tj][ti].child.y = j;
            // save path
            n.add(A.m.maze[i][j]);
            // update path
            A.repaint();
        }
    }

    // helper function to apply changes/reset maze
    private void resetMaze(){
        DFSfini = false;
        isRunning = false;
        solveMaze = false;
        d.resetDraw();
        d.repaint();
        A.resetDraw();
        A.repaint();
        start = false;
        perc.setText("Percent Visited: " + 0);
        status.setText("Status: ");
        Astarfini = false;
    }

    // generates four random directions
    private ArrayList<Integer> rand(){
        ArrayList<Integer> ret = new ArrayList<>();
        for(int i = 0; i < 4; i++)
            ret.add(i);
        Collections.shuffle(ret);
        return ret;
    }

    // my heuristic choice for A* since there are four path choices
    private int Manhattan_Distance(Card one, Card two){
        return (Math.abs(two.x - one.x)) + (Math.abs(two.y - one.y));
    }


    public class nodeCompare implements Comparator<Card>
    {
        public int compare(Card x, Card y)
        {
            // Comparator argument for priority Queue
            // Without this, it could not determine and sort values correctly for Card objects
            if (x.manhattanDistance < y.manhattanDistance)
            {
                return -1;
            }
            if (x.manhattanDistance > y.manhattanDistance)
            {
                return 1;
            }
            return 0;
        }
    }

    void update_path(ArrayList<Point> p, ArrayList<Point> pconcurr, int x, int y){
        p.clear();
        pconcurr.clear();
        Point curr = new Point(x, y);
        while (curr != null){
            pconcurr.add(curr);
            p.add(new Point(10 + curr.x * A.superSize + (A.superSize / 2)
                    , 10 + curr.y * A.superSize + (A.superSize / 2)));
            curr = A.m.maze[curr.x][curr.y].parent;
        }
    }
}