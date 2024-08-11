import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

class Question {
    private String questionText;
    private List<String> options;
    private int correctOption;

    public Question(String questionText, List<String> options, int correctOption) {
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedOption) {
        return selectedOption == correctOption;
    }
}

class Quiz {
    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;

    public Quiz(List<Question> questions) {
        this.questions = questions;
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < 10;
    }

    public Question getNextQuestion() {
        if (hasNextQuestion()) {
            return questions.get(currentQuestionIndex++);
        }
        return null;
    }

    public void checkAnswer(int selectedOption) {
       // if (hasNextQuestion()) {
            Question currentQuestion = questions.get(currentQuestionIndex - 1);
            if (currentQuestion.isCorrect(selectedOption)) {
                score++;
            }
       // }
    }

    public int getScore() {

        return score;
    }
}



class QuizGame {
    static Scanner scanner = new Scanner(System.in);
    static Connection conn;
    static String playerName;

    public static void main(String[] args) throws Exception {

        conn = DriverManager.getConnection("jdbc:mysql://localhost/quizgame", "root", "");

        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM questions");

        List<Question> questions = new ArrayList<>();

        while (rs.next()) {
            String questionText = rs.getString("question_text");
            List<String> options = new ArrayList<>();
            options.add(rs.getString("option1"));
            options.add(rs.getString("option2"));
            options.add(rs.getString("option3"));
            options.add(rs.getString("option4"));
            int correctOption = rs.getInt("correct_option");

            Question question = new Question(questionText, options, correctOption);
            questions.add(question);
        }

        Collections.shuffle(questions);
        char ch = 0;
        do {
            System.out.println("------------WELCOME TO QUIZ GAME----------------");
            System.out.println("1. START QUIZ");
            System.out.println("2. SCOREBOARD");
            System.out.println("3. EXIT");
            ch = scanner.nextLine().charAt(0);
            switch (ch) {
                case '1':
                    System.out.print("Enter Player Name : ");
                    playerName = scanner.nextLine();
                    startQuiz(playerName, questions);
                    scanner.nextLine();
                    break;
                case '2':
                    printScoreBoard();
                    break;
                case '3':
                    break;
                default:
                    System.out.println("Invalid input by user.Make sure your entered number is between 1 to 3");
            }
            
        } while (ch != '3');

        
        rs.close();
        stmt.close();
        conn.close();
    }

    public static void startQuiz(String name, List<Question> questions) {
        
        Quiz quiz = new Quiz(questions);
        int question_no = 1;
        while (quiz.hasNextQuestion()) {
            Question currentQuestion = quiz.getNextQuestion();
            System.out.println("Que." + question_no + "> " + currentQuestion.getQuestionText());
            question_no++;

            List<String> options = currentQuestion.getOptions();
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i));
            }

            System.out.print("Enter your answer (1-" + options.size() + "): ");
            do {

                try {
                    
                    int selectedOption = scanner.nextInt();
                    if(selectedOption > 4){
                        throw new ArithmeticException();
                    }
                    quiz.checkAnswer(selectedOption);
                    break;
                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    System.out.println("Enter number between 1 to 4 : ");
                    scanner.nextLine();
                }
            } while (true);
            System.out.println();
            
        }

        storeIntoDatabase(quiz.getScore());

        System.out.println("Quiz completed! Your score: " + quiz.getScore());

        System.out.println();

    }

    public static void storeIntoDatabase(int score) {
        try {
            String sql = "insert into scoreboard(playerName,score) values (?,?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, QuizGame.playerName);
            pst.setInt(2, score);
            pst.executeUpdate();

        } catch (Exception e) {
           // e.printStackTrace();
            System.out.println("Exception found." + e.getMessage());
        }
    }

    public static void printScoreBoard() {
        try {

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM scoreboard ORDER BY score DESC");

            int rank = 1;
        

            while (rs.next()) {
                System.out.println(rank + "> " + rs.getString("playerName") + " " + rs.getInt("score"));
                rank++;
            }
            System.out.println("Finish.");

        } catch (Exception e) {
            
            System.out.println("Exception found." + e.getMessage());
        }
    }
}