package cn.soroke.springbootDemo.domain.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by song on 2018/1/28.
 */
public class JsonHelper {

    /**
     * 获取json中的指定数据
     */
    public static Object getValueN(Object json,String key) {
        String value = null;
        String sJson = json.toString();
        value = sJson.substring(sJson.indexOf(":",sJson.indexOf(key)) + 2);
        return value.substring(0,value.indexOf("\""));
                //sJson.indexOf(":",sJson.indexOf(key));
    }

    /**
     * 从json中取出key路径的value
     * 例子
     * json={"code":200,"data":{"auth":{"id":"10086"}},"data_2":[{"uid":"321"},{"uid":"123"}]}
     *  key="code"          return 200
     *  key="data.auth"     return {"id":"10086"}
     *  key="data.auth.id"  return "10086"
     *  key="data_2[0]"     return {"uid":"321"}
     *  key="data_2[1].uid" return 123
     *
     * @param json
     *      json串 可以是任意类型
     * @param key
     *      完整的key路径 key之间用.连接
     * @return
     *      value
     */
    public static Object getValue(Object json, String key){
        Object obj = getValue_(json, key);
        if(obj instanceof notFond){
            String lastValue = ((notFond) obj).getValie();
            throw new JSONException("没有从json中找到key\njson:"+json+"\nkey:" + key.substring(0, key.indexOf(lastValue))+lastValue);
        }else {
            return obj;
        }
    }

    /**
     * 从json中删除key字段
     * @param json  同getValue
     * @param key  同getValue
     * @return
     *      删除key之后的json
     */
    public static Object remove(Object json, String key){
        Object obj = remove_(json.toString(), key);
        if(obj instanceof notFond){
            String lastValue = ((notFond) obj).getValie();
            throw new JSONException("没有从json中找到key\njson:"+json+"\nkey:" + key.substring(0, key.indexOf(lastValue))+lastValue);
        }else {
            return obj;
        }
    }

    /**
     *  修改json中删除key字段的值为Object
     * @param json 同getValue
     * @param key 同getValue
     * @param obj1
     *      key的新value
     * @return
     *      修改之后的json
     */
    public static Object update(Object json, String key, Object obj1){
        Object obj = update_(json.toString(), key, obj1);
        if(obj instanceof notFond){
            String lastValue = ((notFond) obj).getValie();
            throw new JSONException("没有从json中找到key\njson:"+json+"\nkey:" + key.substring(0, key.indexOf(lastValue))+lastValue);
        }else {
            return obj;
        }
    }

    /**
     *  对比json
     *  如果有json2里和json1不一样的字段会抛出AssertionError
     * @param json1
     *      json1
     * @param json2
     *      json2
     */
    public static void compare(Object json1, Object json2){
        String oldJson2 = json2.toString();
        json1 = JSON.parse(json1.toString());
        json2 = JSON.parse(json2.toString());

        Object obj = compare_(json1, json2);
        if((obj instanceof JSONObject && 0 != ((JSONObject) obj).size()) || (obj instanceof JSONArray && 0 != ((JSONArray) obj).size())){
            throw new AssertionError("json对比失败\n没有找到"+obj+"\njson1:"+json1+"\njson2:" + oldJson2);
        }else {
            System.err.println("测试通过");
        }
    }

    /**
     * 取json里的value
     * @param json
     * @param key
     * @return
     */
    private static Object getValue_(Object json, String key) {
        key = key.replace("]", "");
        key = key.replace("[", ".");
        String keys[] = key.split("\\.");

        try{
            if(json instanceof JSONArray){
                if(keys.length==1){
                    return ((JSONArray) json).get(Integer.parseInt(keys[0]));
                }else {
                    return getValue_(((JSONArray) json).get(Integer.parseInt(keys[0])), key.substring(key.indexOf(".")+1));
                }
            }
            if(json instanceof JSONObject){
                if(keys.length==1){
                    return ((JSONObject) json).get(keys[0]);
                }else {
                    if(null == ((JSONObject) json).get(keys[0]))
                        return new notFond(keys[0]);
                    else
                        return getValue_(((JSONObject) json).get(keys[0]), key.substring(key.indexOf(".")+1));
                }
            }
            if(json instanceof String){
                return getValue_(JSON.parse((String) json), key);
            }
        }catch (Exception e){
            return new notFond(keys[0]);
        }
        return new notFond(keys[0]);
    }


