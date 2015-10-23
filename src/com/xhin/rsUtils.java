package com.xhin;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class rsUtils {

    // 用户名和密码
    private static String user = null;
    private static String pwd = null;
    // 一会post中要提交的数据
    private static String formHash = null;
    // 最新的帖子的id值
    private static int utlId = 0;
    // 签到页面的URL
    private static String signIn = "http://rs.xidian.edu.cn/plugin.php?id=dsu_paulsign:sign&operation=qiandao&infloat=1&inajax=1";
    // 登陆页面的URL
    private static String logIn = "http://rs.xidian.edu.cn/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1";
    // 瑞思的首页URL
    private static String mainPage = "http://rs.xidian.edu.cn/portal.php";
    // 睿思的论坛首页
    private static String forumPage = "http://rs.xidian.edu.cn/forum.php";
    // 睿思的链接
    private static String mainLink = "http://rs.xidian.edu.cn/";
    // 消息功能的链接
    private static String messageString = "http://rs.xidian.edu.cn/home.php?mod=space&do=pm";
    // 帖子的URL公用地址
    private static String cardUrl = "http://rs.xidian.edu.cn/forum.php?mod=viewthread&tid=";
    // 保存登陆的COOKIE 避免重复验证
    private Map cookies;


    // 两组构造函数
    public rsUtils(String user, String pwd) throws IOException {
        this.user = user;
        this.pwd = pwd;
        iniCookies();
        retToMain();
    }

    public rsUtils() throws IOException {
        iniCookies();
        retToMain();
    }

    // 测试函数 打印函数
    public static void print(String str) {
        System.out.println(str);
    }

    // getter 和 setter 方法
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    // 第一次登陆 初始化cookie
    private void iniCookies() throws IOException {

        // 存储用户名、密码的键值对 ， 用于提交给服务器
        Map userDate = new HashMap();
        userDate.put("username", this.user);
        userDate.put("password", this.pwd);
        Connection connection = Jsoup.connect(logIn);
        // 设置http头信息 ， 模拟火狐浏览器登陆
        connection.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

        // 以post方法提交用户名和密码到服务器
        Connection.Response response = connection.ignoreContentType(true)
                .method(Connection.Method.POST).data(userDate).execute();
        // 存储访问的cookie
        this.cookies = response.cookies();
        Pattern pattern = Pattern.compile("succeed.*");
        Matcher matcher = pattern.matcher(response.body());

        if (matcher.find())
            print("\t\t\t\t\t\t\t恭喜！登陆成功");
        else {
            print("\t\t\t\t\t\t\t\t账号密码有问题  ， 请检查!!");
        }
        //System.out.println(response.body());

    }

    // 登陆首页
    private Connection mainPage() throws IOException {
        // 利用 cookies 进行登陆 避免再次验证
        Connection connection = Jsoup.connect(this.mainPage).cookies(
                this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

        Element element = connection.get().select("[name=formhash]").first();
        this.formHash = element.attr("value");
        connection.execute();
        return connection;
    }

    // 签到的方法
    public void signIn() throws IOException {

        // 登陆签到页面
        Connection connection = Jsoup.connect(this.signIn)
                .cookies(this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        // 签到有点问题
        Map datas = new HashMap();
        datas.put("formhash", this.formHash);
        datas.put("qdxq", "kx");
        datas.put("qdmode", "3");
        datas.put("todaysay", "");
        datas.put("fastreply", "0");
        Connection.Response response = connection.ignoreContentType(true)
                .method(Connection.Method.POST).data(datas).execute();
        String str = response.body();
        Pattern pattern = Pattern.compile("[一-龥]{7}");
        Matcher matcher = pattern.matcher(str);
        // 使用正则式解析是否成功
        while (matcher.find()) {
            System.out.println("");
            System.out.println(matcher.group());
        }
    }

    // 返回瑞思的主页
    private void retToMain() throws IOException {
        mainPage();

    }

    // 从cookie中获取unix时间戳 ， 为提交post数据准备
    private String getUnixTime() {
        String tmp = (String) cookies.get("Q8qA_2132_lastcheckfeed");
        return tmp;
    }

    // 自动评论 刷经验 和积分 ！！！慎用！！！！
    public void Comment(String url, String commentStr) throws IOException {

        Connection connection = Jsoup.connect(url).cookies(this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

        // data 存放要提交的数据
        Map data = new HashMap();
        // 评论的内容、Unix时间戳、formhash 等验证信息
        data.put("message", commentStr);
        data.put("posttime", getUnixTime());
        data.put("usesig", "1");
        data.put("subject", " ");
        Connection.Response response = connection.ignoreContentType(true)
                .method(Connection.Method.POST).data(data).execute();
        // System.out.println(response.body());
    }

    // 查看是否有最新的回复
    private String[] checkMessage() throws IOException {

        Connection connection = Jsoup.connect(this.messageString).cookies(
                this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        System.out.println(connection.execute().body());
        return null;
    }

    // 列出今日热帖的URL ，存进数组
    public String[] todayHot() throws IOException {
        // portal_block_350_content
        // TODO
        String[] link = new String[10];
        Element element = getElementById("portal_block_350_content").child(0);
        Elements elements = element.getElementsByTag("li");
        return getHref(element);

    }

    // 列出最新的帖子的url 存入数组返回
    public String[] latestRealese() throws IOException {
        // div id =portal_block_314_content
        // TODO
        String[] link = new String[10];
        Element element = getElementById("portal_block_314_content").child(0);
        return getHref(element);
    }

    public String[] newReply() throws IOException {
        // portal_block_315_content
        String[] link = new String[10];
        Element element = getElementById("portal_block_315_content").child(0);
        Elements elements = element.getElementsByTag("li");
        // System.out.println("");
        return getHref(element);
    }

    // 根据元素获取内部的href属性，保存在一个字符串数组中
    private String[] getHref(Element element) {

        String[] link = new String[10];
        Elements elements = element.getElementsByTag("li");
        int i = 0;
        for (Element element2 : elements) {
            // System.out.println(element2.html());
            Element aTag = element2.child(0);
            link[i] = this.mainLink + aTag.attr("href");
            i++;
        }
        return link;
    }

    // id选择器的封装
    private Element getElementById(String Id) throws IOException {
        Connection connection = Jsoup.connect(this.forumPage).cookies(
                this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        Element element = connection.get().select("#" + Id).first();
        // 有一个 getallelements 方法 返回所有的元素
        return element;

    }

    // 抓取某个Id 访问过的页面
    private String scanOnePage(String cardId, String usrId) throws IOException {
        // class = xi2
        String URL = this.cardUrl + cardId;

        // 用于存放所有回帖人的数组
        int index = 1;
        Connection connection = getConnection(URL);
        Elements elements = connection.get().select("#postlist")
                .select(".authi").select("a.xi2").select("[target=_blank]");
        for (Element element : elements) {
            // 如果所抓取的id和想要得到的id 一致 ，则存入数组
            if (element.html().toString().equals(usrId)) {
                return this.mainLink + URL + index;
            }
            index++;
        }

        return null;
    }

    // 返回给定的url对应的connnction
    private Connection getConnection(String url) {

        Connection connection = Jsoup.connect(url).cookies(this.cookies);
        connection.header("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        return connection;
    }

    public void setUrlId() throws IOException {

        Connection connection = getConnection(this.forumPage);
        String id = null;
        String href = connection.get().select("#portal_block_314_content")
                .select("li").first().select("a").attr("href");
        //print(href);
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(href);
        while (matcher.find()) {
            id = matcher.group();
        }
        this.utlId = Integer.parseInt(id);
    }

    // 获取urlID的方法
    public int getUrlId() {
        return this.utlId;

    }


    // 抓取最近的页面， 得到想要的ID的最近活动 链接的 最后一位数字是用户所在的楼层数
    public ArrayList scanRecentPage(int begin, int end, String usrId)
            throws IOException {
        // 构造存放URL的线性表数组
        ArrayList arrayList = new ArrayList();
        for (; begin <= end; begin++) {
            String e = scanOnePage(begin + "", usrId);
            if (e != null) {
                // 用这个方法得到id和链接
                //String tmp = (String) e.subSequence(0, e.length() - 1);
                arrayList.add(e);
            }
        }
        return arrayList;
    }

    public String goldCoinUrl(int id) throws IOException {

        // thread_subject
        String url = this.cardUrl + id;
        Connection connection = getConnection(url);
        Element element = connection.get().select("#thread_subject").first();
        //print(element.html());
        Pattern pattern = Pattern.compile("[散抢]*金币");
        Matcher matcher = pattern.matcher(element.html());
        if (matcher.find()) {
            print(matcher.group());
            print(url);
            return url;
        } else {
            return null;
        }
    }

    // 格式化输出 查询的id的最近活动的url和楼层数

    // 测试函数
    public static void main(String[] args) throws IOException {

        rsUtils rsUtils = new rsUtils("rainer", "y389527107");
        rsUtils.signIn();
        /*
         * String[] newReplay = null; newReplay = rsUtils.newReply();
		 * System.out.println(newReplay[2]);
		 *
		 * String[] newRelease = null;
		 *
		 * newRelease = rsUtils.latestRealese();
		 *
		 * System.out.println(newRelease[2]);
		 */

        // rsUtils.checkMessage();
        // rsUtils.specilCareLink("786012", "Rainer");
        // 786038
        // rsUtils.print(rsUtils.scanOnePage("786012", "Rainer"));

        // rsUtils.scanRecentPage(786012, 786038, "Rainer");

        // rsUtils.setUrlId();

        // rsUtils.print(rsUtils.getUtlId()+"");

        //rsUtils.scanRecentPage(786012, 786038, "Rainer");

        rsUtils.goldCoinUrl(785115);

    }

}