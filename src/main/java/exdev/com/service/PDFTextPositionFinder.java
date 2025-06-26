package exdev.com.service;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class PDFTextPositionFinder extends PDFTextStripper {

	private boolean found = false;
    private static final String[] excludeWords = {"재무제표","COPYRIGHT","조회일","감사의견","재무상태"};
	
    private String targetText;
    private List<String[]> insertData;
    private String[] returnData;

	public String[] getReturnData() {
		return returnData;
	}

    public PDFTextPositionFinder(String targetText, List<String[]> insertData) throws IOException {
        super();
        this.targetText = targetText;
        this.insertData = insertData;
    }    

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (text.equals(targetText) && !found) {
        	for(int i=0; i<insertData.size(); i++) {
        		if(text.equals(insertData.get(i)[0])) {
                    TextPosition firstPosition = textPositions.get(0);
                    String[] returnRow = {String.valueOf((int)firstPosition.getXDirAdj()), insertData.get(i)[0], insertData.get(i)[1], insertData.get(i)[2], insertData.get(i)[3]};
                    returnData = returnRow;
        		}
        	}
            found = true;
        }
    }
}