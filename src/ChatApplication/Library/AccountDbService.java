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
            this.connection = DriverManager.getConnection("jdbc:sqlite:src/ChatApplication/Resource/ChatApplicationDB");
            this.statement = this.connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(Account account) {
        try {
            String sql = String.format("INSERT INTO Accounts (Name, Password)" +
                    "VALUES ('%s', '%s')", account.getName(), account.getPassword());
            this.statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Account get(Account account) {
        try {
            String sql = String.format("SELECT * FROM Accounts WHERE Name = '%s'", account.getName());
            ResultSet result = this.statement.executeQuery(sql);
            if (result.next())
                return new Account(result.getString("Name"), result.getString("Password"));
            else
                return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public List<Account> getAllAccounts(){
        try {
            List<Account> resultList = new ArrayList<>();
            String sql = "SELECT * FROM Accounts";
            ResultSet result = this.statement.executeQuery(sql);
            while (result.next())
                resultList.add(new Account(result.getString("Name"), result.getString("Password")));
            return resultList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public void delete(Account account) {
        try {
            String sql = String.format("DELETE FROM Accounts WHERE Name = '%s';", account.getName());
            this.statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateName(Account account, String name) {
        try {
            String sql = String.format("Update Accounts Set Name = '%s' WHERE Name = '%s'",
                    name, account.getName());
            this.statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updatePassword(Account account, String password) {
        try {
            String sql = String.format("Update Accounts Set Password = '%s' WHERE Name = '%s'",
                    password, account.getName());
            this.statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

//     todo löschen
//    public static void main( String args[] ) {
//        AccountDbService service = new AccountDbService();
//        Account account = new Account("Tobias", "123");
//
//        System.out.println(service.get(account));
//        service.delete(new Account("Jakob", "123"));
//        service.set(new Account("Jakob", "123"));
//        service.updatePassword(account, "12");
//        System.out.println(service.getAllAccounts().toString());
//        service.close();
//    }
}
