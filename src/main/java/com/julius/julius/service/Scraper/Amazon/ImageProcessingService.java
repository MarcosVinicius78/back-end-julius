package com.julius.julius.service.Scraper.Amazon;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageProcessingService {

    // Diretório de upload
    private static final String UPLOAD_DIR = "/uploads/produtos-real";

    public String processImageFromUrl(String imageUrl) throws IOException {
        // Baixar a imagem a partir da URL
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        // Valida se a imagem foi carregada corretamente
        if (originalImage == null) {
            throw new IOException("Não foi possível carregar a imagem da URL: " + imageUrl);
        }

        // Obter o maior lado da imagem
        int size = Math.max(originalImage.getWidth(), originalImage.getHeight());

        // Criar uma nova imagem quadrada com fundo branco
        BufferedImage squareImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = squareImage.createGraphics();
        graphics.setColor(Color.WHITE); // Fundo branco
        graphics.fillRect(0, 0, size, size);

        // Centralizar a imagem original
        int xOffset = (size - originalImage.getWidth()) / 2;
        int yOffset = (size - originalImage.getHeight()) / 2;
        graphics.drawImage(originalImage, xOffset, yOffset, null);
        graphics.dispose();

        // Gerar nome único para a imagem
        String uniqueFileName = UUID.randomUUID().toString() + ".png";
        String outputFilePath = UPLOAD_DIR + File.separator + uniqueFileName;

        // Criar diretório, se não existir
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        }

        // Salvar a imagem padronizada
        File outputFile = new File(outputFilePath);
        ImageIO.write(squareImage, "png", outputFile);

        // Retornar o nome da imagem salva
        return uniqueFileName;
    }
}
