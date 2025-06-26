var G_VAL = {
	 COMMON_CD_MAP 	: {}
	,BRAND_CD_MAP 	: {}
	,session		: { storeList : {} }
	,current		: {}
	,PAGE_AUTH_MAP	: {}
	,windowSizeType	: "B"
}

var C_COM = {
	 mousePos 	: { x : 0, y : 0 }
	,lateFn		: {}
	,keypressListenerCallbackFn : {}
	,KEY_CD	: {
		 "ENTER" 	: 13
		,"DEL"		: 46
	 }
	,sessionConfirmCheck : ""
	,_DEFAULT_FIX : 0	// 소수점 기본 자리수
	 // Session저장
	,init : function() {
		$(window).bind("mousedown", function(e){
			C_COM.mousePos.x = e.pageX;
			C_COM.mousePos.y = e.pageY;
		});
		// 키 입력에 대한 Event 처리
		$(window).bind("keyup",function() {
			var pageId 	= C_PM.getCurrentPageId();
			var cFn		= C_COM.keypressListenerCallbackFn[pageId];
			if(typeof cFn == "function") cFn(event.keyCode);
		});
		
		// 공통 코드 읽어 오기
		C_COM.initCommonCode();
		
		C_HELP.init();
		// showAuth 관리자만 보여지는 경우를 Setting		
		if(G_VAL.session.USER_TYPE == "ADMIN") 	G_VAL.session.showAuth = 'block';
		else									G_VAL.session.showAuth = 'none' ;
	 }
	,initCommonCode() {
		// 공통 코드 읽어 오기
        let parm = {
             queryId        : "common.getCommonCodeList"
            ,requestParm    : {}
        }
        C_COM.requestQuery(parm, function(resultData) {
			var commonCodeMap = {}

			$.each(resultData.data, function() {
				if(isEmpty(commonCodeMap[this.GRP_CODE_ID])) {
					commonCodeMap[this.GRP_CODE_ID] = {
						 codeList 	: []
						,codeMap	: {}			
					}
				}
				commonCodeMap[this.GRP_CODE_ID].codeList.push([this.CODE_ID, this.CODE_NM]);
				commonCodeMap[this.GRP_CODE_ID].codeMap[this.CODE_ID] = this;
			});
			
			G_VAL.COMMON_CD_MAP = commonCodeMap;
        });


		// Brand 코드 읽어 오기
        parm = {
             queryId        : "common.getBrandCodeList"
            ,requestParm    : { BRAND_ID : G_VAL.session.BRAND_ID }
        }
        C_COM.requestQuery(parm, function(resultData) {
			var brandCodeMap = {}
			$.each(resultData.data, function() {
				let key = `${this.BRAND_ID}_${this.GRP_CODE_ID}`;
				if(isEmpty(brandCodeMap[key])) {
					brandCodeMap[key] = {
						 codeList 	: []
						,codeMap	: {}			
					}
				}
				brandCodeMap[key].codeList.push([this.CODE_ID, this.CODE_NM]);
				brandCodeMap[key].codeMap[this.CODE_ID] = this;
			});
			G_VAL.BRAND_CD_MAP = brandCodeMap;
        });      

      
	 }
	// 선택한 Table에서 Code를 만들어 낸다.
	,makeCodeListFromTable : function(parm) {
		let searchWhereList = [];
		let searchWhereMap = parm.filterMap;
		if(isValid(searchWhereMap)) {
			$.each(searchWhereMap, function(key, obj) {
				let val = obj;
				if(typeof obj == "function") {
					val = obj();
				}
				if(isValid(val)) {
					let map = {}
					if(Object.prototype.toString.call(val) == '[object Array]') {
						let vals = "(";
						$.each(val, function(idx) {
							if(idx == 0) 	vals = `${vals}  '${this.val}'`;
							else			vals = `${vals}, '${this.val}'`;
						});
						vals = `${vals} )`;
						map = { column : key, value : vals, oper : "IN" }
					} else {
						map = { column : key, value : `'${val}'` , oper : "=" }
					}
					searchWhereList.push(map);
				}
			});
		}
		let rparm = {
			 queryId 		: "common.getTableData"
			,requestParm	: {
				parm : {
					 tableName 			: parm.tableName
					,columnList 		: [{COLUMN_ID : parm.valueColumn}, {COLUMN_ID : parm.nameColumn}]
					,orderColumnList	: parm.orderColumnList
					,searchWhereList	: searchWhereList
				}
			}
		}
		let resultData = C_COM.requestQuery(rparm);
		if(resultData.state == "S") {
        	let selectList = C_COM.makeCodeList(resultData.data, parm.valueColumn, parm.nameColumn);
        	return selectList;
		} else {
			return [];
		}
	 }

	/*
		// 일반적인 사용
		let tparm = {
			 tableName 			: "TBL_EXP_MENU"
			,columnListStr		: "MENU_ID, MENU_NM, PAGE_ID"
			,primaryColumnList	: ["MENU_ID"]
			,filterMap			: {MENU_DEPTH : 1}
			,orderColumnList	: ["MENU_ID"]
		} 
		const menuDataSet = C_COM.makeDataSetFromTable(tparm);
		
		-------------------------------------------------------------------------------------------------
		
		// filterByPrimaryData Data안의 primaryColumnList를 필터로 사용하여 Data 가져오는 경우		
		let tparm = {
			"tableName"				: "TBL_EXP_HELP_MNG",
			"columnListStr"			: "HELP_ID, HELP_TYPE",
			"primaryColumnList"		: ["HELP_ID"],
			"filterByPrimaryData"	: {
				"HELP_ID"		: "HELP_742794437121",
				"MENU_ID"		: "MENU101200",
				"HELP_TYPE"		: "MENU",
				"TEMPLATE_ID"	: "main_dashboard_corCertification",
				"HELP_CONTENT"	: ""
			},
			"orderColumnList"		: ["HELP_ID"]
		}
		const menuDataSet = C_COM.makeDataSetFromTable(tparm);
	*/	
	,makeDataSetFromTable : function(parm) {
		let searchWhereMap 		= parm.filterMap			;
		let primaryColumnList	= parm.primaryColumnList	;
		let orderColumnList		= parm.orderColumnList		;
		let columnListStr		= parm.columnListStr		;
		let filterByPrimaryData	= parm.filterByPrimaryData	;
		
		if(isEmpty(searchWhereMap)) searchWhereMap = parm.searchWhereMap;
		if(isEmpty(columnListStr)) {
			if(isEmpty(parm.columnMap)) {
				C_POP.alert('columnListStr 값이 필요합니다.');				
				return;	
			} else {
				columnListStr = "";
				$.each(parm.columnMap, function(key, obj) {
					if(isEmpty(columnListStr))	columnListStr	 = 		 key;
					else						columnListStr	+= "," + key;	
				});
			}
		}
		if(isEmpty(primaryColumnList)) primaryColumnList = parm.primaryKeyList;
		
		let searchWhereList 	= [];
		let columnList			= [];
		
		const columnArr = columnListStr.split(",");
		
		$.each(columnArr, function() {
			const column = this.trim();
			columnList.push({COLUMN_ID : column});
		});
		
		if(isValid(searchWhereMap)) {
			$.each(searchWhereMap, function(key, obj) {
				let val = obj;
				if(typeof obj == "function") {
					val = obj();
				}
				if(isValid(val)) {
					let map = {}
					if(Object.prototype.toString.call(val) == '[object Array]') {
						let vals = "(";
						$.each(val, function(idx) {
							if(idx == 0) 	vals = `${vals}  '${this.val}'`;
							else			vals = `${vals}, '${this.val}'`;
						});
						vals = `${vals} )`;
						map = { column : key, value : vals, oper : "IN" }
					} else {
						map = { column : key, value : `'${val}'` , oper : "=" }
					}
					searchWhereList.push(map);
				}
			});
		}
		if(isValid(filterByPrimaryData)) {
			$.each(primaryColumnList, function() {
				const columnId = this;
				const map = {column : columnId, value : `'${filterByPrimaryData[columnId]}'`, oper : "=" }
				searchWhereList.push(map);
			});
		}
		let rparm = {
			 queryId 		: "common.getTableData"
			,requestParm	: {
				parm : {
					 tableName 			: parm.tableName
					,columnList 		: columnList
					,orderColumnList	: orderColumnList
					,searchWhereList	: searchWhereList
				}
			}
		}
		let resultData = C_COM.requestQuery(rparm);
		
		let retDataSet = {
			 map 	: {}
			,list	: []
		}
		if(resultData.state == "S") {
			
			const dataList = resultData.data;
			
			$.each(dataList, function() {
				const 	item = this;
				retDataSet.list.push(item);
				let 	key = ""; 
				let errorCheck = false;
				$.each(primaryColumnList, function() {
					if(isEmpty(item[this]) || item[this].length > 500) 	{ errorCheck = true; return false; }
					key += item[this];					
				});
				if(!errorCheck)	retDataSet.map[key] = item;
			});
		}
		return retDataSet;
		
	 }
	,makeCodeList : function(resultDataList, valueColumnName, textColumnName, defaultList) {
		let returnList = [];;
		if(isValid(defaultList)) returnList.push(defaultList);
        $.each(resultDataList, function() {
			returnList.push([this[valueColumnName], this[textColumnName]]);
		});
		return returnList;
	 }
	,getCodeList : function(grpCodeId) {
		var codeInfo = fn_copyFullObject(G_VAL.COMMON_CD_MAP[grpCodeId]);
		if(isEmpty(codeInfo)) {
			return [];
		}
		return codeInfo.codeList;
	 }
	,getBrandCodeList : function(brandId, grpCodeId) {
		let key = `${brandId}_${grpCodeId}`;
		var codeInfo = G_VAL.BRAND_CD_MAP[key];
		if(isEmpty(codeInfo)) {
			return [];
		}
		return codeInfo.codeList;
	 }
	,getCodeAttr : function(grpCodeId, codeId) {
		var codeInfo = G_VAL.COMMON_CD_MAP[grpCodeId];
		if(isEmpty(codeInfo)) {
			return {};
		}
		if(isValid(codeId)) {
			return codeInfo.codeMap[codeId];	
		} else {
			return codeInfo.codeMap;
		}
	 }  
	,getBrandCodeAttr : function(brandId, grpCodeId, codeId) {
		let key = `${brandId}_${grpCodeId}`;
		var codeInfo = G_VAL.COMMON_CD_MAP[key];
		if(isEmpty(codeInfo)) {
			return {};
		}
		if(isValid(codeId)) {
			return codeInfo.codeMap[codeId];	
		} else {
			return codeInfo.codeMap;
		}
	 }  
	,getCodeMap : function(grpCodeId) {
		let codeMap = C_COM.getCodeAttr(grpCodeId);
		if(isEmpty(codeMap)) {
			return {}
		} else {
			let retMap = {};
			$.each(codeMap, function(key, obj) {
				retMap[key] = obj.CODE_NM
			});
			return retMap;
		}
	 }  
	,getBrandCodeMap : function(brandId, grpCodeId) {
		let key = `${brandId}_${grpCodeId}`;
		let codeMap = C_COM.getCodeAttr(key);
		if(isEmpty(codeMap)) {
			return {}
		} else {
			let retMap = {};
			$.each(codeMap, function(key, obj) {
				retMap[key] = obj.CODE_NM
			});
			return retMap;
		}
	 }  
	,addKeypressListener : function(pageId, callback) {
		if(typeof callback != "function") {
			alert('addKeypressListener 호출시 함수를 전달해야 합니다.');
			return;
		}
		C_COM.keypressListenerCallbackFn[pageId] = callback;
	 }
	,getClickPosition : function() {
		return C_COM.mousePos;
	 }
	,saveSessionData : function(sessionId, sessionData)
	 {
		if(	Object.prototype.toString.call(sessionData) == '[object Object]' || 
			Object.prototype.toString.call(sessionData) == '[object Array]'  )
		{
			sessionData = JSON.stringify(sessionData)
		}
		sessionStorage.setItem(sessionId, sessionData);
	 }
	 // Session Load
	,loadSessionData : function (sessionId, valid)
	 {
		var result = sessionStorage.getItem(sessionId);

		if(valid == true) sessionStorage.removeItem(sessionId);
		try {
			var retObj = JSON.parse(result);
			return retObj;
		} catch (e) {
			return result;
		}
	 }
	 // 로컬 스토리지에 정보 저장
	,saveLocalData : function(localId, localData) {
		if(	Object.prototype.toString.call(localData) == '[object Object]' || 
			Object.prototype.toString.call(localData) == '[object Array]'  )
		{
			localData = JSON.stringify(localData)
		}
		localStorage.setItem(localId, localData);
	 }
	 // 로컬 스토리지에서 정보 읽기
	,loadLocalData : function (localId, valid) {
		var result = localStorage.getItem(localId);

		if(valid == true) localStorage.removeItem(localId);
		try {
			var retObj = JSON.parse(result);
			return retObj;
		} catch (e) {
			return result;
		}
	 }
	,deleteLocalData : function (localId) {
		localStorage.removeItem(localId);
	 }
	 // Service 요청
	,requestService		: function(parm, callback, errCallback) {
		// 서버 전송 Info
		try{
			if(isEmpty(parm)) parm = {};
			
			var sendParm = {
				 targetUrl 	: _WEB_ROOT_URL + "/requestService.do"
				,data		: parm
			}
			// LoadingBar 사용 옵션 추가 20210219
			
			if(isValid(callback)) {
				if(parm.noLoadingBar != "Y") C_COM.showLoadingBar();
				ajaxRequest(sendParm, function(resultData) {
					setTimeout(function() {
						if(parm.noLoadingBar != "Y") C_COM.hideLoadingBar();
					}, 300);
					
					if(resultData.state == "S") {
						if(typeof callback == "function") callback(resultData);
					} else if(resultData.state == "NO_SESSION"){
						if(C_COM.sessionConfirmCheck != "Y") {
							C_POP.alert('Session 정보가 없습니다.\n\n로그인 화면으로 이동합니다.');
							C_COM.sessionConfirmCheck = "Y";
							location.href="/loginMain.html";
						}
						return null;
					} else {
						if(resultData.STATUS == "FAIL") {
							C_POP.alert(resultData.STATUS_MESSAGE);
							if(resultData.STATUS_MESSAGE == "No Authority Request.") location.reload();
						} else {
							if(typeof errCallback == "function") {
								errCallback(resultData);
							} else {
								C_POP.alert(resultData.msg);
							}
						}
						return null;
					}
				});
			} else {
				var resultData = ajaxRequest(sendParm);
				if(resultData.state == "S") {
					return resultData;
				} else if(resultData.state == "NO_SESSION"){
					if(C_COM.sessionConfirmCheck != "Y") {
						C_POP.alert('Session 정보가 없습니다.\n\n로그인 화면으로 이동합니다.');
						C_COM.sessionConfirmCheck = "Y";
						location.href="/loginMain.html";
					}
					return null;
				} else {
					C_POP.alert(resultData.msg);
					return null;
				}
			}
		} catch(e){
			alert(e);
		}
	 }
	 // Service 요청
	,requestQuery		: function(parm, callback, errCallback) {
		// 서버 전송 Info
		try{
			if(isEmpty(parm)) parm = {};
			
			var targetUrl = _WEB_ROOT_URL + "/requestQuery.do"
			
			// 여러 쿼리를 동시에 가져올 경우
			if(isValid(parm.queryGroup)) {
				targetUrl = _WEB_ROOT_URL + "/requestQueryGroup.do"
			}
			
			var sendParm = {
				 targetUrl 	: targetUrl
				,data		: parm
			}

			if(isValid(callback)) {
				if(parm.noLoadingBar != "Y") C_COM.showLoadingBar();
				ajaxRequest(sendParm, function(resultData) {
					setTimeout(function() {
						if(parm.noLoadingBar != "Y") C_COM.hideLoadingBar();	
					}, 300);
					if(resultData.state == "S") {
						if(typeof callback == "function") callback(resultData);
					} else if(resultData.state == "NO_SESSION"){
						if(C_COM.sessionConfirmCheck != "Y") {
							C_POP.alert('Session 정보가 없습니다.\n\n로그인 화면으로 이동합니다.');
							C_COM.sessionConfirmCheck = "Y";
							location.href="/";
						}
						return null;
					} else {
						if(typeof errCallback == "function") {
							errCallback(resultData);
						} else {
							C_POP.alert(resultData.msg);
						}
						return null;
					}
				});
				sleep(30);
			} else {
				var resultData = ajaxRequest(sendParm);
				if(resultData.state == "S") {
					return resultData;
				} else {
					if(resultData.STATUS == "FAIL") {
						C_POP.alert(resultData.STATUS_MESSAGE);
						if(resultData.STATUS_MESSAGE == "No Authority Request.") location.reload();
					} else {
						C_POP.alert(resultData.msg);
					}
					return null;
				}
			}
		} catch(e){

		}
	 }
	,getHtmlFile : function(path) {
		var url  = _WEB_ROOT_URL + "/" + path;
		var parm = {
			 targetUrl 	: url
			,dataType	: "text"
			,method		: "get"
		}
		var html = ajaxRequest(parm);
		return html;
	 }
	,getTxtFile : function(path) {
		var url  = _WEB_ROOT_URL + path;
		
		var parm = {
			 targetUrl 	: url
			,dataType 	: "text"	
		}
		var html = ajaxRequest(parm);
		return html;
	 }
	// table rendering
	,renderHtml : function(pid, parm) {
		if(isEmpty(parm.templateId)) parm.templateId = parm.targetId;
		
		if(isValid(parm.listColumn)) {
			parm.list = parm[parm.listColumn]; // 기본 지정 리트스 컬럼
		} 
		
		if(isEmpty(parm.list)) parm.list = [];
		
		if(isValid(parm.maxrow)) {
			parm.list = parm.list.slice(0, parm.maxrow);
		}
		if(isValid(parm.targetTotalId)) {
			$("#" + pid + " #" + parm.targetTotalId).html(addComma(parm.list.length));
		}
		var html = "";
		
		let fullTemplateId = `#${pid} #${parm.templateId}`;
		
		// 공통 Template를 사용하는경우
		if(isValid(parm.commonTemplateId)) fullTemplateId = `#${parm.commonTemplateId}`;
		
		var noDataTemplateCnt = $(fullTemplateId + "_noData_template").length;
		if( isValid(parm.data) || parm.list.length > 0 || noDataTemplateCnt == 0) html = $(fullTemplateId + "_template"			).render(parm);
		else {
			if($(fullTemplateId + "_noData_template"	).length > 0) {
				html = $(fullTemplateId + "_noData_template"	).render(parm);
			}
		}
		
		if(parm.append == "Y") {
			$("#" + pid + " #" + parm.targetId).append(html);
		} else if(parm.prepend == "Y") {
			$("#" + pid + " #" + parm.targetId).prepend(html);
		} else {
			$("#" + pid + " #" + parm.targetId).html(html);
		}
		
		if(parm.replace == "Y") {
			$("#" + pid + " #" + parm.targetId).replaceWith(function() {
			    return $(this).children();
			});
		}
		
		C_COM.makeNumberTypeToInput("#" + pid + " #" + parm.targetId);
		
		C_COM.preInitTemplate(pid, parm.targetId);
		
	}
	// 입력 창을 숫자만 입력되도록 처리
	,makeNumberTypeToInput : function(domObj) {
		$(domObj).find("input[number]").each(function() {
			$(this).css			("text-align", "right");
			$(this).attr		("maxlength", 20);
			$(this).unbind		("keydown"		);
			$(this).bind		("keydown", function() {
				var val = $(this).val();
				if(val == "" || isNumber(val)) $(this).attr("preval", val);
			});
			$(this).unbind		("keyup"		);
			$(this).bind		("keyup", function() {
				var val = $(this).nval();
				if(val != "-" && val != "" && !isNumber(val)) {
					var preval = $(this).attr("preval");
					$(this).val(preval);
				} else if( val != "" && val != "-" && val != "-0") {
					val = val.replaceAll(",", "");
					var lastCharCheck = val.substring(val.length - 1);
					var fix = $(this).attr("fix");

					if(isEmpty(fix)) fix = C_COM._DEFAULT_FIX;
					
					fix = Number(fix);
					
					if(lastCharCheck == "."){
						if(fix == 0) {
							var preval = $(this).attr("preval");
							$(this).val(preval);
						}
					} else {
						// 소수점 아래에서 0이 입력됬을 경우
						var narr = val.split(".");
						if(isValid(narr[1]) && lastCharCheck == "0" && fix > 0) {
							val = narr[0] + "." + narr[1].substring(0, fix);
						} else {
							var xx1	= Math.pow(10, fix);
							
							// PC 계산 오류에 의한 버그 방어 코드(floor를 사용할 경우 10.12 -> 10.199999999.. -> 10.11 오류 발생)
							var xx2	= xx1 + 0.0000000000000000000001;		
							
							// 붙여넣기 여부 확인
							var pasteCheck = $(this).attr("paste");
							if(pasteCheck == "Y") {
								// 붙여 넣기 인경우 반올림  처리
								val = Math.round(val * xx2) / xx1;
								$(this).attr("paste", "");
							} else {
								// 일반 입력시 버림 처리(음수인경우 역으로 계산)
								if(val < 0)	val = Math.ceil (val * xx2) / xx1;
								else		val = Math.floor(val * xx2) / xx1;
							}
						}
						$(this).val(addComma(val));
					}
				}
			});
			$(this).unbind		("paste"		);
			$(this).bind		("paste", function(event) {
				// 붙여 넣기를 할경우 Flag Setting하여 반올림 처리하도록 함.
				$(this).attr("paste", "Y");
			});
		});
	 }
	,makeUniqueId : function() {
		var parm = {
			 serviceId 				: "ExdevCommonService.makeUniqueId"
			,requestParm			: {}
		}
		
		var retData = C_COM.requestService(parm);
		return retData.data.id;
	 }
	,registLateFn : function(fnId, fn, waitTime) {
		C_COM.lateFn[fnId] = fn;
		setTimeout(function() {
			if(isValid(C_COM.lateFn[fnId])) C_COM.lateFn[fnId] = undefined;
		}, waitTime);
	 }
	,excuteLateFn : function(fnId) {
		if(typeof C_COM.lateFn[fnId] == "function") C_COM.lateFn[fnId]();
		C_COM.lateFn[fnId] = undefined;
	 }
	,excelDownloadFromTable : function(domId) {
		var html = $("#" + domId).html();
		
		$("#excelCopyTmpTable").html(html);
		
		$("#excelCopyTmpTable a").each(function() {
			$(this).wrap("<span></span>");
			var val = $(this).html();
			$(this).parent().html(val);
		});
		$("#excelCopyTmpTable").show();
		copyToClipboard("excelCopyTmpTable");
		$("#xcelCopyTmpTable").hide();
		C_POP.alert('본문이 클립보드에 복사되었습니다.\n엑셀에 붙여넣기 하여 사용하세요.');
		$("#excelCopyTmpTable").html("");
	 }
	,loadingDepth : 0
	,showLoadingBar : function() {
		$('#loadingOverlay').show();
		C_COM.loadingDepth++;
	 }
	,hideLoadingBar : function() {
		C_COM.loadingDepth--;
		if(C_COM.loadingDepth < 1) {
			C_COM.loadingDepth = 0;
			$("#loadingOverlay").hide();
		}
	 }
	,getCurrentTemplateId : function() {
		var templateId = C_POP.getCurrentPopupId();
		if(isEmpty(templateId)) templateId = C_PM.getCurrentPageId();
		return templateId
	 }
	,getCurrentViewId : function() {
		let viewId = C_TAB.currentTabCompId;
		if(isEmpty(viewId)) viewId = C_PM.getCurrentPageId();
		return viewId;
	 }
	,showLeftMenu : function() {
		$("#lnb_MenuList").show();
	 }
	,hideLeftMenu : function() {
		$("#lnb_MenuList").hide();
	 }
	//Template 사용하는 모든페이지에 대해 초기화
	,preInitTemplate : function(templateId) {
		// onEnter처리
		var templateWebId = "#" + templateId + " ";
		
		$(`${templateWebId} input, ${templateWebId} select`).each(function() {
			let onEnter = $(this).attr("onEnter");
			if(isEmpty(onEnter)) onEnter = $(this).attr("OnEnter");
			if(isEmpty(onEnter)) onEnter = $(this).attr("onenter");
			if(isValid(onEnter)) {
				
				$(this).unbind("keyup");
				$(this).bind("keyup",function(event) {
					if(event.keyCode == C_COM.KEY_CD.ENTER) {
						eval(onEnter);
					}
				});
			}
		});
		
		$(templateWebId).addClass("col10");

		C_UICOM.addListnerRightIconClick(templateId);
	 }
	,excelUploadCallbackFn : undefined
	// Excel Upload To Table
    ,selectExcelUploadToTable : function(excelUploadCallbackFn) {
        
        C_COM.excelUploadCallbackFn = excelUploadCallbackFn;
        
        $("#_common_excelFileInput").remove();
        
        $("body").append(`<input id="_common_excelFileInput" type="file" onchange="C_COM.excelFileUpload();" style="display:none"/>`);
        
        $("#_common_excelFileInput").trigger("click");
     }
    ,excelFileUpload : function() {
        const fileInput = $('#_common_excelFileInput')[0];
        const file = fileInput.files[0];

        const formData = new FormData();
        formData.append('file', file);
        
        C_COM.showLoadingBar();
        $.ajax({
            url: '/commonExcelUpload.do',
            type: 'POST',
            data: formData,
            contentType: false,
            processData: false,
            success: function(response) {
                C_COM.hideLoadingBar();
                if(typeof C_COM.excelUploadCallbackFn == "function" ) C_COM.excelUploadCallbackFn(response);
                else {
                    C_POP.alert('Excel Upload에 성공 하였습니다.'); 
                }
            }
            ,error: function(error) {
                C_COM.hideLoadingBar();
                if(typeof C_COM.excelUploadCallbackFn == "function" ) C_COM.excelUploadCallbackFn(error);
                else {
                    C_POP.alert('Excel Upload에 실패 하였습니다.'); 
                }
            }
        });
     }
     
    ,FOOD_COMPANY : undefined
     // Excel Upload To Table
    ,selectMaterialsExcelUploadToTable : function(excelUploadCallbackFn,foodCompany ) {
        
        C_COM.excelUploadCallbackFn = excelUploadCallbackFn;
        C_COM.FOOD_COMPANY = foodCompany;
        
        $("#_common_excelFileInput").remove();
        
        $("body").append(`<input id="_common_excelFileInput" type="file" onchange="C_COM.excelMaterialsFileUpload();" style="display:none"/>`);
        
        $("#_common_excelFileInput").trigger("click");
     }
    ,excelMaterialsFileUpload : function() {
        
        const fileInput = $('#_common_excelFileInput')[0];
        const file = fileInput.files[0];

        $("#_excelUpload_form").remove();

        const formHtml = `
                            <form method="POST" id="_excelUpload_form" onsubmit="return false;" enctype="multipart/form-data">
                                <input id="" type="file" onchange="" tabindex="-1" multiple style="display:none"/>
                            </form>
        `    
        $("body").append(formHtml);

        // 폼데이터 담기
        var form = document.querySelector("#_excelUpload_form");
        var formData = new FormData(form);
        var FILE_IDS = C_COM.makeUniqueId();
        formData.append("GRP_FILE_ID"   , 'GRP_EXCEL_UPLOAD');
        formData.append("OWNER_CD"      , 'EXCEL_OWNER'     );
        formData.append("attach_file"   , file              );
        formData.append("FILE_IDS"      , FILE_IDS          );

        let newFormData = new FormData();
        formData.forEach((value,key)=>{
            newFormData.append(key, value);
        })
        C_COM.showLoadingBar();

        $.ajax({
            url: '/multiFileUpload.do',
            method: 'POST',
            data: formData,
            contentType: false,
            processData: false,
            success: function (response) {

                const msg = response.msg;

                const formData = new FormData();
                formData.append('file', file);
                formData.append('FILE_IDS',FILE_IDS);
                formData.append('UPLOAD_LOG_ID',C_COM.makeUniqueId());
                formData.append('FOOD_COMPANY',C_COM.FOOD_COMPANY);
                
                $.ajax({
                    url: '/materialsExcelUpload.do',
                    type: 'POST',
                    data: formData,
                    contentType: false,
                    processData: false,
                    success: function(response) {
                        C_COM.hideLoadingBar();
                        if(typeof C_COM.excelUploadCallbackFn == "function" ) C_COM.excelUploadCallbackFn(response);
                        else {
                            C_POP.alert('Excel Upload에 성공 하였습니다.'); 
                        }
                    }
                    ,error: function(error) {
                        C_COM.hideLoadingBar();
                        if(typeof C_COM.excelUploadCallbackFn == "function" ) C_COM.excelUploadCallbackFn(error);
                        else {
                            C_POP.alert('Excel Upload에 실패 하였습니다.'); 
                        }
                    }
                });
            },
            error: function (xhr, desc, err) {
                C_COM.hideLoadingBar();
                alert('에러가 발생 하였습니다.');
                return;
            }
        })
     }
     
	,getFileId : function(GRP_FILE_ID, OWNER_CD, callback) {
		if(isEmpty(GRP_FILE_ID)) {
			alert('File Group ID가 유효하지 않습니다.');
			return;
		} else if	(isEmpty(OWNER_CD)) {
			alert('File Owner ID가 유효하지 않습니다.');
			return;
		} else if	(isEmpty(callback)) {
			alert('Callback Function이 필요합니다.');
			return;
		}		
		
		var parm = {
			 queryId 		: "Filemng.getFileList"
			,requestParm	: {GRP_FILE_ID : GRP_FILE_ID, OWNER_CD : OWNER_CD}
		}
		C_COM.requestQuery(parm, function(resultData) {
			callback(resultData.data);
		});
	 }
	,fileDownload : function(fileId) {
		location.href="/filedownload.do?FILE_ID=" + fileId;	
	 }
	,getImageUrl : function(fileId) {
		return "/filedownload.do?FILE_ID=" + fileId;
	 }
	,clearFileGroup : function(GRP_FILE_ID, OWNER_CD, callback) {
		if			(isEmpty(GRP_FILE_ID)) {
			alert('File Group ID가 유효하지 않습니다.');
			reutrn;
		} else if	(isEmpty(OWNER_CD)) {
			alert('File Owner ID가 유효하지 않습니다.');
			reutrn;
		} else if	(isEmpty(callback)) {
			alert('Callback Function이 필요합니다.');
			reutrn;
		}		
		
		var parm = {
			 queryId 		: "Filemng.clearFileList"
			,requestParm	: {GRP_FILE_ID : GRP_FILE_ID, OWNER_CD : OWNER_CD}
		}
		C_COM.requestQuery(parm, function(resultData) {
			callback(resultData);
		});
	 }
	,makeArrayTwoColumn : function(list, column1, column2) {
		
		if(isEmpty(list)) {
			return [];
		}
		if(isEmpty(column1) || isEmpty(column2)) {
			C_POP.alert('C_COM.makeArrayTwoColumn Function의 Parameter값을 확인하세요.');
			return [];
		}
		
		let retList = [];
		$.each(list, function() {
			let arr = [this[column1], this[column2]];
			retList.push(arr);
		});
		return retList;
		
	 }
	,saveWorkHistory : function(userId, content) {
		var parm = {
			 queryId 		: "operation.saveWorkHistory"
			,requestParm	: {
				 WH_ID			: C_COM.makeUniqueId()
				,USER_ID 		: userId
				,WORK_CONTENT	: content
			}
		}

		C_COM.requestQuery(parm, function(resultData) {

		});
	 }
/*	,addAlarm : function(parm, callback) {
		var rparm = {
			 queryId 		: "common.addAlarm"
			,requestParm	: {
				 ALARM_ID		: C_COM.makeUniqueId()
				,TARGET_USER_ID : parm.userId
				,CONTENT		: parm.content
				,DIRECT_EXEC	: parm.directExec
			}
		}

		C_COM.requestQuery(rparm, function(resultData) {
			if(typeof callback == "function") callback();
		});
	 }*/
	,getCurrentStoreName : function() {
		const brandId = G_VAL.session.BRAND_ID;
		const storeId = G_VAL.current.storeId;
		const retval = G_VAL.session.storeList[brandId].map[storeId].STORE_NM;
		return retval;
	 }
	,getStoreList : function(brandId, callback, allCheck, keyColumn) {
		if(isEmpty(brandId)) {
			C_POP.alert('Brand ID가 필요합니다.');
			return;
		}
		
		let resultData;

		if(isValid(G_VAL.session.storeList[brandId])) {
			resultData = G_VAL.session.storeList[brandId].list;
		} else {
			var parm = {
				 queryId 		: "common.getStoreList"
				,requestParm	: {BRAND_ID : brandId}
			}
	        resultData = C_COM.requestQuery(parm);
	
			G_VAL.session.storeList[brandId] = {
				 list 	: resultData
				,map	: arrayToMap(resultData.data, "STORE_ID")
			}
		}

		let dataList = resultData.data;

		if(isEmpty(keyColumn)) keyColumn = "STORE_ID";

		if(G_VAL.session.USER_TYPE == "STORE") {
			let newList = []
			$.each(dataList, function() {
				if(this[keyColumn] == G_VAL.session[keyColumn]) {
					newList.push(this);
				}
			});
			dataList = newList;
		}
    	let selectList = C_COM.makeCodeList(dataList, keyColumn, "STORE_NM");

		if(isEmpty(G_VAL.current.storeId)) G_VAL.current.storeId = dataList[0].STORE_ID;
    	
    	if(isEmpty(selectList)) selectList = [];
    	
    	if(allCheck) selectList.unshift(["", "전체"]);
    	
		if(typeof callback == "function") callback(selectList); 
	 }
	// Session이 있으면 treu, 없으면 false를 리턴한다.
	,validSession : function(callback) {
		C_COM.showLoadingBar();
		ajaxRequest("/getSession.do", function(data) {
			C_COM.hideLoadingBar();
			let check = true;
	        if(isEmpty(data) || data.msg == "0" || data.msg == "500") check = false; 
			if(typeof callback == "function") callback(check);
	    });
	 }	 
	,pdfUploadCallbackFn : undefined
	// PDF Upload To Table
	,selectPdfUploadToTable : function(pdfUploadCallbackFn) {
		
		C_COM.validSession(function(check) {
			if(check === true){
				C_COM.pdfUploadCallbackFn = pdfUploadCallbackFn;
				
				$("#_common_pdfFileInput").remove();
				
				$("body").append(`<input id="_common_pdfFileInput" type="file" onchange="C_COM.pdfFileUpload();" style="display:none"/>`);
				
				$("#_common_pdfFileInput").trigger("click");
			}else{
				C_POP.alert('Session 정보가 없습니다.\n\n로그인 화면으로 이동합니다.');
				location.replace("/");
				return; 
			}
		});
	}
	,pdfFileUpload : function() {
		const fileInput = $('#_common_pdfFileInput')[0];
	    const file = fileInput.files[0];

	    const formData = new FormData();
	    formData.append('file', file);
		
		C_COM.showLoadingBar();

	    $.ajax({
	        url: _WEB_ROOT_URL + "/commonPdfUpload.do",
	        type: 'POST',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success: function(response) {
				C_COM.hideLoadingBar();
				if(typeof C_COM.pdfUploadCallbackFn == "function" ) C_COM.pdfUploadCallbackFn(response);
				else {
					C_POP.alert('PDF Upload에 성공 하였습니다.');	
				}
			}
	        ,error: function(error) {
				C_COM.hideLoadingBar();
				if(typeof C_COM.pdfUploadCallbackFn == "function" ) C_COM.pdfUploadCallbackFn(error);
				else {
					C_POP.alert('PDF Upload에 실패 하였습니다.');	
				}
	        }
	    });
	 }
	,processFinAnalStatusCallbackFn : undefined
	,processFinAnalStatus : function(processFinAnalStatusCallbackFn) {
		
		C_COM.showLoadingBar();
	    $.ajax({
	        url: '/processFinAnalStatus.do',
	        type: 'POST',
	        contentType: false,
	        processData: false,
	        success: function(response) {
				C_COM.hideLoadingBar();
				C_POP.alert('기업재무분석 이관에 성공 하였습니다.');	
			}
	        ,error: function(error) {
				C_COM.hideLoadingBar();
				C_POP.alert('기업재무분석 이관에 실패 하였습니다.');	
	        }
	    });

	 }
	,getUniqueId : function() {
		return getUniqueId(G_VAL.session.BRAND_ID);
	 }
	,destroyComponent : function(compId) {
		$("#" + compId).remove();
		try {
			eval("var pObj = " + compId);
			if(typeof pObj.destroy == "function") pObj.destroy();
			eval("delete " + compId);
		} catch(e) {}
	 }
	,showHelp : function(parm) {
		C_POP.open("popup_system_helpView",parm , function(retData) {
			
		});
	 }
	,showCommonHelp : function(parm) {
		C_POP.open("popup_system_commonHelpView",{commonHelpType : parm} , function(retData) {
			
		});
	 }
}
















