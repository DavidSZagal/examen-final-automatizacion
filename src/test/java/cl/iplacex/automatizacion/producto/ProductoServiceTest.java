package cl.iplacex.automatizacion.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void debeCrearProductoCuandoNombreNoExiste() {
        ProductoRequest request = new ProductoRequest(
                "Teclado",
                new BigDecimal("25990.00"),
                10);

        when(productoRepository
                .existsByNombreIgnoreCase("Teclado"))
                .thenReturn(false);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto resultado = productoService.crear(request);

        assertEquals("Teclado", resultado.getNombre());
        assertEquals(new BigDecimal("25990.00"), resultado.getPrecio());
        assertEquals(10, resultado.getStock());

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void debeRechazarProductoDuplicado() {
        ProductoRequest request = new ProductoRequest(
                "Mouse",
                new BigDecimal("15990.00"),
                5);

        when(productoRepository
                .existsByNombreIgnoreCase("Mouse"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crear(request));

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void debeReducirStockDisponible() {
        Producto producto = new Producto(
                "Monitor",
                new BigDecimal("189990.00"),
                8);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.save(producto))
                .thenReturn(producto);

        Producto resultado = productoService.reducirStock(1L, 3);

        assertEquals(5, resultado.getStock());
        verify(productoRepository).save(producto);
    }

    @Test
    void debeRechazarReduccionConStockInsuficiente() {
        Producto producto = new Producto(
                "Notebook",
                new BigDecimal("599990.00"),
                1);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        assertThrows(
                IllegalStateException.class,
                () -> productoService.reducirStock(1L, 2));

        verify(productoRepository, never()).save(producto);
    }
}