package org.getaviz.generator.tests.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.*;
import org.getaviz.generator.mockups.ABAPmock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.types.Node;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IntegrationTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ABAPmock mockUp = new ABAPmock();
    private static NodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    private static String exportString;

    @BeforeAll
    static void setup() {

        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
        mockUp.loadProperties("CityBankTest.properties");

        nodeRepository = new NodeRepository();
        nodeRepository.loadNodesWithRelation(SAPRelationLabels.CONTAINS);
        nodeRepository.loadNodesWithRelation(SAPRelationLabels.TYPEOF);

        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, config);
        aCityCreator.createRepositoryFromNodeRepository(nodeRepository);

        ACityLayouter aCityLayouter = new ACityLayouter(aCityRepository, config);
        aCityLayouter.layoutRepository();

        ACityDesigner designer = new ACityDesigner(aCityRepository, config);
        designer.designRepository();

        ACityAFrameExporter aCityAFrameExporter = new ACityAFrameExporter(aCityRepository, config);
        exportString = aCityAFrameExporter.createAFrameExportFile();
    }

    @AfterAll
    static void close() {
        mockUp.close();
    }

    @Test
    public void nodesLoaded(){
        Collection<Node> allNodes = nodeRepository.getNodes();
        assertEquals(340, allNodes.size());
    }


    @Test
    public void export(){
        assertNotEquals("", exportString);

        System.out.println(exportString);
    }


}
