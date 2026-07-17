package com.automapro.backend.controller;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.service.LicenciaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class PagoController {

    // Token de verificación único de tu cuenta de Hotmart (Hottok)
    @Value("${hotmart.hottok:}")
    private String hotmartHottok;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private LicenciaService licenciaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Webhook para recibir eventos de compra de Hotmart
     * POST /api/pagos/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookHotmart(
            @RequestBody String payload,
            @RequestHeader(value = "X-HOTMART-HOTTOK", required = false) String hottokHeader) {
        try {
            JsonNode evento = objectMapper.readTree(payload);

            // 1. Validar el Hottok (viene en el header y, como respaldo, en el cuerpo)
            String hottokRecibido = hottokHeader;
            if (hottokRecibido == null || hottokRecibido.isBlank()) {
                hottokRecibido = evento.path("hottok").asText(null);
            }
            if (hotmartHottok == null || hotmartHottok.isBlank()
                    || hottokRecibido == null || !hotmartHottok.equals(hottokRecibido)) {
                System.err.println("Webhook Hotmart: Hottok inválido o ausente");
                return ResponseEntity.status(401).build();
            }

            // 2. Procesar solo compras aprobadas
            String nombreEvento = evento.path("event").asText();
            String estado = evento.path("data").path("purchase").path("status").asText();

            if (!"PURCHASE_APPROVED".equals(nombreEvento) && !"APPROVED".equalsIgnoreCase(estado)) {
                System.out.println("Webhook Hotmart: evento ignorado (" + nombreEvento + " / " + estado + ")");
                return ResponseEntity.ok().build();
            }

            // 3. Extraer datos del comprador y del producto
            String emailComprador = evento.path("data").path("buyer").path("email").asText();
            String productoHotmartId = evento.path("data").path("product").path("id").asText();

            System.out.println("Compra Hotmart aprobada - Email: " + emailComprador
                    + " - Producto Hotmart: " + productoHotmartId);

            // 4. Mapear el producto de Hotmart a nuestra aplicación (columna hotmart_product_id)
            Optional<Aplicacion> aplicacionOpt = aplicacionRepository.findByHotmartProductId(productoHotmartId);
            if (aplicacionOpt.isEmpty()) {
                System.err.println("Webhook Hotmart: el producto " + productoHotmartId
                        + " no está mapeado a ninguna aplicación");
                return ResponseEntity.ok().build();
            }
            Long aplicacionId = aplicacionOpt.get().getId();

            // 5. Buscar la licencia del usuario por su correo y convertirla a FULL
            Optional<Licencia> licenciaOpt = licenciaRepository
                    .findByUsuarioEmailAndAplicacionId(emailComprador, aplicacionId);

            if (licenciaOpt.isEmpty()) {
                System.err.println("Webhook Hotmart: no se encontró licencia para " + emailComprador
                        + " en la aplicación " + aplicacionId + " (compra sin cuenta/trial previo)");
                return ResponseEntity.ok().build();
            }

            Licencia licencia = licenciaOpt.get();

            // Idempotencia: si ya es FULL, no hacer nada (Hotmart reintenta hasta 5 veces)
            if ("FULL".equals(licencia.getTipoLicencia())) {
                System.out.println("Webhook Hotmart: la licencia " + licencia.getId()
                        + " ya era FULL, no se hace nada");
                return ResponseEntity.ok().build();
            }

            licenciaService.convertirAFull(licencia.getId());
            System.out.println("Webhook Hotmart: licencia " + licencia.getId() + " convertida a FULL");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Error procesando webhook de Hotmart: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}