package cn.soroke.springbootDemo.controller;

import cn.soroke.springbootDemo.domain.core.Http;
import cn.soroke.springbootDemo.domain.util.JsonHelper;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class HelloWorldController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 获取书籍列表
     * @param bookType  书籍类型：1=有声小说；2=相声评书
     * @param pagesize  数量
     * @return
     */
    @RequestMapping(value = "/recommend.do",method = RequestMethod.GET)
    public String recommend(@RequestParam(value = "booktype") String bookType,@RequestParam(value = "pagesize") String pagesize) {
        Map<Object,Object> params = new HashMap<>();
        params.put("banglei","1");
        params.put("bookTypeId",bookType);
        params.put("oauth_token","");
        params.put("type",bookType);
        params.put("pagesize",pagesize);
        return Http.get("http://app.tingchina.com/index_bang_more.asp",params).getBody();
    }

    /**
     * 获取书籍的详细信息页
     * 参数为 书籍类型：1=有声小说；2=相声评书
     * 书籍ID没什么可以说的
     * @param bookType
     * @param bookid
     * @return
     */
    @RequestMapping(value = "/bookinfo.do",method = RequestMethod.GET)
    public String bookinfo(@RequestParam(value = "booktype") String bookType,@RequestParam(value = "bookid") String bookid) {
        Map<Object,Object> params = new HashMap<>();
        params.put("bookID",bookid);
        params.put("oauth_token","");
        params.put("type",bookType);
        return Http.get("http://app.tingchina.com/book_disp.asp",params).getBody();
    }

    /**
     * 获取书籍的音频列表
     * @param bookType  书籍类型：1=有声小说；2=相声评书
     * @param bookid
     * @return
     */
    @RequestMapping(value = "/bookaudiolist.do",method = RequestMethod.GET)
    public String bookAudioList(@RequestParam(value = "booktype") String bookType,@RequestParam(value = "bookid") String bookid) {
        Map<Object,Object> params = new HashMap<>();
        params.put("bookID",bookid);
        params.put("oauth_token","");
        params.put("type",bookType);
        String bookCount = JsonHelper.getValue(Http.get("http://app.tingchina.com/book_disp.asp",params).getBody(),"data.bookCount").toString();
        params.clear();
        params.put("bookID",bookid);
        params.put("oauth_token","");
        params.put("type",bookType);
        params.put("pend",bookCount);
        params.put("pstr","1");
        return Http.get("http://app.tingchina.com/book_downlist.asp",params).getBody();
    }

    /**
     * 获取播放地址
     * @param bookType  书籍类型：1=有声小说；2=相声评书
     * @param bookid    书籍的ID
     * @param index     书籍的集数
     * @return
     */
    @RequestMapping(value = "/playurl.do",method = RequestMethod.GET)
    public String playurl(@RequestParam(value = "booktype") String bookType,@RequestParam(value = "bookid") String bookid,@RequestParam(value = "index") int index) {
        Map<Object,Object> params = new HashMap<>();
        params.put("bookID",bookid);
        params.put("oauth_token","");
        params.put("bookType",bookType);
        params.put("episodes",index);
        return Http.get("http://app.tingchina.com/play_cdn.asp",params).getBody();
    }

    /**
     * 列表搜索
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/search.do",method = RequestMethod.GET)
    public String search(@RequestParam(value = "keyword") String keyword) {
        Map<Object,Object> params = new HashMap<>();
        params.put("keyword",keyword);
        return Http.get("http://app.tingchina.com/search.asp",params).getBody();
    }

    /**
     * 获取banner图片
     * @return
     */
    @RequestMapping(value = "/getbanner",method = RequestMethod.GET)
    public String getbanner() {
        Map<Object,Object> params = new HashMap<>();
        params.put("num",4);
        params.put("oauth_token","");
        params.put("type",1);
        return Http.get("http://app.tingchina.com/hotnews.asp",params).getBody();
    }

    /**
     * 获取首页数据
     * @return
     */
    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String index() {
        Map<Object,Object> params = new HashMap<>();
        params.put("oauth_token","");
        return Http.get("http://app.tingchina.com/index.asp",params).getBody();
    }

    /**
     * 获取分类列表
     * @return
     */
    @RequestMapping(value = "/gettype",method = RequestMethod.GET)
    public String typeList() {
        Map<Object,Object> params = new HashMap<>();
        params.put("oauth_token","");
        return Http.get("http://app.tingchina.com/type.asp",params).getBody();
    }

    /**
     * 根据分类ID和书籍类型获取书籍列表
     * @param booktypeid    书籍分类ID
     * @param booktype      书籍类型：1=有声小说；2=相声评书
     * @param size          获取数量
     * @return
     */
    @RequestMapping(value = "/gettypelist.do",method = RequestMethod.GET)
    public String gettypelist(@RequestParam(value = "booktypeid") String booktypeid,@RequestParam(value = "booktype") String booktype,@RequestParam(value = "size") int size) {
        Map<Object,Object> params = new HashMap<>();
        params.put("bookTypeId",booktypeid);
        params.put("hotAndNew",2);
        params.put("oauth_token","");
        params.put("pagenum",1);
        params.put("pagesize",size);
        params.put("type",booktype);
        return Http.get("http://app.tingchina.com/typelist.asp",params).getBody();
    }

    /**
     * 获取推荐图书信息
     * @param pagenum   获取页码
     * @param pagesize  获取数量
     * @param type      推荐类型：1=精品推荐；2=新书推荐
     * @return
     */
    @RequestMapping(value = "/getmorebook.do",method = RequestMethod.GET)
    public String index_more(@RequestParam(value = "pagenum") String pagenum,@RequestParam(value = "pagesize") String pagesize,@RequestParam(value = "type") int type) {
        Map<Object,Object> params = new HashMap<>();
        params.put("pagenum",pagenum);
        params.put("pagesize",pagesize);
        params.put("type",type);
        return Http.get("http://app.tingchina.com/index_more.asp",params).getBody();
    }


    /**
     * 首页推荐图书
     * @return
     */
    @RequestMapping(value = "/index_recommend",method = RequestMethod.GET, produces="application/json;charset=UTF-8")
    public String index_recommend() {
        Map<Object,Object> params = new HashMap<>();
        params.put("pagenum",1);
        params.put("pagesize",200);
        params.put("type",1);
        String data = Http.get("http://app.tingchina.com/index_more.asp",params).getBody();
        String book = JsonHelper.getValue(data,"data").toString();
        book = book.replaceAll("\\[","");
        book = book.replaceAll("]","");

        String[] books = book.split("},\\{");

        double d = Math.random();
        int one = (int)(d*200);
        d = Math.random();
        int two = (int)(d*200);
        d = Math.random();
        int three = (int)(d*200);
        d = Math.random();
        int four = (int)(d*200);
        d = Math.random();
        int five = (int)(d*200);
        d = Math.random();
        int six = (int)(d*200);

        String json = "{\"status\":1,\"msg\":\"OK\",\"data\":[";
        json += "{";
        json += books[one];
        json += "},{";
        json += books[two];
        json += "},{";
        json += books[three];
        json += "},{";
        json += books[four];
        json += "},{";
        json += books[five];
        json += "},{";
        json += books[six];
        json += "}]}";

        return json;

    }
}
