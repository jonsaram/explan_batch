var CLASS_GRID = function(parm, gridId) {
	
	if(isEmpty(parm)) parm = {};
	
    this.isDragging 		= false;
    this.startCell 			= null;
    this.selectedCells 		= [];
    this.pasteStartCell 	= null;
    this.gridId 			= gridId;
    this.undoStack			= [];
    this.redoStack 			= [];
    this.initialContent 	= "";
    this.parm 				= parm;
    this.readOnly			= parm.readOnly;
	this.hiddenColumnList	= parm.hiddenColumnList;
	this.newData			= parm.newData;				// grid에 있는 Data가 모두 새로 저장되어야 할때 "Y" 세팅한다. => 초기 row state를 insert로 설정한다.
    this.columnInfo			= {
    	columnType	: {}
    }
    this.columnConfig		= parm.columnConfig;
	if(isEmpty(this.columnConfig)) this.columnConfig = {}
    
    const thisCls					= this;
	
    // Select Box Code List에대해 Map을 생성한다.
    if(isValid(thisCls.columnConfig) && isValid(thisCls.columnConfig.selectBoxCodeList) ) {
    	thisCls.columnConfig.selectBoxCodeList = {}
    	$.each(thisCls.columnConfig.selectBoxCodeList, function(key, list) {
    		$.each(list, function() {
        		if(isEmpty(thisCls.columnConfig.selectBoxCodeList[key])) thisCls.columnConfig.selectBoxCodeList[key] = {};
        		thisCls.columnConfig.selectBoxCodeList[key][this[0]] = this[1];
    		});
    	});
    } 

    this.init = function() {
        
    	const gridId = this.gridId; 
    	
        // 컬럼 Type 설정
    	let columnInfo = {
    		columnArr : {}
    	}
    	$(`#${gridId} th`).each(function(idx) {
			let column 		= $(this).attr("column"		);
			let columnType 	= $(this).attr("columnType"	);
			let columnAttr 	= $(this).attr("columnAttr"	);
			if(isEmpty(columnType)) columnType = "text";
			columnInfo[column] = {
				 column		: column
				,columnType  : columnType
				,columnAttr  : columnAttr
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
//			const uid = $(this).attr("uid");

	        $(this).find('td').each(function(idx) {
				const columnInfo 	= thisCls.getColumnInfo(this);
				
				if(in_array(columnInfo.column, ["selectbox", "popup"])) {
					$(this).addClass("fx-btw");
				}
				if(columnInfo.column != "checkbox") {
		        	let value = $(this).text();
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

    	this.initialContent = $(`#${gridId}`).html();
    };
    this.destroy = function(gridId) {
        $(`#${gridId}`).off('mousedown');
        $(`#${gridId}`).off('mousemove');
        $(`#${gridId}`).off('mouseup');
        $(`#${gridId}`).off('click');
        $(`#${gridId}`).unbind('dblclick');
    };
	this.getColumnInfo = function(domObj) {
		if(typeof domObj == "number") {
			return thisCls.columnInfo.columnArr[domObj]
		} else {
			const idx = $(domObj).index();
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
            	let uid = getUniqueId();
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

    this.clearSelection = function() {
        $(`#${this.gridId} .explan-grid-cell`).removeClass("explan-selected");
        this.pasteStartCell = null;
        this.selectedCells = [];
    };

    this.selectCellsWithinRectangle = function(start, end) {
        const startRow = Math.min(parseInt(start.attr("data-row")), parseInt(end.attr("data-row")));
        const endRow = Math.max(parseInt(start.attr("data-row")), parseInt(end.attr("data-row")));
        const startCol = Math.min(parseInt(start.attr("data-col")), parseInt(end.attr("data-col")));
        const endCol = Math.max(parseInt(start.attr("data-col")), parseInt(end.attr("data-col")));

        $(`#${this.gridId} td.explan-grid-cell`).each(function() {
            const $cell = $(this);
            const row = parseInt($cell.attr("data-row"));
            const col = parseInt($cell.attr("data-col"));
            const isInRectangle = row >= startRow && row <= endRow && col >= startCol && col <= endCol;
            $cell.toggleClass("explan-selected", isInRectangle);
        });
    };

    this.onMouseDown = function(event) {
        if (!$(event.target).closest(`#${this.gridId}`).length) return;
        C_GRID.clearSelection(this.gridId);
        this.isDragging = true;
        this.startCell = $(event.target);
        this.selectedCells = [];
        this.selectCellsWithinRectangle(this.startCell, this.startCell);
    };

    this.onMouseMove = function(event) {
        if (!$(event.target).closest(`#${this.gridId}`).length) return;
        if (this.isDragging) {
            const endCell = $(event.target);
            this.clearSelection();
            this.selectCellsWithinRectangle(this.startCell, endCell);
        }
    };

    this.onMouseUp = function(event) {
        if (!$(event.target).closest(`#${this.gridId}`).length) return;
        if (this.isDragging) {
            this.isDragging = false;
            this.selectedCells = $(`#${this.gridId} .explan-grid-cell.explan-selected`).toArray();
            C_GRID.setCurrentSelectedGridId(this.gridId);
            
        }
    };

    this.onClick = function(event) {
        if (!$(event.target).closest(`#${this.gridId}`).length && !$(event.target).closest('.explan-layer-popup').length) return;
        if (!this.isDragging) {
            this.clearSelection();
			alert($(event.target).tag())
            $(event.target).addClass("explan-selected");
            this.selectedCells = [event.target];
            this.pasteStartCell = $(event.target);
        }
		this.onClickRow(event.target); 
    };

	this.onClickRow = function(targetCell) {
		let fnClickRow = this.columnConfig.onClickRow;
		if( typeof fnClickRow == "function") {
			let returnObj = {}
			$(targetCell).closest("tr").find("td").each(function(idx) {
				let columnInfo 	= thisCls.columnInfo.columnArr[idx];
				let val			= $(this).attr("value");
				returnObj[columnInfo.column] = val;
			});
			fnClickRow(returnObj);
		}
	}


    this.onDocumentMouseDown = function(event) {
        if ($(event.target).closest('.explan-grid-cell, .explan-layer-popup').length === 0) {
            this.clearSelection();
        }
    };

    this.onKeyDown = function(event) {

        let eventKey = event.key.toLowerCase();
    	
    	if(this.gridId != C_GRID.currentSelectedGridId) return;
        if (event.ctrlKey && eventKey === 'c') {
            const selectedText = this.getSelectedTextForExcel();
            if (selectedText != null) this.copyToClipboard(selectedText);
        }
        if (eventKey === 'delete' && this.selectedCells.length > 0) {
            this.backupCellsState(this.selectedCells);
            this.selectedCells.forEach(cell => {
                $(cell).text('');
            });
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

        let rows = {};
        this.selectedCells.forEach(cell => {
            const row = $(cell).attr("data-row");
            const col = $(cell).attr("data-col");
            if (!rows[row]) rows[row] = [];

            let data = $(cell).attr("value");

            if (data.includes('\t') || data.includes('\n')) data = `"${data}"`;
            
            rows[row][col] = data;
        });

        const rowKeys = Object.keys(rows).sort((a, b) => a - b);
        return rowKeys.map(rowKey => {
            const cols = rows[rowKey];
            const colKeys = Object.keys(cols).sort((a, b) => a - b);
            return colKeys.map(colKey => cols[colKey]).join('\t');
        }).join('\n');
    };

    this.copyToClipboard = function(text) {
        const $temp = $("<textarea>");
        $("body").append($temp);
        $temp.val(text).select();
        document.execCommand("copy");
        $temp.remove();
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
		
        this.backupCellsState(this.getCellsToChange(clipboardData));

		clipboardData = this.parseExcelType(clipboardData);
		
        const lines = clipboardData.split("\n");

        const startRow = parseInt($(this.pasteStartCell).attr("data-row"));
        const startCol = parseInt($(this.pasteStartCell).attr("data-col"));

        lines.forEach((line, rowIndex) => {
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
				
				if(columnInfo.columnAttr == "readOnly") return true;

                const targetCell = $(`#${this.gridId} .explan-grid-cell[data-row=${targetRow}][data-col=${targetCol}]`);
                cellText = cellText.replaceAll("[nl]", "</br>");
                if (targetCell.length) {
	                this.setCell(targetCell, cellText);
                } else {
		            const tableBody = $(`#${this.gridId}`);
		            const lastRow = tableBody.find('tr').last();
		            const firstCellInLastRow = lastRow.find('.explan-grid-cell[data-col=1]');
		
		            // 선택 표시 지우기
		            this.clearSelection();
					
		            // 마지막 줄의 첫 번째 셀을 선택된 상태로 만듭니다
		            firstCellInLastRow.addClass('selected');
		            this.selectedCells.push(firstCellInLastRow[0]);
		            
		            this.addRow();
		            
	                const targetCell = $(`#${this.gridId} .explan-grid-cell[data-row=${targetRow}][data-col=${targetCol}]`);
	                this.setCell(targetCell, cellText);
                }
            });
        });
    };
    this.setCell = function() {
		if(typeof arguments[0] == "object") {
			// targetCell, cellText, initType
			this.setCellForObject(arguments[0], arguments[1], arguments[2]);
		} else {
			// rowIndex, columnIndex, cellText
			this.setCellForIndex(arguments[0], arguments[1]);
		}
	}	
	this.setCellForIndex  = function(rowIndex, columnIndex, cellText) {
	    const targetCell = $(`#${this.gridId} .explan-grid-cell[data-row=${rowIndex}][data-col=${columnIndex}]`);
		this.setCellForObject(targetCell, cellText);
	}
    // Cell에 Type에 따라 값을 입력 한다.
    this.setCellForObject = function(targetCell, cellText, initType) {
        let tdIndex = targetCell.index();
		let curCellVal = targetCell.attr("value");
        let columnInfo = this.columnInfo.columnArr[tdIndex];
		if			(in_array(columnInfo.columnType, ["text", "popup"])) {
	        targetCell.attr("value", cellText);
	        targetCell.html(cellText);
		} else if	(columnInfo.columnType == "selectbox") {
			let key 		= columnInfo.column; 
			let viewText	= thisCls.columnConfig.selectBoxCodeList[key][cellText];
			if(isEmpty(viewText)) viewText = "";
			viewText = `${viewText}<button type="button" class="arr-dn-btn"></button>`;
	        targetCell.attr("value", cellText);
	        targetCell.html(viewText);
		}
		// Cell이 이전값과 다른경우, onChangeCell Event가 등록되어 있다면 실행 한다.
		if( curCellVal != cellText) {
			if(isValid(thisCls.columnConfig.onChangeCell) && isValid(thisCls.columnConfig.onChangeCell[columnInfo.column])) {
				if( typeof thisCls.columnConfig.onChangeCell[columnInfo.column].func == "function") {
					let rowIdx = $(targetCell).attr("data-row");
					let colIdx = $(targetCell).attr("data-col");
					
					// 최초 세팅시 Change Event를 막는 설정이 있는 경우 실행 안함.
					if(initType != 'init' || thisCls.columnConfig.onChangeCell[columnInfo.column].notExecInit != "Y") {
						thisCls.columnConfig.onChangeCell[columnInfo.column].func(thisCls, Number(rowIdx), Number(colIdx), targetCell);
					}
				}
			}
			// 상태 업데이트
			if(initType != 'init' && initType != 'new') {
				// 처음로딩이 아니면 상태 변경 한다.
				let state = $(targetCell).parent().attr("__state");
				if(state == "default") $(targetCell).parent().attr("__state", "update");
			} 
		}
		
		
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

    this.onDblClick = function(event) {

    	// 읽기 전용인경우 키인 방지
    	if(this.readOnly == "Y") return;

        if (!$(event.target).closest(`#${this.gridId}`).length) return;
        this.clearSelection();

        const cellWidth = $(event.target).width();
        const cellHeight = $(event.target).height();

        this.backupCellsState([event.target]);
        
        let tdIndex = $(event.target).index();
        let columnInfo = this.columnInfo.columnArr[tdIndex];
		
		if(columnInfo.columnAttr == "readOnly") {
			return;
		} else if		(columnInfo.columnType == "text") {
			this.setTextboxToCell(event, columnInfo)
		} else if	(columnInfo.columnType == "selectbox") {
			this.setSelectboxToCell(event, columnInfo)
		} else if	(columnInfo.columnType == "popup") {
			this.openPopup(event, columnInfo)
		}

    };
    this.openPopup = function(event, columnInfo) {
		let column = columnInfo.column;
		const popupId = this.columnConfig.popupIdMap[column]
		C_POP.open(popupId, {}, function(retData) {
			thisCls.setCell($(event.target), retData);
		});
	}
    this.setSelectboxToCell = function(event, columnInfo) {
    	let selectList = this.columnConfig.selectBoxCodeList[columnInfo.column];
		let selectBoxStr = '';
		selectBoxStr += `<select class="form-control form-control-sm col10"><option value=" "> </option>`;
		$.each(selectList, function() {
			let cd 	= this[0];
			let txt	= this[1];
			selectBoxStr += `<option value='${cd}'>${txt}</option>`;
		});
		selectBoxStr += `</select>`;
		let rval = $(event.target).attr("value");
		if(isEmpty(rval)) rval = $(event.target).text();
		const $selectBox = $(selectBoxStr);
		$selectBox.find("option").each(function() {
			if($(this).val() == rval) $(this).attr("selected", true);
		});
        $(event.target).html($selectBox);
        
        $selectBox.on('blur', function() {
        	thisCls.setCell($(event.target), this.value);
        });
        
        $selectBox.focus();
    }
    
	this.setTextboxToCell = function(event) {

		let itxt = $(event.target).attr("value");

        const $input = $(`<input type='text' class='form-control form-control-sm col10'/>`);
		$input.val(itxt);
        $(event.target).html($input);
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

        $input.blur(function() {
            const newText = $(this).val();

            thisCls.setCell($cell, newText);

            // 상위 tr update 설정
	        let parentTr = $cell.closest('tr'); // 상위 tr 요소 찾기
	        let __state = parentTr.attr("__state"); // __state 속성 값 가져오기
	        if(__state === "default") parentTr.attr("__state", "update");
        });
	} 


    // 행추가
    this.addRow = function() {
    	return this.insertRow("A");
    }
    this.insertRow = function(type) {
	
    	// 읽기 전용인경우 열삽입 방지
    	if(this.readOnly == "Y") return;
	
    	if(isEmpty(type)) type = "I";
        const selectedCell = this.selectedCells[0];
        if (selectedCell) {
        	
            let row 		= $(selectedCell).closest('tr');
            let newRow		= {};

       		newRow 	= row.clone(); // 현재 선택된 행 복사
            
            $(newRow).attr("__state"	, "insert"); // 신규 Row state값 설정
        	let uid = getUniqueId();
            $(newRow).attr("uid", uid); // 신규 Row state값 설정

			$.each(this.hiddenColumnList, function() {
				let val = "";
				if(this["ifNewRandom"] == "Y") val = uid;
	            $(newRow).attr(this["COLUMN_ID"], val); // 신규 Row state값 설정
			});

            
            newRow.find('.explan-grid-cell').each(function() {
				$(this).attr('basevalue', "");
	
				const columnInfo 	= thisCls.getColumnInfo(this);
				if(columnInfo.column != "checkbox") {
					$(this).removeClass('explan-selected').text('').attr("value", ""); // 새로운 행의 모든 셀 내용 지우기
				}

			});
            if(type == "I" ) row.before(newRow); // 새로운 행 삽입
            if(type == "A" ) row.after(newRow); // 새로운 행 추가

            this.setGridIndex(this.gridId); // 그리드 인덱스 업데이트
           
            this.undoStack = [];
            this.redoStack = [];
        } else {
            //alert("선택된 셀이 없습니다.");
        }
    };
    
    
    
    this.deleteRow = function() {

    	// 읽기 전용인경우 열삭제 방지
    	if(this.readOnly == "Y") return;
	
	
	    const selectedCell = this.selectedCells[0];

	    if (!selectedCell) {
	        alert("선택된 셀이 없습니다.");
	    } else {
	        this.selectedCells.forEach(selectedCell => {
		        const row = $(selectedCell).closest('tr');
		    	
		        const uprow		= row.parent();
		    	
		    	const trCnt = uprow.children().length;
		    	
		    	if(trCnt == 1) {
					this.addRow()
		    	}
	
		        row.remove();
	
	        });

	        this.setGridIndex(this.gridId); // Update the grid indices

	        this.undoStack = []
	        this.redoStack = []
		
		}
    };
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

	this.getSelectedCells = function() {
		return this.selectedCells;
	}
    
    
};

var C_GRID = {
	 gridMap : {}
	,currentSelectedGridId : ""
	,setCurrentSelectedGridId(gridId) {
		this.currentSelectedGridId = gridId;
	 }
	,getSelectedGrid() {
		if(this.currentSelectedGridId == "") return;
		return C_GRID.gridMap[this.currentSelectedGridId].gridInstance;
	 }
	,getGridInstance(gridId) {
		return C_GRID.gridMap[gridId];
	 }
	,makeGrid(gridId, parm) {
		
		if(isValid(C_GRID.gridMap[gridId])) {
			if(isValid(C_GRID.gridMap[gridId].gridInstance)) {
				C_GRID.gridMap[gridId].gridInstance.destroy();
				C_GRID.gridMap[gridId].gridInstance = null;
			}
		}
		var gridInstance = new CLASS_GRID(parm, gridId);
		gridInstance.init();
		
		C_GRID.gridMap[gridId] = {
			 gridInstance: gridInstance 
		}
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
			if(gridId == key) return true;
			obj.gridInstance.clearSelection();
		});
	 }
    ,getGridData(gridId) {

    	let baseDataList 	= C_GRID.getBaseGridData(gridId);

    	let curDataList		= C_GRID.getGridMainData(gridId);

        let gridUids 		= curDataList.map(row => row.uid);

        // Filter grid1 to find rows with uids not in grid2
        let filteredData = baseDataList.filter(row => !gridUids.includes(row.uid));

        $.each(filteredData, function(idx) {
        	filteredData[idx].__state = "delete";
        	curDataList.push(filteredData[idx]);
        });
        $.each(curDataList, function(idx) {
        	delete curDataList[idx].uid;
        	delete curDataList[idx]["data-row"];
        });
        
        return curDataList;
     }
    ,getBaseGridData(gridId) {
    	if(isEmpty(C_GRID.gridMap[gridId])) {
    		C_POP.alert('선택된 Grid가 없습니다.');
    		return;
    	}
		let gridInstance 	= C_GRID.gridMap[gridId].gridInstance;
    	let initialContent 	= gridInstance.initialContent;
    	let gridElement 	= $('<div id="wrapper1"><table>' + initialContent + '</table></div>');
    	
        let headers = [];
        let rows = [];
        let invalidCheck = false;
        
        // Get column headers
        $(gridElement).find('th').each(function() {
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
        $(`#${gridId} th`).each(function() {
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

			if(isValid(C_GRID.gridMap[gridId].gridInstance.hiddenColumnList)) {
				let trObj = this;
				$.each(C_GRID.gridMap[gridId].gridInstance.hiddenColumnList, function() {
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
			if(selectedGridInstance.readOnly == "Y") return;
			
            if (!$(event.target).hasClass('explan-selected')) {
                selectedGridInstance.clearSelection();
                $(event.target).addClass('explan-selected');
                selectedGridInstance.selectedCells = [event.target];
            } else {
                event.target.classList.add('explan-selected');
            }

            C_GRID.closeLayerPopup();

            const popup = $(`<div id="explanMenuPopup" class='explan-layer-popup' type='explan-layer-popup' style='left: ${x}px; top: ${y}px; width: 200px; background-color: white; border: 1px solid black; padding: 5px;'>
                                <div class='explan-menu-item' title="Ctrl + e">Cell편집</div>
                                <div class='explan-menu-item' title="Ctrl + i">행삽입</div>
                                <div class='explan-menu-item' title="Ctrl + a">행추가</div>
                                <div class='explan-menu-item' title="Ctrl + d">행삭제</div>
                                <div class='explan-menu-item'>초기화</div>
                                <div class='explan-menu-item' title="Ctrl + z">실행취소(Undo)</div>
                                <div class='explan-menu-item' title="Ctrl + y">재실행(Redo)</div>
                            </div>`);

            $('body').append(popup);

            $('#explanMenuPopup .explan-menu-item').on('mouseenter', function() {
                $(this).css('background-color', 'lightblue');
            });

            $('#explanMenuPopup .explan-menu-item').on('mouseleave', function() {
                $(this).css('background-color', 'initial');
            });

            // 메뉴 클릭 이벤트 수정
            $('#explanMenuPopup .explan-menu-item').on('click', function() {
                const menuText = $(this).text();
	            C_GRID.closeLayerPopup();
				if 		(menuText === '행삽입'			) selectedGridInstance.insertRow();
				else if (menuText === '행추가'			) selectedGridInstance.addRow();
				else if (menuText === '행삭제'			) selectedGridInstance.deleteRow();
				else if (menuText === '초기화'			) selectedGridInstance.reloadGrid();
				else if (menuText === '실행취소(Undo)'	) selectedGridInstance.undo();
				else if (menuText === '재실행(Redo)'	) selectedGridInstance.redo();
				else if (menuText === 'Cell편집'		) selectedGridInstance.editCell();
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
			$.each(C_GRID.gridMap, function(key, obj) {
				obj.gridInstance.onDocumentMouseDown(event);
			});
        });
        $(document).unbind('keydown')
        $(document).bind('keydown'	, function(event) {
			let nxtst;
			$.each(C_GRID.gridMap, function(key, obj) {
				nxtst = obj.gridInstance.onKeyDown(event);
			});
			return nxtst;
        });
        $(document).unbind('paste')
        $(document).bind('paste'	, function(event) {
			$.each(C_GRID.gridMap, function(key, obj) {
				obj.gridInstance.onPaste(event);
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
    	if(isVaild(C_GRID.gridMap[gridId]) && isVaild(C_GRID.gridMap[gridId].gridInstance)) {
	    	C_GRID.gridMap[gridId].gridInstance.readOnly = state;
    	}
     }
    ,getReadOnly(gridId) {
    	if(isVaild(C_GRID.gridMap[gridId]) && isVaild(C_GRID.gridMap[gridId].gridInstance)) {
	    	return C_GRID.gridMap[gridId].gridInstance.readOnly;
    	} else {
    		return null;
    	}
     }
}


$(function() {
	C_GRID.init()
});

