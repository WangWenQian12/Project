package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageServlet extends HttpServlet {

    /**
     * 上传图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");
        // 1. 获取图片的属性信息, 并且存入数据库
        //  a) 需要创建一个 factory 对象 和 upload 对象, 这是为了获取到图片属性做的准备工作
        //     固定的逻辑
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        // b) 通过 upload 对象进一步解析请求(解析HTTP请求中奇怪的 body 中的内容)
        //    FileItem 就代表一个上传的文件对象.
        //    理论上来说, HTTP 支持一个请求中同时上传多个文件
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(req);
        } catch (FileUploadException e) {
            // 出现异常说明解析出错!
            e.printStackTrace();
            // 告诉客户端出现的具体的错误是啥
            resp.getWriter().write("{ \"ok\": false, \"reason\": \"请求解析失败\" }");
            return;
        }
        //  c) 把 FileItem 中的属性提取出来, 转换成 Image 对象, 才能存到数据库中
        //     当前只考虑一张图片的情况
        FileItem fileItem = items.get(0);
        Image image = new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int)fileItem.getSize());
        // 获取一下当前日期, 格式化日期 yyyMMdd
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());
        // MD5
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));
        // 自己构造一个路径来保存, 引入时间戳是为了让文件路径能够唯一
        image.setPath("./image/" + image.getMd5());
        // 存到数据库中
        ImageDao imageDao = new ImageDao();
        imageDao.insert(image);

        // 看看数据库中是否存在相同的 MD5 值的图片, 不存在, 返回 null
        Image existImage = imageDao.selectByMd5(image.getMd5());

        // 2. 获取图片的内容信息, 并且写入磁盘文件
        File file = new File (image.getPath ());
        try {
            fileItem.write (file);
        } catch (Exception e) {
            e.printStackTrace ();
            resp.getWriter ().write ("{ \"ok\": false, \"reason\": \"写磁盘失败\" }");
            return;
        }


        // 3. 给客户端返回一个结果数据
//        resp.setContentType("application/json; charset=utf-8");
//        resp.getWriter().write("{ \"ok\": true }");
        resp.sendRedirect("index.html");
    }

    /**
     * 查看图片属性
     * 查看所有图片、查看指定图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setStatus (200);
//        resp.getWriter().write ("嘿 你好 你说很高兴我能够遇见你 😊");
        // 解析 url 中是否有 ImageId 来判定查看所有图片还是指定图片
        String imageId = req.getParameter ("imageId");
        if(imageId == null || imageId.equals ("")){
            // 查看所有图片
            selectAll(req,resp);
        }else{
            // 查看指定图片
            selectOne(imageId,resp);
        }
    }

    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType ("application/json;charser=utf-8");
        // 1.创建 ImageDao 对象，并查找数据库
        ImageDao imageDao = new ImageDao ();
        List<Image> list = imageDao.selectAll ();
        // 2.把查找到的结果转成 JSON 格式，写回给 resp 对象
        Gson gson = new GsonBuilder ().create ();
        // jsonData 就是一个 json 格式的字符串，json.toJson() 自动帮我们完成大量的转换工作
        String jsonData = gson.toJson (list);
        resp.getWriter ().write (jsonData);
    }

    private void selectOne(String imageId, HttpServletResponse resp) throws IOException {
        resp.setContentType ("application/json;charset=utf-8");
        // 1.创建 ImageDao 对象，查找数据库
        ImageDao imageDao = new ImageDao ();
        Image image = imageDao.selectOne (Integer.parseInt (imageId));
        // 2.把查找到的结果转换为 JSON 格式，写回给 resp 对象
        Gson gson = new GsonBuilder ().create ();
        String jsonData = gson.toJson (image);
        resp.getWriter ().write (jsonData);

    }

    /**
     * 删除图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType ("application/json;charset=utf-8");
        // 1.获取请求中的 imageId
        String imageId = req.getParameter ("imageId");
        if(imageId == null || imageId.equals ("")){
            resp.setStatus (200);
            resp.getWriter ().write ("{\"ok\":false,\"reason\":\"解析请求失败\"}");
            return;
        }
        // 2.创建 ImageDao 对象，查看该图片对应的相关属性（获取其文件路径）
        ImageDao imageDao = new ImageDao ();
        Image image = imageDao.selectOne (Integer.parseInt (imageId));
        if(image == null){
            // 请求的 id 数据库中不存在
            resp.setStatus (200);
            resp.getWriter ().write ("{\"ok\":false,\"reason\":\"imageId 在数据库中不存在\"}");
            return;
        }
        // 3.删除数据中的记录
        imageDao.delete (Integer.parseInt (imageId));

        // 4.删除本地文件
        File file = new File (image.getPath ());
        file.delete ();
        resp.setStatus (200);
        resp.getWriter ().write ("{\"ok\":true}");

    }




}
