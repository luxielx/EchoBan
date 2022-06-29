package luxielx.echoban;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class MySql {
    EchoBan main;
    String table;
    private Connection connection;
    // primary = Victim name
    // ban time
    // ban period
    // baner
    // reports
    // mute time
    // mute period
    // muter
    public MySql(EchoBan plugin) throws IOException, SQLException, ClassNotFoundException {
        this.main = plugin;

        createDatabase();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.createStatement().execute("SELECT 1");
                    }
                } catch (SQLException e) {
                    try {
                        connection = getNewConnection();
                    } catch (ClassNotFoundException | SQLException ex) {
                    }
                }
            }
        }.runTaskTimerAsynchronously(main, 60 * 20, 60 * 20);
        connection = getNewConnection();
        table = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.table");
        PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table +
                "(" +
                "    victim CHAR(36) NOT NULL," +
                "    bantime BIGINT DEFAULT 0 NOT NULL," +
                "    banperiod BIGINT DEFAULT 0 NOT NULL," +
                "    banpunisher TEXT," +
                "    reports INT DEFAULT 0 NOT NULL," +
                "    mutetime BIGINT DEFAULT 0 NOT NULL," +
                "    muteperiod BIGINT DEFAULT 0 NOT NULL," +
                "    mutepunisher TEXT," +
                "    PRIMARY KEY (victim)" +
                "" +
                ");");
        ps.executeUpdate();


    }
    public void addInfo(String victim, Long bantime, Long banperiod, String banpunisher, int reports,Long mutetime,Long muteperiod,String mutepunisher) throws
            SQLException {
        PreparedStatement state = connection.prepareStatement("INSERT INTO "+table+"" +
                "(victim,bantime,banperiod,banpunisher,reports,mutetime,muteperiod,mutepunisher) VALUES (?,?,?,?,?,?,?,?)");
        state.setString(1, victim);
        state.setLong(2, bantime);
        state.setLong(3, banperiod);
        state.setString(4, banpunisher);
        state.setInt(5, reports);
        state.setLong(6, mutetime);
        state.setLong(7, muteperiod);
        state.setString(8, mutepunisher);
        state.executeUpdate();
    }
    public boolean isExist(String victim) throws SQLException {
        String sql = "Select 1 from "+table+" where victim = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,victim);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public void updateReports(String victim, int reports) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET reports = ? WHERE victim = ?");
        state.setInt(1, reports);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateMutePunisher(String victim, String punisher) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET mutepunisher = ? WHERE victim = ?");
        state.setString(1, punisher);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateMutePeriod(String victim, long banperiod) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET muteperiod = ? WHERE victim = ?");
        state.setLong(1, banperiod);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateMutetime(String victim, long bantime) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET mutetime = ? WHERE victim = ?");
        state.setLong(1, bantime);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateBanPunisher(String victim, String punisher) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET banpunisher = ? WHERE victim = ?");
        state.setString(1, punisher);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateBanPeriod(String victim, long banperiod) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET banperiod = ? WHERE victim = ?");
        state.setLong(1, banperiod);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public void updateBantime(String victim, long bantime) throws SQLException {
        PreparedStatement state = connection.prepareStatement("UPDATE "+table+" SET bantime = ? WHERE victim = ?");
        state.setLong(1, bantime);
        state.setString(2, victim);
        state.executeUpdate();
    }
    public long getMutetime(String victim) throws SQLException {
        long value = 0;
        PreparedStatement state = connection.prepareStatement("SELECT mutetime FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getLong("mutetime");
        }
        return value;
    }
    public long getMutePeriod(String victim) throws SQLException {
        long value = 0;
        PreparedStatement state = connection.prepareStatement("SELECT muteperiod FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getLong("muteperiod");
        }
        return value;
    }
    public String getMutePunisher(String victim) throws SQLException {
        String value = "";
        PreparedStatement state = connection.prepareStatement("SELECT mutepunisher FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getString("mutepunisher");
        }
        return value;
    }


    public long getBantime(String victim) throws SQLException {
        long value = 0;
        PreparedStatement state = connection.prepareStatement("SELECT bantime FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getLong("bantime");
        }
        return value;
    }
    public long getPeriod(String victim) throws SQLException {
        long value = 0;
        PreparedStatement state = connection.prepareStatement("SELECT banperiod FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getLong("banperiod");
        }
        return value;
    }
    public String getBanPunisher(String victim) throws SQLException {
        String value = "";
        PreparedStatement state = connection.prepareStatement("SELECT banpunisher FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getString("banpunisher");
        }
        return value;
    }
    public int getReports(String victim) throws SQLException {
        int value = 0;
        PreparedStatement state = connection.prepareStatement("SELECT reports FROM " + table + " WHERE victim = ?");
        state.setString(1, victim);
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            value = resultSet.getInt("reports");
        }
        return value;
    }
    private void createDatabase() throws ClassNotFoundException, SQLException {
        String host = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.host");
        String port = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.port");
        String database = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.database");
        String user = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.user");
        String password = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.password");
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=utf8";
        Connection connection = DriverManager.getConnection(url, user, password);
        Statement stmt = connection.createStatement();
        String sql = "  CREATE DATABASE IF NOT EXISTS " + database + ";";
        stmt.executeUpdate(sql);

    }

    public ArrayList<String> sort() throws SQLException {
        ArrayList<String> arrayList = new ArrayList<>();
        PreparedStatement state = connection.prepareStatement("SELECT victim FROM " + table + " ORDER BY reports DESC LIMIT 55;");
        ResultSet resultSet = state.executeQuery();
        while (resultSet.next()) {
            arrayList.add(resultSet.getString("victim"));
        }

        return arrayList;
    }


    private Connection getNewConnection() throws ClassNotFoundException, SQLException {
        String host = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.host");
        String port = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.port");
        String database = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.database");
        String user = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.user");
        String password = ConfigManager.getInstance().getConfig("config.yml").getString("mysql.password");
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=utf8";
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection;
    }
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }


}
