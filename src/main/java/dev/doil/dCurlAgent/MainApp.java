package dev.doil.dCurlAgent;


import java.io.*;
import java.util.*;

public class MainApp {

    static Properties props;
    static final int DEFAULT_INT = 1000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[AGENT START]");

        try{
            loadProps();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String protocol = props.getProperty("url.protocol");
        String hostUrl = props.getProperty("url.domain");
        String targetAction = props.getProperty("url.action");

        ConnectionManager.ProtocolType protocolType = null;
        try {
            protocolType = ConnectionManager.ProtocolType.valueOf(protocol.toUpperCase());
        }catch (IllegalArgumentException e){
            System.err.printf("프로토콜 에러 [input : %s]",protocol);
            return;
        }

        String methodStr = props.getProperty("url.method");
        ConnectionManager.MethodType method = null;
        try {
            method = ConnectionManager.MethodType.valueOf(methodStr.toUpperCase());
        }catch (IllegalArgumentException e){
            System.err.printf("메소드 에러 [input : %s] | default 메소드로 설정합니다.(POST)",methodStr);
            method = ConnectionManager.MethodType.POST;
        }

        Map<String,Object> params = new HashMap<String,Object>() {{
            put("host" 		, props.getProperty("tracer.host"));
            put("port" 		, props.getProperty("tracer.port"));
            put("ip" 		, "127.0.0.1");
            put("loginId" 	, "doil");
            put("pageUrl" 	, props.getProperty("tracer.pageUrl"));
            put("userAgent" , props.getProperty("tracer.userAgent"));
        }};

        // ip값 구성

        int randomIP_max = DEFAULT_INT;

        try{
            randomIP_max = Integer.parseInt(props.getProperty("tracer.randomIP_max"));
        }catch(NumberFormatException e){ // 정수형이 아니라서 파싱 실패한 경우 default 값인 1000으로 셋팅
            e.printStackTrace();
            randomIP_max = DEFAULT_INT;
        }



        System.out.println("[RANDOM IP LIST-UP] START");
        List<String> ipList = new ArrayList<>();

        Random r = new Random();
        for(int i=0;i<randomIP_max;i++) {
            int a = r.nextInt(255) + 1; // 1~255
            int b = r.nextInt(256); // 0~255
            int c = r.nextInt(256); // 0~255
            int d = r.nextInt(255) + 1; // 1~255
            String ip = String.format("%d.%d.%d.%d",a,b,c,d);
            ipList.add(ip);
        }
        System.out.printf("[RANDOM IP LIST-UP(size : %d)] END\n",randomIP_max);


        ConnectionManager cManager = new ConnectionManager();

        System.out.println("---------------[THREAD START]-----------------");
        while (true) {
            int randomIpIdx = r.nextInt(ipList.size());
            String ip = ipList.get(randomIpIdx);
            String loginId = ip + "_WC";
            params.put("ip" , ip);
            params.put("loginId" , loginId);

            cManager.openConnection(protocolType,hostUrl,targetAction);
            cManager.setConnectionOptions(method,params);
            // REQUEST LOGGIN START SIGN
            System.out.printf("-----------request-----------> \n (%s) %s://%s/%s\n" ,
                    method ,
                    protocolType.toString().toLowerCase() ,
                    hostUrl ,
                    targetAction
            );
            System.out.print("res : ");

            String result = cManager.getResponse();
            System.out.println(result);
            System.out.println("------------[END]------------\n");

            Thread.sleep(100);
        }



    }

    private static void loadProps() throws IOException {

        String propPath = "props/app.properties";


        props = new Properties();


        File file = new File(propPath);
        InputStream is = new FileInputStream(file);
        props.load(is);
    }

}
