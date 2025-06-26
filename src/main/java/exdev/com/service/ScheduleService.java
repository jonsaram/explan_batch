package exdev.com.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.service.ExdevBaseService;

/**
 * This MovieServiceImpl class is an Implementation class to provide movie crud
 * functionality.
 * 
 * @author 이응규
 */
@Service("ScheduleService")
public class ScheduleService extends ExdevBaseService{
    
    @Autowired
    private ExdevCommonDao commonDao;

    /** 
     * 내용        : 일정관리 (scheduleManage.html)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 06. 26 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public int  synchProcess1(Map map ) throws Exception {
        
        int result = 0;
        
        String json = (String)map.get("googleEvent");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> googleEventList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});
        
        // 구글:O,  EXP:O 일경우 수정
        for(Map<String, Object> eventMap : googleEventList){
           
            String scheduleId = (String)eventMap.get("id");
            String title      = (String)eventMap.get("title");
            LinkedHashMap<String, String> extendedProps = (LinkedHashMap<String, String>) eventMap.get("extendedProps");
         
            String systemType = extendedProps.get("systemType");
            
            Map<String, String> saveMap = new HashMap<String, String>();
            if( "GOOGLE".equals(systemType) ) {saveMap.put("scheduleId", null);} else {saveMap.put("scheduleId", scheduleId);}   

            saveMap.put("googleCalendarId", extendedProps.get("googleId"));
            saveMap.put("title", title);
            saveMap.put("scheduleStartDate", extendedProps.get("scheduleStartDate"));
            saveMap.put("startTimeH", extendedProps.get("startTimeH"));
            saveMap.put("startTimeM", extendedProps.get("startTimeM"));
            saveMap.put("scheduleEndDate", extendedProps.get("scheduleEndDate"));
            saveMap.put("endTimeH", extendedProps.get("endTimeH"));
            saveMap.put("endTimeM", extendedProps.get("endTimeM"));
            saveMap.put("location", extendedProps.get("location"));
            saveMap.put("description", extendedProps.get("description"));
            saveMap.put("userId", extendedProps.get("userId"));
            saveMap.put("spCstmId", extendedProps.get("spCstmId"));
            result += commonDao.update("schedule.updateGoogleSchedule", saveMap);
        }
        
        return result;
    }

    /** 
     * 내용        : 구글데이타 동기화 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 06. 26 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  synchProcess2(Map map ) throws Exception {
        
        int result = 0;
        
        // 구글:O,  EXP:X 일경우 입력
        // 삭제로그 조회후 삭제로그에 있을 경우 구글 삭제, 해당 구글 아이디를 리턴한다(*).
        // 삭제로그 조회후 삭제로그에 없을 경우 EXP 입력

        String json = (String)map.get("googleEvent");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> googleEventList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});
        
        List<Map> listMap = new ArrayList<Map>();
        // 구글:O,  EXP:O 일경우 수정
        for(Map<String, Object> eventMap : googleEventList){

            String title      = (String)eventMap.get("title");
            
            LinkedHashMap<String, String> extendedProps = (LinkedHashMap<String, String>) eventMap.get("extendedProps");   
            Map<String, String> saveMap = new HashMap<String, String>();
            String googleCalendarEventId = extendedProps.get("googleId");
            
            saveMap.put("spCstmId", extendedProps.get("spCstmId"));
            saveMap.put("googleCalendarEventId", googleCalendarEventId);
            
            //삭제테이블에서 삭제 여부확인
            Map delSchedul = (Map)commonDao.getObject("schedule.getScheduleDel", saveMap);
            
            String scheduleExist     = (String)delSchedul.get("SCHEDULE_EXIST");
            String scheduleDelExist  = (String)delSchedul.get("SCHEDULE_DEL_EXIST");
            String location          = extendedProps.get("location");
            if (location.length() > 200) {
                location = location.substring(0, 200);
            }
            
            if( "N".equals(scheduleExist) && "N".equals(scheduleDelExist)) {
                //구글 일정이 일정테이블에 없고 삭제 테이블에도 없을경우 구글 일정을 입력한다.
                //입력
                saveMap.put("scheduleId", extendedProps.get("makeScheduleId"));
                saveMap.put("scheduleGrpId", extendedProps.get("makeScheduleGrpId"));
                saveMap.put("loopType", "NOT_REPEAT");
                saveMap.put("loopTypeDtlCd", null);
                saveMap.put("loopTypeDtlVal", null);
                saveMap.put("loopLimitDate", extendedProps.get("scheduleEndDate"));
                saveMap.put("title", title);
                saveMap.put("workType", "OTHER_WORK");
                saveMap.put("timeType", null);
                saveMap.put("scheduleStartDate", extendedProps.get("scheduleStartDate"));
                saveMap.put("startTimeH", extendedProps.get("startTimeH"));
                saveMap.put("startTimeM", extendedProps.get("startTimeM"));
                saveMap.put("scheduleEndDate", extendedProps.get("scheduleEndDate"));
                saveMap.put("endTimeH", extendedProps.get("endTimeH"));
                saveMap.put("endTimeM", extendedProps.get("endTimeM"));
                saveMap.put("contractId", null);
                saveMap.put("position", location);
                saveMap.put("secretYn", "N");
                saveMap.put("description", extendedProps.get("description"));
                saveMap.put("userId", extendedProps.get("userId"));
                result += commonDao.insert("schedule.insertGoogleSchedule", saveMap);
            }else if( "N".equals(scheduleExist) && "Y".equals(scheduleDelExist)) {
                //구글 일정이 일정테이블에 없고 삭제 테이블에 있을경우 해당 구글일정은 구글에서 삭제 대상.
                //구글 삭제 대상
                Map<String, Object> delMap = new HashMap<String, Object>();
                delMap.put("googleId", googleCalendarEventId);
                listMap.add(delMap);
                
            }else {}
            
        }
         
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("list", listMap);
        return returnMap;
    }
    

    /** 
     * 내용        : 구글데이타 동기화 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 01 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  synchProcess3(Map map ) throws Exception {
        
        int result = 0;
        
        // 구글:X,  EXP:O 일경우
        // 1.EXP에서 입력후 오류로           구글 X,   EXP에 구글 ID 없음
        // 2.EXP에서 입력후 구글연동 사용 X, 구글 X,   EXP에 구글 ID 없음
        // 3.EXP에서 입력후 구글에서 삭제    구글 X,   EXP에 구글 ID 있음
        // 1,2 의경우는 사용자가 구글연동 사용 Y일경우 구글에 입력, 입력데이타를 리턴한다(*).
        // 3.일경우 EXP 삭제
        
        String userId = (String)map.get("userId");
        String spCstmId = (String)map.get("spCstmId");
        String startDate = (String)map.get("startDate");
        String endDate = (String)map.get("endDate");
        
        String json = (String)map.get("googleEvent");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> googleEventList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});
        
        List<Map> listMap = new ArrayList<Map>();
        List<Map> delChecklistMap = new ArrayList<Map>();
        

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("spCstmId", spCstmId);
        searchMap.put("userId", userId);
        searchMap.put("startDay", startDate);
        searchMap.put("endDay", endDate);
        
        List<Map> list = commonDao.getList("schedule.getScheduleGoogle", searchMap);
        
        for(Map<String, Object> scheduleMap : list){
        
            String scheduleId             = (String)scheduleMap.get("SCHEDULE_ID");
            String googleCalendarEventId1 = (String)scheduleMap.get("GOOGLE_CALENDAR_EVENT_ID");
            String googleYn               = (String)scheduleMap.get("GOOGLE_YN");
            
            if( "N".equals(googleYn)) {//EXP에는 있는데 구글ID가 없을경우 구글일정에 추가
                listMap.add(scheduleMap);// 구글일정에 추가대상 
            }else {
                delChecklistMap.add(scheduleMap);// EXP에는 있는데 구글ID가 있을경우
            }
        }
        
        // EXP에서 입력후 구글에서 삭제된경우 EXP일정을 삭제함 
        // 구글 X,   EXP에 구글 ID 있음
        for(Map<String, Object> delCheckMap : delChecklistMap){
            String expGoogleEventId  = (String)delCheckMap.get("GOOGLE_CALENDAR_EVENT_ID");
            
            boolean isDelete = true;
            for(Map<String, Object> eventMap : googleEventList){            
                LinkedHashMap<String, String> extendedProps = (LinkedHashMap<String, String>) eventMap.get("extendedProps");
                String googleEventId = extendedProps.get("googleId");

                if( googleEventId.equals(expGoogleEventId)) {// EXP 일정의 구글ID와 구글일정의 ID가 같다면 
                    isDelete = false;
                    break;
                }
            }
            if( isDelete ) {
                Map<String, String> scheduleMap = new HashMap<String, String>();
                
                scheduleMap.put("scheduleId",(String)delCheckMap.get("SCHEDULE_ID") );
                scheduleMap.put("userId", userId);
                scheduleMap.put("spCstmId", spCstmId);
                scheduleMap.put("allApplyYn", "N");
                
                Map<String,Object> resultInfo = deleteSchedule(scheduleMap);
            }
            
        }
        
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("list", listMap);
        return returnMap;
    }

    /** 
     * 내용        : 구글 Event id를 EXP에 적용
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 03 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  synchProcess4(Map map) throws Exception {
        
        int result = 0;

        String userId   = (String)map.get("userId");
        String spCstmId = (String)map.get("spCstmId");
        String json     = (String)map.get("googleEvent");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> saveEventList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});

        for(Map<String, Object> scheduleMap : saveEventList){
            String scheduleId             = (String)scheduleMap.get("scheduleId");
            String googleCalendarEventId = (String)scheduleMap.get("googleCalendarEventId");
            scheduleMap.put("userId", userId);
            scheduleMap.put("spCstmId", spCstmId);
            
            result += commonDao.update("schedule.updateGoogleSchedule1", scheduleMap);
        }
       
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("list", null);
        return returnMap;
    }
    /** 
     * 내용        : 일정관리 수정(addSchedulePopup.html)
     *               일정관리(TBL_EXP_SCHEDULE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  updateSchedule(Map map ) throws Exception {
        
        int result = 0;
        //프로젝트로 대체 24-07-31
        //String json = (String)map.get("users");
        //ObjectMapper mapper = new ObjectMapper();
        //List<Map<String, Object>> apprUserList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});
        
        result += commonDao.update("schedule.updateSchedule", map);
        
        //프로젝트로 대체 24-07-31
        //result += deleteScheduleShare(map);
        //result += saveScheduleShare(map, apprUserList );
        
        
        if( result > 0  ) {
            resultInfo = makeResult(ExdevBaseService.REQUEST_SUCCESS, "", null);
        }else {
            resultInfo = makeResult(ExdevBaseService.REQUEST_FAIL, "", null);
        }
               
        
        
        return resultInfo;
    }

    /** 
     * 내용        : 일정공유 삭제
     *               일정공유(TBL_EXP_SCHEDULE_SHARE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 27 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public int deleteScheduleShare(Map map) throws Exception {
        
        int result = 0;
        result += commonDao.delete("schedule.deleteScheduleShare", map);
        
        return result;
    }
    
    /** 
     * 내용        : 일정관리삭제(addSchedulePopup.html)
     *               일정관리(TBL_EXP_SCHEDULE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 26 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  deleteSchedule(Map map ) throws Exception {

        int result = 0;
        
        String scheduleId    = (String)map.get("scheduleId");
        String userId        = (String)map.get("userId");
        String spCstmId      = (String)map.get("spCstmId");

        Map<String, String> scheduleMap = new HashMap<String, String>();
        scheduleMap.put("scheduleId", scheduleId);
        scheduleMap.put("delUserId", userId);
        scheduleMap.put("delSpCstmId", spCstmId);
        result += commonDao.insert("schedule.insertScheduleDel", scheduleMap);
        result += commonDao.delete("schedule.deleteSchedule", scheduleMap);
        
        if( result > 0  ) {
            resultInfo = makeResult(ExdevBaseService.REQUEST_SUCCESS, "", null);
        }else {
            resultInfo = makeResult(ExdevBaseService.REQUEST_FAIL, "", null);
        }
               
        
        
        return resultInfo;
    }

    /** 
     * 내용        : 일정관리 입력(addSchedulePopup.html)
     *               일정관리(TBL_EXP_SCHEDULE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 21 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  saveSchedule(Map map ) throws Exception {
        
        /*
        ArrayList<String> scheduleIdArry = new ArrayList<>();
        scheduleIdArry = (ArrayList<String>)map.get("scheduleIdArry");
        
        String[] scheduleIds = new String[scheduleIdArry.size()];
        
        for (int i=0; i<scheduleIdArry.size(); i++) {
            scheduleIds[i] = scheduleIdArry.get(i);
        }
        */
        
