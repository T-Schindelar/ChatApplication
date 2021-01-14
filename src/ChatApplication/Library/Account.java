package ChatApplication.Library;

import java.util.Objects;

public class Account implements java.io.Serializable {
    private String name;
    private final String password;
    private final boolean banned;

    public Account(String name, String password, boolean banned) {
        this.name = name;
        this.password = password;
        this.banned = banned;
    }

    public Account(String name, String password) {
        this.name = name;
        this.password = password;
        this.banned = false;
    }

    // getter
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public boolean isBanned() {
        return banned;
    }

    // setter
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return banned == account.banned && Objects.equals(name, account.name) && Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password, banned);
    }

    // ToDo: löschen
    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", banned=" + banned +
                '}';
    }
}
