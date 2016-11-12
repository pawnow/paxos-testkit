package pl.edu.agh.iosr.cdm;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Proposal {

    private Long id;
    private String key;
    private Integer value;
    private String server;
    private Integer highestAcceptedProposalId;

    public Proposal() {
    }

    public Proposal(Long id, String key, Integer value, String server, Integer highestAcceptedProposalId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.server = server;
        this.highestAcceptedProposalId = highestAcceptedProposalId;
    }
}
