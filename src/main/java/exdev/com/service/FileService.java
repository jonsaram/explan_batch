package exdev.com.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.service.ExdevBaseService;
import exdev.com.common.vo.SessionVO;
import exdev.com.util.FileUtil;

/** 
 * 파일저장 서비스
 * @생 성 자   : 이응규
 * @생 성 일자 : 2024. 01. 17
 * @수 정 자   : 
 * @수 정 일자 :
 * @수 정 자
 */
@Service("FileService")
public class FileService extends ExdevBaseService{
	
	@Autowired
	private ExdevCommonDao commonDao;
    
    //==== ASP.NET 파일전송 ====
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    
    /** 
     * 멀티 파일삭제 서비스
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 01. 18 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public Map fileDeleteMulti(HttpServletRequest request, String[] uuidArry) throws Exception {
	    
	    Map<String, String> returnMap 	= new HashMap<String, String>();
		String fileDirectoryPath 		= ExdevConstants.FILE_DIRECTORY_PATH;
	    
		String fileSavePath = (String)env.getProperty("file.savepath");		
		
		if(fileSavePath != null) fileDirectoryPath = fileSavePath;
	    
	    try {
            for(  String uuid:uuidArry ) {
                Map uuidMap = new HashMap();
                uuidMap.put("FILE_ID" , uuid  );
                List<Map> list = commonDao.getList("Filemng.getFile", uuidMap);
                
                int result = 0;
                
                for(Map fileMap : list) {
                    new File((String)fileMap.get("FILE_PATH")).delete();
                    result += commonDao.delete("Filemng.deleteFile", uuidMap);
                }
            }
        
            returnMap.put("msg",ExdevConstants.REQUEST_SUCCESS);
	    } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("msg",ExdevConstants.REQUEST_FAIL);
        }
        return returnMap;
    }

    /** 
     * 파일조회 서비스
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 02 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map> getfile(Map map) throws Exception {
        
        List<Map> list = commonDao.getList("Sample.getFile", map);
        return list;
    }
    
    /** 
     * 파일업로드 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 02. 02 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
	@SuppressWarnings("unchecked")
	public Map fileUploadMulti( HttpServletRequest request, List<MultipartFile> multiFileList, String  GRP_FILE_ID, String[] FILE_IDS, String  uploadPath, SessionVO sessionVo) throws Exception {
		
        Map returnMap = new HashMap();
		
		String fileDirectoryPath 		= ExdevConstants.FILE_DIRECTORY_PATH;
		
		String fileSavePath = (String)env.getProperty("file.savepath");		
		
		if(fileSavePath != null) fileDirectoryPath = fileSavePath;
		
        String path 		= fileDirectoryPath + File.separator + uploadPath;
        
		File fileCheck = new File(path);
		if(!fileCheck.exists()) fileCheck.mkdirs();
		
        List<Map> fileList = new ArrayList<>();
		
        String OWNER_CD 	   = request.getParameter("OWNER_CD"	);
        String CONVERT_TO_PDF  = request.getParameter("CONVERT_TO_PDF");
        
		for(int i = 0; i < multiFileList.size(); i++) {
			String ORG_FILE_NM 	= multiFileList.get(i).getOriginalFilename();
			long FILE_SIZE 		= multiFileList.get(i).getSize();
			String ext 			= ORG_FILE_NM.substring(ORG_FILE_NM.lastIndexOf("."));
			String FILE_ID 		= FILE_IDS[i];
			String STORED_FILE_NM = FILE_ID + ext;
	        String fullPath = path + File.separator + STORED_FILE_NM;
	        
			Map map = new HashMap<>();
			map.put("sessionVo"			, sessionVo		);
			map.put("GRP_FILE_ID"		, GRP_FILE_ID	);
			map.put("FILE_ID"			, FILE_ID		);
			map.put("ORG_FILE_NM"		, ORG_FILE_NM	);
			map.put("OWNER_CD"			, OWNER_CD		);
			map.put("STORED_FILE_NM"	, STORED_FILE_NM);
			map.put("FILE_PATH"			, fullPath		);
			map.put("FILE_TYPE"			, ext			);
			map.put("FILE_SIZE"			, Long.toString(FILE_SIZE));
			fileList.add(map);
		}
		int result = 0;
		// 파일업로드
		try {
			for(int i = 0; i < multiFileList.size(); i++) {
				
				Map insertMap = (Map)fileList.get(i);
				
				File uploadFile = new File((String)insertMap.get("FILE_PATH"));
				
				multiFileList.get(i).transferTo(uploadFile);
				
				/***************************************************************************/
				/* 테이블 입력    테이블 입력    테이블 입력    테이블 입력    테이블 입력    */
				result += commonDao.insert("Filemng.saveFileInfo", insertMap);
				/* 테이블 입력    테이블 입력    테이블 입력    테이블 입력    테이블 입력    */
				/***************************************************************************/

