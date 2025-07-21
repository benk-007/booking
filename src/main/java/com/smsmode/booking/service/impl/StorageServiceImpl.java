/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.service.impl;

import com.smsmode.booking.model.IdentityDocumentModel;
import com.smsmode.booking.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * TODO: add your documentation
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 28 Apr 2025</p>
 */
@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    @Value("${file.upload.identity-document-image}")
    private String imageUploadPath;

    public String storeFile(String path, InputStream inputStream) {
        Path originalFilePath = Paths.get(path);
        try {
            if (!Files.exists(originalFilePath)) {
                Files.createDirectories(originalFilePath.getParent());
            }
            Files.copy(inputStream, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
            return originalFilePath.getFileName().toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void deleteFile(String path) {
        File file = new File(path);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("File: '{}' delete successfully.", path);
            } else {
                log.warn("File: '{}' deletion failed.", path);
            }
        } else {
            log.warn("File: '{}' does not exists.", path);
        }
    }

    @Override
    public String generateDocumentPath(IdentityDocumentModel identityDocument) {
        String extension = identityDocument.getFileName().substring(identityDocument.getFileName().lastIndexOf("."));
        return this.imageUploadPath
                .replace(":guestId", identityDocument.getGuest().getId())
                .concat("/").concat(identityDocument.getId()).concat(extension);
    }
}
