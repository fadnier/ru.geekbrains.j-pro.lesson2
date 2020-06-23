package server;

import java.sql.*;

public class SqliteAuthService implements AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psSelect;
    private static PreparedStatement psSelectNick;
    private static PreparedStatement psInsert;
    private static PreparedStatement psUpdate;


    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/bd.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void prepareGetUserStatements() throws SQLException {
        psSelect = connection.prepareStatement("SELECT u.id, u.name, u.login, u.password, g.privilege FROM users u LEFT JOIN groups g ON u.group_id = g.id " +
                "WHERE u.login = ? AND u.password = ? AND u.archive = false;");
    }

    private static void prepareCheckNicknameStatements() throws SQLException {
        psSelectNick = connection.prepareStatement("SELECT name FROM users WHERE name = ? AND archive = false;");
    }

    private static void prepareUpdUserStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO users(login,password,name) VALUES (?,?,?);");
    }

    private static void prepareUpdNicknameStatements() throws SQLException {
        psUpdate = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?;");
    }

    private boolean checkNickname(String nickname) throws SQLException {
        boolean checked = false;
        psSelectNick.setString(1, nickname);
        ResultSet rs = psSelectNick.executeQuery();
        while (rs.next()) {
            checked = true;
        }
        rs.close();
        return checked;
    }

    private String[] getUser(String login, String password) throws SQLException {
        String[] result = new String[4];
        result[0] = "Not ok";

        psSelect.setString(1, login);
        psSelect.setString(2, password);
        ResultSet rs = psSelect.executeQuery();
        while (rs.next()) {
            result[0] = "ok";
            result[1] = rs.getString("name");
            result[2] = rs.getString("id");
            result[3] = rs.getString("privilege");
        }
        rs.close();
        return result;
    }

    private void insUser(String login, String password, String nickname) throws SQLException {
        psInsert.setString(1, login);
        psInsert.setString(2, password);
        psInsert.setString(3, nickname);
        psInsert.executeUpdate();
    }

    private void updNickname(int id, String nickname) throws SQLException {
        psUpdate.setInt(2, id);
        psUpdate.setString(1, nickname);
        psUpdate.executeUpdate();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            connect();
            prepareGetUserStatements();
            String[] result = getUser(login, password);
            if(result[0].equals("ok")) {
                return result[1];
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public String[] getAuthByLoginAndPassword(String login, String password) {
        try {
            connect();
            prepareGetUserStatements();
            String[] result = getUser(login, password);
            if(result[0].equals("ok")) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            connect();
            prepareUpdUserStatements();
            insUser(login,password,nickname);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return false;
    }

    public boolean changeNickname(int id, String nickname) {
        try {
            connect();
            prepareCheckNicknameStatements();
            if(!checkNickname(nickname)) {
                prepareUpdNicknameStatements();
                updNickname(id,nickname);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return false;
    }
}