/**
 * 작성자 : 위성열 
 * 작성일 : 
 * Page 관리 Class
 */
var C_PM = {
	 eventFn 		: {}
	,currentPageId 	: ""
	,pageParmInfo	: {}
	,homeId			: _ROOT_PAGEID
	/*
	 * 작성자 : 위성열 
	 * 작성일 : 
	 * 설  명 : Page를 이동한다.
	 * Paramter 설명
	 * - pageId : 이동할 Page ID
	 */
	,movePage : function(pageId, parm) {
		
		C_TAB.clear();
		
		if(parm == undefined) parm = {};
		
		parm.pageId = pageId;
		
				// title 처리
		parm.__menuInfo = C_MENU.getCurrentMenuInfo();
		
		parm.session = G_VAL.session;
		
		var clearCheck = parm.clearCheck;
		
		if(!isValid(pageId)) {
			alert('Page ID가 없습니다.');
			return;
		}
		// Page ID에 해당하는 Url의 html을 가져온다.
		var urlBody	= pageId.replaceAll("_", "/");
		var url 	= "ui/" + urlBody + ".html";
		var html 	= C_COM.getHtmlFile(url);
		try {
			if(html == "error") {
				alert("메뉴 화면이 없거나, 메뉴 이동 중 오류가 발생 했습니다. <br/><br/> 관리자에게 문의 하세요.");
				return;
			}
			var token = html.split("/");
		} catch(e) {
			alert('Page ID에 해당하는 Content가 없거나, Page에 오류가 있습니다.');
			return;
		}
		
		// 다른 Page를 Import해서 사용할 수 있도록 한다.
		if(token[0] == "import") {
			urlBody	= token[1].replaceAll("_", "/");
			url 	= "ui/" + urlBody + ".html";
			html 	= C_COM.getHtmlFile(url);
			if(isValid(token[2])) {
				eval("parm = $.extend(parm, " + token[2] + ");");
			}
		}
		
		if(isEmpty(html)) {
			C_POP.alert('Import 하려는 Page ID가 존재 하지 않습니다.\n\nPage ID를 확인하시기 바랍니다.');
			return;
		}

		C_PM.pageParmInfo[pageId] = parm;
		
		var prePageId = this.getCurrentPageId();
		
		if(isValid(prePageId)) {
			eval("if (typeof " + prePageId + ".destroy == 'function') " + prePageId + ".destroy();");
		}
		
		// 현재 Page 등록
		this.setCurrentPageId(pageId);
		
		// 가상의 Document에 가져온 html 을 Setting한다.
		var docDiv = $("<div></div>");
		$(docDiv).html(html);
		
		// html에서 최상위Div에 pageId를 id로 부여한다.(unique값)
		$("page-component", docDiv).eq(0).attr("id"	, pageId);
		// html에서 최상위Div에 Page Block이라는 Name을 부여한다.(전체 page 동일값 pageBlockDiv);
		$("page-component", docDiv).eq(0).attr("name"	, "pageBlockDiv");

		// 기존 Page는 숨긴다.
		$("page-component[name=pageBlockDiv]").hide();
		
		// 동일한 PageId로 이미 Loading되어 있으면 삭제한다.
		C_COM.destroyComponent(pageId);

		// 이동할 Page를 Load 한다.
		var htmlSrc = $(docDiv).html();
		
		htmlSrc = htmlSrc.render("<@", ">", parm);
		
		$("#bodyBlock").append(htmlSrc);
		
		// 이동할 Page의 Page Set Object를 가져온다.
		//var pageObj = fn_getObjectFromString(pageId);
		
		// 현재 Page를 저장한다.
		C_PM.setCurrentPageId(pageId);
		
		// 페이지 이동에 대한 History저장
		C_HM.pushPageStack(pageId);
		
		// 현재 구성된 Page의 스크립트 실행전 공통 초기화
		C_PM.preInitPage(pageId);

		// onLoadPage로 설정된 Function 실행
		if(typeof C_PM.eventFn[pageId] == "function") C_PM.eventFn[pageId](parm);
		
		// 현재 구성된 Page의 스크립트 실행 후 공통 초기화
		C_PM.afterInitPage(pageId);
		
		if(parm.breadcrumb == "hide") {
			$("#breadcrumb").hide();
		} else {
			$("#breadcrumb").show();
		}
	}
	// Page에 Load시 스크립트 실행전 공통 설정을 한다.
	,preInitPage : function(pageId, targetDomId) {
		C_COM.preInitTemplate(pageId);
	 }
	// Page에 Load시 스크립트 실행 후 공통 설정을 한다.
	,afterInitPage : function(pageId) {
	 }
	,setCurrentPageId : function(pageId) {
		C_COM.saveSessionData("PAGE_ID", pageId);
	 }
	,getCurrentPageId : function() {
		return C_COM.loadSessionData("PAGE_ID");
	 }
	,onLoadPage : function(pageId, callback) {
		C_PM.eventFn[pageId] = callback;
	 }
	,goHome : function() {
		C_HM.clear();
		C_PM.movePage(C_PM.homeId);
	 }
	,reloadPage : function() {
		var cPageId = C_PM.getCurrentPageId(); 
		var pageId = C_HM.historyBack();
		var parm = C_PM.pageParmInfo[cPageId];
		C_PM.movePage(cPageId, parm);
	 }
	,replacePage : function(pageId, parm) {
		C_HM.historyBack();
		C_PM.movePage(pageId, parm);
	 }
};
// C_PM 초기화 루틴
(function() {
	C_COM.saveSessionData("PAGE_ID", "");
})();

