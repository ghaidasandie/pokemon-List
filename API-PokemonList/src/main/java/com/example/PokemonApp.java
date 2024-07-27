package com.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class PokemonApp {
    private static final String POKE_API_URL = "https://pokeapi.co/api/v2/pokemon?limit=30&offset=0";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokemonApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Daftar List Pokemon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JList<String> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frame.add(new JScrollPane(list), BorderLayout.CENTER);
        frame.setVisible(true);

        new Thread(() -> {
            try {
                List<String> pokemonNames = fetchPokemonNames();
                SwingUtilities.invokeLater(() -> {
                    list.setListData(pokemonNames.toArray(new String[0]));
                });
            } catch (IOException e) {
                handleFetchError(frame);
            }
        }).start();

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedPokemon = list.getSelectedValue();
                    if (selectedPokemon != null) {
                        showPokemonDetails(selectedPokemon);
                    }
                }
            }
        });
    }

    private static List<String> fetchPokemonNames() throws IOException {
        Request request = new Request.Builder().url(POKE_API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            PokemonResponse pokemonResponse = mapper.readValue(response.body().string(), PokemonResponse.class);

            List<String> names = new ArrayList<>();
            for (Pokemon pokemon : pokemonResponse.getResults()) {
                names.add(pokemon.getName());
            }

            return names;
        }
    }

    private static void showPokemonDetails(String name) {
        new Thread(() -> {
            try {
                String details = fetchPokemonDetails(name);
                int id = fetchPokemonId(name);
                ImageIcon pokemonImage = fetchPokemonImage(id);
                SwingUtilities.invokeLater(() -> {
                    JFrame detailFrame = new JFrame("Detail Pokemon: " + name);
                    detailFrame.setSize(300, 400);
                    JTextArea detailTextArea = new JTextArea(details);
                    JLabel imageLabel = new JLabel(pokemonImage);
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(imageLabel, BorderLayout.NORTH);
                    panel.add(new JScrollPane(detailTextArea), BorderLayout.CENTER);
                    detailFrame.add(panel);
                    detailFrame.setVisible(true);
                });
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String fetchPokemonDetails(String name) throws IOException {
        String url = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            PokemonDetail pokemonDetail = mapper.readValue(response.body().string(), PokemonDetail.class);

            StringBuilder details = new StringBuilder();
            details.append("Name: ").append(pokemonDetail.getName()).append("\n");
            details.append("Type: ");
            for (Type type : pokemonDetail.getTypes()) {
                details.append(type.getType().getName()).append(" ");
            }
            details.append("\n");
            details.append("Height: ").append(pokemonDetail.getHeight() * 10).append(" cm\n");
            details.append("Weight: ").append(pokemonDetail.getWeight() / 10.0).append(" kg\n");

            return details.toString();
        }
    }

    private static int fetchPokemonId(String name) throws IOException {
        String url = "https://pokeapi.co/api/v2/pokemon-species/" + name.toLowerCase();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            PokemonDetail pokemonDetail = mapper.readValue(response.body().string(), PokemonDetail.class);

            return pokemonDetail.getId(); 
        }
    }

    
    private static ImageIcon fetchPokemonImage(int id) throws IOException, URISyntaxException {
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
        System.out.println("Fetching image from URL: " + url); 
        
        URI uri = new URI(url);
        URL urlObj = uri.toURL();
        BufferedImage img = ImageIO.read(urlObj);
        Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH); 
        return new ImageIcon(scaledImg);
    }

    private static void handleFetchError(JFrame frame) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Failed to fetch Pokémon names"));
    }
}
