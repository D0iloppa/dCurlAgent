package dev.doil.dCurlAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker {

    public void work() throws InterruptedException {
        String protocol = MainApp.props.getProperty("url.protocol");
        String hostUrl = MainApp.props.getProperty("url.domain");
        String targetAction = MainApp.props.getProperty("url.action");

        ConnectionManager.ProtocolType protocolType = null;
        try {
            protocolType = ConnectionManager.ProtocolType.valueOf(protocol.toUpperCase());
        }catch (IllegalArgumentException e){
            System.err.printf("프로토콜 에러 [input : %s]",protocol);
            return;
        }

        String methodStr = MainApp.props.getProperty("url.method");
        ConnectionManager.MethodType method = null;
        try {
            method = ConnectionManager.MethodType.valueOf(methodStr.toUpperCase());
        }catch (IllegalArgumentException e){
            System.err.printf("메소드 에러 [input : %s] | default 메소드로 설정합니다.(POST)",methodStr);
            method = ConnectionManager.MethodType.POST;
        }


        String paramsPrefix = MainApp.props.getProperty("prefix");
        if(paramsPrefix == null || "".equals(paramsPrefix)) paramsPrefix = "tracer";

        Map<String,Object> tracerParams = getParameters(paramsPrefix);
        Map<String,Object> params2 = getParameters();

        // ip값 구성
        int randomIP_max = MainApp.DEFAULT_INT;

        try{
            randomIP_max = Integer.parseInt(MainApp.props.getProperty("randomIP_max"));
        }catch(NumberFormatException e){
            // 정수형이 아니라서 파싱 실패한 경우 default 값인 1000으로 셋팅
            e.printStackTrace();
            randomIP_max = MainApp.DEFAULT_INT;
        }

        List<String> ipList = randomIp_listUp(randomIP_max);


        ConnectionManager cManager = new ConnectionManager();

        System.out.println("---------------[THREAD START]-----------------");

        while (true) {
            int randomIpIdx = MainApp.r.nextInt(ipList.size());
            String ip = ipList.get(randomIpIdx);
            String loginId = ip + "_WC";
            tracerParams.put("ip" , ip);
            tracerParams.put("loginId" , loginId);

            cManager.openConnection(protocolType,hostUrl,targetAction);
            cManager.setConnectionOptions(method,tracerParams);
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


    private static List<String> randomIp_listUp(int randomIP_max) {
        System.out.println("[RANDOM IP LIST-UP] START");

        List<String> result = new ArrayList<>();

        for(int i = 0 ; i < randomIP_max ; i++) {
            int a = MainApp.r.nextInt(255) + 1; // 1~255
            int b = MainApp.r.nextInt(256); // 0~255
            int c = MainApp.r.nextInt(256); // 0~255
            int d = MainApp.r.nextInt(255) + 1; // 1~255
            String ip = String.format("%d.%d.%d.%d" , a , b , c , d);
            result.add(ip);
        }

        System.out.printf("[RANDOM IP LIST-UP(size : %d)] END\n" , result.size());

        return result;
    }

    private static Map<String, Object> getParameters() {
        Map<String,Object> result = new HashMap<>();

        MainApp.parameters.entrySet().forEach(item->{
            String k = item.getKey().toString();
            Object v = item.getValue();
            result.put( k , v);
        });


        return result;
    }


    private static Map<String, Object> getParameters(String mode) {
        Map<String,Object> result = new HashMap<>();

        MainApp.props.entrySet().forEach(param->{
            String k = param.getKey().toString();
            if(k.contains(mode)) {
                // key prefix 제거
                k = k.replace((mode+"."),"");
                result.put(k,param.getValue());
            }
        });

        return result;
    }





}
