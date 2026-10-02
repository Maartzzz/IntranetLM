package com.lmoving.intranet.question;

import com.lmoving.intranet.question.dto.QuestionImportRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component  
public class QuestionExcelReader {

    public List<QuestionImportRow> read(InputStream inputStream) {

        List<QuestionImportRow> rows = new ArrayList<>();

        try (
            Workbook workbook = WorkbookFactory.create(inputStream)
        ) {

            Sheet sheet = workbook.getSheetAt(0);

            boolean firstRow = true;

            for (Row row : sheet) {

                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                if (isEmpty(row)) {
                    continue;
                }

                String question = getString(row, 0);
                String option1 = getString(row, 1);
                String option2 = getString(row, 2);
                String option3 = getString(row, 3);
                String option4 = getString(row, 4);

                Integer correct = getInteger(row, 5);

                Boolean active = getBoolean(row, 6);

                rows.add(
                    new QuestionImportRow(
                        question,
                        option1,
                        option2,
                        option3,
                        option4,
                        correct,
                        active
                    )
                );
            }

            return rows;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                "No se pudo leer el archivo Excel",
                e
            );
        }
    }

    private String getString(Row row, int column) {

        Cell cell = row.getCell(column);

        if (cell == null) {
            return null;
        }

        return cell.toString().trim();
    }

    private Integer getInteger(Row row, int column) {

        Cell cell = row.getCell(column);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        String value = cell.toString().trim();

        if (value.isEmpty()) {
            return null;
        }

        return Integer.valueOf(value);
    }

    private Boolean getBoolean(Row row, int column) {

        String value = getString(row, column);

        if (value == null || value.isBlank()) {
            return true;
        }

        return value.equalsIgnoreCase("SI")
            || value.equalsIgnoreCase("SÍ") 
            || value.equalsIgnoreCase("TRUE");
    }

    private boolean isEmpty(Row row) {

        for (int i = 0; i < 7; i++) {

            Cell cell = row.getCell(i);

            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }

        return true;
    }
}