                /* PDF 파일 변환 서버로 전송,이응규, 2024-07-25 */
	            if( "Y".equals(CONVERT_TO_PDF) ) {
	                boolean reseult = sendFileToExternalServer( insertMap);
	                if( reseult ) {
	                    //업데이트
	                    insertMap.put("CONVERT_YN", "Y");
	                    commonDao.update("Filemng.updateFileInfo", insertMap);
	                }
	            }
	            /* PDF 파일 변환 서버로 전송,이응규, 2024-07-25 */
	            
			}
			if( result == multiFileList.size()) {
				returnMap.put("msg", ExdevConstants.REQUEST_SUCCESS);	
			}else {
			    /* 2024-10-15 이응규 멀티첨부파일 올렸을경우 하나라도 에러 발생했을경우 파일이 삭제되는 오류 */
			    /*
				for(int i = 0; i < multiFileList.size(); i++) {
					new File((String)fileList.get(i).get("FILE_PATH")).delete();
				}
				*/
				returnMap.put("msg", ExdevConstants.REQUEST_FAIL);
			}	
			
		} catch (Exception e) {
		    // 만약 업로드 실패하면 파일 삭제
			for(int i = 0; i < multiFileList.size(); i++) {
				new File((String)fileList.get(i).get("FILE_PATH")).delete();
			}
			e.printStackTrace();
			returnMap.put("msg", ExdevConstants.REQUEST_FAIL);
		}
		return returnMap;
	}

    /** 
     * PDF 파일 변환 서버로 파일 전송 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    public boolean sendFileToExternalServer( Map map ) throws Exception {
        
        String fileType = (String)map.get("FILE_TYPE");
        //변환 대상파일
        if( !(".docx".equals(fileType)||".doc".equals(fileType)||".xlsx".equals(fileType)||".xls".equals(fileType)||".pptx".equals(fileType)||".ppt".equals(fileType))) {
            return false;
        }
        
        String transforServerUrl = ExdevConstants.TRANSFOR_SERVER_URL;
        String transforServerUrlConvert = ExdevConstants.TRANSFOR_SERVER_URL_CONVERT;
        String serverUrl = transforServerUrl +"/" + transforServerUrlConvert;

        // PDF 파일 변환 서버가 살아있는지 확인
        boolean isServerAlive = isServerAlive(transforServerUrl);
        
        if( !isServerAlive) { return false;}
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // SSL 검증 무시
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAll, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        
        String path = (String)map.get("FILE_PATH");
        FileUtil.getDirectoryPath(path);
        // PDF 파일 변환 서버로 전송
        boolean returnVal = sendFile( serverUrl, new File( path), FileUtil.getDirectoryPath(path));
        return returnVal;
    }


    /** 
     * 서버상태 확인 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    public boolean isServerAlive(String urlStr) {
        
        try (CloseableHttpClient httpClient = createHttpClientWithNoSSL()) {
            HttpGet request = new HttpGet(urlStr);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode == 200;
            }
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Server is down or unreachable: " + e.getMessage());
            return false;
        }
    }

    /** 
     * PDF 파일 변환 서버로 파일 전송 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    private static CloseableHttpClient createHttpClientWithNoSSL() throws GeneralSecurityException {
        return HttpClientBuilder.create()
                .setSSLContext(new SSLContextBuilder()
                        .loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE)
                        .build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
    
    /** 
     * PDF 파일 변환 서버로부터 받은 파일에서 파일이름 조회 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    private String getFileNameFromHeaders(HttpHeaders headers) {
        String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (contentDisposition != null) {
            String fileName = null;
            if (contentDisposition.contains("filename*=")) {
                int startIdx = contentDisposition.indexOf("filename*=") + 10;
                String encodedFileName = contentDisposition.substring(startIdx);
                fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
                fileName = fileName.replace("UTF-8''", "");
            } else if (contentDisposition.contains("filename=")) {
                int startIdx = contentDisposition.indexOf("filename=") + 9;
                fileName = contentDisposition.substring(startIdx).replace("\"", "");
            }
            return fileName;
        }
        return "downloaded_file";
    }

    /** 
     * URL로 파일전송
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    public boolean sendFile(String serverUrl, File file, String filePath) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Customer", "explan");
        
        FileSystemResource fileResource = new FileSystemResource(file);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(serverUrl, HttpMethod.POST, requestEntity, byte[].class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            byte[] fileBytes = response.getBody();
            String fileName = getFileNameFromHeaders(response.getHeaders());
            try {
                saveFile(fileBytes, fileName,filePath);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
        
    }
    /** 
     * 파일저장 
     * @생 성 자   : 이응규
     * @생 성 일자 : 2024. 07. 25 : 최초 생성
     * @수 정 자   : 
     * @수 정 일자 :
     * @수 정 자   :
     */
    private void saveFile(byte[] fileBytes, String fileName, String filePath) throws IOException {
        String DOWNLOAD_DIR  = filePath;
        
        File downloadDir = new File(DOWNLOAD_DIR);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        Path path = Paths.get(DOWNLOAD_DIR + File.separator + fileName);
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(fileBytes);
        }
    }


}