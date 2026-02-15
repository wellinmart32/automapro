package com.automapro.backend.controller;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
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

/**
 * Controlador para gestionar pagos con Stripe
 */
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

    /**
     * Crear sesión de checkout de Stripe
     * POST /api/pagos/crear-checkout
     */
    @PostMapping("/crear-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> crearCheckout(@RequestBody Map<String, Long> request) {
        try {
            // Configurar la API key de Stripe
            Stripe.apiKey = stripeApiKey;

            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener ID de la aplicación
            Long aplicacionId = request.get("aplicacionId");
            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Verificar que el usuario tenga una licencia TRIAL de esta aplicación
            Licencia licencia = licenciaRepository.findByUsuarioIdAndAplicacionId(usuario.getId(), aplicacionId)
                    .orElse(null);

            // Convertir precio de BigDecimal a centavos (long)
            long precioEnCentavos = aplicacion.getPrecio().multiply(new java.math.BigDecimal("100")).longValue();

            // Crear sesión de Stripe Checkout
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4200/pago-exitoso?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:4200/pago-cancelado")
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
     * POST /api/pagos/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookStripe(@RequestBody String payload) {
        // TODO: Implementar verificación de firma y procesamiento de eventos
        // Por ahora, solo retornamos OK
        return ResponseEntity.ok().build();
    }
}