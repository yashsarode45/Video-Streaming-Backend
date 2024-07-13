package com.backend.videostreaming.services;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map uploadFile(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), Map.of(
                "folder", folder,
                "resource_type", "auto"
        ));
    }
}