// Page History 관리 프로세스 
var C_HM = {
	 eventFn		: {}	
	,pageStack 		: []
	,running		: false
	,pushPageStack	: function(pageId) {
		var newList = [];
		$.each(C_HM.pageStack, function() {
			var item = this + "";
			if(item != pageId) newList.push(item);
		});
		newList.push(pageId);
		C_HM.pageStack = newList;
	 }
	,popPageStack	: function() {
		var tStack= C_HM.pageStack.slice(0, -1);
		if(tStack.length > 0) {
			C_HM.pageStack = tStack;
			var pageId = C_HM.pageStack[C_HM.pageStack.length - 1];
			return pageId;
		} else {
			return null;
		}
	 }
	,historyBack	: function(returnParm) {
		if(C_HM.running) return;
		C_HM.running = true;

		var pageId = C_HM.popPageStack();

		if(isEmpty(pageId)) {
			C_HM.running = false;
			return;
		}
		var csPageId = C_PM.getCurrentPageId();

		C_COM.destroyComponent(csPageId);
		
		C_PM.setCurrentPageId(pageId);
		
		$("#" + pageId).show();
		
		if(isEmpty(returnParm)) returnParm = {};
		
		if(typeof C_HM.eventFn[pageId] == "function") C_HM.eventFn[pageId](csPageId, returnParm);

		C_HM.running = false;
	 }
	,onReturn 	: function(pageId, callback) {
		C_HM.eventFn[pageId] = callback;
	 }
	,clear		: function() {
		var csPageId = C_PM.getCurrentPageId();

		C_COM.destroyComponent(csPageId);

		var pageId = C_HM.popPageStack();
		while(pageId != null) {
			C_COM.destroyComponent(pageId);
			pageId = C_HM.popPageStack();
		}
	 }
}

