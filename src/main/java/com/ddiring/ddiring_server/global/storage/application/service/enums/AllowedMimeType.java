package com.ddiring.ddiring_server.global.storage.application.service.enums;

import java.util.Set;

public class AllowedMimeType {

    private AllowedMimeType(){
    }

    public static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg",
            "image/jpeg",
            "image/png",
            "image/webp"
    );
}

