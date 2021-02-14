package ChatApplication.Library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDbService {
    private Connection connection;
    private Statement statement;

    public AccountDbService() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:src/ChatApplication/Resource/ChatApplicationDB.db");
            this.statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(Account account) {
        try {
            String sql = String.format("INSERT INTO Accounts (Name, Password)" +
                    "VALUES ('%s', '%s')", account.getName(), account.getPassword());
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Account get(String name) {
        try {
            String sql = String.format("SELECT * FROM Accounts WHERE Name = '%s'", name);
            ResultSet result = statement.executeQuery(sql);
            if (result.next())
                return new Account(result.getString("Name"), result.getString("Password"),
                        result.getBoolean("Banned"));
            else
                return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public List<Account> getAllAccounts() {
        try {
            List<Account> resultList = new ArrayList<>();
            String sql = "SELECT * FROM Accounts";
            ResultSet result = statement.executeQuery(sql);
            while (result.next())
                resultList.add(new Account(result.getString("Name"), result.getString("Password"),
                        result.getBoolean("Banned")));
            return resultList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public void delete(String name) {
        try {
            String sql = String.format("DELETE FROM Accounts WHERE Name = '%s';", name);
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateName(String oldName, String newName) {
        try {
            String sql = String.format("Update Accounts Set Name = '%s' WHERE Name = '%s'",
                    newName, oldName);
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updatePassword(String name, String newPassword) {
        try {
            String sql = String.format("Update Accounts Set Password = '%s' WHERE Name = '%s'",
                    newPassword, name);
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateBanned(String name, boolean banned) {
        try {
            String sql = String.format("Update Accounts Set Banned = %b WHERE Name = '%s'",
                    banned, name);
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean nameInUse(String name) {
        try {
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM Accounts WHERE Name = '%s'", name));
            return result.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
