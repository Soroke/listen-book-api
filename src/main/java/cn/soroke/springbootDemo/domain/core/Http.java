package cn.soroke.springbootDemo.domain.core;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

import static org.apache.commons.codec.Charsets.UTF_8;

/**
 * Created by song on 2018/2/28.
 */
public class Http {

     /**
     * headers
     */
     private static Map<Object,Object> headers = new HashMap<Object, Object>();

    /**
     * MultipartEntityBuilder
     */
    private static MultipartEntityBuilder builder = null;
    /**
     * Cookies
     */
     private static CookieStore cookieStore = null;

    /**
     * 编码默认为utf-8
     */
     private static String encode = "utf-8";

    /**
     * log4j打log
     */
    private static Logger log = Logger.getLogger(Http.class);
    /**
     * 请求超时设置时长
     * 单位秒
     */
    private static int timeOut = 15;

    /**
     * 构造httprequest设置
     * 设置请求和传输超时时间
     */
    private static RequestConfig config = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).build();

    /**
     * 添加header的方法
     * @param httpRequestBase
     *      http的对象例如 HttpGet、HttpPost
     */
    private static void addHeaderToHttpRequest(HttpRequestBase httpRequestBase) {
        headers.put("Connection", "close");
        if(!headers.isEmpty()) {
            for(Map.Entry<Object, Object> entry : headers.entrySet()){
                httpRequestBase.addHeader(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    public static void setHeader(String key,String value) {
        Map<Object,Object> hh = new HashMap<Object, Object>();
        hh.put(key,value);
        setHeader(hh);
    }

    public static void setHeader(Map<Object,Object> header) {
        if(!header.isEmpty()) {
            for(Map.Entry<Object, Object> entry : header.entrySet()){
                headers.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    /**
     * 设置cookies
     * @param cookieStore1
     */
    public static void setCookieStore(CookieStore cookieStore1) {
        cookieStore = cookieStore1;
    }

    /**
     * post请求的传参方法
     * @param url
     * @param params
     * @return
     */
    public static Response post(String url,Map<Object,Object> ... params) {
        return postRealization(url,params[0]);
    }

//    /**
//     * post请求的传参方法
//     * @param url
//     * @param params
//     * @return
//     */
//    public static Response post(String url, String fileParamName,File file, Map<Object,Object> ... params) {
//        builder.addBinaryBody(fileParamName, file);
//        if (params.length != 0) {
//            for (Map.Entry<Object, Object> entry : params[0].entrySet()) {
//                builder.addTextBody(entry.getKey().toString(), entry.getValue().toString(), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
//            }
//        }
//        HttpEntity httpEntity = builder.build();
//        return postRealization(url,httpEntity);
//    }
//
//    private static Response post(String url,Map<Object,Object> params) {
//        for (Map.Entry<Object, Object> entry : params.entrySet()) {
//            builder.addTextBody(entry.getKey().toString(), entry.getValue().toString(), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
//        }
//        HttpEntity httpEntity = builder.build();
//        return postRealization(url,httpEntity);
//    }

    /**
     * post请求的传参方法
     * @param url
     * @param params
     * @return
     */
    public static Response post(String url,String params) {
        Map<Object,Object> paramsN = new HashMap<Object,Object>();
        String[] pam = params.split(";");
        for (String p:pam) {
            String pp[] = p.split("=");
            boolean b = true;
            try {
                pp[1].equals("");
            } catch (ArrayIndexOutOfBoundsException e) {
                b = false;
                paramsN.put(pp[0],"");
            }
            if (b) {
                if (pp[1].contains("'semicolon'")) {
                    paramsN.put(pp[0],pp[1].replaceAll("'semicolon'",";"));
                } else {
                    paramsN.put(pp[0],pp[1]);

                }
            }
        }
        return postRealization(url,paramsN);
    }

    /**
     * get请求的传参
     * @param url
     * @param params
     * @return
     */
    public static Response get(String url,Map<Object,Object> ... params) {
        if (params.length == 0) {
            return getRealization(url);
        } else {
            return getRealization(url,params[0]);
        }

    }
    /**
     * get请求的传参
     * @param url
     * @param params
     * @return
     */
    public static Response get(String url,String params) {
        Map<Object,Object> paramsN = new HashMap<Object,Object>();
        String[] pam = params.split(";");
        if (pam.length == 1) {
            pam = params.split("&");
        }
        for (String p:pam) {
            boolean b = true;
            String pp[] = p.split("=");
            try {
                pp[1].equals("");
            } catch (ArrayIndexOutOfBoundsException e) {
                b = false;
                paramsN.put(pp[0],"");
            }


            if (b) {
                if (pp[1].contains("'semicolon'")) {
                    paramsN.put(pp[0],pp[1].replaceAll("'semicolon'",";"));
                } else {
                    paramsN.put(pp[0],pp[1]);

                }
            }
        }
        return getRealization(url,paramsN);
    }

    /**
     * 发送get请求
     *
     * @return
     *  Request对象
     *  包含：
     *      请求类型
     *      url
     *      返回结果
     *      状态码
     *      响应时间
     */
    public static Response getRealization(String url,Map<Object,Object> ... params) {

        if (cookieStore == null)  cookieStore = new BasicCookieStore();
        HttpClient httpClient =  HttpClientBuilder.create().setDefaultRequestConfig(config).setDefaultCookieStore(cookieStore).build();

        String baseUrl = url;
        //请求返回实体对象
        Response rsp = new Response();
        //用于计算接口请求响应时间
        Long startTime = 0L;
        long runTime = 0L;
        log.info("------------------------开始请求------------------------");
        log.info("接口类型：get");
        log.info("接口URL为：" + url);
        /**
         * create by song
         * 如果有参数
         * 设置get请求参数 并和url进行拼接为完整的请求地址
         * 设置请求参数的编码 格式为utf-8
         */
        Map<Object,Object> pam = new HashMap<Object,Object>();
        if (!(params.length == 0)) {
            pam = params[0];
        }
        if(!pam.isEmpty()) {
            String param = "";
            for (Map.Entry<Object,Object> entry : pam.entrySet()) {
                if(!param.equals("")){
                    param = param + "&";
                }
                try{
                    param += URLEncoder.encode(entry.getKey().toString(),encode)+"="+URLEncoder.encode(entry.getValue().toString(),encode);
                } catch (IOException ioe) {
                    log.error("get请求参数设置utf-8编码时出错");
                    ioe.printStackTrace();
                }
                //打印请求参数信息
                log.info("参数：\"" +entry.getKey() + "\":\"" + entry.getValue() + "\"");
            }
            url = url + "?" + param;
        }
log.info("测试全量url：" + url);
        HttpGet httpGet = null;
        HttpResponse response = null;
        int count = 0;
        /**
         * 接口测试不通过时，暂停1s继续请求；
         * 如果三次都不通过再判定测试失败
         */
        while(count < 3) {
            httpGet = new HttpGet(url);
            addHeaderToHttpRequest(httpGet);

            //请求开始时间
            startTime = new Date().getTime();
            try {
                response = httpClient.execute(httpGet);
                rsp.setBody(EntityUtils.toString(response.getEntity(), encode));
                runTime = new Date().getTime() - startTime;
            } catch(IOException ioe) {
                log.error("get请求发送时出错！");
                ioe.printStackTrace();
                return null;
            } catch (NullPointerException npe) {
                log.error("没有设置URL参数！");
                npe.printStackTrace();
                return null;
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                count ++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (count == 3) {
                    log.error("接口:" + baseUrl + "请求返回结果为失败；已重试3次，接口测试失败。");
                } else {
                    log.error("接口:" + baseUrl + "请求返回结果为失败；开始第" + count + "次重试");
                }
            } else {
                count = 5;
            }
        }
        log.info("接口响应时间为：" + runTime + "ms");
        log.info("------------------------请求结束------------------------");
        rsp.setUrl(url);
        rsp.setRunTime(runTime);
        rsp.setResponseType(ResponseType.GET);
        rsp.setCookies(cookieStore);
        //保存header
        Header[] headers = response.getAllHeaders();
        Map<Object,Object> hashMap = new HashMap<Object,Object>();
        for (Header header:headers) {
            hashMap.put(header.getName(),header.getValue());
        }
        rsp.setHeaders(hashMap);
        rsp.setStatusCode(response.getStatusLine().getStatusCode());
        log.info(rsp);
        return rsp;
    }


    /**
     * 发送post请求
     *
     * @return
     *  Request对象
     *  包含：
     *      请求类型
     *      url
     *      参数
     *      返回结果
     *      状态码
     *      响应时间
     */
    public static Response postRealization(String url,Map<Object,Object> ... params) {
        if (cookieStore == null)  cookieStore = new BasicCookieStore();
        HttpClient httpClient =  HttpClientBuilder.create().setDefaultRequestConfig(config).setDefaultCookieStore(cookieStore).build();
        //请求返回实体对象
        Response rsp = new Response();
        //用于计算接口请求响应时间
        Long startTime = 0L;
        long runTime = 0L;
        log.info("------------------------开始请求------------------------");
        log.info("接口类型：post");
        log.info("接口URL为：" + url);
        HttpPost httpPost = new HttpPost(url);
        addHeaderToHttpRequest(httpPost);
        /**
         * 检查参数是否为空
         * 如果存在参数循环添加
         */
        Map<Object,Object> pam = new HashMap<Object,Object>();
        if (!(params.length == 0)) {
            pam = params[0];
        }
        if(!pam.isEmpty()) {
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            for (Map.Entry<Object,Object> entry : pam.entrySet()) {
                log.info("参数：\"" +entry.getKey() + "\":\"" + entry.getValue() + "\"");
                param.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
            }
            HttpEntity entity = new UrlEncodedFormEntity(param,UTF_8);
            httpPost.setEntity(entity);
        }
        int count = 0;
        HttpResponse response = null;
        /**
         * 接口测试不通过时，暂停1s继续请求；
         * 如果三次都不通过再判定测试失败
         */
        while(count < 3 ) {
            //计算请求开始时间
            startTime = new Date().getTime();
            try {
                response = httpClient.execute(httpPost);
                rsp.setBody(EntityUtils.toString(response.getEntity(), encode));
                runTime = new Date().getTime() - startTime;
            } catch(IOException ioe) {
                log.error("post请求发送时出错");
                ioe.printStackTrace();
                return null;
            }catch (NullPointerException npe) {
                log.error("没有设置URL参数！");
                npe.printStackTrace();
                return null;
            }
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count ++;
                if (count == 3) {
                    log.error("接口:" + url + "请求返回结果为失败；已重试3次，接口测试失败。");
                } else {
                    log.error("接口:" + url + "请求返回结果为失败；开始第" + count + "次重试");
                }
            } else {
                count = 5;
            }
        }

        log.info("接口响应时间为：" + runTime + "ms");
        log.info("------------------------请求结束------------------------");
        rsp.setUrl(url);
        rsp.setRunTime(runTime);
        rsp.setResponseType(ResponseType.POST);
        rsp.setCookies(cookieStore);
        //保存header
        Header[] headers = response.getAllHeaders();
        Map<Object,Object> hashMap= new HashMap<Object,Object>();
        for (Header header:headers) {
            hashMap.put(header.getName(),header.getValue());
        }
        rsp.setHeaders(hashMap);
        rsp.setStatusCode(response.getStatusLine().getStatusCode());
        log.info(rsp);
        return rsp;
    }


    /**
     * 发送附带文件的post请求
     * @param serverUrl url地址
     * @param fileParamName 文件类型
     * @param file  文件
     * @param params  参数
     * @return
     */
    public static Response post(String serverUrl, String fileParamName, File file, Map<Object, Object> params) {
        Response rsp = new Response();
        //用于计算接口请求响应时间
        Long startTime = 0L;
        long runTime = 0L;
        log.info("------------------------开始请求------------------------");
        log.info("接口类型：post");
        log.info("接口URL为：" + serverUrl);
        HttpPost httpPost = new HttpPost(serverUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // 上传的文件
        builder.addBinaryBody(fileParamName, file);
        // 设置其他参数
        for (Map.Entry<Object, Object> entry : params.entrySet()) {
            log.info("参数：\"" +entry.getKey() + "\":\"" + entry.getValue() + "\"");
            builder.addTextBody(entry.getKey().toString(), entry.getValue().toString(), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
        }
        HttpEntity httpEntity = builder.build();
        httpPost.setEntity(httpEntity);
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            startTime = new Date().getTime();
            response = httpClient.execute(httpPost);
            rsp.setBody(EntityUtils.toString(response.getEntity(), encode));
            runTime = new Date().getTime() - startTime;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("接口响应时间为：" + runTime + "ms");
        log.info("------------------------请求结束------------------------");
        rsp.setUrl(serverUrl);
        rsp.setRunTime(runTime);
        rsp.setResponseType(ResponseType.POST);
        rsp.setCookies(cookieStore);
        if (null == response || response.getStatusLine() == null) {
//            logger.info("Post Request For Url[{}] is not ok. Response is null", serverUrl);
            return null;
        } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            logger.info("Post Request For Url[{}] is not ok. Response Status Code is {}", serverUrl,
//                    response.getStatusLine().getStatusCode());
            return null;
        }
        //保存header
        Header[] headers = response.getAllHeaders();
        Map<Object,Object> hashMap= new HashMap<Object,Object>();
        for (Header header:headers) {
            hashMap.put(header.getName(),header.getValue());
        }
        rsp.setHeaders(hashMap);
        rsp.setStatusCode(response.getStatusLine().getStatusCode());
        log.info(rsp);
        return rsp;
    }
}
