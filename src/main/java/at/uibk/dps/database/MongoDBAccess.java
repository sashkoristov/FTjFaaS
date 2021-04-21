package at.uibk.dps.database;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.Block;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class MongoDBAccess {
    private static final long workflowExecutionId = System.currentTimeMillis();
    public static String PATH_TO_PROPERTIES = "database.properties";
    public static Block<Document> printBlock = new Block<Document>() {
        @Override
        public void apply(final Document document) {
            System.out.println(document.toJson());
        }
    };
    private static MongoClient mongoClient;
    private static MongoDBAccess mongoDBAccess;
    private static List<Document> entries = Collections.synchronizedList(new ArrayList<>());

    private MongoDBAccess() {
        // disable the logging for mongoDB on stdout
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger mongoLogger = loggerContext.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);

        Properties databaseFile = new Properties();
        try {
            databaseFile.load(new FileInputStream(PATH_TO_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String host = databaseFile.getProperty("host");
        final String username = databaseFile.getProperty("username");
        final String password = databaseFile.getProperty("password");
        final String database = databaseFile.getProperty("database");
        final int port = Integer.parseInt(databaseFile.getProperty("port"));

        MongoCredential sim = MongoCredential.createCredential(username, database, password.toCharArray());
        mongoClient = MongoClients.create
                (MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                        .credential(sim)
                        .build());
    }

    public static MongoClient getConnection() {
        if (mongoClient == null) {
            mongoDBAccess = new MongoDBAccess();
        }
        return mongoClient;
    }

    public static void saveLog(Event event, String functionId, String functionName, String functionType, Long RTT, boolean success, int memorySize, int loopCounter, long startTime, Type type) {
        // TODO add missing fields
        Document log = new Document("workflow_id", workflowExecutionId)
                .append("function_id", functionId)
                .append("functionName", functionName)
                .append("functionType", functionType)
                .append("Event", event.toString())
                .append("RTT", RTT)
                .append("success", success)
                .append("memorySize", memorySize)
                .append("loopCounter", loopCounter)
                .append("startTime", new Timestamp(startTime))
                .append("endTime", new Timestamp(startTime + RTT))
                .append("type", type.toString())
                .append("done", Boolean.FALSE); // flag used to update metadataDB
        // TODO add memory size
        entries.add(log);
    }

    public static long getLastEndDateOverall() {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        return entries.stream()
                .filter(d -> d.getLong("workflow_id") == workflowExecutionId
                        && d.containsKey("function_id"))
                .max(Comparator.comparing(d -> d.getDate("endTime")))
                .get()
                .getDate("endTime")
                .getTime();
    }

    public static long getLastEndDateOutOfLoop() {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        return entries.stream()
                .filter(d -> d.getLong("workflow_id") == workflowExecutionId
                        && d.getInteger("loopCounter") == -1
                        && d.containsKey("function_id"))
                .max(Comparator.comparing(d -> d.getDate("endTime")))
                .get()
                .getDate("endTime")
                .getTime();
    }

    public static long getLastEndDateOutOfLoopStored() {
        MongoClient client = getConnection();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("AFCL");
        MongoCollection<Document> dbCollection = mongoDatabase.getCollection("logs");
        return dbCollection.find(and(eq("workflow_id", workflowExecutionId), eq("loopCounter", -1)))
                .sort(descending("endTime"))
                .limit(1)
                .first()
                .getDate("endTime")
                .getTime();
    }

    public static long getLastEndDateInLoop() {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        return entries.stream()
                .filter(d -> d.getLong("workflow_id") == workflowExecutionId
                        && d.getInteger("loopCounter") != -1
                        && d.containsKey("function_id"))
                .max(Comparator.comparing(d -> d.getDate("endTime")))
                .get()
                .getDate("endTime")
                .getTime();
    }

    public static long getLastEndDateInLoopStored() {
        MongoClient client = getConnection();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("AFCL");
        MongoCollection<Document> dbCollection = mongoDatabase.getCollection("logs");
        return dbCollection.find(and(eq("workflow_id", workflowExecutionId), not(eq("loopCounter", -1))))
                .sort(descending("endTime"))
                .limit(1)
                .first()
                .getDate("endTime")
                .getTime();
    }

    public static FindIterable<Document> findNewEntries() {
        MongoClient client = getConnection();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("AFCL");
        MongoCollection<Document> dbCollection = mongoDatabase.getCollection("logs");
        return dbCollection.find(eq("done", Boolean.FALSE));
    }

    public static void addAllEntries() {
        MongoClient client = getConnection();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("AFCL");
        MongoCollection<Document> dbCollection = mongoDatabase.getCollection("logs");
        if (!entries.isEmpty()) {
            dbCollection.insertMany(entries);
        }
    }

    public static void close() {
        mongoClient.close();
    }

}
