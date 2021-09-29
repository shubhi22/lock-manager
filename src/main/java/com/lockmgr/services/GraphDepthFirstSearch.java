package com.lockmgr.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class GraphDepthFirstSearch {
    private Set<String> visited = new HashSet<>();
    private List<List<String>> cycles = new ArrayList<>();
    private final ConcurrentMap<String, Set<String>> adjacencyList;

    public GraphDepthFirstSearch(ConcurrentMap<String, Set<String>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    void start() {
        for (String client : adjacencyList.keySet()) {
            if (!visited.contains(client)) {
                visit(client, new ArrayList<>());
            }
        }
    }

    List<List<String>> getCycles() {
        return cycles;
    }

    Set<String> getVisited() {
        return visited;
    }

    private void visit(String node, List<String> path) {
        visited.add(node);
        path.add(node);

        if (adjacencyList.containsKey(node)) {
            for (String neighbour : adjacencyList.get(node)) {
                if (!visited.contains(neighbour)) {
                    visit(neighbour, new ArrayList<>(path));
                } else {
                    if (path.contains(neighbour)) {
                        cycles.add(getCycleFromPath(path, neighbour));
                    }
                }
            }
        }
    }

    private List<String> getCycleFromPath(List<String> path, String target) {
        return path.subList(path.indexOf(target), path.size());
    }

}
