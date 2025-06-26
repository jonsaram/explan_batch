package exdev.com.common.service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import exdev.com.ExdevCommonAPI;
import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.util.CommUtil;
import exdev.com.util.DateUtil;

@Service("JobSchedulerService")
public class JobSchedulerService {
    @Autowired
    private ExdevCommonDao commonDao;

    /** 
     * 내용        : API 전송로그
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 02: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean insertApiTransferLog( Map<String, String> saveMap ) throws Exception {
        
        if( saveMap.get("companyCd") == null || "".equals(saveMap.get("companyCd")) ) { saveMap.put("companyCd", ""); }
        if( saveMap.get("apiCd") == null || "".equals(saveMap.get("apiCd")) ) { saveMap.put("apiCd", ""); }
        if( saveMap.get("reqType") == null || "".equals(saveMap.get("reqType")) ) { saveMap.put("reqType", ""); }
        if( saveMap.get("url") == null || "".equals(saveMap.get("url")) ) { saveMap.put("url", ""); }
        if( saveMap.get("reqParam") == null || "".equals(saveMap.get("reqParam")) ) { saveMap.put("reqParam", ""); }
        if( saveMap.get("resParam") == null || "".equals(saveMap.get("resParam")) ) { saveMap.put("resParam", ""); }
        if( saveMap.get("successYn") == null || "".equals(saveMap.get("successYn")) ) { saveMap.put("successYn", ""); }
        if( saveMap.get("errorCd") == null || "".equals(saveMap.get("errorCd")) ) { saveMap.put("errorCd", ""); }
        if( saveMap.get("errorMsg") == null || "".equals(saveMap.get("errorMsg")) ) { saveMap.put("errorMsg", ""); }
        if( saveMap.get("createUser") == null || "".equals(saveMap.get("createUser")) ) { saveMap.put("createUser", ""); }
        
        
        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("logId", saveMap.get("logId"));
        
        Map insertInfo = (Map)commonDao.getObject("jobScheduler.getApiTransferLog", searchMap);
        
        int result = 0;
        if( insertInfo == null) {
            result = commonDao.update("jobScheduler.insertApiTransferLog", saveMap);
        }else {
            result = commonDao.update("jobScheduler.updateApiTransferLog", saveMap);
        }
        
        return true;
        
    }

    /** 
     * 내용        : 배치 실행 상태 저장
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 04: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean ApiBachState( Map<String, String> saveMap ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("brandId", saveMap.get("brandId"));
        searchMap.put("storeId", saveMap.get("storeId"));
        searchMap.put("yyyymmdd", saveMap.get("yyyymmdd"));
        searchMap.put("apiCd", saveMap.get("apiCd"));
        
        Map insertInfo = (Map)commonDao.getObject("jobScheduler.getApiBachState", searchMap);
        
        if( insertInfo == null) {

            saveMap.put("id", ExdevCommonAPI.makeUniqueID(16)); 
            saveMap.put("successYn", "N");
            saveMap.put("createUser", "BATCH");
            commonDao.update("jobScheduler.insertApiBachState", saveMap);
        }else {
            commonDao.update("jobScheduler.updateApiBachState", saveMap);
        }
        
        return true;
        
    }

    /** 
     * 내용        : 지점조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 10: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map> getStoreMst( Map<String, String> map ) throws Exception {
        
        List<Map> listMap = commonDao.getList("jobScheduler.getStoreMst", map);
        
        return listMap;
        
    }
    /*==================================================================================================================*/
    /* 유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트          */

    /** 
     * 내용        : 유니온소프트-지점정보 조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 31 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposStoreList( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("posCompCdNull", "N");// 포스CD가 NOT NULL 
        searchMap.put("posGroupIdNull", "N");// 포스 그룹ID가 NOT NULL 
        
        List<Map> listMap = commonDao.getList("system.getBuyerBrands", searchMap);
        
        for(Map resultMap : listMap) {
            
            String posCompCd = (String)resultMap.get("POS_COMP_CD");
            String posGroupId = (String)resultMap.get("POS_GROUP_ID");
            
            if( ((String)ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd)) {
                unionposStoreListAPI(resultMap);
            }
        }
        
    }

    /** 
     * 내용        : 유니온소프트-일별 메뉴 매출 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposStoreListAPI( Map map ) throws Exception {
        
        String brandId = (String)map.get("BRAND_ID");
        
        String urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/list";
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        String logId = ExdevCommonAPI.makeUniqueID(16);
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setConnectTimeout(10000); // 연결 시간 초과 설정
            connection.setReadTimeout(17000);   // 읽기 시간 초과 설정
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정

            Map<String, String> reqMap = new HashMap<String, String>();
            
            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);

            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.UNIONSOFT.name()); 
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);

            // JSON 출력
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 데이터를 Map으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
            
            // DATA 객체 추출
            Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

            // StoreItemList 추출
            List<Map<String, Object>> StoreList = (List<Map<String, Object>>) data.get("StoreList");

            if( StoreList != null && !StoreList.isEmpty() ) {
                
                for ( Map<String, Object> item : StoreList ) {
                    
                    String bizNo     = ((String)item.get("BizNo")).replace("-", "");
                    String storeCode = (String)item.get("StoreCode");
                    
                    // 데이터를 Map에 저장
                    Map<String, String> saveMap = new HashMap<>();
                    saveMap.put("brandId", brandId);
                    saveMap.put("businessRegnum", bizNo);
                    saveMap.put("posCd", storeCode);
                    saveMap.put("updateUser", "BATCH");

                    commonDao.insert("jobScheduler.updateStoreMst", saveMap);//임시
                    
                    
                }
            }

        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }
    /** 
     * 내용        : 유니온소프트-일별 매출 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 12. 30 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionsoftStoreSales( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("posUseYn", "Y");// 포스 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("posCdNull", "N");// 포스CD가 NOT NULL 
        searchMap.put("apiCd", (String)ExdevConstants.API_CD.STORE_SALES.name());//일별 매출
        
        
        List<Map> listMap = commonDao.getList("jobScheduler.getSalebachTargetStore", searchMap);
        
       
        for(Map resultMap : listMap) {
            /* */
            String posCompCd = (String)resultMap.get("POS_COMP_CD");
            String posCd = (String)resultMap.get("POS_CD");
            
