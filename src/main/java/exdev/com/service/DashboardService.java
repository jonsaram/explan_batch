package exdev.com.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.service.ExdevBaseService;
import exdev.com.util.DateUtil;

/**
 * This MovieServiceImpl class is an Implementation class to provide movie crud
 * functionality.
 * 
 * @author 이응규
 */
@Service("DashboardService")
public class DashboardService extends ExdevBaseService{
    
    @Autowired
    private ExdevCommonDao commonDao;
    

    /** 
     * 내용        : 매출현황 매출액,AI영업이익, 본사영업이익, 홈텍스 영업이익
     *               compPerformanceTab1.html
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 04. 16 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getMonthAccrue(Map map) throws Exception {

        String brandId    = (String)map.get("brandId");
        String startDate  = (String)map.get("startYMD");
        String endDate    = (String)map.get("endYMD");
        String region     = (String)map.get("region");
        String supervisor = (String)map.get("supervisor");
        String storeId    = (String)map.get("storeId");
        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int dateDiff = DateUtil.minusMonth(startDate, endDate);

        System.out.println("dateDiff["+dateDiff+"]    ");
        for( int i =0; i <= dateDiff; i++ ) {
            
            String addMonth  = DateUtil.AddDate(startDate, 0, i, 0);
            
            Map<String, String> searchMap = new HashMap<String, String>();
            searchMap.put("brandId", brandId);
            searchMap.put("seartDate", addMonth.substring(0, 7));
            searchMap.put("storeId", storeId);
            searchMap.put("region", region);
            searchMap.put("supervisor", supervisor);
            
            
            Map<String, Object> addMap = new HashMap<String, Object>();
            addMap.put("YYYYMM", addMonth.substring(0, 7));
            
            //매출액
            Map salesMap  = (Map)commonDao.getObject("dashboardCorPerformance.getSales"   , searchMap);
            //매출액
            if (salesMap != null) {
                if( salesMap.get("AMOUNT") != null) {
                    addMap.put("SALES", salesMap.get("AMOUNT"));
                }else {
                    addMap.put("SALES", 0);   
                }

                if( salesMap.get("AMOUNT") != null) {
                    addMap.put("BUSINESS_PROFITS_BRAND", salesMap.get("BRAND_PROFIT_AMOUNT"));
                }else {
                    addMap.put("BUSINESS_PROFITS_BRAND", 0);   
                }
                
                
            }else {
                addMap.put("SALES", 0);
                addMap.put("BUSINESS_PROFITS_BRAND", 0);
            }
            
            //AI 예측 매출액
            Map salesAIMap  = (Map)commonDao.getObject("dashboardCorPerformance.getAISales"   , searchMap);
            
            //AI 예측 매출액
            if (salesAIMap != null) {
                if( salesAIMap.get("AMOUNT") != null) {
                    addMap.put("BUSINESS_PROFITS_AI", salesAIMap.get("AMOUNT"));
                }else {
                    addMap.put("BUSINESS_PROFITS_AI", 0);    
                }
                

                if( salesAIMap.get("TAX_AMOUNT") != null) {
                    addMap.put("BUSINESS_PROFITS_TAX", salesMap.get("TAX_AMOUNT"));
                }else {
                    addMap.put("BUSINESS_PROFITS_TAX", 0);    
                }
                
            }else {
                addMap.put("BUSINESS_PROFITS_AI", 0);
                addMap.put("BUSINESS_PROFITS_BRAND", 0);
                addMap.put("BUSINESS_PROFITS_TAX", 0);
            }

            //제조원가
            Map costMap  = (Map)commonDao.getObject("dashboardCorPerformance.getCostSales"   , searchMap);

            //AI 예측 매출액
            if (costMap != null) {
                if( costMap.get("SALES_COST") != null) {
                    addMap.put("SALES_COST", costMap.get("SALES_COST"));
                }else {
                    addMap.put("SALES_COST", 0);    
                }
                
            }else {
                addMap.put("SALES_COST", 0);
            }
            
            list.add(addMap);
        }
        
        map.put("list", list);
        return map;
    }
    /** 
     * 내용        : 매출 상위20%, 하위20%
     *               compPerformanceTab1.html
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 03. 20 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getSalesUpperLowerStore(Map map) throws Exception {

        /*=====================================================*/
        String startDate = (String)map.get("startYMD");
        String endDate = (String)map.get("endYMD");
        String region = (String)map.get("region");
        String supervisor = (String)map.get("supervisor");
        String brandId = (String)map.get("brandId");
        String upLow = (String)map.get("upLow");
        String targetRate = (String)map.get("targetRate");
        
