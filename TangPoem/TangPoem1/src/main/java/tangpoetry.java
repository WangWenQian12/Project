import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mysql.jdbc.Connection;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class tangpoetry {

    // 唐诗
    // 1.获取列表页
    // 2.获取详情页
    // 3.sha256（用于计算每一首诗的特殊编号，避免重复新插入数据库）
    // 4.分词（用于项目后期的计算词语出现个数功能）
    // 5.将数据插入数据库

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException,SQLException {

        //获取列表页

        // 创造无界面的浏览器
        WebClient webClient = new WebClient (BrowserVersion.CHROME);//模拟chrome
        //禁用脚本和样式
        //关闭了浏览器中的js执行引擎
        webClient.getOptions ().setJavaScriptEnabled (false);
        //关闭了浏览器中的ccs执行引擎
        webClient.getOptions ().setCssEnabled (false);


        //更具获取到的html文件的格式，拿到我们需要的内容
        HtmlPage page = webClient.getPage ("https://so.gushiwen.org/gushi/tangshi.aspx");
        //System.out.println (page);
//        String path = "E:\\code\\JavaSE\\IDEA\\JavaWeb\\TangPoetry";
//        String fileRealName = path + "/index.html";
//        File file = new File (fileRealName);
        File file = new File ("唐诗三百首\\列表页.html");

        //记录一共有获取到多少个
        int count = 0;
        HtmlElement body = page.getBody ();
        List<HtmlElement> elements = body.getElementsByAttribute
                ("div","class","typecont");
        System.out.println ("获取到的每首唐诗的url：");
        for(HtmlElement e : elements){
            List<HtmlElement> aElements = e.getElementsByTagName ("a");
            for(HtmlElement a : aElements){
                System.out.println (a.getAttribute ("href"));
                String ahref = a.getAttribute ("href");
                String path = "https://so.gushiwen.org" + ahref;
                System.out.println (path);
                HtmlPage page1 = webClient.getPage (path);
                System.out.println (page1);
                HtmlElement body1 = page1.getBody ();


                //标题
                String xpath1 = "//div[@class='cont']/h1/text()";
                Object o1 = body1.getByXPath (xpath1).get (0);
                DomText domText1 = (DomText)o1;
                String title = domText1.asText ();
                System.out.println ("《" + title + "》");


                //朝代
                String xpath2 = "//div[@class='cont']/p[@class='source']/a[1]/text()";
                Object o2 = body1.getByXPath (xpath2).get (0);
                DomText domText2 = (DomText)o2;
                String dynasty = domText2.asText ();
                System.out.print (dynasty + "  ");


                //作者
                String xpath3 = "//div[@class='cont']/p[@class='source']/a[2]/text()";
                Object o3 = body1.getByXPath (xpath3).get (0);
                DomText domText3 = (DomText)o3;
                String author = domText3.asText ();
                System.out.println (author);


                //正文
                String xpath4 = "//div[@class='cont']/div[@class='contson']";
                Object o4 = body1.getByXPath(xpath4).get(0);
                HtmlElement element = (HtmlElement)o4;
                String content = element.getTextContent ().trim ();
                System.out.println(content);

                //计算sha256
                MessageDigest messageDigest = MessageDigest.getInstance ("SHA-256");
                byte[] bytes = content.getBytes ("UTF-8");
                messageDigest.update (bytes);
                byte[] result = messageDigest.digest ();//求hash值
                System.out.println ("标识长度：" + result.length);
                String sha256 = "";
                for(byte r : result){
                    System.out.printf ("%02x",r);
                    sha256 += r;
                }
                System.out.println ();

                //分词（正文）
                List<Term> termList = NlpAnalysis.parse (content).getTerms ();//将正文分词
                String words = "";
                for(Term term : termList){
                    System.out.println (term.getNatureStr() + ":" + term.getRealName());//词性+词语
                    if(!(term.getNatureStr ().equals ("w"))) {
                        words += term.getRealName ();
                        words += ",";
                    }
                }
                 System.out.println ();

                 /*
                 插入数据库
                 1. 注册 Driver
                 2. 通过 DriverManager 获取 Connection

                 Class.forName("com.mysql.jdbc.Driver");
                 String url = "jdbc:mysql://127.0.0.1/tangshi?useSSL=false&characterEncoding=utf8";
                 Connection connection = DriverManager.getConnection(url, "root", "775823");
                 System.out.println(connection);
                 */

                // 通过 DataSource 获取 Connection
                // 这个不带有连接池
                //DataSource dataSource1 = new MysqlDataSource();
                // 这个带有连接池，好处参照线程池
                MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource ();
                dataSource.setServerName ("127.0.0.1");
                dataSource.setPort (3306);
                dataSource.setUser ("root");
                dataSource.setPassword ("775823");
                dataSource.setDatabaseName ("tangpoetry");
                dataSource.setUseSSL (false);
                dataSource.setCharacterEncoding ("UTF-8");

                try(Connection connection = (Connection) dataSource.getConnection()){
                    String sql = "INSERT INTO tangshi"
                            + "(sha256,title,dynasty,author,content,words)"
                            + "VALUES(?,?,?,?,?,?)";
                    try(PreparedStatement statement = connection.prepareStatement (sql)){
                        statement.setString (1,sha256);
                        statement.setString (2,title);
                        statement.setString (3,dynasty);
                        statement.setString (4,author);
                        statement.setString (5,content);
                        statement.setString (6,words);

                        statement.executeUpdate ();
                    }
                }

                count++;
            }
        }
        System.out.println (count);
    }

    //获取详情页



}
