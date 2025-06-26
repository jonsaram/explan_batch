var CLASS_GRID = function(parm, gridId) {
	
	if(isEmpty(parm)) parm = {};
	this.gridIsEmpty		= false;	
    this.isDragging 		= false;
    this.startCell 			= null;
    this.preSelectedCells 	= [];
    this.selectedCells 		= [];
    this.pasteStartCell 	= null;
    this.gridId 			= gridId;
    this.undoStack			= [];
    this.redoStack 			= [];
	this.deleteRowDataList	= [];
    this.initialContent 	= "";
    this.parm 				= parm;
    this.readOnly			= parm.readOnly;
    this.updateOnly			= parm.updateOnly;
	this.deleteRowDataList	= [];
	this.hiddenColumnList	= parm.hiddenColumnList;
	this.newData			= parm.newData;				// grid에 있는 Data가 모두 새로 저장되어야 할때 "Y" 세팅한다. => 초기 row state를 insert로 설정한다.
    this.columnInfo			= {
    	columnType	: {}
    }
    this.columnConfig		= parm.columnConfig;
	if(isEmpty(this.columnConfig)) this.columnConfig 	= {}
    this.rowConfig			= parm.rowConfig;
	if(isEmpty(this.rowConfig)	 ) this.rowConfig 		= {}
    this.columnMap			= parm.columnMap;
	if(isEmpty(this.columnMap)	 ) this.columnMap 		= {}

    this.cellConfig			= parm.cellConfig;
	
	this.primaryKeyList		= parm.primaryKeyList;

	this.uniqueKeyList		= parm.uniqueKeyList;
	
	this.onInitListner		= {}
	
	this.tableInitRow		= parm.tableInitRow;
	this.noDataFirstAddFn	= parm.noDataFirstAddFn;
	
	if(typeof parm.onInit == "function") this.onInitListner[gridId] = parm.onInit;
    
    const thisCls					= this;

	// SelectBox List Data Setting
	$.each(this.columnMap, function(key, obj) {
		if(obj.columnType == "selectbox" ) {
			if(isEmpty(thisCls.columnConfig.selectBoxCodeList)) thisCls.columnConfig.selectBoxCodeList = {}
			if(isValid(obj.useCodeId)) {
				var selectBoxList = C_COM.getCodeList(obj.useCodeId);
				thisCls.columnConfig.selectBoxCodeList[key] = selectBoxList;
			} else if(isValid(obj.useBrandCodeId)) {
				var selectBoxList = C_COM.getBrandCodeList(G_VAL.session.BRAND_ID, obj.useBrandCodeId);
				thisCls.columnConfig.selectBoxCodeList[key] = selectBoxList;
			} else if(isValid(obj.selectBoxCodeList)) {
				thisCls.columnConfig.selectBoxCodeList[key] = obj.selectBoxCodeList;
			}
		}
	});

    // Select Box Code List에대해 Map을 생성한다.
    if(isValid(thisCls.columnConfig) && isValid(thisCls.columnConfig.selectBoxCodeList) ) {
    	thisCls.columnConfig.selectBoxCodeMap = {}
    	$.each(thisCls.columnConfig.selectBoxCodeList, function(key, list) {
    		$.each(list, function() {
        		if(isEmpty(thisCls.columnConfig.selectBoxCodeMap[key])) thisCls.columnConfig.selectBoxCodeMap[key] = {};
        		thisCls.columnConfig.selectBoxCodeMap[key][this[0]] = this[1];
    		});
    	});
    } 
	this.getColumnMap = function(column) {
		if(isValid(this.columnMap[column])) {
			return this.columnMap[column];	
		} else {
			return {}
		} 
	}
    this.init = function(sendParm) {
		this.deleteRowDataList	= [];
	
		if(isEmpty(sendParm)) sendParm = {};
		
		C_GRID.unlockGridEvent();
        
    	const gridId 	= this.gridId; 
		const callType 	= sendParm.callType;
    	
        // 컬럼 Type 설정
    	let columnInfo = {
    		columnArr : {}
    	}
    	$(`#${gridId} colgroup col`).each(function(idx) {
			let column 		= $(this).attr("column"		);
			let columnType 	= $(this).attr("columnType"	);
			let dataType	= thisCls.getColumnMap(column).dataType;
			let saveType	= thisCls.getColumnMap(column).saveType;
			let readOnly	= thisCls.getColumnMap(column).readOnly;
			let fix			= thisCls.getColumnMap(column).fix;
			let buttonNm	= thisCls.getColumnMap(column).buttonNm;
			let buttonFn	= thisCls.getColumnMap(column).buttonFn;
			
			if(isEmpty(columnType)) columnType = "text";
			
			columnInfo[column] = {
				 column		 : column
				,columnType  : columnType
				,dataType  	 : dataType
				,saveType  	 : saveType
				,readOnly  	 : readOnly
				,fix		 : fix
				,buttonNm	 : buttonNm
				,buttonFn	 : buttonFn
				,seq		 : (idx + 1)
			};
			columnInfo.columnArr[idx] = columnInfo[column];
    	});
    	this.columnInfo = columnInfo;
    	
        $(`#${gridId} tbody tr td`).addClass("explan-grid-cell");
		
		let initType = 'init'; 
		if(this.newData == "Y") {
			initType = 'new';
		}
		
        this.setGridIndex(gridId, initType);
        
        $(`#${gridId} tbody tr`).each(function() {

	        $(this).find('td').each(function(idx) {
				const columnInfo 	= thisCls.getColumnInfo(this);
				
				if(columnInfo.column == "checkbox") {
					$(this).find("input[type=checkbox]").on("change", function() {
						thisCls.onChangeCheckBox(this);						
					});
				} else {
		        	let value = $(this).attr("value");
					if(isEmpty(value)) value = $(this).text();
		            thisCls.setCell($(this), value, initType);

                    // 초기값 저장
                    $(this).attr("basevalue", value);
				}
				
				if(idx == 0) {
					// column index 설정
					const colIdx = $(this).attr("data-col");
					thisCls.columnInfo[columnInfo.column].columnIdx = colIdx;
				}
	        });
        });
        $(`#${gridId}`).on('mousedown'	, '.explan-grid-cell', this.onMouseDown.bind(this));
        $(`#${gridId}`).on('mousemove'	, '.explan-grid-cell', this.onMouseMove.bind(this));
        $(`#${gridId}`).on('mouseup'	, this.onMouseUp.bind(this));
        $(`#${gridId}`).on('click'		, '.explan-grid-cell', this.onClick.bind(this));
        $(`#${gridId}`).unbind('dblclick');
        $(`#${gridId}`).bind('dblclick'	, '.explan-grid-cell', this.onDblClick.bind(this));

    	if(callType != "scroll") this.initState();
		
    };
	this.onChangeCheckBox = function(tDom) {

		let targetDom = this.getTdDom(tDom);
		
		let state	= $(tDom).prop("checked");
		
		// checkboxType이 single인경우 하나만 선택되도록 한다.
		if(this.parm.checkboxType == "single") {
			this.setAllCheckBox(false);
			$(tDom).prop("checked", state);
		}
		if( typeof this.rowConfig.onChangeCheckBoxState == "function" ) {
			
			let rowData = this.getRowData(targetDom);
			
			this.rowConfig.onChangeCheckBoxState(state, rowData);	
		} 
	}
	this.initState = function() {
	    this.undoStack			= [];
	    this.redoStack 			= [];
		//this.initialContent = $(`#${this.gridId}`).html();	
		
		// 초기화 Event가 등록되어있으면 실행 한다.
		if(typeof this.onInitListner[this.gridId] == "function") this.onInitListner[this.gridId]();
	};
    this.destroy = function(gridId) {
        $(`#${gridId}`).off('mousedown');
        $(`#${gridId}`).off('mousemove');
        $(`#${gridId}`).off('mouseup');
        $(`#${gridId}`).off('click');
        $(`#${gridId}`).unbind('dblclick');
    };
	this.getColumnInfo = function(domObj) {
		if			(typeof domObj == "number") {
			return thisCls.columnInfo.columnArr[domObj]
		} else if	(typeof domObj == "string") {
			return thisCls.columnInfo[domObj]; 
		} else {
			const idx = this.getIndex(domObj);
			return thisCls.columnInfo.columnArr[idx]
		}
	};
    
    this.undo = function() {
        if (this.undoStack.length === 0) return;
        const lastState = this.undoStack.pop();
        let undoBack = [];
        lastState.forEach(cellState => {
            const cell = $(`#${this.gridId} .explan-grid-cell[data-row=${cellState.row}][data-col=${cellState.col}]`);
            undoBack.push({
                row: cellState.row,
                col: cellState.col,
                text: cell.attr("value"),
            });
            thisCls.setCell(cell, cellState.text);
        });
        this.redoStack.push(undoBack);
    };

    this.redo = function() {
        if (this.redoStack.length === 0) return;
        const nextState = this.redoStack.pop();
        let redoBack = [];
        nextState.forEach(cellState => {
            const cell = $(`#${this.gridId} .explan-grid-cell[data-row=${cellState.row}][data-col=${cellState.col}]`);
            redoBack.push({
                row: cellState.row,
                col: cellState.col,
                text: cell.attr("value"),
            });
            cell.text(cellState.text);
        });
        this.undoStack.push(redoBack);
    };

	this.editCell = function() {
        const selectedCell = this.selectedCells[0];

		this.onDblClick({target : selectedCell});
	}
	this.copyCell = function() {
        const selectedText = this.getSelectedTextForExcel();
        if (selectedText != null) this.copyToClipboard(selectedText);
	}
	this.pasteCell = function() {
	    navigator.clipboard.readText()
	        .then(clipboardData => {
		        thisCls.pasteStartCell = $(thisCls.preSelectedCells[0]);
	            thisCls.pasteFromClipboard(clipboardData);
	        })
	        .catch(err => {
	            console.error('클립보드에서 데이터를 읽는 데 실패했습니다:', err);
        });
	}
	this.clearState = function() {
        $(`#${this.gridId} tr`).each(function() {
        	$(this).attr("__state", "default"	);
		});
	};
	
    this.setGridIndex = function(gridId, type) {
        this.gridId = gridId;
        let rowCounter = 1;
        const rowSpans = [];

        $(`#${gridId} tr`).each(function() {
            let colCounter = 1;

            while (rowSpans[colCounter] && rowSpans[colCounter] > 0) {
                rowSpans[colCounter]--;
                colCounter++;
            }
            
            // state값이 초기 'DEFAULT' 설정 (init에서 호출된 경우만)
            if(type == 'init' || type== 'new') {
				let uid = C_COM.getUniqueId();
            	$(this).attr("uid"	, uid		);
				if(type == "new") {
	            	$(this).attr("__state", "insert"	);
				} else {
	            	$(this).attr("__state", "default"	);
				}
            }
			
			$(this).attr("data-row", rowCounter);
			
            $(this).children('.explan-grid-cell').each(function() {
                while (rowSpans[colCounter] && rowSpans[colCounter] > 0) {
                    colCounter++;
                }

                $(this).attr('data-row', rowCounter);
                $(this).attr('data-col', colCounter);

                const rowspan = parseInt($(this).attr('rowspan')) || 1;
                const colspan = parseInt($(this).attr('colspan')) || 1;

                for (let i = 0; i < colspan; i++) {
                    rowSpans[colCounter + i] = rowspan - 1;
                }

                colCounter += colspan;
            });

            rowCounter++;
        });

		this.setNumberColumn()

        //$(`#${this.gridId}`).off('click').on('click', '.explan-grid-cell', this.onClick.bind(this));
    };

	this.setNumberColumn = function() {
		const numberColIdx = this.parm.numberColIdx;
		if(isValid(numberColIdx)) {
			$(`#${this.gridId} td[data-col=${numberColIdx}]`).each(function(idx) {
				$(this).html(idx + 1).attr("value", (idx + 1));
			});
		}
	}

    this.clearSelection = function(event) {
		if(isEmpty(event) || !event.shiftKey) {
	        $(`#${this.gridId} .explan-grid-cell`).removeClass("explan-selected");

			/*
			if(this.columnConfig.remainLastSelectedCell == "Y" && this.selectedCells.length == 1) {
		        $(`#${this.gridId} .explan-grid-cell`).removeClass("explan-selected");
		        $(`#${this.gridId} .explan-grid-cell`).closest("tr").removeClass("explan-selected-tr");
	            $(this.selectedCells[0]).addClass("explan-selected");
	            $(this.selectedCells[0]).closest("tr").addClass("explan-selected-tr");

			} else {

		        $(`#${this.gridId} .explan-grid-cell`).removeClass("explan-selected");
		        $(`#${this.gridId} .explan-grid-cell`).closest("tr").removeClass("explan-selected-tr");
		        this.pasteStartCell = null;
				this.preSelectedCells = this.selectedCells; 
		        this.selectedCells = [];
			}
			*/ 
		}
    };

    this.selectCellsWithinRectangle = function(start, end) {
        const startRow = Math.min(parseInt(start.attr("data-row")), parseInt(end.attr("data-row")));
        const endRow = Math.max(parseInt(start.attr("data-row")), parseInt(end.attr("data-row")));
        const startCol = Math.min(parseInt(start.attr("data-col")), parseInt(end.attr("data-col")));
        const endCol = Math.max(parseInt(start.attr("data-col")), parseInt(end.attr("data-col")));

        $(`#${this.gridId} .explan-grid-cell`).each(function() {
			if($(this).prop("tagName") == "button") return true;
            const $cell = $(this);
            const row = parseInt($cell.attr("data-row"));
            const col = parseInt($cell.attr("data-col"));
            const isInRectangle = row >= startRow && row <= endRow && col >= startCol && col <= endCol;
            $cell.toggleClass("explan-selected", isInRectangle);
        });
    };

    this.onMouseDown = function(event) {
		let targetDom = this.getTdDom(event.target);

		if(isEmpty(targetDom)) return;
		
    	if (event.shiftKey && this.selectedCells.length > 0) {
            const endCell = $(targetDom);
            this.clearSelection(event);
            this.selectCellsWithinRectangle(this.startCell, endCell);
            this.isDragging = false;  // 드래그 상태를 초기화
            this.selectedCells = $(`#${this.gridId} .explan-grid-cell.explan-selected`).toArray();
    	} else {
			let tagName = $(targetDom).prop("tagName");
			if(tagName.toUpperCase() == "SELECT") return;
				
	        if (!$(targetDom).closest(`#${this.gridId}`).length) return;
	        C_GRID.clearSelection(this.gridId);
	        this.isDragging = true;
	        this.startCell = $(targetDom);
			this.preSelectedCells = this.selectedCells; 
	        this.selectedCells = [];
	        this.selectCellsWithinRectangle(this.startCell, this.startCell);
		}
    };

    this.onMouseMove = function(event) {
		let targetDom = this.getTdDom(event.target);

		if(isEmpty(targetDom)) return;

		let tagName = $(targetDom).prop("tagName");
		if(tagName.toUpperCase() == "SELECT") return;
        if (!$(targetDom).closest(`#${this.gridId}`).length) return;
        if (this.isDragging) {
            const endCell = $(targetDom);
            this.clearSelection(event);
            this.selectCellsWithinRectangle(this.startCell, endCell);
        }
    };

    this.onMouseUp = function(event) {
		let targetDom = this.getTdDom(event.target);
		if(isEmpty(targetDom)) return;
		let tagName = $(targetDom).prop("tagName");
		if(tagName.toUpperCase() == "SELECT") return;
        if (!$(targetDom).closest(`#${this.gridId}`).length) return;
        if (this.isDragging) {
            this.isDragging = false;
            this.selectedCells = $(`#${this.gridId} .explan-grid-cell.explan-selected`).toArray();
            C_GRID.setCurrentSelectedGridId(this.gridId);
        }
    };
	this.getTdDom = function(targetDom) {
		let tagName = $(targetDom).prop("tagName");
		
		if(isEmpty(tagName)) return null;
		
		if(tagName.toUpperCase() == "SELECT") return targetDom;
		
		if(!in_array(tagName.toUpperCase(), ["TD", "TH"])) {
			tagName = $(targetDom).parent().prop("tagName");	
			if(!in_array(tagName.toUpperCase(), ["TD", "TH"])) return targetDom;
			
			if(isValid(targetDom.parentNode)) 	return targetDom.parentNode
			else								return $(targetDom).parent()
		} else {
			return targetDom;
		}
	}
	this.selectedProcess = function(targetDom) {

        $(`#${this.gridId} .explan-grid-cell`).removeClass("explan-selected");
        $(`#${this.gridId} .explan-grid-cell`).closest("tr").removeClass("explan-selected-tr");
		
        $(targetDom).addClass("explan-selected");
        $(targetDom).closest("tr").addClass("explan-selected-tr");
        this.selectedCells = [targetDom];
        this.pasteStartCell = $(targetDom);
	} 
    this.onClick = function(event) {
	
		// shift Key 누른상태에서는 동작 안함.
        if (event.shiftKey) return;

		let tagName = $(event.target).prop("tagName");

		if(in_array(tagName.toUpperCase(), ["SELECT"])) {
			return;
		}

		let targetDom = this.getTdDom(event.target);
		if(isEmpty(targetDom)) return;

		tagName = $(targetDom).prop("tagName");
		
		
        if (!$(targetDom).closest(`#${this.gridId}`).length && !$(targetDom).closest('.explan-layer-popup').length) return;
		

        if (!this.isDragging) {
            this.clearSelection(event);
			this.selectedProcess(targetDom);
        }

		this.onClickRow(targetDom);

		let rowData = this.getRowData(targetDom);			

        let tdIndex = this.getIndex(targetDom);
        let columnInfo = this.columnInfo.columnArr[tdIndex];
		
		if(typeof this.columnConfig.onClickCell == "function") this.columnConfig.onClickCell(columnInfo.column, rowData);
    };

	this.onClickRow = function(targetCell) {
		let fnClickRow = this.rowConfig.onClickRow;

		if( typeof fnClickRow == "function") {
			
			let returnObj = this.getRowData(targetCell);			
			
			fnClickRow(returnObj);
		}
	}

    this.onDocumentMouseDown = function(event) {
        if ($(event.target).closest('.explan-grid-cell, .explan-layer-popup').length === 0) {
            this.clearSelection(event);
        }
    };
	this.deleteCell = function(selectedCells) {

    	// 읽기 전용인경우 삭제 방지
    	if(this.readOnly == "Y") return;
		
		// 현재 inputbox에 포커싱 되었을때 실행 방지
		if ($(document.activeElement).is('input')) return;
		
		let newSelectedCells = []
		$.each(selectedCells, function() {
			let columnInfo = thisCls.getColumnInfo(this);
			if(columnInfo.columnType == "readOnly" || columnInfo.readOnly == "Y") return true;
			newSelectedCells.push(this);
		});
        this.backupCellsState(newSelectedCells);
        newSelectedCells.forEach(cell => {
            thisCls.setCell(cell, '');
        });
	}
    this.onKeyDown = function(event) {

        let eventKey = event.key.toLowerCase();
    	
    	if(this.gridId != C_GRID.currentSelectedGridId) return;
        if (event.ctrlKey && eventKey === 'c') {
            const selectedText = this.getSelectedTextForExcel();
            if (selectedText != null) this.copyToClipboard(selectedText);
        }
        if (eventKey === 'delete' && this.selectedCells.length > 0) {
			this.deleteCell(this.selectedCells);
        }
        if (event.ctrlKey && eventKey === 'z') this.undo();
        if (event.ctrlKey && eventKey === 'y') this.redo();
        if (event.ctrlKey && eventKey === 'i') this.insertRow();
        if (event.ctrlKey && eventKey === 'e') {
			this.editCell();
			return false;
		}
        if (event.ctrlKey && eventKey === 'a') {
        	event.preventDefault();
        	this.addRow();
        }
        if (event.ctrlKey && eventKey === 'd') {
        	event.preventDefault();
            this.deleteRow();
        }
    };

    this.onPaste = function(event) {
    	
    	// 읽기 전용인경우 붙여넣기 방지
    	if(this.readOnly == "Y") return;
    	
    	if(this.gridId != C_GRID.currentSelectedGridId) return;
        const clipboardData = event.originalEvent.clipboardData.getData('text');
        this.pasteFromClipboard(clipboardData);
    };

    this.getSelectedTextForExcel = function() {
        if (this.selectedCells.length < 1) return null;

        if (this.selectedCells.some(cell => $(cell).find('input').length > 0)) return null;

        this.selectedCells.sort((a, b) => {
            const rowDiff = parseInt($(a).attr("data-row")) - parseInt($(b).attr("data-row"));
            if (rowDiff !== 0) return rowDiff;
            return parseInt($(a).attr("data-col")) - parseInt($(b).attr("data-col"));
        });

        let rows 		= {};
		let startCol 	= -1;
        this.selectedCells.forEach(cell => {
            const row = $(cell).attr("data-row");
            const col = $(cell).attr("data-col");
			if(startCol == -1) startCol = col;

            if (!rows[row]) rows[row] = [];

            let data = $(cell).attr("value");

            if (data.includes('\t') || data.includes('\n')) data = `"${data}"`;
            
            rows[row][col - startCol] = data;
        });

		$.each(rows, function(key, list) {
			$.each(list, function(num, val) {
				if(val == null)rows[key][num] = "";
			});
		});

        const rowKeys = Object.keys(rows).sort((a, b) => a - b);
        return rowKeys.map(rowKey => {
            const cols = rows[rowKey];
            const colKeys = Object.keys(cols).sort((a, b) => a - b);
            return colKeys.map(colKey => cols[colKey]).join('\t');
        }).join('\n');
    };

    this.copyToClipboard = function(text) {
		if (navigator.clipboard && window.isSecureContext) {
			// 클립보드 API 사용
			navigator.clipboard.writeText(text)
			  .then(() => {
			    console.log('텍스트가 성공적으로 복사되었습니다!');
			  })
			  .catch(err => {
			    console.error('텍스트 복사 실패: ', err);
			  });
		} else {
			// 오래된 브라우저의 경우를 대비한 폴백
			const $tempInput = document.createElement('input');
			$tempInput.type = 'text';
			$tempInput.value = text;
			document.body.appendChild($tempInput);
			$tempInput.select();
			document.execCommand('copy');
			document.body.removeChild($tempInput);
		}
    };

	this.parseExcelType = function(input) {
	    // 정규 표현식: 따옴표로 묶인 문자열과 묶이지 않은 문자열을 분리
	    const regex = /"([^"]*)"/g;
	    let modifiedInput = input;
	
	    // 따옴표로 묶인 부분을 찾고 \n을 [nl]로 바꾸기
	    modifiedInput = modifiedInput.replace(regex, (match, p1) => {
	        // p1: 따옴표 안의 내용
	        const modifiedContent = p1.replace(/\n/g, '[nl]');
	        return modifiedContent;
	    });
	
	    return modifiedInput;
	    
	}
	this.pasteFromClipboard = function(clipboardData) {
        if (!this.pasteStartCell) return;

		clipboardData = this.parseExcelType(clipboardData);
		
        const lines = clipboardData.split("\n");

		if(lines.length == 1) {
			const words = lines[0].split("\t");
			if(words.length == 1 && this.selectedCells.length > 1) {
				this.pasteFromClipboardSingleData(words[0]);
				return;
			} 
		}
		this.pasteFromClipboardMultiData(clipboardData, lines)
	}
	this.pasteFromClipboardSingleData = function(word) {
		
		this.backupCellsState(this.selectedCells);
		
		$.each(this.selectedCells, function() {
			const targetCell = this;
			thisCls.setCell(targetCell, word);
		});
	}
	this.pasteFromClipboardMultiData = function(clipboardData, lines) {

        this.backupCellsState(this.getCellsToChange(clipboardData));

        const startRow = parseInt($(this.pasteStartCell).attr("data-row"));
        const startCol = parseInt($(this.pasteStartCell).attr("data-col"));

        lines.forEach((line, rowIndex) => {
			if(isEmpty(line)) return true;
            // row __state 설정
            const targetRow = startRow + rowIndex;
            
            let nowState = $(`#${this.gridId} tr[data-row=${targetRow}]`).attr("__state");
            
            if(nowState == "default") {
            	$(`#${this.gridId} tr[data-row=${targetRow}]`).attr("__state", "update");
            }
            
            const cells = line.split("\t");
            cells.forEach((cellText, colIndex) => {
                const targetCol = startCol + colIndex;
				
				const columnInfo = thisCls.columnInfo.columnArr[targetCol - 1];
				
				// 컬럼 정보가 없으면 중단한다.
				if(isEmpty(columnInfo)) return false;
				
				if(columnInfo.columnType == "readOnly" || columnInfo.readOnly == "Y") return true;

                const targetCell = $(`#${thisCls.gridId} .explan-grid-cell[data-row=${targetRow}][data-col=${targetCol}]`);
                cellText = cellText.replaceAll("[nl]", "</br>");
                if (targetCell.length) {
	                thisCls.setCell(targetCell, cellText);
                } else {
		            const tableBody = $(`#${thisCls.gridId} tbody`);
		            const lastRow = tableBody.find('tr').last();
		            const firstCellInLastRow = lastRow.find('.explan-grid-cell[data-col=1]');
		
		            // 선택 표시 지우기
		            thisCls.clearSelection();
					
		            // 마지막 줄의 첫 번째 셀을 선택된 상태로 만듭니다
		            firstCellInLastRow.addClass('selected');
		            thisCls.selectedCells.push(firstCellInLastRow[0]);
		            
		            thisCls.addNewRow({});
		            
	                const targetCell = $(`#${thisCls.gridId} .explan-grid-cell[data-row=${targetRow}][data-col=${targetCol}]`);
	                thisCls.setCell(targetCell, cellText);
                }
            });
        });
    };

	this.setRow = function(targetRowDom, rowData) {
		let lastRowNum = this.getLastRowNum();
		$(targetRowDom).find("td").each(function() {
			let tdDom = this;
			let columnInfo = thisCls.getColumnInfo(tdDom);
			$.each(rowData, function(key, val) {
				if(columnInfo.column == key) {
					if(key == "num") val = lastRowNum;
					thisCls.setCell(tdDom, val);
				}
			});
		});
		// hidden Column처리
		$.each(this.hiddenColumnList, function() {
			let val = rowData[this.COLUMN_ID];
			if(isValid(val)) {
				$(targetRowDom).attr(this.COLUMN_ID, val);	
			}
		});
	}
	
	this.getLastRowNum = function() {
		return $(`#${this.gridId} tbody tr`).length;
	}
	this.getTotalRow = function() {
		return $(`#${this.gridId} tbody tr`).length;
	}
	
	this.checkEmptyGrid = function() {
		let gridDataList = C_GRID.getGridMainData(this.gridId);
		let emptyCheck = true;
		if(gridDataList.length == 1) {
			let rowData = gridDataList[0]
			$.each(this.columnMap, function(key, obj) {
				
				if(isValid(rowData[key]) && obj.hidden != "Y") {
					emptyCheck = false;
					return false;
				}
			});
		} else {
			emptyCheck = false;
		}
		return emptyCheck;
	}
	
	this.addNewRow = function(rowData) {
        const tableBody = $(`#${this.gridId} tbody`);
        const lastRow = tableBody.find('tr').last();
        const firstCellInLastRow = lastRow.find('.explan-grid-cell[data-col=3]');
		
		let emptyCheck = this.checkEmptyGrid();

		//if(emptyCheck) this.addRow();
		
        // 선택 표시 지우기
        //this.clearSelection();
		
        // 마지막 줄의 첫 번째 셀을 선택된 상태로 만듭니다
		this.selectedProcess(firstCellInLastRow);
        //firstCellInLastRow.addClass('selected');
        //this.selectedCells.push(firstCellInLastRow[0]);
        
        let newRow = this.addRow();

		this.setRow(newRow, rowData)
		
		//if(emptyCheck) lastRow.remove();

		if(emptyCheck) this.deleteRow([firstCellInLastRow]);

        // 선택 표시 지우기
        this.clearSelection();
	}

    this.setCellToColumn = function(targetDom, cellText, columnId) {
		this.backupCellsState([targetDom]);

		if(isEmpty(cellText)) cellText = "";
		// hidden Column처리
		let hdn = false;
		$.each(this.hiddenColumnList, function() {
			if(this.COLUMN_ID == columnId) {
				let $tr = $(targetDom).closest("tr");
				$tr.attr(columnId, cellText);
				hdn = true;
			}
		});
		
		if(hdn) return;
		
		let columnInfo = this.getColumnInfo(columnId);
		let cidx = columnInfo.seq;
		let ridx = $(targetDom).attr("data-row");
		this.setCell(ridx, cidx, cellText);
	}

    this.setCellDirect = function(targetDom, cellText) {
		this.backupCellsState([targetDom]);
		this.setCell(targetDom, cellText)
	}
    this.setCell = function() {
		if(typeof arguments[0] == "object") {
			// targetCell, cellText, initType
			this.setCellForObject(arguments[0], arguments[1], arguments[2]);
		} else {
			// rowIndex, columnIndex, cellText
			this.setCellForIndex(arguments[0], arguments[1], arguments[2]);
		}
	}	
	this.setCellForIndex  = function(rowIndex, columnIndex, cellText) {
	    const targetCell = $(`#${this.gridId} .explan-grid-cell[data-row=${rowIndex}][data-col=${columnIndex}]`);
		this.setCellForObject(targetCell, cellText);
	}
	
	
	// Cell 설정값을 읽는다.
	this.getCellColumnInfo = function(targetCell) {
		if(isEmpty(this.cellConfig)) return;
		const cellDataTypeList = this.cellConfig.cellDataTypeList;
		if(isEmpty(cellDataTypeList)) return;

		const rowData = this.getRowData(targetCell);

		let key 		= "";
		let columnId	= $(targetCell).attr("columnId");

		$.each(this.primaryKeyList, function() {
			if(isValid(key)) key += "_";
			key += `${rowData[this]}`;
		});
		let returnColumnInfo;
		$.each(cellDataTypeList, function() {
			if(key == this.key && columnId == this.columnId) {
				returnColumnInfo = this;
				return false;
			}
		});
		if(isEmpty(returnColumnInfo)) return;
		
		if(returnColumnInfo.columnType == "selectbox") {
			if(isEmpty(returnColumnInfo.directCodeMap)) {
				let directCodeList = [];
				if(isEmpty(directCodeList)) {
					if		(isValid(returnColumnInfo.useCodeId		)) directCodeList = C_COM.getCodeList		(returnColumnInfo.useCodeId);
					else if (isValid(returnColumnInfo.useBrandCodeId)) directCodeList = C_COM.getBrandCodeList	(G_VAL.session.BRAND_ID, returnColumnInfo.useBrandCodeId);
				}
				let directCodeMap = {}
				$.each(directCodeList, function() {
					directCodeMap[this[0]] = this[1];
				});
				returnColumnInfo.directCodeList	= directCodeList;
				returnColumnInfo.directCodeMap	= directCodeMap;
			}
		}
		return returnColumnInfo;
	}
    // Cell에 Type에 따라 값을 입력 한다.
    this.setCellForObject = function(targetCell, cellText, initType) {
	
		if(isValid(cellText)) cellText = (new String(cellText)).trim();
		
		targetCell = this.getTdDom(targetCell);	

		if(isEmpty(targetCell)) return;
		
		$targetCell = $(targetCell);
		
        let tdIndex = Number($targetCell.attr("data-col")) - 1;
		
		if(tdIndex < 0) return;
		let curCellVal = $targetCell.attr("value");

        let columnInfo = this.getCellColumnInfo(targetCell);
		if(isEmpty(columnInfo)) columnInfo = this.columnInfo.columnArr[tdIndex];
		if(isEmpty(columnInfo)) return;

		let dataType = columnInfo.dataType
		
		if(dataType == "number") {
			cellText = cellText.replaceAll(",", "");
		}

		let viewText = cellText;
		if			(columnInfo.columnType == "text") {

			if(dataType == "number")	{
				let fix = columnInfo.fix;
				if(isEmpty(fix)) fix = 0;
				viewText = addComma(toNumber(viewText, fix))
			}

		} else if	(columnInfo.columnType == "button") {
			
			if(isValid(columnInfo.buttonNm)) viewText = columnInfo.buttonNm;
			
			C_GRID.registColumnFn(this.gridId, columnInfo.column, columnInfo.buttonFn);
			
			viewText = `<button class="outline-btn col10" type="button" onClick="C_GRID.execColumnFn('${this.gridId}', '${columnInfo.column}', this)">${viewText}</button>`;
			
		} else if	(columnInfo.columnType == "selectbox") {
			let key 		= columnInfo.column; 
			
			if(isValid(columnInfo.directCodeMap)) {
				viewText	= columnInfo.directCodeMap[cellText];
			} else {
				if(isEmpty(thisCls.columnConfig.selectBoxCodeMap[key])) thisCls.columnConfig.selectBoxCodeMap[key] = {}
				viewText	= thisCls.columnConfig.selectBoxCodeMap[key][cellText];
			}
			
			if(isEmpty(viewText)) 			viewText = "&nbsp;";
			else if(dataType == "number")	{
				let fix = columnInfo.fix;
				if(isEmpty(fix)) fix = 0;
				viewText = addComma(toNumber(viewText, fix))
			}
			
			if(this.readOnly != "Y" && columnInfo.readOnly != "Y") viewText = `<span class="td-cnt">${viewText}</span><button type="button" class="arr-dn-btn" onclick="C_GRID.dblClickTrigger('${this.gridId}', this)"></button>`;	
		} else if	(columnInfo.columnType == "popup") {
			if(isEmpty(viewText)) viewText = "&nbsp;";

			if(isEmpty(viewText)) 			viewText = "&nbsp;";
			else if(dataType == "number")	{
				let fix = columnInfo.fix;
				if(isEmpty(fix)) fix = 0;
				viewText = addComma(toNumber(viewText, fix))
			}

			if(this.readOnly != "Y") viewText = `<span class="td-cnt">${viewText}</span><button type="button" data-toggle="modal" href="#myModal2" class="srch-btn01" onclick="C_GRID.dblClickTrigger('${this.gridId}', this)"></button>`;
		} else if	(columnInfo.columnType == "password") {
			let len = cellText.length;
			viewText = "";
			for(let ii=0;ii<len;ii++) {
				viewText += "*";	
			}
		} else {
			if(dataType == "number")	{
				let fix = columnInfo.fix;
				if(isEmpty(fix)) fix = 0;
				viewText = addComma(toNumber(viewText, fix))
			}
		}

		if(isValid(columnInfo.column) && isValid(thisCls.columnConfig.makeCellViewData) && typeof thisCls.columnConfig.makeCellViewData[columnInfo.column] == "function") {
			const rowData = this.getRowData(targetCell);
			viewText = thisCls.columnConfig.makeCellViewData[columnInfo.column](cellText, rowData);
		}
		
		$targetCell.html(viewText			);
        $targetCell.attr("value", cellText	);
        $targetCell.attr("title", cellText	);
		
		// Cell이 이전값과 다른경우, onChangeCell Event가 등록되어 있다면 실행 한다.
		if( curCellVal != cellText) {
			if(isValid(thisCls.columnConfig.onChangeCell) && isValid(thisCls.columnConfig.onChangeCell[columnInfo.column])) {
				if( typeof thisCls.columnConfig.onChangeCell[columnInfo.column].func == "function") {
					let rowIdx = $targetCell.attr("data-row");
					let colIdx = $targetCell.attr("data-col");
					
					// 최초 세팅시 Change Event를 막는 설정이 있는 경우 실행 안함.
					let execInit = thisCls.columnConfig.onChangeCell[columnInfo.column].execInit;
					if(isEmpty(execInit)) execInit = "N"
					if(initType != 'init' || execInit == "Y") {
						let rowData = this.getRowData(targetCell)
						thisCls.columnConfig.onChangeCell[columnInfo.column].func(thisCls, Number(rowIdx), Number(colIdx), targetCell, rowData);
					}
				}
			}
			// 상태 업데이트
			if(initType != 'init' && initType != 'new') {
				// 처음로딩이 아니면 상태 변경 한다.
				let state = $targetCell.parent().attr("__state");
				if(state == "default") $targetCell.parent().attr("__state", "update");
			} 
		}
		
		
    };

	this.trigger = function(eventName, columnId) {
		const evt = this.columnConfig[eventName];
		if(isEmpty(evt) || isEmpty(evt[columnId]) || typeof evt[columnId].func != "function") {
			C_POP.alert(`등록된 Event가 없습니다.`);
			return;
		}
		
		const targetCell = this.selectedCells[0];
		
		let rowIdx = $(targetCell).attr("data-row");
		let colIdx = $(targetCell).attr("data-col");
		
		let rowData = this.getRowData(targetCell)

		evt[columnId].func(thisCls, Number(rowIdx), Number(colIdx), targetCell, rowData);
	};

    this.backupCellsState = function(cells) {
        if (cells.length === 0) return;

        const lastState = this.undoStack[this.undoStack.length - 1];
        const newState = cells.map(cell => ({
        	type	: "cell",
            row		: $(cell).attr("data-row"),
            col		: $(cell).attr("data-col"),
            text	: $(cell).attr("value"),
        }));

        if (!lastState || JSON.stringify(lastState) !== JSON.stringify(newState)) {
            this.undoStack.push(newState);
            this.redoStack = []; // 삭제 시 redo 스택 초기화
        }
    };

    this.getCellsToChange = function(clipboardData) {
        const lines = clipboardData.split("\n");
        const startRow = parseInt(this.pasteStartCell.attr("data-row"));
        const startCol = parseInt(this.pasteStartCell.attr("data-col"));
        let cells = [];

        lines.forEach((line, rowIndex) => {
            const lineCells = line.split("\t");
            lineCells.forEach((_, colIndex) => {
                const targetRow = startRow + rowIndex;
                const targetCol = startCol + colIndex;
                const targetCell = $(`#${this.gridId} .explan-grid-cell[data-row=${targetRow}][data-col=${targetCol}]`);
                if (targetCell.length) {
                    cells.push(targetCell[0]);
                }
            });
        });

        return cells;
    };

	this.onDblClickRow = function(targetCell) {
		let fnClickRow = this.rowConfig.onDblClickRow;
		if( typeof fnClickRow == "function") {
			let rowData = this.getRowData(targetCell);
			fnClickRow(rowData);
		}
	}
	this.getIndex = function(targetDom) {
        let tdIndex = Number($(targetDom).attr("data-col") - 1);
		return tdIndex;
	}
    this.onDblClick = function(event) {

		let targetDom = this.getTdDom(event.target);
		
		if(isEmpty(targetDom)) return;
		
		const tagName = $(targetDom).prop("tagName");

		if(tagName.toUpperCase() == "TH") return;

        let tdIndex = this.getIndex(targetDom);
        let columnInfo = this.getCellColumnInfo(targetDom);
		if(isEmpty(columnInfo)) columnInfo = this.columnInfo.columnArr[tdIndex];
		if(isEmpty(columnInfo)) return;

    	// 읽기 전용인경우 키인 방지
    	if(this.readOnly == "Y") {

			this.onDblClickRow(targetDom);

			let rowData = this.getRowData(targetDom);			

			if(typeof this.columnConfig.onDblClickCell == "function") this.columnConfig.onDblClickCell(columnInfo.column, rowData);

			return;
		} 

        if (!$(targetDom).closest(`#${this.gridId}`).length && !$(targetDom).closest('.explan-layer-popup').length) return;

        this.clearSelection(event);

        const cellWidth = $(targetDom).width();
        const cellHeight = $(targetDom).height();

        this.backupCellsState([targetDom]);

		if(columnInfo.columnType == "readOnly" || columnInfo.readOnly == "Y") {
			return;
		} else if		(columnInfo.columnType == "text") {
			this.setTextboxToCell(event, columnInfo)
		} else if	(columnInfo.columnType == "selectbox") {
			this.setSelectboxToCell(targetDom, columnInfo)
		} else if	(columnInfo.columnType == "popup") {
			this.openPopup(targetDom, columnInfo)
		} else {
			this.setTextboxToCell(event, columnInfo)
		}

    };
    this.openPopup = function(targetDom, columnInfo) {
		let column = columnInfo.column;
		let popupConfig = this.columnConfig.popupConfig[column];
		if(isEmpty(popupConfig)) {
			C_POP.alert('popupConfig 설정이 필요합니다.');
			return;			
		}
		let popupId	= popupConfig.popupId;
		
		let parm	= this.getRowData(targetDom);
		
		if( typeof popupConfig.parmFn == "function") parm = $.extend(parm, popupConfig.parmFn()); 
		 
		C_POP.open(popupId, parm, function(retData) {
			if( typeof popupConfig.resultFn == "function") {
				flag = popupConfig.resultFn(thisCls, targetDom, retData);
				if(flag === true && isValid(retData)) {
					thisCls.setCell($(targetDom), retData);				
				}
			} else {
				if(isValid(retData)) {
					thisCls.setCell($(targetDom), retData);				
				}
			} 
		});
	}
    this.setSelectboxToCell = function(targetDom, columnInfo) {
    	let selectList = [];
		if(isValid(columnInfo.directCodeList))	selectList = columnInfo.directCodeList;
		else									selectList = this.columnConfig.selectBoxCodeList[columnInfo.column];
		
		let selectBoxStr = '';
		selectBoxStr += `<select class="form-control form-control-sm col10" onEnter=""><option value=" "> </option>`;
		$.each(selectList, function() {
			let cd 	= this[0];
			let txt	= this[1];
			selectBoxStr += `<option value='${cd}'>${txt}</option>`;
		});
		selectBoxStr += `</select>`;
		let rval = $(targetDom).attr("value");
		if(isEmpty(rval)) rval = $(targetDom).text();
		const $selectBox = $(selectBoxStr);
		$selectBox.find("option").each(function() {
			if($(this).val() == rval) $(this).attr("selected", true);
		});
        $(targetDom).html($selectBox);
        
        $selectBox.on('blur', function() {
        	thisCls.setCell($(targetDom), this.value);
        });
        
        $selectBox.focus();

		let onEnter = $selectBox.attr("onEnter");
		if(isEmpty(onEnter)) onEnter = $selectBox.attr("OnEnter");
		if(isEmpty(onEnter)) onEnter = $selectBox.attr("onenter");
		if(isValid(onEnter)) {
			
			$selectBox.unbind("keyup");
			$selectBox.bind("keyup",function(event) {
				if(event.keyCode == C_COM.KEY_CD.ENTER) {
					eval(onEnter);
				}
			});
		}

    }
    
	this.setTextboxToCell = function(event, columnInfo) {

		let itxt = $(event.target).attr("value");
		
		let inputCompStr = "";
		
		if			(columnInfo.dataType == "number") {
			let fix = columnInfo.fix;
			if(isEmpty(fix)) fix = "0";
			inputCompStr = `<input type='text' class='form-control form-control-sm col10' style="padding:5" onclick="C_GRID.clickInputProcess('${this.gridId}')" number fix="${fix}"/>`;
		} else if	(columnInfo.dataType == "date") {
			inputCompStr = `<input type="date" class="form-control form-control-sm col10 calender-form" onBlur="C_GRID.onDateInputBlur('${this.gridId}', this)">`;			
		} else {
			inputCompStr = `<input type='text' class='form-control form-control-sm col10' style="padding:5" onclick="C_GRID.clickInputProcess('${this.gridId}')"/>`;
		}
        const $input = $(inputCompStr);
		
		if (columnInfo.dataType == "number") 	$input.val(addComma(itxt)); 
		else 									$input.val(itxt);
		
        $(event.target).html($input);

		if(columnInfo.dataType == "number") {
			C_COM.makeNumberTypeToInput(event.target);
		}

        $input.focus();
        var inputLength = $input.val().length;
        $input[0].setSelectionRange(inputLength, inputLength);
        const $cell = $(event.target);

        $input.on('paste', function(event) {
            const clipboardData = event.originalEvent.clipboardData.getData('text');
            const textBefore = $input.attr("value");
            const cursorPosition = $input[0].selectionStart;
            const textAfter = textBefore.slice(0, cursorPosition) + clipboardData + textBefore.slice(cursorPosition);
            
            thisCls.setCell($input, textAfter);

            return false;
        });

        $input.on('copy', function(event) {
            const text = window.getSelection().toString();
            event.originalEvent.clipboardData.setData('text', text);
            return false;
        });

        $input.on('keydown', function(event) {
	        let eventKey = event.key.toLowerCase();

            if ((event.ctrlKey || event.metaKey) && eventKey === 'c') {
                event.preventDefault();
                document.execCommand('copy');
            } else if ((event.ctrlKey || event.metaKey) && eventKey === 'v') {
                return;
            } else if (eventKey === 'enter') {
                const newText = $(this).val();

                thisCls.setCell($cell, newText);
                
                // 상위 tr update 설정
		        let parentTr = $cell.closest('tr'); // 상위 tr 요소 찾기
		        let __state = parentTr.attr("__state"); // __state 속성 값 가져오기
		        if(__state === "default") parentTr.attr("__state", "update");
            }
        });
		
		C_GRID.lockGridEvent();
        $input.blur(function() {

			C_GRID.unlockGridEvent();

            const newText = $(this).val();
			
            thisCls.setCell($cell, newText);

            // 상위 tr update 설정
	        let parentTr = $cell.closest('tr'); // 상위 tr 요소 찾기
	        let __state = parentTr.attr("__state"); // __state 속성 값 가져오기
	        if(__state === "default") parentTr.attr("__state", "update");
        });

		C_UICOM.addListnerRightIconClick($input);
	} 


    // 행추가
    this.addRow = function() {
    	return this.insertRow("A");
    }
    this.insertRow = function(type) {

    	// 읽기 전용인경우 열삽입 방지
    	if(this.readOnly == "Y") return;
	
		// 최초 Data가 없을경우 처리

		if(isValid(this.tableInitRow)) {
			this.tableInitRow = undefined;
			if(typeof this.noDataFirstAddFn == "function") this.noDataFirstAddFn();
			return;
		}

    	// 업데이트 전용인경우 열삽입 방지
    	if(this.updateOnly == "Y") {
			C_POP.alert('추가/삭제는 금지되어 있습니다. 수정만 가능합니다.')
			return;
		}
		
		let rowcnt = $("#" + this.gridId).find('tr').length;
		if(rowcnt >= 1000) {
			C_POP.alert('Grid Row는 1000개이상 추가 할 수 없습니다.');
			return;
		}
		
    	if(isEmpty(type)) type = "I";
        const selectedCell = this.selectedCells[0];
        if (selectedCell) {
        	
            let row 		= $(selectedCell).closest('tr');
            let newRow		= {};

       		newRow 	= row.clone(); // 현재 선택된 행 복사
            
            $(newRow).attr("__state"	, "insert"); // 신규 Row state값 설정
        	let uid = C_COM.getUniqueId();
            $(newRow).attr("uid", uid); // 신규 Row state값 설정

			$.each(this.hiddenColumnList, function() {
				let val = "";
				if(isValid(this["ifNewAutoSet"])) {
					if(this["ifNewAutoSet"] == "random" || this["ifNewAutoSet"] == "Y" ) {
						val = uid;
					} else if( typeof this["ifNewAutoSet"] == "function" ) {
						val = this["ifNewAutoSet"]();
					}
				} else if(isValid(this["default"])) {
					let defaultObj = this["default"];
					if(typeof defaultObj == "function") val = defaultObj(this["COLUMN_ID"], {}); 
					else 								val = defaultObj;
				}
	            $(newRow).attr(this["COLUMN_ID"], val); // 신규 Row state값 설정
			});
            newRow.find('.explan-grid-cell').each(function() {
				const columnInfo 	= thisCls.getColumnInfo(this);
				
				let val = "";
				if(isValid(thisCls.columnMap[columnInfo.column])) {
					let defaultObj = thisCls.columnMap[columnInfo.column]["default"];
					if(isValid(defaultObj)) {
						if(typeof defaultObj == "function") val = defaultObj("", {}); 
						else 								val = defaultObj;
					}
				}
				$(this).attr('basevalue', val);
				if(columnInfo.column != "checkbox") {
					// 새로운 행의 모든 셀 초기화
					$(this).removeClass('explan-selected');
					thisCls.setCell($(this), val, 'init');
				}

			});
            if(type == "I" ) row.before(newRow); // 새로운 행 삽입
            if(type == "A" ) row.after(newRow); // 새로운 행 추가

            this.setGridIndex(this.gridId); // 그리드 인덱스 업데이트
           
            this.undoStack = [];
            this.redoStack = [];

			return newRow;
        } else {
            //alert("선택된 셀이 없습니다.");
        }
		this.gridIsEmpty = false;

    };

	this.setUpdatableAllRow = function() {
		$(`#${gridId} tbody tr`).each(function() {
			let state = $(this).attr("__state");			
			if(state == "default") $(this).attr("__state", "update");			
		});
	} 
    
    this.isEmpty = function() {
		let gridDataList = C_GRID.getGridMainData(this.gridId);
		if(gridDataList.length == 1 && this.gridIsEmpty) {
			let cmap = this.columnMap;
			$.each(thisCls.hiddenColumnList, function() {
				delete cmap[this.COLUMN_ID]
			});
			
			let item = gridDataList[0]
			let checkEmpty = true; 
			$.each(cmap, function(key, val) {
				if(isValid(item[key])) {
					checkEmpty = false;
					return false;
				}
			});
			return checkEmpty;
		} else return false;
	}
    
    this.deleteRow = function(targetCells) {

    	// 읽기 전용인경우 열삭제 방지
    	if(this.readOnly == "Y") return;

    	// 업데이트 전용인경우 열삭제 방지
    	if(this.updateOnly == "Y") {
			C_POP.alert('추가/삭제는 금지되어 있습니다. 수정만 가능합니다.')
			return;
		}
	
		if(isEmpty(targetCells)) targetCells = this.selectedCells;
		let targetdCell = targetCells[0];	

	    if (!targetdCell) {
	        alert("선택된 셀이 없습니다.");
	    } else {
	        targetCells.forEach(selectedCell => {
				
		        const row = $(selectedCell).closest('tr');

				const rowData = thisCls.getRowData(selectedCell);
				
				rowData["__state"] = "delete";
				thisCls.deleteRowDataList.push(rowData);				
		    	
		        const uprow		= row.parent();
		    	
		    	const trCnt = uprow.children().length;
		    	
		    	if(trCnt == 1) {
					this.addRow();
		        	row.remove();
					thisCls.gridIsEmpty = true;
		    	} else {
		        	row.remove();
				}
				
				// 등록된 Delete Event가 있으면 실행 시켜준다.
				if(typeof thisCls.rowConfig.onDeleteRow == "function" ) thisCls.rowConfig.onDeleteRow(row, rowData); 


	        });

	        this.setGridIndex(this.gridId); // Update the grid indices

	        this.undoStack = []
	        this.redoStack = []
		
		}
    };
	/*
    this.reloadGrid = function() {
    	if(confirm('초기화를 진행하면 이전 상태로 되돌릴 수 없습니다.\n\n초기화를 진행 하시겠습니까?')) {
	        const gridId = this.gridId;
	        const initialGridContent = this.initialContent;
	        if (initialGridContent) {
	            $(`#${gridId}`).html(initialGridContent);
	            this.init(gridId); // 그리드 다시 초기화
	            C_GRID.closeLayerPopup();
	        } else {
	            alert("초기 내용이 없습니다.");
	        }
    	}
    }
	*/
	this.getSelectedRowData = function() {
		let targetCell = this.selectedCells[0];
		if(isValid(targetCell)) return this.getRowData(targetCell);
	}

	this.getRowDataForReadOnly = function(targetCell) {

		let glist = C_GRID.getBaseGridData(this.gridId)
		
		if(isEmpty(glist)) return null;
		
		let rowidx = Number($(targetCell).attr("data-row") - 2);
		
		return glist[rowidx];
	}

	this.getRowData = function(targetCell) {
		if( this.readOnly == "Y" ) {
			let rowData = this.getRowDataForReadOnly(targetCell);
			if(isValid(rowData)) return rowData;
		}
		let returnObj = {}
		let $tr = $(targetCell).closest("tr");
		$.each(this.hiddenColumnList, function() {
			let columnId = this.COLUMN_ID
			returnObj[columnId] = $tr.attr(columnId);
		});

		$tr.find("td").each(function(idx) {
			let columnId 	= $(this).attr("columnId");
			let val			= $(this).attr("value");
			returnObj[columnId] = val;
		});
		return returnObj;
	}
	
	this.getRowDataByPk = function(pkMap) {
		let key = "";
		let primaryKeyList = this.primaryKeyList;
		$.each(primaryKeyList, function() {
			if(isEmpty(pkMap[this])) return false;
			key += pkMap[this]; 
		});
		if(key == "") return null;
		
		let gridDataList = C_GRID.getGridData(this.gridId);
		
		let resultData = {}
		$.each(gridDataList, function() {
			let cfkey = "";
			let item = this;
			$.each(primaryKeyList, function() {
				cfkey += item[this]; 
			});
			if(cfkey == key) {
				resultData = item;
				return false;	
			}
		});
		return resultData;
	}

    this.getSelectedCells = function() {
		return this.selectedCells;
	}
	this.searchCellList = function(parm) {
		let searchList	= parm.searchList;
		let searchType	= parm.searchType;
		if(isEmpty(searchType)) searchType = "EQ";
		
		let resultList = []; 
		$.each(searchList, function() {
			let column 		= this.column;
			let searchVal	= this.searchVal;
			let columnInfo	= thisCls.columnInfo[column];
			let cidx 		= columnInfo.seq;
			
			$(`#${gridId} td[data-col=${cidx}]`).each(function() {
				let tval = $(this).html();
				if			(searchType == "ALL") {
					resultList.push(this);
				} else if			(searchType == "EQ") {
					if(tval == searchVal				) resultList.push(this);
				} else if	(searchType == "LK") {
					if(tval.indexOf(searchVal) > -1		) resultList.push(this);
				}
			});
		});
		return resultList;
	}
	this.getCellObject = function(rowIdx, colIdx) {
		return $(`#${gridId} td[data-row='${rowIdx}'][data-col='${colIdx}']`);
	}
	this.setCheckBox = function(tdDom, flag) {
        let row 		= $(tdDom).closest('tr');
		$(row).find("input[type='checkbox']").prop("checked", flag);
	}	 
	this.setAllCheckBox = function(flag) {
        $(`#${gridId} input[type='checkbox']`).prop("checked", flag);
	}
	this.setRowDataByPk = function(setData) {
		let primaryKeyList = this.primaryKeyList;
		let key = "";
		$.each(primaryKeyList, function() {
			if(isEmpty(setData[this])) return false;
			key += setData[this]; 
		});
		if(key == "") return null;

        const tableBody = $(`#${this.gridId} tbody`);

		$(tableBody).find("tr").each(function(idx) {
			let returnObj = {}
			let $tr = $(this);
			$.each(thisCls.hiddenColumnList, function() {
				let columnId = this.COLUMN_ID
				returnObj[columnId] = $tr.attr(columnId);
			});
			let targetCell = {};
			$tr.find("td").each(function(idx) {
				let columnId 	= $(this).attr("columnId");
				let val			= $(this).attr("value");
				returnObj[columnId] = val;
				targetCell = this;
			});
			let tkey = "";
			$.each(primaryKeyList, function() {
				tkey += returnObj[this]; 
			});
			if(key == tkey) {
				$.each(setData, function(key, val) {
					if(in_array(key, primaryKeyList)) return true;
					thisCls.setCellToColumn(targetCell, val, key)
				});
				return false;
			}
		});
		
	}	 
	this.isChanged = function() {
		let data = C_GRID.getGridData(this.gridId);
		let gridIsEmpty = this.isEmpty();
		if(gridIsEmpty) return false;
		let retFlag = false;
		$.each(data, function(idx) {
			let item = this;
			if(in_array(this.__state, ["insert", "update", "delete"])) {
				retFlag = true;
				return false;
			}
		});
		return retFlag;
	}
	this.getGridData = function() {
		return C_GRID.getGridMainData(this.gridId);
	}
};

