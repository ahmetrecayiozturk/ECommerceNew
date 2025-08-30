package com.ecommerce.deliveryservice.api;

import com.ecommerce.deliveryservice.application.DeliveryApplicationService;
import com.ecommerce.deliveryservice.domain.aggregate.Delivery;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryApplicationService deliveryApplicationService;

    private final Environment environment;

    public DeliveryController(DeliveryApplicationService deliveryApplicationService, Environment environment) {
        this.deliveryApplicationService = deliveryApplicationService;
        this.environment = environment;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody DeliveryCreateRequest deliveryCreateRequest) throws IOException {
            deliveryApplicationService.createDelivery(deliveryCreateRequest);
            return ResponseEntity.ok("Delivery added successfully");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Delivery>> getAllProducts() {
        return ResponseEntity.ok((List<Delivery>) deliveryApplicationService.getAllDelivery());
    }
    @PostMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        String port = environment.getProperty("local.server.port");
        String msg = "Delivery Service is up and running on port: " + port;
        System.out.println(msg);
        return ResponseEntity.ok(msg);
    }
}
    /*
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody PaymentUpdateRequest paymentUpdateRequest) throws IOException {
        paymentApplicationService.updateProduct(paymentUpdateRequest);
        return ResponseEntity.ok("Product updated successfully");
    }*/