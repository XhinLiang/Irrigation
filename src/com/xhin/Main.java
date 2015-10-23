package com.xhin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xhinliang on 15-10-8.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String username = null;
        String password = null;
        if (args[0] != null)
            username = args[0];
        if (args[1] != null)
            password = args[1];
        if (username == null || password == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("username");
            username = br.readLine();
            System.out.println("username");
            password = br.readLine();
            br.close();
        }
        RsWater water = new RsWater(username, password);
        System.out.println(water.login());
        water.water();
    }
}
