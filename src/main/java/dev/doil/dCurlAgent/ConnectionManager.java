package dev.doil.dCurlAgent;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private URLConnection connection;




    public enum ProtocolType { HTTP,HTTPS };
    public enum MethodType { GET,POST }

    public ConnectionManager() {
        System.out.println("Connection Manager is Started!");
        try {
            ignoreSsl();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    ////////////////////////////////
    /** 내부 connection 객체 관리 **/


    /**
     * manager 내부의 connection 객체 open
     * @param protocolType
     * @param hostDomain
     * @param actionTarget
     */
    public void openConnection(ProtocolType protocolType , String hostDomain, String actionTarget){
        connection = null;

        String urlStr = String.format("%s://%s/%s", protocolType.toString().toLowerCase(), hostDomain, actionTarget);

        try {
            URL url = new URL(urlStr);

            switch(protocolType) {
                case HTTP:
                    connection = (HttpURLConnection) url.openConnection();
                    break;
                case HTTPS:
                    connection = (HttpsURLConnection) url.openConnection();
                    break;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * manager 내부의 connection 객체의 connect 옵션 설정
     * @param method : GET,POST
     * @param params : 파라미터
     */
    public void setConnectionOptions(MethodType method , Map<String,Object> params) {
        if( connection == null) {
            System.out.println("There is no connection");
            return;
        }
        if(params == null) params = new HashMap<String,Object>();

        String userCredentials = "kdi3939:doil";
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        try {
            StringBuilder paramsData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (paramsData.length() != 0) paramsData.append('&');
                paramsData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                paramsData.append('=');
                paramsData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] paramsDataBytes = paramsData.toString().getBytes("UTF-8");


            // method setting
            if(connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).setRequestMethod(method.toString());
            else if(connection instanceof HttpsURLConnection)
                ((HttpsURLConnection) connection).setRequestMethod(method.toString());

            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("Content-Length", paramsData.length()+"");
            connection.setDoOutput(true);
            connection.getOutputStream().write(paramsDataBytes);

        }catch(Exception e){
            e.printStackTrace();
        }
    }



    /**
     * manager 내부의 connection 객체 close
     */
    public void disconnect() {
        if(connection == null ) return;

        if(connection instanceof HttpURLConnection)
            ((HttpURLConnection) connection).disconnect();
        else if(connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection).disconnect();
    }

    /**
     * manager 내부의 connection 객체의 response값
     * @return : response
     */
    public String getResponse() {
        int responseCode = 0;

        try {
            // response CODE get
            if(connection instanceof HttpURLConnection)
                responseCode = ((HttpURLConnection) connection).getResponseCode();
            else if(connection instanceof HttpsURLConnection)
                responseCode = ((HttpsURLConnection) connection).getResponseCode();
            else throw new Exception("connection err");

            if(responseCode == 0) throw new Exception("connection err");



            if (responseCode == 401) {
                System.out.println("401:: Header를 확인해주세요.");
            } else if (responseCode == 500) {
                System.out.println("500:: 서버 에러");
            } else { // response
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(connection != null) this.disconnect();

        return "ERR";
    }


    ///////////////////////////////
    /** 외부 connection 객체 관리 및 처리 **/


    /**
     * 외부 connection 객체에 connection 구현체 반환
     * @param protocolType
     * @param hostDomain
     * @param actionTarget
     * @return
     */
    public URLConnection getConnection(ProtocolType protocolType , String hostDomain, String actionTarget) {
        URLConnection conn = null;

        String urlStr = String.format("%s://%s/%s", protocolType.toString(), hostDomain, actionTarget);

        try {
            URL url = new URL(urlStr);

            switch(protocolType) {
                case HTTP:
                    conn = (HttpURLConnection) url.openConnection();
                    break;
                case HTTPS:
                    ignoreSsl();
                    conn = (HttpsURLConnection) url.openConnection();
                    break;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * 외부 connection 객체의 connect option값을 setting
     * @param conn : connection 객체
     * @param method : GET, POST
     * @param params : 파라미터
     */
    public void setConnectionOptions(URLConnection conn , MethodType method , Map<String,Object> params) {
        if(params == null) params = new HashMap<String,Object>();

        String userCredentials = "kdi3939:doil";
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        try {
            StringBuilder paramsData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (paramsData.length() != 0) paramsData.append('&');
                paramsData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                paramsData.append('=');
                paramsData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] paramsDataBytes = paramsData.toString().getBytes("UTF-8");


            // method setting
            if(conn instanceof HttpURLConnection)
                ((HttpURLConnection) conn).setRequestMethod(method.toString());
            else if(conn instanceof HttpsURLConnection)
                ((HttpsURLConnection) conn).setRequestMethod(method.toString());

            conn.setRequestProperty("Authorization", basicAuth);
            conn.setRequestProperty("Content-Length", paramsData.length()+"");
            conn.setDoOutput(true);
            conn.getOutputStream().write(paramsDataBytes);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    


    /**
     * 외부 connection 객체에 대한 disconnect
     * @param conn
     */
    public void disconnect(URLConnection conn) {
        if(conn instanceof HttpURLConnection)
            ((HttpURLConnection) conn).disconnect();
        else if(conn instanceof HttpsURLConnection)
            ((HttpsURLConnection) conn).disconnect();
    }

    /**
     * 외부 connection 객체에 대한 응답결과
     * @param conn
     * @return response값
     */
    public String getResponse(URLConnection conn){
        int responseCode = 0;

        try {
            // response CODE get
            if(conn instanceof HttpURLConnection)
                responseCode = ((HttpURLConnection) conn).getResponseCode();
            else if(conn instanceof HttpsURLConnection)
                responseCode = ((HttpsURLConnection) conn).getResponseCode();
            else throw new Exception("connection err");

            if(responseCode == 0) throw new Exception("connection err");



            if (responseCode == 401) {
                System.out.println("401:: Header를 확인해주세요.");
            } else if (responseCode == 500) {
                System.out.println("500:: 서버 에러");
            } else { // response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ERR";
    }


    private void ignoreSsl() throws NoSuchAlgorithmException, KeyManagementException {
        System.out.println("ignoring SSL job START....");

    	/*
 	   //SSL 무시하기
        TrustManager[] trustAllCerts = new TrustManager[] {
     	    new X509TrustManager() {
		        	   public X509Certificate[] getAcceptedIssuers() {return null;}
		        	   public void checkClientTrusted(X509Certificate[] certs, String authType) {}
		        	   public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		        	}
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    */


        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        System.out.println("ignored SSL.");
    }

    class miTM implements TrustManager,X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }



}