        int startDateNum = Integer.parseInt(startDate.replace("-", ""));
        int endDateNum = Integer.parseInt(endDate.replace("-", ""));
        
        Map<String, String> commonMap = new HashMap<String, String>();
        commonMap.put("GRP_CODE_ID", "TARGET_RATE");
        commonMap.put("CODE_ID", targetRate);
        
        Map codeInfo = (Map)commonDao.getObject("common.getCommonCodeList"   , commonMap);
        double upVal  = Double.parseDouble((String)codeInfo.get("ATTR1"));
        double lowVal = Double.parseDouble((String)codeInfo.get("ATTR2"));

        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        
        for( int i =0; startDateNum < endDateNum; i++ ) {
           
            String addMonth  = DateUtil.AddDate(startDate, 0, i, 0);
            startDateNum = Integer.parseInt(addMonth.replace("-", ""));
            
            Map<String, Object> searchMap = new HashMap<String, Object>();
            searchMap.put("brandId", brandId);
            searchMap.put("seartDate", addMonth.substring(0, 7));
            searchMap.put("region", region);
            searchMap.put("supervisor", supervisor);
            searchMap.put("upLow", upLow);
            searchMap.put("upVal", upVal);
            searchMap.put("lowVal", lowVal);
            
            List<Map> resultList = commonDao.getList("dashboardCorPerformance.getSalesUpperLowerStore", searchMap);
            for(Map resultMap : resultList) {
                Map<String, Object> addMap = new HashMap<String, Object>();
                addMap.put("YYYYMM", addMonth.substring(0, 7));
                String YEAR = (String)resultMap.get("YEAR");
                String MONTH = (String)resultMap.get("MONTH");
                String STORE_NM  = String.valueOf(resultMap.get("STORE_NM"));
                String GROSS_SALES  = String.valueOf(resultMap.get("GROSS_SALES"));
          
                addMap.put("YEAR", YEAR);
                addMap.put("MONTH", MONTH);
                addMap.put("STORE_NM", STORE_NM);
                addMap.put("GROSS_SALES", GROSS_SALES);
                list.add(addMap);
            }
        }
        map.put("list", list);
        return map;
    }
    /** 
     * 내용        : 매출 상위20%, 하위20%, 평균
     *               compPerformanceTab1.html
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 03. 20 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getUpperLowerSales(Map map) throws Exception {

        /*=====================================================*/
        String startDate = (String)map.get("startYMD");
        String endDate = (String)map.get("endYMD");
        String region = (String)map.get("region");
        String supervisor = (String)map.get("supervisor");
        String brandId = (String)map.get("brandId");
        String targetRate = (String)map.get("targetRate");
        
        int startDateNum = Integer.parseInt(startDate.replace("-", ""));
        int endDateNum = Integer.parseInt(endDate.replace("-", ""));

        Map<String, String> commonMap = new HashMap<String, String>();
        commonMap.put("GRP_CODE_ID", "TARGET_RATE");
        commonMap.put("CODE_ID", targetRate);
        
        Map codeInfo = (Map)commonDao.getObject("common.getCommonCodeList"   , commonMap);
        double upVal  = Double.parseDouble((String)codeInfo.get("ATTR1"));
        double lowVal = Double.parseDouble((String)codeInfo.get("ATTR2"));
        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        
        for( int i =0; startDateNum < endDateNum; i++ ) {
           
            String addMonth  = DateUtil.AddDate(startDate, 0, i, 0);
            startDateNum = Integer.parseInt(addMonth.replace("-", ""));
            
            Map<String, Object> searchMap = new HashMap<String, Object>();
            searchMap.put("brandId", brandId);
            searchMap.put("seartDate", addMonth.substring(0, 7));
            searchMap.put("region", region);
            searchMap.put("supervisor", supervisor);
            searchMap.put("upVal", upVal);
            searchMap.put("lowVal", lowVal);
        
            Map<String, Object> addMap = new HashMap<String, Object>();
            addMap.put("YYYYMM", addMonth.substring(0, 7));
            
            List<Map> resultList = commonDao.getList("dashboardCorPerformance.getUpperLowerSales", searchMap);
            for(Map resultMap : resultList) {
                String YEAR = (String)resultMap.get("YEAR");
                String MONTH = (String)resultMap.get("MONTH");
                String UPPER_20_AVG  = String.valueOf(resultMap.get("UPPER_20_AVG"));
                String LOWER_20_AVG  = String.valueOf(resultMap.get("LOWER_20_AVG"));
                String TOTAL_AVG  = String.valueOf(resultMap.get("TOTAL_AVG"));
          
                addMap.put("MONTH", MONTH);
                addMap.put("YEAR", YEAR);
                addMap.put("UPPER_20_AVG", UPPER_20_AVG);
                addMap.put("LOWER_20_AVG", LOWER_20_AVG);
                addMap.put("TOTAL_AVG", TOTAL_AVG);
      
            }
                  
            list.add(addMap);
        }
        map.put("list", list);
        return map;
    }

    
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
    public Object getOpenCloseCnt(Map map) throws Exception {

        /*=====================================================*/
        String startDate = (String)map.get("startDate");
        String endDate = (String)map.get("endDate");
        String region = (String)map.get("region");
        String supervisor = (String)map.get("supervisor");
        String brandId = (String)map.get("brandId");
        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        int dateDiff = DateUtil.minusMonth(startDate, endDate);
        
        for( int i =0; i <= dateDiff; i++ ) {
           
            String addMonth  = DateUtil.AddDate(startDate, 0, i, 0);
            
            Map<String, String> searchMap = new HashMap<String, String>();
            searchMap.put("brandId", brandId);
            searchMap.put("seartDate", addMonth.substring(0, 7));
            searchMap.put("region", region);
            searchMap.put("supervisor", supervisor);
        
            Map<String, Object> addMap = new HashMap<String, Object>();
            addMap.put("YYYYMM", addMonth.substring(0, 7));
            
            List<Map> resultList = commonDao.getList("dashboardCorPerformance.getOpenCloseCnt", searchMap);
            for(Map resultMap : resultList) {
                String type = (String)resultMap.get("TYPE");
                String cnt  = String.valueOf(resultMap.get("CNT"));
                
                if( "OPEN".equals( type )) {
                    addMap.put("OPEN", cnt);
                }else if( "CLOSE".equals( type )) {
                    addMap.put("CLOSE", cnt);
                }else if( "ALL".equals( type )) { 
                    addMap.put("ALL", cnt);
                }
            }
            
            list.add(addMap);
        }
        map.put("list", list);
        return map;
    }

    /** 
     * 내용        : 
     *               compPerformanceTab4.html
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getmenuMarginMonthTop5(Map map) throws Exception {

        /*=====================================================*/
        String group1 = (String)map.get("group1");
        String group2 = (String)map.get("group2");
        String menu = (String)map.get("menu");
        String year = (String)map.get("year");
        String store = (String)map.get("store");
        String region = (String)map.get("region");
        String brandId = (String)map.get("brandId");
        String supervisor = (String)map.get("supervisor");
        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("group1", group1);
        searchMap.put("group2", group2);
        searchMap.put("menu", menu);
        searchMap.put("year", year);
        searchMap.put("store", store);
        searchMap.put("region", region);
        searchMap.put("brandId", brandId);
        searchMap.put("supervisor", supervisor);
    
        

        String queryUrl = "dashboardCorPerformance.getmenuMarginMonthTop5";
        //serviceUrl = 'dashboardCorPerformance.getmenuMarginDayTop5';
        
        List<Map> resultList = commonDao.getList(queryUrl, searchMap);
        for(Map resultMap : resultList) {
            Map<String, Object> addMap = new HashMap<String, Object>();
            String BRAND_MENU_ID = (String)resultMap.get("BRAND_MENU_ID");
            String MENU_NM = (String)resultMap.get("MENU_NM");
            String MONTH = (String)resultMap.get("MONTH");
            String SALES  = String.valueOf(resultMap.get("SALES"));
            
            addMap.put("BRAND_MENU_ID", BRAND_MENU_ID);
            addMap.put("MENU_NM", MENU_NM);
            addMap.put("MONTH", MONTH);
            addMap.put("SALES", SALES);
            list.add(addMap);
        }
        map.put("list", list);
        return map;
    }
    
    /** 
     * 내용        : 매장별 메뉴 매출
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 05. 22 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getMenuMonthSales(Map map) throws Exception {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map> resultList = commonDao.getList("dashboardCorPerformance.getmenuSalesMonthTop5", map);
        
        for(Map resultMap : resultList) {
            
            
        }
        map.put("list", list);
        return map;
    }
    
}