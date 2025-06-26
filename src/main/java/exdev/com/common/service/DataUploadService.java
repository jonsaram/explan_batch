package exdev.com.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.service.FileService;

@Service("DataUploadService")
public class DataUploadService {
    @Autowired
    private ExdevCommonDao commonDao;

    @Autowired
    private FileService fileService;
    
    /**
     * 내용 : 엑셀 업로드 데이타 삭제
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 14: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean deleteExcelUplod(Map map) throws Exception {
        
        int result = 0;

        String uploadLogId = (String)map.get("uploadLogId");
        String applyCompYn = (String)map.get("applyCompYn");
        String companyCd   = (String)map.get("companyCd");
        String fileId      = (String)map.get("fileId");
        
        System.out.println("uploadLogId["+uploadLogId+"]  uploadLogId["+uploadLogId+"]  uploadLogId["+uploadLogId+"]  fileId["+fileId+"]   ");
        
        String[] fileIdArray = new String[1];
        
        fileIdArray[0] = fileId;
        fileService.fileDeleteMulti(null, fileIdArray); 
        
        Map<String, String> delMap = new HashMap<String, String>();
        delMap.put("UPLOAD_LOG_ID", uploadLogId);
        if( "Y".equals(applyCompYn) ) {
            commonDao.delete("storeManage.deleteFoodMaterialsSalesFromOrder", delMap);
        }

        commonDao.delete("dataUpload.delMaterialsExceluploadLog", delMap);
        
        if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_BONFS.name()).equals(companyCd)) {
            commonDao.delete("dataUpload.delBonfoodSalesTemp", delMap);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_JANGGA.name()).equals(companyCd)) {
            commonDao.delete("dataUpload.delJanggaSalesTemp", delMap);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_HARAM.name()).equals(companyCd)) {
            commonDao.delete("dataUpload.delHaramSalesTemp", delMap);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_SPC.name()).equals(companyCd)) {
        }
        
        
        return true;
        
    }
    

    /**
     * 내용 : 환산단위당 단가
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 04. 02: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean conversionUnitPrice() throws Exception {

        boolean result = false;

        Map<String, String> searchMap = new HashMap<String, String>();

        List<Map> loglistMap = commonDao.getList("dataUpload.getUnitPriceBrand", searchMap);
        for (Map logMap : loglistMap) {
            
            String brandId = (String) logMap.get("BRAND_ID");
            
            setConversionUnitPrice(brandId);
        }
        
        return result;
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
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setConversionUnitPrice(String brandId) throws Exception {
        boolean result = false;

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("brandId", brandId);

        List<Map> listMap = commonDao.getList("dataUpload.getConversionUnitPrice", searchMap);

        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "SYSTEM");
            saveMap.put("CREATE_USER", "SYSTEM");

            commonDao.update("dataUpload.setMaterialsConUnitPrice", saveMap);
        }

        return result;
        
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
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setMaterialsSales() throws Exception {

        boolean result = false;

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("applyCompYn", "N");

        List<Map> loglistMap = commonDao.getList("dataUpload.getMaterialsExceluploadLog", searchMap);
        for (Map logMap : loglistMap) {
            
            String company = (String) logMap.get("COMPANY_CD");
            
            if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_BONFS.name()).equals(company)) {
                
                setBonFoodProduct(logMap);
                setBonFoodMaterialsSales(logMap);
                setBonFoodUnitPrice();
                
                result = true;
            } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_JANGGA.name()).equals(company)) {
                
                setJanggaFoodMaterialsSales(logMap);
                setJanggaUnitPrice();
                result = true;
            } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_HARAM.name()).equals(company)) {
                setHaramFoodMaterialsSales(logMap);
                setHaramUnitPrice();
                result = true;
            } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_SPC.name()).equals(company)) {
                //setSpcMaterialsSales(logMap);
                result = true;
            }
  
            Map<String, String> updateMap = new HashMap<String, String>();
            updateMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));
            updateMap.put("APPLY_COMP_YN", "Y");
            updateMap.put("UPDATE_USER", "BATCH");
            commonDao.update("dataUpload.updateMaterialsExceluploadLog", updateMap);
            
            //변환 단가 수정
            //setConversionUnitPrice(company);
        }
        
        return result;
    }
    
    /**
     * 내용 : 본푸드 단가 수정
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 04. 02: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setBonFoodUnitPrice() throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("companyCd", ((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_BONFS.name()));

        List<Map> listMap = commonDao.getList("dataUpload.getBonfoodUnitPrice", searchMap);

        for (Map saveMap : listMap) {
            
            saveMap.put("UPDATE_USER", "BATCH");

            commonDao.update("dataUpload.setCompanyUnitPrice", saveMap);
        }
        return true;
    }

    /**
     * 내용 : 장가 단가 수정
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 04. 02: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setJanggaUnitPrice() throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("companyCd", ((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_JANGGA.name()));

        List<Map> listMap = commonDao.getList("dataUpload.getJanggaUnitPrice", searchMap);

        for (Map saveMap : listMap) {
            
            saveMap.put("UPDATE_USER", "BATCH");

            commonDao.update("dataUpload.setCompanyUnitPrice", saveMap);
        }
        return true;
    }
    

    /**
     * 내용 : 하람 단가 수정
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 04. 02: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setHaramUnitPrice() throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("companyCd", ((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_HARAM.name()));

        List<Map> listMap = commonDao.getList("dataUpload.getHaramUnitPrice", searchMap);

        for (Map saveMap : listMap) {
            
            saveMap.put("UPDATE_USER", "BATCH");

            commonDao.update("dataUpload.setCompanyUnitPrice", saveMap);
        }
        return true;
    }
    /**
     * 내용 : 변환 단위당 단가 수정
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 28: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    /*
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setConversionUnitPrice1(String companyCd) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("companyCd", companyCd);

        List<Map> listMap = commonDao.getList("dataUpload.getConversionUnitPrice", searchMap);

        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "SYSTEM");
            saveMap.put("CREATE_USER", "SYSTEM");

            //commonDao.update("dataUpload.setCompanyConUnitPrice", saveMap);
            commonDao.update("dataUpload.setMaterialsConUnitPrice", saveMap);
        }
        return true;
    }
    */
    