    /**
     * 删除json里指定位置的数据
     * @param json
     * @param key
     * @return
     */
    public static Object remove_(Object json, String key){
        key = key.replace("]", "");
        key = key.replace("[", ".");
        String keys[] = key.split("\\.");
        try{
            if(json instanceof JSONArray){
                if(keys.length==1){
                    ((JSONArray) json).remove(Integer.parseInt(keys[0]));
                }else {
                    Object newObj = remove_(((JSONArray) json).get(Integer.parseInt(keys[0])), key.substring(key.indexOf(".")+1));
                    ((JSONArray) json).remove(Integer.parseInt(keys[0]));
                    ((JSONArray) json).add(newObj);
                }
            }
            if(json instanceof JSONObject){
                if(keys.length==1){
                    ((JSONObject) json).remove(keys[0]);
                }else {
                    if(null == ((JSONObject) json).get(keys[0])){
                        return new notFond(keys[0]);
                    }
                    else {
                        Object newObj = remove_(((JSONObject) json).get(keys[0]), key.substring(key.indexOf(".") + 1));
                        ((JSONObject) json).remove(keys[0]);
                        ((JSONObject) json).put(keys[0], newObj);
                    }
                }
            }
            if(json instanceof String){
                return remove_(JSON.parse((String) json), key);
            }
        }catch (Exception e){
            return new notFond(keys[0]);
        }
        return json;
    }


    /**
     * 更新json里的值
     * @param json
     * @param key
     * @param obj
     * @return
     */
    private static Object update_(Object json, String key, Object obj){
        key = key.replace("]", "");
        key = key.replace("[", ".");
        String keys[] = key.split("\\.");

        try{
            if(json instanceof JSONArray){
                if(keys.length==1){
                    ((JSONArray) json).remove(Integer.parseInt(keys[0]));
                    ((JSONArray) json).add(keys[0]);
                }else {
                    Object newObj = update_(((JSONArray) json).get(Integer.parseInt(keys[0])), key.substring(key.indexOf(".")+1), obj);
                    ((JSONArray) json).remove(Integer.parseInt(keys[0]));
                    ((JSONArray) json).add(newObj);
                }
            }
            else if(json instanceof JSONObject){
                if(keys.length==1){
                    ((JSONObject) json).remove(keys[0]);
                    ((JSONObject) json).put(keys[0], obj);
                }else {
                    if(null == ((JSONObject) json).get(keys[0])){
                        return new notFond(keys[0]);
                    }
                    else {
                        Object newObj = update_(((JSONObject) json).get(keys[0]), key.substring(key.indexOf(".") + 1), obj);
                        ((JSONObject) json).remove(keys[0]);
                        ((JSONObject) json).put(keys[0], newObj);
                    }
                }
            }
            else if(json instanceof String){
                return update_(JSON.parse((String) json), key, obj);
            }
        }catch (Exception e){
            return new notFond(keys[0]);
        }
        return json;
    }

    /**
     * 对比
     * @param json1
     * @param json2
     * @return
     */
    private static Object compare_(Object json1, Object json2){

        if(json1 instanceof JSONArray && json2 instanceof JSONArray){
            for (int i=((JSONArray) json2).size()-1; i>=0; i--){
                Object newObj = compare_(getValue(json1, String.valueOf(i)), getValue(json2, String.valueOf(i)));
                if(null == newObj
                        || (newObj instanceof JSONObject && ((JSONObject)newObj).size() == 0)
                        || (newObj instanceof JSONArray && ((JSONArray)newObj).size() == 0)){
                    ((JSONArray) json2).remove(i);
                }
            }
        }
        else if (json1 instanceof JSONObject && json2 instanceof JSONObject){
            Iterator<Map.Entry<String, Object>> iterator = ((JSONObject) json2).entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Object> entry = iterator.next();
                Object newObj = compare_(getValue(json1, entry.getKey()), getValue(json2, entry.getKey()));
                if(null == newObj
                        || (newObj instanceof JSONObject && ((JSONObject)newObj).size() == 0)
                        || (newObj instanceof JSONArray && ((JSONArray)newObj).size() == 0)){
                    iterator.remove();
                }
            }
        }else {
            if(json1 == json2 || (null!=json1 && null!=json2 && json1.equals(json2))){
                json2 = null;
            }
        }

        return json2;
    }

    static class notFond{
        String valie;
        public notFond(String valie){
            this.valie = valie;
        }
        public String getValie(){
            return this.valie;
        }
    }

}
