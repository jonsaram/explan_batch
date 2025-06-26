package exdev.com.service;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import exdev.com.ExdevCommonAPI;
import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.service.ExdevBaseService;
import exdev.com.common.service.ExdevCommonService;
import exdev.com.common.vo.SessionVO;

@Service("ExcelService")
public class ExcelService  extends ExdevBaseService{
	
	
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @ResponseBody Map<String, Object> commonExcelUpload(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
		
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        int cnt = workbook.getNumberOfSheets();
        Map resultMap = null;
        for (int ii = 0; ii < cnt; ii++) {
    		resultMap = commonExcelUploadExec(file, session, ii);
    		resultMap.put("sheetNum", ii);
    		// error Check
    		String state = (String)resultMap.get("state");
    		if("E".equals(state)) break;
		}
		return resultMap;
	}
	@SuppressWarnings("unchecked")
	public @ResponseBody Map<String, Object> commonExcelUploadExec(@RequestParam("file") MultipartFile file, HttpSession session, int sheetNum) throws Exception {

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
	                
	        		tableName 	= cell1.toString();
	        		
	        		if(!ExdevCommonAPI.isValid(tableName)) {
	        			// 코드관리에서 Table Column정의가 없는 경우 오류 처리
	        			throw new Exception(sheetNum + "번째 Sheet의 Format이 형식에 맞지 않습니다.");
	        			
	        		}

	        		tableName	= commonDao.makeTableName(tableName);
	        		
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
	        		
	    			String orgTableName 	= "";
	    			String bkTableName 		= "";
	    			String orgBkTableName 	= "";
	    			String owner 			= (String)commonDao.getOwner();
	    			if(tableName.indexOf(".") > -1) {
	    				String [] spArr = tableName.split("\\.");
	    				bkTableName		= spArr[0] + ".EXBACKUP_" + spArr[1];
	    				orgBkTableName	= "EXBACKUP_" + spArr[1];
	    				owner			= spArr[0];
	    				orgTableName	= spArr[1];
	    			} else {
	    				bkTableName		= "EXBACKUP_" + tableName;
	    				orgBkTableName	= "EXBACKUP_" + tableName;
	    				orgTableName	= tableName;
	    			}
	        		
	        		optionMap.put("TABLE_NAME"			, tableName			);
	        		optionMap.put("ORG_TABLE_NAME"		, orgTableName		);
	        		optionMap.put("BK_TABLE_NAME"		, bkTableName		);
	        		optionMap.put("ORG_BK_TABLE_NAME"	, orgBkTableName	);
	        		optionMap.put("OWNER"				, owner				);
	        		optionMap.put("CLEAR_CHECK"			, clearCheck		);
	        		optionMap.put("DUPLE_PROCESS"		, dupleProcess		);
	        		optionMap.put("BACKUP_DATE"			, ExdevCommonAPI.getToday("yyyyMMddHHmmss"));
	        		optionMap.put("sessionVo", sessionVo);
	        		
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
		
    public Workbook download(Map<String, Object> requestBodyMap, HttpSession session) throws Exception {
    	
        Workbook workbook = new XSSFWorkbook();
        List<String> noSaveList 	= (List<String> ) 	requestBodyMap.get("noSave");
        List<String> dateTypeList 	= (List<String> ) 	requestBodyMap.get("dateType");
        List<String> numberTypeList	= (List<String> ) 	requestBodyMap.get("numberType");
        String checkRowStr 			= (String) 			requestBodyMap.get("checkedRow");
        Map downInfo 				= (Map) 			requestBodyMap.get("downInfo");
        Map requestParm 			= (Map) 			requestBodyMap.get("requestParm");

        List<String> primaryKeyList = (List<String>) 	requestParm.get("primaryKeyList");
        String tableName 			= (String) 			requestParm.get("tableName");
        String primaryKey 			= (primaryKeyList == null || primaryKeyList.isEmpty()) ? "1" : "1/" + String.join("/", primaryKeyList);
        String title 				= (String) downInfo.get("title");
        Sheet sheet 				= workbook.createSheet(title);
        
        sheet.setDefaultColumnWidth(28);

        // 스타일 정의
        CellStyle headerCellStyle	= getHeaderCellStyle(workbook);
        CellStyle bodyCellStyle2	= getBodyCellStyle(workbook, HorizontalAlignment.RIGHT);
        CellStyle tableNameCellStyle= getTableNameCellStyle(workbook,true);
        CellStyle optionCellStyle 	= getTableNameCellStyle(workbook,false);
        CellStyle saveNoUpCellStyle = getSaveNoCellStyle(workbook,"UP");
        CellStyle saveNoDwnCellStyle= getSaveNoCellStyle(workbook,"DOWN");
        CellStyle titleCellStyle 	= getTitleCellStyle(workbook);
        CellStyle blueHeaderStyle 	= getBlueHeaderCellStyle(workbook);
        CellStyle greenHeaderStyle 	= getGreenHeaderCellStyle(workbook);

        Map resultMap = commonService.requestQuery(requestBodyMap, session);
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) resultMap.get("data");

        if (checkRowStr != null && !checkRowStr.isEmpty()) {
            List<Map<String, Object>> dataListTemp = new ArrayList<>();
            String[] checkRows = checkRowStr.split(",");
            for (String rowIdx : checkRows) {
                int index = Integer.parseInt(rowIdx);
                if (index >= 0 && index < dataList.size()) {
                    Map<String, Object> tempRow = dataList.get(index);
                    dataListTemp.add(tempRow);
                }
            }
            dataList = dataListTemp;
        }
        
        String[] columnNames 	= ((List<String>) requestBodyMap.get("columnNames")).toArray(new String[0]);
        String[] columnOrders 	= ((List<String>) requestBodyMap.get("columnOrders")).toArray(new String[0]);
        int columnCount = columnNames.length;

        // 첫 번째 ROW
        Row firstRow = sheet.createRow(0);
        firstRow.setHeightInPoints(50);
        createMergedCell(sheet, firstRow, 0, columnCount - 1, "* Column에 대한 순서 변경 금지.", titleCellStyle);

        // 두 번째 ROW
        Row secondRow = sheet.createRow(1);
        secondRow.setHeightInPoints(35);
        createMergedCell(sheet, secondRow, 0, 0, "Table명"								, headerCellStyle);
        createMergedCell(sheet, secondRow, 1, 1, "중복 검사 Column 번호\nex) 1 or 1/2"	, headerCellStyle); // primaryKey.세팅.
        createMergedCell(sheet, secondRow, 2, 2, "초기화 여부\nex) Y or N"				, headerCellStyle);
        createMergedCell(sheet, secondRow, 3, 3, "중복 처리\nex) CHECK or OVERWRITE"	, headerCellStyle);
        createMergedCell(sheet, secondRow, 4, 4, "Backup 여부"							, headerCellStyle);

        // 세 번째 ROW
        Row thirdRow = sheet.createRow(2);
        thirdRow.setHeightInPoints(35);
        createMergedCell(sheet, thirdRow, 0, 0, tableName								, tableNameCellStyle);
        createMergedCell(sheet, thirdRow, 1, 1, primaryKey								, optionCellStyle);
        createMergedCell(sheet, thirdRow, 2, 2, "Y"										, optionCellStyle);
        createMergedCell(sheet, thirdRow, 3, 3, "CHECK"									, optionCellStyle);
        createMergedCell(sheet, thirdRow, 4, 4, "Y"										, optionCellStyle);

        // 네 번째 ROW
        int columnWidth = 0;
        Row headerRow = sheet.createRow(3);
        List noSaveIndex = new ArrayList();
        for (int i = 0; i < columnOrders.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            String cellValue = columnOrders[i];
            if(dateTypeList.contains(cellValue)) {
            	cellValue = cellValue+":D";
            }            
            headerCell.setCellValue(cellValue);
            CellStyle style = blueHeaderStyle;
            if(noSaveList.contains(cellValue)) {
            	style = saveNoUpCellStyle;
            	noSaveIndex.add(i);
            }

            headerCell.setCellStyle(style);
            columnWidth = 6000;
            sheet.setColumnWidth(i, columnWidth);
        }
        
        // 다섯 번째 ROW
        Row headerDataRow = sheet.createRow(4);
        for (int i = 0; i < columnNames.length; i++) {
            Cell headerCell = headerDataRow.createCell(i);
            headerCell.setCellValue(columnNames[i]);
            CellStyle style = greenHeaderStyle;
            if(noSaveIndex.contains(i)) {
            	style = saveNoDwnCellStyle;
            }
            headerCell.setCellStyle(style);
            sheet.setColumnWidth(i, columnWidth);
        }

        // 여섯 번째 ROW부터 데이터 입력
        int rowCount = 5;
        for (Map<String, Object> rowData : dataList) {
            Row dataRow = sheet.createRow(rowCount++);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = dataRow.createCell(i);
                Object value = rowData.get(columnOrders[i]);
                
                if (value != null) {
                    if(numberTypeList.contains(columnOrders[i])) {
                    	setNumberValue(workbook,cell,value);
                    }else {
                    	cell.setCellValue(value.toString());
                    	bodyCellStyle2	= getBodyCellStyle(workbook, HorizontalAlignment.RIGHT);
                    	cell.setCellStyle(bodyCellStyle2);    	
                    }
                }else {
                   	bodyCellStyle2	= getBodyCellStyle(workbook, HorizontalAlignment.RIGHT);
                	cell.setCellStyle(bodyCellStyle2);    	
                }
            }
        }

