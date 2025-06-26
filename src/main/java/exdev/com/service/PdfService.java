package exdev.com.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;

import javax.servlet.http.HttpSession;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import exdev.com.ExdevCommonAPI;
import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.service.ExdevBaseService;
import exdev.com.common.service.ExdevCommonService;
import exdev.com.common.vo.SessionVO;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

@Service("PdfService")
public class PdfService extends ExdevBaseService{
	
	
	@Autowired
	private ExdevCommonService commonService;

	@Autowired
	private ExdevCommonDao commonDao;
	
	
	public @ResponseBody Map<String, Object> upload(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {

		SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("sessionVo", sessionVo);
	    resultMap.put("filename", file.getOriginalFilename());

	    try {
	        Workbook workbook = WorkbookFactory.create(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(0);
	        
	        List<Map<String, Object>> excelDataMapList = new ArrayList<>();
	        List<String> headerList = new ArrayList<>();
	        boolean headerRow = true;
	        
	        for (Row row : sheet) {
	            Map<String, Object> cellMap = new LinkedHashMap<>();
	            
	            for (int i = 0; i < row.getLastCellNum(); i++) {
	                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                if (headerRow) {
	                    headerList.add(cell.toString());
	                } else {
	                    cellMap.put(headerList.get(i), cell.toString());
	                }
	            }
	            
	            if (!headerRow) {
	                excelDataMapList.add(cellMap);
	            }
	            headerRow = false;
	        }

            workbook.close();

            Gson gson = new Gson();
            String json = gson.toJson(excelDataMapList);
            resultMap.put("msg","");
    		resultMap.put("data",json);
            resultMap.put("state","S");
    		
            return resultMap;
            
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg","");
            resultMap.put("state","");

            return resultMap;
        }
	}
	/**
	 * 위성열 
	 * 20240308
	 * @param file
	 * @param session
	 * @return
	 * @throws Exception
	 */
    private String targetText;
    private List<String[]> insertData;
    private boolean found = false;
    private static final String[] excludeWords = {"재무제표","COPYRIGHT","조회일","감사의견","재무상태","손익계산서"};
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @ResponseBody Map<String, Object> commonPdfUpload(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
		SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
    	List<String> firstWords = new ArrayList<String>();
    	List<String[]> insertData = new ArrayList<String[]>();
    	boolean capture = false;
    	boolean excludeStr = true;
        try {
        	String buyerName = file.getOriginalFilename();
        	InputStream inputStream = file.getInputStream();
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream));
            int numberOfPages = pdfDoc.getNumberOfPages();

            // 첫페이지 표지 제외하고 시작[재무상태표]
            for (int i = 2; i <= numberOfPages; i++) {
                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
                //System.out.println(text);
                // 페이지마다 1라인씩 처리
                String[] lines = text.split("\\n+");
                for (String line : lines) {
                	String[] words = line.split("\\s+");
                	if("재무상태표".equals(words[0])) {
                		capture = true;
                	}else if("손익계산서".equals(words[0])) {
                		capture = false; 
                	}
                	if(capture) {
						for(int j=0; j<excludeWords.length; j++) {
							if(words[0].indexOf(excludeWords[j])>-1) {
								excludeStr = false;
							}
						}
						if(excludeStr) {
							firstWords.add(words[0]);
							insertData.add(words);
						}
						excludeStr = true;
                	}
                }
            }
            
        	PDDocument document = PDDocument.load(file.getInputStream());
        	List<String[]> insertDataList = new ArrayList<String[]>();
            for(int i=0; i<firstWords.size(); i++) {
            	try {
	            	PDFTextPositionFinder stripper = null;
	                stripper = new PDFTextPositionFinder(firstWords.get(i), insertData);
	                stripper.setSortByPosition(true);
	                stripper.getText(document);
	                insertDataList.add(stripper.getReturnData());
	                System.out.println("==size===>"+insertDataList.size());
            	}catch(Exception e) {
            		System.out.println("==error===>"+e.getMessage());
            	}
            }
            document.close();
            
            if(insertDataList.size()>0) {
            	Map deletePdfMap = new HashMap();
            	deletePdfMap.put("BUYER_ID"			, sessionVo.getBuyerId());
            	deletePdfMap.put("ACCOUNT_GUBUN"	, "01");
            	commonDao.update("common.deleteFinPdfTemp"	, deletePdfMap);
	            for (String[] insertPdfData : insertDataList) {
	            	try {
	            		if(insertPdfData != null) {
			            	Map insertPdfMap = new HashMap();
			            	insertPdfMap.put("BUYER_ID"			, sessionVo.getBuyerId());
			            	insertPdfMap.put("ACCOUNT_GUBUN"	, "01");
			            	insertPdfMap.put("INDENT_DEGREE"	, insertPdfData[0]);	            	
			            	insertPdfMap.put("ACCOUNT_NAME"		, insertPdfData[1]);
			            	insertPdfMap.put("YEAR_DATA01"		, insertPdfData[2]);
			            	insertPdfMap.put("YEAR_DATA02"		, insertPdfData[3]);
			            	insertPdfMap.put("YEAR_DATA03"		, insertPdfData[4]);	            	
			            	insertPdfMap.put("sessionVo"		, sessionVo);
			            	commonDao.update("common.insertFinPdfTemp"	, insertPdfMap);
			            	System.out.println("==insertPdfData===>"+insertPdfData[0]+"/"
			            			+insertPdfData[1]+"/"+insertPdfData[2]+"/"+insertPdfData[3]+"/"
			            			+insertPdfData[4]);
	            		}
	            	}catch(Exception e) {
	            		System.out.println("==error===>"+insertPdfData.length+"/"+e.getMessage());
	            	}
	            }
            }
            
        	firstWords = new ArrayList<String>();
        	insertData = new ArrayList<String[]>();
            capture = false;
            excludeStr = true;
            // 첫페이지 표지 제외하고 시작[손익계산서]
            for (int i = 2; i <= numberOfPages; i++) {
                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
                System.out.println(text);
                // 페이지마다 1라인씩 처리
                String[] lines = text.split("\\n+");
                for (String line : lines) {
                	String[] words = line.split("\\s+");
                	if("손익계산서".equals(words[0])) {
                		capture = true;
                	}else if("현금흐름표".equals(words[0])) {
                		capture = false; 
                	}
                	if(capture) {
						for(int j=0; j<excludeWords.length; j++) {
							if(words[0].indexOf(excludeWords[j])>-1) {
								excludeStr = false;
							}
						}
						if(excludeStr) {
							firstWords.add(words[0]);
							insertData.add(words);
						}
						excludeStr = true;
                	}
                }
            }
            
        	document = PDDocument.load(file.getInputStream());
        	insertDataList = new ArrayList<String[]>();
            for(int i=0; i<firstWords.size(); i++) {
            	PDFTextPositionFinder stripper = null;
                stripper = new PDFTextPositionFinder(firstWords.get(i), insertData);
                stripper.setSortByPosition(true);
                stripper.getText(document);
                insertDataList.add(stripper.getReturnData());
            }
            document.close();
            
            if(insertDataList.size()>0) {
            	Map deletePdfMap = new HashMap();
            	deletePdfMap.put("BUYER_ID"			, sessionVo.getBuyerId());
            	deletePdfMap.put("ACCOUNT_GUBUN"	, "02");
            	commonDao.update("common.deleteFinPdfTemp"	, deletePdfMap);
	            for (String[] insertPdfData : insertDataList) {
	            	Map insertPdfMap = new HashMap();
	            	insertPdfMap.put("BUYER_ID"			, sessionVo.getBuyerId());
	            	insertPdfMap.put("ACCOUNT_GUBUN"	, "02");
	            	insertPdfMap.put("INDENT_DEGREE"	, insertPdfData[0]);	            	
	            	insertPdfMap.put("ACCOUNT_NAME"		, insertPdfData[1]);
	            	insertPdfMap.put("YEAR_DATA01"		, insertPdfData[2]);
	            	insertPdfMap.put("YEAR_DATA02"		, insertPdfData[3]);
	            	insertPdfMap.put("YEAR_DATA03"		, insertPdfData[4]);	            	
	            	insertPdfMap.put("sessionVo"		, sessionVo);
	            	commonDao.update("common.insertFinPdfTemp"	, insertPdfMap);
	            }
            }
            
            pdfDoc.close();
            

            resultMap.put("msg","");
            resultMap.put("state","S");
    		return resultMap;
        } catch (Exception e) {        	
            e.printStackTrace();
            resultMap.put("msg",e.getMessage());
            resultMap.put("state","E");
    		return resultMap;
        }
	}
	@SuppressWarnings("unchecked")
	public @ResponseBody Map<String, Object> commonPdfUploadExec(@RequestParam("file") MultipartFile file, HttpSession session, int sheetNum) throws Exception {

		SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("sessionVo", sessionVo);
	    resultMap.put("filename", file.getOriginalFilename());

        int idx = 0;

        try {
	        Workbook workbook = WorkbookFactory.create(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(sheetNum);

	        List<Map<String, Object>> 	excelDataMapList 	= new ArrayList<>();
	        List<String> 				headerList 			= new ArrayList<>();
	        HashMap 					optionMap			= new HashMap();
	        String 						tableName 			= "";
	        String 						prmKeyNumStr		= "";
	        String 						prmKeyNumAttr	[]	= null;
	        String 						prmKeyAttr		[]	= null;
	        String 						clearCheck 			= "";
	        String 						dupleProcess		= "";
	        String 						backupYn			= "";
	        
	        for (Row row : sheet) {
	        	
	        	// 첫행,두번째행은 설명으로 Skip 
	        	if(idx <= 1) {
		        	// 첫Cell이 skip이면 Sheet 종료  
	        		if(idx == 0) {
		                Cell cell1 	 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		        		String sheetCommand = cell1.toString();
		        		if("skip".equals(sheetCommand)) {
		                    resultMap.put("msg","");
		            		resultMap.put("data","skip");
		                    resultMap.put("state","S");
		                    return resultMap;
		        		}
	        		}
	        	// 세번째행은 Table Name 읽어서 CODE에서 Column 정보 가져옴 및 기타 옵션 가져옴
	        	} else if(idx == 2) {
	                Cell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                Cell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                Cell cell3 = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                Cell cell4 = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                Cell cell5 = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                
	        		tableName 	 = cell1.toString();
	        		if(!ExdevCommonAPI.isValid(tableName)) {
	        			// 코드관리에서 Table Column정의가 없는 경우 오류 처리
	        			throw new Exception(sheetNum + "번째 Sheet의 Format이 형식에 맞지 않습니다.");
	        			
	        		}
	        		
	        		prmKeyNumStr = cell2.toString();
	        		if(ExdevCommonAPI.isValid(prmKeyNumStr)) {
	        			prmKeyNumAttr 	= prmKeyNumStr.split("/");
	        			prmKeyAttr		= new String[prmKeyNumAttr.length];
	            		for (int i=0;i<prmKeyNumAttr.length;i++) {
	            			prmKeyNumAttr[i] = prmKeyNumAttr[i].replaceAll(".0", ""); 
						}
	        		}
	        		clearCheck 	 = cell3.toString();
	        		dupleProcess = cell4.toString();
	        		backupYn	 = cell5.toString();
	        		
	    			String exTableName 		= "";
	    			String orgExTableName 	= "";
	    			if(tableName.indexOf(".") > -1) {
	    				String [] spArr = tableName.split(".");
	    				exTableName		= spArr[0] + ".EXBACKUP_" + spArr[1];
	    				orgExTableName	= "EXBACKUP_" + spArr[1];
	    			} else {
	    				exTableName		= "EXBACKUP_" + tableName;
	    				orgExTableName	= "EXBACKUP_" + tableName;
	    			}
	        		
	        		optionMap.put("TABLE_NAME"			, tableName);
	        		optionMap.put("BK_TABLE_NAME"		, exTableName);
	        		optionMap.put("ORG_BK_TABLE_NAME"	, orgExTableName);
	        		optionMap.put("CLEAR_CHECK"			, clearCheck);
	        		optionMap.put("DUPLE_PROCESS"		, dupleProcess);
	        		optionMap.put("BACKUP_DATE"			, ExdevCommonAPI.getToday("yyyyMMddHHmmss"));
	        		optionMap.put("sessionVo"			, sessionVo);
	        		
	        		// Table에 해당하는 Column읽어온다.
	            	Map lm = (Map)commonDao.getObject("common.getExcelUploadColumnList", optionMap);
	            	
	            	if( lm == null) {
	        			// 코드관리에서 Table Column정의가 없는 경우 오류 처리
	        			throw new Exception("Table의 헤더가 Excel Upload Column 관리 Table에 등록되어 있지 않습니다.\n\n[3,A] Cell의 Table명이 Excel Upload관리 메뉴에 등록 되어 있어야 합니다.");
	            	} else {
	            		String columnList = (String)lm.get("COLUMN_LIST");
	            		String columnArray [] = columnList.split("/");
	            		int cidx = 1;
	            		int keyIdx = 0;
	            		for (String column : columnArray) {
		            		headerList.add(column);
		            		for (String num : prmKeyNumAttr) {
								if((cidx + "").equals(num)) {
									//엑셀에서 지정한 Primary Key를 구한다.
									prmKeyAttr[keyIdx++] = column;
									break;
								}
							}
		            		cidx++;
						}
	            	}
	            // 네번째 행은 Header 처리 및 옵션 처리
	        	} else if (idx == 3) {
	        		int cellcnt = row.getLastCellNum();
	        		int headerCnt = headerList.size();
	        		if(cellcnt < headerCnt) {
	        			// 컬럼 개수가 일치하지 않아 오류처리
	        			throw new Exception("Column개수가 일치하지 않습니다.");
	        		}

	        		boolean check = true;
	        		for (int ii=0; ii < headerCnt;ii++ ) {
	        			String rgColumn = headerList.get(ii);
	        			Cell cell = row.getCell(ii, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	        			String rowColumn = cell.toString();
	        			if(!rgColumn.equals(rowColumn)) {
	        				check = false;
	        				break;
	        			}
	        		}
	        		if(!check) {
		        		// 등록된 Column과 일치하지 않는 경우
	        			throw new Exception("등록된 Column Name과 일치하지 않습니다.");
	        		}
	        		
	        		// 기존 Data 지우고 새로 upload인경우 처리
	        		if("Y".equals(clearCheck)) {

	        			// Backup 옵션 확인
	        			if(!"N".equals(backupYn)) {
	        				// 기존 Data Backukp 후 Clear함
	        				List tabCntList = (List)commonDao.getList("common.existTableName", optionMap);
	        				Integer tabCnt = tabCntList.size();
	        				if(tabCnt == 1) {
	        					// Backup Table이 이미 존재하는 경우
	        					commonDao.update("common.insertBackupTable"	, optionMap);
	        					optionMap.put("BK_TYPE", "INSERT");
	        					commonDao.update("common.insertBackupLog"	, optionMap);
	        				} else {
	        					// Backup Table이 없는 경우
	        					commonDao.update("common.createBackupTable"	, optionMap);
	        					optionMap.put("BK_TYPE", "CREATE");
	        					commonDao.update("common.insertBackupLog"	, optionMap);
	        				}
		        			// 백업 완료
	        			}
	        			
	        			// 기존 Data를 지운다.
        				commonDao.update("common.deleteTable"				, optionMap);
	        		}
	        	// 다섯번째 행은 컬럼 Comment
	        	} else if (idx == 4) {
	        		
		        // 여섯번째 행부터 Data
	            } else if (idx > 4) {

		            Map<String, Object> cellMap = new LinkedHashMap<>();

		            for (int i = 0; i < headerList.size(); i++) {
		                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		                
		                String cellValue = cell.toString();
		                
		                String column = headerList.get(i);
	            		String [] columnArry = column.split(":");

	            		if(columnArry.length > 1 && "D".equals(columnArry[1])) {
                            
	            			Date date = cell.getDateCellValue();
	            			if(date == null) {
	            				cellValue = "";
	            			} else {
	                            // yyyy-MM-dd 형식으로 변환
	                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                            String formattedDate = sdf.format(date);
	                            cellValue = formattedDate;
	            			}
	            		} else if(cell.getCellType() == CellType.NUMERIC) {
	                		
	            			cell.setCellType(CellType.STRING);
	            			
	            			cellValue = cell.getStringCellValue();
	            			
	            			Double 	dcellValue 	= Double.parseDouble(cellValue);
	                		Long 	lcellValue 	= Math.round(dcellValue);
	                		
	                		String compStr = lcellValue + ".0";
	                		if(compStr.equals(cellValue.toString())) {
	                			cellValue = String.valueOf(lcellValue);
	                		}
	                		
	            		}
	                    cellMap.put(headerList.get(i), cellValue);
		            }
		            
	                excelDataMapList.add(cellMap);
	            }
	        	idx++;
	        }
            workbook.close();
            
            // 중복 Check
            String dupleNum = checkExcelDataValid(excelDataMapList, prmKeyAttr);
            if(dupleNum != null) {
            	resultMap.put("dupleNum", dupleNum);
    			resultMap.put("errorType", "excel_duple");
            	throw new Exception("Excel Data에 중복 Data가 있습니다.");
            }
            
            for (Map<String, Object> map : excelDataMapList) {
            	List<Map> setInfoList 	= new ArrayList<Map>();
            	List<Map> setUpdateList = new ArrayList<Map>();
            	for (String column : headerList) {
            		HashMap infoMap = new HashMap(); 
            		
            		String [] columnArry = column.split(":");
            		String columnNm 	= columnArry[0];
            		String columnType	= "X";
            		if(columnArry.length > 1) columnType = columnArry[1];
            		
            		String value = (String)map.get(column);
            		if(value == null) value = "";
            		
            		infoMap.put("header"		, columnNm);
            		infoMap.put("columnType"	, columnType);
            		infoMap.put("data"			, value);
            		setInfoList.add(infoMap);
            		
            		boolean keyCheck = false;
            		for (String key : prmKeyAttr) {
						if(key.equals(column)) keyCheck = true;
					}
            		if(!keyCheck) {
            			setUpdateList.add(infoMap);
            		}
				}
            	HashMap imap = new HashMap();
            	imap.put("tableName"	, tableName	);
            	imap.put("setInfoList"	, setInfoList);
            	imap.put("prmKeyAttr"	, prmKeyAttr);
            	imap.put("setUpdateList", setUpdateList);
            	imap.put("sessionVo"	, sessionVo	);
            	
            	if("OVERWRITE".equals(dupleProcess)) {
            		// 덮어쓰기
                	commonDao.update("common.excelOverwriteToTable", imap);
            	} else {
            		// Primary Key 중복인 경우 Error리턴
            		commonDao.update("common.excelUploadToTable", imap);
            	}
            	
            	
			}
            Gson gson = new Gson();
            String json = gson.toJson(excelDataMapList);
            resultMap.put("msg","");
    		resultMap.put("data",json);
            resultMap.put("state","S");
    		
            return resultMap;
            
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg"		,e.getMessage());
            resultMap.put("stopIdx"	,(idx - 3));
            resultMap.put("state","E");

            return resultMap;
        }
	}
	
	private String checkExcelDataValid(List<Map<String, Object>> excelDataMapList, String [] prmKeyAttr) {
		HashMap<String, String> keyList = new HashMap<String, String>();
		String rdata = null;
		int idx = 5;
        for (Map<String, Object> map : excelDataMapList) {
        	
        	String pk = "";
    		for (String key : prmKeyAttr) {
    			String addKey = (String)map.get(key);
    			pk += addKey;
			}
    		String dupleNum = keyList.get(pk);
    		if(dupleNum != null) {
    			rdata = dupleNum + "/" + idx;
    			break;
    		}
    		keyList.put(pk, idx + "");
    		idx++;
        }
		return rdata;
	}
	    
	private String getCellValue(String colName, Object value) {

        if (value instanceof BigDecimal) {
            value = String.valueOf(((BigDecimal) value).intValue());
        } else if (value instanceof Double) {
            value = String.valueOf(((Double) value).intValue());
        }
        return (String)value;
        
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @ResponseBody Map<String, Object> processFinAnalStatus(Map<String, Object> requestMap) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
        	commonDao.update("common.deleteFinAnalStatus"	, requestMap);
        	commonDao.update("common.insertFinAnalStatus"	, requestMap);
        	// 데이터 이관후 TEMP 정보삭제
        	commonDao.update("common.deleteFinPdfTemp"	, requestMap);

        	resultMap.put("msg","");
            resultMap.put("state","S");
    		return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg",e.getMessage());
            resultMap.put("state","E");
    		return resultMap;
        }
	}
 }