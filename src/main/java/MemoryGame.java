import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class MemoryGame implements ActionListener {
    private final JFrame frame = new JFrame("Memory Game");

    private final JPanel field = new JPanel();
    private final JPanel menu_top = new JPanel();
    private final JPanel menu_center = new JPanel();
    private final JPanel menu_bottom = new JPanel();

    private final JPanel start_screen = new JPanel();
    private final JPanel end_screen = new JPanel();
    private final JPanel instruction_screen = new JPanel();

    private final JButton[] btn = new JButton[20];
    private final JButton start = new JButton("Start");
    private final JButton over = new JButton("Exit");
    private final JButton easy = new JButton("Easy");
    private final JButton hard = new JButton("Hard");
    private final JButton instruction = new JButton("Instructions");
    private final JButton go_back = new JButton("Main Menu");

    private final Random random_generator = new Random();
    private boolean purgatory = false;
    private int level = 0; // liczba par do dopasowania
    private int score = 0; // todo trochę awkward bo im mniejszy tym lepszy ale nie mam czasu teraz tego poprawić
    private int total_cards = 0;
    private int pairs_left_to_match = 0;

    private String[] board;
    private Boolean shown = true;
    private int first_card = -1;
    private int second_card = -1;

    private final JTextField level_text_input = new JTextField(10);
    private final JTextArea instruction_text = new JTextArea("""
            When the game begins, the screen will be filled
            with pairs of buttons.
             Memorize their placement.
            Once you press any button, they will all clear.\s
             Your goal is to click the matching buttons in a row.
            When you finish that, you win.
            Every incorrect click gives you a point (those are bad).
             GOOD LUCK!\s
            for a single level: enter a level between 1 and 10,
            select easy or hard, then press start.""");

    // sql
    Connection connection = null;
    ArrayList<String> terms = new ArrayList<>();
    ArrayList<String> definitions = new ArrayList<>();
    HashMap<String, String> term_to_definition = new HashMap<>();
    HashMap<String, String> definition_to_term = new HashMap<>();

    public MemoryGame() {
        frame.setSize(500,300);
        frame.setLocation(500,300);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        start_screen.setLayout(new BorderLayout());
        menu_top.setLayout(new FlowLayout(FlowLayout.CENTER));
        menu_bottom.setLayout(new FlowLayout(FlowLayout.CENTER));
        menu_center.setLayout(new FlowLayout(FlowLayout.CENTER));

        start_screen.add(menu_top, BorderLayout.NORTH);
        start_screen.add(menu_center, BorderLayout.CENTER);
        start_screen.add(menu_bottom, BorderLayout.SOUTH);

        JLabel label = new JLabel("Enter level from 1 to 10");
        menu_top.add(label);
        menu_top.add(level_text_input);

        JPanel mini = new JPanel();
        mini.setLayout(new FlowLayout(FlowLayout.CENTER));
        menu_center.add(mini, BorderLayout.CENTER);

        mini.add(easy, BorderLayout.NORTH);
        easy.addActionListener(this);
        easy.setEnabled(true);

        mini.add(hard, BorderLayout.NORTH);
        hard.addActionListener(this);
        hard.setEnabled(true);

        mini.add(instruction, BorderLayout.SOUTH);
        instruction.addActionListener(this);
        instruction.setEnabled(true);

        start.addActionListener(this);
        start.setEnabled(true);
        menu_bottom.add(start);

        over.addActionListener(this);
        over.setEnabled(true);
        menu_bottom.add(over);

        frame.add(start_screen, BorderLayout.CENTER);
        frame.setVisible(true);

        // sql
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from glossary");
            while (rs.next()) {
                String term = rs.getString("term");
                String definition = rs.getString("definition");
                terms.add(term);
                definitions.add(definition);
                term_to_definition.put(term, definition);
                definition_to_term.put(definition, term);
            }
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch(SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void setUpGame(int pairs) {
        level = pairs;
        total_cards = 2 * pairs; // każda karta występuje dwa razy
        pairs_left_to_match = pairs;

        clearMain();

        board = new String[total_cards];

        for (int i = 0; i < total_cards; i++) {
            btn[i] = new JButton("");
            btn[i].setBackground(new Color(220, 220, 220));
            btn[i].addActionListener(this);
            btn[i].setEnabled(true);
            field.add(btn[i]);
        }

        String[] cards = new String[total_cards];
        int index = 0;
        for (int i = 0; i < pairs; i++) {
            cards[index++] = terms.get(i);
            cards[index++] = definitions.get(i);
        }

        for (int i = 0; i < total_cards; i++) {
            int random_place = random_generator.nextInt(total_cards);
            while (board[random_place] != null) {
                random_place = random_generator.nextInt(total_cards);
            }
            btn[random_place].setText(cards[i]);
            board[random_place] = cards[i];
        }

        createBoard();
    }

    public void hideField() { // this sets all the boxes blank
        for (int i = 0; i < total_cards; i++){
            btn[i].setText("");
        }
        shown = false;
    }

    public void switchSpot(int i) { // this will switch the current spot to either blank or what it should have
        if (!Objects.equals(board[i], "done")) { // when a match is correctly chosen, it will no longer switch when pressed
            if (Objects.equals(btn[i].getText(), "")){
                btn[i].setText(board[i]);
            } else {
                btn[i].setText("");
            }
        }
    }

    public void checkWin() { // checks if every spot is labeled as done
        if (pairs_left_to_match > 0) {
            return;
        }
        winner();
    }

    public void winner() {
        start_screen.remove(field);
        start_screen.add(end_screen, BorderLayout.CENTER);
        JTextField you_win = new JTextField("You Win");
        you_win.setEditable(false);
        end_screen.add(you_win, BorderLayout.NORTH);
        JTextField user_score = new JTextField("Score: " + score);
        user_score.setEditable(false);
        end_screen.add(user_score, BorderLayout.SOUTH);
        end_screen.add(go_back);
        go_back.setEnabled(true);
        go_back.addActionListener(this);
    }

    public void goToMainScreen() {
        new MemoryGame();
    }

    public void createBoard() { // this is just gui stuff to show the board
        field.setLayout(new BorderLayout());
        start_screen.add(field, BorderLayout.CENTER);

        field.setLayout(new GridLayout(5,4,2,2));
        field.setBackground(Color.black);
        field.requestFocus();
    }

    public void clearMain() { // clears the main menu so i can add the board or instructions
        start_screen.remove(menu_top);
        start_screen.remove(menu_center);
        start_screen.remove(menu_bottom);

        start_screen.revalidate();
        start_screen.repaint();
    }

    public void actionPerformed(ActionEvent click) {
        Object source = click.getSource();

        if (purgatory) {
            score++;
            switchSpot(first_card);
            switchSpot(second_card);
            first_card = -1;
            second_card = -1;
            purgatory = false;
        }

        if (source == start) { // start sets level and difficulty and calls method to set up game
            try {
                level = Integer.parseInt(level_text_input.getText());
            } catch (Exception e) {
                frame.dispose();
                goToMainScreen();// todo tu fajnie by było coś nieco bardziej sensownego robić, jakiś komunikat o błędzie
            }
            setUpGame(level);
        }

        if (source == over) { // quits
            System.exit(0);
        }

        if (source == instruction) { // this just sets the instruction screen
            clearMain();

            start_screen.add(instruction_screen, BorderLayout.NORTH);
            JPanel one = new JPanel();
            one.setLayout(new FlowLayout(FlowLayout.CENTER));
            JPanel two = new JPanel();
            two.setLayout(new FlowLayout(FlowLayout.CENTER));
            instruction_screen.setLayout(new BorderLayout());
            instruction_screen.add(one, BorderLayout.NORTH);
            instruction_screen.add(two, BorderLayout.SOUTH);

            instruction_text.setEditable(false);
            one.add(instruction_text);
            two.add(go_back);
            go_back.addActionListener(this);
            go_back.setEnabled(true);
        }

        if (source == go_back) { // back to main screen
            frame.dispose();
            goToMainScreen();
        }

        if (source == easy) { // sets the type. ex. if easy is clicked it turns blue and hard remains black
            easy.setForeground(Color.BLUE);
            hard.setForeground(Color.BLACK);
        } else if (source == hard) {
            hard.setForeground(Color.BLUE);
            easy.setForeground(Color.BLACK);
        }

        for (int i = 0; i < total_cards; i++) { // gameplay when a button is pressed
            if (source == btn[i]) {
                if (shown) {
                    hideField(); // if first time, hides field
                } else { // otherwise play
                    switchSpot(i);
                    if (first_card == -1) {
                        first_card = i;
                    } else {
                        // tu w sumie wystarczyłaby jedna mapa i sprawdzanie w obie strony nw jak jest czytelniej
                        if ((!Objects.equals(term_to_definition.get(board[first_card]), board[i]) &&
                             !Objects.equals(definition_to_term.get(board[first_card]), board[i])) ||
                             first_card == i) { // niepoprawne
                            second_card = i;
                            purgatory = true;
                        } else { // poprawne
                            // te done też można by jakoś ładniej ewentualnie kiedyś zrobić
                            board[i] = "done";
                            board[first_card] = "done";
                            pairs_left_to_match--;
                            checkWin();
                            first_card = -1;
                        }
                    }
                }
            }
        }
    }

}
	