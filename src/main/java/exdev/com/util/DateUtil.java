package exdev.com.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DateUtil {

    /** 
     * 내용        : 날짜 더하기
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 22 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     * String addDay   = DateUtil.AddDate("2024-03-21", 0, 0, 1); 1일후 날짜
     * String addWeek  = DateUtil.AddDate("2024-03-21", 0, 0, 8); 1주 후 날짜
     * String addMonth = DateUtil.AddDate("2024-03-21", 0, 1, 1); 1달 후 날짜
     * String addYear  = DateUtil.AddDate("2024-03-21", 1, 0, 1); 1년 후 날짜
     */  
    public static String AddDate(String strDate, int year, int month, int day) throws Exception {
        

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date dt = dtFormat.parse(strDate);
        
        cal.setTime(dt);
        
        cal.add(Calendar.YEAR,  year);
        cal.add(Calendar.MONTH, month);
        cal.add(Calendar.DATE,  day);
        
        return dtFormat.format(cal.getTime());
    }

    /** 
     * 내용        : 개월수 차이
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 04. 23 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     * int result = DateUtil.minusMonth("2024-01-01","2025-01-01");
     */  

    public static int minusMonth(String startDate, String endDate) throws Exception {
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        
        // 시작일
        Date strStartDate = dtFormat.parse(startDate);
        Calendar calStrDt = Calendar.getInstance();
        calStrDt.setTime(strStartDate);
        int startYear  = calStrDt.get(Calendar.YEAR); 
        int startMonth = calStrDt.get(Calendar.MONTH) +1;
        int startDay   = calStrDt.get(Calendar.DAY_OF_MONTH);
            
        // 종료일
        Date strEndDate   = dtFormat.parse(endDate);
        Calendar calEndDt = Calendar.getInstance();
        calEndDt.setTime(strEndDate);
        int endYear  = calEndDt.get(Calendar.YEAR); 
        int endMonth = calEndDt.get(Calendar.MONTH) +1;
        int endDay   = calEndDt.get(Calendar.DAY_OF_MONTH);
        
        // 차이 구하기
        int diffMonths = endMonth - startMonth;
        int diffYears = endYear - startYear;
        int reDate = (diffYears * 12 + diffMonths);
        
        return reDate;
    }

    /** 
     * 내용        : 현재 날짜 YYYY-MM-DD
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 11. 28 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     * DateUtil.getDateYMD(1); 1일후 날짜
     * DateUtil.getDateYMD( -1); 1일전 날짜
     */  
    public static String getDateYMD(int addDay) throws Exception {
    
        // 현재 날짜 생성
        Date now = new Date();
    
        // Calendar 사용하여 하루 전 날짜 계산
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, addDay); // 하루 전
    
        // 하루 전 날짜를 Date 객체로 변환
        Date yesterday = calendar.getTime();
    
        // SimpleDateFormat 사용
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    
        // 포맷된 날짜 출력
        String formattedDate = formatter.format(yesterday);
    
        // 년, 월, 일 개별 추출
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
    
        String year = yearFormat.format(yesterday);
        String month = monthFormat.format(yesterday);
        String day = dayFormat.format(yesterday);
    
        return year + "-" + month + "-" + day;
    }

    /** 
     * 내용        : 현재 날짜 YYYY-MM-DD
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 11. 28 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     * DateUtil.makeDateYMDArr( "2024-01-01", "2024-08-31" ); 
     */  
    public static List<String> makeDateYMDArr(String startDate, String endDate) throws Exception {
    

        // 시작 날짜와 종료 날짜 설정
        String[] dateRange = {startDate , endDate};
        
        // DateTimeFormatter를 사용해 문자열을 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate1 = LocalDate.parse(dateRange[0], formatter);
        LocalDate endDate1 = LocalDate.parse(dateRange[1], formatter);
        
        // 날짜 생성
        List<String> dateList = new ArrayList<>();
        while (!startDate1.isAfter(endDate1)) {
            dateList.add(startDate1.format(formatter)); // 포맷된 문자열 추가
            startDate1 = startDate1.plusDays(1); // 하루씩 증가
        }
        
        // 결과 출력
        dateList.forEach(System.out::println);
    
        return dateList;
    }

}
