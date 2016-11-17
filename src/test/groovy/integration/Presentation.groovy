package integration

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import pl.edu.agh.iosr.cdm.AcceptedProposal
import pl.edu.agh.iosr.cdm.Node
import pl.edu.agh.iosr.cdm.Proposal
import pl.edu.agh.iosr.utils.ApplicationEndpoints
import pl.edu.agh.iosr.utils.NodesProvider
import spock.lang.Specification

class Presentation extends Specification{

    def RestTemplate restTemplate = new RestTemplate()
    def Node first
    def Node second
    def Node third


    def setup() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        NodesProvider.getListOfApplicationNodes()
                .stream()
                .forEach({
            node -> restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class)
        })

        first = NodesProvider.get(0);
        second = NodesProvider.get(1);
        third = NodesProvider.get(2)
    }


    def testBasicPaxos(){
/*        Client   Proposer      Acceptor     Learner
        |         |          |  |  |       |  |
        X-------->|          |  |  |       |  |  Request
        |         X--------->|->|->|       |  |  Prepare(1)
        |         |<---------X--X--X       |  |  Promise(1,{Va,Vb,Vc})
        |         X--------->|->|->|       |  |  Accept!(1,Vn)
        |         |<---------X--X--X------>|->|  Accepted(1,Vn)
        |<---------------------------------X--X  Response
        |         |          |  |  |       |  |*/

        //lag jak 2 razy wyslemy ten sam key
        given:
        HttpEntity<?> entity = new HttpEntity<>();
        Long value = 123l;
        String params = "?key=abcd&value="+value
        String url = first.getNodeUrl()+ApplicationEndpoints.CLIENT_PROPOSE_URL+params;

        when:
        Proposal response = restTemplate.getForObject(url, Proposal.class);

        then:
        //retrieve
        String queryUrl = first.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL+params;
        Integer responseValue = restTemplate.getForObject(queryUrl, Integer.class);
        responseValue == value

        //check learners
        AcceptedProposal firstNodeProposal = restTemplate.getForObject(first.getNodeUrl()+ApplicationEndpoints.LERNER_URL+"/abcd", AcceptedProposal.class);
        firstNodeProposal.getValue() == value
        AcceptedProposal secondNodeProposal = restTemplate.getForObject(second.getNodeUrl()+ApplicationEndpoints.LERNER_URL+"/abcd", AcceptedProposal.class);
        secondNodeProposal.getValue() == value
        AcceptedProposal thirdNodeProposal = restTemplate.getForObject(first.getNodeUrl()+ApplicationEndpoints.LERNER_URL+"/abcd", AcceptedProposal.class);
        thirdNodeProposal.getValue() == value


    }
    def testShouldEveryNodeLearnValue() {
/*        given:
;
        HttpEntity<?> entity = new HttpEntity<>();

        //check if leader

        //Proposal response = restTemplate.getForObject(url, Proposal.class);*/

    }


}
