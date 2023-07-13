package util.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelTest {

    @DisplayName("Excel 을 읽어 특정필드의 값과 색을 변경한다")
    @Test
    void Excel_을_읽어_특정필드의_값과_색을_변경한다() {
        String filePath = System.getProperty("user.dir") + "/data/sample.xlsx";  // 엑셀 파일 경로
        String cellValue = "19"; // 변경할 row 의 0번째 cell 값

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.out.println("해당 시트를 찾을 수 없습니다.");
                return;
            }

            // 특정 컬럼의 값과 색상 변경
            for (Row row : sheet) {
                Cell cell = row.getCell(0);

                if (cellValue.equals(cell.getStringCellValue())) {
                    Cell targetCell = row.getCell(1);

                    // 값 변경
                    targetCell.setCellValue("배송완료");

                    // 색상 변경
                    CellStyle style = workbook.createCellStyle();
                    style.setFillForegroundColor(IndexedColors.RED.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    targetCell.setCellStyle(style);
                }
            }

            // 변경된 내용을 파일에 저장
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                workbook.write(fileOutputStream);
            }

            System.out.println("엑셀 파일 수정이 완료되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

