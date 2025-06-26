

function _excelDownload (columnMap, excelColumnNames ,excelColumnOrder,requestParm , title ,userId,hiddenColumns)  {

		let excelParam = {};
		excelParam["queryId"		] = requestParm.queryId; 
		excelParam["columnOrders"	] = excelColumnOrder;
		excelParam["downInfo"		] = JSON.stringify({  title : title, menu : title});
		excelParam["requestParm"	] = JSON.stringify(requestParm.requestParm );
		excelParam["columnNames"	] = excelColumnNames;
		excelParam["checkedRow"		] = '';
		excelParam["hiddenColumns"	] = hiddenColumns;
		excelParam["columnMap"		] = columnMap;	
		excelParam["hiddenColumns"].unshift("N"	  );
		
	    var xhr = new XMLHttpRequest();
	    var urlParams = new URLSearchParams( excelParam ).toString();

		try {
			xhr.open('POST', '/excelDownload.do?'+urlParams, true);
			xhr.responseType = 'blob';
			xhr.setRequestHeader('Content-Type', 'application/json');
			
		    xhr.onload = function() {
			
				if (xhr.status === 200) {
	
		    		C_COM.hideLoadingBar();        
		            var blob = new Blob([xhr.response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
		            var url = window.URL.createObjectURL(blob);
		            
		            var a = document.createElement('a');
		            a.href = url;
		            a.download = title+"_"+userId+'.xlsx';
		            document.body.appendChild(a);
		            a.click();
		            window.URL.revokeObjectURL(url);
		        } else {
		        	C_COM.hideLoadingBar();
		            console.error('Failed to download excel file');
		            
		        }
		    };
	
	        xhr.onerror = function() {
	            // Handle network errors
	            console.error('Network error occurred during Excel download');
	            C_COM.hideLoadingBar();
	        };
		    
			xhr.send(JSON.stringify(excelParam));
			
		} catch (error) {
	        console.error('Error during Excel download setup:', error);
    	    C_COM.hideLoadingBar();
    	}
}
