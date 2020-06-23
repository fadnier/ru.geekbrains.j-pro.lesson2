package server;

import java.sql.*;
import java.util.HashMap;

public class SqliteMessageService implements MessageService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psSelect;
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

    private static void prepareUpdMsgStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO message(user_id,receiving_id,text) VALUES (?,?,?);");
    }

    private static void prepareGetMsgStatements() throws SQLException {
        psSelect = connection.prepareStatement("SELECT ms.id, ms.text FROM message ms " +
               // "LEFT JOIN users u ON ms.receiving_id = u.id LEFT JOIN users u2 ON ms.user_id = u2.id " +
                "WHERE (ms.receiving_id IS NULL OR ms.receiving_id = ? OR ms.user_id = ?) AND ms.archive = false;");
    }

    private HashMap<Integer,String> getMsg(int id) throws SQLException {
        HashMap<Integer, String> msg = new HashMap();
        psSelect.setInt(1, id);
        psSelect.setInt(2, id);
        ResultSet rs = psSelect.executeQuery();
        while (rs.next()) {
            msg.put(rs.getInt("id"),rs.getString("text"));
        }
        rs.close();
        return msg;
    }

    private void insMsg(int user_id, int receiving_id, String text) throws SQLException {
        psInsert.setInt(1, user_id);
        if(receiving_id !=0) {
            psInsert.setInt(2, receiving_id);
        } else {
            psInsert.setNull(2,Types.INTEGER);
        }
        psInsert.setString(3, text);
        psInsert.executeUpdate();
    }

    @Override
    public HashMap<Integer,String> getAllMessage(int idSender) {
        try {
            connect();
            prepareGetMsgStatements();
            HashMap<Integer,String> allMsg = getMsg(idSender);
            return allMsg;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public void addMessage(String msg, int idReceiving, int idSender) {
        try {
            connect();
            prepareUpdMsgStatements();
            insMsg(idSender,idReceiving,msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

}
