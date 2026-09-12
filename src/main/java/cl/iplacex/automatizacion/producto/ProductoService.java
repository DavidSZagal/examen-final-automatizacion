package cl.iplacex.automatizacion.producto;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto crear(ProductoRequest request) {
        String nombreNormalizado = request.nombre().trim();

        if (productoRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre");
        }

        Producto producto = new Producto(
                nombreNormalizado,
                request.precio(),
                request.stock());

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto reducirStock(Long id, int cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Producto no encontrado"));

        producto.reducirStock(cantidad);

        return productoRepository.save(producto);
    }
}