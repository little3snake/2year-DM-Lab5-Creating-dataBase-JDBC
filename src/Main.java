import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //String dbName = "cruise_db";

        // Создание главного окна
        JFrame frame = new JFrame("Choose Mode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);

        // Панель выбора режима
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        // Кнопки для выбора режима
        JButton adminButton = new JButton("Admin");
        JButton guestButton = new JButton("Guest");
        JButton customizeUserButton = new JButton("Customize User");

        panel.add(adminButton);
        panel.add(guestButton);
        panel.add(customizeUserButton);

        frame.add(panel);

        // Обработчик для кнопок
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = JOptionPane.showInputDialog(frame, "Enter admin password:");
                frame.setVisible(false);
                new CruiseApp("admin", password, null);
            }
        });

        guestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = JOptionPane.showInputDialog(frame, "Enter guest password:");
                frame.setVisible(false);
                new CruiseApp("guest", password, null);
            }
        });

        customizeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Окно для ввода имени, пароля и выбора прав доступа
                JPanel customizePanel = new JPanel(new GridLayout(8, 2));

                JTextField usernameField = new JTextField();
                JPasswordField passwordField = new JPasswordField();

                // Флажки для выбора прав доступа
                JCheckBox selectCheckBox = new JCheckBox("SELECT");
                JCheckBox insertCheckBox = new JCheckBox("INSERT");
                JCheckBox updateCheckBox = new JCheckBox("UPDATE");
                JCheckBox deleteCheckBox = new JCheckBox("DELETE");

                customizePanel.add(new JLabel("Username:"));
                customizePanel.add(usernameField);
                customizePanel.add(new JLabel("Password:"));
                customizePanel.add(passwordField);
                customizePanel.add(new JLabel("Privileges:"));
                customizePanel.add(new JLabel(""));  // Пустое место для выравнивания
                customizePanel.add(selectCheckBox);
                customizePanel.add(insertCheckBox);
                customizePanel.add(updateCheckBox);
                customizePanel.add(deleteCheckBox);

                int option = JOptionPane.showConfirmDialog(frame, customizePanel, "Customize User", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());

                    // Сбор выбранных прав
                    ArrayList<Object> privileges = new ArrayList<>();
                    if (selectCheckBox.isSelected()) privileges.add("SELECT");
                    if (insertCheckBox.isSelected()) privileges.add("INSERT");
                    if (updateCheckBox.isSelected()) privileges.add("UPDATE");
                    if (deleteCheckBox.isSelected()) privileges.add("DELETE");

                    // Передаем введенные данные в метод для обработки
                    //new CruiseApp("guest", password);
                    new CruiseApp(username, password, privileges);
                }
            }
        });

        frame.setVisible(true);
    }
}




