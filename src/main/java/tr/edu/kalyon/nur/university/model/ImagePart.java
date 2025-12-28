package tr.edu.kalyon.nur.university.model;

import org.springframework.http.MediaType;

public record ImagePart(byte[] bytes, String filename, MediaType mediaType) {}
