package dao;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 用于获取数据库连接
public class DBUtil {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/image_server?characterEncoding=utf8&useSSL=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // 单例模式：
    // 用 volatile、二次判断、加锁 来确保线程安全
    private static volatile DataSource dataSource = null;
    public static DataSource getDataSource(){
        // 创建 DataSource 的实例
        if(dataSource == null){
            synchronized (DBUtil.class){
                if(dataSource == null){
                    dataSource = new MysqlDataSource();
                    MysqlDataSource tmpDataSource = (MysqlDataSource)dataSource;
                    tmpDataSource.setURL(URL);
                    tmpDataSource.setUser(USERNAME);
                    tmpDataSource.setPassword(PASSWORD);
                }
            }
        }
        return dataSource;
    }

    //建立连接
    public static Connection getConnection(){
        try {
            return getDataSource ().getConnection ();
        } catch (SQLException e) {
            e.printStackTrace ();
        }
        return null;
    }

    //关闭连接(注意关闭顺序)
    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet){

        try {
            if (resultSet != null) {
                resultSet.close ();
            }
            if (statement != null) {
                statement.close ();
            }
            if (connection != null) {
                connection.close ();
            }
        }catch(SQLException e){
            e.printStackTrace ();
        }
    }
}