        Map<String,Object> resultMap = saveScheduleAndShare( map );
        
        return resultMap;
    }


    /** 
     * 내용        : 일정관리 입력
     *               일정관리(TBL_EXP_SCHEDULE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 21 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public Map<String,Object>  saveScheduleAndShare(Map map ) throws Exception {
        
        int result = 0;
        
        //String json              = (String)map.get("users");
        //ObjectMapper mapper = new ObjectMapper();
        //List<Map<String, Object>> apprUserList = mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>(){});
        
        result += commonDao.insert("schedule.insertSchedule", map);
        
        //프로젝트로 대체 24-07-31
        //result += saveScheduleShare(map, apprUserList );
        
        
        if( result > 0  ) {
            resultInfo = makeResult(ExdevBaseService.REQUEST_SUCCESS, "", null);
        }else {
            resultInfo = makeResult(ExdevBaseService.REQUEST_FAIL, "", null);
        }
        
        return resultInfo;
    }

    /** 
     * 내용        : 일정공유 입력
     *               일정공유(TBL_EXP_SCHEDULE_SHARE)
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 21 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public int saveScheduleShare(Map map, List<Map<String, Object>> userList ) throws Exception {
        
        int result = 0;
        
        for(Map<String, Object> userMap : userList){
            
            Map<String, String> saveMap = new HashMap<String, String>();
            
            saveMap.put("scheduleId", (String)map.get("scheduleId"));
            saveMap.put("userId", (String)userMap.get("USER_ID"));
            saveMap.put("spCstmId", (String)userMap.get("SP_CSTM_ID"));
            result += commonDao.insert("schedule.insertScheduleShare", saveMap);
            
        }
        
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getNotRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        List<Map> list = commonDao.getList("schedule.getNotRepeat", map);
        for(Map resultMap : list) {
            listMap.add(resultMap);
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getDayRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매일반복 */
        List<Map> weeklist = commonDao.getList("schedule.getDayRepeat", map);
        for(Map resultMap : weeklist) {
            listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getWeekRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매주반복 */
        /**/
        List<Map> weeklist = commonDao.getList("schedule.getWeekRepeat", map);
        for(Map resultMap : weeklist) {
            listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getMonthRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매월반복 말일선택 */
        List<Map> monthListSelectDay = commonDao.getList("schedule.getMonthRepeatSelectDay", map);
        for(Map resultMap : monthListSelectDay) {
          listMap.add(resultMap); 
        } 
        
        /* 매월반복 */  
        List<Map> monthList = commonDao.getList("schedule.getMonthRepeat", map);
        for(Map resultMap : monthList) {
          listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getQuarterRepeatSchedule(Map map) throws Exception {
                
        List<Map> listMap = new ArrayList<Map>();
        
        /* 분기반복 */  
        List<Map> monthList = commonDao.getList("schedule.getQuarterRepeat", map);

        for(Map resultMap : monthList) {
          listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getYearRepeatSchedule(Map map) throws Exception {
                
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매년반복 */
        List<Map> yearList = commonDao.getList("schedule.getYearRepeat", map);
    
        for(Map resultMap : yearList) {
          listMap.add(resultMap);
        }
        
        map.put("list", listMap);
        return map;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매일반복 */
        /**/
        List<Map> daylist = commonDao.getList("schedule.getTeamDayRepeat", map);
        for(Map resultMap : daylist) {
            listMap.add(resultMap); 
        }
        
        /* 반복안함 */
        /**/
        List<Map> notRepeatlist = commonDao.getList("schedule.getTeamNotRepeat", map);
        for(Map resultMap : notRepeatlist) {
            listMap.add(resultMap); 
        }
        
        /* 매주 반복 */
        /**/
        List<Map> weekRepeatlist = commonDao.getList("schedule.getTeamWeekRepeat", map);
        for(Map resultMap : weekRepeatlist) {
            listMap.add(resultMap); 
        }

        /* 매월 말일선택 반복 */
        /* */
        List<Map> monthRepeatlistSelectDay = commonDao.getList("schedule.getTeamMonthRepeatSelectDay", map);
        for(Map resultMap : monthRepeatlistSelectDay) {
            listMap.add(resultMap);  
        }
        
        /* 매월 반복 */
        /* */
        List<Map> monthRepeatlist = commonDao.getList("schedule.getTeamMonthRepeat", map);
        for(Map resultMap : monthRepeatlist) {
            listMap.add(resultMap);  
        }
        
        /* 매년 반복 */
        /**/
        List<Map> yearRepeatlist = commonDao.getList("schedule.getTeamYearRepeat", map);
        for(Map resultMap : yearRepeatlist) {
            listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamDayRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 매일반복 */
        /**/
        List<Map> daylist = commonDao.getList("schedule.getTeamDayRepeat", map);
        for(Map resultMap : daylist) {
            listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamNotRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();
        
        /* 반복안함 */
        /**/
        List<Map> notRepeatlist = commonDao.getList("schedule.getTeamNotRepeat", map);
        for(Map resultMap : notRepeatlist) {
            listMap.add(resultMap); 
        }
        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamWeekRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();

        /* 매주 반복 */
        /**/
        List<Map> weekRepeatlist = commonDao.getList("schedule.getTeamWeekRepeat", map);
        for(Map resultMap : weekRepeatlist) {
            listMap.add(resultMap); 
        }

        
        map.put("list", listMap);
        return map;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamMonthRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();

        /* 매월 말일선택 반복 */
        /* */
        List<Map> monthRepeatlistSelectDay = commonDao.getList("schedule.getTeamMonthRepeatSelectDay", map);
        for(Map resultMap : monthRepeatlistSelectDay) {
            listMap.add(resultMap);  
        }
        
        /* 매월 반복 */
        /* */
        List<Map> monthRepeatlist = commonDao.getList("schedule.getTeamMonthRepeat", map);
        for(Map resultMap : monthRepeatlist) {
            listMap.add(resultMap);  
        }

        
        map.put("list", listMap);
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getTeamYearRepeatSchedule(Map map) throws Exception {
        
        List<Map> listMap = new ArrayList<Map>();

        /* 매년 반복 */
        /**/
        List<Map> yearRepeatlist = commonDao.getList("schedule.getTeamYearRepeat", map);
        for(Map resultMap : yearRepeatlist) {
            listMap.add(resultMap); 
        }

        
        map.put("list", listMap);
        return map;
    }
    
}

