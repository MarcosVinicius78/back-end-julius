package com.julius.julius.service.Scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.exception.NotFoundException;
import com.julius.julius.DTO.response.ProdutoScraperDTO;

@Service
public class ShopeeService {

    private static final String appIdSe = "18199430003"; // Substitua pelo seu appID real
    private static final String secretSe = "HB6T6RCEXPLBYXMT4ZGDD2PMUZE5DGII"; // Substitua pelo seu secret real

    private static final String appIdOmc = "18164950004"; // Substitua pelo seu appID real
    private static final String secretOmc = "APTSBAEACEBBD6ZQVJ6M2XFDVDDNV3NN"; // Substitua pelo seu secret real
    private String url = "https://open-api.affiliate.shopee.com.br/graphql"; // Substitua pela sua URL
                                                                             // real7

    public String fetchProductOffers(String url, Long site) {
        try {
            // Extrair código do produto
            String codigoProduto = extractCodeFromUrl(getFinalUrl(url));

            // Montar payload
            String payload = String.format(
                    "{\"query\":\"{productOfferV2(itemId: %s){nodes{productName price imageUrl productLink offerLink}}}\"}",
                    codigoProduto);

            // Obter cabeçalho de autorização baseado no site
            String authorizationHeader = generateAuthorizationHeader(site, payload);

            // Enviar requisição POST
            return sendPostRequest(this.url, payload, authorizationHeader);
        } catch (Exception e) {
            // Logar exceção
            
            return ""; // Retorno vazio pode ser substituído por uma exceção personalizada, se
                       // necessário
        }
    }

    private String generateAuthorizationHeader(Long site, String payload) {
        long timestamp = Instant.now().getEpochSecond();
        String appId, secret;

        // Configuração de credenciais com base no site
        if (site == 1L) {
            appId = appIdSe;
            secret = secretSe;
        } else if (site == 2L) {
            appId = appIdOmc;
            secret = secretOmc;
        } else {
            throw new IllegalArgumentException("Site inválido: " + site);
        }

        // Construir fator de assinatura
        String factor = appId + timestamp + payload + secret;
        String signature;
        try {
            signature = sha256(factor);
            return String.format("SHA256 Credential=%s, Timestamp=%d, Signature=%s", appId, timestamp, signature);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public ProdutoScraperDTO pegarInfoProdutosShopee(String response) {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject dataObject = jsonObject.getJSONObject("data");
        JSONObject productOfferV2 = dataObject.getJSONObject("productOfferV2");
        JSONArray nodesArray = productOfferV2.getJSONArray("nodes");

        String produtoNome = "";
        String preco = "";
        String imagemUrl = "";
        String link = "";
        // Loop through nodes and extract values
        for (int i = 0; i < nodesArray.length(); i++) {
            JSONObject node = nodesArray.getJSONObject(i);
            produtoNome = node.getString("productName");
            preco = "R$ " + node.getString("price");
            imagemUrl = node.getString("imageUrl");
            link = node.getString("offerLink");
        }
        return new ProdutoScraperDTO(produtoNome, preco.replace(".", ","), imagemUrl, link, link, "");
    }

    // Método para obter a URL final após redirecionamentos
    private String getFinalUrl(String urlString) throws IOException {
        String expandedUrl;
        try {
            URL urlGet = new URL(urlString);
            URLConnection connection = urlGet.openConnection();
            InputStream is = connection.getInputStream();
            expandedUrl = connection.getURL().toString();
            return expandedUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Método para extrair o código da URL final
    private String extractCodeFromUrl(String urlString) throws URISyntaxException {
        URI uri = new URI(urlString);
        String path = uri.getPath();
        // Procura pelo padrão "-i.{storeId}.{productId}" no final da URL
        Pattern pattern = Pattern.compile("-i\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            // Captura o código do produto (productId)
            return matcher.group(2); // O segundo grupo é o productId
        } else {
            Pattern pattern2 = Pattern.compile("/product/(\\d+)/(\\d+)");
            Matcher matcher2 = pattern2.matcher(path);

            if (matcher2.find()) {
                // Captura o código do produto (productId)
                return matcher2.group(2); // O segundo grupo é o productId
            }
        }

        return "";
    }

    private static String sendPostRequest(String urlString, String payload, String authorizationHeader)
            throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authorizationHeader);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (var in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private static String sha256(String base) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
