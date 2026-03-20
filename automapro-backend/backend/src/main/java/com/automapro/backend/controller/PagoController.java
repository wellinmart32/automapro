package com.automapro.backend.controller;

import com.automapro.backend.entity.Licencia;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.service.LicenciaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class PagoController {

    @Value("${lemonsqueezy.api.key}")
    private String lsApiKey;

    @Value("${lemonsqueezy.webhook.secret}")
    private String lsWebhookSecret;

    @Value("${lemonsqueezy.variant.id}")
    private String lsVariantId;

    @Value("${lemonsqueezy.store.id}")
    private String lsStoreId;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LicenciaService licenciaService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crear checkout en Lemon Squeezy
     */
    @PostMapping("/crear-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> crearCheckout(@RequestBody Map<String, Long> request) {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Long aplicacionId = request.get("aplicacionId");

            // Buscar licencia TRIAL del usuario
            Licencia licencia = licenciaRepository
                    .findByUsuarioEmailAndAplicacionId(email, aplicacionId)
                    .orElse(null);

            // Construir body del checkout
            Map<String, Object> checkoutData = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();
            Map<String, Object> relationships = new HashMap<>();
            Map<String, Object> variant = new HashMap<>();
            Map<String, Object> variantData = new HashMap<>();
            Map<String, Object> checkoutOptions = new HashMap<>();
            Map<String, Object> checkoutData2 = new HashMap<>();
            Map<String, Object> custom = new HashMap<>();

            // Metadata para identificar la licencia
            custom.put("licenciaId", licencia != null ? licencia.getId().toString() : "nueva");
            custom.put("aplicacionId", aplicacionId.toString());
            custom.put("usuarioEmail", email);

            checkoutData2.put("custom", custom);
            checkoutOptions.put("embed", false);
            checkoutOptions.put("success_url", "https://automapro-frontend.vercel.app/cliente/pago-exitoso");
            checkoutOptions.put("cancel_url", "https://automapro-frontend.vercel.app/cliente/pago-cancelado");

            attributes.put("checkout_options", checkoutOptions);
            attributes.put("checkout_data", checkoutData2);
            attributes.put("expires_at", (Object) null);

            variantData.put("type", "variants");
            variantData.put("id", lsVariantId);
            variant.put("data", variantData);
            relationships.put("variant", variant);

            Map<String, Object> store = new HashMap<>();
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("type", "stores");
            storeData.put("id", lsStoreId);
            store.put("data", storeData);
            relationships.put("store", store);

            data.put("type", "checkouts");
            data.put("attributes", attributes);
            data.put("relationships", relationships);
            checkoutData.put("data", data);

            // Llamar a la API de Lemon Squeezy
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + lsApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/vnd.api+json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(checkoutData, headers);

            ResponseEntity<String> lsResponse = restTemplate.exchange(
                    "https://api.lemonsqueezy.com/v1/checkouts",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Extraer URL del checkout
            JsonNode responseNode = objectMapper.readTree(lsResponse.getBody());
            String checkoutUrl = responseNode
                    .path("data")
                    .path("attributes")
                    .path("url")
                    .asText();

            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", checkoutUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error creando checkout Lemon Squeezy: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear sesión de pago: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Webhook para recibir eventos de Lemon Squeezy
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookLemonSqueezy(
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {
        try {
            // Verificar firma del webhook
            if (!verificarFirma(payload, signature)) {
                System.err.println("Firma de webhook inválida");
                return ResponseEntity.status(401).build();
            }

            JsonNode event = objectMapper.readTree(payload);
            String eventName = event.path("meta").path("event_name").asText();

            // Procesar solo order_created
            if ("order_created".equals(eventName)) {
                JsonNode meta = event.path("meta");
                JsonNode customData = meta.path("custom_data");

                String licenciaIdStr = customData.path("licenciaId").asText();
                String usuarioEmail = customData.path("usuarioEmail").asText();
                String aplicacionIdStr = customData.path("aplicacionId").asText();

                System.out.println("Pago recibido - Email: " + usuarioEmail + " - Licencia: " + licenciaIdStr);

                // Convertir licencia TRIAL a FULL si existe
                if (licenciaIdStr != null && !"nueva".equals(licenciaIdStr)) {
                    Long licenciaId = Long.parseLong(licenciaIdStr);
                    licenciaService.convertirAFull(licenciaId);
                    System.out.println("Licencia " + licenciaId + " convertida a FULL");
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verificar firma HMAC del webhook de Lemon Squeezy
     */
    private boolean verificarFirma(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    lsWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            System.err.println("Error verificando firma: " + e.getMessage());
            return false;
        }
    }
}