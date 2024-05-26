package com.julius.julius.service.Scraper.Amazon;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.exception.NotFoundException;
import com.julius.julius.DTO.response.ProdutoScraperDTO;

@Service
public class AmazonService {

    private static final String HOST = "webservices.amazon.com.br";
    private static final String URI_PATH = "/paapi5/getitems";
    @Value("${aws.accessKeyId}")
    private String ACCESS_KEY;

    @Value("${aws.secretAccessKey}")
    private String SECRET_KEY;
    private static final String REGION = "us-east-1";

    private static final Pattern ASIN_PATTERN = Pattern.compile("/([A-Z0-9]{10})(?:[/?]|$)");

    public String getProdutoAmazon(String codigoProduto) {

        try {
            String requestPayload = "{"
                    + " \"ItemIds\": ["
                    + "  \"" + codigoProduto + "\""
                    + " ],"
                    + " \"Resources\": ["
                    + "  \"Images.Primary.Medium\","
                    + "  \"ItemInfo.Title\","
                    + "  \"Offers.Listings.Price\""
                    + " ],"
                    + " \"PartnerTag\": \"sergipeofer0e-20\","
                    + " \"PartnerType\": \"Associates\","
                    + " \"Marketplace\": \"www.amazon.com.br\""
                    + "}";

            TreeMap<String, String> headers = new TreeMap<String, String>();
            headers.put("host", HOST);
            headers.put("content-type", "application/json; charset=UTF-8");
            headers.put("x-amz-target", "com.amazon.paapi5.v1.ProductAdvertisingAPIv1.GetItems");
            headers.put("content-encoding", "amz-1.0");

            AWSV4Auth awsv4Auth = new AWSV4Auth.Builder(ACCESS_KEY, SECRET_KEY)
                    .path(URI_PATH)
                    .region(REGION)
                    .service("ProductAdvertisingAPI")
                    .httpMethodName("POST")
                    .headers(headers)
                    .payload(requestPayload)
                    .build();

            HttpClient client = (HttpClient) HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost("https://" + HOST + URI_PATH);
            httpPost.setEntity(new StringEntity(requestPayload));
            Map<String, String> header = awsv4Auth.getHeaders();
            for (Map.Entry<String, String> entrySet : header.entrySet()) {
                httpPost.addHeader(entrySet.getKey(), entrySet.getValue());
            }

            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("Successfully received response from Product Advertising API.");
                return jsonResponse;
            } else {
                JSONObject json = new JSONObject(jsonResponse);
                if (json.has("Errors")) {
                    JSONArray errorArray = json.getJSONArray("Errors");
                    for (int i = 0; i < errorArray.length(); i++) {
                        JSONObject e = errorArray.getJSONObject(i);
                        System.out.println("Error Code: " + e.get("Code") + ", Message: " + e.get("Message"));
                    }
                } else {
                    System.out.println(
                            "Error Code: InternalFailure, Message: The request processing has failed because of an unknown error, exception or failure. Please retry again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public String pegarCodigoProdutoAmazon(String url) {

        String expandedUrl = url;

        if (url.contains("amz")) {
            try {
                URL urlGet = new URL(url);
                URLConnection connection = urlGet.openConnection();
                InputStream is = connection.getInputStream();
                expandedUrl = connection.getURL().toString();
            } catch (Exception e) {
                throw new NotFoundException("Não Foi Possivel acessar o Link");
            }
        }

        String regexDp = "/dp/(\\w+)";
        Pattern pattern = Pattern.compile(regexDp);
        Matcher matcher = pattern.matcher(expandedUrl);
        
        String codigoProduto = null;
        
        if (matcher.find()) {
            codigoProduto = matcher.group(1);
        }else{
            String regexGp = "\"/([A-Z0-9]{10})(?:[/?]|$)\"";
            codigoProduto = extractAsin(expandedUrl);
        }
        return codigoProduto;   
    }

    public String extractAsin(String url) {
        Matcher matcher = ASIN_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("No ASIN found in the provided URL");
    }

    public ProdutoScraperDTO montarProdutoAmazon(String jsonResponse,String url) {

        JSONObject jsonObject = new JSONObject(jsonResponse);

        JSONObject itemsResult = jsonObject.getJSONObject("ItemsResult");

        JSONArray itemsArray = itemsResult.getJSONArray("Items");

        // Vamos considerar apenas o primeiro item do array
        JSONObject firstItem = itemsArray.getJSONObject(0);

        JSONObject itemInfo = firstItem.getJSONObject("ItemInfo");

        JSONObject titleObject = itemInfo.getJSONObject("Title");

        String displayValue = titleObject.getString("DisplayValue");

        // pegar url da imagem

        JSONObject imagesResult = firstItem.getJSONObject("Images");

        JSONObject prymaryResult = imagesResult.getJSONObject("Primary");

        JSONObject mediumResult = prymaryResult.getJSONObject("Medium");

        String urlImagem = mediumResult.getString("URL");

        // pegar preço do produto

        JSONObject offersResult = firstItem.getJSONObject("Offers");

        JSONArray listingsArray = offersResult.getJSONArray("Listings");

        JSONObject firstItemPrice = listingsArray.getJSONObject(0);

        JSONObject priceResult = firstItemPrice.getJSONObject("Price");

        String amount = priceResult.getString("DisplayAmount");

        return new ProdutoScraperDTO(displayValue, amount, urlImagem,url,"");
    }
    
}
