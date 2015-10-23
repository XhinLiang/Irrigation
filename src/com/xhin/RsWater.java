package com.xhin;

import com.xhin.util.TextUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Created by xhinliang on 15-10-9.
 */
public class RsWater {
    private static final String rsLoginUrl = "http://rs.xidian.edu.cn/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1";
    private static final String rsHomePageUrl = "http://rs.xidian.edu.cn/portal.php";
    private static final String rsWaterUrl = "http://rs.xidian.edu.cn/forum.php?mod=forumdisplay&fid=72&orderby=lastpost&orderby=lastpost&filter=lastpost&page=";


    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_AGENT = "User-Agent";
    private static final String VALUE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)";
    private static final String VALUE_SUCCESS = "succeed";

    private static final String FILE_COOKIES = "cookies";


    private String username;
    private String password;
    private Map<String, String> cookies;
    private String unixTime;
    private Queue<String> pages;

    private int currenPage = 1;


    public RsWater(String username, String password) {
        this.username = username;
        this.password = password;
        pages = new LinkedList<>();
    }

    public boolean login() throws IOException {
        if (cookies != null)
            return true;
        File file = new File(FILE_COOKIES);
        if (file.exists() && file.length() != 0) {
            cookies = new HashMap<>();
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String nextLine = bufferedReader.readLine();
            while (!TextUtil.getInstance().isEmpty(nextLine)) {
                String preLine = nextLine;
                if (preLine.contains("lastcheckfeed")) {
                    unixTime = bufferedReader.readLine();
                    cookies.put(preLine, unixTime);
                    nextLine = bufferedReader.readLine();
                    continue;
                }
                cookies.put(preLine, bufferedReader.readLine());
                nextLine = bufferedReader.readLine();
            }
            bufferedReader.close();
            System.out.println("Read File Success");
            return true;
        }
        Connection connection = Jsoup.connect(rsLoginUrl).timeout(20000);
        connection.data(KEY_USERNAME, username);
        connection.data(KEY_PASSWORD, password);
        connection.header(KEY_USER_AGENT, VALUE_USER_AGENT);
        Connection.Response response = connection.ignoreContentType(true).method(Connection.Method.POST).execute();
        if (response.body().contains(VALUE_SUCCESS)) {
            cookies = response.cookies();
            File newFile = new File(FILE_COOKIES);
            FileWriter fileWriter = new FileWriter(newFile.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
            for (Map.Entry<String, String> item : cookies.entrySet()) {
                if (item.getKey().contains("lastcheckfeed"))
                    unixTime = item.getValue();
                bufferWritter.write(item.getKey());
                bufferWritter.newLine();
                bufferWritter.write(item.getValue());
                bufferWritter.newLine();
            }
            bufferWritter.close();
            return true;
        }
        return false;
    }


    public void water() throws IOException {
        for (int i = 1; i < 20; i++) {
            String nextPage = getListPageUrl(i);
            Connection connection = Jsoup.connect(nextPage).header(KEY_USER_AGENT, VALUE_USER_AGENT);
            Document document = connection.cookies(cookies).timeout(20000).get();
            Elements thElements = document.getElementsByTag("th");
            for (Element element : thElements) {
                if (!element.attr("class").contains("common") && !element.attr("class").contains("new"))
                    continue;
                if (element.text().contains("隐藏置顶帖") || element.text().contains("签到记录贴") || element.text().contains("公告"))
                    continue;
                System.out.println(element.text());
                String postID = element.getElementsByAttributeValueContaining("id", "content_").attr("id").replace("content_", "");
                pages.add(postID);
            }
        }
        for (String page : pages) {
            comment(page, getCommentMessageRandomly());
            try {
                Thread.sleep(18000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 自动评论 刷经验 和积分 ！！！慎用！！！！
    public void comment(String postID, String commentStr) throws IOException {
        String postUrl = getPostUrl(postID);
        Connection connection = Jsoup.connect(postUrl).timeout(20000).cookies(cookies);
        connection.header(KEY_USER_AGENT, VALUE_USER_AGENT);
        // 评论的内容、Unix时间戳、formhash 等验证信息
        connection.ignoreContentType(true)
                .method(Connection.Method.POST);
        connection.data("message", commentStr);
        connection.data("posttime", unixTime);
        connection.data("usesig", "1");
        connection.data("subject", " ");
        connection.data("formhash", getFormHash(postID));
        Connection.Response response = connection.execute();
        if (response.body().contains("succeed"))
            System.out.println(postID + ",回帖成功 : " + commentStr);
    }

    public String getFormHash(String postID) throws IOException {
        String pageURL = "http://rs.xidian.edu.cn/forum.php?mod=viewthread&tid=" + postID + "&extra=page%3D1%26filter%3Dlastpost%26orderby%3Dlastpost&page=1";
        Connection connection = Jsoup.connect(pageURL).timeout(20000).cookies(cookies);
        connection.header(KEY_USER_AGENT, VALUE_USER_AGENT);
        Document document = connection.ignoreContentType(true).get();
        return document.getElementsByAttributeValueContaining("name", "formhash").attr("value");
    }

    private String getPostUrl(String postID) {
        return "http://rs.xidian.edu.cn/forum.php?mod=post&action=reply&fid=72&tid=" + postID + "&extra=page%3D1%26filter%3Dlastpost%26orderby%3Dlastpost&replysubmit=yes&infloat=yes&handlekey=fastpost&inajax=1";
    }

    private String getListPageUrl(int page) {
        return rsWaterUrl + page++;
    }

    public String getCommentMessageRandomly() {
        String[] messges = {"我真的不知道应该说一些什么比较合适啊....", "看完楼主的这个帖子以后,我的心久久不能平静,震撼啊!",
                "楼主听话，快到碗里来！!!!", "楼主,我支持您!希望您以后戒骄戒躁，继续努力",
                "我只想安静的大便，沉湎于怀念。", "前排求粉 哥已经收藏!此贴必火! ......",
                "不知道怎么回事只能绑定了 ", "万恶意淫为首，百善回帖为先",
                "从专业角度来看，楼主的帖子立意肤浅，内容空洞，文笔晦涩，完全是水根本不值一顶；但既然已经上当进来，把帖子点开，不顶，则意味著少积1分，失去早日升级的机会。",
                "闪瞎了我的钛合金狗眼.....", "我不断地踟蹰著，彷徨著，犹豫著。。。", "第一次评论啊，好紧张啊，该怎么说啊，打多少字才显的有文采啊，这样说好不好啊， 会不会成热贴啊，我写的这么好会不会太招遥，写的这么深奥别人会不会看不懂啊，怎样才能写出我博士后的水平呢，半年写了这么多会不会太快啊，好激动啊"};
        int random = Math.abs(new Random().nextInt()) % messges.length;
        return messges[random];
    }
}
