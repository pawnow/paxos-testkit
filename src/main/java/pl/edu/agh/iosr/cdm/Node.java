package pl.edu.agh.iosr.cdm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Node {

    private Long id;

    private String nodeUrl;

    public Node() {
    }
    public Node(Long id, String nodeUrl) {
        this.id = id;
        this.nodeUrl = nodeUrl;
    }
}
