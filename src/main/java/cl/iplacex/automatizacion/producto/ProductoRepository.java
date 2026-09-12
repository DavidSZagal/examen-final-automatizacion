package cl.iplacex.automatizacion.producto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}