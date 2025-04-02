package com.msn.msncars.image;

import io.minio.StatObjectResponse;
import org.springframework.core.io.Resource;

public record Image(StatObjectResponse metadata, Resource data) { }
