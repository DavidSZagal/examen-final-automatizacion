package cl.iplacex.automatizacion.producto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/productos")
@Validated
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(
            @Valid @RequestBody ProductoRequest request) {

        return productoService.crear(request);
    }

    @PatchMapping("/{id}/stock")
    public Producto reducirStock(
            @PathVariable Long id,
            @RequestParam
            @Positive(message = "La cantidad debe ser mayor que cero")
            int cantidad) {

        return productoService.reducirStock(id, cantidad);
    }
}