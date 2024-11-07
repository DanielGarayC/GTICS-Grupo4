package com.example.gtics;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DNIAPI {

    //https://apis.net.pe/
    private static final String BASE_URL = "https://api.apis.net.pe/v2/reniec/dni?numero=";

    private static final String BEARER_TOKEN = "apis-token-11398.m8hg4mraeI1mBseN1kGKIO12fRP4wK6G";

    public static List<String> getDni(String dni) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + dni;
        List<String> error = new ArrayList<>();

        // Configurar los encabezados
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + BEARER_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realizar la solicitud
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Imprimir la respuesta para depuración
            System.out.println(response);

            // Verificar el código de estado de la respuesta
            if (response.getStatusCode().is5xxServerError()) {
                System.out.println("Error del servidor: " + response.getStatusCodeValue());
                return error; // Devolver lista vacía en caso de error del servidor
            } else if (response.getStatusCode().is2xxSuccessful()) {
                // Convertir la respuesta JSON a lista de cadenas
                return responseToList(response);
            } else {
                System.out.println("Respuesta inesperada: " + response.getStatusCodeValue());
                return error; // Manejar otros códigos de estado si es necesario
            }
        } catch (RestClientException e) {
            // Verificar si el error es específico de HTTP
            if (e instanceof HttpStatusCodeException) {
                HttpStatusCodeException httpException = (HttpStatusCodeException) e;
                System.out.println("Error HTTP: " + httpException.getStatusCode().value());
            } else {
                System.out.println("Error al realizar la solicitud: " + e.getMessage());
            }
            return error;
        }
    }

    public static List<String> responseToList(ResponseEntity<String> response){
        // Crear una lista para almacenar los valores
        List<String> values = new ArrayList<>();
        try {
            // Parsear el JSON
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response.getBody());

            System.out.println(jsonObject);

            // Extraer los valores de las claves y añadirlos a la lista
            values.add(formatString((String) jsonObject.get("nombres")));
            values.add(formatString((String) jsonObject.get("apellidoPaterno")));
            values.add(formatString((String) jsonObject.get("apellidoMaterno")));
            values.add(formatString((String) jsonObject.get("numeroDocumento")));

            System.out.println(jsonObject);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return values;
    }


    public static String formatString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Lista de palabras que deben permanecer en minúsculas
        List<String> lowerCaseWords = Arrays.asList("de", "del", "la", "los", "las", "y", "da", "dos");

        String[] words = input.split("\\s+");
        StringBuilder formattedName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                // Capitaliza la primera palabra siempre
                if (i == 0) {
                    formattedName.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        formattedName.append(word.substring(1).toLowerCase());
                    }
                } else {
                    if (lowerCaseWords.contains(word.toLowerCase())) {
                        formattedName.append(word.toLowerCase());
                    } else {
                        formattedName.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) {
                            formattedName.append(word.substring(1).toLowerCase());
                        }
                    }
                }
                formattedName.append(" ");
            }
        }

        // Quitar el último espacio adicional
        return formattedName.toString().trim();
    }
}