// Popup
var C_POP = {
	 eventFn 			: {}
	,activeCnt 			: 0 
	,popupStack			: []
	,alivePopupObject	: {}
	,callbackMap		: {}
	,normalSizeMap		: {}
	,open	: function(popupId, parm, callback) {
		if(parm == undefined) parm = {};

		parm.popupId = popupId;
		
		parm.opener	 = C_COM.getCurrentTemplateId();
		
		parm.session = G_VAL.session;
		
		let prePopupId = C_POP.getCurrentPopupId();
		
		C_POP.callbackMap[popupId] = callback;
		
		// Popup ID에 해당하는 Url의 html을 가져온다.
		var urlBody	= popupId.replaceAll("_", "/");
		var url 	= "ui/" + urlBody + ".html";
		var html 	= C_COM.getHtmlFile(url);
		
		if(isEmpty(html)) {
			C_POP.alert('Popup ID가 존재 하지 않습니다.\n\nPopup ID를 확인하시기 바랍니다.');
			return;
		}
		
		// 가상의 Document에 가져온 html 을 Setting한다.
		var docDiv = $("<div></div>");
		$(docDiv).html(html);
		// html에서 최상위Div에 popupId를 id로 부여한다.(unique값)
		$("div", docDiv).eq(0).attr("id"	, popupId);
		
		// 동일한 popupId로 이미 Loading되어 있으면 삭제한다.
		C_COM.destroyComponent(popupId);

		// 이동할 Popup를 Load 한다.
		var htmlSrc = $(docDiv).html();
		htmlSrc = htmlSrc.render("<@", ">", parm);
		
		$("body").append(htmlSrc);
		
		C_POP.pushPopupStack(popupId);
		
        var myModal = document.getElementById(popupId);
        // 모달 인스턴스를 생성합니다
        var modal = new bootstrap.Modal(myModal, {
	        backdrop: 'static', // 외부 클릭 시 닫힘 방지
	        keyboard: false // ESC 키로 닫힘 방지
		});

        // 모달을 표시합니다
        modal.show();

		C_POP.alivePopupObject[popupId] = modal;

		// onLoadPopup로 설정된 Function 실행
		if(typeof C_POP.eventFn[popupId] == "function") C_POP.eventFn[popupId](parm);
		
		// Page 내의 처리는 Popup도 Page와 동일하기 떄문에 C_PM의 initPage를 사용한다.
		C_POP.preInitPopup(popupId);
		
		if(parm.size == "MAX") {
			C_POP.maxSize(popupId);
		}
		if(parm.size == "NORMAL") {
			C_POP.normalSize(popupId);
		}

		$(`#${popupId} .btn_zoomInOut`).unbind("click");
		$(`#${popupId} .btn_zoomInOut`).bind("click", function() {
			C_POP.toggleSize(popupId);
		});
		
		// 기존 Popup이 있으면 비활성화
		if(isValid(prePopupId)) {
			$("#" + prePopupId).addClass("explan-inactive");			
		}
		
	    $(`#${popupId}`).draggable({
	        handle: ".modal-header"  // #title을 클릭/드래그하면 전체 #allpopup이 움직임
	    });		
		
		
	 }
	// Page에 Load시 스크립트 실행전 공통 설정을 한다.
	,preInitPopup : function(popupId) {
		C_COM.preInitTemplate(popupId);
	 }
	,close	: function(returnData) {
		var popupId = C_POP.getCurrentPopupId();
		C_POP.popPopupStack();

		if(isEmpty(C_POP.alivePopupObject[popupId])) return;
		
		C_POP.alivePopupObject[popupId].hide();
		
		C_POP.alivePopupObject[popupId] = undefined;
		
		if(typeof C_POP.callbackMap[popupId] == "function") C_POP.callbackMap[popupId](returnData);

		C_POP.callbackMap[popupId] = undefined;
		
		setTimeout(function() {
			C_COM.destroyComponent(popupId);
		}, 300);
	 }
	,onLoadPopup : function(popupId, callback) {
		C_POP.eventFn[popupId] = callback;
	 }
	,pushPopupStack	: function(popupId) {
		var newList = [];
		$.each(C_POP.popupStack, function() {
			var item = this + "";
			if(item != popupId) newList.push(item);
		});
		newList.push(popupId);
		C_POP.popupStack = newList;
	 }
	,popPopupStack	: function() {
		C_POP.popupStack = C_POP.popupStack.slice(0, -1);
		if(C_POP.popupStack.length > 0) {
			var popupId = C_POP.popupStack[C_POP.popupStack.length - 1];
			// 현재 Popup이 있으면 활성화
			if(isValid(popupId)) {
				$("#" + popupId).removeClass("explan-inactive");			
			}
			return popupId;
		} else {
			return null;
		}
	 }
	,getCurrentPopupId : function() {
		if(C_POP.popupStack.length == 0 ) 	return null;
		else 								return C_POP.popupStack[C_POP.popupStack.length - 1];
	 }
	,getPopupState : function() {
		if(C_POP.popupStack.length > 0)	return "on";
		else							return "off";
	 }
	,alert : function(msg, callback) {
		alert(msg);
		if(typeof callback == "function") callback();
	 }
	,confirm : function(msg, okFn) {
		var flag = confirm(msg);
		if(flag) {
			if(typeof okFn == "function") okFn();
		}
	 }
	,maxSize : function(popupId) {
		$(`#${popupId} div[role='document']`).addClass("full-modal-dialog");
	 }
	,normalSize : function(popupId) {
		// 창크기 복원
		$(`#${popupId} div[role='document']`).removeClass("full-modal-dialog");
	 }
	,toggleSize : function(popupId) {
		if(isEmpty(C_POP.normalSizeMap[popupId])) {
			C_POP.maxSize(popupId);
		} else {
			if(C_POP.normalSizeMap[popupId].popupState == "N") {
				C_POP.maxSize(popupId);
			} else {
				C_POP.normalSize(popupId);
			}
		}
	 }
}		

// UI 관련 공통

