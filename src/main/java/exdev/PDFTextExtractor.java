package exdev;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PDFTextExtractor extends PDFTextStripper {

    public PDFTextExtractor() throws IOException {
        super();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        StringBuilder builder = new StringBuilder();
        float previousX = 0;

        for (TextPosition position : textPositions) {
            float x = position.getXDirAdj();
            if (x > previousX + 5) { // 들여쓰기 감지 (5는 임의의 값, 조정 가능)
                builder.append("\t");
            }
            builder.append(position.getUnicode());
            previousX = x;
        }

        System.out.println(builder.toString());
    }

    public static void main(String[] args) {
        try {
            PDDocument document = PDDocument.load(new File("C:\\\\Users\\\\USER\\\\Downloads\\\\4.pdf"));
            PDFTextExtractor stripper = new PDFTextExtractor();
            stripper.setSortByPosition(true); // 텍스트 위치에 따라 정렬
            stripper.getText(document);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
