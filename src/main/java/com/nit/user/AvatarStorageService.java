
package com.nit.user;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarStorageService {

    private static final long MAX_SIZE = 2L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadDirectory;

    public AvatarStorageService(
            @Value("${app.avatar.upload-dir:uploads/avatars}")
            String uploadDirectory) {

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 2 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and WebP images are allowed.");
        }

        String extension = validateAndGetExtension(file);

        String filename = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(uploadDirectory);

            Path destination = uploadDirectory.resolve(filename).normalize();

            if (!destination.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("Invalid filename.");
            }

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }

            // Relative URL under the application's context path.
            return "uploads/avatars/" + filename;

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to save the profile image.", ex);
        }
    }

    private String validateAndGetExtension(MultipartFile file) {
        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? ""
                                : file.getOriginalFilename())
                .toLowerCase(Locale.ROOT);

        byte[] header = new byte[12];

        try (InputStream input = file.getInputStream()) {
            int count = input.read(header);

            if (count < 8) {
                throw new IllegalArgumentException("Invalid image file.");
            }

            boolean jpeg =
                    (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;

            boolean png =
                    (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;

            boolean webp =
                    count >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';

            if (jpeg && hasExtension(originalFilename, ".jpg", ".jpeg")) {
                return ".jpg";
            }

            if (png && hasExtension(originalFilename, ".png")) {
                return ".png";
            }

            if (webp && hasExtension(originalFilename, ".webp")) {
                return ".webp";
            }

            throw new IllegalArgumentException(
                    "File content and extension do not match a supported image.");
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Unable to read the image file.", ex);
        }
    }

    private boolean hasExtension(String filename, String... extensions) {
        for (String extension : extensions) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}