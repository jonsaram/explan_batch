package exdev.com.common.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import exdev.com.common.ExdevConstants;
import exdev.com.common.service.DataUploadService;
import exdev.com.common.service.JobSchedulerService;

@Controller("ExdevJobSchedulerController")
public class ExdevJobSchedulerController {

    @Autowired
    private JobSchedulerService jobSchedulerService;
    
    @Autowired
    private DataUploadService dataUploadService;

    /*
    public void test() throws Exception {
        String aa = jobSchedulerService.getOpenCloseCnt();
        System.out.println("ExdevJobSchedulerController");
        
    }
    */
    
    /** 
     * 내용        : 세친구 매출 API 호출
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 11. 21: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void taxpalStoreSales() throws Exception {
        jobSchedulerService.taxpalStoreSales();//세친구-사업장 정보 등록/수정 
    }
    

    /** 
     * 내용        : 세친구 매입 API 호출
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void taxpalStorePurchase() throws Exception {
        jobSchedulerService.taxpalStorePurchase();
        System.out.println("taxpalJob");
        
    }

    /** 
     * 내용        : 세친구-사업장 정보 등록/수정 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 06: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void taxpalStoresRegist() throws Exception {
        
        String type = (String)ExdevConstants.API_TYPE.USER.name();
        /*
         
         */
        // List<Map<String, Object>> 선언 및 생성
        List<Map<String, Object>> storeIDList = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("storeId", "5828b62e50e84eb6");
        map1.put("brn", "8018502621");//"8018502621"
        map1.put("name", "오봉집행당점");
        map1.put("bossName", "박세령");
        map1.put("phoneNumber", "01055524329");
        
        Map<String, Object> map2 = new HashMap<>();
        map2.put("storeId", "74967cb180214475");
        map2.put("brn", "8132001359");
        map2.put("name", "오봉집미아점");
        map2.put("bossName", "김은숙");
        map2.put("phoneNumber", "01084317788");
       
        storeIDList.add(map1);
        storeIDList.add(map2);
        
        
        Map<String, String> map = new HashMap<>();
        map.put("brandId", "obong");
        map.put("taxpalUseYn", "Y");
        map.put("taxpalRegistYnNotY", "Y");
        
        //List<Map> storeIDList = jobSchedulerService.getStoreMst( map );
        
        jobSchedulerService.taxpalStoresRegist( map );
        
    }
    
    /** 
     * 내용        : 유니온소프트 API 호출
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 13: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void unionsoftStoreSales() throws Exception {
        jobSchedulerService.unionsoftStoreSales();
    }


    /** 
     * 내용        : 유니온소프트 메뉴 매출 API 호출
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void unionposMenuSale() throws Exception {
        jobSchedulerService.unionposMenuSale();
        //jobSchedulerService.unionposMenuSaletTest();
    }

    /** 
     * 내용        : 유니온소프트 메뉴정보조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void unionposGroupMenu() throws Exception {
        jobSchedulerService.unionposGroupMenu();
    }
    
    /** 
     * 내용        : 유니온소프트 지점정보 조회
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 31: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void unionposStoreList() throws Exception {
        jobSchedulerService.unionposStoreList();
    }
    
    
    /** 
     * 내용        : API 로그 삭제
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 01. 14: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void delLog() throws Exception {
        jobSchedulerService.delLog();
    }
    

    /** 
     * 내용        : 매장별 목표 매출 생성
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 21: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void storeSalesGoal() throws Exception {
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        String thisDay = today.format(formatter);
        String[] thisDateParts = thisDay.split("-");
        // 년, 월, 일 추출
        String thisYear = thisDateParts[0];
        
        jobSchedulerService.storeSalesGoal("star1008" , thisYear);
    }
    
    /** 
     * 내용        : 지점별 식자재 재고
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 11. 28: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void storeMaterialsInventory() throws Exception {
       
        boolean result = jobSchedulerService.storeMaterialsInventoryIn();
        
    }
    
    /** 
     * 내용        : 임시 - 지점메뉴 매출 데이타 만들기
     * @생 성 자   : 이응규
     * @생 성 일자 : 2025. 02. 17: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    public void createStoreMenuSalesMst() throws Exception {
       
        boolean result = jobSchedulerService.createStoreMenuSalesMst();
        
    }

    /**
     * 내용 : 엑셀 업로드 템프 데이타 식자재 매출 입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 26: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    public void setMaterialsSales() throws Exception {
        dataUploadService.setMaterialsSales();
        dataUploadService.conversionUnitPrice();//환산단위당 단가
        
    }

    /** 
     * 내용        : KAKAO 채널로 데이터 전송
     * @생 성 자   : 유태원
     * @생 성 일자 : 2025. 03. 04: 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자
     */
    @Value("${kakao.accessToken}")
    private String kakaoAccessToken;
	public void pushDataToKakaoChannel() {
		RestTemplate restTemplate = new RestTemplate();

		String url = "https://kapi.kakao.com/v2/api/talk/memo/send";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer "+kakaoAccessToken);  // 카카오 인증 토큰
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 메시지 전송 데이터
		Map<String, Object> messagePayload = new HashMap<>();
		//messagePayload.put("template_id", "{TEMPLATE_ID}");  // 승인된 템플릿 ID
		//messagePayload.put("receiver_uuids", List.of("{USER_ID}"));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(messagePayload, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		System.out.println("Response: " + response.getBody());
		
	}
    
    
    
    
}