var C_UICOM = {
	 useSelectBoxIdList 	: {}
	,listenerChangeFnMap	: {}
	,listenerConfirmFnMap	: {}
	,multiBoxPreValListMap	: {}
	,selectBoxOption		: {}
	,layerMenuBoxIdList		: {}
	,init			: function() {
		$(document).on('click', function(e) {
			const list = C_UICOM.layerMenuBoxIdList;
			$.each(list, function() {
				const targetId = this.targetId;
		        if (!$(e.target).closest(`#menu_${targetId}, #${targetId}`).length) {
		            $(`#menu_${targetId}`).hide();
		        }
			})
		});
	 }
	,makeSelectBox : function(parm, selectType) {
		
		if(isEmpty(selectType)) selectType = "single";
		parm.selectType = selectType;
		var defaultItem = parm.defaultItem
		var list 		= parm.data;
		var templateId 	= parm.templateId;
		var targetId 	= parm.targetId;

		if(isValid(defaultItem)) {
			nlist = [defaultItem];
			$.each(list,function(){nlist.push(this)});
			list = nlist;
		}

		if(isEmpty(templateId)) templateId = C_COM.getCurrentTemplateId();

		var templateWebId = "#" + templateId + " #" + targetId + " ";

		var templateTargetId = templateId + targetId;
		
		if(typeof parm.onChange == "function") {
			C_UICOM.listenerChangeFnMap[templateTargetId] = parm.onChange;	
		}
		if(typeof parm.onConfirm == "function") {
			C_UICOM.listenerConfirmFnMap[templateTargetId] = parm.onConfirm;	
		}
		C_UICOM.selectBoxOption[templateTargetId] = parm;

		C_UICOM.useSelectBoxIdList[templateTargetId] = {templateId : templateId, targetId : targetId};

		if(list.length == 0) {
			alert('Select Box Set Data가 없습니다.');
			return;
		}
		if(selectType == "single") {
			$(templateWebId).html("");
			$.each(list, function() {
				let optionStr =  `<option value="${this[0]}">${this[1]}</option>`;
				$(templateWebId).append(optionStr);
			});
			// 기본값 지정이 있으면 Setting
			if(isValid(parm.defaultVal)) C_UICOM.setSingleBox(targetId, parm.defaultVal);
			
		} else if(selectType == "singleByFilter") {
			let optionList = [];
			$.each(list, function() {
				let item = {
					 val : this[0]
					,txt : this[1]
				}
				optionList.push(item);
			});
			var rparm = {
				 targetId 			: targetId
				,commonTemplateId	: "singleBoxByFilterComponent"
				,list				: optionList
			}
			C_COM.renderHtml(templateId, rparm);
			
			C_UICOM.layerMenuBoxIdList[templateTargetId] = {templateId : templateId, targetId : targetId};
			
			if(isValid(parm.defaultVal)) {
				C_UICOM.setSingleBoxByFilter(targetId, parm.defaultVal);	
			}
		} else if(selectType == "multi") {
			let optionList = [];
			$.each(list, function() {
				let item = {
					 val : this[0]
					,txt : this[1]
				}
				optionList.push(item);
			});
			var rparm = {
				 targetId 			: targetId
				,commonTemplateId	: "multiBoxComponent"
				,list				: optionList
			}
			C_COM.renderHtml(templateId, rparm);
			
			C_UICOM.layerMenuBoxIdList[templateTargetId] = {templateId : templateId, targetId : targetId};

			if(isValid(parm.defaultVal)) parm.defaultValList = [parm.defaultVal] 
			
			if(isValid(parm.defaultValList)) {
				C_UICOM.setMultiBox(targetId, parm.defaultValList);	
			}
		}
	 }
	,addListnerOnChange : function(targetId, changeFn) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateTargetId 	= templateId + targetId;
		C_UICOM.listenerChangeFnMap[templateTargetId] = changeFn;
	 }
	,setSingleBox : function(targetId, val) {
		var templateId = C_COM.getCurrentTemplateId();
		var templateWebId = "#" + templateId + " #" + targetId + " ";
		$(`${templateWebId} option[value=${val}]`).prop("selected", true);
	 }
	,setMultiBox : function(targetId, valList) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		$(`${templateWebId} input[type=checkbox]`).prop("checked", false);		
		
		if(isEmpty(valList)) return;
		
		$.each(valList, function() {
			let val = this;

			if(!isNumber(val) && val.toUpperCase() == "ALL") {
				$(`#${templateId} input[type=checkbox]`).prop("checked", true);
				return false;
			}
			
			$(`#${templateId} #cb_${val}`).prop("checked", true);
		});
		this.makeMultiBoxText(targetId);
		
		this.multiBoxPreValListMap[templateTargetId] = valList;
		
	 }
	,setSingleBoxByFilter : function(targetId, val) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		if(isEmpty(val)) return;
		
		const itemtxt = $(`${templateWebId} #label_${val}`).attr("itemtxt");
		
		$(`${templateWebId} #label_${val}`).addClass("explan-selectbg");

		$(`${templateWebId} #selectedText`).attr("value", val).html(itemtxt);
		
	 }
	,makeMultiBoxText : function(targetId) {
		var templateId = C_COM.getCurrentTemplateId();
		var templateWebId = "#" + templateId + " #" + targetId + " ";
		var totalCheckboxCnt = $(`${templateWebId} input[type=checkbox]`).length - 1;
		let selectedList = C_UICOM.getData(targetId);
		let selectedText = "선택";
		if			(selectedList.length == totalCheckboxCnt) {
			selectedText = "전체";
		} else if	(selectedList.length == 1) {
			selectedText = selectedList[0].txt
		} else if	(selectedList.length > 1 ) {
			selectedText = `${selectedList[0].txt} 외${selectedList.length - 1}건`;
		}
		$(`${templateWebId} #selectedText`).html(selectedText);
	 }
	,getData : function(targetId){

		var templateId = C_COM.getCurrentTemplateId();

		var templateWebId = "#" + templateId + " #" + targetId + " ";

		var templateTargetId = templateId + targetId;

		let option = C_UICOM.selectBoxOption[templateTargetId];
		
		if 			( option.selectType == "single") {
			let retval = $(`${templateWebId} option:selected`).val();
			return retval;
		} else if 	( option.selectType == "multi") {
			let result = [];
			$(`${templateWebId} input[type=checkbox]:checked`).each(function() {
				let item = {
					 val : $(this).attr("value") 	
					,txt : $(this).next().text()
				}
				if(item.val == "all") return true;
				result.push(item);
			});
			return result;
		} else if 	( option.selectType == "singleByFilter") {
			const val = $(`${templateWebId} #selectedText`).attr("value");
			return val;
		}
	 }
	,getDataMap : function(targetId){

		var templateId = C_COM.getCurrentTemplateId();

		var templateWebId = "#" + templateId + " #" + targetId + " ";

		var templateTargetId = templateId + targetId;

		let option = C_UICOM.selectBoxOption[templateTargetId];
		
		if 			( option.selectType == "single") {
			let retval = $(`${templateWebId} option:selected`).val();
			let rettxt = $(`${templateWebId} option:selected`).text();
			return {value : retval, text : rettxt};
		} else if 	( option.selectType == "multi") {
			let result = [];
			$(`${templateWebId} input[type=checkbox]:checked`).each(function() {
				let item = {
					 val : $(this).attr("value") 	
					,txt : $(this).next().text()
				}
				result.push(item);
				if(item.val == "all") return true;
			});
			return result;
		}
		
	 }
	,setSelectBoxOption : function(targetId, key, val) {
		var templateId = C_COM.getCurrentTemplateId();

		var templateWebId = "#" + templateId + " #" + targetId + " ";

		if( val == "Y") {
			$(templateWebId).prop("disabled", true);	
		} else {
			$(templateWebId).prop("disabled", false);
		}
	 }
	,clickSelectBoxItem : function(targetId, val, txt) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		$(`${templateWebId} label`).removeClass("explan-selectbg");
		
		$(`${templateWebId} #label_${val}`).addClass("explan-selectbg");
		
		$(`${templateWebId} #selectedText`).html(txt);
		$(`${templateWebId} #selectedText`).attr("value", val);
		
		$(`#menu_${targetId}`).hide();
		
		if( typeof C_UICOM.listenerChangeFnMap[templateTargetId] == "function" ) {
			let valList = C_UICOM.getData(targetId);
			C_UICOM.listenerChangeFnMap[templateTargetId](valList);
		}
	 }
	,changeSelectBoxCheckBox : function(targetId) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		let allCheck = true;
		
		$(`${templateWebId} input[type=checkbox]`).each(function() {
			
			let id		= $(this).attr("id");
			if(id == "cb_all") return true;
			
			let checked = $(this).prop("checked");
			if(!checked) {
				allCheck = false;
				return false;
			}
		});
		
		$(`${templateWebId} #cb_all`).prop("checked", allCheck);
		
		C_UICOM.makeMultiBoxText(targetId);
		
		if( typeof C_UICOM.listenerChangeFnMap[templateTargetId] == "function" ) {
			let valList = C_UICOM.getData(targetId);
			C_UICOM.listenerChangeFnMap[templateTargetId](valList);
		}
	 }
	,changeSelectBoxAllCheckBox : function(targetId) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		let allCheck = $(`${templateWebId} #cb_all`).prop("checked");
		
		$(`${templateWebId} input[type=checkbox]`).prop("checked", allCheck);
		
		C_UICOM.makeMultiBoxText(targetId);

		if( typeof C_UICOM.listenerChangeFnMap[templateTargetId] == "function" ) {
			let valList = C_UICOM.getData(targetId);
			C_UICOM.listenerChangeFnMap[templateTargetId](valList);
		}
	 }
	,onConfirmSelectBox : function(targetId) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;

		let valMapList = C_UICOM.getData(targetId);
		
		let valList = [];
		
		$.each(valMapList, function() {
			valList.push(this.val);
		});

		this.multiBoxPreValListMap[templateTargetId] = valList;
		
		$(`${templateWebId} #menu_${targetId}`).hide();

		if( typeof C_UICOM.listenerConfirmFnMap[templateTargetId] == "function" ) {
			C_UICOM.listenerConfirmFnMap[templateTargetId](valMapList);
		}
	 }
	,onCancelSelectBox : function(targetId) {
		var templateId 			= C_COM.getCurrentTemplateId();
		var templateWebId 		= "#" + templateId + " #" + targetId + " ";
		var templateTargetId 	= templateId + targetId;
		
		const preValList = this.multiBoxPreValListMap[templateTargetId];
		
		C_UICOM.setMultiBox(targetId, preValList);
		
		$(`${templateWebId} #menu_${targetId}`).hide();
	 }
	,keyupFromMultiSelectbox : function(targetId, value) {
		
		if(isEmpty(value)) {
			$(`#${targetId} label[name='list']`).show();			
		} else {
			$(`#${targetId} label[name='list']`).each(function() {
				const tstr = $(this).attr("itemtxt");
				if(tstr.indexOf(value) > -1) 	$(this).show();
				else							$(this).hide();
			});
		}
	 }
	,hasIcon : function(dom) {
		if($(dom).hasClass("search-form")) 	return true;
		else								return false;
	 }
	,addListnerRightIconClick : function(domId) {
		let dom;
		
		if(typeof domId =="string") dom = $(`#${domId} input[type=text]`);
		else 						dom = domId;
		
		// 정사각형 영역 크기 (예: 30px)
		var squareSize = 12;
		if(this.hasIcon(dom)) 	{
			$(dom).addClass	("pr30");
			squareSize = 30;
		} else {
			$(dom).addClass	("delet-form").addClass	("pr15");	
		}
		
		$(dom).unbind	('click');
		$(dom).bind		('click', function(e) {
			// 입력창의 너비와 위치 정보 얻기
			var rect = this.getBoundingClientRect();
			var clickX = e.clientX;
			var relativeX = clickX - rect.left;
			
			if (relativeX > rect.width - squareSize) {
				
				const fn = $(this).attr("onClickRightIcon");
				
				if(isValid(fn)) eval(fn);
				
				// 오른쪽 끝 정사각형 영역 클릭
				if($(this).hasClass("delet-form")) {
					$(this).val("");
				}
			}
		});
	}
}


