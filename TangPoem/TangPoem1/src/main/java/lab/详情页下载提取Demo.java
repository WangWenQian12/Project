package lab;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class 详情页下载提取Demo {

    public static void main(String[] args) throws IOException {
        WebClient webClient = new WebClient (BrowserVersion.CHROME);
        webClient.getOptions ().setCssEnabled (false);
        webClient.getOptions ().setJavaScriptEnabled (false);

        String url = "https://so.gushiwen.org/shiwenv_f324eea45183.aspx";
        HtmlPage page = webClient.getPage (url);
        HtmlElement body = page.getBody ();

        //根据其html页的格式求取需要的内容

        //标题
        {
            String xpath = "//div[@class='cont']/h1/text()";
            Object o = body.getByXPath (xpath).get (0);
            DomText domText = (DomText)o;
            System.out.println (domText.asText ());
        }

    }


}
