package com.lmoving.intranet.question;

import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.courses.CoursesRepository;
import com.lmoving.intranet.question.dto.QuestionImportRow;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionImportService {

    private final QuestionExcelReader excelReader;
    private final QuestionImportValidator validator;

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final CourseQuestionRepository courseQuestionRepository;
    private final CoursesRepository courseRepository;

    public QuestionImportService(
        QuestionExcelReader excelReader,
        QuestionImportValidator validator,
        QuestionRepository questionRepository,
        QuestionOptionRepository questionOptionRepository,
        CourseQuestionRepository courseQuestionRepository,
        CoursesRepository courseRepository
    ) {
        this.excelReader = excelReader;
        this.validator = validator;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.courseQuestionRepository = courseQuestionRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public List<QuestionImportRow> importQuestions(
        Long courseId,
        InputStream inputStream
    ) {

        List<QuestionImportRow> rows =
            excelReader.read(inputStream);

        validator.validate(rows);

        Courses course = courseRepository.findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el curso con id: " + courseId
                )
            );

        for (QuestionImportRow row : rows) {

            Question question = new Question();

            question.setQuestionText(
                row.question().trim()
            );

            question.setActive(
                row.active() == null
                    || row.active()
            );

            question = questionRepository.save(question);

            List<QuestionOption> options =
                createOptions(question, row);

            questionOptionRepository.saveAll(options);

            CourseQuestion courseQuestion =
                new CourseQuestion();

            courseQuestion.setCourse(course);
            courseQuestion.setQuestion(question);

            courseQuestionRepository.save(
                courseQuestion
            );
        }

        return rows;
    }

    private List<QuestionOption> createOptions(
        Question question,
        QuestionImportRow row
    ) {

        List<QuestionOption> options =
            new ArrayList<>();

        addOption(
            options,
            question,
            row.option1(),
            row.correct() == 1
        );

        addOption(
            options,
            question,
            row.option2(),
            row.correct() == 2
        );

        addOption(
            options,
            question,
            row.option3(),
            row.correct() == 3
        );

        addOption(
            options,
            question,
            row.option4(),
            row.correct() == 4
        );

        return options;
    }

    private void addOption(
        List<QuestionOption> options,
        Question question,
        String optionText,
        boolean correct
    ) {

        if (optionText == null
            || optionText.isBlank()) {

            return;
        }

        QuestionOption option =
            new QuestionOption();

        option.setQuestion(question);
        option.setOptionText(optionText.trim());
        option.setCorrect(correct);

        options.add(option);
    }

    @Transactional(readOnly = true)
    public List<QuestionImportRow> readAndValidate(
        InputStream inputStream
    ) {

        List<QuestionImportRow> rows =
            excelReader.read(inputStream);

        validator.validate(rows);

        return rows;
    }

    public byte[] generateTemplate() {
    
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                 new ByteArrayOutputStream()) {
    
            Sheet sheet = workbook.createSheet("Preguntas");
    
            Row header = sheet.createRow(0);
    
            header.createCell(0).setCellValue("pregunta");
            header.createCell(1).setCellValue("opcion_1");
            header.createCell(2).setCellValue("opcion_2");
            header.createCell(3).setCellValue("opcion_3");
            header.createCell(4).setCellValue("opcion_4");
            header.createCell(5).setCellValue("correcta");
            header.createCell(6).setCellValue("activa");
    
            Row example = sheet.createRow(1);
    
            example.createCell(0).setCellValue(
                "¿Cuál es el equipo de protección obligatorio?"
            );
            example.createCell(1).setCellValue("Casco");
            example.createCell(2).setCellValue("Sandalias");
            example.createCell(3).setCellValue("Gorra");
            example.createCell(4).setCellValue("Ninguno");
            example.createCell(5).setCellValue(1);
            example.createCell(6).setCellValue("SI");
    
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
    
            workbook.write(outputStream);
    
            return outputStream.toByteArray();
    
        } catch (Exception e) {
            throw new IllegalStateException(
                "No se pudo generar la plantilla Excel.",
                e
            );
        }
    }
}