package com.julius.julius.controller;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.ProdutoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";

    // Spoof the scraping by telling the page from where the request has been sent:
    private final String REFERRER = "https://www.google.com";

    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<ProdutoScraperDTO> getProduto(@RequestParam String url) {

        Response response;
        try {
            response = Jsoup.connect(url)
                    .ignoreContentType(false)
                    .userAgent(
                            "Mozilla/5.0 (compatible; 008/0.83; http://www.80legs.com/webcrawler.html) Gecko/2008032620")
                    .referrer("https://www.amazon.com.br/")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();

            Document doc = response.parse();

            String titulo = doc.getElementById("productTitle").text();

            String precoSalvo = "R$ ";

            String preco = doc.getElementsByClass("a-price-whole").first().text();
            precoSalvo += preco;
            preco = doc.getElementsByClass("a-price-fraction").first().text();
            precoSalvo += preco;

            Element imgElement = doc.select("div#imgTagWrapperId img[src]").first();
            String urlImagem = imgElement.attr("src");

            return ResponseEntity.ok().body(new ProdutoScraperDTO(titulo, precoSalvo, urlImagem));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.noContent().build();
        
        // Configurar opções
        // connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)
        // AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        // connection.header("Accept",
        // "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        // connection.header("Accept-Encoding", "gzip, deflate, br");
        // connection.header("Accept-Language", "en-US,en;q=0.5");
        // connection.header("Connection", "keep-alive");
        // connection.header("Referer", "https://www.google.com/");
        // connection.header("Upgrade-Insecure-Requests", "1");
        // connection.ignoreHttpErrors(true);
        // connection.ignoreContentType(true);

        // Execute a requisição
        // Connection.Response response =
        // connection.method(Connection.Method.GET).execute();

        // Obtenha o HTML da página

        // try {

        // WebDriverManager.firefoxdriver().setup();

        // // WebDriverManager.chromedriver().setup();

        // // System.setProperty("webdriver.edge.driver",
        // "chromedriver_win32/chromedriver");
        // // EdgeOptions options = new EdgeOptions();

        // FirefoxOptions options = new FirefoxOptions();
        // // ChromeOptions options = new ChromeOptions();
        // System.setProperty("webdriver.firefox.bin", "/usr/bin/firefox");
        // options.setBinary("/usr/bin/firefox");
        // options.addArguments("--headless");
        // options.addArguments("--no-sandbox");
        // options.addArguments("--disable-dev-shm-usage");
        // options.addArguments("window-size=800,600");
        // options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)
        // AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");

        // // WebDriver driver = new EdgeDriver(options);
        // // WebDriver driver = new ChromeDriver(options);
        // WebDriver driver = new FirefoxDriver(options);

        // driver.get(url);

        // WebElement valorElemetno =
        // driver.findElement(By.cssSelector("#productTitle"));

        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // return null;
    }

}
