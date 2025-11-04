import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class QuizApp {
    private JFrame frame;
    private JLabel questionLabel, feedbackLabel, timerLabel;
    private JButton[] optionButtons;
    private java.util.List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String username;
    private int timeLeft = 30;  
    private javax.swing.Timer countdownTimer;

    private static final String RESULTS_FILE = "C:\\Users\\TARUN\\Desktop\\tarun\\results.txt";  
  

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApp::new);
    }

    public QuizApp() {
        loadQuestionsFromFile("C:\\Users\\TARUN\\Desktop\\tarun\\questions.txt"); 
        setupStartPage();
    }

    private void setupStartPage() {
        frame = new JFrame("Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        ImageIcon originalIcon = new ImageIcon("C:\\Users\\TARUN\\Downloads\\qimage.png"); 
        Image resizedImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); 
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        JLabel imageLabel = new JLabel(resizedIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome to the Quiz App!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("<html><center>Test your knowledge!<br>Enter your name below to get started.</center></html>", JLabel.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField usernameField = new JTextField(15);
        usernameField.setMaximumSize(new Dimension(400, 50));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        usernameField.setHorizontalAlignment(JTextField.CENTER);

        JButton startButton = new JButton("Start Quiz");
        startButton.setFont(new Font("Arial", Font.BOLD, 28));
        startButton.setBackground(new Color(30, 144, 255));
        startButton.setForeground(Color.WHITE);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton.addActionListener(e -> {
            username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter your name!");
                return;
            }
            frame.getContentPane().removeAll();
            setupQuizUI();
            frame.revalidate();
            frame.repaint();
            loadQuestion();
        });

    
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(imageLabel); 
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(instructionLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(startButton);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void setupQuizUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        questionLabel = new JLabel("", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 32));
        topPanel.add(questionLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("Time: 30", JLabel.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(timerLabel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 28));
            optionButtons[i].setPreferredSize(new Dimension(350, 100));
            optionsPanel.add(optionButtons[i]);
            int index = i;
            optionButtons[i].addActionListener(e -> checkAnswer(index));
        }
        frame.add(optionsPanel, BorderLayout.CENTER);

        feedbackLabel = new JLabel("", JLabel.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.BOLD, 36));
        feedbackLabel.setPreferredSize(new Dimension(frame.getWidth(), 150));
        feedbackLabel.setBackground(new Color(255, 255, 255));
        feedbackLabel.setOpaque(true);
        feedbackLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        frame.add(feedbackLabel, BorderLayout.SOUTH);
    }

    private void loadQuestionsFromFile(String fileName) {
        java.util.List<Question> allQuestions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String text = parts[0];
                    String[] options = Arrays.copyOfRange(parts, 1, 5);
                    int correctIndex = Integer.parseInt(parts[5]) - 1;
                    allQuestions.add(new Question(text, options, correctIndex));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to load questions: " + e.getMessage());
            System.exit(1);
        }

        Collections.shuffle(allQuestions);
        questions = allQuestions.subList(0, Math.min(10, allQuestions.size()));
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResultsWithDelay();
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><center>" + "Q" + (currentQuestionIndex + 1) + ": " + question.text + "</center></html>");

        java.util.List<String> shuffledOptions = new ArrayList<>(Arrays.asList(question.options));
        Collections.shuffle(shuffledOptions);

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(shuffledOptions.get(i));
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackground(null);
        }

        timeLeft = 30;
        startTimer();
    }

    private void startTimer() {
        timerLabel.setText("Time: " + timeLeft);

        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft);
            } else {
                countdownTimer.stop();
                feedbackLabel.setText("Time's up!");
                showCorrectAnswer();
                delayNextQuestion();
            }
        });

        countdownTimer.start();
    }

    private void checkAnswer(int index) {
        countdownTimer.stop();
        Question question = questions.get(currentQuestionIndex);
        String selectedOption = optionButtons[index].getText();

        if (selectedOption.equals(question.options[question.correctIndex])) {
            score++;
            feedbackLabel.setText("Correct!");
            optionButtons[index].setBackground(Color.GREEN);
        } else {
            feedbackLabel.setText("Wrong! Correct answer: " + question.options[question.correctIndex]);
            optionButtons[index].setBackground(Color.RED);
            showCorrectAnswer();
        }

        delayNextQuestion();
    }

    private void showCorrectAnswer() {
        Question question = questions.get(currentQuestionIndex);
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].getText().equals(question.options[question.correctIndex])) {
                optionButtons[i].setBackground(Color.GREEN);
            }
        }
    }

    private void delayNextQuestion() {
        javax.swing.Timer delay = new javax.swing.Timer(1200, e -> {
            currentQuestionIndex++;
            loadQuestion();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void showResultsWithDelay() {
        javax.swing.Timer delay = new javax.swing.Timer(1100, e -> showResults());
        delay.setRepeats(false);
        delay.start();
    }

    private void showResults() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        JLabel resultLabel = new JLabel("<html><center>Quiz Over!<br>Name: " + username + "<br>Your Score: " + score + "/10</center></html>", JLabel.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 48));
        frame.add(resultLabel, BorderLayout.CENTER);

        JButton saveResultsButton = new JButton("Save Results");
        saveResultsButton.setFont(new Font("Arial", Font.PLAIN, 24));
        saveResultsButton.addActionListener(e -> saveResults());
        frame.add(saveResultsButton, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();
    }

    private void saveResults() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULTS_FILE, true))) {
            writer.write("Name: " + username + " | Score: " + score + "/10\n");
            JOptionPane.showMessageDialog(frame, "Results saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to save results: " + e.getMessage());
        }
    }

    private static class Question {
        String text;
        String[] options;
        int correctIndex;

        Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }
}
