
package exdev.com.util;

import java.util.Map;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class CommUtil {

    /** 
     * 내용        : Map 데이타를 json데티아로 변황
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 11. 21 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */  
    public static String mapToJson(Map<String, String>  mapData) throws Exception {
        // ObjectMapper를 사용해 Map을 JSON으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(mapData);
        return jsonString;

    }

    /** 
     * 내용        : Exception 데이타를 String데티아로 변황
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 20 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    public static String getPrintStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
      }

    /** 
     * 내용        : Object를 String 으로 변환
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 20 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    public static String ObjectToString(Object data) {
        String result; 
        if (data instanceof Integer) {
            result = String.valueOf(data); // Integer를 String으로 변환
        } else if (data instanceof String) {
            result = (String) data; // 이미 String이므로 그대로 사용
        } else if (data instanceof Double) {
            result = String.valueOf(data); // Double을 String으로 변환
        } else if (data == null) {
            result = null; // null인 경우 "null"로 처리
        } else {
            result = data.toString(); // 나머지 객체들은 toString() 호출
        }
        return result;
    }


}