    /**
     * 내용 : 식자재 매출 - 장가 데이타 입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 26: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setJanggaFoodMaterialsSales( Map logMap ) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));
        
        List<Map> listMap = commonDao.getList("dataUpload.getJanggaSales", searchMap);

        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "JANGGA");
            saveMap.put("CREATE_USER", "JANGGA");
            saveMap.put("COMPANY_CD", (String) logMap.get("COMPANY_CD"));
            saveMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));

            commonDao.insert("dataUpload.insertFoodMaterialsSales", saveMap);// 임시
        }
        return true;
    }

    /**
     * 내용 : 본푸드 상품 데이타 입력- 본푸드 상품에 없는 엑셀데이타 신규입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 12: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean setBonFoodProduct(Map logMap) throws Exception {

        List<Map> listMap = commonDao.getList("dataUpload.getBonfoodTempProduct", null);
        for (Map saveMap : listMap) {

            saveMap.put("CREATE_USER", "BATCH");
            saveMap.put("DEL_YN", "N");
            saveMap.put("FOOD_MATERIALS_UNIT", null);
            saveMap.put("CONVERSION_QUANTITY", null);
            saveMap.put("MATERIALS_CD", null);

            commonDao.insert("dataUpload.insertFoodCompanyProduct", saveMap);
        }

        return true;
    }

    /**
     * 내용 : 식자재 매출 - 본푸드 데이타 입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 26: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean setBonFoodMaterialsSales(Map logMap) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));

        List<Map> listMap = commonDao.getList("dataUpload.getBonfoodSales", searchMap);
        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "BONFS");
            saveMap.put("CREATE_USER", "BONFS");
            saveMap.put("COMPANY_CD", (String) logMap.get("COMPANY_CD"));
            saveMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));

            commonDao.insert("dataUpload.insertFoodMaterialsSales", saveMap);
        }

        return true;
    }

    /**
     * 내용 : 식자재 매출 - 하람 데이타 입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 27: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setHaramFoodMaterialsSales(Map logMap) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));
        List<Map> listMap = commonDao.getList("dataUpload.getHaramSales", searchMap);

        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "HARAM");
            saveMap.put("CREATE_USER", "HARAM");
            saveMap.put("COMPANY_CD", (String) logMap.get("COMPANY_CD"));
            saveMap.put("UPLOAD_LOG_ID", (String) logMap.get("UPLOAD_LOG_ID"));

            commonDao.insert("dataUpload.insertFoodMaterialsSales", saveMap);
        }
        return true;
    }

    /**
     * 내용 : 식자재 매출 - SPC 데이타 입력
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 02. 27: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public boolean setSpcMaterialsSales(String company) throws Exception {

        Map<String, String> searchMap = new HashMap<String, String>();
        List<Map> listMap = commonDao.getList("dataUpload.getSpcSales", searchMap);

        for (Map saveMap : listMap) {
            saveMap.put("UPDATE_USER", "SPC");
            saveMap.put("CREATE_USER", "SPC");

            commonDao.insert("dataUpload.mergeFoodMaterialsSales", saveMap);// 임시
        }
        return true;
    }

}
