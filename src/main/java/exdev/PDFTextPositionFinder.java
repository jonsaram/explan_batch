package exdev;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFTextPositionFinder extends PDFTextStripper {

    private String targetText;
    private List<String[]> insertData;
    private boolean found = false;
    private static final String[] excludeWords = {"재무제표","COPYRIGHT","조회일","감사의견","재무상태"};

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
                    System.out.println(firstPosition.getXDirAdj() + ":" + insertData.get(i)[0] +"/" + insertData.get(i)[1] +"/" + insertData.get(i)[2] +"/" + insertData.get(i)[3]);        			
        		}
        	}
            //System.out.println("X: " + firstPosition.getXDirAdj());
            //System.out.println("Y: " + firstPosition.getYDirAdj());
            found = true;
        }
        //super.writeString(text, textPositions);
    }

    public static void main(String[] args) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        try {
        	List<String> firstWords = new ArrayList<String>();
        	List<String[]> insertData = new ArrayList<String[]>();
        	boolean capture = false;
        	boolean excludeStr = true;
            PdfDocument pdfDoc = new PdfDocument(new PdfReader("C:\\\\Users\\\\USER\\\\Downloads\\\\4.pdf"));
            int numberOfPages = pdfDoc.getNumberOfPages();
            // 첫페이지 표지 제외하고 시작
            for (int i = 2; i <= numberOfPages; i++) {
                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new SimpleTextExtractionStrategy());
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
            pdfDoc.close();

        	PDDocument document = PDDocument.load(new File("C:\\\\Users\\\\USER\\\\Downloads\\\\4.pdf"));            
            for(int i=0; i<firstWords.size(); i++) {
            	PDFTextPositionFinder stripper = null;
                stripper = new PDFTextPositionFinder(firstWords.get(i), insertData);
                stripper.setSortByPosition(true);
                stripper.getText(document);
            }
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}