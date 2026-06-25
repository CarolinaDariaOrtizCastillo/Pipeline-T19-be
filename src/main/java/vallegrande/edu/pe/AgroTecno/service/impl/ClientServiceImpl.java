package vallegrande.edu.pe.AgroTecno.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vallegrande.edu.pe.AgroTecno.model.Client;
import vallegrande.edu.pe.AgroTecno.repository.ClientRepository;
import vallegrande.edu.pe.AgroTecno.service.ClientService;

@Service
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);
    private final ClientRepository clientRepository;

    private final TemplateEngine templateEngine;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, TemplateEngine templateEngine) {
        this.clientRepository = clientRepository;
        this.templateEngine = templateEngine;
    }

    private LocalDateTime getCurrentLimaTime() {
        return LocalDateTime.now(ZoneId.of("America/Lima"));
    }
    
    private LocalDateTime convertLimaToUTC(LocalDateTime limaTime) {
        if (limaTime == null) return null;
        ZonedDateTime limaZoned = limaTime.atZone(ZoneId.of("America/Lima"));
        ZonedDateTime utcZoned = limaZoned.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZoned.toLocalDateTime();
    }
    
    private LocalDateTime convertUTCToLima(LocalDateTime utcTime) {
        if (utcTime == null) return null;
        ZonedDateTime utcZoned = utcTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime limaZoned = utcZoned.withZoneSameInstant(ZoneId.of("America/Lima"));
        return limaZoned.toLocalDateTime();
    }

    @Override
    public List<Client> findAll() {
        log.info("Listando Todos los Clientes");
        List<Client> clients = clientRepository.findAll();
        
        // Opcional: Convertir fechas a Lima para mostrar
        clients.forEach(client -> {
            log.info("Cliente: {} - Creado UTC: {}, Creado Lima: {}", 
                client.getName(), 
                client.getCreatedAt(),
                convertUTCToLima(client.getCreatedAt()));
        });
        
        return clients;
    }

    @Override
    public List<Client> findByEstado(Boolean estado) {
        log.info("Listando Clientes por Estado: " + estado);
        return clientRepository.findByEstado(estado);
    }

    @Override
    public Client findById(Integer id) {
        log.info("Listando Cliente por ID: " + id);
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    @Override
    public Client save(Client client) {
        LocalDateTime nowInLima = getCurrentLimaTime();
        LocalDateTime nowInUTC = convertLimaToUTC(nowInLima);
        
        log.info("Registrando Cliente - Hora Lima: " + nowInLima.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("Registrando Cliente - Hora UTC: " + nowInUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Guardar en UTC
        client.setCreatedAt(nowInUTC);
        client.setUpdatedAt(nowInUTC);
        client.setEstado(true);
        client.setDeletedAt(null);
        client.setRestoredAt(null);
        
        Client saved = clientRepository.save(client);
        log.info("Cliente guardado - Fecha UTC en BD: " + saved.getCreatedAt());
        
        return saved;
    }

    @Override
    public Client update(Client client) {
        log.info("Editando Cliente: " + client);

        Client existing = clientRepository.findById(client.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + client.getClientId()));

        // Preservar la fecha de creación original (ya está en UTC)
        client.setCreatedAt(existing.getCreatedAt());
        
        // Actualizar fecha en UTC
        LocalDateTime nowInUTC = convertLimaToUTC(getCurrentLimaTime());
        client.setUpdatedAt(nowInUTC);
        client.setEstado(true);
        
        // Preservar fechas de auditoría anteriores
        client.setDeletedAt(existing.getDeletedAt());
        client.setRestoredAt(existing.getRestoredAt());

        return clientRepository.save(client);
    }

    @Override
    public Client delete(Integer id) {
        log.info("Eliminando Cliente: " + id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        client.setEstado(false);
        client.setDeletedAt(convertLimaToUTC(getCurrentLimaTime()));
        client.setUpdatedAt(convertLimaToUTC(getCurrentLimaTime()));

        return clientRepository.save(client);
    }

    @Override
    public Client restore(Integer id) {
        log.info("Restaurando Cliente: " + id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        client.setEstado(true);
        client.setRestoredAt(convertLimaToUTC(getCurrentLimaTime()));
        client.setUpdatedAt(convertLimaToUTC(getCurrentLimaTime()));

        return clientRepository.save(client);
    }

     // 🛠️✅ Implementación del método Importar CSV
    public void importCsv(MultipartFile file) throws Exception {
        log.info("Importando CSV: " + file.getOriginalFilename());
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        List<String[]> rows = reader.readAll();
        List<Client> customers = new ArrayList<>();
        for(int i=1; i<rows.size(); i++){
            String[] row = rows.get(i);
            Client customer = new Client();
            
            // Corregido según Client.java
            customer.setDocumentType("DNI"); 
            customer.setDocumentNumber(row[0]);
            customer.setPhone(row[1]);
            customer.setName(row[2]);
            customer.setLastName(row[3]);
            customer.setEstado("true".equalsIgnoreCase(row[4]) || "activo".equalsIgnoreCase(row[4]));
            
            // Fechas requeridas
            customer.setRegistrationDate(LocalDateTime.now());
            customer.setCreatedAt(LocalDateTime.now());
            
            customers.add(customer);
        }
        clientRepository.saveAll(customers);
    }

    // 🛠️✅ Implementación del método Exportar PDF
    public byte[] exportPdf() throws Exception {
        log.info("Exportando PDF de clientes");
        Context context = new Context();
        
        // 1. CORREGIDO: Se cambia "customer" por "clients" para que coincida con el HTML
        context.setVariable("clients", clientRepository.findAll());
        
        // 2. CORREGIDO: Se cambia "customer" por "clients" (el nombre real de tu archivo html)
        String html = templateEngine.process("clients", context);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();
        return out.toByteArray();
    }

    // 🛠️✅ Implementación del método Exportar Excel
    public byte[] exportExcel() throws Exception {
        log.info("Exportando Excel");
        List<Client> customers = clientRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Customers");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("DNI");
        header.createCell(1).setCellValue("CELULAR");
        header.createCell(2).setCellValue("NOMBRE");
        header.createCell(3).setCellValue("APELLIDO");
        int rowNum = 1;
        for(Client c : customers){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getDocumentNumber());
            row.createCell(1).setCellValue(c.getPhone());
            row.createCell(2).setCellValue(c.getName());
            row.createCell(3).setCellValue(c.getLastName());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
