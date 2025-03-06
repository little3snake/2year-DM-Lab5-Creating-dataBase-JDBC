import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CruiseApp {
    private static String userRole;
    private static String userPassword;

    public CruiseApp(String role, String password, ArrayList<Object> privileges) {
        userRole = role;
        userPassword = password;
        CruiseDBManager.verification(userRole, userPassword, privileges);
        JFrame frame = new JFrame("Cruise App");
        frame.setSize(800, 600); // Увеличим размер окна для отображения таблицы
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Панель с кнопками
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        //panel.setPreferredSize(new Dimension(400, 300));

        // Кнопки для разных операций
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 6, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton createDbButton = new JButton("Create Database");
        JButton addRecordButton = new JButton("Add Record");
        JButton updateButton = new JButton("Update Record");
        JButton deleteButton = new JButton("Delete Record");
        JButton searchButton = new JButton("Search");
        JButton resetSearchButton = new JButton("Reset Search");
        JButton clearTableButton = new JButton("Clear Table");
        JButton deleteDbButton = new JButton("Delete Database");

        // Устанавливаем фон для всех кнопок
        createDbButton.setBackground(Color.pink);
        addRecordButton.setBackground(Color.cyan);
        updateButton.setBackground(Color.cyan);
        deleteButton.setBackground(Color.cyan);
        searchButton.setBackground(Color.cyan);
        resetSearchButton.setBackground(Color.cyan);
        clearTableButton.setBackground(Color.cyan);
        deleteDbButton.setBackground(Color.pink);

        buttonPanel.add(createDbButton);
        buttonPanel.add(addRecordButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(resetSearchButton);
        buttonPanel.add(clearTableButton);
        buttonPanel.add(deleteDbButton);

        // Таблица для отображения данных
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Загрузка данных в таблицу при запуске
        loadTableData(table);

        // Обработчики событий
        createDbButton.addActionListener(e -> {
            String dbName = "cruise_db";
            try {
                // Попытка создать базу данных
                CruiseDBManager.createDatabase(dbName, userRole, userPassword);
                // Если создание прошло успешно, выполняем остальные SQL-операции
                CruiseDBManager.executeSQLFile("sql_scripts/clean_table.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/add_permitions.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/add_record.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/delete_record.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/search.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/update_record.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/check_privilege.sql", dbName);
                CruiseDBManager.executeSQLFile("sql_scripts/add_permissions_unknown.sql", dbName);
                CruiseDBManager.init_passwords(userRole, userPassword);
                CruiseDBManager.addPermissions(dbName, userRole, privileges);
                // Обновление данных в таблице после создания базы данных
                loadTableData(table);
            } catch (SQLException ex) {
                // Обработка исключений
                JOptionPane.showMessageDialog(null,
                        ex.getMessage(),
                        "Ошибка доступа",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        addRecordButton.addActionListener(e -> {
            // Окно для ввода данных записи
            JPanel recordPanel = new JPanel(new GridLayout(6, 2));
            JTextField lastNameField = new JTextField();
            JTextField firstNameField = new JTextField();
            JTextField cruiseNameField = new JTextField();
            JTextField departureDateField = new JTextField();
            JTextField departureCityField = new JTextField();
            JTextField arrivalCityField = new JTextField();

            recordPanel.add(new JLabel("Last Name:"));
            recordPanel.add(lastNameField);
            recordPanel.add(new JLabel("First Name:"));
            recordPanel.add(firstNameField);
            recordPanel.add(new JLabel("Cruise Name:"));
            recordPanel.add(cruiseNameField);
            recordPanel.add(new JLabel("Departure Date (YYYY-MM-DD):"));
            recordPanel.add(departureDateField);
            recordPanel.add(new JLabel("Departure City:"));
            recordPanel.add(departureCityField);
            recordPanel.add(new JLabel("Arrival City:"));
            recordPanel.add(arrivalCityField);

            int option = JOptionPane.showConfirmDialog(frame, recordPanel, "Add Record", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    // Попытка добавить запись в базу данных
                    CruiseDBManager.addRecord(userRole, lastNameField.getText(), firstNameField.getText(), cruiseNameField.getText(), departureDateField.getText(), departureCityField.getText(), arrivalCityField.getText());
                    // Обновление данных в таблице после добавления записи
                    loadTableData(table);
                } catch (SQLException ex) {
                    // Обработка исключения, если возникла ошибка при добавлении записи
                    JOptionPane.showMessageDialog(null,
                            "Ошибка при добавлении записи: " + ex.getMessage(),
                            "Ошибка доступа",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a record to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Получаем данные из выбранной строки
            int id = (int) table.getValueAt(selectedRow, 0);
            String lastName = (String) table.getValueAt(selectedRow, 1);
            String firstName = (String) table.getValueAt(selectedRow, 2);
            String cruiseName = (String) table.getValueAt(selectedRow, 3);
            String departureDate = table.getValueAt(selectedRow, 4).toString();
            String departureCity = (String) table.getValueAt(selectedRow, 5);
            String arrivalCity = (String) table.getValueAt(selectedRow, 6);

            // Создаем форму с уже заполненными данными
            JPanel updatePanel = new JPanel(new GridLayout(7, 2));
            JTextField lastNameField = new JTextField(lastName);
            JTextField firstNameField = new JTextField(firstName);
            JTextField cruiseNameField = new JTextField(cruiseName);
            JTextField departureDateField = new JTextField(departureDate);
            JTextField departureCityField = new JTextField(departureCity);
            JTextField arrivalCityField = new JTextField(arrivalCity);

            updatePanel.add(new JLabel("Last Name:"));
            updatePanel.add(lastNameField);
            updatePanel.add(new JLabel("First Name:"));
            updatePanel.add(firstNameField);
            updatePanel.add(new JLabel("Cruise Name:"));
            updatePanel.add(cruiseNameField);
            updatePanel.add(new JLabel("Departure Date (YYYY-MM-DD):"));
            updatePanel.add(departureDateField);
            updatePanel.add(new JLabel("Departure City:"));
            updatePanel.add(departureCityField);
            updatePanel.add(new JLabel("Arrival City:"));
            updatePanel.add(arrivalCityField);

            int option = JOptionPane.showConfirmDialog(frame, updatePanel, "Update Record", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    CruiseDBManager.updateCruise(
                            userRole,
                            id,
                            lastNameField.getText(),
                            firstNameField.getText(),
                            cruiseNameField.getText(),
                            departureDateField.getText(),
                            departureCityField.getText(),
                            arrivalCityField.getText()
                    );
                    loadTableData(table); // Перезагрузка данных после обновления
                } catch (SQLException ex) {
                    // Обработка исключения, если возникла ошибка при добавлении записи
                    JOptionPane.showMessageDialog(null,
                            "Ошибка при обновлении записи: " + ex.getMessage(),
                            "Ошибка доступа",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        clearTableButton.addActionListener(e -> {
            try {
                CruiseDBManager.clearTable("cruises", userRole);
                // Обновление данных в таблице после очистки
                loadTableData(table);
            } catch (SQLException ex) {
                // Обработка исключения, если возникла ошибка при добавлении записи
                JOptionPane.showMessageDialog(null,
                        "Ошибка при удалении таблицы: " + ex.getMessage(),
                        "Ошибка доступа",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        deleteDbButton.addActionListener(e -> {
            try {
                CruiseDBManager.deleteDatabase("cruise_db", userRole);
                // Обновление данных в таблице после очистки
                loadTableData(table);
                JOptionPane.showMessageDialog(frame, "База данных успешно удалена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                // Обработка исключения, если возникла ошибка при добавлении записи
                JOptionPane.showMessageDialog(null,
                        "Ошибка при удалении базы данных: " + ex.getMessage(),
                        "Ошибка доступа",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        resetSearchButton.addActionListener(e -> {
            //CruiseDBManager.clearTable("cruises", userRole);
            // Обновление данных в таблице после очистки
            loadTableData(table);
        });

        searchButton.addActionListener(e -> {
            String element = JOptionPane.showInputDialog(frame, "Enter element to search for:");
            //CruiseDBManager.searchRecord(element, userRole);
            try {
                ResultSet searchResult = CruiseDBManager.searchRecord(element, userRole);
                if (searchResult != null) {
                    updateTableWithSearchResult(table, searchResult);
                } else {
                    JOptionPane.showMessageDialog(frame, "Ошибка при выполнении поиска.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                // Обработка исключения, если возникла ошибка при добавлении записи
                JOptionPane.showMessageDialog(null,
                        "Ошибка при поиске по таблице: " + ex.getMessage(),
                        "Ошибка доступа",
                        JOptionPane.ERROR_MESSAGE);
            }
            // Обновление данных в таблице после поиска
            //loadTableData(table);
        });

        deleteButton.addActionListener(e -> {
            String element = JOptionPane.showInputDialog(frame, "Enter element to delete:");
            try {
                CruiseDBManager.deleteRecord(element, userRole);
                // Обновление данных в таблице после удаления
                loadTableData(table);
            } catch (SQLException ex) {
                // Обработка исключения, если возникла ошибка при добавлении записи
                JOptionPane.showMessageDialog(null,
                        "Ошибка при удалении записи: " + ex.getMessage(),
                        "Ошибка доступа",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        frame.add(panel);
        frame.setVisible(true);
    }

    // Метод для загрузки данных в таблицу
    private void loadTableData(JTable table) {
        String dbName = "cruise_db";
        String password = userPassword;

        try (Connection conn = CruiseDBManager.getConnection(dbName, userRole, password);
             Statement stmt = conn.createStatement()) {
            // Проверка существования базы данных
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
            if (!rs.next()) {
                // База данных не существует, очищаем таблицу
                table.setModel(new javax.swing.table.DefaultTableModel(new Object[0][], new String[]{}));
                System.out.println("База данных " + dbName + " не существует.");
                return;
            }

            // Если база данных существует, загружаем данные
            rs = stmt.executeQuery("SELECT * FROM cruises");

            // Преобразование ResultSet в модель таблицы
            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("LastName"),
                        rs.getString("FirstName"),
                        rs.getString("CruiseName"),
                        rs.getDate("DepartureDate"),
                        rs.getString("DepartureCity"),
                        rs.getString("ArrivalCity")
                };
                data.add(row);
            }

            // Создание модели таблицы
            Object[][] rowData = data.toArray(new Object[0][]);
            String[] columnNames = {"ID", "Last Name", "First Name", "Cruise Name", "Departure Date", "Departure City", "Arrival City"};
            table.setModel(new javax.swing.table.DefaultTableModel(rowData, columnNames));

        } catch (SQLException e) {
            // Если произошла ошибка (например, база данных не существует), очищаем таблицу
            table.setModel(new javax.swing.table.DefaultTableModel(new Object[0][], new String[]{}));
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void updateTableWithSearchResult(JTable table, ResultSet searchResult) {
        try {
            // Преобразование ResultSet в модель таблицы
            List<Object[]> data = new ArrayList<>();
            while (searchResult.next()) {
                Object[] row = {
                        searchResult.getInt("id"),
                        searchResult.getString("LastName"),
                        searchResult.getString("FirstName"),
                        searchResult.getString("CruiseName"),
                        searchResult.getDate("DepartureDate"),
                        searchResult.getString("DepartureCity"),
                        searchResult.getString("ArrivalCity")
                };
                data.add(row);
            }

            // Создание модели таблицы
            Object[][] rowData = data.toArray(new Object[0][]);
            String[] columnNames = {"ID", "Last Name", "First Name", "Cruise Name", "Departure Date", "Departure City", "Arrival City"};
            table.setModel(new javax.swing.table.DefaultTableModel(rowData, columnNames));

        } catch (SQLException e) {
            // Если произошла ошибка, очищаем таблицу
            table.setModel(new javax.swing.table.DefaultTableModel(new Object[0][], new String[]{}));
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }
}