var C_PAGING = {
	 defaultListRange 	: 10
	,defaultPageRange 	: 10
	,listRange			: {}	// 리스트 범위
	,pageRange			: {}	// Page 범위
	,listDomId			: {}	// 리스트가 표시되는 Dom Id
	,pagingDomId		: {}	// Page가 표시되는 Dom Id
	,totalCntDomId		: {}	// Page가 표시되는 Dom Id
	,queryId			: {}
	,parmObj			: {}
	,parmFn				: {}
	,onPageClickCallback: {}
	,makeListFn			: {}
	,allDataList		: {}
	,pageInfo			: {}
	,create 			: function(parm) {
		var pageId 		= parm.pageId;
		var listDomId 	= parm.listDomId;
		var key = pageId + listDomId;
		if(isEmpty(parm.listRange)) parm.listRange = C_PAGING.defaultListRange;
		if(isEmpty(parm.pageRange)) parm.pageRange = C_PAGING.defaultPageRange;

		C_PAGING.listRange				[key] = parm.listRange 			; 
		C_PAGING.pageRange				[key] = parm.pageRange 			;
		C_PAGING.listDomId				[key] = parm.listDomId     		;
		C_PAGING.pagingDomId			[key] = parm.pagingDomId     	;
		C_PAGING.totalCntDomId			[key] = parm.totalCntDomId     	;
		C_PAGING.queryId				[key] = parm.queryId   			;
		C_PAGING.parmObj				[key] = parm.parmObj		   	;
		C_PAGING.parmFn					[key] = parm.parmFn			   	;
		C_PAGING.onPageClickCallback	[key] = parm.onPageClickCallback;
		C_PAGING.makeListFn				[key] = parm.makeListFn			;
		
		if(parm.type == "page") {
			C_PAGING.makePageList(pageId, listDomId, 1);
		} else {
			if(typeof parm.callback == "function" ) {
				C_PAGING.getGridPageList(pageId, listDomId, 1, parm.callback);
			}		
		}
	 }
	,goSearch		: function(pageId, listDomId) {
		C_PAGING.makePageList(pageId, listDomId, 1);
	 }
	,getGridPageList	: function(pageId, listDomId, pageIdx, callback) {
		var key = pageId + listDomId;
		
		var pagingDomId = C_PAGING.pagingDomId[key];
		
		var option = {
			 currentPage	: Number(pageIdx)
			,listRange		: C_PAGING.listRange[key]
			,pageRange		: C_PAGING.pageRange[key]
		}
		
		// 호출한 페이지에서 제공하는 Parm을 option에 Setting한다.
		if(isValid(C_PAGING.parmObj[key])) {
			option.parm = C_PAGING.parmObj[key];
		} else if(typeof C_PAGING.parmFn[key] == "function") {
			option.parm = C_PAGING.parmFn[key]();
		} else {
			option.parm = {};
		}
		var parm = {
			 serviceId 	: "ExdevCommonService.getPagingList"
			,requestParm: {
				 queryId	: C_PAGING.queryId[key]
				,option 	: option
			 }
			,useLoadingBar	: true
		}
		C_COM.requestService(parm, function(resultData) {

			var totalPage 		= resultData.data.totalPage;
			
			var totalCnt		= resultData.data.totalCnt;
			
			if(isEmpty(totalCnt)) resultData.data.totalCnt = 0;
			
			// Data 가공 Function이 등록되어있는 경우.
			
			let dataList = resultData.data;
			if(isValid(C_PAGING.makeListFn)) {
				if(typeof C_PAGING.makeListFn[key] == "function") {
					dataList = C_PAGING.makeListFn[key](dataList);
				}	
			}
			
			if(typeof callback == "function") callback(dataList);
			
		});
	 }
	,makePageList	: function(pageId, listDomId, pageIdx) {
		var key = pageId + listDomId;
		
		var pagingDomId = C_PAGING.pagingDomId[key];
		
		var option = {
			 currentPage	: Number(pageIdx)
			,listRange		: C_PAGING.listRange[key]
			,pageRange		: C_PAGING.pageRange[key]
		}
		
		// 호출한 페이지에서 제공하는 Parm을 option에 Setting한다.
		if(isValid(C_PAGING.parmObj[key])) {
			option.parm = C_PAGING.parmObj[key];
		} else if(typeof C_PAGING.parmFn[key] == "function") {
			option.parm = C_PAGING.parmFn[key]();
		} else {
			option.parm = {};
		}

		var parm = {
			 serviceId 	: "ExdevCommonService.getPagingList"
			,requestParm: {
				 queryId	: C_PAGING.queryId[key]
				,option 	: option
			 }
			,useLoadingBar	: true
		}
		C_COM.requestService(parm, function(resultData) {

			var totalPage 		= resultData.data.totalPage;
			
			var totalCnt		= resultData.data.totalCnt;
			
			if(isEmpty(totalCnt)) totalCnt = 0;

			var maxNextPage 	= Math.floor(totalPage / option.pageRange)
			var startPageIdx	= Math.floor((pageIdx - 1) / option.pageRange) * option.pageRange + 1
			var	endPageIdx		= startPageIdx + option.pageRange - 1;
			if(endPageIdx > totalPage) endPageIdx = totalPage;
			
			var prevPageIdx		= startPageIdx 	- 1;
			var nextPageIdx		= endPageIdx 	+ 1;
			
			
			var pageListHtml	 = "";
			
			
			var pageListHtml	 	 = ``;
			
			if(pageIdx > 1) {
				pageListHtml 	+= `	<a href="javascript:C_PAGING.makePageList('${pageId}', '${listDomId}', 1)" class="btn_pg_first">첫번째 페이지</a>`;
			} else {
				pageListHtml 	+= `	<a href="javascript:" class="btn_pg_first disabled">첫번째 페이지</a>`;
			}
			if(startPageIdx > option.pageRange) {
				pageListHtml 	+= `	<a href="javascript:C_PAGING.makePageList('${pageId}', '${listDomId}', ${prevPageIdx})" class="btn_pg_prev">이전 페이지</a>`;
			} else {
				pageListHtml 	+= `	<a href="javascript:" class="btn_pg_prev disabled">이전 페이지</a>`;
			}
			for(var ii = startPageIdx; ii <= endPageIdx; ii++) {
				var acrive = "";
				if(ii == pageIdx) {
					pageListHtml 	+= `	<strong title="현재 페이지">${pageIdx}</strong>`;
					
				} else {
					pageListHtml 	+= `	<a href="javascript:C_PAGING.makePageList('${pageId}', '${listDomId}', ${ii})">${ii}</a>`;
				}
			}
			if(endPageIdx < totalPage) {
				pageListHtml 	+= `	<a href="javascript:C_PAGING.makePageList('${pageId}', '${listDomId}', ${nextPageIdx})" class="btn_pg_next">다음 페이지</a>`;
			} else {
				pageListHtml 	+= `	<a href="javascript:" class="btn_pg_next disabled">다음 페이지</a>`;
			}
			if(pageIdx < totalPage) {
				pageListHtml 	+= `	<a href="javascript:C_PAGING.makePageList('${pageId}', '${listDomId}', ${totalPage})" class="btn_pg_last">마지막 페이지</a>`;
			} else {
				pageListHtml 	+= `	<a href="javascript:" class="btn_pg_last disabled">마지막 페이지</a>`;
			}
			
			$("#" + pageId + " #" + pagingDomId).html(pageListHtml);
			
			var list = resultData.data.pageList;
			if(typeof C_PAGING.makeListFn[key] == "function") list = C_PAGING.makeListFn[key](list);
			
			var rparm = {
				 targetId 		: C_PAGING.listDomId[key]
				,list			: resultData.data.pageList
			}
			C_COM.renderHtml(pageId, rparm);
			
			if(isValid(C_PAGING.totalCntDomId[key])) {
				$("#" + pageId + " #" + C_PAGING.totalCntDomId[key]).html(totalCnt);
			}
			
			
			
			if(typeof C_PAGING.onPageClickCallback[key] == "function") C_PAGING.onPageClickCallback[key](resultData.data);
		});
	 }
	 // Local Paging 
	,renderHtml : function(pageId, parm) {
		
		if(isValid(parm.list)) {
			$.each(parm.list, function(idx) {
				if(idx == 0 && isValid(this.rownum)) return false;
				parm.list[idx].rownum = idx + 1;
			});
		} else parm.list = []; 
		
		

		var listDomId 	= parm.targetId;
		var key = pageId + listDomId;
		if(isEmpty(parm.listRange)) parm.listRange = C_PAGING.defaultListRange;
		if(isEmpty(parm.pageRange)) parm.pageRange = C_PAGING.defaultPageRange;
	
		C_PAGING.listRange				[key] = parm.listRange 			; 
		C_PAGING.pageRange				[key] = parm.pageRange 			;
		C_PAGING.listDomId				[key] = parm.listDomId     		;
		C_PAGING.pagingDomId			[key] = parm.pagingDomId     	;
		C_PAGING.totalCntDomId			[key] = parm.totalCntDomId     	;
		C_PAGING.onPageClickCallback	[key] = parm.onPageClickCallback;
		C_PAGING.allDataList			[key] = parm.list				; //new
		
		let totalCnt = parm.list.length;
		
		C_PAGING.pageInfo				[key] = {
			 totalPage	: Math.ceil(totalCnt / parm.listRange)
			,totalCnt	: totalCnt
		}
		
		C_PAGING.makeRenderHtml(pageId, listDomId, 1);
	 }
	,makeRenderHtml : function(pageId, listDomId, pageIdx) {
		
		var key = pageId + listDomId;
		
		var pagingDomId = C_PAGING.pagingDomId[key];
		
		var option = {
			 currentPage	: Number(pageIdx)
			,listRange		: C_PAGING.listRange[key]
			,pageRange		: C_PAGING.pageRange[key]
		}

		let pageInfo 	= C_PAGING.pageInfo		[key];
		let allDataList	= C_PAGING.allDataList	[key];
		
		var totalPage 		= pageInfo.totalPage;
		
		var totalCnt		= pageInfo.totalCnt;
		
		if(isEmpty(totalCnt)) totalCnt = 0;

		var maxNextPage 	= Math.floor(totalPage / option.pageRange)
		var startPageIdx	= Math.floor((pageIdx - 1) / option.pageRange) * option.pageRange + 1
		var	endPageIdx		= startPageIdx + option.pageRange - 1;
		if(endPageIdx > totalPage) endPageIdx = totalPage;
		
		var prevPageIdx		= startPageIdx 	- 1;
		var nextPageIdx		= endPageIdx 	+ 1;
		
		var pageListHtml	  = ``;
		
		if(pageIdx > 1) {
			pageListHtml 	+= `	<a href="javascript:C_PAGING.makeRenderHtml('${pageId}', '${listDomId}', 1)" class="btn_pg_first">첫번째 페이지</a>`;
		} else {
			pageListHtml 	+= `	<a href="javascript:" class="btn_pg_first disabled">첫번째 페이지</a>`;
		}
		if(startPageIdx > option.pageRange) {
			pageListHtml 	+= `	<a href="javascript:C_PAGING.makeRenderHtml('${pageId}', '${listDomId}', ${prevPageIdx})" class="btn_pg_prev">이전 페이지</a>`;
		} else {
			pageListHtml 	+= `	<a href="javascript:" class="btn_pg_prev disabled">이전 페이지</a>`;
		}
		for(var ii = startPageIdx; ii <= endPageIdx; ii++) {
			var acrive = "";
			if(ii == pageIdx) {
				pageListHtml 	+= `	<strong title="현재 페이지">${pageIdx}</strong>`;
				
			} else {
				pageListHtml 	+= `	<a href="javascript:C_PAGING.makeRenderHtml('${pageId}', '${listDomId}', ${ii})">${ii}</a>`;
			}
		}
		if(endPageIdx < totalPage) {
			pageListHtml 	+= `	<a href="javascript:C_PAGING.makeRenderHtml('${pageId}', '${listDomId}', ${nextPageIdx})" class="btn_pg_next">다음 페이지</a>`;
		} else {
			pageListHtml 	+= `	<a href="javascript:" class="btn_pg_next disabled">다음 페이지</a>`;
		}
		if(pageIdx < totalPage) {
			pageListHtml 	+= `	<a href="javascript:C_PAGING.makeRenderHtml('${pageId}', '${listDomId}', ${totalPage})" class="btn_pg_last">마지막 페이지</a>`;
		} else {
			pageListHtml 	+= `	<a href="javascript:" class="btn_pg_last disabled">마지막 페이지</a>`;
		}
		
		$("#" + pageId + " #" + pagingDomId).html(pageListHtml);
		
		if(typeof C_PAGING.makeListFn[key] == "function") list = C_PAGING.makeListFn[key](list);
		
		let startListIdx	= (pageIdx - 1) * option.listRange 
		let endListIdx		= pageIdx * option.listRange 
		
		let list = [];
		
		$.each(allDataList, function(idx) {
			if(idx >= startListIdx && idx < endListIdx) {
				list.push(this);
			}
		});

		var rparm = {
			 targetId 		: listDomId
			,list			: list
		}
		C_COM.renderHtml(pageId, rparm);
		
		if(isValid(C_PAGING.totalCntDomId[key])) {
			$("#" + pageId + " #" + C_PAGING.totalCntDomId[key]).html(totalCnt);
		}
		
		if(typeof C_PAGING.onPageClickCallback[key] == "function") {
			
			let retParm = {
				 pageIdx 	: pageIdx
				,list		: list
			}
			
			C_PAGING.onPageClickCallback[key](retParm);	
		}
		
		C_COM.preInitTemplate(pageId, listDomId);
		
	}		
}