        return workbook;
    }
    
    CellStyle styleNumberValue = null;
	private void setNumberValue(Workbook workbook, Cell cell, Object value) {
    	
    	cell.setCellValue(((BigDecimal)value).doubleValue());
        if(styleNumberValue == null) styleNumberValue = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        BigDecimal bigDecimalValue = (BigDecimal) value;
        if (bigDecimalValue.scale() > 0) {
            // 소수점이 있으면 소수점 2자리까지 표시
        	styleNumberValue.setDataFormat(dataFormat.getFormat("#,##0.00"));
        } else {
            // 소수점이 없으면 정수형 서식
        	styleNumberValue.setDataFormat(dataFormat.getFormat("#,##0"));
        }
        styleNumberValue.setBorderLeft(BorderStyle.THIN);
        styleNumberValue.setBorderRight(BorderStyle.THIN);
        styleNumberValue.setBorderTop(BorderStyle.THIN);
        styleNumberValue.setBorderBottom(BorderStyle.THIN);
        cell.setCellStyle(styleNumberValue); // 셀 서식 적용
		
	}
	private void createMergedCell(Sheet sheet, Row row, int startCol, int endCol, String text, CellStyle style) {
	    Cell cell = row.createCell(startCol);
	    cell.setCellValue(text);
	    cell.setCellStyle(style);
	    if (startCol != endCol) {
	        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
	    }
	}
	
    // 파란 배경 스타일 함수
    private CellStyle getBlueHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // 연두 배경 스타일 함수
    private CellStyle getGreenHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(218, 242, 208), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // SaveNo
	private CellStyle getSaveNoCellStyle(Workbook workbook,String gubun) {
		
		Font font = workbook.createFont();
		CellStyle style = workbook.createCellStyle();
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Color color = null; 
		if( gubun.equals("UP") )color =new java.awt.Color(170, 174, 235);
		else color =new java.awt.Color(214, 216, 212);
        XSSFColor myColor = new XSSFColor(color, null); // #AAAEEB
	    ((XSSFCellStyle) style).setFillForegroundColor(myColor);
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);	
		return style;
	}
	
    // 회색 배경
	private CellStyle getTitleCellStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

	    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        return style;
    }

    // 빈 배경
	private CellStyle getTableNameCellStyle(Workbook workbook, boolean bold) {
		Font tableNameFont = workbook.createFont();
		if( bold )tableNameFont.setBold(true);
		CellStyle tableNameCellStyle = workbook.createCellStyle();
		tableNameCellStyle.setFont(tableNameFont);
		tableNameCellStyle.setAlignment(HorizontalAlignment.CENTER);
		tableNameCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		tableNameCellStyle.setFillPattern(FillPatternType.NO_FILL);        
		return tableNameCellStyle;
	}
	
	private CellStyle getBodyCellStyle(Workbook workbook, HorizontalAlignment hA) {
		CellStyle bodyCellStyle = workbook.createCellStyle();
        bodyCellStyle.setBorderLeft(BorderStyle.THIN);
        bodyCellStyle.setBorderRight(BorderStyle.THIN);
        bodyCellStyle.setBorderTop(BorderStyle.THIN);
        bodyCellStyle.setBorderBottom(BorderStyle.THIN);
        bodyCellStyle.setAlignment(hA); 		
        return bodyCellStyle;
	}

	private CellStyle getHeaderCellStyle(Workbook workbook) {
	   Font font = workbook.createFont();
	    
	    CellStyle style = workbook.createCellStyle();
	    style.setFont(font);
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    style.setWrapText(true); // 자동 줄 바꿈 활성화
		byte[] customRGB = new byte[]{(byte) 244, (byte) 245, (byte) 249};
		XSSFColor customColor = new XSSFColor(customRGB, new DefaultIndexedColorMap());
		
	    ((XSSFCellStyle) style).setFillForegroundColor(customColor);
	    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    	return style;
	}
	
	/**
	 * 특별 엑셀 업로드  
	 * @param file
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public Map namedExcelUpload(MultipartFile file,String uploadLogId, HttpSession session, String foodCompany)  throws Exception {
		
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        int cnt = workbook.getNumberOfSheets();
        Map resultMap = null;
        for (int ii = 0; ii < cnt; ii++) {
    		resultMap = namedExcelUploadExec(file, uploadLogId, session, ii,foodCompany);
    		resultMap.put("sheetNum", ii);
    		// error Check
    		String state = (String)resultMap.get("state");
    		if("E".equals(state)) break;
		}
		return resultMap;
	}

    /**
     * 내용 : 물류매출 엑셀업로드
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 12: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public @ResponseBody Map<String, Object> namedExcelUploadExec(@RequestParam("file") MultipartFile file, String uploadLogId, HttpSession session, int sheetNum, String foodCompany) throws Exception {

        SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sessionVo", sessionVo);
        resultMap.put("filename", file.getOriginalFilename());

        HashMap imap = new HashMap();      

        if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_BONFS.name()).equals(foodCompany)) {
            resultMap = ExcelUploadBonfs(file,session,sheetNum,uploadLogId);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_JANGGA.name()).equals(foodCompany)) {
            resultMap = ExcelUploadJangga(file,session,sheetNum,uploadLogId);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_HARAM.name()).equals(foodCompany)) {
            resultMap = ExcelUploadHaram(file,session,sheetNum,uploadLogId);
        } else if (((String) ExdevConstants.FOOD_MATERIALS_COMPANY.obong_SPC.name()).equals(foodCompany)) {
            // setSpcMaterialsSales(logMap);
            //result = true;
        }
        
        return resultMap;
        
    }

    /**
     * 내용 : 장가 엑셀업로드
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 12: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public Map<String, Object> ExcelUploadJangga(MultipartFile file, HttpSession session,int sheetNum,String uploadLogId) throws Exception {

        SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sessionVo", sessionVo);
        resultMap.put("filename", file.getOriginalFilename());

        int idx = 0;
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.iterator();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (rowIterator.hasNext()) {
                Row row = null;
                if(idx < 1) {// 첫번째 행까지 건너뛰기 
                    idx++;
                    row = rowIterator.next();
                    continue;
                }
                row = rowIterator.next();
                // 날짜 변환
                Cell dateCell = row.getCell(1); // 첫 번째 컬럼이 날짜
                
                LocalDate localDate = dateCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                String YYYYMMDD = localDate.format(formatter);
                String QUANTITY       = getStringCellValue(row.getCell(2));   // 매출수량
                String UNIT_PRICE     = getStringCellValue(row.getCell(3));   // 매출단가
                String SUPPLY_PRICE   = getStringCellValue(row.getCell(4));   // 매출액
                String STORE_CD       = getStringCellValue(row.getCell(5));   // 매출처코드

                Map<String, Object> saveMap = new HashMap<>();            
                saveMap.put("YYYYMMDD"      , YYYYMMDD);        
                saveMap.put("QUANTITY"      , QUANTITY);      
                saveMap.put("UNIT_PRICE"    , UNIT_PRICE);      
                saveMap.put("SUPPLY_PRICE"  , SUPPLY_PRICE);       
                saveMap.put("STORE_CD"      , STORE_CD); 
                saveMap.put("UPLOAD_LOG_ID" , uploadLogId);
                commonDao.update("dataUpload.excelInsertToJangga", saveMap);
            
                
            }
            return resultMap;
            
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg"     ,e.getMessage());
            resultMap.put("stopIdx" ,(idx - 3));
            resultMap.put("state","E");

            return resultMap;
        }
    }

    /**
     * 내용 : 하람 엑셀업로드
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 12: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public Map<String, Object> ExcelUploadHaram(MultipartFile file, HttpSession session,int sheetNum,String uploadLogId) throws Exception {

        SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sessionVo", sessionVo);
        resultMap.put("filename", file.getOriginalFilename());

        String fileName = file.getOriginalFilename();
        int idx = 0;
        Map<String, Object> cellMap = new LinkedHashMap<>();
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.iterator();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String unitPrice = "";
            while (rowIterator.hasNext()) {
                
                Row row = rowIterator.next();
                if(idx == 0) {
                    unitPrice = getStringCellValue(row.getCell(5));
                }else {
                    
                    Cell dateCell = row.getCell(0); // 첫 번째 컬럼이 날짜
                    String yyyymmdd = "";
                    LocalDate localDate = dateCell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    yyyymmdd = localDate.format(formatter);
                    
    
                    // 나머지 데이터 읽기
                    String quantity = getStringCellValue(row.getCell(3)); // 수량
                    String weight = getStringCellValue(row.getCell(4)); // 중량
                    String supplyPrice = getStringCellValue(row.getCell(5)).replace(",", ""); // 금액 (쉼표 제거)
                    String storeCd = getStringCellValue(row.getCell(6)); // 매출처코드
                    
                    System.out.println("yyyymmdd: " + yyyymmdd);
                    System.out.println("quantity: " + quantity);
                    System.out.println("weight: " + weight);
                    System.out.println("unitPrice: " + unitPrice);
                    System.out.println("storeCd: " + storeCd);
                    System.out.println("supplyPrice: " + supplyPrice);
                    
                    Map<String, Object> saveMap = new HashMap<>();            
                    saveMap.put("YYYYMMDD", yyyymmdd);
                    saveMap.put("QUANTITY", quantity);
                    saveMap.put("WEIGHT", weight);
                    saveMap.put("UNIT_PRICE", unitPrice);
                    saveMap.put("STORE_CD", storeCd);
                    saveMap.put("SUPPLY_PRICE", supplyPrice);
                    saveMap.put("UPLOAD_LOG_ID", uploadLogId);
                    
                    commonDao.update("dataUpload.excelInsertToHaram", saveMap);
                }
                idx++;
            }
            return resultMap;
            
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg"     ,e.getMessage());
            resultMap.put("stopIdx" ,(idx - 3));
            resultMap.put("state","E");

            return resultMap;
        }
    }

    /**
     * 내용 : 본푸드 엑셀업로드
     * 
     * @생 성 자 : 이응규
     * @생 성 일자 : 2025. 03. 12: 최초 생성
     * @수 정 자 :
     * @수 정 일자 :
     * @수 정 자
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    public Map<String, Object> ExcelUploadBonfs(MultipartFile file, HttpSession session,int sheetNum,String uploadLogId) throws Exception {

        SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sessionVo", sessionVo);
        resultMap.put("filename", file.getOriginalFilename());
        int idx = 0;

        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = null;
                if(idx < 2) {// 두번째 행까지 건너뛰기 
                    idx++;
                    row = rowIterator.next();
                    continue;
                }
                row = rowIterator.next();
                
                Map<String, Object> saveMap = new HashMap<>();           
                saveMap.put("STORE_CD"      , getStringCellValue(row.getCell(0)));    // 코드     
                saveMap.put("PRODUCT_CD"    , getStringCellValue(row.getCell(6)));    // 품목코드   
                saveMap.put("PRODUCT_NM"    , getStringCellValue(row.getCell(7)));    // 품명  
                saveMap.put("STANDARD"      , getStringCellValue(row.getCell(8)));    // 규격  
                saveMap.put("UNIT"          , getStringCellValue(row.getCell(9)));    // 단위         
                saveMap.put("YYYYMMDD"      , getStringCellValue(row.getCell(13)));   // 배송일자     
                saveMap.put("UNIT_PRICE"    , getStringCellValue(row.getCell(14)));   // 단가        
                saveMap.put("QUANTITY"      , getStringCellValue(row.getCell(15)));   // 배송량      
                saveMap.put("SUPPLY_PRICE"  , getStringCellValue(row.getCell(16)));   // 공급가      
                saveMap.put("SURTAX"        , getStringCellValue(row.getCell(17)));   // 부가세      
                saveMap.put("TOTAL_AMOUNT"  , getStringCellValue(row.getCell(18)));   // 합계금액
                saveMap.put("UPLOAD_LOG_ID" , uploadLogId);
                commonDao.update("dataUpload.excelInsertToBonfs", saveMap);
            
                
            }
            return resultMap;
            
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("msg"     ,e.getMessage());
            resultMap.put("stopIdx" ,(idx - 3));
            resultMap.put("state","E");

            return resultMap;
        }
    }

    // 셀 값 가져오기 (문자열 변환)
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (Math.floor(cell.getNumericCellValue()) == cell.getNumericCellValue()) {
                    // 정수인 경우 정수로 변환
                    return String.valueOf((long) cell.getNumericCellValue());
                } else {
                    // 소수점이 있는 경우 double 그대로 변환
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
	@SuppressWarnings("unchecked")
	public @ResponseBody Map<String, Object> namedExcelUploadExec_bak(@RequestParam("file") MultipartFile file, String uploadLogId, HttpSession session, int sheetNum) throws Exception {

		SessionVO sessionVo = (SessionVO) session.getAttribute(ExdevConstants.SESSION_ID);
	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("sessionVo", sessionVo);
	    resultMap.put("filename", file.getOriginalFilename());

	    String fileName = file.getOriginalFilename();
        int idx = 0;

        try {
	        Workbook workbook = WorkbookFactory.create(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(sheetNum);

	        List<Map<String, Object>> 	excelDataMapList 	= new ArrayList<>();
	        List<String> 				headerList 			= new ArrayList<>();
	        HashMap 					optionMap			= new HashMap();
	        String 						tableName 			= "";
	        String 						specialVal			= "";
	        if(fileName.startsWith("장가_엑셀업로드") || fileName.startsWith("하람_엑셀업로드")	) {idx++;}
	        for (Row row : sheet) {
	        	 Map<String, Object> cellMap = new LinkedHashMap<>();
	        	if(idx <= 1) {
	        		if( idx == 1) {
	        			
	        			if(fileName.startsWith("본푸드_엑셀업로드")	)tableName 	 = "TBL_EXP_BONFOOD_SALES_TEMP";
	        			if(fileName.startsWith("장가_엑셀업로드")	)tableName 	 = "TBL_EXP_JANGGA_SALES_TEMP";
	        			if(fileName.startsWith("하람_엑셀업로드")	)tableName   = "TBL_EXP_HARAM_SALES_TEMP";
	        			//if(fileName.startsWith("SPC_엑셀업로드")	)tableName 	 = "TBL_EXP_SPC_SALES_TEMP_1";
	        			
	        			
	        			if(fileName.startsWith("하람_엑셀업로드")	){
        				
							Cell cell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							DataFormatter formatter = new DataFormatter();
							FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // 수식 평가기
							
							if (cell.getCellType() == CellType.FORMULA) {
								// 수식이 있는 경우 평가하여 값을 가져오기
								specialVal = formatter.formatCellValue(cell, evaluator);
							} else {
							    //일반 값인 경우
								specialVal = formatter.formatCellValue(cell);
							}
							
	        			}
		        		
		        		optionMap.put("TABLE_NAME"		, tableName);
		        		optionMap.put("BACKUP_DATE"		, ExdevCommonAPI.getToday("yyyyMMddHHmmss"));
		        		optionMap.put("sessionVo", sessionVo);
		        		
		        		// Table에 해당하는 Column읽어온다.
		            	Map lm = (Map)commonDao.getObject("common.getExcelUploadColumnList", optionMap);
		            	
		            	if( lm == null) {
		        			// 코드관리에서 Table Column정의가 없는 경우 오류 처리
		        			throw new Exception("Table의 헤더가 Excel Upload Column 관리 Table에 등록되어 있지 않습니다.");
		            	} else {
		            		String columnList = (String)lm.get("COLUMN_LIST");
		            		String columnArray [] = columnList.split("/");
		            		
		            		for (String column : columnArray) {
			            		headerList.add(column);
							}
		            	}
	        		}
	        	} else if(idx >= 2) {

	        		int[] valColumnIdx= {};
	        		
        			if(fileName.startsWith("본푸드_엑셀업로드")	)valColumnIdx = new int[]{0, 6, 13, 14, 15, 16, 17, 18};
        			//if(fileName.startsWith("본푸드_엑셀업로드")  )valColumnIdx = new int[]{0, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18};
        			if(fileName.startsWith("장가_엑셀업로드")	)valColumnIdx = new int[]{1, 2, 3, 4, 5};
        			if(fileName.startsWith("하람_엑셀업로드")	)valColumnIdx = new int[]{0, 3, 4, 5, 5, 6};
        			//if(fileName.startsWith("SPC_엑셀업로드")	)valColumnIdx = new int[]{1, 4, 7, 8, 10, 14, 15, 17, 18, 19};
	        		
        			DataFormatter formatter = new DataFormatter();
        	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // 수식 평가기
        	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	        headerList.add("UPLOAD_LOG_ID");
        	        System.out.println("headerList.size() ==>"+headerList.size());
		            for (int i = 0; i < headerList.size(); i++) {
		            	

	                    System.out.println("i ==>"+i);
		            	String column = headerList.get(i);
		            	System.out.println("column ==>"+column);
		            	
		            	if( column.equals("UPLOAD_LOG_ID")) {
		            		 cellMap.put("UPLOAD_LOG_ID", uploadLogId);
		            	}else  if(fileName.startsWith("하람_엑셀업로드") && column.equals("UNIT_PRICE")) {
		            		cellMap.put("UNIT_PRICE", specialVal);
		            	}else if(fileName.startsWith("하람_엑셀업로드") && column.equals("YYYYMMDD")) {
	            			 Cell cell = row.getCell(valColumnIdx[i], Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	            			 Date date = cell.getDateCellValue();
	            			 cellMap.put(column, dateFormat.format(date));
		            	}
		            	else {
		            		Cell cell = row.getCell(valColumnIdx[i], Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		            		String cellValue = "";
		            		 if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
		                         Date date = cell.getDateCellValue();
		                         cellValue = dateFormat.format(date);
		            		 }
		            		 
		                    if (cell.getCellType() == CellType.FORMULA) {
		                        // 수식이 있는 경우 평가하여 값을 가져오기
		                        cellValue = formatter.formatCellValue(cell, evaluator);
		                        
		                    } else if (cell.getCellType() == CellType.NUMERIC) {
		                    	
		                    	cellValue = processNumeric(cell, dateFormat,formatter);

		                    } else {
		                        // 일반 값인 경우
		                        cellValue = formatter.formatCellValue(cell);
		                    }
		            		
		            		cellMap.put(column, cellValue);
		            	}
		            	
		            }
		           
	            	if( !"< 소 계 >".equals((String)cellMap.get("STORE_CD"))
	            			&&  !"< 총 계 >".equals((String)cellMap.get("STORE_CD"))){
	            		
	            		excelDataMapList.add(cellMap);
	            		
	            	}
	        	} 
	        	idx++;
	        }
            workbook.close();
            
            boolean bFirst = true;
            
            for (Map<String, Object> map : excelDataMapList) {

            	String selectClause = map.entrySet().stream()
            			.map(entry -> "'" + entry.getValue() + "' AS " + entry.getKey())
            			.collect(java.util.stream.Collectors.joining(", "));
            	
            	HashMap imap = new HashMap();
            	imap.put("tableName"	, tableName	);
            	imap.put("selectCaluse"	, selectClause);
            	imap.put("sessionVo"	, sessionVo	);
            	if( bFirst) {
            		commonDao.update("common.excelDeleteFromTable", imap);
            		bFirst=false;
            	}

            	commonDao.update("common.excelInsertToTable", imap);
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
	private String processNumeric(Cell cell,SimpleDateFormat dateFormat,DataFormatter formatter) {
    	
		String cellValue;
		
        short dataFormat = cell.getCellStyle().getDataFormat();
        
        if (DateUtil.isCellDateFormatted(cell)) {
        	
            // 날짜 형식이면 문자열로 변환
            Date date = cell.getDateCellValue();
            cellValue = dateFormat.format(date);
        } else {
        	
        	double numericValue = cell.getNumericCellValue();
        	if (numericValue >= 45259 && numericValue <= 49349) { // 2025년 ~ 2035년 사이
                
            	// 날짜 형식으로 오인될 가능성이 높음
                // 1900년 1월 1일부터 경과한 일수를 나타내므로 이 범위가 날짜 값인지 숫자(통화)인지 확인
            	
                Date date = DateUtil.getJavaDate(numericValue);
                if (numericValue > 45259) {  // 통화로 해석하려면 날짜와 차이를 분명히 해야 함
                    // 통화 서식으로 변환
                    if (dataFormat == 164 || dataFormat == 44 || dataFormat == 188) {
                        // 164 = 원화(₩), 44 = 일반 통화, 188 = 사용자 지정 통화
                        cellValue = formatter.formatCellValue(cell);
                    } else {
                        // 다른 숫자 형태로 처리
                        cellValue = formatter.formatCellValue(cell);
                    }
                } else {
                    // 날짜로 처리
                    cellValue = dateFormat.format(date);
                }
            	
            	
            } else {
            	// 통화 서식인지 확인 후 변환
        
                if (dataFormat == 164 || dataFormat == 44 || dataFormat == 188) { 
                    // 164 = 원화(₩), 44 = 일반 통화, 188 = 사용자 지정 통화
                    cellValue = formatter.formatCellValue(cell);
                } else {
                    // 일반 숫자 처리
                    cellValue = formatter.formatCellValue(cell);
                }
            }
        }
		
        return cellValue;
	}
	
	public int saveExcelUploadLog(Map<String, Object> map) {
		try {
			return commonDao.update("common.saveExcelUploadLog", map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;	
	}	
  }