var C_GRID = {
	 gridMap 				: {}
	,currentSelectedGridId 	: ""
	,gridColumnFnMap		: {}
	,lockGridEventCheck		: false
	,setCurrentSelectedGridId(gridId) {
		this.currentSelectedGridId = gridId;
	 }
	,getSelectedGrid() {
		if(this.currentSelectedGridId == "") return;
		return C_GRID.gridMap[this.currentSelectedGridId];
	 }
	,getGridInstance(gridId) {
		return C_GRID.gridMap[gridId];
	 }
	,makeGrid(gridId, parm) {
		
		if(isValid(C_GRID.gridMap[gridId])) {
			if(isValid(C_GRID.gridMap[gridId])) {
				C_GRID.gridMap[gridId].destroy();
				C_GRID.gridMap[gridId] = null;
			}
		}
		var gridInstance = new CLASS_GRID(parm, gridId);
		
		gridInstance.init();
		
		C_GRID.gridMap[gridId] = gridInstance
	 }
	,selectFirstRow : function(gridId) {
		if(isEmpty($(`#${gridId} .explan-grid-cell`)) || isEmpty($(`#${gridId} .explan-grid-cell`)[0])) return;

		let targetDom = $(`#${gridId} .explan-grid-cell`)[0];
		
		if(isEmpty(targetDom)) return;

		C_GRID.gridMap[gridId].selectedProcess(targetDom);
	 }

	,clearState(gridId) {
    	if(isEmpty(C_GRID.gridMap[gridId])) {
    		C_POP.alert('선택된 Grid가 없습니다.');
    		return;
    	}
		C_GRID.gridMap[gridId].clearState();
	 }

	,clearSelection(gridId) {
		$.each(C_GRID.gridMap, function(key, obj) {
			if(gridId != key) return true;
			obj.clearSelection();
		});
	 }
    ,getGridData(gridId) {

//    	let baseDataList 	= C_GRID.getBaseGridData(gridId);

    	let curDataList		= [];
			
		if(isEmpty(C_GRID.gridMap[gridId])) return;

		if(!C_GRID.gridMap[gridId].isEmpty()) {
			curDataList = C_GRID.getGridMainData(gridId);	
		}
		let deleteRowDataList = C_GRID.gridMap[gridId].deleteRowDataList;
		curDataList = [...deleteRowDataList,...curDataList];
		
        let gridUids 		= curDataList.map(row => row.uid);

        // Filter grid1 to find rows with uids not in grid2
//      let filteredData = baseDataList.filter(row => !gridUids.includes(row.uid));

//        $.each(filteredData, function(idx) {
//        	filteredData[idx].__state = "delete";
//        	curDataList.push(filteredData[idx]);
//        });

		
        $.each(curDataList, function(idx) {
        	delete curDataList[idx].uid;
        	delete curDataList[idx]["data-row"];
        });

        return curDataList;
     }
    ,getBaseGridData(gridId) {
    	if(isEmpty(C_GRID.gridMap[gridId])) {
    		//C_POP.alert('선택된 Grid가 없습니다.');
    		return;
    	}
		let gridInstance 	= C_GRID.gridMap[gridId];
    	let initialContent 	= gridInstance.initialContent;
    	let gridElement 	= $('<div id="wrapper1"><table>' + initialContent + '</table></div>');
    	
        let headers = [];
        let rows = [];
        let invalidCheck = false;
        
        // Get column headers
        $(gridElement).find('col').each(function() {
            let column = $(this).attr('column');
            if (isEmpty(column)) invalidCheck = true;
            headers.push(column);
        });

        if (invalidCheck) {
            alert(`Table의 Column 설정이 필요합니다.`);
            return null;
        }

        // Get row data
        $(gridElement).find('tr[uid]').each(function() {
			let trObj = this;
            let rowData = {};
            $(trObj).find('td').each(function(index) {
				if(headers[index] == "checkbox") {
	                rowData[headers[index]] = $(this).find("input[type=checkbox]").prop("checked");
				} else {
					let val = $(this).attr("value");
					if(isEmpty(val)) val = "";
	                rowData[headers[index]] = val;
				}
            });

            if (Object.keys(rowData).length === 0) {
                return true;
            }

			$.each(gridInstance.hiddenColumnList, function() {
				let columnId 		= this["COLUMN_ID"];
				rowData[columnId] 	= $(trObj).attr(columnId);
			});

            let __state = $(this).attr("__state");
            rowData["__state"] = __state;
            rowData["uid"] = $(this).attr("uid");

            rows.push(rowData);
        });
        return rows;
    	
     }
    ,getGridMainData(gridId) {
        let headers = [];
        let rows = [];
		let invalidCheck = false;        
        $(`#${gridId} colgroup col`).each(function() {
        	let column = $(this).attr('column');
        	if(isEmpty(column)) invalidCheck = true;
            headers.push(column);
        });
        if(invalidCheck) {
        	alert(`${gridId} Table의 Column 설정이 필요합니다.`);
        	return null;
        }
        $(`#${gridId} tbody tr`).each(function() {
	
			const state = $(this).attr("__state");
			
			let display = $(this).css("display");
			if(display == "none") return true;

			let id 		= $(this).attr("id");
			if(id == "nodata") return true;
			
            let rowData = {};
            let baseRowData = {}

			if(isValid(C_GRID.gridMap[gridId].hiddenColumnList)) {
				let trObj = this;
				$.each(C_GRID.gridMap[gridId].hiddenColumnList, function() {
					let val = $(trObj).attr(this.COLUMN_ID);
					baseRowData[this.COLUMN_ID] = val;
				});
			}

            $(this).find('td').each(function(index) {
				if(headers[index] == "checkbox") {
	                rowData[headers[index]] = $(this).find("input[type=checkbox]").prop("checked");
				} else {
	                rowData[headers[index]] = $(this).attr("value").trim();
					if(state == 'insert') {
						baseRowData[headers[index]] = $(this).attr("value").trim();
					} else {
						baseRowData[headers[index]] = $(this).attr("basevalue").trim();
					}
				}
            });
			if (Object.keys(rowData).length === 0) {
			    return true;
			}
            
            rowData["_BASE_ROW_DATA"] = baseRowData;

            let attrList = C_GRID.getAllAttrFromDom(this, 'big');
            
            let allRowData = Object.assign({}, rowData, attrList);

            rows.push(allRowData);
        });
        return rows
     }
	,init() {
        $(document).on('contextmenu', '.explan-grid-cell', function(event) {
            event.preventDefault(); // 기본 우클릭 메뉴 방지
            const x = event.pageX; // 마우스 클릭한 x 좌표
            const y = event.pageY; // 마우스 클릭한 y 좌표
			
			let selectedGridInstance = C_GRID.getSelectedGrid();

			// 그리드가 readOnly = "Y" 읽기 전용이면 contextmenu 사용 안함.			
			
            if (!$(event.target).hasClass('explan-selected')) {
                selectedGridInstance.clearSelection(event);
                $(event.target).addClass('explan-selected');
                selectedGridInstance.selectedCells = [event.target];
            } else {
                event.target.classList.add('explan-selected');
            }

            C_GRID.closeLayerPopup();

			let popup = "";

			if(selectedGridInstance.readOnly == "Y") {
	            popup = $(`<div id="explanMenuPopup" class='explan-layer-popup' type='explan-layer-popup' style='left: ${x}px; top: ${y}px; width: 200px; background-color: white; border: 1px solid black; padding: 5px;z-index:9999'>
                               <div mtype="Cell복사" 	class='explan-menu-item' title="Ctrl + c">Cell복사(Ctrl + c)</div>
                           </div>`);
				
			} else {
	            popup = $(`<div id="explanMenuPopup" class='explan-layer-popup' type='explan-layer-popup' style='left: ${x}px; top: ${y}px; width: 200px; background-color: white; border: 1px solid black; padding: 5px;z-index:9999'>
                               <div mtype="Cell편집" 	class='explan-menu-item' title="Ctrl + e">Cell편집(Ctrl + e)</div>
                               <div mtype="Cell복사" 	class='explan-menu-item' title="Ctrl + c">Cell복사(Ctrl + c)</div>
                               <div mtype="붙여넣기" 	class='explan-menu-item' title="Ctrl + v">붙여넣기(Ctrl + v)</div>
                               <div mtype="행삽입" 	class='explan-menu-item' title="Ctrl + i">행삽입(Ctrl + i)</div>
                               <div mtype="행추가" 	class='explan-menu-item' title="Ctrl + a">행추가(Ctrl + a)</div>
                               <div mtype="행삭제" 	class='explan-menu-item' title="Ctrl + d">행삭제(Ctrl + d)</div>
                               <div mtype="실행취소" 	class='explan-menu-item' title="Ctrl + z">실행취소(Undo/Ctrl + z)</div>
                               <div mtype="재실행" 	class='explan-menu-item' title="Ctrl + y">재실행(Redo/(Ctrl + y))</div>
                           </div>`);
			}

            $('body').append(popup);

            $('#explanMenuPopup .explan-menu-item').on('mouseenter', function() {
                $(this).css('background-color', 'lightblue');
            });

            $('#explanMenuPopup .explan-menu-item').on('mouseleave', function() {
                $(this).css('background-color', 'initial');
            });

            // 메뉴 클릭 이벤트 수정
            $('#explanMenuPopup .explan-menu-item').on('click', function() {
                const menuText = $(this).attr("mtype");
	            C_GRID.closeLayerPopup();
				if 		(menuText === '행삽입'		) selectedGridInstance.insertRow();
				else if (menuText === '행추가'		) selectedGridInstance.addRow();
				else if (menuText === '행삭제'		) selectedGridInstance.deleteRow();
				//else if (menuText === '초기화'		) selectedGridInstance.reloadGrid();
				else if (menuText === '실행취소'	) selectedGridInstance.undo();
				else if (menuText === '재실행'		) selectedGridInstance.redo();
				else if (menuText === 'Cell편집'	) selectedGridInstance.editCell();
				else if (menuText === 'Cell복사'	) selectedGridInstance.copyCell();
				else if (menuText === '붙여넣기'	) selectedGridInstance.pasteCell();
				else {
				    alert(`선택한 메뉴: ${menuText}`);
				}
	            selectedGridInstance.clearSelection(); // 선택 해제
            });

            popup.on('click', function(event) {
                event.stopPropagation();
            });

            $(document).one('click', function() {
                popup.remove();
            });

            return false;
        });
        $(document).unbind('mousedown')
        $(document).bind('mousedown'	, function(event) {
			if(C_GRID.lockGridEventCheck) return true;
			$.each(C_GRID.gridMap, function(key, obj) {
				if(isEmpty(obj)) return true;
				obj.onDocumentMouseDown(event);
			});
        });
        $(document).unbind('keydown')
        $(document).bind('keydown'	, function(event) {
			if(C_GRID.lockGridEventCheck) return true;
			let nxtst;
			$.each(C_GRID.gridMap, function(key, obj) {
				nxtst = obj.onKeyDown(event);
			});
			return nxtst;
        });
        $(document).unbind('paste')
        $(document).bind('paste'	, function(event) {
			if(C_GRID.lockGridEventCheck) return true;
			$.each(C_GRID.gridMap, function(key, obj) {
				obj.onPaste(event);
			});
        });
	 }
    ,closeLayerPopup() {
        $('div[type="explan-layer-popup"]').remove();
     }
	,getAllAttrFromDom(domObj, type) {
	    var attributes = {};
	    $(domObj).each(function() {
	        $.each(this.attributes, function() {
	            if(this.specified) {
	            	let name = this.name;
	            	if(type==='big' && !in_array(name, ['__state','uid','data-row']) )  name=name.toUpperCase();
	                attributes[name] = this.value;
	            }
	        });
	    });
	    return attributes;
	 }
    ,setReadOnly(gridId, state) {  // state="Y" is Readonly , "N" or undefined is  editable 
    	if(isVaild(C_GRID.gridMap[gridId]) && isVaild(C_GRID.gridMap[gridId])) {
	    	C_GRID.gridMap[gridId].readOnly = state;
    	}
     }
    ,getReadOnly(gridId) {
    	if(isVaild(C_GRID.gridMap[gridId]) && isVaild(C_GRID.gridMap[gridId])) {
	    	return C_GRID.gridMap[gridId].readOnly;
    	} else {
    		return null;
    	}
     }
	,dblClickTrigger(gridId, targetObj) {

		let gridObj = C_GRID.getGridInstance(gridId)
		
		let event = { target : $(targetObj).parent() }
		
		gridObj.onDblClick(event);
	 }
	,setValueToGridCell(gridId, targetObj, value) {
		let gridObj = C_GRID.getGridInstance(gridId)
		gridObj.setCell(targetObj, value);
	 }
	,getDataFromCell(targetDom) {
		let retObj = {
			 value 		: $(targetDom).attr("value")
			,basevalue	: $(targetDom).attr("basevalue")
			,text		: $(targetDom).text()
		}
		return retObj; 
	 } 
	,clickInputProcess(gridId) {
		setTimeout(function() {
			C_GRID.clearSelection(gridId);
		}, 50);
	 }
	,onDateInputBlur(gridId, targetDom) {
        const newText = $(targetDom).val();
		const gridObj = C_GRID.getGridInstance(gridId);
		if(isValid(newText)) {
			gridObj.setCell(targetDom, newText);	
		}
        // 상위 tr update 설정
        let parentTr = $(targetDom).closest('tr'); // 상위 tr 요소 찾기
        let __state = parentTr.attr("__state"); // __state 속성 값 가져오기
        if(__state === "default") parentTr.attr("__state", "update");
	 }
	,getSelectedCells 	: function(gridId) {
		const gridObj = C_GRID.getGridInstance(gridId);
		return gridObj.getSelectedCells();
	 }
	,registColumnFn		: function(gridId, column, buttonFn) {
		let key = gridId + column;
		C_GRID.gridColumnFnMap[key] = buttonFn;
	 }
	,execColumnFn		: function(gridId, column, targetCell) {
		const gridObj	= C_GRID.getGridInstance(gridId);
		let rowData		= gridObj.getRowData(targetCell);
		let key = gridId + column;
		if(typeof C_GRID.gridColumnFnMap[key] == "function") C_GRID.gridColumnFnMap[key](targetCell, rowData, gridObj);
	 }
	,lockGridEvent : function() {
		C_GRID.lockGridEventCheck = true;
	 } 
	,unlockGridEvent : function() {
		C_GRID.lockGridEventCheck = false;
	 } 
}

$(function() {
	C_GRID.init()
});