            if( ((String)ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd)) {
            	if(posCd != null && !"".equals(posCd)) {
            	    unionsoftStoreSalesAPI(resultMap);
            	}
            }
        }
    }

    /** 
     * 내용        : 유니온소프트-일별매출조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 13 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionsoftStoreSalesAPI( Map map ) throws Exception {
    
        
        String posCd = (String)map.get("POS_CD");
        String storeId = (String)map.get("STORE_ID");
        String posGroupId = (String)map.get("POS_GROUP_ID");
        String brandId = (String)map.get("BRAND_ID");
        String yyyymmdd = (String)map.get("YYYYMMDD");
        
        String urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/day";
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        
        String apiCd = (String)ExdevConstants.API_CD.STORE_SALES.name();
        String apiCompCd = (String)ExdevConstants.API_COMPANY.UNIONSOFT.name();
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);
        try {

            Map<String, String> reqMap = new HashMap<String, String>();
        
            reqMap = new HashMap<String, String>();
            reqMap.put("StoreCode", posCd);
            reqMap.put("StartDate", yyyymmdd);
            reqMap.put("EndDate", yyyymmdd);
            String reqParam = "StoreCode["+posCd+"], StartDate["+yyyymmdd+"], EndDate["+yyyymmdd+"]";
            
            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정
            
            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);

            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
            }
            
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.UNIONSOFT.name());
            logMap.put("apiCd", (String)ExdevConstants.API_CD.STORE_SALES.name());
            logMap.put("reqParam", reqParam);
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // JSON 출력
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 데이터를 Map으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
            
            // DATA 객체 추출
            Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

            // DayList 추출
            List<Map<String, Object>> DayList = (List<Map<String, Object>>) data.get("DayList");

            // StoreItemList 출력
            for (Map<String, Object> item : DayList) {
                
                String StoreSalesId = ExdevCommonAPI.makeUniqueID(16);
                // EndTimeDate 값
                String EndTimeDate = (String)item.get("EndTimeDate");
                
                // 문자열을 자르고 변환
                String year = EndTimeDate.substring(0, 4);
                String month = EndTimeDate.substring(4, 6);
                String day = EndTimeDate.substring(6, 8);
                
                // 원하는 형식으로 결합
                String yyyymmdd1 = year + "-" + month + "-" + day;
                
                item.put("StoreSalesId", StoreSalesId);
                item.put("StoreId", storeId);
                item.put("yyyymmdd", yyyymmdd1);

                commonDao.insert("jobScheduler.updateStoreSalesMst", item);
                /*
                Map<String, String> updateMapApiBachState = new HashMap<String, String>();
                
                updateMapApiBachState.put("brandId", brandId);
                updateMapApiBachState.put("storeId", storeId);
                updateMapApiBachState.put("apiCd",  apiCd);
                updateMapApiBachState.put("apiCompCd",  apiCompCd);
                updateMapApiBachState.put("yyyymmdd", year+month+day);
                updateMapApiBachState.put("successYn", "Y");
                updateMapApiBachState.put("updateUser", "BATCH");
                
                ApiBachState(updateMapApiBachState);
                */

            }
                
            
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
    }

    /** 
     * 내용        : 유니온소프트-일별 매출 조회수동 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 12. 30 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionsoftStoreSalesManual( Map dateMap ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("posUseYn", "Y");// 포스 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("posCdNull", "N");// 포스CD가 NOT NULL 
        searchMap.put("apiCd", (String)ExdevConstants.API_CD.STORE_SALES.name());//일별 매출
        
        
        List<Map> listMap = commonDao.getList("jobScheduler.getSalebachTargetStoreManual", searchMap);
        
       
        for(Map resultMap : listMap) {
            String posCompCd = (String)resultMap.get("POS_COMP_CD");
            String posCd = (String)resultMap.get("POS_CD");
            
            if( ((String)ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd)) {
                if(posCd != null && !"".equals(posCd)) {
                    unionsoftStoreSalesAPIManual(resultMap,dateMap);
                }
            }
        }
    }
    /** 
     * 내용        : 유니온소프트-일별매출조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 13 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionsoftStoreSalesAPIManual( Map map, Map dateMap ) throws Exception {
    
        
        String posCd = (String)map.get("POS_CD");
        String StoreId = (String)map.get("STORE_ID");
        String posGroupId = (String)map.get("POS_GROUP_ID");
        
        String urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/day";
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);

        // 시작 날짜와 종료 날짜 설정
        LocalDate startDate = LocalDate.parse( (String)dateMap.get("startDate"), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate   = LocalDate.parse( (String)dateMap.get("endDate"), DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> dateList = new ArrayList<>();

        // 날짜를 증가시키면서 리스트에 추가
        while (!startDate.isAfter(endDate)) {
            dateList.add(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            startDate = startDate.plusDays(1);  // 날짜 1일 증가
        }

        
        try {

            Map<String, String> reqMap = new HashMap<String, String>();

            for(int i=0; i<dateList.size(); i++) {
                reqMap = new HashMap<String, String>();
                reqMap.put("StoreCode", posCd);
                reqMap.put("StartDate", dateList.get(i));
                reqMap.put("EndDate", dateList.get(i));

                // URL 객체 생성
                URL obj = new URL(urlString);

                // HttpURLConnection 객체 생성
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty(accessKey, accessValue);
                connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정
                
                // 요청 JSON 데이터 생성
                String jsonInputString = CommUtil.mapToJson(reqMap);

                // 요청 데이터를 전송
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                
                Map<String, String> logMap = new HashMap<String, String>();
                logMap.put("logId", logId);
                logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.UNIONSOFT.name()); 
                logMap.put("reqType", "POST");
                logMap.put("url", urlString);
                logMap.put("successYn", "N");
                logMap.put("createUser", "BATCH");
                
                //전송로그 저장
                //insertApiTransferLog(logMap);
                
                // 응답 코드 확인
                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
                
                    logMap.put("logId", logId);
                    logMap.put("successYn", "N");
                    logMap.put("errorCd", Integer.toString(responseCode));
                    logMap.put("errorMsg", "접속오류");
                    logMap.put("updateUser", "BATCH");
                    //전송로그 저장
                    //insertApiTransferLog(logMap);
                    return;
                }
                // 응답 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                logMap.put("logId", logId);
                logMap.put("successYn", "Y");
                logMap.put("resParam", response.toString());
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                //insertApiTransferLog(logMap);
                
                // JSON 출력
                ObjectMapper objectMapper = new ObjectMapper();

                // JSON 데이터를 Map으로 변환
                Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
                
                // DATA 객체 추출
                Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

                // DayList 추출
                List<Map<String, Object>> DayList = (List<Map<String, Object>>) data.get("DayList");

                // StoreItemList 출력
                for (Map<String, Object> item : DayList) {
                    String StoreSalesId = ExdevCommonAPI.makeUniqueID(16);
                    
                    // EndTimeDate 값
                    String EndTimeDate = (String)item.get("EndTimeDate");
                    
                    // 문자열을 자르고 변환
                    String year = EndTimeDate.substring(0, 4);
                    String month = EndTimeDate.substring(4, 6);
                    String day = EndTimeDate.substring(6, 8);
                    
                    // 원하는 형식으로 결합
                    String yyyymmdd = year + "-" + month + "-" + day;
                    
                    // 숫자 데이터 변환 (null 체크 포함)
                    double CashReceiptAmt = getNumberValue(item.get("CashReceiptAmt"));
                    double CustNum = getNumberValue(item.get("CustNum"));
                    double VoidAmt = getNumberValue(item.get("VoidAmt"));
                    double SellAllCnt = getNumberValue(item.get("SellAllCnt"));
                    double CardAmt = getNumberValue(item.get("CardAmt"));
                    double CashbagAmt = getNumberValue(item.get("CashbagAmt"));
                    double DcAmt = getNumberValue(item.get("DcAmt"));
                    double EdenredAmt = getNumberValue(item.get("EdenredAmt"));
                    double ReceiveAmt = getNumberValue(item.get("ReceiveAmt"));
                    double CouponAmt = getNumberValue(item.get("CouponAmt"));
                    double KeepAmt = getNumberValue(item.get("KeepAmt"));
                    double SellCnt = getNumberValue(item.get("SellCnt"));
                    double TickAmt = getNumberValue(item.get("TickAmt"));
                    double CancelAmt = getNumberValue(item.get("CancelAmt"));
                    double WorkAmt = getNumberValue(item.get("WorkAmt"));
                    double CashAmt = getNumberValue(item.get("CashAmt"));
                    double PointAmt = getNumberValue(item.get("PointAmt"));
                    double EtcAmt = getNumberValue(item.get("EtcAmt"));
                    double SDCancelAmt = getNumberValue(item.get("SDCancelAmt"));
                    
                    item.put("StoreSalesId", StoreSalesId);
                    item.put("StoreId", StoreId);
                    item.put("yyyymmdd", yyyymmdd);
                    item.put("year", year);
                    item.put("month", month);
                    item.put("day", day);
                    item.put("STORE_SALES_ID", StoreSalesId);
                    item.put("STORE_ID", StoreId);
                    item.put("YYYYMMDD", yyyymmdd);
                    item.put("YEAR", year);
                    item.put("MONTH", month);
                    item.put("DAY", day);
                    item.put("RECEIVE_AMT", ReceiveAmt);
                    item.put("CUST_AMT", CustNum);
                    item.put("SELL_CNT", SellCnt);
                    item.put("CARD_AMT", CardAmt);
                    item.put("CASH_AMT", CashAmt);
                    item.put("DC_AMT", DcAmt);
                    
                    commonDao.insert("jobScheduler.updateStoreSalesMst", item);

                }
            }//for(int i=0; i<dateList.size(); i++) 
            
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            //insertApiTransferLog(logMap);
            e.printStackTrace();
        }
    }

    /** 
     * 내용        : 유니온소프트-일별 메뉴 매출 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposMenuSale( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("posUseYn", "Y");// 포스 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("posCdNull", "N");// 포스CD가 NOT NULL 
        searchMap.put("posCompCd", "N");// 포스 업체 코드가 NOT NULL
        searchMap.put("apiCd", (String)ExdevConstants.API_CD.STORE_MENU_SALES.name());//일별 메뉴 매출
        
        List<Map> listMap = commonDao.getList("jobScheduler.getbachTargetStore", searchMap);
                
        //포스 로우 데이타 데이타 삭제
        commonDao.delete("jobScheduler.delPosStoreMenuSalesMst", null);
        
        // 중복 제거를 위한 Set과 결과 List
        Set<String> dataSet = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        
        for(Map resultMap : listMap) {
            
            String brandId = (String) resultMap.get("BRAND_ID");
            if (!dataSet.contains(brandId)) {
                dataSet.add(brandId); // 브랜드 중복 확인을 위해 Set에 추가
                list.add(brandId); // 브랜드 중복되지 않은 항목을 결과 리스트에 추가
            }
            
            String posCompCd = (String)resultMap.get("POS_COMP_CD");
            
            if( ((String)ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd) ) {
                //POS API 호출
                unionposMenuSaleAPI(resultMap);
                
            }else {
            } 
        }

        for(int i=0;i<list.size();i++) {
            String brandId= list.get(i);
            Map<String, String> mergeMap = new HashMap<>();
            mergeMap.put("brandId", brandId);
            mergeMap.put("updateUser", "BATCH");
            mergeMap.put("createUser", "BATCH");
            commonDao.update("jobScheduler.mergeStoreMenuSalesMst", mergeMap);
        }
        
        
    }

    /** 
     * 내용        : 유니온소프트-일별매출조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 13 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposMenuSaleAPI( Map map ) throws Exception {
    
        
        String storeId = (String)map.get("STORE_ID");
        String brandId = (String)map.get("BRAND_ID");
        String posCompCd = (String)map.get("POS_COMP_CD");
        String posGroupId = (String)map.get("POS_GROUP_ID");
        String posStoreCode = (String)map.get("POS_CD");
        String yyyymmdd = (String)map.get("YYYYMMDD");
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        String logId = ExdevCommonAPI.makeUniqueID(16);
        
        
        String urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/item";
        /*
        if( posGroupId == null || "".equals(posGroupId)) {
            urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/item";
        }else {
            urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/group/sales/item";
        }
        */
        
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setConnectTimeout(10000); // 연결 시간 초과 설정
            connection.setReadTimeout(17000);   // 읽기 시간 초과 설정
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정

            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("GroupID", posGroupId);
            reqMap.put("StartDate", yyyymmdd);//yyyymmdd
            reqMap.put("EndDate", yyyymmdd);//yyyymmdd
            reqMap.put("TermSearch", "DAY");
            reqMap.put("StoreCode", posStoreCode);
           
            String reqParam = "StoreCode["+posStoreCode+"], StartDate["+yyyymmdd+"], EndDate["+yyyymmdd+"] ";
            
            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);

            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            String apiCd = (String)ExdevConstants.API_CD.STORE_MENU_SALES.name();
            String apiCompCd = (String)ExdevConstants.API_COMPANY.UNIONSOFT.name();
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", apiCompCd); 
            logMap.put("apiCd", apiCd);
            logMap.put("reqParam", reqParam);
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            
            Map<String, String> bachStateMap = new HashMap<String, String>();
            bachStateMap.put("brandId", brandId);
            bachStateMap.put("storeId", storeId);
            bachStateMap.put("apiCd",  apiCd);
            bachStateMap.put("apiCompCd",  apiCompCd);
            bachStateMap.put("yyyymmdd", yyyymmdd);
            ApiBachState(bachStateMap);
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);

            // JSON 출력
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 데이터를 Map으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
            
            // DATA 객체 추출
            Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

            // StoreItemList 추출
            List<Map<String, Object>> SalesItemList = (List<Map<String, Object>>) data.get("SalesItemList");

            if( SalesItemList != null && !SalesItemList.isEmpty() ) {
                
                for ( Map<String, Object> item : SalesItemList ) {
                    
                    String ReceiveAmt = CommUtil.ObjectToString(item.get("ReceiveAmt"));
                    String CardAmt = CommUtil.ObjectToString(item.get("CardAmt"));
                    String DcAmt = CommUtil.ObjectToString(item.get("DcAmt"));
                    String Qty = CommUtil.ObjectToString(item.get("Qty"));
                    String CashAmt = CommUtil.ObjectToString(item.get("CashAmt"));
                    String EtcAmt = CommUtil.ObjectToString(item.get("EtcAmt"));
                    String PriceAmt = CommUtil.ObjectToString(item.get("PriceAmt"));
                    
                    // 데이터를 Map에 저장
                    Map<String, String> saveMap = new HashMap<>();
                    saveMap.put("brandId", brandId);
                    saveMap.put("posCompCd", posCompCd);
                    saveMap.put("yyyymmdd",  yyyymmdd);
                    saveMap.put("column_01", storeId);
                    saveMap.put("column_02", (String)item.get("GrpCode"));
                    saveMap.put("column_03", (String)item.get("GrpCode"));
                    saveMap.put("column_04", (String)item.get("ItemCode"));
                    saveMap.put("column_05", ReceiveAmt); 
                    saveMap.put("column_06", Qty);
                    saveMap.put("column_07", CashAmt);
                    saveMap.put("column_08", CardAmt);
                    saveMap.put("column_09", EtcAmt);
                    saveMap.put("column_10", DcAmt);
                    saveMap.put("createUser", "BATCH");

                    commonDao.insert("jobScheduler.insertPosStoreMenuSalesMst", saveMap);//임시
                    
                }

                Map<String, String> bachStateMap1 = new HashMap<String, String>();
                bachStateMap1.put("brandId", brandId);
                bachStateMap1.put("storeId", storeId);
                bachStateMap1.put("apiCd",  apiCd);
                bachStateMap1.put("apiCompCd",  apiCompCd);
                bachStateMap1.put("yyyymmdd", yyyymmdd);
                bachStateMap1.put("successYn", "Y");
                bachStateMap1.put("updateUser", "BATCH");
                ApiBachState(bachStateMap1);
            }

        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            String errorMsg = CommUtil.getPrintStackTrace(e);
            String limitedStr = errorMsg.length() > 250 ? errorMsg.substring(0, 250) : errorMsg;
            
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", limitedStr);
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }

    /** 
     * 내용        : 유니온소프트-일별 메뉴 매출 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposMenuSaleManual( Map dateMap ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("posUseYn", "Y");// 포스 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("posCdNull", "N");// 포스CD가 NOT NULL 
        searchMap.put("posCompCd", "N");// 포스 업체 코드가 NOT NULL
        searchMap.put("apiCd", (String)ExdevConstants.API_CD.STORE_MENU_SALES.name());//일별 메뉴 매출
        //searchMap.put("storeId", "obong_a00214");// 행당점:obong_a00250,  성수점:obong_a00214 
        
        
        List<Map> listMap = commonDao.getList("jobScheduler.getbachTargetStoreManual", searchMap);
                
        //포스 로우 데이타 데이타 삭제
        commonDao.delete("jobScheduler.delPosStoreMenuSalesMst", null);
        
        // 중복 제거를 위한 Set과 결과 List
        Set<String> dataSet = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        
        for(Map resultMap : listMap) {
            
            String brandId = (String) resultMap.get("BRAND_ID");
            if (!dataSet.contains(brandId)) {
                dataSet.add(brandId); // 브랜드 중복 확인을 위해 Set에 추가
                list.add(brandId); // 브랜드 중복되지 않은 항목을 결과 리스트에 추가
            }
            
            String posCompCd = (String)resultMap.get("POS_COMP_CD");
            
            if( ((String)ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd) ) {
                //POS API 호출

                // 시작 날짜와 종료 날짜 설정
                LocalDate startDate = LocalDate.parse((String)dateMap.get("startDate"), DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate endDate   = LocalDate.parse((String)dateMap.get("endDate"), DateTimeFormatter.ofPattern("yyyyMMdd"));
                List<String> dateList = new ArrayList<>();

                // 날짜를 증가시키면서 리스트에 추가
                while (!startDate.isAfter(endDate)) {
                    dateList.add(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                    startDate = startDate.plusDays(1);  // 날짜 1일 증가
                }
                for(int i=0; i<dateList.size(); i++) {
                    resultMap.put("YYYYMMDD", dateList.get(i));
                    unionposMenuSaleAPIManual(resultMap);
                }
                
                
            }else {
            } 
        }
        /*  */
        for(int i=0;i<list.size();i++) {
            String brandId= list.get(i);
            Map<String, String> mergeMap = new HashMap<>();
            mergeMap.put("brandId", brandId);
            mergeMap.put("updateUser", "BATCH");
            mergeMap.put("createUser", "BATCH");

            commonDao.update("jobScheduler.mergeStoreMenuSalesMst", mergeMap);
        }
       
        
        
    }
    /** 
     * 내용        : 유니온소프트-일별매출조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 13 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposMenuSaleAPIManual( Map map ) throws Exception {
    
        
        String storeId = (String)map.get("STORE_ID");
        String brandId = (String)map.get("BRAND_ID");
        String posCompCd = (String)map.get("POS_COMP_CD");
        String posGroupId = (String)map.get("POS_GROUP_ID");
        String posStoreCode = (String)map.get("POS_CD");
        String yyyymmdd = (String)map.get("YYYYMMDD");
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        String logId = ExdevCommonAPI.makeUniqueID(16);
        
        String urlString = "";

        if( true ) {
            urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/item";
        }
        
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setConnectTimeout(10000); // 연결 시간 초과 설정
            connection.setReadTimeout(17000);   // 읽기 시간 초과 설정
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정

            Map<String, String> reqMap = new HashMap<String, String>();
            /*
            if( posGroupId == null || "".equals(posGroupId)) {
                urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/sales/item";
            }else {
                urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/group/sales/item";
            }
            */
            
            if( true ) {
                reqMap.put("StartDate", yyyymmdd);//yyyymmdd
                reqMap.put("EndDate",   yyyymmdd);//yyyymmdd
                reqMap.put("StoreCode", posStoreCode);
            }
            
            String reqParam = "StoreCode["+posStoreCode+"], StartDate["+yyyymmdd+"], EndDate["+yyyymmdd+"] ";
            
            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);

            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            String apiCd = (String)ExdevConstants.API_CD.STORE_MENU_SALES.name();
            String apiCompCd = (String)ExdevConstants.API_COMPANY.UNIONSOFT.name();
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", apiCompCd); 
            logMap.put("apiCd", apiCd);
            logMap.put("reqParam", reqParam);
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            
            Map<String, String> bachStateMap = new HashMap<String, String>();
            bachStateMap.put("brandId", brandId);
            bachStateMap.put("storeId", storeId);
            bachStateMap.put("apiCd",  apiCd);
            bachStateMap.put("apiCompCd",  apiCompCd);
            bachStateMap.put("yyyymmdd", yyyymmdd);
            //ApiBachState(bachStateMap);
            
            //전송로그 저장
            //insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            //insertApiTransferLog(logMap);

            // JSON 출력
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 데이터를 Map으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
            
            // DATA 객체 추출
            Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

            // StoreItemList 추출
            List<Map<String, Object>> SalesItemList = (List<Map<String, Object>>) data.get("SalesItemList");

            if( SalesItemList != null && !SalesItemList.isEmpty() ) {
                
                for ( Map<String, Object> item : SalesItemList ) {
                    
                    String ReceiveAmt = CommUtil.ObjectToString(item.get("ReceiveAmt"));
                    String CardAmt = CommUtil.ObjectToString(item.get("CardAmt"));
                    String DcAmt = CommUtil.ObjectToString(item.get("DcAmt"));
                    String Qty = CommUtil.ObjectToString(item.get("Qty"));
                    String CashAmt = CommUtil.ObjectToString(item.get("CashAmt"));
                    String EtcAmt = CommUtil.ObjectToString(item.get("EtcAmt"));
                    String PriceAmt = CommUtil.ObjectToString(item.get("PriceAmt"));
                    
                    // 데이터를 Map에 저장
                    Map<String, String> saveMap = new HashMap<>();
                    saveMap.put("brandId", brandId);
                    saveMap.put("posCompCd", posCompCd);
                    saveMap.put("yyyymmdd",  yyyymmdd);
                    saveMap.put("column_01", storeId);
                    saveMap.put("column_02", (String)item.get("GrpCode"));
                    saveMap.put("column_03", (String)item.get("GrpCode"));
                    saveMap.put("column_04", (String)item.get("ItemCode"));
                    saveMap.put("column_05", ReceiveAmt); 
                    saveMap.put("column_06", Qty);
                    saveMap.put("column_07", CashAmt);
                    saveMap.put("column_08", CardAmt);
                    saveMap.put("column_09", EtcAmt);
                    saveMap.put("column_10", DcAmt);
                    saveMap.put("createUser", "BATCH");

                    commonDao.insert("jobScheduler.insertPosStoreMenuSalesMst", saveMap);//임시
                    
                }

                Map<String, String> bachStateMap1 = new HashMap<String, String>();
                bachStateMap1.put("brandId", brandId);
                bachStateMap1.put("storeId", storeId);
                bachStateMap1.put("apiCd",  apiCd);
                bachStateMap1.put("apiCompCd",  apiCompCd);
                bachStateMap1.put("yyyymmdd", yyyymmdd);
                bachStateMap1.put("successYn", "Y");
                bachStateMap1.put("updateUser", "BATCH");
                ApiBachState(bachStateMap1);
            }

        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }

    // 숫자 값 변환 함수 (null 체크 및 형 변환 처리)
    private double getNumberValue(Object value) {
        if (value == null) return 0.0;  // NULL 값이면 0 반환
        if (value instanceof Number) {
            return ((Number) value).doubleValue();  // 숫자형이면 double 변환
        }
        try {
            return Double.parseDouble(value.toString().replace(",", "")); // 쉼표 제거 후 변환
        } catch (NumberFormatException e) {
            return 0.0; // 변환 실패 시 0 반환
        }
    }
    /** 
     * 내용        : 유니온소프트 - 메뉴정보조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unionposGroupMenu( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("posUseYn", String.valueOf("Y"));// 포스 사용
        searchMap.put("businessRegNumNull", String.valueOf("N"));// 사업자 번호가 있는것
        searchMap.put("posCdNull", String.valueOf("N"));// 포스CD가 NOT NULL
        
        List<Map> listMap = commonDao.getList("jobScheduler.getStoreMst", searchMap);
        
        // 중복 제거를 위한 Set과 결과 List
        Set<String> dataSet = new HashSet<>();
        
        ArrayList<String> list = new ArrayList<>();
        
        // 데이터를 Map에 저장
        Map<String, String> delMap = new HashMap<>();
        //포스 로우 데이타 데이타 삭제
        commonDao.delete("jobScheduler.delPosMenuLowDataTemp", delMap);
        
        
        for(Map resultMap : listMap) {

            String brandId = (String) resultMap.get("BRAND_ID");
            if (!dataSet.contains(brandId)) {
                dataSet.add(brandId); // 브랜드 중복 확인을 위해 Set에 추가
                list.add(brandId); // 브랜드 중복되지 않은 항목을 결과 리스트에 추가
            }
            
            String posCompCd = (String) resultMap.get("POS_COMP_CD");
            if (((String) ExdevConstants.API_COMPANY.UNIONSOFT.name()).equals(posCompCd)) {
                unionposGroupMenuAPI(resultMap);
            }
        }
        
        for(int i=0;i<list.size();i++) {
            String brandId= list.get(i);
            storeMenuPriceMigration( brandId );//지점메뉴 마스터 데이타를 지점 메뉴 가격에 입력 
        }
        
        for(int i=0;i<list.size();i++) {
            String brandId= list.get(i);
            insertStoreMenuMst( brandId );//포스에서 받은 임시데이타를 지점 메뉴 마스터에 입력
        }
        
    }
    
    /** 
     * 내용        : 포스에서 받은 임시데이타를 지점 메뉴 마스터에 입력
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 18 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    public void insertStoreMenuMst( String brandId ) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("brandId", brandId);
        searchMap.put("updateUser", "BATCH");
        searchMap.put("createUser", "BATCH");
        
        commonDao.update("jobScheduler.insertStoreMenuMst", searchMap);
        
    }
    /** 
     * 내용        : 지점메뉴 마스터 => 지점 메뉴 가격
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 16 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    public void storeMenuPriceMigration( String brandId ) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        LocalDate currentDate = LocalDate.now();
        LocalDate oneMonthAgo = currentDate.minusMonths(1);// 한달전

        int year = oneMonthAgo.getYear();
        int month = oneMonthAgo.getMonthValue();

        String monthStr = String.format("%02d", month);
        
        searchMap.put("brandId", brandId);
        searchMap.put("year", Integer.toString(year));
        searchMap.put("month", monthStr);
        searchMap.put("updateUser", "BATCH");
        searchMap.put("createUser", "BATCH");
        
        commonDao.update("jobScheduler.updateStoreMenuPrice", searchMap);
        
    }

    /** 
     * 내용        : 유니온소프트 - 메뉴정보조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자  
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })         
    public void unionposGroupMenuAPI( Map map ) throws Exception {
        
        String brandId = (String)map.get("BRAND_ID");
        String posCompCd = (String)map.get("POS_COMP_CD");
        String posGroupId = (String)map.get("POS_GROUP_ID");
        String posStoreCode = (String)map.get("POS_CD");
        String accessKey = ExdevConstants.UNIONSOFT_ACCESS_KEY ;
        String accessValue = ExdevConstants.UNIONSOFT_ACCESS_VALUE ;
        
        String urlString = "";
        String ItemList = "";
        
        if( posGroupId == null || "".equals(posGroupId)) {
            ItemList = "ItemList";
            urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/store/item/list";
        }else {
            ItemList = "StoreItemList";
            urlString = ExdevConstants.UNIONSOFT_BASE_URL + "/group/item/list";
        }
        
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);
        
        try {
        
            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setConnectTimeout(5000); // 연결 시간 초과 설정
            connection.setReadTimeout(7000);   // 읽기 시간 초과 설정
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정

            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("StoreCode", posStoreCode);
            reqMap.put("GroupID", posGroupId);
            
            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);

            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.UNIONSOFT.name()); 
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // JSON 출력
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 데이터를 Map으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
            
            // DATA 객체 추출
            Map<String, Object> data = (Map<String, Object>) responseMap.get("DATA");

            // StoreItemList 추출
            List<Map<String, Object>> storeItemList = (List<Map<String, Object>>) data.get(ItemList);

            String yyyymmdd   = DateUtil.getDateYMD(0).replace("-", "");
            

            // StoreItemList 출력
            for (Map<String, Object> item : storeItemList) {
                
                // 데이터를 Map에 저장
                Map<String, String> saveMap = new HashMap<>();
                saveMap.put("brandId", brandId);
                saveMap.put("posCompCd", posCompCd);
                saveMap.put("yyyymmdd", yyyymmdd);
                saveMap.put("column01", posStoreCode);
                saveMap.put("column02", (String)item.get("ItemCode") );
                saveMap.put("column03", (String)item.get("ItemName") );
                saveMap.put("column04", (String)item.get("GrpCode") );
                saveMap.put("column05", (String)item.get("GrpName") );
                saveMap.put("column06", String.valueOf(item.get("Price")) );
                saveMap.put("column07", (String)item.get("DelFlag") );
                saveMap.put("column08", String.valueOf(item.get("OrignalAmt")) );
                saveMap.put("createUser", "BATCH");
                
                commonDao.insert("jobScheduler.insertPosMenuLowDataTemp", saveMap);
                
            }
            
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }
    
    /* 유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트    유니온소프트          */
    /*==================================================================================================================*/
    
    
    /*==================================================================================================================*/
    /* 세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구 */
    
    /** 
     * 내용        : 세친구-일별 매출 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 12. 30 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void taxpalStoreSales( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("taxpalUseYn", "Y");// 세친구 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("closingDateNull", "Y");// 폐점이 아닌것
        
        /* 1. 세친구 사용여부가 Y 인 지점찾기
         *   - 사이트 코드, ID,PW 조회
        */
        List<Map> listMap = commonDao.getList("jobScheduler.getStoreMst", searchMap);
        
        //fromDate
        //toDate
        String fromDate = DateUtil.getDateYMD(-3).replace("-", "");
        String toDate   = DateUtil.getDateYMD(-1).replace("-", "");
        
        for(Map resultMap : listMap) {
            resultMap.put("fromDate", fromDate);
            resultMap.put("toDate", toDate);
            resultMap.put("storeId", (String)resultMap.get("STORE_ID"));
            
            taxpalSalesAPI(resultMap);// 매출자료 조회
        }
    }

    /** 
     * 내용        : 세친구-일별매출조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void taxpalSalesAPI( Map map ) throws Exception {

        String storeId = (String)map.get("storeId");
        
        String urlString = ExdevConstants.TAXPAL_BASE_URL + "/v1/stores/" + (String)map.get("BUSINESS_REGNUM")
                          +"/sales?fromDate="+(String)map.get("fromDate")+"&toDate="+(String)map.get("toDate");
        
        String accessKey = ExdevConstants.TAXPAL_ACCESS_KEY ;
        String accessValue = ExdevConstants.TAXPAL_ACCESS_VALUE ;
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // GET 요청 설정
            connection.setRequestMethod("GET");

            // doOutput을 명시적으로 false로 설정 (GET 요청에서는 필요 없음)
            connection.setDoOutput(false);

            // 헤더 설정 (x-access-key 추가)
            connection.setRequestProperty(accessKey, accessValue);
            connection.setRequestProperty("Accept", "application/json");
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.TAXPAL.name()); 
            logMap.put("reqType", "GET");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // JSON 출력
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");
            if (success) {
                JSONArray jsonList = jsonResponse.getJSONArray("list");
               
                for (int i = 0; i < jsonList.length(); i++) {
                    
                    JSONObject item = jsonList.getJSONObject(i);
                    String date = item.getString("date");
                    // 연, 월, 일을 각각 추출
                    String year = date.substring(0, 4);  // 첫 4자리: 연도
                    String month = date.substring(4, 6); // 5번째~6번째: 월
                    String day = date.substring(6, 8);   // 7번째~8번째: 일

                    int deliveryAmount = item.getInt("deliveryAmount");
                    Map<String, Object> saveMap = new HashMap<>();
                    saveMap.put("storeId", storeId);
                    saveMap.put("year", year); 
                    saveMap.put("month", month); 
                    saveMap.put("day", day);                      
                    saveMap.put("deliveryAmount", deliveryAmount);// 세친구에서는 배달 매출만 입력,  
                    saveMap.put("deliveryCount", item.getInt("deliveryCount"));// 세친구에서는 배달 매출만 입력,  
                    saveMap.put("createUser", "BATCH");
                    saveMap.put("updateUser", "BATCH");
                    
                    Map insertInfo = (Map)commonDao.getObject("dashboardCorPerformance.getStoreSalesMst", saveMap);
                    
                    int result = 0;
                    if( insertInfo == null) {
                        result = commonDao.insert("jobScheduler.insertStoreSalesMst", saveMap);
                    }else {
                        
                        int grossSales = 0;
                        if ( insertInfo.get("GROSS_SALES") != null) {
                            grossSales = ((Number) insertInfo.get("GROSS_SALES")).intValue()  + deliveryAmount;
                        }else {
                            grossSales = deliveryAmount; // 안전하게 숫자로 변환
                        }
                        saveMap.put("grossSales", grossSales);
                        saveMap.put("storeSalesId", (String)insertInfo.get("STORE_SALES_ID"   ));
                        result = commonDao.update("jobScheduler.updateStoreSalesMst1", saveMap);
                    }
                    
                   
                } 
            }
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }

    /** 
     * 내용        : 세친구-일별 매입 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void taxpalStorePurchase( ) throws Exception {
        
        Map<String, String> searchMap = new HashMap<String, String>();
        
        searchMap.put("brandId", "obong");
        searchMap.put("taxpalUseYn", "Y");// 세친구 사용
        searchMap.put("businessRegNumNull", "N");// 사업자 번호가 있는것
        searchMap.put("closingDateNull", "Y");// 폐점이 아닌것
        
        /* 1. 세친구 사용여부가 Y 인 지점찾기
         *   - 사이트 코드, ID,PW 조회
        */
        List<Map> listMap = commonDao.getList("jobScheduler.getStoreMst", searchMap);
        
        //fromDate
        //toDate
        String fromDate = "20241201";//DateUtil.getDateYMD(-3);
        String toDate   = "20241202";//DateUtil.getDateYMD(-1);
        
        for(Map resultMap : listMap) {
            resultMap.put("fromDate", fromDate);
            resultMap.put("toDate", toDate);
            resultMap.put("storeId", (String)resultMap.get("STORE_ID"));
            resultMap.put("storeCd", (String)resultMap.get("STORE_CD"));
            
            taxpalPurchaseAPI(resultMap);// 매출자료 조회
        }
    }

    /** 
     * 내용        : 세친구-일별매입조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void taxpalPurchaseAPI( Map map ) throws Exception {

        
        //String urlString = "https://dev-api.taxpal.co.kr/v1/stores/4364400557/sales?fromDate=20240702&toDate=20240705";
        
        String urlString = ExdevConstants.TAXPAL_BASE_URL + "/v1/stores/" + (String)map.get("BUSINESS_REGNUM")
                          +"/sales?fromDate="+(String)map.get("fromDate")+"&toDate="+(String)map.get("toDate");
        
        String accessKey = ExdevConstants.TAXPAL_ACCESS_KEY ;
        String accessValue = ExdevConstants.TAXPAL_ACCESS_VALUE ;
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // GET 요청 설정
            connection.setRequestMethod("GET");

            // doOutput을 명시적으로 false로 설정 (GET 요청에서는 필요 없음)
            connection.setDoOutput(false);

            // 헤더 설정 (x-access-key 추가)
            connection.setRequestProperty(accessKey, accessValue);
            connection.setRequestProperty("Accept", "application/json");
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.TAXPAL.name()); 
            logMap.put("reqType", "GET");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // JSON 출력
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");
            if (success) {
                JSONArray jsonList = jsonResponse.getJSONArray("list");

                String storeCd = (String)map.get("storeCd");
                
                for (int i = 0; i < jsonList.length(); i++) {
                    
                    JSONObject item = jsonList.getJSONObject(i);
                    String date = item.getString("date");

                    Map<String, Object> saveMap = new HashMap<>();
                    saveMap.put("storeCd", storeCd);
                    saveMap.put("yyyymmdd", date); 
                    saveMap.put("cardAmount", item.getInt("cardAmount")); 
                    saveMap.put("cardCount", item.getInt("cardCount"));  
                    saveMap.put("cashAmount", item.getInt("cashAmount")); 
                    saveMap.put("cashCount", item.getInt("cashCount")); 
                    saveMap.put("totalAmount", item.getInt("totalAmount"));
                    saveMap.put("totalCount", item.getInt("totalCount")); 
                    saveMap.put("createUser", "BATCH");
                    saveMap.put("updateUser", "BATCH");
                    
                    Map<String, Object> saveCheck = new HashMap<>();
                    saveCheck.put("storeCd", storeCd);
                    saveCheck.put("yyyymmdd", date);
                    
                    Map insertInfo = (Map)commonDao.getObject("dashboardCorPerformance.getStorePurchaseMst", saveCheck);
                    
                    int result = 0;
                    if( insertInfo == null) {
                        saveMap.put("purchaseId", ExdevCommonAPI.makeUniqueID(16));
                        result = commonDao.insert("jobScheduler.insertStorePurchaseMst", saveMap);
                    }else {
                        saveMap.put("purchaseId", (String)insertInfo.get("PURCHASE_ID"));
                        result = commonDao.update("jobScheduler.updateStorePurchaseMst", saveMap);
                    }
                   
                } 
            }
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        
    }
    
    /** 
     * 내용        : 세친구-사업장 정보 등록/수정 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean taxpalStoresRegist( Map map ) throws Exception {
        boolean returnValue = false;

        String storeId      = (String)map.get("storeId");
        String brn          = (String)map.get("brn");
        String name         = (String)map.get("name");
        String bossName     = (String)map.get("bossName");
        String phoneNumber  = (String)map.get("phoneNumber");
        
        List<Map> listMap= getStoreMst(map);
        
        //returnValue = taxpalStoresRegistAPI(map);
        for ( Map<String, Object> storeMap : listMap) {
            returnValue = taxpalStoresRegistAPI(storeMap);
        }

        return returnValue;
    }

    /** 
     * 내용        : 세친구-일별매입조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean taxpalStoresRegistAPI( Map map ) throws Exception {
         boolean returnValue = false;
        
        String storeId      = (String)map.get("STORE_ID");
        String brn          = (String)map.get("BUSINESS_REGNUM");
        String name         = (String)map.get("STORE_NM");
        String bossName     = (String)map.get("STORE_MANAGER_NM");
        String phoneNumber  = (String)map.get("PHONE_MANAGER");
        
        if( phoneNumber != null) {
            phoneNumber = phoneNumber.replaceAll("-", "");
        }
        
        String urlString = ExdevConstants.TAXPAL_BASE_URL + "/v1/stores";
        String accessKey = ExdevConstants.TAXPAL_ACCESS_KEY ;
        String accessValue = ExdevConstants.TAXPAL_ACCESS_VALUE ;
        
        List<Map<String, Object>> list = new ArrayList<>();
        String logId = ExdevCommonAPI.makeUniqueID(16);
        try {

            // URL 객체 생성
            URL obj = new URL(urlString);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(accessKey, accessValue);
            connection.setConnectTimeout(10000); // 연결 시간 초과 설정
            connection.setReadTimeout(17000);   // 읽기 시간 초과 설정
            connection.setDoOutput(true); // POST 요청에 Body 데이터를 보내기 위해 설정

            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("brn", brn);
            reqMap.put("name", name);
            reqMap.put("bossName", bossName);
            reqMap.put("phoneNumber", phoneNumber);

            // 요청 JSON 데이터 생성
            String jsonInputString = CommUtil.mapToJson(reqMap);
            
            // 요청 데이터를 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("companyCd", (String)ExdevConstants.API_COMPANY.TAXPAL.name());
            logMap.put("apiCd", "STORES_REGIST");
            logMap.put("reqType", "POST");
            logMap.put("url", urlString);
            logMap.put("successYn", "N");
            logMap.put("createUser", "BATCH");
            
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) { // 실패
            
                logMap.put("logId", logId);
                logMap.put("successYn", "N");
                logMap.put("errorCd", Integer.toString(responseCode));
                logMap.put("errorMsg", "접속오류");
                logMap.put("updateUser", "BATCH");
                //전송로그 저장
                insertApiTransferLog(logMap);
                return returnValue;
            }
            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            logMap.put("logId", logId);
            logMap.put("successYn", "Y");
            logMap.put("resParam", response.toString());
            logMap.put("updateUser", "BATCH");
            //전송로그 저장
            insertApiTransferLog(logMap);
            
            // JSON 출력
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");
            
            
            Map<String, String> saveMap = new HashMap<>();
            saveMap.put("updateUser", "BATCH");
            saveMap.put("storeId", storeId);
            
            if (success) {
                saveMap.put("taxpalRegistYn", "Y");
            }else {
                saveMap.put("taxpalRegistYn", "Y");
            }

            commonDao.insert("jobScheduler.updateStoreMst", saveMap);//임시
            returnValue = true;
            
        } catch (Exception e) {
            //전송로그 저장
            Map<String, String> logMap = new HashMap<String, String>();
            logMap.put("logId", logId);
            logMap.put("successYn", "N");
            logMap.put("errorCd", "999");
            logMap.put("errorMsg", CommUtil.getPrintStackTrace(e));
            logMap.put("updateUser", "BATCH");
            insertApiTransferLog(logMap);
            e.printStackTrace();
        }
        return returnValue;
        
    }
    /* 세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구    세친구 */
    /*==================================================================================================================*/
    

    /** 
     * 내용        : 가맹점 리스트및 현황
     *               compPerformanceTab4.html
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String getOpenCloseCnt() throws Exception {
        
        String urlString = "https://jsonplaceholder.typicode.com/comments";
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            // 1. URL 연결 설정
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // 2. 응답 코드 확인
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            // 3. 응답 데이터 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // 4. JSON 데이터를 List<Map<String, Object>>로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            list = objectMapper.readValue(response.toString(), new TypeReference<List<Map<String, Object>>>() {});

            // 5. 출력 (테스트 용도)
            for (Map<String, Object> map : list) {
                Map<String, String> saveMap = new HashMap<String, String>();
                
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key   = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    
                    if( "postId".equals(key) ) { saveMap.put("TEST_01",  value ); }
                    else if( "id".equals(key) ) { saveMap.put("TEST_02",  value ); }
                    else if( "name".equals(key) ) { saveMap.put("TEST_03",  value ); }
                    else if( "email".equals(key) ) { saveMap.put("TEST_04",  value ); }
                    
                    
                    int result = commonDao.insert("dashboardCorPerformance.testExpJobScheduler", saveMap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "";
    }


    /** 
     * 내용        : API 로그 삭제
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public boolean delLog() throws Exception {

        String dateYMDPre = DateUtil.getDateYMD(-60);// 90일 전

        
        Map<String, String> map = new HashMap<>();
        map.put("delDate", dateYMDPre);
        
        commonDao.delete("jobScheduler.delApiTransferLog", map);
        commonDao.delete("jobScheduler.delPosMenuLowDataTemp", map);

        commonDao.delete("jobScheduler.deleteBonfoodSales", map);
        commonDao.delete("jobScheduler.deleteJanggaSales", map);
        commonDao.delete("jobScheduler.deleteHaramSales", map);
        commonDao.delete("jobScheduler.deleteSpcSales", map);
        
        return true;
        
    }

    /** 
     * 내용        : 매장별 목표 매출 생성
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 21: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean storeSalesGoal( String brandId, String year ) throws Exception {

        String  previousYear = String.valueOf(Integer.parseInt(year) - 1);
        
        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("year", year);
        searchMap.put("preYear", previousYear);// 직전년
        searchMap.put("brandId", brandId);
        
        
        //대상 지점 조회
        List<Map> storeListMap = commonDao.getList("jobScheduler.getTargetStoreSalesGoal", searchMap);

        // 브랜드의 전년월 평균 매출 조회
        Map<String, Object> brandAvgMap = (Map<String, Object>) commonDao.getObject("jobScheduler.getBrandSalesMonthAvg", searchMap);
        
        for(Map storeMap : storeListMap) {
            
            String storeId = (String)storeMap.get("STORE_ID");
            String storeCd = (String)storeMap.get("STORE_CD");
            searchMap.put("storeId", storeId);
            searchMap.put("storeCd", storeCd);
            
            // 직전년도 월 평균값을 구한다.
            Map avgMap = (Map)commonDao.getObject("jobScheduler.getStoreSalesMonthAvg", searchMap);

            BigDecimal salesGoal = BigDecimal.ZERO;
            if( avgMap == null) {
                // 직전년도의 월 평균값이 없는경우 전체 지점의 월평균을 입력한다.
                if (brandAvgMap != null && brandAvgMap.get("MONTH_AVG_SALES") != null) {
                    Object value = brandAvgMap.get("MONTH_AVG_SALES");
                
                    if (value instanceof BigDecimal) {
                        salesGoal = (BigDecimal) value;
                    } else if (value instanceof String) {
                        salesGoal = new BigDecimal((String) value);
                    }
                }
            }else {
                salesGoal = avgMap.get("AVG_GROSS_SALES") instanceof BigDecimal ? (BigDecimal) avgMap.get("AVG_GROSS_SALES") : BigDecimal.ZERO;
            }
            
        
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("brandId", brandId);
            saveMap.put("storeId", storeId);
            saveMap.put("year", year);
            saveMap.put("createUser", "BATCH");
            
            BigDecimal month1  = (BigDecimal)storeMap.get("MONTH1");
            BigDecimal month2  = (BigDecimal)storeMap.get("MONTH2");
            BigDecimal month3  = (BigDecimal)storeMap.get("MONTH3");
            BigDecimal month4  = (BigDecimal)storeMap.get("MONTH4");
            BigDecimal month5  = (BigDecimal)storeMap.get("MONTH5");
            BigDecimal month6  = (BigDecimal)storeMap.get("MONTH6");
            BigDecimal month7  = (BigDecimal)storeMap.get("MONTH7");
            BigDecimal month8  = (BigDecimal)storeMap.get("MONTH8");
            BigDecimal month9  = (BigDecimal)storeMap.get("MONTH9");
            BigDecimal month10 = (BigDecimal)storeMap.get("MONTH10");
            BigDecimal month11 = (BigDecimal)storeMap.get("MONTH11");
            BigDecimal month12 = (BigDecimal)storeMap.get("MONTH12");
            
            if(month1  == BigDecimal.ZERO) {saveMap.put("month1",   salesGoal);}else {saveMap.put("month1",   month1);}
            if(month2  == BigDecimal.ZERO) {saveMap.put("month2",   salesGoal);}else {saveMap.put("month2",   month2 );}
            if(month3  == BigDecimal.ZERO) {saveMap.put("month3",   salesGoal);}else {saveMap.put("month3",   month3 );}
            if(month4  == BigDecimal.ZERO) {saveMap.put("month4",   salesGoal);}else {saveMap.put("month4",   month4 );}
            if(month5  == BigDecimal.ZERO) {saveMap.put("month5",   salesGoal);}else {saveMap.put("month5",   month5 );}
            if(month6  == BigDecimal.ZERO) {saveMap.put("month6",   salesGoal);}else {saveMap.put("month6",   month6 );}
            if(month7  == BigDecimal.ZERO) {saveMap.put("month7",   salesGoal);}else {saveMap.put("month7",   month7 );}
            if(month8  == BigDecimal.ZERO) {saveMap.put("month8",   salesGoal);}else {saveMap.put("month8",   month8 );}
            if(month9  == BigDecimal.ZERO) {saveMap.put("month9",   salesGoal);}else {saveMap.put("month9",   month9 );}
            if(month10 == BigDecimal.ZERO) {saveMap.put("month10",  salesGoal);}else {saveMap.put("month10",  month10);}
            if(month11 == BigDecimal.ZERO) {saveMap.put("month11",  salesGoal);}else {saveMap.put("month11",  month11);}
            if(month12 == BigDecimal.ZERO) {saveMap.put("month12",  salesGoal);}else {saveMap.put("month12",  month12);}
            
            commonDao.insert("jobScheduler.insertStoreSalesMonthAvg", saveMap);
                
        }

        
        return true;
        /*        
                 return true;
        */
        
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public BigDecimal getBrandSalesGoalAvg(List<Map> brandAvgMapList, String brandId, String year, String month) {
        
        for (Map<String, Object> brandAvgMap : brandAvgMapList) {
            String currentBrandId = (String) brandAvgMap.get("BRAND_ID");
            String currentYear = (String) brandAvgMap.get("YEAR");
            String currentMonth = (String) brandAvgMap.get("MONTH");
            
            if (currentBrandId.equals(brandId) && currentYear.equals(year) && currentMonth.equals(month)) {
                Object salesGoalObj = brandAvgMap.get("AVG_GROSS_SALES");
                if (salesGoalObj != null) {
                    if (salesGoalObj instanceof BigDecimal) {
                        return (BigDecimal) salesGoalObj;
                    } else if (salesGoalObj instanceof Number) {
                        return BigDecimal.valueOf(((Number) salesGoalObj).doubleValue());
                    } else {
                        try {
                            return new BigDecimal(salesGoalObj.toString());
                        } catch (NumberFormatException e) {
                            return BigDecimal.ZERO; // 변환 실패 시 null 반환
                        }
                    }
                }
            }
        }
        return BigDecimal.ZERO; // 조건에 맞는 데이터가 없을 경우 null 반환
    }
    
     // 데이터를 저장하는 메서드
     public void saveData(Map<String, String> data) {
         // 실제 저장 로직을 구현 (예: 데이터베이스에 저장)
         System.out.println("Saving data:");
         System.out.println("BRN: " + data.get("brn"));
         System.out.println("Name: " + data.get("name"));
         System.out.println("Boss Name: " + data.get("bossName"));
         System.out.println("Phone Number: " + data.get("phoneNumber"));
     }

     /*=========================================================================================================*/

     /** 
      * 내용        : 임시 - 지점메뉴 매출 데이타 만들기
      * @생 성 자   : 이응규
      * @생 성 일자 : 2025. 02. 17: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public boolean createStoreMenuSalesMst() throws Exception {
         

         Map<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("BRAND_ID", "gopbuni");
         
         List<Map> listMap = commonDao.getList("jobScheduler.getTestTargetStore", searchMap);
         
         for(Map storeMap : listMap) {

             String storeId = (String)storeMap.get("STORE_ID");
             
             List<Map> saleslistMap = commonDao.getList("jobScheduler.getTestMenuSales", searchMap);

             for(Map salesMap : saleslistMap) {

                 String STORE_ID       = (String)salesMap.get("STORE_ID");
                 String GROUP_1        = (String)salesMap.get("GROUP_1");
                 String GROUP_2        = (String)salesMap.get("GROUP_2");
                 String MENU_ID        = (String)salesMap.get("MENU_ID");
                 String DAY            = (String)salesMap.get("DAY");
                 String CREATE_USER    = (String)salesMap.get("CREATE_USER");
                 
                 BigDecimal SALES = salesMap.get("SALES") instanceof BigDecimal ? (BigDecimal) salesMap.get("SALES") : BigDecimal.ZERO;
                 BigDecimal DAY_AVERAGE = salesMap.get("DAY_AVERAGE") instanceof BigDecimal ? (BigDecimal) salesMap.get("DAY_AVERAGE") : BigDecimal.ZERO;
                 BigDecimal SALES_QUANTITY = salesMap.get("SALES_QUANTITY") instanceof BigDecimal ? (BigDecimal) salesMap.get("SALES_QUANTITY") : BigDecimal.ZERO;
                 BigDecimal CASH = salesMap.get("CASH") instanceof BigDecimal ? (BigDecimal) salesMap.get("CASH") : BigDecimal.ZERO;
                 BigDecimal CARD = salesMap.get("CARD") instanceof BigDecimal ? (BigDecimal) salesMap.get("CARD") : BigDecimal.ZERO;
                 BigDecimal OTHER = salesMap.get("OTHER") instanceof BigDecimal ? (BigDecimal) salesMap.get("OTHER") : BigDecimal.ZERO;
                 BigDecimal DISCOUNT = salesMap.get("DISCOUNT") instanceof BigDecimal ? (BigDecimal) salesMap.get("DISCOUNT") : BigDecimal.ZERO;
                 /*
                 String DAY_AVERAGE    = String.valueOf(salesMap.get("DAY_AVERAGE"));
                 String SALES_QUANTITY = String.valueOf(salesMap.get("SALES_QUANTITY"));
                 String CASH           = String.valueOf(salesMap.get("CASH"));
                 String CARD           = String.valueOf(salesMap.get("CARD"));
                 String OTHER          = String.valueOf(salesMap.get("OTHER"));
                 String DISCOUNT       = String.valueOf(salesMap.get("DISCOUNT"));
                 String CREATE_USER    = (String)salesMap.get("CREATE_USER");
                 */

                 
                 String YEAR1 = "2025"; 
                 String MONTH1 = "02"; 
                 String YYYYMMDD = YEAR1+"-"+MONTH1+"-"+DAY; 
                 /*
                 System.out.println("STORE_ID         ==>"+storeId       );
                 System.out.println("GROUP_1          ==>"+GROUP_1        );
                 System.out.println("GROUP_2          ==>"+GROUP_2        );
                 System.out.println("MENU_ID          ==>"+MENU_ID        );
                 System.out.println("YEAR             ==>"+YEAR1           );
                 System.out.println("MONTH            ==>"+MONTH1          );
                 System.out.println("DAY              ==>"+DAY            );
                 System.out.println("SALES            ==>"+SALES          );
                 System.out.println("DAY_AVERAGE      ==>"+DAY_AVERAGE    );
                 System.out.println("SALES_QUANTITY   ==>"+SALES_QUANTITY );
                 System.out.println("CASH             ==>"+CASH           );
                 System.out.println("CARD             ==>"+CARD           );
                 System.out.println("OTHER            ==>"+OTHER          );
                 System.out.println("DISCOUNT         ==>"+DISCOUNT       );
                 System.out.println("CREATE_USER      ==>"+CREATE_USER    );
                 */
                 
                 Map<String, Object> saveMap = new HashMap<>();
                 saveMap.put("MENU_SALES_ID",   ExdevCommonAPI.makeUniqueID(16)  );
                 saveMap.put("STORE_ID",   storeId       );
                 saveMap.put("GROUP_1",   GROUP_1        );
                 saveMap.put("GROUP_2",   GROUP_2        );
                 saveMap.put("MENU_ID",   MENU_ID        );
                 saveMap.put("YEAR",   YEAR1           );
                 saveMap.put("MONTH",   MONTH1           );
                 saveMap.put("DAY",   DAY            );
                 saveMap.put("SALES",   SALES          );
                 saveMap.put("DAY_AVERAGE",   DAY_AVERAGE    );
                 saveMap.put("SALES_QUANTITY",   SALES_QUANTITY );
                 saveMap.put("CASH",   CASH           );
                 saveMap.put("CARD",   CARD           );
                 saveMap.put("OTHER",   OTHER          );
                 saveMap.put("DISCOUNT",   DISCOUNT       );
                 saveMap.put("YYYYMMDD",   YYYYMMDD       );
                 saveMap.put("CREATE_USER",   CREATE_USER    );
                 
                 createStoreMenuSalesMst1( saveMap, MONTH1, YYYYMMDD );
                 /*
                 MONTH1 = "06"; 
                 YYYYMMDD = YEAR1+"-"+MONTH1+"-"+DAY;
                 createStoreMenuSalesMst1( saveMap, MONTH1, YYYYMMDD );
                 
                 MONTH1 = "09"; 
                 YYYYMMDD = YEAR1+"-"+MONTH1+"-"+DAY;
                 createStoreMenuSalesMst1( saveMap, MONTH1, YYYYMMDD );

                 MONTH1 = "11"; 
                 YYYYMMDD = YEAR1+"-"+MONTH1+"-"+DAY;
                 createStoreMenuSalesMst1( saveMap, MONTH1, YYYYMMDD );
                 
                 MONTH1 = "12"; 
                 YYYYMMDD = YEAR1+"-"+MONTH1+"-"+DAY;
                 //createStoreMenuSalesMst1( saveMap, MONTH1, YYYYMMDD );
                 
                  */
                 
             }
             
             
         }
         
         return true;
         /*        
                  return true;
         */
         
     }

     
     /** 
      * 내용        : 배치일에 저장된 데이타가 없는경우 최근 데이타를 저장한다.
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 12. 10: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean createStoreMenuSalesMst1( Map<String, Object> saveMap, String MONTH1, String YYYYMMDD ) throws Exception {
         
         saveMap.put("MENU_SALES_ID",   ExdevCommonAPI.makeUniqueID(16)  );
         saveMap.put("MONTH",   MONTH1           );
         saveMap.put("YYYYMMDD",   YYYYMMDD           );
         commonDao.insert("jobScheduler.insertTestMenuSales", saveMap);
         /*
         Map insertInfo = (Map)commonDao.getObject("jobScheduler.getTestMenuSales1", saveMap);
         
         if( insertInfo == null) {
             commonDao.insert("jobScheduler.insertTestMenuSales", saveMap);
         }
         */
         return true;
         
     } 
     /** 
      * 내용        : 지점별 식자재 입고
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 11. 28: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean storeMaterialsInventoryIn() throws Exception {
         
         /*
         String dateYMD = DateUtil.getDateYMD(0);
         String[] dateParts = dateYMD.split("-");

         String year = dateParts[0];
         String month = dateParts[1];
         String day = dateParts[2];
         
         String dateYMDPre = DateUtil.getDateYMD(-1);

         */
         List<String> dateArr = DateUtil.makeDateYMDArr("2024-02-29", "2024-08-31");

         
         for(int i =0; i <dateArr.size(); i++) {
             String dateStr = dateArr.get(i);
             String dateYMDPre1 = DateUtil.AddDate( dateStr, 0, 0, -1);
             

             String[] dates1 = dateStr.split("-");
             String year  = dates1[0];
             String month = dates1[1];
             String day   = dates1[2];
             

             String[] dates2 = dateYMDPre1.split("-");
             String preYear  = dates2[0];
             String preMonth = dates2[1];
             String preDay   = dates2[2];
             
             Map<String, String> searchMap = new HashMap<String, String>();
             searchMap.put("year", year);
             searchMap.put("month", month);
             searchMap.put("day", day);
             searchMap.put("preYear", preYear);
             searchMap.put("preMonth", preMonth);
             searchMap.put("preDay", preDay);
             
             //int result1 = commonDao.update("jobScheduler.updateStockStoreCfg", searchMap);
             
             //매입
             //insertStockIn(searchMap);
             
             //매출
             //insertStockOut(searchMap);
             
             //재고계산
             insertStockOfDay(searchMap);
             
             //배치일에 해당하는 데이타가 없는경우
             insertNotExistData(searchMap);
             

             
         }
         
         return true;
         /*        
                  return true;
         */
         
     }

     
     /** 
      * 내용        : 배치일에 저장된 데이타가 없는경우 최근 데이타를 저장한다.
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 12. 10: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean insertNotExistData( Map<String, String> searchMap ) throws Exception {

         List<Map> list = commonDao.getList("jobScheduler.getTblExpStockMst", searchMap);
         
         String year   = searchMap.get("year");
         String month  = searchMap.get("month");
         String day    = searchMap.get("day");
         
         for(Map<String, Object> map : list){
             
             String stockMstId    = (String)map.get("STOCK_MST_ID");
             String brandId       = (String)map.get("BRAND_ID");
             String storeId       = (String)map.get("STORE_ID");//
             
             Map<String, String> searchMap1 = new HashMap<String, String>();
             
             searchMap1.put("stockMstId", stockMstId);
             searchMap1.put("brandId", brandId);
             searchMap1.put("storeId", storeId);
             searchMap1.put("year", year);
             searchMap1.put("month", month);
             searchMap1.put("day", day);
             
             
             Map checkMap    = (Map)commonDao.getObject("stockOfDayCheck", searchMap1);
             String checkStr = (String)checkMap.get("DATA_EXISTS");
             
             if( "N".equals(checkStr)) {//일별재고량에 재고가 없을경우 
                 
                 //최근날짜의 데이타 조회
                 Map lastMap    = (Map)commonDao.getObject("getLaststockOfDay", searchMap1);
                 

                 Map<String, Object> saveMap = new HashMap<>();
                 saveMap.put("stockMstId", searchMap1.get("stockMstId"));
                 saveMap.put("brandId", searchMap1.get("brandId"));
                 saveMap.put("storeId", searchMap1.get("storeId")); 
                 saveMap.put("year", searchMap1.get("year")); 
                 saveMap.put("month", searchMap1.get("month")); 
                 saveMap.put("day", searchMap1.get("day"));
                 
                 
                 if (lastMap != null && !lastMap.isEmpty()) {
                     // 최근날짜의 데이타가 있을경우
                     saveMap.put("quantity", (BigDecimal) lastMap.get("QUANTITY"));
                     
                 } else {
                     // 최근날짜의 데이타가 없을경우
                     saveMap.put("quantity", new BigDecimal("100"));
                 }
                 int result = commonDao.insert("jobScheduler.insertLastStockOfDay", saveMap);
                 
             }
             
         }
         
         
         return true;
         
     }

     
     /** 
      * 내용        : 재고 입출고 : 입고
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 12. 03: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean insertStockIn( Map<String, String> searchMap ) throws Exception {

         int result = commonDao.update("jobScheduler.stockInoutIn", searchMap);
         
         return true;
         
     }
     
     /** 
      * 내용        : 재고 입출고 : 출고
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 12. 03: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean insertStockOut( Map<String, String> searchMap ) throws Exception {

         int result = commonDao.update("jobScheduler.stockInoutOut", searchMap);
         
         return true;
         
     }

     /** 
      * 내용        : 재고 입출고 : 입고
      * @생 성 자   : 이응규
      * @생 성 일자 : 2024. 12. 03: 최초 생성
      * @수 정 자   : 
      * @수 정 일자 :
      * @수 정 자
      */
     public boolean insertStockOfDay( Map<String, String> searchMap ) throws Exception {

         int result = commonDao.update("jobScheduler.insertStockOfDay", searchMap);
         
         return true;
         
     }

}
