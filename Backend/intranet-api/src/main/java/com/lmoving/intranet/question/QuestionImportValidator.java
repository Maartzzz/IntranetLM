package com.lmoving.intranet.question;

import com.lmoving.intranet.question.dto.QuestionImportRow;

import java.util.List;

import org.springframework.stereotype.Component;

@Component 
public class QuestionImportValidator {

    public void validate(List<QuestionImportRow> rows) {

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException(
                "El archivo no contiene preguntas."
            );
        }

        for (int i = 0; i < rows.size(); i++) {

            QuestionImportRow row = rows.get(i);

            validateRow(row, i + 2);
        }
    }

    private void validateRow(
        QuestionImportRow row,
        int excelRow
    ) {

        if (row.question() == null
            || row.question().isBlank()) {

            throw new IllegalArgumentException(
                "Fila " + excelRow
                + ": la pregunta es obligatoria."
            );
        }

        if (row.option1() == null
            || row.option1().isBlank()) {

            throw new IllegalArgumentException(
                "Fila " + excelRow
                + ": la opción 1 es obligatoria."
            );
        }

        if (row.option2() == null
            || row.option2().isBlank()) {

            throw new IllegalArgumentException(
                "Fila " + excelRow
                + ": la opción 2 es obligatoria."
            );
        }

        if (row.correct() == null
            || row.correct() < 1
            || row.correct() > 4) {

            throw new IllegalArgumentException(
                "Fila " + excelRow
                + ": 'correcta' debe ser 1, 2, 3 o 4."
            );
        }

        String correctOption = getOption(
            row,
            row.correct()
        );

        if (correctOption == null
            || correctOption.isBlank()) {

            throw new IllegalArgumentException(
                "Fila " + excelRow
                + ": la opción indicada como correcta no existe."
            );
        }
    }

    private String getOption(
        QuestionImportRow row,
        int optionNumber
    ) {

        return switch (optionNumber) {

            case 1 -> row.option1();

            case 2 -> row.option2();

            case 3 -> row.option3();

            case 4 -> row.option4();

            default -> null;
        };
    }
}