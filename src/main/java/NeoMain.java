import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NeoMain {
    enum Country implements Label {
        JAPAN, GERMANY
    }

    enum CarType implements Label {
        COUPE, WAGON, SUV
    }

    enum ServiceTypes implements RelationshipType {
        PRODUCES, REPAIRS
    }

    public static void main(String[] args) throws IOException {
        File storeDir = new File("db.graphdb");
        removeDir(storeDir);
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);

        try (Transaction t = db.beginTx()) {
            Node honda = db.createNode();
            honda.setProperty("name", "Honda");
            honda.addLabel(Country.JAPAN);

            Node s2000 = db.createNode();
            s2000.setProperty("name", "Honda S2000");
            s2000.addLabel(CarType.COUPE);

            Relationship relationship = honda.createRelationshipTo(
                    s2000, ServiceTypes.PRODUCES
            );
            relationship.setProperty("since", 1999);

            t.success();
        }

        try(Transaction t = db.beginTx();
        ResourceIterator<Node> cars = db.findNodes(CarType.COUPE)) {
            while(cars.hasNext()) {
                Node car = cars.next();
                System.out.println(car.getProperty("name"));
            }

            t.success();
        }

        db.shutdown();
    }

    private static void removeDir(File f) {
        try {
            Files.walk(f.toPath())
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
