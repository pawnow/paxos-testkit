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

class Presentation extends Specification {

    def RestTemplate restTemplate = new RestTemplate()
    def Node first
    def Node second
    def Node third


    def setup() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        NodesProvider.getListOfApplicationNodes()
                .stream()
                .forEach({
            node -> restTemplate.postForEntity(node.getNodeUrl() + ApplicationEndpoints.ONLINE_URL, params, String.class)
        })

        first = NodesProvider.get(0);
        second = NodesProvider.get(1);
        third = NodesProvider.get(2)
    }


    def testBasicPaxosLearnOneValue() {
/*        Client   Proposer      Acceptor     Learner
        |         |          |  |  |       |  |
        X-------->|          |  |  |       |  |  Request
        |         X--------->|->|->|       |  |  Prepare(1)
        |         |<---------X--X--X       |  |  Promise(1,{Va,Vb,Vc})
        |         X--------->|->|->|       |  |  Accept!(1,Vn)
        |         |<---------X--X--X------>|->|  Accepted(1,Vn)
        |<---------------------------------X--X  Response
        |         |          |  |  |       |  |*/

        given:
        HttpEntity<?> entity = new HttpEntity<>();
        Random r = new Random();
        Integer value = r.nextInt();
        Long valueLong = value;
        String key = "abck"+r.nextInt()
        String params = "?key="+key+"&value=" + value
        String url = first.getNodeUrl() + ApplicationEndpoints.CLIENT_PROPOSE_URL + params;

        when:
        Proposal response = restTemplate.getForObject(url, Proposal.class);

        then:
        //retrieve
        String queryUrl = first.getNodeUrl() + ApplicationEndpoints.CLIENT_RETRIEVE_URL + params;
        Integer responseValue = restTemplate.getForObject(queryUrl, Integer.class);
        responseValue == valueLong

        //check learners
        AcceptedProposal firstNodeProposal = restTemplate.getForObject(first.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        firstNodeProposal.getValue() == valueLong
        AcceptedProposal secondNodeProposal = restTemplate.getForObject(second.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        secondNodeProposal.getValue() == valueLong
        AcceptedProposal thirdNodeProposal = restTemplate.getForObject(third.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        thirdNodeProposal.getValue() == valueLong

    }


    def testNodeNotLeaderFailure() {

        given:
        HttpEntity<?> entity = new HttpEntity<>();
        Random r = new Random();
        Integer value = r.nextInt();
        Long valueLong = value;
        String key = "abck"+r.nextInt()
        String params = "?key="+key+"&value=" + value
        String url = first.getNodeUrl() + ApplicationEndpoints.CLIENT_PROPOSE_URL + params;
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<String, String>();


        when:
        HttpEntity<String> responseTurnOffline = restTemplate.postForEntity(third.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, paramsMap, String.class);
        Proposal response = restTemplate.getForObject(url, Proposal.class);

        then:
        //retrieve
        String queryUrl = first.getNodeUrl() + ApplicationEndpoints.CLIENT_RETRIEVE_URL + params;
        Integer responseValue = restTemplate.getForObject(queryUrl, Integer.class);
        responseValue == valueLong

        //check learners
        AcceptedProposal firstNodeProposal = restTemplate.getForObject(first.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        firstNodeProposal.getValue() == valueLong
        AcceptedProposal secondNodeProposal = restTemplate.getForObject(second.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        secondNodeProposal.getValue() == valueLong
      //  AcceptedProposal thirdNodeProposal = restTemplate.getForObject(first.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        //thirdNodeProposal.getValue() == valueLong/

    }

    def testLeaderFailure() {

        given:
        HttpEntity<?> entity = new HttpEntity<>();
        Random r = new Random();
        Integer value = r.nextInt();
        Long valueLong = value;
        String key = "abck"+r.nextInt()
        String params = "?key="+key+"&value=" + value
        String url = second.getNodeUrl() + ApplicationEndpoints.CLIENT_PROPOSE_URL + params;
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<String, String>();


        when:
        HttpEntity<String> responseTurnOffline = restTemplate.postForEntity(first.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, paramsMap, String.class);
        Proposal response = restTemplate.getForObject(url, Proposal.class);

        then:
        //retrieve
        String queryUrl = second.getNodeUrl() + ApplicationEndpoints.CLIENT_RETRIEVE_URL + params;
        Integer responseValue = restTemplate.getForObject(queryUrl, Integer.class);
        responseValue == valueLong

        //check learners
        //AcceptedProposal firstNodeProposal = restTemplate.getForObject(first.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        //firstNodeProposal.getValue() == valueLong
        AcceptedProposal secondNodeProposal = restTemplate.getForObject(second.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        secondNodeProposal.getValue() == valueLong
        AcceptedProposal thirdNodeProposal = restTemplate.getForObject(third.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        thirdNodeProposal.getValue() == valueLong

    }


    def testAcceptorResponseFailure() {
/*
        Client   Proposer      Acceptor     Learner
        |         |          |  |  |       |  |
        X-------->|          |  |  |       |  |  Request
        |         X--------->|->|->|       |  |  Prepare(1)
        |         |          |  |  !       |  |  !! FAIL !!
        |         |<---------X--X          |  |  Promise(1,{null,null})
        |         X--------->|->|          |  |  Accept!(1,V)
        |         |<---------X--X--------->|->|  Accepted(1,V)
        |<---------------------------------X--X  Response
        |         |          |  |          |  |*/

        given:
        HttpEntity<?> entity = new HttpEntity<>();
        Random r = new Random();
        Integer value = r.nextInt();
        Long valueLong = value;
        String key = "abck"+r.nextInt()
        String params = "?key="+key+"&value=" + value
        String url = second.getNodeUrl() + ApplicationEndpoints.CLIENT_PROPOSE_URL + params;
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<String, String>();


        when:
        Proposal response = restTemplate.getForObject(url, Proposal.class);
        //ubić akceptora 1
        value = r.nextInt();
        valueLong = value;
        key = "abck"+r.nextInt()
        params = "?key="+key+"&value=" + value
        url = second.getNodeUrl() + ApplicationEndpoints.CLIENT_PROPOSE_URL + params;
        response = restTemplate.getForObject(url, Proposal.class);



        then:
        //retrieve
        String queryUrl = second.getNodeUrl() + ApplicationEndpoints.CLIENT_RETRIEVE_URL + params;
        Integer responseValue = restTemplate.getForObject(queryUrl, Integer.class);
        responseValue == valueLong

        //check learners
        //AcceptedProposal firstNodeProposal = restTemplate.getForObject(first.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        //firstNodeProposal.getValue() == valueLong
        AcceptedProposal secondNodeProposal = restTemplate.getForObject(second.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        secondNodeProposal.getValue() == valueLong
        AcceptedProposal thirdNodeProposal = restTemplate.getForObject(third.getNodeUrl() + ApplicationEndpoints.LERNER_URL + "/"+key, AcceptedProposal.class);
        thirdNodeProposal.getValue() == valueLong

    }
}