// Comp Class
var C_COMP = {
	 eventFn 			: {}
	,callbackMap		: {}
	,import	: function(targetId, compId, parm, callback) {
		
		if(parm == undefined) parm = {};
		
		var templateId = parm.templateId;
		
		parm.session = G_VAL.session;
		
		if(isEmpty(templateId)) templateId = C_COM.getCurrentTemplateId();
		
		var templateWebId = "#" + templateId + " #" + targetId + " ";
		
		// Comp ID에 해당하는 Url의 html을 가져온다.
		var urlBody	= compId.replaceAll("_", "/");
		var url 	= "ui/" + urlBody + ".html";

		var html 	= C_COM.getHtmlFile(url);
		
		if(isEmpty(html)) {
			C_POP.alert('Component ID가 존재 하지 않습니다.\n\nComponent ID를 확인하시기 바랍니다.');
			return;
		}
		parm.compTemplateId = compId;

		compId = templateId + targetId;
		
		C_COM.destroyComponent(compId);
		
		// 기본 Component인경우 targetId를 그대로 사용함.
		if(templateId == "baseComponent") compId = targetId;

		parm.compId = compId;
		
		parm.parentId = templateId;
		
		C_COMP.callbackMap[compId] = callback;
		
		// 가상의 Document에 가져온 html 을 Setting한다.
		var docDiv = $("<div></div>");
		$(docDiv).html(html);

		// html에서 최상위Div에 compId를 id로 부여한다.(unique값)
		if($("component", docDiv).length > 0) {
			$("component"	, docDiv).eq(0).attr("id"	, compId);
		} else {
			$("div"			, docDiv).eq(0).attr("id"	, compId);
		}
		
		// Import할 Comp를 Load 한다.
		var htmlSrc = $(docDiv, "#" + compId).html();
		
		htmlSrc = htmlSrc.render("<@", ">", parm);
		
		$(templateWebId).html(htmlSrc);
		
		if(parm.width != "free") $(templateWebId).addClass("col10");
				
		// onLoadComp로 설정된 Function 실행
		if(typeof C_COMP.eventFn[compId] == "function") C_COMP.eventFn[compId](parm);
		
		// Page 내의 처리는 Comp도 Page와 동일하기 떄문에 C_PM의 initPage를 사용한다.
		C_COMP.preInitComp(compId);
		
		eval(templateId + "." + targetId + " = " + compId);

		eval(`${compId}.compId = '${parm.compTemplateId}';`);
		
	 }
	// Page에 Load시 스크립트 실행전 공통 설정을 한다.
	,preInitComp : function(compId) {
		C_COM.preInitTemplate(compId);
	 }
	,onLoadComp : function(compId, callback) {
		C_COMP.eventFn[compId] = callback;
	 }
	,callback : function(compId, data) {
		if(typeof C_COMP.callbackMap[compId] == "function") C_COMP.callbackMap[compId](data);
	 }
	,getCompObj : function(compId) {
		try {
			var templateId = C_COM.getCurrentTemplateId();
			eval("let compObj = " + templateId + compId);
			return compObj;
		} catch(e) {
			alert('Component Object를 가져오는데 실패 했습니다.');
			return null;
		}
	 }
}


var C_ALARM = {
	  realTimeAlarmList : []
	 ,init			: function() {
		C_ALARM.realTimeAlarmSet(function(realTimeAlarmList) {
			C_ALARM.realTimeAlarmList = realTimeAlarmList;
	     	let rparm = {
				 queryId 		: "common.getAlarmListCount"
				,requestParm	: {}
				,noLoadingBar	: "Y"
	     	}
			C_COM.requestQuery(rparm, function(resultData) {
				let cnt = resultData.data[0].CNT + realTimeAlarmList.length;
				if(cnt == 0) {
					$("#alarmCount").hide();
				} else {
					$("#alarmCount").show();
					$("#alarmCount").html(cnt)
				}
			});
		});
		setTimeout(C_ALARM.init, 15000);
	 }
	,realTimeAlarmSet : function(callback) {
		// 알람 발생 등록
		//// 자문진행 현황 메모 변화 등록
		C_COM.requestQuery({queryId	: "contract.getCmmtUpdateContractList", requestParm :{}, noLoadingBar : "Y" }, function(resultData) {
			callback(resultData.data);
		});
	 }  
	,showAlarmPopup : function()  {
		C_POP.open('popup_common_alarmPopup', {}, function(retData) {
			//C_ALARM.init();
		});
	 }
	,addAlarm : function(parm, callback) {
		var rparm = {
			 queryId 		: "common.addAlarm"
			,requestParm	: {
				 ALARM_ID		: C_COM.makeUniqueId()
				,TARGET_USER_ID : parm.userId
				,PARAM			: parm.param || ''
				,CONTENT		: parm.content
				,DIRECT_EXEC	: parm.directExec
			}
		}

		C_COM.requestQuery(rparm, function(resultData) {
			if(typeof callback == "function") callback();
		});
	 }

}


var C_TAB = {
	 tabListMap 			: {}
	,currentTabCompId 		: ""
	,currentTabCompPageId 	: ""
	,currentTabTitle 		: ""
	,makeTab : function(templateId, targetId, tabList, sendParm) {
		var tabKey = templateId + "_" + targetId;
		
		var rparm = {
			 targetId 			: targetId
			,templateId			: templateId
			,commonTemplateId	: "tabComponent"
			,list				: tabList
			,tabKey				: tabKey
		}
		C_COM.renderHtml(templateId, rparm);
		
		C_TAB.tabListMap[tabKey] = tabList;
		
		C_TAB.openTab(templateId, targetId, 0, sendParm);
		
	 }
	,openTabByCompId : function(templateId, targetId, compId, sendParm) {
		
		var tabKey = templateId + "_" + targetId;
		
		const tabList = C_TAB.tabListMap[tabKey];
		
		let selectedIdx = 0;
		
		$.each(tabList, function(idx) {
			if(this.compId == compId) {
				selectedIdx = idx;
				return false;	
			}
		});
		
		C_TAB.openTab(templateId, targetId, selectedIdx, sendParm);
	 }	
	,openTab : function(templateId, targetId, idx, sendParm) {

	    let tabKey = templateId + "_" + targetId;
	    
	    let tabBodyId = `${tabKey}_tab${idx}`;
	    
	    let tabObj	  = C_TAB.tabListMap[tabKey][idx];

	    let selectedTabCompId = tabObj.compId;
	    
	    let headerKey = `${tabKey}_header`;

	    let $header = $("#" + headerKey);

	    $(this).addClass("pop_tab_action");

		$header.find(".pop_tab_action"	).removeClass("pop_tab_action");
		$header.find("#tab" + idx		).addClass("pop_tab_action");
	    
	    let $btns = $header.find(".pop_tab_btn");
	    
	    // component가 Load 안되어 있으면 Load한다. TODO

		if(isEmpty(tabObj.parm	)) tabObj.parm = {}
		
		tabObj.parm.directCallParm = sendParm;
		
		tabObj.parm.tabTitle = tabObj.title; 
		
		C_TAB.currentTabCompId 		= selectedTabCompId;
		C_TAB.currentTabCompPageId 	= tabObj.compPageId;
		C_TAB.currentTabTitle		= tabObj.title;

	    C_COMP.import(selectedTabCompId, tabObj.compPageId, tabObj.parm , tabObj.callback);		    

	    let $pop_tab_nav_content = $(`#${templateId} #${targetId} .pop_tab_nav_content`);
	    $pop_tab_nav_content.hide();

	    $(`#${templateId} #${tabBodyId}`).show();
	 }
	,clear : function() {
		delete C_TAB.currentTabCompId 	;
		delete C_TAB.currentTabTitle	;
	 }
	,getCurrentTabCompId 	: function() {
		return C_TAB.currentTabCompId;
	 }
	,getCurrentTabTitle		: function() {
		return C_TAB.currentTabTitle	;
	 }
}  



var C_STOCK = {
	 stockEvaluationCriteriaList 	: []
	,stockInOutListMap				: {}
	,stockAdjustListMap				: {}
	,init : function() {
		if(isValid(C_STOCK.stockEvaluationCriteriaList)) return;
		var parm = {
			 queryId 		: "common.getStockEvaluationCriteria"
			,requestParm	: { BRAND_ID : G_VAL.session.BRAND_ID }
		}
        C_COM.requestQuery(parm, function(resultData) {
        	C_STOCK.stockEvaluationCriteriaList = resultData.data;
        });
	 }
	,makeStockOfDay : function(sparm, callback) {
		
		C_STOCK.init();
		
		let brandId 	= G_VAL.session.BRAND_ID;
		let storeId 	= G_VAL.current.storeId;

		var parm = {
			 queryId 		: "storeManage.getStockAnalysisByDay2"
			,requestParm	: { BRAND_ID : brandId, STORE_ID : storeId }
		}
        C_COM.requestQuery(parm, function(resultData) {
        	if(resultData.state == "S") {
        		let stockInOutList = resultData.data
        		
    			var parm2 = {
       				 queryId 		: "storeManage.getStockAdjustList"
       				,requestParm	: { BRAND_ID : brandId, STORE_ID : storeId }
       			}
       	        C_COM.requestQuery(parm2, function(resultData2) {
    	        	if(resultData2.state == "S") {
    	        		let stockAdjustList = resultData2.data
						C_STOCK._makeStockListOfDay(stockInOutList, stockAdjustList, callback);	    	        		
    	        	}
       	        });
        	}
        });
	 }
	,_makeStockListOfDay : function(stockInOutList, stockAdjustList, callback) {

		let stockInOutListMap 	= {};
		let stockAdjustListMap 	= {};
		let dateListMap			= {};
		let dateList			= [];
		$.each(stockAdjustList, function() {
			let item = this;
			if(isEmpty(stockAdjustListMap[item.PRODUCT_CD])) {
				stockAdjustListMap[item.PRODUCT_CD] = {
					 dateList : []
					,itemMap  : {}
				};
			}
			if(isEmpty(dateListMap[item.PRODUCT_CD])) dateListMap[item.PRODUCT_CD] = { dateList : [] };
			stockAdjustListMap[item.PRODUCT_CD].dateList.push(item.ADJUST_DATE);
			stockAdjustListMap[item.PRODUCT_CD].itemMap[item.ADJUST_DATE] = item;
			dateListMap[item.PRODUCT_CD][item.ADJUST_DATE] = "Y";
		});
		$.each(stockInOutList, function() {
			let item = this;
			if(isEmpty(stockInOutListMap[item.PRODUCT_CD])) {
				stockInOutListMap[item.PRODUCT_CD] = {
					 baseInfo : item
					,dateList : []
					,itemMap  : {}
				};
			}
			if(isEmpty(dateListMap[item.PRODUCT_CD])) dateListMap[item.PRODUCT_CD] = { dateList : [] };
			stockInOutListMap[item.PRODUCT_CD].dateList.push(item.YYYYMMDD);
			stockInOutListMap[item.PRODUCT_CD].itemMap[item.YYYYMMDD] = item;
			dateListMap[item.PRODUCT_CD][item.YYYYMMDD] = "Y";
		});
		
		$.each(dateListMap, function(pcd, obj) {
			$.each(obj, function(ymd, obj2) {
				if(ymd == "dateList") return true;
				dateListMap[pcd].dateList.push(ymd);
			});
			dateListMap[pcd].dateList.sort();
		});
		
		C_STOCK.stockInOutListMap 	= stockInOutListMap;
		C_STOCK.stockAdjustListMap 	= stockAdjustListMap;
		C_STOCK.dateListMap			= dateListMap;
		
		if(typeof callback == "function" ) return callback({stockInOutListMap : stockInOutListMap, stockAdjustListMap : stockAdjustListMap});
	 }
	,getStockList : function() {
		let stockList = [];
		$.each(C_STOCK.stockInOutListMap, function(key, map) {
			let resultList 	= C_STOCK.getStockAnalysisByDay(map.baseInfo.PRODUCT_CD);
			let lastItem	= resultList[resultList.length - 1]; 
			stockList.push(lastItem);
		});
		return stockList;
	 }
	,getStockAnalysisByDay : function(productCd) {
		
		let stockAnalysisByDay = [];
		
		if(isEmpty(C_STOCK.stockInOutListMap[productCd]		)) C_STOCK.stockInOutListMap[productCd] = {}
		if(isEmpty(C_STOCK.stockAdjustListMap[productCd]	)) C_STOCK.stockAdjustListMap[productCd] = {}
		
		let inOutItemMap 	= C_STOCK.stockInOutListMap[productCd].itemMap;
		let adjustItemMap 	= C_STOCK.stockAdjustListMap[productCd].itemMap;
		
		if(isEmpty(inOutItemMap	)) inOutItemMap 	= {}
		if(isEmpty(adjustItemMap)) adjustItemMap	= {}

		let dateList		= C_STOCK.dateListMap[productCd].dateList;

		let resultList		= []
		let adjustList		= []
		
		let nujukSumQt		= 0;
		let totIn 			= 0;
		let totSumIn 		= 0;
		let totSumOut		= 0;
		let preInoutItem	= {}
		let totalLossQt 	= 0;
		let totalLossRt 	= 0;
		let lossCnt		= 0;
		$.each(dateList, function() {
			let yyyymmdd 	= this;
			let inoutItem 	= inOutItemMap [yyyymmdd];
			let adjustItem 	= adjustItemMap[yyyymmdd];
			
			if(isValid(inoutItem)) {
				nujukSumQt 							= inoutItem.SUM_QUANTITY + nujukSumQt;
				inoutItem.nujukSumQt 				= nujukSumQt;
				inoutItem.STOCK_RATE 				= Math.round(nujukSumQt / inoutItem.AVG_QUANTITY *1000 ) / 10;;

				if(isEmpty(inoutItem.IN_QUANTITY	)) inoutItem.IN_QUANTITY 	= 0;
				if(isEmpty(inoutItem.OUT_QUANTITY	)) inoutItem.OUT_QUANTITY 	= 0;
				if(isEmpty(inoutItem.AVG_QUANTITY	)) inoutItem.AVG_QUANTITY 	= 0;

				resultList.push(inoutItem);
				preInoutItem 						= inoutItem;
				totIn								= totIn  	+ inoutItem.IN_QUANTITY	;
				totSumIn							= totSumIn  + inoutItem.IN_QUANTITY	;
				totSumOut							= totSumOut + inoutItem.OUT_QUANTITY;
				inoutItem.inQuantitySum				= totSumIn							;
				inoutItem.outQuantitySum			= totSumOut							;
				inoutItem.startDate					= dateList[0]						;
				
				inoutItem.allLossQt 				= totSumIn - totSumOut - nujukSumQt;
				if(totSumIn == 0) 	inoutItem.allLossRt	= "Error";
				else				inoutItem.allLossRt	= Math.round(inoutItem.allLossQt / totSumIn *1000) / 10;
			}
			if(isValid(adjustItem)) {
				if(lossCnt == 0) 	adjustItem.lossQt = 0;
				else				adjustItem.lossQt = nujukSumQt - adjustItem.ADJUST_QUANTITY;
				totalLossQt 					   += adjustItem.lossQt;
				if(totIn == 0) 	adjustItem.lossRt	= "Error";
				else {
					if(lossCnt == 0) 	adjustItem.lossRt	= 0;
					else				adjustItem.lossRt	= Math.round(adjustItem.lossQt / totIn *1000) / 10;
					totalLossRt += adjustItem.lossRt;
				}	
				lossCnt++
				adjustItem.nujukSumQt				= adjustItem.ADJUST_QUANTITY;
				adjustItem.YYYYMMDD 				= adjustItem.ADJUST_DATE;
				adjustItem.AVG_QUANTITY				= preInoutItem.AVG_QUANTITY;
				adjustItem.STOCK_RATE 				= Math.round(adjustItem.ADJUST_QUANTITY / preInoutItem.AVG_QUANTITY *1000 ) / 10;
				
				resultList.push(adjustItem);
				adjustList.push(adjustItem);
				nujukSumQt 							= adjustItem.ADJUST_QUANTITY;
				totIn								= adjustItem.ADJUST_QUANTITY;
			}
		});
		if( lossCnt < 2 )	{
			resultList[resultList.length - 1].avgLossQt = "";
			resultList[resultList.length - 1].avgLossRt = "";	
		} else {
			lossCnt--;
			resultList[resultList.length - 1].avgLossQt = Math.round(totalLossQt / lossCnt);
			resultList[resultList.length - 1].avgLossRt = Math.round(totalLossRt / lossCnt * 10) / 10;	
		}
		return resultList;
	 }
	,evaluationCriteriaOfStock : function(rate) {
		
		if(!isNumber(rate)) return {COLOR_CODE : "#FF0000", EVALUATION : "매출 Data 점검필요"};
		
		const elist = C_STOCK.stockEvaluationCriteriaList;
		rate = Number(rate);
		let result = {};
		$.each(elist, function(idx) {
			let item = this
			if			(isEmpty(this.TOP_VAL		) && this.BOTTOM_VAL <= rate) {
				item.COLOR_CODE = this.COLOR_CODE
				result = item;
				return false;
			} else if	(isEmpty(this.BOTTOM_VAL	) && this.TOP_VAL > rate) {
				item.COLOR_CODE = this.COLOR_CODE
				result = item;
				return false;
			} else if	(this.TOP_VAL > rate 		  && rate >= this.BOTTOM_VAL ) {
				item.COLOR_CODE = this.COLOR_CODE
				result = item;
				return false;
			}
		});
		return result;
	 }
} 



