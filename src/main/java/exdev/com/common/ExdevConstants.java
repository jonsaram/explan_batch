package exdev.com.common;

import java.util.Arrays;
import java.util.List;

public class ExdevConstants {
	public static String SESSION_ID 				= "SID";
	public static String EMAIL_TEMPLATE_PATH 		= "/ui/mail-template/";
    public static final String REQUEST_SUCCESS 	= "S";
    public static final String REQUEST_FAIL    	= "F";
    public static final String FILE_UPLOAD_PATH	= "uploadFiles";
    public static final String EDITOR_PATH      = "editorFiles";
        
    //로컬
    //     public static final String FILE_DIRECTORY_PATH= "D:/OCI/workspace/exdev";
//    public static final String TRANSFOR_SERVER_URL = "https://localhost:44327";
//    public static final String TRANSFOR_SERVER_URL_CONVERT = "Home/ConvertToPdf";
    
    //스테이지
    public static final String FILE_DIRECTORY_PATH= "/shared/uploadFiles";
    public static final String TRANSFOR_SERVER_URL = "http://explan02.cafe24.com/";
    public static final String TRANSFOR_SERVER_URL_CONVERT = "Home/ConvertToPdf";
    
    //운영
    //public static final String FILE_DIRECTORY_PATH= "/home/ubuntu/spring-boot";
    //public static final String TRANSFOR_SERVER_URL = "http://explan01.cafe24.com/";
    //public static final String TRANSFOR_SERVER_URL_CONVERT = "Home/ConvertToPdf";
    

    //API 업체
    public static final String API_COMPANY_TAXPAL  = "TAXPAL"; //세친구
    public enum API_COMPANY{
        TAXPAL,      //세친구
        UNIONSOFT;   //유니온 소프트
    }
    //세친구 개발
    public static final String TAXPAL_BASE_URL  = "https://dev-api.taxpal.co.kr";
    public static final String TAXPAL_ACCESS_KEY = "x-access-key";
    public static final String TAXPAL_ACCESS_VALUE= "Exyh+zAYvMw0Ui6WYeq9iOlPpZHPJPfYbWmqvoOFLxo";

    public static final String SITE_CD_HOMETAX  = "hometax";
    public static final String SITE_CD_YESSIN   = "yessin";
    public static final String SITE_CD_BAEMIN   = "baemin";
    

    //유니온 소프트
    public static final String UNIONSOFT_BASE_URL  = "https://api.unionpos.kr/api/v2/out";
    public static final String UNIONSOFT_ACCESS_KEY = "Authorization";
    public static final String UNIONSOFT_ACCESS_VALUE= "Union c4a4bef569ea42c0b160c71093a2df11";
    
    //API 코드
    public enum API_CD{
        STORE_SALES,        //지점 일별매출
        STORE_MENU_SALES;   //지점 일별메뉴매출
    }
    
    //API 호출 타입
    public enum API_TYPE{
        BATCH,  //배치
        USER;   //사용자
    }

    //식자재 업체
    public enum FOOD_MATERIALS_COMPANY{
        obong_SPC,  //SPC
        obong_BONFS,   //본푸드
        obong_JANGGA,   //장가
        obong_HARAM;   //하람
    }
    
    // COMN 계정 공통
    public static final List<String> commonTableList = Arrays.asList(
       	 "TBL_EXP_AUTH"
       	,"TBL_EXP_CODE"
       	,"TBL_EXP_GRPCODE"
       	,"TBL_EXP_MENU"
       	,"TBL_EXP_ROLE"
       	,"TBL_EXP_ROLEMENU"
    );
       
    // POSD 계정 공통
    public static final List<String> posDataTableList = Arrays.asList(
          "TBL_EXP_STORE_SALES_MST"
       	 ,"TBL_EXP_STORE_MENU_SALES_MST"
    );
}
