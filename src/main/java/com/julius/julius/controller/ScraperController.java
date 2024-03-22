package com.julius.julius.controller;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.models.Produto;
import com.julius.julius.service.ProdutoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final ProdutoService produtoService;

    @GetMapping
    public ProdutoScraperDTO getProduto(@RequestParam String url) {

        try {

            System.setProperty("webdriver.edge.driver", "chromedriver_win32/msedgedriver.exe");
            EdgeOptions options = new EdgeOptions();

            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("window-size=800,600");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");

            WebDriver driver = new EdgeDriver(options);

            driver.get(url);

            WebElement valorElemetno = driver.findElement(By.cssSelector("#productTitle"));

            String titulo = valorElemetno.getText();

            String precoSalvo = "R$ ";
            
            valorElemetno = driver.findElement(By.className("a-price-whole"));
            String preco = valorElemetno.getText();
            precoSalvo += preco;
            valorElemetno = driver.findElement(By.className("a-price-fraction"));
            precoSalvo += ","+ valorElemetno.getText();

            valorElemetno = driver.findElement(By.id("landingImage"));
            String urlImagem = valorElemetno.getAttribute("src");
            
            driver.quit();;

            return new ProdutoScraperDTO(titulo, precoSalvo,urlImagem);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}