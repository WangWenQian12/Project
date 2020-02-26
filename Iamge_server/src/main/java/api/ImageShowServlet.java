package api;

import dao.Image;
import dao.ImageDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

// 展示图片
public class ImageShowServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1.解析出 imageId
        String imageId = req.getParameter ("imageId");
        if(imageId == null || imageId.equals ("")){
            resp.setContentType ("application/json;charset=utf-8");
            resp.getWriter ().write ("{\"ok\":false,\"reason\":\"imageId 解析错误\"}");
            return;
        }
        // 2.根据 imageId 查找数据库，得到图片属性（获取存储路径）
        ImageDao imageDao = new ImageDao ();
        Image image = imageDao.selectOne (Integer.parseInt (imageId));
        // 3.根据路径打开文件，读取内容，写入响应 resp 对象中
        resp.setContentType (image.getContentType ());
        File file = new File(image.getPath ());
        // 图片是二进制文件，应该使用字节流的方式读取文件
        OutputStream outputStream = resp.getOutputStream ();
        FileInputStream fileInputStream = new FileInputStream (file);
        byte[] buffer = new byte[1024];
        while(true){
            // 将文件写进 buffer
            int len = fileInputStream.read (buffer);
            if(len == -1){
                break;//文件读取结束
            }
            // 把 buffer 中的内容写进响应对象 resp
            outputStream.write (buffer);
        }
        //关闭流
        fileInputStream.close ();
        outputStream.close ();

    }
}
