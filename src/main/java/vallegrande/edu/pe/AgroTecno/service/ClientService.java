package vallegrande.edu.pe.AgroTecno.service;

import java.util.List;

import vallegrande.edu.pe.AgroTecno.model.Client;

import org.springframework.web.multipart.MultipartFile;

public interface ClientService {
    List<Client> findAll();
    List<Client> findByEstado(Boolean estado);
    Client findById(Integer id);
    Client save(Client client);
    Client update(Client client);
    Client delete(Integer id);                // Nuevo - eliminar con retorno
    Client restore(Integer id);               // Nuevo - restaurar con retorno

    // ⚙️✅ Definir método Importar CSV
    void importCsv(MultipartFile file) throws Exception;

    // ⚙️✅ Definir método Exportar PDF
    byte[] exportPdf() throws Exception;

    // ⚙️✅ Definir método Exportar Excel
    byte[] exportExcel() throws Exception;
}