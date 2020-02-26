package dao;

import common.ImageServerException;
import org.omg.CORBA.IMP_LIMIT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Image对象管理器，对Image对象进行增删查改
public class ImageDao {

    /**
     * 把 image 对象插入到数据库中
     * @param image
     */
    public void insert(Image image){
        // 获取数据库连接
        Connection connection = DBUtil.getConnection ();
        // 构造 SQL 语句
        String sql = "insert into image_table values(null,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        // 执行 SQL 语句
        try {
            statement = connection.prepareStatement (sql);
            statement.setString (1,image.getImageName ());
            statement.setInt (2,image.getSize ());
            statement.setString (3,image.getUploadTime ());
            statement.setString (4,image.getContentType ());
            statement.setString (5,image.getPath ());
            statement.setString (6,image.getMd5 ());

            int ret = statement.executeUpdate (); // 插入数据成功后会返回1
            if(ret != 1){
                throw new ImageServerException ("插入数据库出错！");
            }
        } catch (SQLException | ImageServerException e) {
            e.printStackTrace ();
        }finally{
            // 关闭 连接 和 statement 对象
            DBUtil.close (connection,statement,null);//放在finally里避免抛出异常后不执行关闭操作
        }

    }


    /**
     * 查找数据库中所有图片的信息
     * @return
     */
    public List<Image> selectAll(){
        List<Image> list = new ArrayList<> ();
        // 获取数据库链接
        Connection connection = DBUtil.getConnection ();
        // 构造 SQL 语句
        String sql = "select * from image_table";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        // 执行 SQL 语句
        try {
            statement = connection.prepareStatement (sql);
            resultSet = statement.executeQuery ();
            //executeQuery()方法会把数据库响应的查询结果存放在ResultSet类中

            // 处理结果集
            while(resultSet.next ()){
                Image image = new Image ();
                image.setImageId (resultSet.getInt ("imageId"));
                image.setImageName (resultSet.getString ("imageName"));
                image.setSize (resultSet.getInt ("size"));
                image.setUploadTime (resultSet.getString("uploadTime"));
                image.setContentType (resultSet.getString ("contentType"));
                image.setPath (resultSet.getString ("path"));
                image.setMd5 (resultSet.getString ("md5"));
                list.add(image);
            }
            return list;

        } catch (SQLException e) {
            e.printStackTrace ();
        }finally {
            // 关闭连接
            DBUtil.close (connection,statement,resultSet);
        }
        return null;
     }


    /**
     * 从数据库中查找指定图片的信息
     * @param imageId
     * @return
     */
     public Image selectOne(int imageId){
         // 获取数据库连接
         Connection connection = DBUtil.getConnection ();
         // 构造 SQL 语句
         String sql = "select * from image_table where imageId = ?";
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         // 执行 SQL 语句
         try {
             statement = connection.prepareStatement (sql);
             statement.setInt (1,imageId);
             resultSet = statement.executeQuery ();

             // 处理结果集
             while(resultSet.next ()){
                Image image = new Image ();
                image.setImageId (resultSet.getInt ("imageId"));
                image.setImageName (resultSet.getString ("imageName"));
                image.setSize (resultSet.getInt ("size"));
                image.setUploadTime (resultSet.getString ("uploadTime"));
                image.setContentType (resultSet.getString ("contentType"));
                image.setPath (resultSet.getString ("path"));
                image.setMd5 (resultSet.getString ("md5"));
                return image;
             }
         } catch (SQLException e) {
             e.printStackTrace ();
         } finally {
             // 关闭连接
             DBUtil.close (connection,statement,resultSet);
         }

         return null;
     }

    /**
     * 从数据库中删除指定图片
     * @param imageId
     */
    public void delete(int imageId){

        // 获取数据库连接
        Connection connection = DBUtil.getConnection ();
        // 构造 SQL 语句
        String sql = "delete from image_table where imageId = ?";
        PreparedStatement statement = null;
        // 执行 SQL 语句
        try {
            statement = connection.prepareStatement (sql);
            statement.setInt (1,imageId);
            int ret = statement.executeUpdate ();
            //executeUpdate方法主要用于执行增删改的sql语句，其返回的结果是一个整数表示该操作影响了数据表中的几条数据
            if(ret != 1){
                throw new ImageServerException("数据库删除操作失败！");
            }
        } catch (SQLException | ImageServerException e) {
            e.printStackTrace ();
        }finally {
            // 关闭连接
            DBUtil.close (connection,statement,null);
        }
    }

    /**
     * 从数据库中查找md5
     * @param md5
     * @return
     */
    public Image selectByMd5(String md5){
        // 1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        // 2. 构造 SQL 语句
        String sql = "select * from image_table where md5 = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            // 3. 执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setString(1, md5);
            resultSet = statement.executeQuery();
            // 4. 处理结果集
            if (resultSet.next()) {
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 5. 关闭链接
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    public static void main(String[] args) {
        //测试插入图片(成功)
        Image image = new Image ();
        image.setImageName ("12.png");
        image.setSize (12);
        image.setUploadTime ("20200219");
        image.setContentType ("image/png");
        image.setPath("./data/1.png");
        image.setMd5("11223344");
        ImageDao imageDao = new ImageDao();
        imageDao.insert(image);

        //测试查询所有图片信息
//        ImageDao imageDao = new ImageDao ();
//        List<Image> list = imageDao.selectAll ();
//        System.out.println (list);

        //测试查询指定图片信息
//        ImageDao imageDao = new ImageDao ();
//        System.out.println (imageDao.selectOne (1));

        //测试删除指定图片
//        ImageDao imageDao2 = new ImageDao ();
//        imageDao2.delete (1);
    }

}
