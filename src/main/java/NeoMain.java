import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;

import java.io.File;
import java.io.IOException;

public class NeoMain {
    public static void main(String[] args) throws IOException {
        // z.B. "C:/neo4jDatabases/graph.db" (mit Quotes!)
        File storeDir = new File(args[0]);
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);

        printWithStandardApi(db, "Bart Simpson");
        printWithStandardApi(db, "Abraham J. Simpson");

        printTreeWithTraversalFramework(db, "Bart Simpson");
        printTreeWithTraversalFramework(db, "Abraham J. Simpson");

        db.shutdown();
    }

    private static void printWithStandardApi(GraphDatabaseService db, String name) {
        System.out.printf("Printing tree of %s%n", name);
        try (Transaction t = db.beginTx();
             ResourceIterator<Node> people = db.findNodes(Label.label("Person"), "name", name)) {
            while (people.hasNext()) {
                Node target = people.next();
                traverse(target);
            }

            t.success();
        }
        System.out.println("-----");
    }

    private static void traverse(Node node) {
        System.out.println(node.getProperty("name"));
        Iterable<Relationship> relationships = node.getRelationships(Direction.INCOMING,
                RelationshipType.withName("fatherOf"),
                RelationshipType.withName("motherOf"));
        relationships.forEach(relationship -> traverse(relationship.getStartNode()));
    }

    private static void printTreeWithTraversalFramework(GraphDatabaseService db, String name) {
        System.out.printf("Printing tree of %s%n", name);
        try (Transaction t = db.beginTx();
             ResourceIterator<Node> target = db.findNodes(Label.label("Person"), "name", name)) {
            while (target.hasNext()) {
                Node startingNode = target.next();
                for (Node currentPerson : db.traversalDescription()
                        .depthFirst()
                        .relationships(RelationshipType.withName("fatherOf"), Direction.INCOMING)
                        .relationships(RelationshipType.withName("motherOf"), Direction.INCOMING)
                        .evaluator(Evaluators.toDepth(99))
                        .traverse(startingNode)
                        .nodes()) {
                    System.out.println(currentPerson.getProperty("name"));
                }
            }

            t.success();
        }
        System.out.println("-----");
    }
}
