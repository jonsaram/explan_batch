package exdev.com.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import exdev.com.common.controller.ExdevJobSchedulerController;

/*
 * delLog
     - cron = "0 00 1 * * *"
   unionposGroupMenu
     - cron = "0 5 1 1 * ?"
   unionsoftStoreSales
     - cron = "0 10 1 * * *"
   taxpalStoreSales
     - cron = "0 15 1 * * *"
   taxpalStorePurchase
     - cron = "0 20 1 * * *"
   unionposMenuSale
     - cron = "0 25 1 * * *"  
   unionposStoreList
     - cron = "0 30 1 * * *"
   storeSalesGoal
     - cron = "0 32 1 * * *"
   setMaterialsSales
     - cron = "0 40 1 * * *"      
     
     
 * */
@Component
public class JobScheduler {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private ExdevJobSchedulerController exdevJobSchedulerController;

    boolean IS_REAL = true;//true
    
    /*
    * 세친구 매출  cron = "0 15 1 * * *"
    * unionsoftStoreSales 보다 뒤에 실행되야 한다.
    * 
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled(cron = "0 15 1 * * *")
    public void taxpalStoreSales() {
      
      try {
          if(IS_REAL) {exdevJobSchedulerController.taxpalStoreSales();}
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.taxpalStoreSales(): ", e);
      }
    }

    /*
    * 세친구 매입  cron = "0 20 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled(cron = "0 20 1 * * *")
    public void taxpalStorePurchase() {
      
      try {
    
          // ExdevJobSchedulerController의 test 메서드 호출
          //exdevJobSchedulerController.taxpalStorePurchase();
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }

    /*
    * 세친구 사업장 정보 등록
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 * * * * *")
    public void taxpalStoresRegist() {
      
      try {
          
          //exdevJobSchedulerController.taxpalStoresRegist();
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }
    
    /*
    * 유니온 소프트 - 메뉴정보조회 매월 1일 새벽 4시 5분 cron = "0 5 4 1 * ?"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 5 4 1 * ?")
    public void unionposGroupMenu() {
      
      try {
          if(IS_REAL) {exdevJobSchedulerController.unionposGroupMenu();}
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.unionposGroupMenu(): ", e);
      }
    }

    /*
    * 유니온 소프트 - 포스업체(일별매출) cron = "0 10 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 10 1 * * *" )
    public void unionsoftStoreSales() {  
      try {

          if(IS_REAL) {exdevJobSchedulerController.unionsoftStoreSales();}
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.unionpos(): ", e);
      }
    }

    /*
    * 유니온 소프트 - 포스업체(메뉴매출) cron = "0 25 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 25 1 * * *")
    public void unionposMenuSale() {
      try {
          if(IS_REAL) {exdevJobSchedulerController.unionposMenuSale();}
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.unionposMenuSale(): ", e);
      }
    }
    
    /*
    * 유니온 소프트 - 지점정보 조회 cron = "0 30 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 30 1 * * *" )
    public void unionposStoreList() {
      try {
          if(IS_REAL) {exdevJobSchedulerController.unionposStoreList();}
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.unionposStoreList(): ", e);
      }
    }

    /*
    * API 로그 삭제cron = "0 00 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 00 1 * * *") 
    public void delLog() {
      
      try {
          // ExdevJobSchedulerController의 test 메서드 호출
          if(IS_REAL) {exdevJobSchedulerController.delLog();}
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }

    /*
    * 매장별 목표 매출 생성  cron = "0 32 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 32 1 * * *" )
    public void storeSalesGoal() {
      
      try {
          // ExdevJobSchedulerController의 test 메서드 호출
          if(IS_REAL) {exdevJobSchedulerController.storeSalesGoal();}
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }

    /*
    * 물류매출 엑셀업로드 데이타 적용  cron = "0 40 1 * * *"
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    */
    @Scheduled( cron = "0 43 1 * * *" )
    public void setMaterialsSales() {
      
      try {
          // ExdevJobSchedulerController의 test 메서드 호출
          if(IS_REAL) {exdevJobSchedulerController.setMaterialsSales();}
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }

    /*=================================================================================*/
    /*
    * 임시 - 지점메뉴 매출 데이타 만들기
    1분마다          :  cron = "0 * * * * *"
    매일새벽 1시  5분 : cron = "0 5 1 * * *"
    매일새벽 2시 15분 : cron = "0 15 2 * * *"
    매일오후 5시 10분 : cron = "0 12 17 * * *"
    
    @Scheduled(cron = "0 40 1 * * *")
    public void createStoreMenuSalesMst() {
      
      try {
          // ExdevJobSchedulerController의 test 메서드 호출
          exdevJobSchedulerController.createStoreMenuSalesMst();
          
      } catch (Exception e) {
          logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
      }
    }
    */
    /*=================================================================================*/

///* 
//* 지점별 식자재 재고
//1분마다          :  cron = "0 * * * * *"
//매일새벽 1시  5분 : cron = "0 5 1 * * *"
//매일새벽 2시 15분 : cron = "0 15 2 * * *"
//매일오후 5시 10분 : cron = "0 12 17 * * *"
//*/
//@Scheduled(cron = "0 35 17 * * *")
//public void storeMaterialsInventory() {
//
//  try {
//
//      logger.error("ExdevJobSchedulerController의 storeMaterialsInventory 메서드 호출 1 ");
//      // ExdevJobSchedulerController의 test 메서드 호출
//      exdevJobSchedulerController.storeMaterialsInventory();
//      
//      logger.error("ExdevJobSchedulerController의 storeMaterialsInventory 메서드 호출 2 ");
//      
//  } catch (Exception e) {
//      logger.error("Error executing ExdevJobSchedulerController.test(): ", e);
//  }
//}

    
    /*=================================================================================*/
    /*
    * KAKAO 채널로 PUSH 데이터 전송
    */
    //@Scheduled(cron = "0 40 1 * * *")
    public void pushDataToKakaoChannel() {
      
      try {
          exdevJobSchedulerController.pushDataToKakaoChannel();
          
      } catch (Exception e) {
          logger.error("Error executing pushDataToKakaoChannel: ", e);
      }
    }

    /*=================================================================================*/
}
