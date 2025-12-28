package tr.edu.kalyon.nur.university.controller.dto;

public class ImageWithIdResponse {
    private Long id;
    private String contentType;
    private String filename;
    private String base64;

    public ImageWithIdResponse(Long id, String contentType, String filename, String base64) {
        this.id = id;
        this.contentType = contentType;
        this.filename = filename;
        this.base64 = base64;
    }

    public Long getId() { return id; }
    public String getContentType() { return contentType; }
    public String getFilename() { return filename; }
    public String getBase64() { return base64; }
}
