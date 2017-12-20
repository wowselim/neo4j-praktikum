import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;

public class NeoMain {
    public static void main(String[] args) throws IOException {
        // z.B. "C:/neo4jDatabases/graph.db" (mit Quotes!)
        File storeDir = new File(args[0]);
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);

        try (Transaction t = db.beginTx();
             ResourceIterator<Node> people = db.findNodes(Label.label("Person"))) {
            while (people.hasNext()) {
                Node car = people.next();
                System.out.println(car.getProperty("name"));
            }

            t.success();
        }

        db.shutdown();
    }
}
