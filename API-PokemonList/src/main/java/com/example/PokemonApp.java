package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class PokemonApp {
    private static final String POKE_API_URL = "https://pokeapi.co/api/v2/pokemon?limit=10";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokemonApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Daftar list Pokemon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setVisible(true);

        
        new Thread(() -> {
            try {
                String pokemonNames = fetchPokemonNames();
                SwingUtilities.invokeLater(() -> textArea.setText(pokemonNames));
            } catch (IOException e) {
                handleFetchError(textArea);
            }
        }).start();
    }

    private static String fetchPokemonNames() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(POKE_API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ObjectMapper mapper = new ObjectMapper();
            PokemonResponse pokemonResponse = mapper.readValue(response.body().string(), PokemonResponse.class);

            StringBuilder names = new StringBuilder();
            for (Pokemon pokemon : pokemonResponse.getResults()) {
                names.append(pokemon.getName()).append("\n");
            }
            return names.toString();
        }
    }

    private static void handleFetchError(JTextArea textArea) {
        SwingUtilities.invokeLater(() -> textArea.setText("Failed to fetch Pokémon names"));
    }
}