// rs render 사용자 정의 function
//jsrender 사용자 정의 함수
$.views.converters({
	 // DateFormat 
	 dt 	: function(value) {return getDateFormat(value, 8);}								//YYYY-MM-dd HH:mm:ss
	,numb 	: function(value) {
		if(isNumber(value)) return addComma(value);
		else				return nvl(value, "");
	 }
	,numb2 	: function(value) {
		if(isNumber(value)) {
			let result = Math.round(value * 100) / 100; 
			return addComma(result);
		} 	
		else return nvl(value, "");
	 }
	,fix	: function(value) {
		if(typeof value == "number") {
			return value.toFixed(2)
		} else {
			return value
		}
	 }
	,toKb	: function(value) {
		if(typeof value == "number") {
			return Math.round(value / 10.24) / 100	
		} else {
			return "";
		}
			
	 }
});


// 숫자인경우 구두점 넣어서 입력됨, 구두점 제거 후 리턴됨
$.fn.nval = function(value){
	if(isValid(value)) {
		if(isNumber(value)) {
			$(this).val(addComma(value));
		} else {
			$(this).val(value);
		}
	} else {
		var val = $(this).val();
		if(typeof val == "string") 	return val.replaceAll(",", "");
		else						return val;
	}
};

//
// 최초 공통 설정 사항
//


// Browser 뒤로가기 방지

history.pushState(null, null, location.href);

var dupCheck = false;
window.onpopstate = function () {
	if(dupCheck) return;
	dupCheck = true;
    history.go(1);
    C_HM.historyBack();
    setTimeout(function() {
    	dupCheck = false;
    }, 500);
};


var C_WIN = {
	 callbackMap			: {}
	,windowSizeType 		: "B"
	,preSizeType 			: "B"
	,chageTypeCallbackMap 	: {}
	,init : function() {
		if(window.innerWidth > 769) 	C_WIN.windowSizeType =  "B";
		else							C_WIN.windowSizeType =  "S";
	 }
	,addListenerWindowResize : function(templateId, callback) {
		C_WIN.callbackMap[templateId] = callback;
	 }
	,addListenerWindowChangeSizeType : function(uid, callback) {
		C_WIN.chageTypeCallbackMap[uid] = callback;
	 }
	,onWindowResize : function() {
		var templateId = C_COM.getCurrentTemplateId();
		if(isValid(C_WIN.callbackMap[templateId])) {
			C_WIN.callbackMap[templateId]();
		}
	 }
	,onWindowChageSizeType : function() {
		$.each(C_WIN.chageTypeCallbackMap, function(key, fn) {
			fn(C_WIN.windowSizeType);
		});
	 }
} 

var C_HELP = {
	 helpColumnListMap : {}
	,init : function() {
		var parm = {
			 queryId 		: "system.getHelpContentList"
			,requestParm	: { HELP_TYPE : 'COLUMN'}
		}
        C_COM.requestQuery(parm, function(resultData) {
        	if(resultData.state == "S") {
				$.each(resultData.data, function() {
					const key = this.TEMPLATE_ID + this.COLUMN_ID
					C_HELP.helpColumnListMap[key] = this.HELP_CONTENT;
				});
        	}
        });
	 }
	,getHelpColumnContent : function(key) {
		return C_HELP.helpColumnListMap[key];
	 }
}



var C_WIDGET = {
	 updateWidgetState : function(tabTitle, checked, compId, domGrpId, title) {
		if(isEmpty(title)) title = $(`#${domGrpId}_title .biz-subTitle`).contents().get(0).nodeValue.trim();
		
		let active = "";
		if(checked)	active = "Y";  // 활성화
		else		active = "N";  // 활성화	

		let parm = {
			 USER_ID 	: G_VAL.session.USER_ID
			,COMP_ID	: compId
			,DOM_GRP_ID	: domGrpId
			,ACTIVE		: active
			,TITLE		: title
			,TAB_TITLE	: tabTitle
		}
		C_WIDGET.updateWidget(parm)
	 }
	,updateWidget : function(parm) {
		var rparm = {
			queryGroup : [
				 {
					 queryId 		: "common.updateWidget"
					,requestParm	: parm
				 }
				,{
					 queryId 		: "common.updateWidgetGrp"
					,requestParm	: parm
				 }
			]
			,noLoadingBar	: "Y"
		} 
		
		
        C_COM.requestQuery(rparm, function(resultData) {
        	if(resultData.state != "S") {
        		C_POP.alert('위젯 등록에 실패 하였습니다.');
        	}
        });
	 }
	,getWidgetList : function(compTamplateId, callback) {
		var rparm = {
			 queryId 		: "common.getWidgetList"
			,requestParm	: {COMP_ID : compTamplateId}
		}
        C_COM.requestQuery(rparm, function(resultData) {
        	if(resultData.state == "S") {
        		if(typeof callback == "function") callback(resultData.data);
        	}
        });
	 }
	,setWidgetState : function(templateId, widgetList) {
		$.each(widgetList, function() {
			if(this.ACTIVE != "Y") return true;
			$(`#${templateId} #${this.DOM_GRP_ID}_cb`).prop("checked", true);
			C_WIDGET.showGrpWidget(templateId, this.DOM_GRP_ID);
		});
	 }
	,hideAllWidget : function(grpId) {
		$(`#${grpId} div[wgtgrp='${grpId}']`).hide();
	 }
	,showAllWidget : function(grpId) {
		$(`#${grpId} div[wgtgrp='${grpId}']`).show();
	 }
	,hideGrpWidget : function(templateId, grpId) {
		$(`#${templateId} div[domgrpid='${grpId}']`).hide();
	 }
	,showGrpWidget : function(templateId, grpId) {
		$(`#${templateId} div[domgrpid='${grpId}']`).show();
	 }
	,init : function(callback) {
		C_WIDGET.getWidgetList("ALL", function(widgetList) {
    		if(typeof callback == "function") callback(widgetList);
		});
	 }
}


$(window).resize(function() {
	//hasYScrollBar();
	//hasXScrollBar();
	C_WIN.onWindowResize();
	
	C_WIN.init();
	
	if(C_WIN.preSizeType != C_WIN.windowSizeType) {
		C_WIN.onWindowChageSizeType();
		C_WIN.preSizeType = C_WIN.windowSizeType;	
	}
});


var hiddencommand 	= "SHOWPAGEID";
var showTabId 		= "SHOWTABID";
var showViewId 		= "SHOWVIEWID";
var showMenuId 		= "SHOWMENUID";
var showColumn		= "SHOWCOLUMN";
var debugcommand 	= "GODEBUG";
var configcommand 	= "GOCONFIG";
var hiddenconfirm 	= ""
$(function() {
	//C_COM.init();
	//C_ALARM.init();
	C_UICOM.init();
	C_WIN.init();
	$(window).bind("keydown",function() {
		
		hiddenconfirm += String.fromCharCode(event.keyCode);
		if(hiddenconfirm.length > 10) {
			hiddenconfirm = hiddenconfirm.substring(1,11);
		}
		if(hiddencommand 		== hiddenconfirm) {
			hiddenconfirm = "";
			var templateId = C_COM.getCurrentTemplateId();
			alert(templateId);
		} else if(showViewId 	== hiddenconfirm) {
			hiddenconfirm = "";
			alert(C_COM.getCurrentViewId());
		} else if(showColumn 	== hiddenconfirm) {
			let templateId = C_COM.getCurrentTemplateId();
			$(`#${templateId} span[name='hiddenColumnId']`).show();
			C_GRID.lockGridEvent();
			$("table thead th").removeClass("explan-noTextSelect");
		} else if(showMenuId 	== hiddenconfirm) {
			hiddenconfirm = "";
			alert(C_MENU.currentMenuId);
		} else if(hiddenconfirm.indexOf(showTabId		) > -1 ) {
			hiddenconfirm = "";
			alert(C_TAB.currentTabCompPageId);
		} else if(hiddenconfirm.indexOf(debugcommand	) > -1 ) {
			hiddenconfirm = "";
			C_PM.movePage("sample_index");
		} else if(hiddenconfirm.indexOf(configcommand	) > -1 ) {
			hiddenconfirm = "";
			C_PM.movePage("main_common_config");
		}
	});
});
