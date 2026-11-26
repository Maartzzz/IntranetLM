package com.lmoving.intranet.operationimport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmoving.intranet.audit.AuditLogService;
import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.repository.CargoTypeRepository;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.operation.OperationRepository;
import com.lmoving.intranet.operation.OperationRequest;
import com.lmoving.intranet.operation.OperationService;
import com.lmoving.intranet.operationimport.dto.ImportDtos.*;

@Service
@Transactional
public class OperationImportService {
    private static final long MAX_FILE_BYTES = 20L * 1024 * 1024;
    private static final int MAX_DATA_ROWS = 5000;
    private static final List<String> FIELD_ORDER = List.of("reference", "client", "cargoType",
            "customerOrderNumber", "origin", "destination", "quantity", "estimatedDate", "observation");
    private static final Map<String, List<String>> ALIASES = aliases();

    private final OperationImportBatchRepository batchRepository;
    private final OperationImportFileRepository fileRepository;
    private final OperationImportProfileRepository profileRepository;
    private final ClientRepository clientRepository;
    private final CargoTypeRepository cargoTypeRepository;
    private final OperationRepository operationRepository;
    private final OperationService operationService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public OperationImportService(OperationImportBatchRepository batchRepository,
            OperationImportFileRepository fileRepository, OperationImportProfileRepository profileRepository,
            ClientRepository clientRepository, CargoTypeRepository cargoTypeRepository,
            OperationRepository operationRepository, OperationService operationService,
            AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.batchRepository = batchRepository;
        this.fileRepository = fileRepository;
        this.profileRepository = profileRepository;
        this.clientRepository = clientRepository;
        this.cargoTypeRepository = cargoTypeRepository;
        this.operationRepository = operationRepository;
        this.operationService = operationService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public PreviewResponse crearPrevisualizacion(MultipartFile file) {
        if (file == null || file.isEmpty()) throw badRequest("Selecciona un archivo Excel.");
        if (file.getSize() > MAX_FILE_BYTES) throw badRequest("El archivo supera el límite de 20 MB.");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("importacion.xlsx")
                .replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (name.isBlank()) name = "importacion.xlsx";
        if (name.length() > 255) name = name.substring(name.length() - 255);
        String lower = name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".xlsx") && !lower.endsWith(".xls")) {
            throw badRequest("Formato no válido. Solo se admiten archivos .xls y .xlsx.");
        }
        try {
            byte[] bytes = file.getBytes();
            WorkbookInfo info = inspect(bytes, null, null);
            OperationImportBatch batch = new OperationImportBatch();
            batch.setOriginalFileName(name);
            batch.setContentType(file.getContentType());
            batch.setStatus("PREVIEW");
            OperationImportBatch saved = batchRepository.saveAndFlush(batch);
            fileRepository.save(new OperationImportFile(saved, bytes));
            return previewResponse(saved.getId(), name, info);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo Excel.", ex);
        }
    }

    @Transactional(readOnly = true)
    public PreviewResponse inspeccionar(Long id, int sheetIndex, Integer headerRow) {
        OperationImportBatch batch = buscarBatch(id);
        byte[] bytes = obtenerArchivo(id);
        return previewResponse(id, batch.getOriginalFileName(), inspect(bytes, sheetIndex, headerRow));
    }

    public AnalyzeResponse analizar(Long id, AnalyzeRequest request) {
        OperationImportBatch batch = buscarBatch(id);
        List<AnalyzedRow> rows = analizarFilas(obtenerArchivo(id), request.sheetIndex(), request.headerRow(), request.mapping());
        int valid = (int) rows.stream().filter(r -> r.response().valid()).count();
        int duplicates = (int) rows.stream().filter(r -> r.response().possibleDuplicate()).count();
        int invalid = rows.size() - valid;
        batch.setStatus("ANALYZED");
        batch.setTotalRows(rows.size());
        batch.setMappingJson(writeJson(request.mapping()));
        batchRepository.save(batch);
        return new AnalyzeResponse(id, rows.size(), valid, duplicates, invalid,
                rows.stream().map(AnalyzedRow::response).toList());
    }

    public ExecuteResponse ejecutar(Long id, ExecuteRequest request) {
        OperationImportBatch batch = buscarBatch(id);
        if ("COMPLETED".equals(batch.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta importación ya fue ejecutada.");
        }
        if (request.selectedRows() == null || request.selectedRows().isEmpty()) throw badRequest("Selecciona al menos una fila para importar.");
        List<AnalyzedRow> rows = analizarFilas(obtenerArchivo(id), request.sheetIndex(), request.headerRow(), request.mapping());
        Set<Integer> selected = new HashSet<>(request.selectedRows());
        int created = 0;
        for (AnalyzedRow row : rows) {
            if (!selected.contains(row.response().rowNumber()) || !row.response().valid() || row.request() == null) continue;
            operationService.crear(row.request());
            created++;
        }
        batch.setStatus("COMPLETED");
        batch.setTotalRows(rows.size());
        batch.setCreatedRows(created);
        batch.setSkippedRows(rows.size() - created);
        batch.setMappingJson(writeJson(request.mapping()));
        batch.setCompletedAt(LocalDateTime.now());
        batchRepository.save(batch);
        auditLogService.registrar("OPERACIONES", "IMPORTACION_OPERACIONES", id, "COMPLETED",
                "Se completó la importación del archivo " + batch.getOriginalFileName(), null,
                "Filas=" + rows.size() + "; Operaciones creadas=" + created + "; Omitidas=" + (rows.size() - created));
        return new ExecuteResponse(id, batch.getStatus(), rows.size(), created,
                rows.size() - created, batch.getCompletedAt());
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> historial() {
        return batchRepository.findTop50ByOrderByCreatedAtDesc().stream().map(this::toHistory).toList();
    }

    @Transactional(readOnly = true)
    public DownloadFile descargarOriginal(Long id) {
        OperationImportBatch batch = buscarBatch(id);
        OperationImportFile file = fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo original no encontrado."));
        return new DownloadFile(batch.getOriginalFileName(), batch.getContentType(), file.getOriginalFile());
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> listarPerfiles() {
        return profileRepository.findAllByOrderByNameAsc().stream().map(this::toProfile).toList();
    }

    public ProfileResponse guardarPerfil(ProfileRequest request) {
        if (request.name() == null || request.name().isBlank()) throw badRequest("Indica un nombre para el perfil.");
        if (request.name().trim().length() > 100) throw badRequest("El nombre del perfil supera 100 caracteres.");
        validarMapping(request.mapping());
        OperationImportProfile profile = profileRepository.findByNameIgnoreCase(request.name().trim())
                .orElseGet(OperationImportProfile::new);
        profile.setName(request.name().trim());
        profile.setMappingJson(writeJson(request.mapping()));
        return toProfile(profileRepository.save(profile));
    }

    public void eliminarPerfil(Long id) {
        if (!profileRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado.");
        profileRepository.deleteById(id);
    }

    private List<AnalyzedRow> analizarFilas(byte[] bytes, int sheetIndex, int headerRow, Map<String, Integer> mapping) {
        validarMapping(mapping);
        if (mapping.get("client") == null || mapping.get("cargoType") == null) {
            throw badRequest("Debes asignar las columnas Cliente y Tipo de carga.");
        }
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) throw badRequest("La hoja seleccionada no existe.");
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (headerRow < 0 || headerRow >= Math.max(sheet.getLastRowNum() + 1, 1)) throw badRequest("La fila de encabezados no es válida.");
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("es-PE"));
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<Client> clients = clientRepository.findAllByActiveTrueOrderByBusinessNameAsc();
            List<CargoType> cargoTypes = cargoTypeRepository.findAllByOrderByNameAsc().stream().filter(CargoType::isActive).toList();
            Map<String, List<Client>> clientsByName = new HashMap<>();
            for (Client client : clients) {
                addClientMatch(clientsByName, normalize(client.getBusinessName()), client);
                addClientMatch(clientsByName, normalize(client.getDocumentNumber()), client);
            }
            Map<String, CargoType> cargoByName = cargoTypes.stream().collect(Collectors.toMap(
                    c -> normalize(c.getName()), c -> c, (a, b) -> a));
            List<Operation> existing = operationRepository.findAll();
            Set<String> knownKeys = new HashSet<>();
            for (Operation op : existing) {
                if (op.getClient() == null) continue;
                if (notBlank(op.getReference())) knownKeys.add("R|" + op.getClient().getId() + "|" + normalize(op.getReference()));
                if (notBlank(op.getCustomerOrderNumber())) knownKeys.add("P|" + op.getClient().getId() + "|" + normalize(op.getCustomerOrderNumber()));
            }
            Set<String> seenInFile = new HashSet<>();
            List<AnalyzedRow> result = new ArrayList<>();
            for (Row row : sheet) {
                int index = row.getRowNum();
                if (index <= headerRow || isBlankRow(row, formatter, evaluator)) continue;
                if (result.size() >= MAX_DATA_ROWS) throw badRequest("El archivo supera el máximo de 5,000 filas con datos.");
                Map<String, String> values = new LinkedHashMap<>();
                for (String field : FIELD_ORDER) values.put(field, valueAt(row, mapping.get(field), formatter, evaluator));
                List<String> issues = new ArrayList<>();
                List<Client> clientMatches = clientsByName.getOrDefault(normalize(values.get("client")), List.of());
                Client client = clientMatches.size() == 1 ? clientMatches.get(0) : null;
                CargoResolution cargoResolution = resolveCargoAndQuantity(values.get("cargoType"),
                        values.get("quantity"), cargoTypes, cargoByName, issues);
                CargoType cargo = cargoResolution.cargoType();
                Integer quantity = cargoResolution.quantity();
                if (clientMatches.isEmpty()) issues.add("Cliente no encontrado o inactivo en IntranetLM.");
                else if (clientMatches.size() > 1) issues.add("El cliente coincide con más de un registro; utiliza un dato más específico.");
                LocalDate estimatedDate = parseDate(row, mapping.get("estimatedDate"), values.get("estimatedDate"), formatter, evaluator, issues);
                if (cargo != null) values.put("cargoType", cargo.getName());
                values.put("quantity", quantity == null ? "" : quantity.toString());
                String reference = clean(values.get("reference"));
                String orderNumber = clean(values.get("customerOrderNumber"));
                validateLength(reference, 150, "La referencia supera 150 caracteres.", issues);
                validateLength(orderNumber, 100, "El pedido del cliente supera 100 caracteres.", issues);
                validateLength(clean(values.get("origin")), 200, "El origen supera 200 caracteres.", issues);
                validateLength(clean(values.get("destination")), 200, "El destino supera 200 caracteres.", issues);
                validateLength(clean(values.get("observation")), 1000, "La observación supera 1,000 caracteres.", issues);
                boolean duplicate = false;
                if (client != null) {
                    if (notBlank(reference)) {
                        String key = "R|" + client.getId() + "|" + normalize(reference);
                        duplicate = !seenInFile.add(key) || knownKeys.contains(key);
                    }
                    if (notBlank(orderNumber)) {
                        String key = "P|" + client.getId() + "|" + normalize(orderNumber);
                        duplicate = !seenInFile.add(key) || knownKeys.contains(key) || duplicate;
                    }
                }
                OperationRequest operationRequest = issues.isEmpty() && client != null && cargo != null
                        ? new OperationRequest(reference, client.getId(), null, cargo.getId(), orderNumber,
                            clean(values.get("origin")), clean(values.get("destination")), quantity,
                            estimatedDate, null, clean(values.get("observation"))) : null;
                RowResponse response = new RowResponse(index + 1, values, issues.isEmpty(), duplicate, List.copyOf(issues));
                result.add(new AnalyzedRow(response, operationRequest));
            }
            return result;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo procesar el archivo Excel.", ex);
        }
    }

    private WorkbookInfo inspect(byte[] bytes, Integer requestedSheet, Integer requestedHeader) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            if (workbook.getNumberOfSheets() == 0) throw badRequest("El archivo no contiene hojas.");
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("es-PE"));
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int selectedSheet = requestedSheet == null ? 0 : requestedSheet;
            int selectedHeader = requestedHeader == null ? 0 : requestedHeader;
            if (requestedSheet == null || requestedHeader == null) {
                int bestScore = -1;
                for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                    int candidate = detectHeaderRow(workbook.getSheetAt(s), formatter, evaluator);
                    int score = headerScore(workbook.getSheetAt(s).getRow(candidate), formatter, evaluator);
                    if (score > bestScore) { bestScore = score; selectedSheet = s; selectedHeader = candidate; }
                }
            }
            if (selectedSheet < 0 || selectedSheet >= workbook.getNumberOfSheets()) throw badRequest("La hoja seleccionada no existe.");
            Sheet sheet = workbook.getSheetAt(selectedSheet);
            if (selectedHeader < 0 || selectedHeader > Math.max(sheet.getLastRowNum(), 0)) throw badRequest("La fila de encabezados no es válida.");
            Row header = sheet.getRow(selectedHeader);
            if (header == null) throw badRequest("No se encontró la fila de encabezados.");
            int lastColumn = Math.max(header.getLastCellNum(), 0);
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < lastColumn; c++) {
                String value = formatter.formatCellValue(header.getCell(c), evaluator).trim();
                headers.add(value.isBlank() ? "Columna " + (c + 1) : value);
            }
            Map<String, Integer> suggestions = suggestMapping(headers);
            List<List<String>> sample = new ArrayList<>();
            for (Row data : sheet) {
                if (data.getRowNum() <= selectedHeader || sample.size() >= 10) continue;
                if (isBlankRow(data, formatter, evaluator)) continue;
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < headers.size(); c++) cells.add(valueAt(data, c, formatter, evaluator));
                sample.add(cells);
            }
            List<String> sheetNames = new ArrayList<>();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) sheetNames.add(workbook.getSheetName(s));
            return new WorkbookInfo(sheetNames, selectedSheet, selectedHeader, headers, suggestions, sample);
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof ResponseStatusException rse) throw rse;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no es un Excel válido o está dañado.", ex);
        }
    }

    private PreviewResponse previewResponse(Long id, String name, WorkbookInfo info) {
        return new PreviewResponse(id, name, info.sheets(), info.sheetIndex(), info.headerRow(),
                info.headers(), info.suggestedMapping(), info.sampleRows());
    }

    private int detectHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int bestRow = 0, bestScore = -1;
        for (int r = 0; r <= Math.min(sheet.getLastRowNum(), 19); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            int score = headerScore(row, formatter, evaluator);
            if (score > bestScore) { bestScore = score; bestRow = r; }
        }
        return bestRow;
    }

    private int headerScore(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) return 0;
        Set<String> matched = new HashSet<>();
        for (Cell cell : row) {
            String value = normalize(formatter.formatCellValue(cell, evaluator));
            for (String field : FIELD_ORDER) {
                if (ALIASES.get(field).contains(value)) matched.add(field);
            }
        }
        return matched.size();
    }

    private Map<String, Integer> suggestMapping(List<String> headers) {
        Map<String, Integer> mapping = new LinkedHashMap<>();
        for (String field : FIELD_ORDER) {
            for (int i = 0; i < headers.size(); i++) {
                if (ALIASES.get(field).contains(normalize(headers.get(i)))) { mapping.put(field, i); break; }
            }
        }
        return mapping;
    }

    private String valueAt(Row row, Integer column, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (column == null || column < 0) return "";
        Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (Cell cell : row) if (!formatter.formatCellValue(cell, evaluator).isBlank()) return false;
        return true;
    }

    private CargoResolution resolveCargoAndQuantity(String cargoRaw, String quantityRaw,
            List<CargoType> cargoTypes, Map<String, CargoType> cargoByName, List<String> issues) {
        String cargoText = clean(cargoRaw);
        String quantityText = clean(quantityRaw);
        String normalizedCargo = normalize(cargoText);

        // El dato FCL puede venir completo en la columna de modalidad o repartido
        // entre modalidad (FCL) y cantidad (1x40, 2*20, etc.).
        ContainerSpec parsedSpec = parseContainerSpec(cargoText);
        if (parsedSpec == null) parsedSpec = parseContainerSpec(quantityText);
        final ContainerSpec spec = parsedSpec;
        if (containsMultipleContainerSpecs(cargoText) || containsMultipleContainerSpecs(quantityText)) {
            issues.add("La fila contiene más de un tamaño o grupo de contenedores. Divide la información en filas separadas para no perder cantidades.");
            return new CargoResolution(null, null);
        }

        boolean fcl = normalizedCargo.equals("fcl") || normalizedCargo.startsWith("fcl ");
        boolean lcl = normalizedCargo != null && (normalizedCargo.equals("lcl")
                || normalizedCargo.contains("carga suelta") || normalizedCargo.contains("consolidada"));

        if (spec != null) {
            CargoType cargo = cargoTypes.stream().filter(CargoType::isContainer)
                    .filter(c -> normalize(c.getName()).matches(".*\\b" + spec.sizeFeet() + "\\b.*"))
                    .findFirst().orElse(null);
            if (cargo == null) {
                issues.add("No existe un tipo de contenedor activo de " + spec.sizeFeet() + " pies en el catálogo de IntranetLM.");
                return new CargoResolution(null, spec.count());
            }
            Integer separateQuantity = parseOptionalQuantity(quantityText);
            if (parseContainerSpec(quantityText) == null && separateQuantity != null && !separateQuantity.equals(spec.count())) {
                issues.add("La cantidad indicada (" + separateQuantity + ") no coincide con los contenedores descritos (" + spec.count() + ").");
            }
            return new CargoResolution(cargo, spec.count());
        }

        CargoType cargo = cargoByName.get(normalizedCargo);
        if (fcl && cargo == null) {
            issues.add("La modalidad FCL no indica el tamaño del contenedor. Usa un formato como FCL 1x40 o FCL 2*20.");
            return new CargoResolution(null, parseOptionalQuantity(quantityText));
        }
        if (cargo == null && lcl) {
            List<CargoType> nonContainer = cargoTypes.stream().filter(c -> !c.isContainer()).toList();
            cargo = nonContainer.stream().filter(c -> normalize(c.getName()).contains("carga suelta"))
                    .findFirst().orElse(nonContainer.size() == 1 ? nonContainer.get(0) : null);
        }
        if (cargo == null) {
            issues.add("Tipo de carga no reconocido. Indica FCL con tamaño (por ejemplo 1x40) o LCL.");
            return new CargoResolution(null, parseOptionalQuantity(quantityText));
        }
        if (cargo.isContainer()) {
            Integer size = containerSizeFromName(cargo.getName());
            if (size == null) {
                issues.add("El tipo de contenedor del catálogo no permite identificar si es de 20 o 40 pies.");
                return new CargoResolution(null, parseOptionalQuantity(quantityText));
            }
        }
        List<String> quantityIssues = new ArrayList<>();
        Integer quantity = parseQuantity(quantityText, quantityIssues);
        if (!quantityIssues.isEmpty()) issues.addAll(quantityIssues);
        if (cargo.isContainer() && quantity == null) {
            issues.add("Para FCL indica la cantidad de contenedores o usa un valor como 2x40 en la columna de modalidad.");
        }
        if (!cargo.isContainer() && quantity == null) {
            issues.add("Para LCL indica la cantidad de bultos en la columna Cantidad.");
        }
        return new CargoResolution(cargo, quantity);
    }

    private boolean containsMultipleContainerSpecs(String raw) {
        if (!notBlank(raw)) return false;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)\\d{1,3}\\s*[X*]\\s*(?:20|40)").matcher(raw);
        int matches = 0;
        while (matcher.find()) if (++matches > 1) return true;
        return false;
    }

    private ContainerSpec parseContainerSpec(String raw) {
        if (!notBlank(raw)) return null;
        String text = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT).replace('×', 'X');
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "(?:FCL\\s*)?(\\d{1,3})\\s*[X*]\\s*(20|40)(?:\\s*(?:HC|HQ|DV|GP|ST|RF|OT))?").matcher(text);
        if (!matcher.find()) return null;
        try {
            int count = Integer.parseInt(matcher.group(1));
            int size = Integer.parseInt(matcher.group(2));
            return count > 0 ? new ContainerSpec(count, size) : null;
        } catch (NumberFormatException ex) { return null; }
    }

    private Integer containerSizeFromName(String name) {
        String normalized = normalize(name);
        if (normalized.matches(".*\\b20\\b.*")) return 20;
        if (normalized.matches(".*\\b40\\b.*")) return 40;
        return null;
    }

    private Integer parseOptionalQuantity(String raw) {
        if (!notBlank(raw) || parseContainerSpec(raw) != null) return null;
        List<String> ignored = new ArrayList<>();
        return parseQuantity(raw, ignored);
    }

    private record ContainerSpec(int count, int sizeFeet) {}
    private record CargoResolution(CargoType cargoType, Integer quantity) {}

    private Integer parseQuantity(String raw, List<String> issues) {
        if (!notBlank(raw)) return null;
        try {
            String normalized = raw.trim().replaceAll("(?i)\\s*(bultos?|paquetes?|packages?|pkg|unidades?)\\s*$", "").replace(" ", "");
            if (normalized.matches("-?\\d{1,3}([.,]\\d{3})+")) normalized = normalized.replace(",", "").replace(".", "");
            else normalized = normalized.replace(",", ".");
            int value = new BigDecimal(normalized).intValueExact();
            if (value <= 0) throw new ArithmeticException("La cantidad debe ser positiva");
            return value;
        } catch (Exception ex) {
            issues.add("La cantidad debe ser un número entero positivo.");
            return null;
        }
    }

    private LocalDate parseDate(Row row, Integer column, String raw, DataFormatter formatter,
            FormulaEvaluator evaluator, List<String> issues) {
        if (!notBlank(raw)) return null;
        if (column != null) {
            Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        }
        List<DateTimeFormatter> formats = List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ofPattern("d-M-uuuu"),
                DateTimeFormatter.ofPattern("d.M.uuuu"), DateTimeFormatter.ofPattern("d/M/uuuu H:mm"),
                DateTimeFormatter.ofPattern("d-M-uuuu H:mm"));
        for (DateTimeFormatter format : formats) {
            try { return LocalDateTime.parse(raw.trim(), format).toLocalDate(); } catch (DateTimeParseException ignored) {}
            try { return LocalDate.parse(raw.trim(), format); } catch (DateTimeParseException ignored) {}
        }
        issues.add("La fecha estimada no tiene un formato reconocido.");
        return null;
    }

    private void validarMapping(Map<String, Integer> mapping) {
        if (mapping == null) throw badRequest("No se recibió el mapeo de columnas.");
        for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
            if (!FIELD_ORDER.contains(entry.getKey())) throw badRequest("El mapeo contiene un campo desconocido.");
            if (entry.getValue() != null && entry.getValue() < 0) throw badRequest("El mapeo contiene una columna no válida.");
        }
    }

    private byte[] obtenerArchivo(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo original no encontrado."))
                .getOriginalFile();
    }

    private OperationImportBatch buscarBatch(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Importación no encontrada."));
    }

    private HistoryResponse toHistory(OperationImportBatch b) {
        return new HistoryResponse(b.getId(), b.getOriginalFileName(), b.getStatus(), b.getTotalRows(),
                b.getCreatedRows(), b.getSkippedRows(), b.getCreatedAt(), b.getCompletedAt());
    }

    private ProfileResponse toProfile(OperationImportProfile p) {
        try {
            Map<String, Integer> mapping = objectMapper.readValue(p.getMappingJson(), new TypeReference<>() {});
            return new ProfileResponse(p.getId(), p.getName(), mapping, p.getUpdatedAt());
        } catch (JsonProcessingException ex) { throw new IllegalStateException("No se pudo leer el perfil de importación.", ex); }
    }

    private String writeJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("No se pudo guardar la configuración de importación.", ex); }
    }

    private static void validateLength(String value, int max, String message, List<String> issues) {
        if (value != null && value.length() > max) issues.add(message);
    }

    private static void addClientMatch(Map<String, List<Client>> lookup, String key, Client client) {
        if (key.isBlank()) return;
        List<Client> matches = lookup.computeIfAbsent(key, ignored -> new ArrayList<>());
        if (matches.stream().noneMatch(existing -> existing.getId().equals(client.getId()))) matches.add(client);
    }

    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static boolean notBlank(String value) { return value != null && !value.isBlank(); }
    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }
    private static ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }

    private static Map<String, List<String>> aliases() {
        Map<String, List<String>> a = new LinkedHashMap<>();
        a.put("reference", normalizedAliases("referencia", "ref", "referencia operacion", "nro referencia", "numero referencia", "operation reference"));
        a.put("client", normalizedAliases("cliente", "razon social", "razon social cliente", "consignatario", "importador", "exportador", "customer", "client name"));
        a.put("cargoType", normalizedAliases("tipo carga", "tipo de carga", "carga", "modalidad", "cargo type", "tipo servicio", "fcl lcl", "fcl/lcl"));
        a.put("customerOrderNumber", normalizedAliases("pedido cliente", "orden cliente", "orden compra", "oc", "numero pedido", "nro pedido", "booking", "customer order"));
        a.put("origin", normalizedAliases("origen", "puerto origen", "procedencia", "origin"));
        a.put("destination", normalizedAliases("destino", "puerto destino", "destino final", "destination"));
        a.put("quantity", normalizedAliases("cantidad", "cant", "contenedores", "numero contenedores", "cantidad contenedores", "bultos", "numero bultos", "unidades", "qty", "quantity"));
        a.put("estimatedDate", normalizedAliases("fecha estimada", "fecha llegada", "eta", "fecha operacion", "fecha", "estimated date"));
        a.put("observation", normalizedAliases("observacion", "observaciones", "comentario", "comentarios", "notas", "remarks"));
        return Collections.unmodifiableMap(a);
    }
    private static List<String> normalizedAliases(String... values) { return Arrays.stream(values).map(OperationImportService::normalize).toList(); }

    public record DownloadFile(String fileName, String contentType, byte[] content) {}
    private record WorkbookInfo(List<String> sheets, int sheetIndex, int headerRow, List<String> headers,
            Map<String, Integer> suggestedMapping, List<List<String>> sampleRows) {}
    private record AnalyzedRow(RowResponse response, OperationRequest request) {}
}
