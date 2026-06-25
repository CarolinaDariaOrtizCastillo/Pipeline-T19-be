package vallegrande.edu.pe.AgroTecno.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.AgroTecno.model.Client;
import vallegrande.edu.pe.AgroTecno.service.ClientService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/client")
@Tag(name = "Client API", description = "API para la gestión de Clientes")
public class ClientRest {

    private final ClientService clientService;

    @Autowired
    public ClientRest(ClientService clientService) {
        this.clientService = clientService;
    }

    // GET - Listar todos
    @GetMapping
    @Operation(summary = "Listar todos los Clientes")
    public List<Client> findAll(){
        return clientService.findAll();
    }

    // GET - Listar por estado
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar por estado")
    public List<Client> findByEstado(@PathVariable Boolean estado) {
        return clientService.findByEstado(estado);
    }

    // GET - Buscar por ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID")
    public Client findById(@PathVariable Integer id) {
        return clientService.findById(id);
    }

    // POST - Crear
    @PostMapping("/save")
    @Operation(summary = "Crear Cliente")
    public Client save(@RequestBody Client client) {
        return clientService.save(client);
    }

    // PUT - Actualizar
    @PutMapping("/update/{id}")
    @Operation(summary = "Actualizar Cliente")
    public Client update(@PathVariable Integer id, @RequestBody Client client) {
        client.setClientId(id);
        return clientService.update(client);
    }

    // PATCH - Eliminar lógico
    @PatchMapping("/delete/{id}")
    @Operation(summary = "Eliminar lógico")
    public Client delete(@PathVariable Integer id) {
        return clientService.delete(id);
    }

    // PATCH - Restaurar
    @PatchMapping("/restore/{id}")
    @Operation(summary = "Restaurar")
    public Client restore(@PathVariable Integer id) {
        return clientService.restore(id);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) throws Exception {
        clientService.importCsv(file);
        return ResponseEntity.ok("CSV importado correctamente");
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf() throws Exception {
        byte[] pdf = clientService.exportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() throws Exception {
        byte[] excel = clientService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(excel);
    }
}
