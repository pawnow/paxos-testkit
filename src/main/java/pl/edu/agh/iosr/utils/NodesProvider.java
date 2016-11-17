package pl.edu.agh.iosr.utils;

import com.google.common.collect.Lists;
import pl.edu.agh.iosr.cdm.Node;

import java.util.List;

public class NodesProvider {

    private static List<Node> nodes = Lists.newArrayList(
            new Node(1L, "http://localhost:8081"),
            new Node(2L, "http://localhost:8082"),
            new Node(3L, "http://localhost:8083")
    );

    public static List<Node> getListOfApplicationNodes(){
        return nodes;
    }

    public static Node get(int n){
        return nodes.get(n);
    }

}
