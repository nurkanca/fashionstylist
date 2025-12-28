package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public class ImageBatchRequest {
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
