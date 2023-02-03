package dev.doil.dCurlAgent;


import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class MainApp {

    static Properties props;
    static Properties parameters;
    static final int DEFAULT_INT = 1000;
    static final SecureRandom r = new SecureRandom();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[AGENT START]");

        try{
            loadProps();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Worker worker = new Worker();
        worker.work();

    }

    private static void loadProps() throws IOException {

        String propPath = "props/app.properties";
        String paramPath = "props/params.properties";

        props = new Properties();
        parameters = new Properties();

        props.load(new FileInputStream(new File(propPath)));
        parameters.load(new FileInputStream(new File(paramPath)));
    }





}
