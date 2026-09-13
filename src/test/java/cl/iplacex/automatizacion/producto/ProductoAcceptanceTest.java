package cl.iplacex.automatizacion.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductoAcceptanceTest {

    @LocalServerPort
    private int puerto;

    @Autowired
    private ProductoRepository productoRepository;

    private HttpClient httpClient;

    @BeforeEach
    void prepararEscenario() {
        productoRepository.deleteAll();

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    void clientePuedeCompletarFlujoDeCompra() throws Exception {
        String productoJson = """
                {
                    "nombre": "Audifonos Gamer",
                    "precio": 49990.00,
                    "stock": 6
                }
                """;

        HttpResponse<String> creacion =
                crearProducto(productoJson);

        assertEquals(201, creacion.statusCode());
        assertTrue(creacion.body()
                .contains("\"nombre\":\"Audifonos Gamer\""));
        assertTrue(creacion.body().contains("\"stock\":6"));

        long productoId = extraerId(creacion.body());

        HttpResponse<String> consulta = consultarProductos();

        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("Audifonos Gamer"));

        HttpResponse<String> reduccion =
                reducirStock(productoId, 2);

        assertEquals(200, reduccion.statusCode());
        assertTrue(reduccion.body().contains("\"stock\":4"));
    }

    @Test
    void clienteRecibeErrorSiElStockEsInsuficiente()
            throws Exception {

        String productoJson = """
                {
                    "nombre": "Mouse Inalambrico",
                    "precio": 15990.00,
                    "stock": 2
                }
                """;

        HttpResponse<String> creacion =
                crearProducto(productoJson);

        long productoId = extraerId(creacion.body());

        HttpResponse<String> respuesta =
                reducirStock(productoId, 5);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body()
                .contains("Stock insuficiente"));
    }

    private HttpResponse<String> crearProducto(String json)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(crearUri("/api/productos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> consultarProductos()
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(crearUri("/api/productos"))
                .GET()
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> reducirStock(
            long productoId,
            int cantidad) throws Exception {

        String ruta = "/api/productos/"
                + productoId
                + "/stock?cantidad="
                + cantidad;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(crearUri(ruta))
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private URI crearUri(String ruta) {
        return URI.create(
                "http://localhost:" + puerto + ruta);
    }

    private long extraerId(String json) {
        Pattern patron = Pattern.compile(
                "\"id\"\\s*:\\s*(\\d+)");

        Matcher coincidencia = patron.matcher(json);

        assertTrue(
                coincidencia.find(),
                "La respuesta debe contener el identificador");

        return Long.parseLong(coincidencia.group(1));
    }
}