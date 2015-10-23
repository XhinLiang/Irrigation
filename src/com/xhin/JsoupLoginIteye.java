package com.xhin;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class JsoupLoginIteye {



    /**
     * 模拟登陆Iteye
     *
     * @param userName 用户名
     * @param pwd      密码
     **/
    public void login(String userName, String pwd) throws Exception {

        //第一次请求
        Connection con = Jsoup.connect("http://rs.xidian.edu.cn/forum.php");//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        Connection.Response rs = con.execute();//获取响应
        Document d1 = Jsoup.parse(rs.body());//转换为Dom树
        //获取，cooking和表单属性，下面map存放post时的数据
        Map<String, String> datas = new HashMap<>();
        for (Element e : d1.getAllElements()) {
            if (e.attr("name").equals("username")) {
                e.attr("value", userName);//设置用户名
            }

            if (e.attr("name").equals("password")) {
                e.attr("value", pwd); //设置用户密码
            }

            if (e.attr("name").length() > 0) {//排除空值表单属性
                datas.put(e.attr("name"), e.attr("value"));
            }
        }


        /**
         * 第二次请求，post表单数据，以及cookie信息
         *
         * **/
        Connection con2 = Jsoup.connect("http://rs.xidian.edu.cn/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1");
        con2.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        //设置cookie和post上面的map数据
        Connection.Response login = con2.ignoreContentType(true).method(Connection.Method.POST).data(datas).cookies(rs.cookies()).execute();
        //打印，登陆成功后的信息
        System.out.println(login.body());

        //登陆成功后的cookie信息，可以保存到本地，以后登陆时，只需一次登陆即可
        Map<String, String> map = login.cookies();
        for (String s : map.keySet()) {
            System.out.println(s + "      " + map.get(s));
        }

    }


}
