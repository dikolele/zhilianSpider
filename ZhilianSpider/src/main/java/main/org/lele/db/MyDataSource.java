package main.org.lele.db;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

public class MyDataSource {
    public static DataSource getDataSource(String connectURI){
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver"); //MySQL的jdbc驱动
        ds.setUsername("root");              //所要连接的数据库名
        ds.setPassword("root");                //MySQL的登陆密码
        ds.setUrl(connectURI);
        return ds;
    }
}
