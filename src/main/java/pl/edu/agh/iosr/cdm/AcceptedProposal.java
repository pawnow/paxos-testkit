package pl.edu.agh.iosr.cdm;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class AcceptedProposal {

    private Long id;
    private Integer value;
    private String server;
    private Integer highestAcceptedProposalId;
    private String key;

    public AcceptedProposal() {
    }

    public AcceptedProposal(Long id, Integer value, String server, Integer highestAcceptedProposalId, String key) {
        this.id = id;
        this.value = value;
        this.server = server;
        this.highestAcceptedProposalId = highestAcceptedProposalId;
        this.key = key;
    }

    public AcceptedProposal(Proposal proposal){
        id = proposal.getId();
        value = proposal.getValue();
        server = proposal.getServer();
        highestAcceptedProposalId = proposal.getHighestAcceptedProposalId();
        key = proposal.getKey();
    }
}
