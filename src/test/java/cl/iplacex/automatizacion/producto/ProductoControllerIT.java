package cl.iplacex.automatizacion.producto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        productoRepository.deleteAll();
    }

    @Test
    void debeCrearYListarProducto() throws Exception {
        String productoJson = """
                {
                    "nombre": "Teclado",
                    "precio": 25990.00,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Teclado"))
                .andExpect(jsonPath("$.stock").value(10));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }

    @Test
    void debeRechazarProductoConDatosInvalidos() throws Exception {
        String productoJson = """
                {
                    "nombre": " ",
                    "precio": 1000.00,
                    "stock": 1
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El nombre es obligatorio"));
    }

    @Test
    void debeReducirStockDelProducto() throws Exception {
        Producto producto = productoRepository.save(
                new Producto(
                        "Mouse",
                        new BigDecimal("15990.00"),
                        5));

        mockMvc.perform(patch(
                "/api/productos/{id}/stock",
                producto.getId())
                .param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mouse"))
                .andExpect(jsonPath("$.stock").value(3));
    }
}