package com.julius.julius.service.Scraper.Amazon;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageProcessingService {

    // Diretório de upload
    private static final String UPLOAD_DIR = "/uploads/produtos-real";

    // Tamanho final da imagem
    private static final int TARGET_WIDTH = 652;
    private static final int TARGET_HEIGHT = 650;

    public String processImageFromUrl(String imageUrl) throws IOException {
        // Baixar e carregar a imagem
        BufferedImage originalImage = downloadImage(imageUrl);

        // Converter imagem para RGB corretamente
        BufferedImage rgbImage = convertToRgb(originalImage);

        // Tornar a imagem quadrada
        BufferedImage squareImage = makeSquareImage(rgbImage);

        // Redimensionar para o tamanho final
        BufferedImage resizedImage = resizeImage(squareImage, TARGET_WIDTH, TARGET_HEIGHT);

        // Salvar a imagem
        return saveImage(resizedImage);
    }

    // 1. Baixar e carregar imagem
    public BufferedImage downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configurar timeout para conexão e leitura
        connection.setConnectTimeout(5000); // 5 segundos
        connection.setReadTimeout(5000);    // 5 segundos
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true); // Seguir redirecionamentos automaticamente

        // Verificar código de resposta HTTP
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("Erro ao baixar imagem. Código HTTP: " + status);
        }

        // Tentar ler a imagem
        try (InputStream inputStream = connection.getInputStream();
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {

            BufferedImage image = ImageIO.read(bufferedStream);
            if (image == null) {
                throw new IOException("Formato de imagem não suportado ou imagem inválida: " + imageUrl);
            }

            return image;
        } catch (IOException e) {
            throw new IOException("Erro ao processar imagem: " + e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }
    // 2. Converter para RGB corretamente
    private BufferedImage convertToRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = rgbImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Caso ainda haja inconsistências, use ColorConvertOp
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
        op.filter(image, rgbImage);

        return rgbImage;
    }

    // 3. Tornar a imagem quadrada
    private BufferedImage makeSquareImage(BufferedImage image) {
        int size = Math.max(image.getWidth(), image.getHeight());
        BufferedImage squareImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = squareImage.createGraphics();
        g2d.setColor(Color.WHITE); // Fundo branco
        g2d.fillRect(0, 0, size, size);

        int xOffset = (size - image.getWidth()) / 2;
        int yOffset = (size - image.getHeight()) / 2;
        g2d.drawImage(image, xOffset, yOffset, null);
        g2d.dispose();

        return squareImage;
    }

    // 4. Redimensionar imagem
    private BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();

        return resizedImage;
    }

    // 5. Salvar imagem
    private String saveImage(BufferedImage image) throws IOException {
        // Garantir que o diretório existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gerar nome único para o arquivo
        String uniqueFileName = UUID.randomUUID().toString() + ".png";
        Path outputPath = uploadPath.resolve(uniqueFileName);

        // Salvar no disco
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            ImageIO.write(image, "png", outputStream);
        }

        return uniqueFileName;
    }
}

