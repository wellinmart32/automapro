package com.automapro.backend.controller;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.service.LicenciaService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class PagoController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LicenciaService licenciaService;

    /**
     * Crear sesión de checkout de Stripe
     */
    @PostMapping("/crear-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> crearCheckout(@RequestBody Map<String, Long> request) {
        try {
            Stripe.apiKey = stripeApiKey;

            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener aplicación
            Long aplicacionId = request.get("aplicacionId");
            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Buscar licencia TRIAL del usuario
            Licencia licencia = licenciaRepository.findByUsuarioIdAndAplicacionId(usuario.getId(), aplicacionId)
                    .orElse(null);

            // Convertir precio a centavos
            long precioEnCentavos = aplicacion.getPrecio().multiply(new java.math.BigDecimal("100")).longValue();

            // Crear sesión de Stripe Checkout
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4200/cliente/pago-exitoso?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:4200/cliente/pago-cancelado")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(precioEnCentavos)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(aplicacion.getNombre() + " - Versión Completa")
                                                                    .setDescription("Licencia completa para " + aplicacion.getNombre())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("aplicacionId", aplicacionId.toString())
                    .putMetadata("usuarioId", usuario.getId().toString())
                    .putMetadata("licenciaId", licencia != null ? licencia.getId().toString() : "nueva")
                    .build();

            Session session = Session.create(params);

            // Retornar URL de checkout
            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", session.getUrl());
            response.put("sessionId", session.getId());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear sesión de pago: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Webhook para recibir eventos de Stripe
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookStripe(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Parsear el evento de Stripe
            Event event = Event.GSON.fromJson(payload, Event.class);

            // Procesar solo eventos de checkout completado
            if ("checkout.session.completed".equals(event.getType())) {
                // Obtener datos de la sesión
                Map<String, Object> dataObject = (Map<String, Object>) event.getDataObjectDeserializer().getObject().get();
                Map<String, String> metadata = (Map<String, String>) dataObject.get("metadata");

                String licenciaIdStr = metadata.get("licenciaId");
                
                // Si hay licencia existente, convertirla a FULL
                if (licenciaIdStr != null && !"nueva".equals(licenciaIdStr)) {
                    Long licenciaId = Long.parseLong(licenciaIdStr);
                    licenciaService.convertirAFull(licenciaId);
                }
                
                System.out.println("Pago procesado exitosamente - Licencia ID: " + licenciaIdStr);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}