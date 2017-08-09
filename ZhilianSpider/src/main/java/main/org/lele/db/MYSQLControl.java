package main.org.lele.db;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import main.org.lele.model.Item;

import org.apache.commons.dbutils.QueryRunner;


/*
 * Mysql操作的QueryRunner方法
 * 一个数据库操作类，别的程序直接调用即可
 */
public class MYSQLControl {

    //根据自己的数据库地址修改
    static DataSource ds = MyDataSource.getDataSource("jdbc:mysql://localhost/spider?useUnicode=true&amp;characterEncoding=utf-8");
    static QueryRunner qr = new QueryRunner(ds);
    //第一类方法
    public static void executeUpdate(String sql){
        try {
            qr.update(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //第二类数据库操作方法
    public static void executeInsert(List<Item> zhilianData) throws SQLException {
        /*
         * 定义一个Object数组，行列
         * 10表示列数，根据自己的数据定义这里面的数字
         * params[i][0]等是对数组赋值，这里用到集合的get方法
         * 
         */
        Object[][] params = new Object[zhilianData.size()][10];
        for ( int i=0; i<params.length; i++ ){
            params[i][0] = zhilianData.get(i).getJobName();
            params[i][1] = zhilianData.get(i).getJobSalary();
            params[i][2] = zhilianData.get(i).getJobHref();
            params[i][3] = zhilianData.get(i).getReleaseDate();
            params[i][4] = zhilianData.get(i).getWorkPlace();
            params[i][5] = zhilianData.get(i).getCompanyIndustry();
            params[i][6] = zhilianData.get(i).getCompanySize();
            params[i][7] = zhilianData.get(i).getCompanyType();
            params[i][8] = zhilianData.get(i).getRecruitmentNumber();
            params[i][9] = zhilianData.get(i).getJobDescription();
        }
        qr.batch("insert into job_offers (job_name,job_salary,job_href,release_date,work_place,company_industry,company_size,company_type,recruitment_number,job_description)"
                + "values (?,?,?,?,?,?,?,?,?,?)", params);
        System.out.println("执行数据库完毕！"+"成功插入数据："+zhilianData.size()+"条");

    }
    
 
}