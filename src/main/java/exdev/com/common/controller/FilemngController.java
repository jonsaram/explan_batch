package exdev.com.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.vo.SessionVO;
import exdev.com.service.FileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.vo.SessionVO;
import exdev.com.service.FileService;
import exdev.com.util.FileUtil;

/**
 * @author 위성열
 */
@Controller("FilemngController")
public class FilemngController {

	@Autowired
	private FileService fileService;
	
	@Autowired
	private ExdevCommonDao commonDao;

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@PostMapping("/multiFileUpload.do")
	public @ResponseBody Map<String, Object> multiFileUpload(@RequestParam("attach_file") List<MultipartFile> multiFileList,			
            HttpServletRequest request, HttpSession session)  throws Exception {
		
        SessionVO sessionVo = (SessionVO)session.getAttribute(ExdevConstants.SESSION_ID);
        
        
        Map<String, String> returnFileMap 			= new HashMap<String, String>();
        Map<String, Object> returnMap 				= new HashMap<String, Object>();
        
        String GRP_FILE_ID 	= request.getParameter("GRP_FILE_ID");
        String OWNER_CD 	= request.getParameter("OWNER_CD"	);
        String uploadPath	= ExdevConstants.FILE_UPLOAD_PATH + File.separator + OWNER_CD;
		String[] FILE_IDS	= request.getParameterValues("FILE_IDS");
		
		returnFileMap = fileService.fileUploadMulti( request, multiFileList, GRP_FILE_ID, FILE_IDS, uploadPath, sessionVo);
		
		if( ExdevConstants.REQUEST_SUCCESS.equals(returnFileMap.get("msg").toString())) {
			
		    returnMap.put("msg", "파일 업로드에 성공하였습니다.");
            
            List<String> list = new ArrayList<String>();
            for(int i =0; i < FILE_IDS.length; i++ ) {
                list.add(FILE_IDS[i]);
            }
            returnMap.put("list", list);
			
		}else{
            returnMap.put("msg", "파일 업로드에 실패하였습니다.");
            
		}
		
		return returnMap;
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("filedownload.do")
	public void filedownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    
	    String FILE_ID 	= (String)request.getParameter("FILE_ID");
	    
	    Map requestParm = new HashMap();
	    requestParm.put("FILE_ID", FILE_ID);
	    
	    Map fileInfo = (Map)commonDao.getObject("Filemng.getFileInfo", requestParm);
	    
	    String filePath = (String) fileInfo.get("FILE_PATH");
	    String orgFileName = (String) fileInfo.get("ORG_FILE_NM");
	    
	    File f = new File(filePath);
	    
	    // 한글 파일명 처리
	    String encodedFileName = URLEncoder.encode(orgFileName, "UTF-8").replaceAll("\\+", "%20");

	    // 다운로드 설정
	    response.setContentType("application/octet-stream");
	    response.setContentLength((int) f.length());
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\";");
	    response.setHeader("Content-Transfer-Encoding", "binary");

	    // 파일 다운로드 처리
	    OutputStream os = response.getOutputStream();
	    FileInputStream fis = new FileInputStream(f);
	    FileCopyUtils.copy(fis, os);
	    
	    fis.close();
	    os.close();
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping("previewFile.do")
    public ResponseEntity<Resource> previewFile(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
        try {
            String FILE_ID     = (String)request.getParameter("FILE_ID");
            String CONVERT_YN  = (String)request.getParameter("CONVERT_YN");
           
            
            Map     requestParm = new HashMap();
            requestParm.put("FILE_ID", FILE_ID);
            Map fileInfo = (Map)commonDao.getObject("Filemng.getFileInfo", requestParm);
            
            String filePathStr = "";
            
            if( "Y".equals(CONVERT_YN)) {
                filePathStr = FileUtil.convertFileType( (String)fileInfo.get("FILE_PATH"), ".pdf");
            }else {
                filePathStr = (String)fileInfo.get("FILE_PATH");
            }
            
            filePathStr = filePathStr.replace("\\", "/"); // 모든 `\`를 `/`로 변환
            Path filePath = Paths.get(filePathStr);
            Resource resource = new UrlResource(filePath.toUri());
            
            // 파일이 없을 때 에러 방지.
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }            
            
            // 파일의 MIME 타입 결정
            String contentType = Files.probeContentType(filePath);
            
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Content-Disposition 헤더를 inline으로 설정
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
}
