import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuizApp extends JFrame implements ActionListener {
    JLabel questionLabel, timerLabel;
    JProgressBar timerBar;
    JRadioButton[] options = new JRadioButton[4];
    ButtonGroup optionGroup;
    JButton nextButton, restartButton;

    ArrayList<String> questionsList = new ArrayList<>();
    ArrayList<String[]> choicesList = new ArrayList<>();
    ArrayList<Integer> correctAnswersList = new ArrayList<>();

    int currentQuestion = 0;
    int score = 0;
    int timeLeft = 10;
    Timer timer;

    public QuizApp() {
        setTitle("🧠 Quiz Application");
        setSize(650, 450);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(30, 60, 90));

        loadQuestionsFromJSON("questions.json");
        
        Font boldFont = new Font("Segoe UI", Font.BOLD, 18);
        questionLabel = new JLabel("Question");
        questionLabel.setFont(boldFont);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setBounds(40, 30, 550, 30);
        add(questionLabel);

        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            options[i].setBounds(50, 80 + (i * 40), 500, 30);
            options[i].setForeground(Color.WHITE);
            options[i].setBackground(new Color(30, 60, 90));
            optionGroup.add(options[i]);
            add(options[i]);
        }

        nextButton = new JButton("Next ➡");
        nextButton.setBounds(140, 300, 120, 40);
        nextButton.setFont(boldFont);
        nextButton.setBackground(new Color(70, 130, 180));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.addActionListener(this);
        add(nextButton);

        restartButton = new JButton("Restart 🔁");
        restartButton.setBounds(300, 300, 140, 40);
        restartButton.setFont(boldFont);
        restartButton.setBackground(new Color(100, 149, 237));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> restartQuiz());
        add(restartButton);

        timerLabel = new JLabel("Time left: 10s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(Color.CYAN);
        timerLabel.setBounds(480, 10, 140, 30);
        add(timerLabel);

        timerBar = new JProgressBar(0, 10);
        timerBar.setValue(10);
        timerBar.setBounds(40, 360, 550, 20);
        timerBar.setForeground(new Color(100, 149, 237));
        add(timerBar);

        loadQuestion();
        startTimer();

        setVisible(true);
    }

    void loadQuestionsFromJSON(String filename) {
        try {
            StringBuilder jsonData = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
            JSONArray arr = new JSONArray(jsonData.toString());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                questionsList.add(obj.getString("question"));
                JSONArray options = obj.getJSONArray("choices");
                String[] choiceArr = new String[4];
                for (int j = 0; j < 4; j++) {
                    choiceArr[j] = options.getString(j);
                }
                choicesList.add(choiceArr);
                correctAnswersList.add(obj.getInt("answer"));
            }

            // Shuffle questions
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < questionsList.size(); i++) indices.add(i);
            Collections.shuffle(indices);

            ArrayList<String> qTemp = new ArrayList<>();
            ArrayList<String[]> cTemp = new ArrayList<>();
            ArrayList<Integer> aTemp = new ArrayList<>();
            for (int i : indices) {
                qTemp.add(questionsList.get(i));
                cTemp.add(choicesList.get(i));
                aTemp.add(correctAnswersList.get(i));
            }
            questionsList = qTemp;
            choicesList = cTemp;
            correctAnswersList = aTemp;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load questions: " + e.getMessage());
            System.exit(1);
        }
    }

    void loadQuestion() {
        if (currentQuestion < questionsList.size()) {
            questionLabel.setText("Q" + (currentQuestion + 1) + ": " + questionsList.get(currentQuestion));
            String[] opts = choicesList.get(currentQuestion);
            for (int i = 0; i < 4; i++) options[i].setText(opts[i]);
            optionGroup.clearSelection();
            timeLeft = 10;
            timerBar.setValue(10);
        } else {
            showResult();
        }
    }

    void startTimer() {
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time left: " + timeLeft + "s");
            timerBar.setValue(timeLeft);
            if (timeLeft <= 0) {
                checkAnswer();
                currentQuestion++;
                loadQuestion();
            }
        });
        timer.start();
    }

    void checkAnswer() {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) selected = i;
        }
        if (selected == correctAnswersList.get(currentQuestion)) {
            playSound("correct.wav");
            score++;
        } else {
            playSound("wrong.wav");
        }
    }

    void showResult() {
        timer.stop();
        playSound("end.wav");
        JOptionPane.showMessageDialog(this, "Quiz Over!\nScore: " + score + "/" + questionsList.size(), 
                                      "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    void restartQuiz() {
        currentQuestion = 0;
        score = 0;
        loadQuestion();
        timer.start();
    }

    void playSound(String file) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(file));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ignored) {}
    }

    public void actionPerformed(ActionEvent e) {
        checkAnswer();
        currentQuestion++;
        loadQuestion();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(QuizApp::new);
    }
}